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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.model.parameters.FunctionParameter;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.FundFactory;

/**
  * A unit test for the {@link ExogenousFund} class.
  * 
  * @author phillips
  */
public final class TestExogenousFund {
   
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
   
   /**
     * Test whether an instance of {@link ExogenousFund} behaves as
     * expected. This test operates as follows:<br><br>
     * 
     * {@code (a)}
     *   An {@link ExogenousFund} entity is created. The deposits of this
     *   {@link Fund} are held by an instance of {@link ExogenousDepositHolder};<br>
     * {@code (b)}
     *   The {@link ExogenousFund} is provided with {@code floor(t)} units of
     *   exogenous cash at simulation time {@code t}, for all {@code t};<br>
     * {@code (c)}
     *   One {@link DepositingHousehold} {@link Agent} opens an investment
     *   account with the {@link ExogenousFund}. {@code £1.0} is invested at
     *   the time the account is created;<br>
     * {@code (d)}
     *   Several simulation cycles elapse. After each cycle has elapsed, it is
     *   asserted that the exogenous fund income has been transferred to the 
     *   {@link DepositingHousehold}.
     */
   @Test
   public void testExogenousFundWithExogenousIncome() {
      final ExogenousDepositHolder
         depositHolder = new ExogenousDepositHolder();
      final ExogenousFund
         fund = new ExogenousFund(
            depositHolder, 
            new FundFactory() {
               @Override
               public Fund create(final DepositHolder depositHolder) {
                  return new TrivialFund(depositHolder, new ClearingHouse());
               }
            },
            new FunctionParameter(
               new UnivariateFunction() {
                  @Override
                  public double value(double t) {
                     return Math.floor(t);
                  }
               },
               "Parameter")
            );
      
      final DepositingHousehold
         household = new DepositingHousehold(depositHolder);
      household.debit(1.0);
      try {
         fund.invest(household, 1.);
      } catch (final InsufficientFundsException neverThrows) {
         Assert.fail();
      }
      
      advanceTimeByOneCycle();
      
      advanceTimeByOneCycle();  // Fires at T = 0
      
      advanceTimeByOneCycle();  // Fires at T = 1
      
      // At this point the exogenous fund has been debitted with £1.0
      // from exogenous sources. This income should have been passed to
      // clients.
      
      Assert.assertEquals(household.getTotalAssets(), 2.0, 1.e-12);
      
      advanceTimeByOneCycle();  // Fires at T = 2
      
      Assert.assertEquals(household.getTotalAssets(), 4.0, 1.e-12);
      
      advanceTimeByOneCycle();  // Fires at T = 3
      
      Assert.assertEquals(household.getTotalAssets(), 7.0, 1.e-12);
      
      Assert.assertTrue(household.getTotalLiabilities() == 0.);
      Assert.assertEquals(fund.getTotalLiabilities(), 7.0, 1.e-12);
      Assert.assertEquals(fund.getTotalAssets(), 7.0, 1.e-12);
      Assert.assertEquals(fund.getBalance(household), 7.0, 1.e-12);
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
         final TestExogenousFund
            test = new TestExogenousFund();
         test.setUp();
         test.testExogenousFundWithExogenousIncome();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}