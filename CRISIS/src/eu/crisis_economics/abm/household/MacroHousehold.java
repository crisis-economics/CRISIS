/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Ariel Y. Hoffman
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import sim.util.Bag;
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
import eu.crisis_economics.abm.household.plugins.HouseholdDecisionRule;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarketOrder;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarketOrder;
import eu.crisis_economics.abm.markets.nonclearing.GoodsMarketOrder;
import eu.crisis_economics.abm.markets.nonclearing.LabourMarketOrder;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.StateVerifier;

public class MacroHousehold 
   extends StrategicHousehold 
   implements Employee, GoodsBuyer, Party, ConsumerHousehold, FundInvestor {
   
   private SimpleDomesticGoodsConsumer
      domesticGoodsConsumer;
   
   /** Local encapsulation for household cash deposits and loans. When using this 
     * inner class locally, avoid direct member field access to the extent 
     * that this is possible.
     **/
   private class DepositsLoansAndInvestments {
      
      private double
         depositsAtBeginningOfCycle,
         intendedFundContribution,
         lastFundInvestmentAccountValue,
         fundInvestmentAccountValueAtBeginningOfCycle;
      
      private Fund
         fund;
      
      private FundInvestmentDecisionRule
         fundContributionAlgorithm;
      
      private DepositsLoansAndInvestments() {
         Simulation.repeat(
            this, "resetMemories", NamedEventOrderings.BEFORE_ALL);
         Simulation.repeat(
            this, "consumptionBudgetDecision",
               CustomSimulationCycleOrdering.create(
                  NamedEventOrderings.WAGE_PAYMENT, 2));
         Simulation.repeat(
            this, "fundContributionDecision", NamedEventOrderings.HOUSEHOLD_FUND_PAYMENTS);
      }
      
      @SuppressWarnings("unused") // Scheduled
      private void resetMemories() {
         cashLedger.disallocateAll();
         depositsAtBeginningOfCycle = getDepositValue();
         intendedFundContribution = 0.;
         lastFundInvestmentAccountValue = fundInvestmentAccountValueAtBeginningOfCycle;
         fundInvestmentAccountValueAtBeginningOfCycle = 
            (fund == null) ? 0. : fund.getBalance(MacroHousehold.this);
      }
      
      @SuppressWarnings("unused") // Scheduled
      private void consumptionBudgetDecision() {
         // Compute the next goods consumption budget.
         HouseholdDecisionRule consumptionBudgetAllocationAlgorithm =
            MacroHousehold.this.consumptionAndGoods.getConsumptionBudgetAllocationAlgorithm();
         consumptionBudgetAllocationAlgorithm.computeNext(getState());
      }
      
      @SuppressWarnings("unused") // Scheduled
      private void fundContributionDecision() {
         if(fund == null)                                               // No fund to invest in.
            return;
         double
            fundContribution = accept(fundContributionAlgorithm);
         final double
            existingFundBalance = fund.getBalance(MacroHousehold.this),
            maximumInvestment = cashLedger.getUnallocated();
         if(fundContribution > maximumInvestment) {
            final String warningMsg =
               "MacroHousehold.fundContributionDecision: investment decision rule intended "
             + "to invest " + fundContribution + " in funds, however the household only has "
             + maximumInvestment + " units of deposit cash at its disposal for this purpose." + 
             " The intended fund investment has been trimmed to " + maximumInvestment + ".";
            fundContribution = maximumInvestment;
            System.err.println(warningMsg);
         }
         if(fundContribution < 0 && -fundContribution > existingFundBalance) {
            final String warningMsg =
               "MacroHousehold.fundContributionDecision: investment decision rule intended "
             + "to withdraw " + -fundContribution + " from funds, however the value of household"
             + " fund investments is currently " + existingFundBalance + ". The intended fund " 
             + "withdrawal amount has been trimmed to " + existingFundBalance + ".";
            fundContribution = -existingFundBalance;
            System.err.println(warningMsg);
         }
         if(fundContribution > 0.) {
            try {
               fund.invest(MacroHousehold.this, fundContribution);
            } catch (final InsufficientFundsException unexpected) {             // Failed
               fundContribution = 0.;                                           // Abandon
            }
         }
         else if(fundContribution < 0.) {
            try {
               fund.requestWithdrawal(MacroHousehold.this, -fundContribution);
            } catch (final InsufficientFundsException unexpected) {             // Failed
               fundContribution = 0.;                                           // Abandon
            }
         }
         
         intendedFundContribution = fundContribution;
      }
      
      /** Get the cash deposit value at the beginning of this cycle. */
      private double getDepositsAtBeginningOfCycle() {
         return depositsAtBeginningOfCycle;
      }
      
      private double getFundBalance() {
         return fund == null ? 0. : fund.getBalance(MacroHousehold.this);
      }
      
      @Override
      public String toString() {
         String result = "cash position: " + cashLedger.toString() + ".";
         return result;
      }
   }
   
   private DepositsLoansAndInvestments
      depositsLoansAndInvestments;
   
   /** Local encapsulation for goods consumption and household budgets. When 
     * using inner class locally, avoid direct member field access to the extent
     * that this is possible.
     * 
     * This object contains a reference to the goods exchange market, a list of 
     * active goods orders, consumption budget decision algorithms, and the 
     * household consumption function (typically CES).
     **/
   private class ConsumptionAndGoods {
      private SimpleGoodsMarket
         goodsMarket;                           // Goods exchange market
      
      private Bag
         goodsOrderList;                        // Active Goods order list
      
      private HouseholdDecisionRule
         consumptionBudgetAllocationAlgorithm;  // Consumption budget decision algorithm
      
      private HouseholdConsumptionFunction
         consumptionFunction;                   // Household Goods consumption function
      
      private double 
         cashSpendOnGoodsThisBusinessCycle;     // Total cash spent on Goods in this cycle. 
      
      private Map<String, Double>
         lastPurchardGoodsVolumes;              // Amount of each Goods type bought last cycle.
      
      private ConsumptionAndGoods(
         SimpleGoodsMarket goodsMarket,
         HouseholdDecisionRule consumptionBudgetAlgorithm,
         HouseholdConsumptionFunction consumptionFunction
         ) {
         StateVerifier.checkNotNull(goodsMarket, consumptionBudgetAlgorithm, consumptionFunction);
         this.goodsMarket = goodsMarket;
         this.goodsOrderList = new Bag();
         this.consumptionBudgetAllocationAlgorithm = consumptionBudgetAlgorithm;
         this.consumptionFunction = consumptionFunction;
         this.cashSpendOnGoodsThisBusinessCycle = 0.;
         this.lastPurchardGoodsVolumes = new HashMap<String, Double>();
         
         resetMemories();
         
         Simulation.repeat(this, "resetMemories", NamedEventOrderings.BEFORE_ALL);
      }
      
      private void resetMemories() {
         cashSpendOnGoodsThisBusinessCycle = 0.;
         lastPurchardGoodsVolumes.clear();
      }
      
      /**
        * Decide the Goods consumption budget and order these goods from the market.
        */
      private void decideConsumptionBudgetAndOrderGoods() {
         
         final List<Double>
            goodsMarketPrices = new ArrayList<Double>();
         final List<String>
            goodsToConsider = new ArrayList<String>();
         {
         final List<String>
            goodsToConsiderIfAvailable = domesticGoodsConsumer.getGoodsTypesConsidered();
         for(final String type : goodsToConsiderIfAvailable)
            if(goodsMarket.hasInstrument(type))
               goodsToConsider.add(type);
         }
         
         if(goodsToConsider.size() == 0)
            return;                                                        // No goods to buy
         
         for(final String type : goodsToConsider)
            goodsMarketPrices.add(
               goodsMarket.getInstrument(type).getWorstAskPriceAmongSellers());
         
         assert goodsToConsider.size() == goodsMarketPrices.size();
         
         double maximumGoodsConsumptionBudget =
            consumptionAndGoods.getMostRecentConsumptionBudget();
         final double
            availableDeposits = Math.max(cashLedger.getUnallocated(), 0.);
         if(maximumGoodsConsumptionBudget > availableDeposits) {
            final String warningMsg =
               "MacroHousehold.decideConsumptionBudgetAndOrderGoods: consumption budget decision "
             + "rule intended a budget of value " + maximumGoodsConsumptionBudget + ", however"
             + " the household only has access to " + availableDeposits + " units of"
             + " deposits. The consumption budget has been trimmed to " + availableDeposits + ".";
            maximumGoodsConsumptionBudget = availableDeposits;
            System.err.println(warningMsg);
            System.err.flush();
         }
         maximumGoodsConsumptionBudget = Math.max(0., maximumGoodsConsumptionBudget);
         
         final Double[] goodsDemandsByType =
            consumptionFunction.calculateConsumption(
               maximumGoodsConsumptionBudget, 
               goodsMarketPrices.toArray(new Double[0])
               );
         
         for(int i = 0; i< goodsToConsider.size(); ++i) {
            final String
               name = goodsToConsider.get(i);
            final double
               amountDesired = goodsDemandsByType[i],
               price = goodsMarketPrices.get(i);
            if(Double.isInfinite(amountDesired) ||
               Double.isNaN(amountDesired) ||
               Double.isInfinite(price) ||
               Double.isNaN(price)
               ) {
               System.err.println(
                  "MacroHousehold.ConsumptionAndGoods: error: price and desired quantity " + 
                  "are infinite or NaN, details follow. Quantity to order: " + amountDesired + 
                  ", unit price: " + price + ". No action was taken."
                  );
               System.err.flush();
               continue;
            }
            try {
               if(amountDesired > 0.) {
                  SimpleGoodsMarketOrder newGoodsOrder =
                     goodsMarket.addOrder(
                        MacroHousehold.this,
                        name,
                        -amountDesired,                                 // Buy order
                        price
                        );
                  goodsOrderList.add(newGoodsOrder);
               }
            }
            catch(final AllocationException e) {                        // Failed
               // System.err.println(errMsg + e);
               // Abandon
            }
            catch(final OrderException e) {                             // Failed
               // System.err.println(errMsg + e);
               // Abandon
            }
         }
      }
      
      // Remove a goods market order.
      private boolean removeGoodsOrder(SimpleGoodsMarketOrder order) {
         if(order == null)
            return false;
         final double unobtainableGoodsValue = order.getPrice() * order.getOpenSize();
         cashLedger.changeAllocatedBy(-unobtainableGoodsValue);
         return goodsOrderList.remove(order);
      }
      
      // Trade registration
      
      // Respond, as appropriate, to incoming Goods.
      private void registerIncomingTradeVolume(
         final String goodsType,
         final double goodsAmount,
         final double unitCostPaid
         ) {
         if(lastPurchardGoodsVolumes.containsKey(goodsType))
            lastPurchardGoodsVolumes.put(
               goodsType, lastPurchardGoodsVolumes.get(goodsType) + goodsAmount);
         else
            lastPurchardGoodsVolumes.put(goodsType, goodsAmount);
         cashSpendOnGoodsThisBusinessCycle += 
             goodsAmount * unitCostPaid;
      }
      
      /** Get the (cumulative) amount of cash spent on goods in this cycle. */
      private double getCashSpendOnGoodsToDateThisCycle() {
         return cashSpendOnGoodsThisBusinessCycle;
      }
      
      /** Get the most recent total Goods consumption budget. */
      private double getMostRecentConsumptionBudget() {
         return consumptionBudgetAllocationAlgorithm.getLastValue();
      }
      
      /** Get the consumption budget algorithm for this household. */
      HouseholdDecisionRule getConsumptionBudgetAllocationAlgorithm() {
         return consumptionBudgetAllocationAlgorithm;
      }
      
      @Override
      public String toString() {
         String result = 
            "active goods orders: " + (goodsOrderList.size() == 0 ? "none." : "") + ".\n";
         {
         int goodsOrderIndex = 1;
         for(Object obj : goodsOrderList) {
            SimpleGoodsMarketOrder order = (SimpleGoodsMarketOrder)obj; 
            result += String.format(
               "order %3d: size: %16.10g unit price: %16.10g.\n",
               goodsOrderIndex, order.getSize(), order.getPrice());
            ++goodsOrderIndex;
         }
         }
         result += 
            "most recent budget for goods acquisition: " +
               consumptionBudgetAllocationAlgorithm.getLastValue() + ".\n" +
            "cash expenditure on goods this cycle: " + 
               getCashSpendOnGoodsToDateThisCycle() + ".";
         return result;
      }
   }
   
   private ConsumptionAndGoods consumptionAndGoods;
   
   /** Local encapsulation for labour contracts and employment. When using this 
     * inner class locally, avoid direct member field access to the extent that 
     * this is possible. 
     * 
     * This object contains a reference to the labour exchange market, a list of 
     * active labour orders, unemployment bookkeeping, and the labour wage ask 
     * selection algorithm (price per unit labour per unit time).
     **/
   private class LabourAndEmployment {
      private final static int 
         DEFAULT_CONTRACT_MATURITY = 1;   // Labour contract maturity (number of time steps).
      
      private TimeseriesParameter<Double>
         labourToOffer;
      
      private SimpleLabourMarket 
         labourMarket;                    // Labour exchange market.
      
      private Bag
         labourOrderList;                 // Bag (of SimpleLabourMarketOrder) active orders. 
      
      private List<Labour>
         labourEmployed;                  // List of active labour contracts.
      
      private HouseholdDecisionRule
         labourWageAlgorithm;             // Labour wage (unit ask price) selection algorithm.
      
      private double
         labourNotEmployed;               // Maximum employable labour now.
         
      private double
         reservedLabourAmount,            // Reserved labour.
         totalLabourAvailableThisCycle,   // Labour available to market in this cycle.
         totalWagesReceivedThisCycle,     // Total labour wages received this cycle.
         labourEmployedLastCycle,         // Labour employed in the last cycle.
         meanWagePerUnitLabourLastCycle;  // Trade-weighted mean wage per unit labour, last cycle.
            
      private LabourAndEmployment(
         TimeseriesParameter<Double> labourToOffer,
         SimpleLabourMarket labourMarket,
         HouseholdDecisionRule labourWageAlgorithm
         ) {
         StateVerifier.checkNotNull(labourMarket, labourWageAlgorithm);
         this.labourToOffer = labourToOffer;
         this.labourMarket = labourMarket;
         this.labourOrderList = new Bag();
         this.labourEmployed = new LinkedList<Labour>();
         this.labourNotEmployed = labourToOffer.get();
         this.reservedLabourAmount = 0.;
         this.labourWageAlgorithm = labourWageAlgorithm;
         this.totalLabourAvailableThisCycle = labourToOffer.get();
         
         Simulation.repeat(this, "rememberState", NamedEventOrderings.AFTER_ALL);
         Simulation.repeat(this, "resetMemories", NamedEventOrderings.BEFORE_ALL);
      }
      
      @SuppressWarnings("unused")
      private void resetMemories() {
         reservedLabourAmount = 0.;
         totalWagesReceivedThisCycle = 0.;
         if(labourOrderList.size() > 0) {
            for(Object obj : labourOrderList)
               ((SimpleLabourMarketOrder) obj).cancel();
            labourOrderList.clear();
            System.err.println(
               "MacroHousehold.LabourAndEmployment: error: a labour market order is outstanding "
             + "from the last business cycle. This behaviour is not expected in the current "
             + "implementation. Outstanding orders have been cancelled."
               );
         }
         
         final double reservedCash = cashLedger.getAllocated();
         if(Math.abs(reservedCash) > 1.) {
            System.out.println("reserved cash: " + reservedCash);
         }
         
         this.totalLabourAvailableThisCycle = labourToOffer.get();
         this.labourNotEmployed =
            Math.max(totalLabourAvailableThisCycle - getLabourAmountEmployed(), 0.);
      }
      
      @SuppressWarnings("unused")
      private void rememberState() {
         labourEmployedLastCycle = getLabourAmountEmployed();
         meanWagePerUnitLabourLastCycle = getMeanWagePerUnitLabourEmployed();
      }
      
      /** Offer labour to the market for sale. */
      private void offerLabourToMarket() {
         if(labourEmployed.size() > 0)
            System.err.println(
               "MacroHousehold.LabourAndEmployment: error: a labour contract is outstanding " + 
               "from the last business cycle. This behaviour is not expected in the current " +
               "implementation. Continuing.");
         
         labourWageAlgorithm.computeNext(getState());
         
         double labourToOfferToMarket = getTotalLabourNotEmployed();
         
         if(labourToOfferToMarket > 0.) {
            final String errMsg =
               "MacroHousehold.LabourAndEmployment: a household failed to submit a " +
               "labour market sale order; details follow. Contract maturity: " + 
               DEFAULT_CONTRACT_MATURITY + ", wage ask price: " + getMostRecentWageAsk() + 
               ", " + "labour amount offered to market: " + labourNotEmployed + ".";
            try {
               SimpleLabourMarketOrder labourSaleOrder =
                  labourMarket.addOrder(
                     MacroHousehold.this,
                     DEFAULT_CONTRACT_MATURITY,
                     labourToOfferToMarket,
                     getMostRecentWageAsk()
                     );
               labourOrderList.add(labourSaleOrder);
            }
            catch (final AllocationException e) {                           // Failed
               System.err.println(errMsg + e);
            }
            catch(final OrderException e) {                                 // Failed
               System.err.println(errMsg + e);
            }
         }
      }
      
      // Repond, as appropriate, to outgoing labour.
      private void registerOutgoingEmployment(
          double amountEmployed,
          double wagePerUnitLabour
          ) {
          labourNotEmployed -= amountEmployed;
          if(labourNotEmployed < -1.e-10)
             System.err.println(
                "MacroHousehold.LabourAndEmployment: " + amountEmployed + " has been employed, " +
                "however the amount of labour available for employment in this household is " + 
                "less than this amount."
                );
          labourNotEmployed = Math.max(labourNotEmployed, 0.);
          totalWagesReceivedThisCycle += wagePerUnitLabour * amountEmployed;
      }
      
      // Respond, as appropriate, to incoming wage or benefit payments.
      private void registerIncomingWageOrBenefit(double totalPayment) {
         totalWagesReceivedThisCycle += totalPayment;
      }
      
      // Record the establishment of a new labour contract.
      private void startNewLabourContract(Labour labour) {
         if(labour == null) return;
         labourEmployed.add(labour);
      }
      
      // Remove a labour market sale order, if this order exists.
      private boolean removeOrder(SimpleLabourMarketOrder order) {
         return labourOrderList.remove(order);
      }
      
      /** Remove a labour contract, freeing the underlying labour resource for reuse. */
      private void endExistingLabourContract(Labour labourContract) {
         final boolean result = labourEmployed.remove(labourContract);
         if(!result) return;                                                    // Silent
         labourNotEmployed += labourContract.getQuantity();
      }
      
      /** Get the most recent labour wage (ask price per unit labour per unit time). */
      private double getMostRecentWageAsk() {
         return labourWageAlgorithm.getLastValue();
      }
      
      /** Get the amount of labour available for employment at this time. */
      private double getTotalLabourNotEmployed() {
         return labourNotEmployed;
      }
      
      /** Get the total labour employed in the last cycle. */
      private double getLabourEmployedLastCycle() {
         return labourEmployedLastCycle;
      }
      
      /** Get the total amount of labour currently employed. */
      private double getTotalLabourCurrentlyEmployed() {
         double totalLabourEmployedNow = 0.;
         for(Object obj : labourEmployed) {
            Labour labour = (Labour)obj;
            totalLabourEmployedNow += labour.getQuantity();
         }
         return totalLabourEmployedNow;
      }
      
      /** Get the total wages received in this business cycle. */
      private double getTotalWagesReceivedThisCycle() { 
         return totalWagesReceivedThisCycle;
      }
      
      /** Get the (contract-weighted) mean labour wage received in this business cycle. */
      private double getMeanWagePerUnitLabourEmployed() {
         double
            totalLabourEmployedNow = 0.,
            meanWage = 0.;
         for(Object obj : labourEmployed) {
            Labour labour = (Labour)obj;
            totalLabourEmployedNow += labour.getQuantity();
            meanWage += labour.getQuantity() * labour.getWage();
         }
         if(totalLabourEmployedNow == 0.) return 0.;
         else return meanWage / totalLabourEmployedNow;
      }
      
      /** Get the (contract-weighted) mean labour wage received in the last business cycle. */
      private double getMeanWagePerUnitLabourEmployedLastCycle() {
         return meanWagePerUnitLabourLastCycle;
      }
      
      /** Try to reserve (block) an amount of labour. */
      private void tryReserveLabour(double positiveLabourAmount) throws AllocationException {
         if (positiveLabourAmount <= 0.) return;                             // Silent
         double newReservedLabourAmount = 
             (reservedLabourAmount + positiveLabourAmount);
         if(newReservedLabourAmount > labourNotEmployed) {
            if(newReservedLabourAmount - labourNotEmployed > 
               50. * Math.ulp(newReservedLabourAmount))
               throw new AllocationException(
                  "MacroHousehold.tryReserveaLabour: error: cannot reserve " + 
                  positiveLabourAmount + " unit of labour. Total unemployed labour " + 
                  "is " + labourNotEmployed + ".");
            newReservedLabourAmount = labourNotEmployed;
         }
         reservedLabourAmount = newReservedLabourAmount;
      }
      
      /** Try to unreserve (unblock) an amount of labour. */
      private void unreserveLabour(double positiveLabourAmount) {
         if(positiveLabourAmount <= 0.) return;
         reservedLabourAmount -= Math.min(positiveLabourAmount, reservedLabourAmount);
      }
      
      /** Get the total amount of labour available to the market this cycle. */
      private double getTotalLabourAvailableToMarketThisCycle() {
         return totalLabourAvailableThisCycle;
      }
      
      @Override
      public String toString() {
         String result = 
            "active labour market orders: " + (labourOrderList.size() == 0 ? "none." : "") + "\n";
         {
         int labourOrderIndex = 1;
         for(Object obj : labourOrderList) {
            SimpleLabourMarketOrder order = (SimpleLabourMarketOrder)obj;
            result += String.format(
               "labour order %3d, size: %16.10g, open: %16.10g, unit price: %16.10g.\n",
               labourOrderIndex, order.getSize(), order.getOpenSize(), order.getPrice());
            ++labourOrderIndex;
         }
         }
         result += 
            "active labour contracts: " + (labourEmployed.size() == 0 ? "none." : "") + "\n";
         {
         int labourContractIndex = 1;
         for(Labour labour : labourEmployed) {
            result += String.format(
               "labour contract %3d, size: %16.10g, wage: %16.10g.\n",
               labourContractIndex, labour.getQuantity(), labour.getWage());
            ++labourContractIndex;
         }
         }
         result += 
            "most recent wage ask: " + getMostRecentWageAsk() + ".\n" +
            "reserved labour now: " + reservedLabourAmount + ".";
         return result;
      }
   }
   
   private LabourAndEmployment labourAndEmployment;
   
   /**
     * Create a {@link MacroHousehold} object. This constructor is 
     * assisted with dependency injection.
     * @param depositHolder
     *        The {@link DepositHolder}, who holds the {@link Household}
     *        deposit account.
     * @param fund
     *        The {@link Fund} in which the {@link Household} invests.
     * @param initialCashEndowment
     *        The initial (starting) {@link Household} cash.
     * @param initialLabourEndowment
     *        The initial (starting) {@link Household} labour.
     * @param numberOfGoodsTypes
     *        The number of goods types available for consumption.
     * @param goodsMarket
     *        The goods market over which to buy goods.
     * @param labourMarket
     *        The labour market over which to submit labour orders.
     * @param labourWageAlgorithm
     *        The {@link HouseholdDecisionRule} governing {@link Household}
     *        wage per unit labour expectations.
     * @param consumptionBudgetAllocationAlgorithm
     *        The {@link HouseholdDecisionRule} governing {@link Household}
     *        consumption budget (size) decisions.
     * @param consumptionFunction
     *        The {@link Household} consumption function, governing the division
     *        of {@link Household} consumption between differentiated 
     *        goods types.
     */
   @Inject
   public MacroHousehold(
   @Assisted
      final DepositHolder depositHolder,                    // Manual
   @Named("HOUSEHOLD_INITIAL_CASH_ENDOWMENT")
      final double initialCashEndowment,
   @Named("HOUSEHOLD_LABOUR_TO_OFFER")
      final TimeseriesParameter<Double> labourToOffer,
      final SimpleGoodsMarket goodsMarket,
      final SimpleLabourMarket labourMarket,
   @Named("HOUSEHOLD_LABOUR_WAGE_ASK_RULE")
      final HouseholdDecisionRule labourWageAlgorithm,
   @Named("HOUSEHOLD_BUDGET_DECISION_RULE")
      final HouseholdDecisionRule consumptionBudgetAllocationAlgorithm,
   @Named("HOUSEHOLD_CONSUMPTION_FUNCTION")
      final HouseholdConsumptionFunction consumptionFunction
      ) {
      super(depositHolder, initialCashEndowment);
      
      // Deposits, Funds and Loan behaviour
      this.depositsLoansAndInvestments = new DepositsLoansAndInvestments();
      
      // Consumption (budgets) and goods behaviour
      this.consumptionAndGoods = new ConsumptionAndGoods(
         goodsMarket,
         consumptionBudgetAllocationAlgorithm,
         consumptionFunction
         );
      
      // Labour and employment behaviour
      this.labourAndEmployment = new LabourAndEmployment(
         labourToOffer,
         labourMarket,
         labourWageAlgorithm
         );
      
      this.domesticGoodsConsumer = new SimpleDomesticGoodsConsumer(this);
      
      Simulation.repeat(this, "offerLabourToMarket", NamedEventOrderings.HIRE_WORKERS);
      Simulation.repeat(this, "considerConsumption", NamedEventOrderings.CONSUMPTION);
   }
    
   @SuppressWarnings("unused") // Scheduled
   private void offerLabourToMarket() {
      labourAndEmployment.offerLabourToMarket();
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void considerConsumption() {
      consumptionAndGoods.decideConsumptionBudgetAndOrderGoods();
   }
   
   @Override
   public void updateState(Order order) { }
   
   private HouseholdDecisionRule.HouseholdState getState() {
      HouseholdDecisionRule.HouseholdState.Builder
         builder = new HouseholdDecisionRule.
            HouseholdState.Builder();
      builder
         .mostRecentConsumptionBudget(consumptionAndGoods.getMostRecentConsumptionBudget())
         .householdEquity(getEquity())
         .lastLabourEmployed(labourAndEmployment.getLabourEmployedLastCycle())
         .maximumLabourOffered(labourAndEmployment.getTotalLabourAvailableToMarketThisCycle())
         .lastLabourUnitWage(labourAndEmployment.getMostRecentWageAsk())
         .totalAssets(getTotalAssets())
         .previousDividendReceived(super.getDividendReceivedLastCycle())
         .totalDeposits(getDepositValue())
         .fundAccountValue(depositsLoansAndInvestments.getFundBalance())
         .previousFundAccountValue(depositsLoansAndInvestments.lastFundInvestmentAccountValue)
         .previousTotalConsumption(consumptionAndGoods.getCashSpendOnGoodsToDateThisCycle())
         .totalLiabilities(getTotalLiabilities())
         .unreservedDeposits(cashLedger.getUnallocated())
         .previousMeanWage(labourAndEmployment.getMeanWagePerUnitLabourEmployedLastCycle());
      return builder.build();
   }
   
   /** Get the most recent total Goods consumption budget. */
   @Override
   public double getConsumptionBudget() {
      return consumptionAndGoods.getMostRecentConsumptionBudget();
   }
   
   /** Allocate (reserve) an amount of labour. */
   @Override
   public void allocateLabour(double positiveLabourAmountToAllocate)
      throws AllocationException {
      labourAndEmployment.tryReserveLabour(positiveLabourAmountToAllocate);
   }
   
   /** Disallocate (unreserve) an amount of labour. */
   @Override
   public void disallocateLabour(double positiveLabourAmountToDisallocate) {
      labourAndEmployment.unreserveLabour(positiveLabourAmountToDisallocate);
   }
   
   /** Record a new labour contract. */
   @Override
   public void startContract(Labour labour) throws DoubleEmploymentException {
      labourAndEmployment.startNewLabourContract(labour);
   }
   
   /** Remove an existing labour contract. */
   @Override
   public void endContract(Labour labour) {
      labourAndEmployment.endExistingLabourContract(labour);
   }
   
   /** Remove a labour market order, if this order exists. */
   public boolean removeOrder(SimpleLabourMarketOrder order) {
      return labourAndEmployment.removeOrder(order);
   }
   
   @Override
   public double getLabourAmountEmployed() {
      return labourAndEmployment.getTotalLabourCurrentlyEmployed();
   }
   
   @Override
   public double getMaximumLabourSupply() {
      return labourAndEmployment.getTotalLabourAvailableToMarketThisCycle();
   }
   
   @Override
   public double getLabourAmountUnemployed() {
      return getMaximumLabourSupply() - getLabourAmountEmployed();
   }
   
   @Override
   public double allocateCash(double positiveAmount) {
      return cashLedger.changeAllocatedBy(+positiveAmount);
   }
   
   @Override
   public double disallocateCash(double positiveAmount) {
      return -cashLedger.changeAllocatedBy(-positiveAmount);
   }
   
   @Override
   public double getAllocatedCash() {
      return cashLedger.getAllocated();
   }
   
   /** Get the goods repository for this household. */
   @Override
   public GoodsRepository getGoodsRepository() {
      return m_goodsRepository;
   }
   
   @Override
   public double calculateAllocatedCashFromActiveOrders() {
      throw new UnsupportedOperationException();
   }
   
   /** Remove a goods market order, if the order is known to this household. */
   public boolean removeOrder(SimpleGoodsMarketOrder order) {
      return consumptionAndGoods.removeGoodsOrder(order);
   }
   
   @Override
   protected void considerDepositPayment() { }
   
   // TODO:
   @Override
   public void addOrder(Order order) {
      super.addOrder(order);
      if (order instanceof LabourMarketOrder) {
         throw new UnsupportedOperationException();
      }
      if (order instanceof GoodsMarketOrder) {
         throw new UnsupportedOperationException();
      }
   }
   
   // TODO:
   @Override
   public boolean removeOrder(Order order) {
      super.removeOrder(order);
      if (order instanceof LabourMarketOrder) {
         throw new UnsupportedOperationException();
      }
      if (order instanceof GoodsMarketOrder) {
         throw new UnsupportedOperationException();
      }
      return false;
   }
   
   // Trade Registration
   
   @Override
   public void registerIncomingTradeVolume(
      String goodsType,
      double goodsAmount,
      double unitCostPaid
      ) { 
      consumptionAndGoods.registerIncomingTradeVolume(
         goodsType,
         goodsAmount,
         unitCostPaid
         );
   }
   
   @Override
   public void notifyOutgoingEmployment(
      double outGoingLabourAmount,
      double wagePerUnitLabour
      ) {
      labourAndEmployment.registerOutgoingEmployment(outGoingLabourAmount, wagePerUnitLabour);
   }
   
   @Override
   public void notifyIncomingWageOrBenefit(double totalPayment) {
      labourAndEmployment.registerIncomingWageOrBenefit(totalPayment);
   }
   
   // FundInvestor interface
   
   @Override
   public void setFundInvestmentTargetDecisionRule(
      final FundInvestmentDecisionRule targetInvestmentValue
      ) {
      depositsLoansAndInvestments.fundContributionAlgorithm =
         Preconditions.checkNotNull(targetInvestmentValue);
   }
   
   /** Value of welfare received by employee from government agent (when a government exists) */
   private double welfareAllowance;
   
   /** Get value of welfare received by employee from government agent (when a government exists) */
   public double getWelfareAllowance() {
      return welfareAllowance;
   }
   
   /** Allow value of welfare received in a time-step to be set (and subsequently recorded) */
   public void setWelfareAllowance(double valueOfWelfareToReceive) {
      welfareAllowance = valueOfWelfareToReceive;
   }
   
   @Override
   public void setFund(Fund fund) {
      depositsLoansAndInvestments.fund = fund;
   }

   @Override
   public Fund getFund() {
      return depositsLoansAndInvestments.fund;
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
   
   // CSV-file annotations
   
   /** Get the domestic consumption budget at this time. */
   @RecorderSource("DomesticBudget")
   public double getDomesticBudgetAtThisTime() {
      return consumptionAndGoods.consumptionBudgetAllocationAlgorithm.getLastValue();
   }
       
   /** Get the total cash spend on goods at this time. */
   @RecorderSource("TotalCashSpentOnGoods")
   public double getTotalCashSpentOnGoodsAtThisTime() {
      return consumptionAndGoods.getCashSpendOnGoodsToDateThisCycle();
   }
   
   /** Get the total remuneration received in this employment cycle. */
   @RecorderSource("TotalRemuneration")
   public double getTotalRemunerationAtThisTime() {
       return labourAndEmployment.getTotalWagesReceivedThisCycle();
   }
   
   /** Get the total income not spent on  */
   @RecorderSource("TotalIncome")
   public double getTotalIncome() {
       return getDepositValue() - 
          depositsLoansAndInvestments.getDepositsAtBeginningOfCycle() 
        + getTotalCashSpentOnGoodsAtThisTime();
   }
   
   @RecorderSource("FundContribution")
   public double getFundContributionAtThisTime() {
      return depositsLoansAndInvestments.intendedFundContribution;
   }
}