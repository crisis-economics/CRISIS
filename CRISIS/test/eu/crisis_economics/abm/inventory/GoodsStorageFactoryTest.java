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
import eu.crisis_economics.abm.inventory.goods.GoodsStorage;
import eu.crisis_economics.abm.inventory.goods.GoodsInventoryFactory;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

public class GoodsStorageFactoryTest {
   private SimState state;
   private double timeToSample;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing GoodsStorageFactory..");
      state = new EmptySimulation(0L);
      state.start();
      // Sample goods storage spaces 2*delta after BEFORE_ALL.
      timeToSample =
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.BEFORE_ALL, 2).getUnitIntervalTime();
   }
   
   private void advanceToTimeZero() {
      while(state.schedule.getTime() < 0.)
         state.schedule.step(state);
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
   
   @Test
   /**
     * Create a storage structure for goods with:
     *   (a) no time decay (no response to the passage of time), and
     *   (b) no change due to use (eg. in manufacturing).
     */
   public void testDurableIndestructibleStorageCreation() {
      final double initialAmount = 10.;
      // Goods with 0% time decay and 0% loss when used:
      final GoodsStorage store = GoodsInventoryFactory.createInventory(initialAmount, 0., 0.);
      advanceToTimeZero();
      store.pushWithDelay(initialAmount);
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      store.consume();
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      store.consume(initialAmount / 2.);
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(store.getStoredQuantity(), 2.*initialAmount, 0.);
   }
   
   @Test
   /**
     * Create a storage structure for goods with:
     *   (a) total (100%) decay every time the simulation clock advances,
     *   (b) no reusability (100% consumption).
     */
   public void testNonDurableStorageCreation() {
      final double initialAmount = 10.;
      // Goods with 100% decay in time and 100% loss when utilized (single-use):
      final GoodsStorage store = GoodsInventoryFactory.createInventory(initialAmount, 1., 1.);
      advanceToTimeZero();
      store.pushWithDelay(initialAmount);
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      store.consume();
      Assert.assertEquals(store.getStoredQuantity(), 0., 0.);
      store.consume(initialAmount / 2.);
      Assert.assertEquals(store.getStoredQuantity(), 0., 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(store.getStoredQuantity(), 0., 0.);
   }
   
   @Test
   /**
     * Create a storage structure for goods with:
     *   (a) no response to the passage of time (zero decay), and
     *   (b) 70% per use.
     */
   public void testDurableGoodsStorageCreationWithZeroDecay() {
      final double initialAmount = 10.;
      // Goods with 0% decay in time and 70% loss per use:
      final GoodsStorage store = GoodsInventoryFactory.createInventory(initialAmount, 0., .7);
      advanceToTimeZero();
      store.pushWithDelay(initialAmount);
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      store.consume();
      Assert.assertEquals(store.getStoredQuantity(), .3 * initialAmount, 0.);
      store.consume(initialAmount / 4.);
      Assert.assertEquals(store.getStoredQuantity(), (.3 - .25 * .7) * initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(store.getStoredQuantity(), (1. + .3 - .25 * .7) * initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(store.getStoredQuantity(), (1. + .3 - .25 * .7) * initialAmount, 0.);
   }
   
   @Test
   /**
     * Create a storage structure for goods with:
     *   (a) 40% decay per unit time,
     *   (b) no consumption (infinite reusability).
     */
   public void testDurableGoodsStorageCreationWithZeroConsumption() {
      final double initialAmount = 10.;
      // Create a goods store with 40% loss per unit time and no consumption (infinite reuse):
      final GoodsStorage store = GoodsInventoryFactory.createInventory(initialAmount, .4, 0.);
      advanceToTimeZero();
      store.pushWithDelay(initialAmount);
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      store.consume();
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      store.consume(initialAmount / 2.);
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(store.getStoredQuantity(), (1. + .6) * initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(store.getStoredQuantity(), .6 * (1. + .6) * initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      Assert.assertEquals(store.getStoredQuantity(), .6 * .6 * (1. + .6) * initialAmount, 1.e-12);
   }
   
   @Test
   /**
     * Create a storage structure for goods with:
     *   (a) 60% decay per unit time,
     *   (b) 75% loss per use (consumption).
     */
   public void testDurableGoodsStorageCreationWithPartialDecayAndConsumption() {
      final double initialAmount = 10.;
      // 60% decay per unit time, 75% consumption per use:
      final GoodsStorage store = GoodsInventoryFactory.createInventory(initialAmount, .6, .75);
      advanceToTimeZero();
      store.pushWithDelay(initialAmount);
      Assert.assertEquals(store.getStoredQuantity(), initialAmount, 0.);
      store.consume();
      Assert.assertEquals(store.getStoredQuantity(), .25 * initialAmount, 0.);
      store.consume(initialAmount / 8.);
      Assert.assertEquals(store.getStoredQuantity(), (.125 + .03125) * initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      double expectedMultiplier = (1. + .4 * (.125 + .03125));
      
      Assert.assertEquals(store.getStoredQuantity(), expectedMultiplier * initialAmount, 0.);
      store.consume();
      expectedMultiplier *= .25;
      Assert.assertEquals(store.getStoredQuantity(), expectedMultiplier * initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      expectedMultiplier *= .4;
      Assert.assertEquals(store.getStoredQuantity(), expectedMultiplier * initialAmount, 0.);
      store.consume();
      expectedMultiplier *= .25;
      Assert.assertEquals(store.getStoredQuantity(), expectedMultiplier * initialAmount, 0.);
      
      advanceUntilSampleTime();
      
      expectedMultiplier *= .4;
      Assert.assertEquals(store.getStoredQuantity(), expectedMultiplier * initialAmount, 1.e-10);
      store.consume();
      expectedMultiplier *= .25;
      Assert.assertEquals(store.getStoredQuantity(), expectedMultiplier * initialAmount, 1.e-10);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("SimpleInventoryTest tests pass.");
      state.finish();
      state = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         GoodsStorageFactoryTest test = new GoodsStorageFactoryTest();
         test.setUp();
         test.testDurableIndestructibleStorageCreation();
         test.tearDown();
      } catch(final Exception e) {
         Assert.fail();
      }
      
      try {
         GoodsStorageFactoryTest test = new GoodsStorageFactoryTest();
         test.setUp();
         test.testNonDurableStorageCreation();
         test.tearDown();
      } catch(final Exception e) {
         Assert.fail();
      }
      
      try {
         GoodsStorageFactoryTest test = new GoodsStorageFactoryTest();
         test.setUp();
         test.testDurableGoodsStorageCreationWithZeroDecay();
         test.tearDown();
      } catch(final Exception e) {
         Assert.fail();
      }
      
      try {
         GoodsStorageFactoryTest test = new GoodsStorageFactoryTest();
         test.setUp();
         test.testDurableGoodsStorageCreationWithZeroConsumption();
         test.tearDown();
      } catch(final Exception e) {
         Assert.fail();
      }
      
      try {
         GoodsStorageFactoryTest test = new GoodsStorageFactoryTest();
         test.setUp();
         test.testDurableGoodsStorageCreationWithPartialDecayAndConsumption();
         test.tearDown();
      } catch(final Exception e) {
         Assert.fail();
      }
   }
}
