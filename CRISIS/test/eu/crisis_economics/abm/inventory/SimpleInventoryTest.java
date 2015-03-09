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
package eu.crisis_economics.abm.inventory;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.simulation.EmptySimulation;

public class SimpleInventoryTest {
   private SimState state;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing SimpleInventoryTest..");
      state = new EmptySimulation(0L);
      state.start();
   }
   
   @Test
   public void testRegularInventory() {
      testInventory(new SimpleResourceInventory());
   }
   
   @Test
   public void testCashLedger() {
      DepositHolder bank = new Bank(1000);
      Depositor depositor = new DepositingHousehold(bank, 0.);
      DepositAccount account = new DepositAccount(bank, depositor, 0., 0.);
      Inventory ledger = new CashLedger(account);
      testInventory(ledger);
      Assert.assertEquals(ledger.getStoredQuantity(), account.getValue(), 1.e-12);
      Assert.assertEquals(account.getValue(), 10., 1.e-10);
   }
   
   /**
     * This unit test inspects the state of an inventory object following
     * a sequence of operations. The numbers indicated in this test have
     * been calculated manually.
     */
   private void testInventory(Inventory inventory) {
      int lastCycle = 0;
      inventory.push(100.);
      
      Assert.assertEquals(100., inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(100., inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0. ,  inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(100., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(0.,   inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(100., inventory.getUnallocated(),         1.e-12);
      
      inventory.setAllocated(10.);
      
      Assert.assertEquals(100., inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(100., inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0. ,  inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(100., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(10.,  inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(90.,  inventory.getUnallocated(),         1.e-12);
      
      while(state.schedule.getTime() < lastCycle + 1) {                 // Advance by one cycle.
         state.schedule.step(state);
      }
      ++lastCycle;
      
      Assert.assertEquals(0.,   inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(0.,   inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0. ,  inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(100., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(10.,  inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(90.,  inventory.getUnallocated(),         1.e-12);
      
      inventory.push(100.);
      inventory.setAllocated(10.);
      
      Assert.assertEquals(100., inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(100., inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0. ,  inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(200., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(10.,  inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(190., inventory.getUnallocated(),         1.e-12);
      
      inventory.changeAllocatedBy(-5.);
      
      Assert.assertEquals(100., inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(100., inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0. ,  inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(200., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(5.,   inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(195., inventory.getUnallocated(),         1.e-12);
      
      inventory.changeAllocatedBy(+10.);
      
      Assert.assertEquals(100., inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(100., inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0. ,  inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(200., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(15.,  inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(185., inventory.getUnallocated(),         1.e-12);
      
      inventory.push(10.);
      
      Assert.assertEquals(110., inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(110., inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0. ,  inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(210., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(15.,  inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(195., inventory.getUnallocated(),         1.e-12);
      
      inventory.pull(10.); 
      
      Assert.assertEquals(100., inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(110., inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(10. , inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(200., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(15.,  inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(185., inventory.getUnallocated(),         1.e-12);
      
      Assert.assertEquals(inventory.pull(100.), 100., 1.e-10);
      
      Assert.assertEquals(0.,   inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(110., inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(110., inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(100., inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(15.,  inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(85.,  inventory.getUnallocated(),         1.e-12);
      
      inventory.flush();
      
      Assert.assertEquals(0.,   inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(0.,   inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0.,   inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(0.,   inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(0.,   inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(0.,   inventory.getUnallocated(),         1.e-12);
      
      inventory.pushWithDelay(10.);
      
      Assert.assertEquals(0.,   inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(0.,   inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0.,   inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(0.,   inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(0.,   inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(0.,   inventory.getUnallocated(),         1.e-12);
      
      while(state.schedule.getTime() < lastCycle + 1) {                 // Advance by one cycle.
         state.schedule.step(state);
      }
      ++lastCycle;
      
      Assert.assertEquals(0.,   inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(0.,   inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0.,   inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(10.,  inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(0.,   inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(10.,  inventory.getUnallocated(),         1.e-12);
      
      inventory.pushWithDelay(-10.);                                        // No action.
      
      while(state.schedule.getTime() < lastCycle + 1) {                  // Advance by one cycle.
         state.schedule.step(state);
      }
      ++lastCycle;
      
      Assert.assertEquals(0.,   inventory.netFlowAtThisTime(),      1.e-12);
      Assert.assertEquals(0.,   inventory.totalInflowAtThisTime(),  1.e-12);
      Assert.assertEquals(0.,   inventory.totalOutflowAtThisTime(), 1.e-12);
      Assert.assertEquals(10.,  inventory.getStoredQuantity(),      1.e-12);
      Assert.assertEquals(0.,   inventory.getAllocated(),           1.e-12);
      Assert.assertEquals(10.,  inventory.getUnallocated(),         1.e-12);
      
      state.finish();
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("SimpleInventoryTest tests pass.");
      state.finish();
      state = null;
   }
   
   static public void main(String[] args) {
      try {
         SimpleInventoryTest test = new SimpleInventoryTest();
         test.setUp();
         test.testRegularInventory();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
      
      try {
         SimpleInventoryTest test = new SimpleInventoryTest();
         test.setUp();
         test.testCashLedger();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
   }
}
