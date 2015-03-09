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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.bank.StockReleasingBank;
import eu.crisis_economics.abm.bank.StockTradingBank;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.TargetValueStockMarketResponseFunction;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

public class ClearingStockMarketsTest {
   private SimState state;
   private double timeToSample;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing TestClearingStockMarkets..");
      state = new EmptySimulation(0L);
      timeToSample =
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.AFTER_ALL, 2).getUnitIntervalTime();
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
   
   private void resetSimulation() {
      state.finish();
      state = new EmptySimulation(0L);
      state.start();
   }
   
   private void advanceToTimeZero() {
      while(state.schedule.getTime() < 0.)
         state.schedule.step(state);
   }
   
   @Test
   /**
     * This unit test generates random stock market clearing sessions,
     * processes all desired trades, and inspects the resulting state
     * of the economy. 
     * 
     * The test sequence is as follows:
     *    (a) a stock releasing bank is created;
     *    (b) a random number of stock-trading banks are created;
     *    (c) cash injection behaviour is disabled for all agents
     *        created in step (b); 
     *    (d) random initial shares are distributed to stock
     *        trading banks;
     *    (e) each stock trading bank is given a random stock
     *        investment target;
     *    (f) a clearing stock market is established, and processed;
     *    (g) it is asserted that: i. the stock price has been modified,
     *        and ii. that no cash has been created in the system.
     * 
     * Steps (a-g) are then repeated 200 times, afresh, for independent
     * randomly generated configurations generated configurations.
     */
   public void testSeveralRandomClearingMarketSessions() {
      int numTests = 200;
      for(int i = 0; i< numTests; ++i) {
         testRandomClearingMarketSession(i);
         resetSimulation();
      }
   }
   
   private void testRandomClearingMarketSession(int seed) {
      advanceToTimeZero();
      
      final Random
         dice = new Random(seed);
      final int
         numberOfStockBuyers = dice.nextInt(50) + 2;
      final double
         priceScaleForTransactions = 1.e10;
      final double[]
         desiredStockInvestments = new double[numberOfStockBuyers],
         initialBankCash = new double[numberOfStockBuyers],
         existingStocksHeld = new double[numberOfStockBuyers];
      final double
         initialPPS = dice.nextDouble() * priceScaleForTransactions + .1;
      
      final StockReleasingBank stockReleaser = new StockReleasingBank(100.);    // Stock releaser
      
      UniqueStockExchange.Instance.setStockPrice(stockReleaser.getUniqueName(), initialPPS);
      Assert.assertEquals(
         UniqueStockExchange.Instance.getStockPrice(stockReleaser), initialPPS, 0.) ;
      
      for(int i = 0; i< numberOfStockBuyers; ++i) {
         desiredStockInvestments[i] = dice.nextDouble() * priceScaleForTransactions + .1;
         initialBankCash[i] = dice.nextDouble() * priceScaleForTransactions + .1;
         existingStocksHeld[i] = dice.nextDouble() * (1. / numberOfStockBuyers);
      }
      
      class CustomBank extends StockTradingBank
         implements ClearingMarketParticipant {                                  // Stock buyers
         private final double
            targetStockInvestment;
         public CustomBank(
            double initialCash, double targetStockInvestment) {
            super(initialCash);
            this.targetStockInvestment = targetStockInvestment;
         }
         @Override
         public MarketResponseFunction getMarketResponseFunction(
            final ClearingMarketInformation marketInformation
            ) {
            return new TargetValueStockMarketResponseFunction(                   // Stock Market
               super.getNumberOfSharesOwnedIn(stockReleaser.getUniqueName()), 
               targetStockInvestment);
         }
         
         @Override
         public void cashFlowInjection(double amount) { }                        // No CFIs.
         
         @Override
         public double credit(double amount) throws InsufficientFundsException {
            if(amount <= 0.) return 0.;
            if(amount > getCashReserveValue())
               throw new InsufficientFundsException();
            decreaseCashReserves(amount);
            return amount;
         }
      }
      
      ClearingHouse clearingHouse = new ClearingHouse();
      clearingHouse.addMarket(new ClearingStockMarket(stockReleaser, clearingHouse));
      
      final CustomBank[] stockBuyers = new CustomBank[numberOfStockBuyers];
      for(int i = 0; i< numberOfStockBuyers; ++i) {
         stockBuyers[i] = new CustomBank(initialBankCash[i], desiredStockInvestments[i]);
         UniqueStockExchange.Instance.generateFreeSharesFor(
            stockReleaser.getUniqueName(), stockBuyers[i], existingStocksHeld[i]);
         Assert.assertEquals(stockBuyers[i].getNumberOfSharesOwnedIn(
            stockReleaser.getUniqueName()), existingStocksHeld[i], 1.e-10);
         clearingHouse.addStockMarketParticipant(stockBuyers[i]);
      }
      
      // Record the total initial cash in this system:
      
      double initialCashInSystem = 0.;
      for(CustomBank bank : stockBuyers)
         initialCashInSystem += bank.getCashReserveValue();
      initialCashInSystem += stockReleaser.getCashReserveValue();
      
      advanceUntilSampleTime();
      advanceUntilSampleTime(); // first clearing market session has been processed.
      
      Assert.assertTrue(initialPPS != UniqueStockExchange.Instance.getStockPrice(stockReleaser));
      
      // Test no cash has been created during the clearing process:
      
      double finalCashInSystem = 0.;
      for(CustomBank bank : stockBuyers)
         finalCashInSystem += bank.getCashReserveValue();
      finalCashInSystem += stockReleaser.getCashReserveValue();
      
      Assert.assertEquals(
         initialCashInSystem, finalCashInSystem,
         Math.max(initialCashInSystem, finalCashInSystem) * 1.e-10
         );
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("TestClearingStockMarkets tests pass.");
      state.finish();
      state = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         ClearingStockMarketsTest test = new ClearingStockMarketsTest();
         test.setUp();
         test.testSeveralRandomClearingMarketSessions();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
   }
}
