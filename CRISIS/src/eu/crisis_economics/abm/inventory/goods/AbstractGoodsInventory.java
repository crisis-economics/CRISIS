/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.inventory.goods;

import eu.crisis_economics.abm.inventory.AbstractPerishable;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.utilities.StateVerifier;

/** 
  * Skeletal implementation of the GoodsInventory interface.
  * @author phillips
  */
public abstract class AbstractGoodsInventory extends AbstractPerishable 
   implements GoodsInventory {
   
   private final Inventory inventory;
   
   public AbstractGoodsInventory(final Inventory inventory) {
      super(
         NamedEventOrderings.BEFORE_ALL,
         ScheduleIntervals.ONE_DAY
         );
      StateVerifier.checkNotNull(inventory);
      this.inventory = inventory;
   }
   
   public double getStoredQuantity() {
      return inventory.getStoredQuantity();
   }
   
   public double getMaximumAllocatable() {
      return inventory.getMaximumAllocatable();
   }
   
   public void recordInflow(double positiveAmount) {
      inventory.recordInflow(positiveAmount);
   }
   
   public boolean isEmpty() {
      return inventory.isEmpty();
   }
   
   public double getAllocated() {
      return inventory.getAllocated();
   }
   
   public double pull(double positiveAmount) {
      return inventory.pull(positiveAmount);
   }
   
   public double setStoredQuantity(double amount) {
      return inventory.setStoredQuantity(amount);
   }
   
   public void recordOutflow(double positiveAmount) {
      inventory.recordOutflow(positiveAmount);
   }
   
   public void recordFlow(double amount) {
      inventory.recordFlow(amount);
   }
   
   public double netFlowAtThisTime() {
      return inventory.netFlowAtThisTime();
   }
   
   public double totalInflowAtThisTime() {
      return inventory.totalInflowAtThisTime();
   }
   
   public double setAllocated(double posiveAmount) {
      return inventory.setAllocated(posiveAmount);
   }
   
   public double setUnallocated(double positiveAmount) {
      return inventory.setUnallocated(positiveAmount);
   }
   
   public double changeAllocatedBy(double volume) {
      return inventory.changeAllocatedBy(volume);
   }
   
   public void push(double positiveAmount) {
      inventory.push(positiveAmount);
   }
   
   public double getUnallocated() {
      return inventory.getUnallocated();
   }
   
   public double totalOutflowAtThisTime() {
      return inventory.totalOutflowAtThisTime();
   }
   
   public void allocateAll() {
      inventory.allocateAll();
   }
   
   public void disallocateAll() {
      inventory.disallocateAll();
   }
   
   public void pushWithDelay(double positiveAmount) {
      inventory.pushWithDelay(positiveAmount);
   }
   
   public void flush() {
      inventory.flush();
   }
   
   // Abstract methods
   
   /**
     * See {@link GoodsStorage.consume(double quantity)}.
     */
   @Override
   public abstract void consume(double quantity);
   
   /**
     * See {@link GoodsStorage.consume()}.
     */
   @Override
   public abstract void consume();
   
   /**
     * See {@link AbstractPerishable.applyPassageOfTime}.
     */
   @Override
   protected abstract void applyPassageOfTime(double timeHasElapsed);
}
