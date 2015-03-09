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
package eu.crisis_economics.abm.firm;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.BoundedUnivariateFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.InverseExpIOCPartitionFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.PartitionedResponseFunction;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A simple implementation of the {@link ClearingFirm} class. This {@link Firm}
  * represents an exogenous producer with access to exogenous funds.<br><br>
  * 
  * This {@link Firm} holds its deposits in two distinct locations: {@code (a)} 
  * with an indogenous {@link DepositHolder}, and {@code (b)} in an exogenous unmodelled
  * location. The size (cash value) of the {@link DepositAccount} held by {@code (a)} is
  * customizable. This {@link Firm} otherwise has access to limitless exogenous funding
  * via {@code (b)}.<br><br>
  * 
  * In order to use this {@link Agent}, you must specify the following quantities 
  * as {@link Provider}{@code s} of {@link Double} (series in simulation time {@code T}):
  * 
  * <ul>
  *   <li> The cash value {@code D(t)} of the {@link DepositAccount} to be held with the 
  *        indogenous source. This {@link Firm} will pay for all its expenses out of its 
  *        exogenous funding source. Once per simulation cycle, at time 
  *        {@link NamedEventOrderings#DEPOSIT_PAYMENT}, this {@link Firm} will top up 
  *        (or subtract from) the value of its indogenous {@link DepositAccount} in order 
  *        to match your specification for the {@link DepositAccount} value. The indogenous
  *        {@link DepositAccount} will otherwise not be modified by this {@link Agent}.
  *        If you specify a negative account value {@code D(t) < 0}, the value will
  *        be taken to be {@code 0}.<br><br>
  *        
  *   <li> The commercial loan demand {@code C(t)} per simulation cycle. This {@link Firm}
  *        will order <i>at most</i> {@code C(t)} units of commercial loans per simulation
  *        cycle. The exact size of commercial loan orders made by this {@link Firm} is
  *        specified by the profile of the {@link CreditDemandFunction}, and this {@link Firm}
  *        will order the same maximum quantity {@code C(t)} from all clearing commercial
  *        loan markets to which it belongs;<br><br>
  *        
  *   <li> The desired {@link Labour} (workforce size) {@code L(t)} per simulation cycle. 
  *        This {@link Firm} will post a buy order for {@code max(0., L(t))} units of labour once
  *        per simulation cycle;<br><br>
  *        
  *   <li> The desired {@link Labour} bid price (wage per unit labour) {@code W(t)} per 
  *        simulation cycle. When posting an order for {@code W(t)} units of {@link Labour},
  *        this {@link Firm} will specify an ask price per unit of value {@code max(0, W(t))};
  *        <br><br>
  *        
  *   <li> The production yield {@code Y(T)}. Once per simulation cycle, at time 
  *        {@link NamedEventOrderings#PRODUCTION} this {@link Firm} will produce 
  *        {@code max(0., Y(T))} units of goods. The cost of this production activity 
  *        cannot be detected by any other entity in the simulation. Note that the 
  *        specification of production goods for this {@link Firm} may be durable, and,
  *        in particular, unsold production goods may accumulate in the inventory of
  *        this {@link Agent} in time. This {@link Firm} will attempt to sell all
  *        goods of the type it produces every simulation cycle.
  *        <br><br>
  *      
  *   <li> The desired production goods ask price (per unit) {@code P(t)} per simulation
  *        cycle. If your selection {@code P(t)} is negative, it will be treated as 
  *        {@code 0}.<br><br>
  *   
  *   <li> The total dividend payment {@code D(t)} to be paid every simulation cycle. Once
  *        per simulation cycle, at time {@link NamedEventOrderings#FIRM_ACCOUNTING}, this 
  *        {@link Firm} will reset its dividend per share such that the total outgoings in 
  *        pending dividends amount to {@code max(D(t), 0)}. Note that this {@link Firm} has 
  *        no direct control over the recipients of this dividend payment. This dividend
  *        will be paid from exogenous funds and not the indogenous {@link Firm} 
  *        deposit account.<br><br>
  *        
  *   <li> The maximum lending rate {@code r(t)} at which the {@link Firm} is willing
  *        to participate in commercial loan borrowing. The expression {@code max(r(t), 0.)} 
  *        specifies the root of the {@link Firm} credit demand function.<br><br>
  * </ul>
  * 
  * @author phillips
  */
public final class ExogenousFirm extends ClearingFirm implements GoodsSeller, Employer {
   
   private final TimeseriesParameter<Double>
      deposits,
      commercialLoanDemand,
      desiredLabour,
      wageBidPrice,
      productionYield,
      sellingPrice,
      dividendPayment,
      maximumLendingRate;
   
   private final String
      productionGoodsType;
   
   private final SimpleGoodsMarket
      goodsMarket;
   
   private final SimpleLabourMarket
      labourMarket;
   
   private final CreditDemandFunction
      creditDemandFunction;
   
   private GoodsRepository
      durableGoodsRepository;
   
   @Inject
   public ExogenousFirm(
   @Assisted
      DepositHolder depositHolder,
   @Assisted
      StockHolder stockHolder,
   @Assisted
      double numberOfShares,
   @Named("FIRM_INITIAL_PRICE_PER_SHARE")
      double emissionPrice,
   @Named("FIRM_SECTOR_NAME")
      String sectorName,
      SimpleLabourMarket labourMarket,
      SimpleGoodsMarket goodsMarket,
   @Named("EXOGENOUS_FIRM_DEPOSITS_TIMESERIES")
      final TimeseriesParameter<Double> deposits,
   @Named("EXOGENOUS_FIRM_CREDIT_DEMAND_TIMESERIES")
      final TimeseriesParameter<Double> commercialLoanDemand,
   @Named("EXOGENOUS_FIRM_LABOUR_DEMAND_TIMESERIES")
      final TimeseriesParameter<Double> desiredLabour,
   @Named("EXOGENOUS_FIRM_WAGE_BID_PRICE_TIMESERIES")
      final TimeseriesParameter<Double> wageBidPrice,
   @Named("EXOGENOUS_FIRM_PRODUCTION_TIMESERIES")
      final TimeseriesParameter<Double> productionYield,
   @Named("EXOGENOUS_FIRM_SELLING_PRICE_TIMESERIES")
      final TimeseriesParameter<Double> sellingPrice,
   @Named("EXOGENOUS_FIRM_DIVIDEND_PAYMENT")
      final TimeseriesParameter<Double> dividendPayment,
   @Named("EXOGENOUS_FIRM_MAXIMUM_LENDING_RATE")
      final TimeseriesParameter<Double> maximumLendingRate,
   @Named("EXOGENOUS_FIRM_CREDIT_DEMAND_FUNCTION")
      final CreditDemandFunction creditDemandFunction,
   @Named("FIRM_NAME_GENERATOR")
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder,
            deposits.get(),
            stockHolder,
            numberOfShares,
            emissionPrice,
            nameGenerator
            );
      this.deposits = Preconditions.checkNotNull(deposits);
      this.commercialLoanDemand = Preconditions.checkNotNull(commercialLoanDemand);
      this.desiredLabour = Preconditions.checkNotNull(desiredLabour);
      this.wageBidPrice = Preconditions.checkNotNull(wageBidPrice);
      this.productionYield = Preconditions.checkNotNull(productionYield);
      this.sellingPrice = Preconditions.checkNotNull(sellingPrice);
      this.dividendPayment = Preconditions.checkNotNull(dividendPayment);
      this.maximumLendingRate = Preconditions.checkNotNull(maximumLendingRate);
      this.productionGoodsType = Preconditions.checkNotNull(sectorName);
      this.labourMarket = Preconditions.checkNotNull(labourMarket);
      this.goodsMarket = Preconditions.checkNotNull(goodsMarket);
      this.creditDemandFunction = Preconditions.checkNotNull(creditDemandFunction);
      this.labourEmployed = new LinkedList<Labour>();
      this.durableGoodsRepository = 
         new DurableGoodsRepository(goodsMarket.getGoodsClassifier());
      if(!goodsMarket.hasInstrument(sectorName))
         goodsMarket.addInstrument(sectorName);
      goodsMarket.getGoodsInstrument(sectorName).registerSeller(this);
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "flushMemories", 
         CustomSimulationCycleOrdering.create(NamedEventOrderings.BEFORE_ALL, 2));
      Simulation.repeat(this, "offerGoodsToMarket", NamedEventOrderings.SELL_ORDERS);
      Simulation.repeat(this, "produceGoods", NamedEventOrderings.PRODUCTION);
      Simulation.repeat(this, "firmBuyLabour", NamedEventOrderings.FIRM_BUY_INPUTS);
      Simulation.repeat(this, "setDividendPerShare", NamedEventOrderings.FIRM_ACCOUNTING);
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
   protected void considerCommercialLoanMarkets() { }
   
   @SuppressWarnings("unused")   // Sheduled
   private void flushMemories() {
      revenueFromGoodsSales = 0.;
      soldGoodsInThisCycle = 0.;
      maximumGoodsToSell = getGoodsRepository().getStoredQuantity(productionGoodsType);
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void offerGoodsToMarket() {
      final double
         quantityToSell = getGoodsRepository().getStoredQuantity(productionGoodsType),
         marketPricePerUnit = sellingPrice.get();
      final String errMsg = 
         "ExogenousFirm: goods market has rejected an ask order. The order had size " 
        + quantityToSell + " and market unit price " + marketPricePerUnit + ". This order "
        + "has been abandonned.";
      try {
         if(quantityToSell > 0.) {
            goodsMarket.addOrder(
               this, productionGoodsType, quantityToSell, marketPricePerUnit);
         }
      }
      catch(final AllocationException nothingToSell) {                   // Failed. No action.
         System.err.println(errMsg);
         System.err.flush();
      }
      catch(final OrderException unexpectedOrderRejection) {             // Failed. No action.
         System.err.println(errMsg);
         System.err.flush();
      }
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void produceGoods() {
      durableGoodsRepository.pushWithDelay(
         productionGoodsType, Math.max(0., productionYield.get()));
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void firmBuyLabour() {
      final double
         labourDemand = desiredLabour.get() - getEmployment(),
         labourWage = wageBidPrice.get();
      final String errMsg = 
         "ExogenousFirm: unable to submit labour order to the market; details follow. "
       + "Amount of labour desired: " + labourDemand + ", labour unit wage ask: " +
         labourWage + " [participant: " + getUniqueName() + "]. This order has been "
       + "abandonned.";
      if(labourDemand > 0.) {
         try {
            labourMarket.addOrder(this, 1, -labourDemand, labourWage);
         }
         catch (final AllocationException e) {                             // Failed. No action.
            System.err.println(errMsg + e);
            System.err.flush();
         }
         catch (final OrderException e) {                                  // Failed. No action.
            System.err.println(errMsg + e);
            System.err.flush();
         }
      }
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void setDividendPerShare() {
      final double
         totalDividendToPay = Math.max(dividendPayment.get(), 0.);
      if(getNumberOfEmittedShares() != 0)
         setDividendPerShare(totalDividendToPay / getNumberOfEmittedShares());
      else
         setDividendPerShare(0.);
   }
   
   @Override
   protected void considerProduction() { }
   
   @Override
   protected void considerDepositPayment() {
      final double
         currentDepositValue = Math.max(0., getDepositValue()),
         targetDepositValue = deposits.get(),
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
   
   @Override
   public double getRevenue() {
      return revenueFromGoodsSales;
   }
   
   @Override
   public double getProfit() {
      return 0.;                                                                 // TODO
   }
   
   @Override
   public double getEmployment() {
      return getLabourForce();
   }
   
   @Override
   public double getProduction() {
      return Math.max(productionYield.get(), 0.);
   }
   
   @Override
   public double getGoodsSellingPrice() {
      return Math.max(sellingPrice.get(), 0.);
   }
   
   @Override
   public MarketResponseFunction getMarketResponseFunction(
      final ClearingMarketInformation caller
      ) {
      if(ClearingLoanMarket.class.isAssignableFrom(caller.getMarketType())) {
         return new PartitionedResponseFunction(
            new InverseExpIOCPartitionFunction(1./maximumLendingRate.get()),
            new BoundedUnivariateFunction() {
               @Override
               public double value(double interestRate) {
                  return creditDemandFunction.getCreditDemandResponse(
                     commercialLoanDemand.get(), 
                     Math.max(0., maximumLendingRate.get()), 
                     interestRate
                     );
               }
               @Override
               public double getMaximumInDomain() {
                   return Math.max(0., maximumLendingRate.get());
               }
               @Override
               public double getMinimumInDomain() {
                  return 0;
               }
            });
      }
      return null;
   }
   
   @Override
   public GoodsRepository getGoodsRepository() {
      return durableGoodsRepository;
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
      allocatedExogenousFunds -= positiveAmount;
      allocatedExogenousFunds = Math.max(allocatedExogenousFunds, 0.);
      return positiveAmount;
   }
   
   @Override
   public double getAllocatedCash() {
      return allocatedExogenousFunds;
   }
   
   /**
    * This {@link Agent} has access to unlimited exogenous funds. In this interpretation, 
    * the size of the exogenous allocated funding pot simply grows to match the current 
    * cash allocation requirement, so the unallocated cash is numerically unlimited.
    */
   @Override
   public double getUnallocatedCash() {
      return Double.MAX_VALUE;
   }
   
   private List<Labour>
      labourEmployed;
   
   @Override
   public void addLabour(final Labour labour) {
      if(labour != null)
         labourEmployed.add(labour);
   }

   @Override
   public boolean removeLabour(final Labour labour) {
      if(labour != null)
         return labourEmployed.remove(labour);
      return false;
   }

   @Override
   public double getLabourForce() {
      double value = 0.;
      for(final Labour labour : labourEmployed)
         value += labour.getQuantity();
      return value;
   }

   @Override
   public void disallocateLabour(double size) {
      // No action.
   }
   
   @Override
   public void registerIncomingEmployment(
      double labourEmployed,
      double unitLabourWage) {
      // No action.
   }
   
   private double
      revenueFromGoodsSales,
      soldGoodsInThisCycle,
      maximumGoodsToSell;
   
   @Override
   public double getUnsold() {
      return (maximumGoodsToSell - soldGoodsInThisCycle);
   }
   
   @Override
   public void registerOutgoingTradeVolume(
      String goodsType,
      double goodsAmount,
      double unitPricePaid
      ) {
      revenueFromGoodsSales += goodsAmount * unitPricePaid;
      soldGoodsInThisCycle += goodsAmount;
   }
   
   @Override
   public void handleBankruptcy() {
      // No action
   }
}