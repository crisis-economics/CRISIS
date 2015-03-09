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
package eu.crisis_economics.abm.fund;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * Unit tests for the class {@link FloorInterpolator}.
  * 
  * @author phillips
  */
public class TestTrivialFund {
   
   private Simulation
      state;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "...");
      state = new EmptySimulation(0L);
      state.start();
   }
   
   private void advanceTimeByOneCycle() {
      final double timeNow = state.schedule.getTime();
      while(state.schedule.getTime() < timeNow + 1)
         state.schedule.step(state);
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void event() { }
   
   /**
     * Test whether an instance of {@link FloorInterpolator} interpolates
     * a simple discrete input series as expected. This unit test operates
     * as follows:<br><br>
     * 
     * {@code (a)}
     * {@code (b)}
     * {@code (c)}
     */
   @Test
   public void testTrivialFundSettlesOrdersAsExpected() {
      final int
         numHouseholds = 100;
      Bank bank = new Bank(0.);
      Fund trivialFund = new TrivialFund(bank, new ClearingHouse());
      final HouseholdStub[] households = new HouseholdStub[numHouseholds];
      for(int i = 0; i< numHouseholds; ++i) {
         households[i] = new HouseholdStub(trivialFund, bank, 1.);
         try {
            trivialFund.invest(households[i], .5);          // 50% initial fund investment
         } catch (final InsufficientFundsException neverThrows) {
            Assert.fail();
         }
      }
      
      for(int i = 0; i< numHouseholds; ++i) {
         Assert.assertEquals(trivialFund.getBalance(households[i]), .5, 1.e-12);
         Assert.assertEquals(households[i].getTotalAssets(), 1., 1.e-12);
         Assert.assertEquals(households[i].getTotalLiabilities(), 0., 1.e-12);
         Assert.assertTrue(households[i].getAssets().size() == 2);
      }
      Assert.assertTrue(trivialFund.getTotalAssets() == numHouseholds * .5);
      Assert.assertEquals(trivialFund.getEquity(), 0., 1.e-12);
      
      Simulation.repeat(this, "event", NamedEventOrderings.BEFORE_ALL);
      
      while(state.schedule.getTime() < 50)
         state.schedule.step(state);
      
      // Examine the state of the fund after 100 timesteps. No change should be observed:
      
      for(int i = 0; i< numHouseholds; ++i) {
         Assert.assertEquals(trivialFund.getBalance(households[i]), .5, 1.e-12);
         Assert.assertEquals(households[i].getTotalAssets(), 1., 1.e-12);
         Assert.assertEquals(households[i].getTotalLiabilities(), 0., 1.e-12);
         Assert.assertTrue(households[i].getAssets().size() == 2);
      }
      Assert.assertTrue(trivialFund.getTotalAssets() == numHouseholds * .5);
      Assert.assertEquals(trivialFund.getEquity(), 0., 1.e-12);
      
      // All households request a withdrawal of .25 units cash:
      
      for(int i = 0; i< numHouseholds; ++i)
         try {
            trivialFund.requestWithdrawal(households[i], .25);
         } catch (final InsufficientFundsException neverThrows) {
            Assert.fail();
         }
      
      // The withdrawal request should no be processed immediately:
      
      for(int i = 0; i< numHouseholds; ++i) {
         Assert.assertEquals(trivialFund.getBalance(households[i]), .5, 1.e-12);
         Assert.assertEquals(households[i].getTotalAssets(), 1., 1.e-12);
         Assert.assertEquals(households[i].getTotalLiabilities(), 0., 1.e-12);
         Assert.assertTrue(households[i].getAssets().size() == 2);
      }
      Assert.assertTrue(trivialFund.getTotalAssets() == numHouseholds * .5);
      Assert.assertEquals(trivialFund.getEquity(), 0., 1.e-12);
      
      advanceTimeByOneCycle();
      
      for(int i = 0; i< numHouseholds; ++i) {
         Assert.assertEquals(trivialFund.getBalance(households[i]), .25, 1.e-12);
         Assert.assertEquals(households[i].getTotalAssets(), 1., 1.e-12);
         Assert.assertEquals(households[i].getTotalLiabilities(), 0., 1.e-12);
         Assert.assertTrue(households[i].getAssets().size() == 2);
      }
      Assert.assertTrue(trivialFund.getTotalAssets() == numHouseholds * .25);
      Assert.assertEquals(trivialFund.getEquity(), 0., 1.e-12);
      
      // No futher change is expected:
      
      while(state.schedule.getTime() < 100)
         state.schedule.step(state);
      
      for(int i = 0; i< numHouseholds; ++i) {
         Assert.assertEquals(trivialFund.getBalance(households[i]), .25, 1.e-12);
         Assert.assertEquals(households[i].getTotalAssets(), 1., 1.e-12);
         Assert.assertEquals(households[i].getTotalLiabilities(), 0., 1.e-12);
         Assert.assertTrue(households[i].getAssets().size() == 2);
      }
      Assert.assertTrue(trivialFund.getTotalAssets() == numHouseholds * .25);
      Assert.assertEquals(trivialFund.getEquity(), 0., 1.e-12);
      
      // All households invest .25 units cash:
      
      for(int i = 0; i< numHouseholds; ++i)
         try {
            trivialFund.invest(households[i], .25);
         } catch (final InsufficientFundsException neverThrows) {
            Assert.fail();
         }
      
      // The change is expected to be immediate:
      
      for(int i = 0; i< numHouseholds; ++i) {
         Assert.assertEquals(trivialFund.getBalance(households[i]), .5, 1.e-12);
         Assert.assertEquals(households[i].getTotalAssets(), 1., 1.e-12);
         Assert.assertEquals(households[i].getTotalLiabilities(), 0., 1.e-12);
         Assert.assertTrue(households[i].getAssets().size() == 2);
      }
      Assert.assertTrue(trivialFund.getTotalAssets() == numHouseholds * .5);
      Assert.assertEquals(trivialFund.getEquity(), 0., 1.e-12);
      
      // No futher change is expected:
      
      while(state.schedule.getTime() < 100)
         state.schedule.step(state);
      
      for(int i = 0; i< numHouseholds; ++i) {
         Assert.assertEquals(trivialFund.getBalance(households[i]), .5, 1.e-12);
         Assert.assertEquals(households[i].getTotalAssets(), 1., 1.e-12);
         Assert.assertEquals(households[i].getTotalLiabilities(), 0., 1.e-12);
         Assert.assertTrue(households[i].getAssets().size() == 2);
      }
      Assert.assertTrue(trivialFund.getTotalAssets() == numHouseholds * .5);
      Assert.assertEquals(trivialFund.getEquity(), 0., 1.e-12);
      
      // Debit the fund with a significant amount of cash:
      
      trivialFund.debit(numHouseholds * .5);
      
      advanceTimeByOneCycle();
      
      // Check whether the cash has been transferred to investors:
      
      for(int i = 0; i< numHouseholds; ++i) {
         Assert.assertEquals(trivialFund.getBalance(households[i]), 1., 1.e-12);
         Assert.assertEquals(households[i].getTotalAssets(), 1.5, 1.e-12);
         Assert.assertEquals(households[i].getTotalLiabilities(), 0., 1.e-12);
         Assert.assertTrue(households[i].getAssets().size() == 2);
      }
      Assert.assertTrue(trivialFund.getTotalAssets() == numHouseholds * 1.0);
      Assert.assertEquals(trivialFund.getEquity(), 0., 1.e-12);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println(getClass().getSimpleName() + " tests pass.");
      state.finish();
   }
   
   /*
    * Manual entry point.
    */
   static public void main(final String[] args) {
      try {
         final TestTrivialFund
            test = new TestTrivialFund();
         test.setUp();
         test.testTrivialFundSettlesOrdersAsExpected();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
