/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 *
 * CRISIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CRISIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CRISIS.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.crisis_economics.abm.household;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.DoubleEmploymentException;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.fund.FundInvestmentDecisionRule;
import eu.crisis_economics.abm.fund.FundInvestor;
import eu.crisis_economics.abm.household.plugins.HouseholdConsumptionFunction;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsInstrument;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

public final class ExogenousHousehold
   extends DepositingHousehold
   implements Employee, GoodsBuyer, ConsumerHousehold, FundInvestor {
   
   private Fund
      fund;
   
   private SimpleLabourMarket
      labourMarket;
   
   private SimpleGoodsMarket
      goodsMarket;
   
   private GoodsRepository
      durableGoodsRepository;
   
   private List<Labour>
      employedLabour;
   
   private final TimeseriesParameter<Double>
      deposits,
      labourSupply,
      wageAskPrice,
      consumptionBudget;
   
   private FundInvestmentDecisionRule
      fundInvestmentDecisionRule;
   
   private HouseholdConsumptionFunction
      consumptionFunction;
   
   private SimpleDomesticGoodsConsumer
      domesticGoodsConsumer;
   
   @Inject
   public ExogenousHousehold(
   @Assisted
      final DepositHolder depositHolder,
   @Named("HOUSEHOLD_INITIAL_CASH_ENDOWMENT")
      final double initialCashEndowment,
      final SimpleLabourMarket labourMarket,
      final SimpleGoodsMarket goodsMarket,
   @Named("EXOGENOUS_HOUSEHOLD_DEPOSITS_TIMESERIES")
      final TimeseriesParameter<Double> deposits,
   @Named("EXOGENOUS_HOUSEHOLD_LABOUR_SUPPLY_TIMESERIES")
      final TimeseriesParameter<Double> labourSupply,
   @Named("EXOGENOUS_HOUSEHOLD_WAGE_ASK_PRICE_TIMESERIES")
      final TimeseriesParameter<Double> wageBidPrice,
   @Named("EXOGENOUS_HOUSEHOLD_CONSUMPTION_BUDGET_TIMESERIES")
      final TimeseriesParameter<Double> consumptionBudget,
   @Named("EXOGENOUS_HOUSEHOLD_CONSUMPTION_FUNCTION")
      final HouseholdConsumptionFunction consumptionFunction
      ) {
      super(depositHolder);
      this.labourMarket = Preconditions.checkNotNull(labourMarket);
      this.goodsMarket = Preconditions.checkNotNull(goodsMarket);
      this.durableGoodsRepository = new DurableGoodsRepository(goodsMarket.getGoodsClassifier());
      this.employedLabour = new LinkedList<Labour>();
      this.deposits = Preconditions.checkNotNull(deposits);
      this.labourSupply = Preconditions.checkNotNull(labourSupply);
      this.wageAskPrice = Preconditions.checkNotNull(wageBidPrice);
      this.consumptionBudget = Preconditions.checkNotNull(consumptionBudget);
      this.consumptionFunction = Preconditions.checkNotNull(consumptionFunction);
      this.domesticGoodsConsumer = new SimpleDomesticGoodsConsumer(this);
      
      super.debit(Math.max(initialCashEndowment, 0.));
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "considerDeposits", NamedEventOrderings.DEPOSIT_PAYMENT);
      Simulation.repeat(
         this, "fundContributionDecision", NamedEventOrderings.HOUSEHOLD_FUND_PAYMENTS);
      Simulation.repeat(this, "offerLabourToMarket", NamedEventOrderings.HIRE_WORKERS);
      Simulation.repeat(this, "considerConsumption", NamedEventOrderings.CONSUMPTION);
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void considerDeposits() {
      final double
         currentDepositValue = getDepositValue(),
         targetDepositValue = Math.max(0., deposits.get()),
         change = targetDepositValue - currentDepositValue;
      if(change > 0)
         super.debit(change);
      else
         try {
            super.credit(change);
         } catch (final InsufficientFundsException neverThrows) {
            // Failed
         }
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void fundContributionDecision() {
      if(fund == null || fundInvestmentDecisionRule == null)
         return;
      final double
         change = accept(fundInvestmentDecisionRule);
      if(change == 0.)
         return;
      if(change > 0)
         try {
            fund.invest(this, change);
         } catch (final InsufficientFundsException e) {                       // Failed
            System.err.println(
               "ExogenousHousehold: failed to invest " + change + " in funds. The value of "
             + "fund investments is " + fund.getBalance(this) + ". No further "
             + "action will be taken on this investment."
             );
            System.err.flush();                                               // Abandon
         }
      else
         try {
            fund.requestWithdrawal(this, -change);
         } catch (final InsufficientFundsException e) {                       // Failed
            System.err.println(
               "ExogenousHousehold: failed to withdraw " + (-change) + " from funds. The value of "
             + "fund investments is " + fund.getBalance(this) + ". No further "
             + "action will be taken on this withdrawal."
             );
            System.err.flush();                                               // Abandon
         }
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void offerLabourToMarket() {
      if(getLabourAmountEmployed() > 0.) {
         System.err.println(
            "ExogenousHousehold: a labour contract is outstanding from the last business cycle. "
          + "This behaviour is not expected in the current implementation. Continuing.");
         System.err.flush();                                                  // Not Expected
      }
      
      final double
         labourToOfferToMarket = getMaximumLabourSupply() - getLabourAmountEmployed(),
         askPrice = wageAskPrice.get();
      
      if(labourToOfferToMarket > 0.) {
         final String errMsg =
            "ExogenousHousehold: a household failed to submit a labour market sale order. "
          + "Ask price: " + askPrice + ", " + "labour amount offered to market: " 
          + labourToOfferToMarket + ". No further action will be taken on this order.";
         try {
            labourMarket.addOrder(this, 1, labourToOfferToMarket, askPrice);
         }
         catch (final AllocationException e) {                                // Failed
            System.err.println(errMsg + " " + e);
            System.err.flush();                                               // Abandon
         }
         catch(final OrderException e) {                                      // Failed
            System.err.println(errMsg + " " +e);
            System.err.flush();                                               // Abandon
         }
      }
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void considerConsumption() {
      
      // Create a map of keyed goods with fixed iteration order
      final List<Double>
         worstCaseUnitPrices = new ArrayList<Double>();
      final List<String>
         availableGoodsIntruments = new ArrayList<String>();
      {
      final List<String>
         goodsTypesToConsiderIfAvailable = domesticGoodsConsumer.getGoodsTypesConsidered();
      for(final String goodsType : domesticGoodsConsumer.getGoodsTypesConsidered())
         if(goodsMarket.hasInstrument(goodsType))
            availableGoodsIntruments.add(goodsType);
      }
      if(availableGoodsIntruments.isEmpty())
         return;                                                           // No goods to buy
      
      for(final String goodsType : availableGoodsIntruments) {
         final SimpleGoodsInstrument
            instrument = goodsMarket.getInstrument(goodsType);
         final double
            price = instrument.getWorstAskPriceAmongSellers();
         worstCaseUnitPrices.add(price);
      }
      
      // Select the consumption budget
      final double
         budget = Math.max(0., consumptionBudget.get());
      
      // Select which goods types to order
      final Double[]
         goodsDemandsByType = consumptionFunction.calculateConsumption(
            budget, worstCaseUnitPrices.toArray(new Double[0]));
      
      assert worstCaseUnitPrices.size() == availableGoodsIntruments.size();
      
      for(int i = 0; i< availableGoodsIntruments.size(); ++i) {
         final double
            amountDesired = goodsDemandsByType[i],
            expectedUnitPrice = worstCaseUnitPrices.get(i);
         final String
            goodsType = availableGoodsIntruments.get(i);
         if(amountDesired <= 0.)
            continue;                                                         // Unwanted
         final String errMsg =
            "ExogenousHousehold: a household failed to submit a goods market sale order. "
          + "Bid price: " + expectedUnitPrice + ", " + "goods quantity desired: " 
          + amountDesired + ". No further action will be taken on this order.";
         try {
            goodsMarket.addOrder(this, goodsType, -amountDesired, expectedUnitPrice);
         }
         catch(final AllocationException e) {                                 // Failed
             System.err.println(errMsg + " " + e);
             System.err.flush();                                              // Abandon
         }
         catch(final OrderException e) {                                      // Failed
             System.err.println(errMsg + " " + e);
             System.err.flush();                                              // Abandon
         }
      }
   }
   
   /**
     * All credits for this implementation are sources from exogenous funds.
     */
   @Override
   public double credit(final double amount)
      throws InsufficientFundsException {
      if(amount < 0.) return 0.;
      return amount;
   }
   
   /**
     * All debits for this implementation are stored exogenously.
     */
   @Override
   public void debit(final double amount) {
      return;
   }
   
   /**
     * This agent has no requirement for cash injections as it has access to 
     * unlimited exogenous funds.
     */
   @Override
   public void cashFlowInjection(double amount) {
      return;
   }
   
   @Override
   public double calculateAllocatedCashFromActiveOrders() {
      return 0;
   }
   
   private double
      allocatedExogenousFunds;
   
   @Override
   public double allocateCash(double positiveAmount) {
      if(positiveAmount < 0.) return 0.;
      allocatedExogenousFunds += positiveAmount;
      return positiveAmount;
   }
   
   @Override
   public double disallocateCash(double positiveAmount) {
      if(positiveAmount < 0.) return 0.;
      positiveAmount = Math.min(positiveAmount, allocatedExogenousFunds);
      allocatedExogenousFunds -= positiveAmount;
      allocatedExogenousFunds = Math.max(allocatedExogenousFunds, 0.);
      return positiveAmount;
   }
   
   @Override
   public double getAllocatedCash() {
      return allocatedExogenousFunds;
   }
   
   /**
     * This {@link Agent} has access to unlimited exogenous funds. It is therefor
     * assumed at an unlimited amount of funding is unallocated at all times.
     */
   @Override
   public double getUnallocatedCash() {
      return Double.MAX_VALUE;
   }
   
   @Override
   public GoodsRepository getGoodsRepository() {
      return durableGoodsRepository;
   }
   
   @Override
   public void registerIncomingTradeVolume(
      String goodsType,
      double goodsAmount,
      double unitPricePaid
      ) {
      // No action.
   }
   
   @Override
   public void startContract(final Labour labour) throws DoubleEmploymentException {
      if(labour == null)
         return;
      if(employedLabour.contains(labour))
         throw new DoubleEmploymentException();
      else employedLabour.add(labour);
   }
   
   @Override
   public void endContract(final Labour labour) {
      if(labour == null)
         return;
      employedLabour.remove(labour);
   }
   
   @Override
   public double getLabourAmountEmployed() {
      double result = 0.;
      for(final Labour labour : employedLabour)
         result += labour.getQuantity();
      return result;
   }
   
   @Override
   public double getMaximumLabourSupply() {
      return labourSupply.get();
   }
   
   @Override
   public double getLabourAmountUnemployed() {
      return getMaximumLabourSupply() - getLabourAmountEmployed();
   }
   
   @Override
   public void allocateLabour(double amount) throws AllocationException {
      // No action
   }
   
   @Override
   public void disallocateLabour(final double amount) throws IllegalArgumentException {
      // No action
   }
   
   @Override
   public void notifyOutgoingEmployment(
      final double labourEmployed,
      final double wagePerUnitLabour) {
      // No action.
   }
   
   @Override
   public void notifyIncomingWageOrBenefit(
      final double totalPayment) {
      // No action.
   }
   
   @Override
   public double getConsumptionBudget() {
      return consumptionBudget.get();
   }
   
   // FundInvestor interface
   
   /**
     * It is not permitted to change the decision rules of an {@link ExogenousHousehold}.
     * This implementation of {@link #setFundInvestmentTargetDecisionRule(AgentOperation)}
     * does nothing and cannot fail.
     */
   @Override
   public void setFundInvestmentTargetDecisionRule(
      final FundInvestmentDecisionRule fundInvestmentDecisionRule) {
      this.fundInvestmentDecisionRule = fundInvestmentDecisionRule;
   }
   
   @Override
   public void setFund(Fund fund) {
      this.fund = fund;
   }
   
   @Override
   public Fund getFund() {
      return fund;
   }
   
   // DomesticGoodsConsumer
   
   public void addGoodToConsider(final String type) {
      domesticGoodsConsumer.addGoodToConsider(type);
   }
   
   public void addGoodsToConsider(final List<String> types) {
      domesticGoodsConsumer.addGoodsToConsider(types);
   }
   
   public void removeGoodToConsider(final String type) {
      domesticGoodsConsumer.removeGoodToConsider(type);
   }
   
   public void removeGoodsToConsider(final List<String> types) {
      domesticGoodsConsumer.removeGoodsToConsider(types);
   }
   
   public List<String> getGoodsTypesConsidered() {
      return domesticGoodsConsumer.getGoodsTypesConsidered();
   }
}
