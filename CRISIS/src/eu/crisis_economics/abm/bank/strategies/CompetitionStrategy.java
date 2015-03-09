/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 James Porter
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
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
package eu.crisis_economics.abm.bank.strategies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sim.util.Bag;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author james
 */
public abstract class CompetitionStrategy extends EmptyBankStrategy {
   
   /*
    * A parent class for more sophisticated bankStrategies, which have
    * multi-level allocation decisions
    */
   
   // Constants
   
   public static final int LOAN_DURATION = 7;
   public static final double RESERVE_CASH_PROPORTION = 0.05; // lend as much
   // as possible
   // up to this
   public static final double TARGET_MULTIPLIER = 5.0;
   public static final double EPSILON = 0.001;
   
   // Things which must be initialised by/for all subclasses
   
   protected Bag firms;
   protected final List<String> stockOrderNames = new ArrayList<String>();
   
   // Main Variables
   
   protected double equity;
   protected final List<String> stockOrderInstrumentNames = new ArrayList<String>();
   
   // Information Variables (should assume these have already been calculated
   // for use in bankStrategies).
   
   // Overview of allocation
   
   /**
	 * 
	 */
   protected double currentLoanAllocation;
   protected double currentStockAllocation;
   protected double currentTotalAllocation;
   
   protected double currentRelativeLoanAllocation;
   protected double currentRelativeStockAllocation;
   
   protected double currentInterbankLendingAllocation;
   protected double currentCash; // bank.getCashReserve()
   
   // Firm Lending:
   
   protected double totalFirmLending = 0.0;
   protected double averageFirmLendingRate = 0.0;
   // amount total amount of interest due in single period from outstanding
   // loans. Divide by totalFirmLending to get profitability
   protected double totalLendingReturn = 0.0;
   
   // Stocks:
   
   protected final List<Double> stockPrices = new ArrayList<Double>();
   protected final List<Double> stockReturns = new ArrayList<Double>();
   
   protected Double averageStockPrice;
   protected Double averageStockReturn;
   
   // Interbank:
   
   // currently don't track anything here
   
   public CompetitionStrategy(final StrategyBank bank) {
      super(bank);
   }
   
   /*
    * Probably want to actually make an overall decision, but may wish to revise
    * some parts below.
    */
   public abstract ArrayList<LoanOrderContainer> makeAllocationDecisionForLoans(
         StrategyBank bank);
   
   /*
    * This happens after loan allocation so may have more cash than expected
    */
   public abstract ArrayList<StockOrderContainer> makeAllocationDecisionForStockMarket(
         StrategyBank bank);
   
   /*
    * NB May now have more or less cash than expected
    */
   public abstract ArrayList<InterbankOrderContainer> makeAllocationDecisionForInterbankMarket(
         StrategyBank bank);
   
   /*
    * This is called immediately after making interbank decision. NB Don't know
    * what interbank order(s) have actually been fulfilled (this is probably
    * something to change for next competition?)
    */
   public abstract double makeAllocationDecisionForDividend(StrategyBank bank);
   
   @Override
   public void considerCommercialLoanMarkets() {
      cancelCurrentLoanOrders(getBank());
      
      // Information gathering method calls, might wish to override some of
      // these if additional information required for your strategy
      calculateAllocationInformation(getBank());
      calculateLoanInformation(getBank());
      calculateStockInformation(getBank());
      calculateInterbankInformation(getBank());
      
      // execute the orders (potentially keeping track of them internally)
      placeLoanOrders(getBank(), makeAllocationDecisionForLoans(getBank()));
   }
   
   @Override
   public void considerStockMarkets() {
      cancelCurrentStockOrders(getBank());
      
      // TODO add information gathering method calls
      
      placeStockOrders(getBank(), makeAllocationDecisionForStockMarket(getBank()));
   }
   
   @Override
   public void considerInterbankMarkets() {
      cancelInterbankOrders(getBank());
      
      // TODO add information calls
      
      placeInterbankOrder(getBank(), makeAllocationDecisionForInterbankMarket(getBank()));
      payDividends(getBank(), makeAllocationDecisionForDividend(getBank()));
   }
   
   protected void cancelCurrentLoanOrders(final StrategyBank bank) {
      for (final Order order : bank.getOrders()) {
         if (order instanceof CommercialLoanOrder) {
            order.cancel();
         }
      }
   }
   
   protected void cancelCurrentStockOrders(final StrategyBank bank) {
      for (final Order order : bank.getOrders()) {
         if (order instanceof StockOrder) {
            order.cancel();
         }
      }
   }
   
   protected void cancelInterbankOrders(final StrategyBank bank) {
      for (final Order order : bank.getOrders()) {
         if (order instanceof InterbankLoanOrder) {
            order.cancel();
         }
      }
   }
   
   protected double getCurrentLoanTotal(StrategyBank bank) {
      double total = 0.0;
      for (final Contract contract : bank.getAssets()) {
         if (contract instanceof Loan) {
            total += contract.getValue();
         }
      }
      return total;
   }
   
   protected void calculateAllocationInformation(StrategyBank bank) {
      /*
       * Calculates overall current allocations to make it easier to formulate
       * bankStrategies
       */
      currentCash = bank.getCashReserveValue();
      currentLoanAllocation = bank.getAssetsInLoans();
      currentStockAllocation = bank.getAssetsInStock();
      currentTotalAllocation = currentStockAllocation + currentLoanAllocation;
      
      if (currentTotalAllocation > 0.0) {
         currentRelativeLoanAllocation = currentLoanAllocation
               / currentTotalAllocation;
         currentRelativeStockAllocation = currentStockAllocation
               / currentTotalAllocation;
      } else {
         currentRelativeLoanAllocation = 0.0;
         currentRelativeStockAllocation = 0.0;
      }
   }
   
   protected void calculateLoanInformation(StrategyBank bank) {
      /*
       * TODO Reconsider: I'm not convinced these estimated values are correct,
       * but for now just want some kind of estimate of return/earnings so this
       * will probably do
       */
      totalFirmLending = 0.0;
      averageFirmLendingRate = 0.0;
      totalLendingReturn = 0.0;
      
      double totalLendingRate = 0.0;
      
      for (Iterator<Loan> ls = bank.getAssetLoans().iterator(); ls.hasNext();) {
         Loan loan = ls.next();
         totalFirmLending += loan.getLoanPrincipalValue();
         totalLendingRate += loan.getInterestRate()
               * loan.getLoanPrincipalValue();
         totalLendingReturn += loan.getValue() - loan.getLoanPrincipalValue();
      }
      averageFirmLendingRate = totalLendingRate / totalFirmLending;
   }
   
   protected void calculateStockInformation(StrategyBank bank) {
      /*
       * Apparently the market value may not be so useful if it hasn't been
       * traded recently, but this is probably acceptable as a rough estimate
       */
      averageStockReturn = 0.0;
      
      double totalStockPrice = 0.0;
      double totalStockReturn = 0.0;
      
      for (int i = 0; i < firms.size(); i++) {
         double marketValue = ((StockReleasingFirm) firms.get(i))
               .getMarketValue();
         
         double returnFromStock = 0;
         if (marketValue > 0) {
            returnFromStock = ((StockReleasingFirm) firms.get(i))
                  .getDividendPerShare() / marketValue;
         }
         
         stockPrices.set(i, marketValue);
         stockReturns.set(i, returnFromStock);
         
         totalStockPrice += marketValue;
         totalStockReturn += returnFromStock;
         
      }
      averageStockPrice = totalStockPrice / firms.size();
      averageStockReturn = totalStockReturn / firms.size();
   }
   
   protected void calculateInterbankInformation(StrategyBank bank) {
      // TODO add something if useful
   }
   
   protected void placeLoanOrders(StrategyBank bank,
         ArrayList<LoanOrderContainer> loanOrders) {
      // TODO this for loop shouldn't be necessary: should have easy access to
      // CommercialLoanMarket(s) from bank?
      for (final Market market : bank
            .getMarketsOfType(CommercialLoanMarket.class)) {
         try {
            for (int i = 0; i < loanOrders.size(); i++) {
               ((CommercialLoanMarket) market)
                     .addOrder(bank, LOAN_DURATION,
                           loanOrders.get(i).orderVolume,
                           loanOrders.get(i).orderRate);
            }
         } catch (OrderException e) {
            e.printStackTrace();
            Simulation.getSimState().finish();
         } catch (AllocationException e) {
            e.printStackTrace();
            Simulation.getSimState().finish();
         }
      }
   }
   
   protected void placeStockOrders(StrategyBank bank,
         ArrayList<StockOrderContainer> stockOrders) {
      if (stockOrders != null) {
         for (final Market market : bank.getMarketsOfType(StockMarket.class)) {
            for (int i = 0; i < stockOrders.size(); i++) {
               try {
                  ((StockMarket) market).addSellOrder(bank,
                        stockOrders.get(i).stockName,
                        stockOrders.get(i).orderVolume,
                        stockOrders.get(i).orderPrice);
               } catch (AllocationException e) {
                  e.printStackTrace();
                  Simulation.getSimState().finish();
               } catch (OrderException e) {
                  e.printStackTrace();
                  Simulation.getSimState().finish();
               }
            }
         }
      }
   }
   
   protected void placeInterbankOrder(StrategyBank bank,
         ArrayList<InterbankOrderContainer> interbankOrders) {
      if (interbankOrders != null) {
         for (final Market market : bank
               .getMarketsOfType(InterbankLoanMarket.class)) {
            try {
               for (int i = 0; i < interbankOrders.size(); i++) {
                  ((InterbankLoanMarket) market).addOrder(bank, 1,
                        interbankOrders.get(i).orderVolume,
                        interbankOrders.get(i).orderRate);
               }
            } catch (final OrderException e) {
               e.printStackTrace();
               Simulation.getSimState().finish();
            } catch (AllocationException e) {
               e.printStackTrace();
               Simulation.getSimState().finish();
            }
         }
      }
   }
   
   protected void payDividends(StrategyBank bank, double dividendsToBePaid) {
      if (dividendsToBePaid < 0.0)
         throw new IllegalArgumentException("Must pay positive or 0 dividends");
      
      bank.setDividendPerShare(dividendsToBePaid
            / bank.getNumberOfEmittedShares());
   }
   
   protected class LoanOrderContainer {
      public double orderRate;
      public double orderVolume;
      
      public LoanOrderContainer(double rate, double volume) {
         if (rate < 0.0)
            throw new IllegalArgumentException("Rate must be positive");
         
         if (volume < 0.0)
            throw new IllegalArgumentException("Volume must be positive");
         
         orderRate = rate;
         orderVolume = volume;
      }
   }
   
   protected class StockOrderContainer {
      public String stockName;
      public double orderPrice;
      public int orderVolume;
      
      public StockOrderContainer(String name, int volume, double price) {
         if (price < 0.0)
            throw new IllegalArgumentException("Price must be positive");
         
         if (volume < 0.0)
            throw new IllegalArgumentException("Volume must be positive");
         
         stockName = name;
         volume = orderVolume;
         price = orderPrice;
      }
   }
   
   protected class InterbankOrderContainer {
      public double orderRate;
      public double orderVolume;
      
      public InterbankOrderContainer(double rate, double volume) {
         if (rate < 0.0)
            throw new IllegalArgumentException("Rate must be positive");
         
         if (volume < 0.0)
            throw new IllegalArgumentException("Volume must be positive");
         
         orderRate = rate;
         orderVolume = volume;
      }
   }
   
}
