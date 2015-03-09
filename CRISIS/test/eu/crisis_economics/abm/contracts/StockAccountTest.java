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
package eu.crisis_economics.abm.contracts;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.household.StockTradingHousehold;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

public class StockAccountTest {
   private SimState state;
   private double timeToSample;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing StockAccountTest..");
      state = new EmptySimulation(0L);
      timeToSample =
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.AFTER_ALL, 2).getUnitIntervalTime();
      state.start();
   }
   
   private void resetSimulation() {
      state.finish();
      state = new EmptySimulation(0L);
      state.start();
   }
   
   private void advanceUntilSampleTime() {
      double
         startTime = state.schedule.getTime(),
         startFloorTime = Math.floor(startTime),
         endTime = startFloorTime + timeToSample;
      if(endTime <= startTime)
         endTime += 1.;
      while(state.schedule.getTime() <= endTime)
         state.schedule.step(state);
   }
   
   private void advanceToTimeZero() {
      while(state.schedule.getTime() < 0.)
         state.schedule.step(state);
   }
   
   @Test
   /**
     * This unit test generates a random number of stock releasers,
     * share holders with random cash endowments, and random 
     * shareholder stock transactions. It is asserted that no shares
     * have been destroyed, and that no cash has been created, after
     * all share transactions have taken place.
     * 
     * The test sequence is as follows:
     *    (a) a unique deposit-holding bank is created;
     *    (b) a random number of stock releasers are created;
     *    (c) a random number of shareholders are created,
     *        each with a random cash endowment;
     *    (d) shares are randomly released to shareholders;
     *    (e) for each stock type, each stockholders sells
     *        a random number of stocks to each peer;
     *    (f) for each stock type, each stockholders transfers
     *        a random number of stocks to each peer at no cost;
     *    (g) it is asserted that: i. the cash in the system has
     *        been conserved, ii. the total number of shares
     *        in the system, for each distinct stock type, is 
     *        conserved; and iii. that nontrivial transactions
     *        have taken place despite i. and ii.
     * 
     * Steps (a-g) are then repeated 200 times, afresh, for independent
     * randomly generated configurations.
     * 
     * This unit tests additionally checks whether the stock exchange
     * is tracking the correct number of shares in existance following
     * steps (e) and (f).
     */
   public void testSeveralRandomShareSellingAndTransferSessions() {
      int numTests = 200;
      for(int i = 0; i< numTests; ++i) {
         testSharesAndCashAreConservedWhenUsingFundamentalStockAccountOperations(i);
         resetSimulation();
      }
   }
   
   private void testSharesAndCashAreConservedWhenUsingFundamentalStockAccountOperations(int seed) {
      final Random dice = new Random(seed);
      final int
         numStockReleasers = dice.nextInt(10) + 1,
         numStockHolders = dice.nextInt(10) + 1;
      final double
         stockPriceScale = 1.e5,
         initialDepositsScale = stockPriceScale * numStockReleasers * 10.;
      final Bank
         depositHolder = new Bank(100.0);
      StockReleaser[]
         stockReleasers = new StockReleasingFirm[numStockReleasers];
      for(int i = 0; i< numStockReleasers; ++i) {
         stockReleasers[i] =
            new StockReleasingFirm(depositHolder, 0., new InstanceIDAgentNameFactory());
         final double
            startingStockPrice = stockPriceScale * dice.nextDouble() + stockPriceScale / 1.e8;
         UniqueStockExchange.Instance.setStockPrice(
            stockReleasers[i].getUniqueName(), startingStockPrice);
      }
      StockHolder[]
         stockHolders = new StockHolder[numStockHolders];
      for(int i = 0; i< numStockHolders; ++i)
         stockHolders[i] = new StockTradingHousehold(depositHolder,
            initialDepositsScale * dice.nextDouble() + initialDepositsScale / 1.e8);
      double
         totalInitialCashInSystem = depositHolder.getCashReserveValue();
      double[]
         numberOfSharesInSystem = new double[numStockReleasers];
      
      advanceToTimeZero();
      advanceUntilSampleTime();
      
      // Issue random initial shares to stockholders:
      for(int i = 0; i< numStockHolders; ++i) {
         for(int j = 0; j< numStockReleasers; ++j) {
            final double
               sharesToGive = dice.nextDouble();
            try {
               stockReleasers[j].releaseShares(
                  sharesToGive,
                  UniqueStockExchange.Instance.getStockPrice(stockReleasers[j]),
                  stockHolders[i]
                  );
               System.out.printf("generating %16.10g shares for " + 
                  stockHolders[i].getUniqueName() + "\n", sharesToGive);
            } catch (InsufficientFundsException e) {
               System.out.printf("generating zero shares for " + 
                  stockHolders[i].getUniqueName() + "\n");
            }
         }
      }
      
      // Count the number of shares in existence:
      numberOfSharesInSystem = countSharesInExistence(stockHolders, stockReleasers);
      
      // Assert that the share volume is being tracked correctly:
      for(int j = 0; j< numStockReleasers; ++j) {
         Assert.assertEquals(
            UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockReleasers[j]),
            numberOfSharesInSystem[j],
            numberOfSharesInSystem[j] * 1.e-12
            );
      }
      
      for(int j = 0; j< numStockReleasers; ++j)
         performRandomShareSaleTransactions(stockHolders, stockReleasers[j], dice);
      
      // Recount sum the number of shares in existence:
      double[] newSharesInExistenace = countSharesInExistence(stockHolders, stockReleasers);
      for(int i = 0; i< newSharesInExistenace.length; ++i) {
         Assert.assertTrue(newSharesInExistenace[i] > 0.);
         Assert.assertEquals(newSharesInExistenace[i], numberOfSharesInSystem[i], 1.e-12);
      }
      
      // Assert no cash has been created:
      Assert.assertEquals(
         totalInitialCashInSystem,
         depositHolder.getCashReserveValue(),
         totalInitialCashInSystem * 1.e-10
         );
      
      // Perform a sequence of free share transactions:
      for(int j = 0; j< numStockReleasers; ++j)
         performRandomFreeShareExchanges(stockHolders, stockReleasers[j], dice);
      
      // Recount sum the number of shares in existence:
      newSharesInExistenace = countSharesInExistence(stockHolders, stockReleasers);
      for(int i = 0; i< newSharesInExistenace.length; ++i) {
         Assert.assertTrue(newSharesInExistenace[i] > 0.);
         Assert.assertEquals(newSharesInExistenace[i], numberOfSharesInSystem[i], 1.e-12);
      }
      
      // Assert no cash has been created:
      Assert.assertEquals(
         totalInitialCashInSystem,
         depositHolder.getCashReserveValue(),
         totalInitialCashInSystem * 1.e-10
         );
      
      System.out.printf("Session complete.\n");
   }
   
   /*
    * Perform a sequence of random share transactions.
    * 
    * This method generates random peer-to-peer share transactions
    * and asserts that stock account values are as expected following
    * each transaction.
    */
   private void performRandomShareSaleTransactions(
      StockHolder[] stockHolders, 
      StockReleaser stockReleaser,
      Random dice
      ) {
      double shareTradeVolume = 0;
      System.out.printf(
         "begin share transactions (share type " + stockReleaser.getUniqueName() + ")\n");
      for(int i = 0; i< stockHolders.length; ++i) {
         for(int j = 0; j< stockHolders.length; ++j) {
            final double
               sharesOwned =
                  stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName()),
               attemptedTransactionSize =
                  dice.nextDouble() * 1.1 * sharesOwned / stockHolders.length;
            StockAccount account =
               stockHolders[i].getStockAccount(stockReleaser.getUniqueName());
            if(account == null)
               continue;
            final double
               firstPartyInitialShares =
                  stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName()),
               secondPartyInitialShares =
                  stockHolders[j].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName());
            try {
               double tradeAmount = 
                  account.sellSharesTo(attemptedTransactionSize, stockHolders[j]);
               shareTradeVolume += tradeAmount;
               System.out.printf("share transaction: " +
                  stockHolders[i].getUniqueName() + " sells %16.10g shares to " +
                  stockHolders[j].getUniqueName() + ".\n", tradeAmount);
               if(i != j) {
                  Assert.assertEquals(
                     firstPartyInitialShares - tradeAmount,
                     stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName())
                     );
                  Assert.assertEquals(
                     secondPartyInitialShares + tradeAmount,
                     stockHolders[j].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName())
                     );
               }
               else {
                  Assert.assertEquals(
                     firstPartyInitialShares,
                     stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName()),
                     firstPartyInitialShares * 1.e-12
                     );
               }
            } catch (InsufficientFundsException e) {
               Assert.assertEquals(
                  firstPartyInitialShares,
                  stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName())
                  );
               Assert.assertEquals(
                  secondPartyInitialShares,
                  stockHolders[j].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName())
                  );
            }
         }
      }
      Assert.assertTrue(shareTradeVolume > 0.);
   }
   
   /*
    * Perform a sequence of free share transactions.
    */
   private void performRandomFreeShareExchanges(
      StockHolder[] stockHolders, 
      StockReleaser stockReleaser,
      Random dice
      ) {
      double shareTradeVolume = 0;
      System.out.printf(
         "begin free share exchanges (share type " + stockReleaser.getUniqueName() + ")\n");
      for(int i = 0; i< stockHolders.length; ++i) {
         for(int j = 0; j< stockHolders.length; ++j) {
            final double
               sharesOwned =
                  stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName()),
               attemptedTransactionSize =
                  dice.nextDouble() * 1.1 * sharesOwned / stockHolders.length;
            StockAccount account =
               stockHolders[i].getStockAccount(stockReleaser.getUniqueName());
            if(account == null)
               continue;
            final double
               firstPartyInitialShares =
                  stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName()),
               secondPartyInitialShares =
                  stockHolders[j].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName());
            double tradeAmount = 
               account.transferSharesTo(attemptedTransactionSize, stockHolders[j]);
            shareTradeVolume += tradeAmount;
            System.out.printf("share exchange: " +
               stockHolders[i].getUniqueName() + " gives %16.10g shares to " +
               stockHolders[j].getUniqueName() + ".\n", tradeAmount);
            if(i != j) {
               Assert.assertEquals(
                  firstPartyInitialShares - tradeAmount,
                  stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName())
                  );
               Assert.assertEquals(
                  secondPartyInitialShares + tradeAmount,
                  stockHolders[j].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName())
                  );
            }
            else {
               Assert.assertEquals(
                  firstPartyInitialShares,
                  stockHolders[i].getNumberOfSharesOwnedIn(stockReleaser.getUniqueName()),
                  firstPartyInitialShares * 1.e-12
                  );
            }
         }
      }
      Assert.assertTrue(shareTradeVolume > 0.);
   }
   
   /*
    * Count the number of shares in existence by summing the values of 
    * all known stock accounts.
    */
   private double[] countSharesInExistence(
      final StockHolder[] stockHolders, 
      final StockReleaser[] stockReleasers
      ) {
      double[] result = new double[stockReleasers.length];
      for(int i = 0; i< stockHolders.length; ++i) {
         for(int j = 0; j< stockReleasers.length; ++j) {
            result[j] += 
               stockHolders[i].getNumberOfSharesOwnedIn(stockReleasers[j].getUniqueName());
         }
      }
      return result;
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("StockAccountTest tests pass.");
      state.finish();
      state = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         StockAccountTest test = new StockAccountTest();
         test.setUp();
         test.testSeveralRandomShareSellingAndTransferSessions();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
   }
}
