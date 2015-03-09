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

import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.FixedValueContract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A unit test for the {@link ExogenousDepositHolder} class.
  * 
  * @author phillips
  */
public final class TestExogenousDepositHolder {
   
   private Simulation
      state;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "...");
      state = new EmptySimulation(0L);
      state.start();
   }
   
   /**
     * Test whether an instance of {@link ExogenousDepositHolder} behaves as
     * expected. This test operates as follows:<br><br>
     * 
     * {@code (a)}
     *    One {@link TestExogenousDepositHolder} entity {@code E} is created;<br>
     * {@code (b)}
     *    One {@link DepositingHousehold} {@link Agent} {@code H} is created;<br>
     * {@code (c)}
     *    {@code H} opens a {@link DepositAccount} with {@code E} and deposits
     *    {@code £100}. The balance sheet of both participants is verified;<br>
     * {@code (d)}
     *    {@code H} withdraws their deposits from {@code E}. The balance sheet of 
     *    both participants is verified;<br>
     * {@code (e)}
     *    {@code H} attempts, and fails, to withdraw further cash from {@code E}.
     *    The balance sheet of both participants is verified.
     */
   @Test
   public void testExogenousDepositHolder() {
      DepositHolder
         depositHolder = new ExogenousDepositHolder();
      {
      final Contract
         liability = new FixedValueContract();
      depositHolder.addLiability(liability);
      
      Assert.assertTrue(depositHolder.getLiabilities().size() == 1);
      
      depositHolder.removeLiability(liability);
      
      Assert.assertTrue(depositHolder.getLiabilities().size() == 0);
      Assert.assertEquals(depositHolder.isBankrupt(), false);
      }
      
      final DepositingHousehold
         depositor = new DepositingHousehold(depositHolder);
      depositor.debit(100.);
      
      Assert.assertTrue(depositor.getAssets().size() == 1);
      Assert.assertEquals(depositor.getTotalAssets(), 100., 1.e-12);
      Assert.assertEquals(depositor.getTotalLiabilities(), 0., 0.);
      Assert.assertTrue(depositHolder.getLiabilities().size() == 1);
      Assert.assertEquals(depositHolder.getLiabilities().get(0).getValue(), 100., 1.e-12);
      
      try {
         depositor.credit(100.);
      } catch (final InsufficientFundsException neverThrows) {
         Assert.fail();
      }
      
      Assert.assertTrue(depositor.getAssets().size() == 1);
      Assert.assertEquals(depositor.getTotalAssets(), 0., 1.e-12);
      Assert.assertEquals(depositor.getTotalLiabilities(), 0., 0.);
      Assert.assertTrue(depositHolder.getLiabilities().size() == 1);
      Assert.assertEquals(depositHolder.getLiabilities().get(0).getValue(), 0., 1.e-12);
      
      {
      boolean
         raised = false;
      
      try {
         depositor.credit(1.);
      } catch (final InsufficientFundsException neverThrows) {
         raised = true;
      }
      if(!raised)
         Assert.fail();
      }
      
      Assert.assertTrue(depositor.getAssets().size() == 1);
      Assert.assertEquals(depositor.getTotalAssets(), 0., 1.e-12);
      Assert.assertEquals(depositor.getTotalLiabilities(), 0., 0.);
      Assert.assertTrue(depositHolder.getLiabilities().size() == 1);
      Assert.assertEquals(depositHolder.getLiabilities().get(0).getValue(), 0., 1.e-12);
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
         final TestExogenousDepositHolder
            test = new TestExogenousDepositHolder();
         test.setUp();
         test.testExogenousDepositHolder();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
