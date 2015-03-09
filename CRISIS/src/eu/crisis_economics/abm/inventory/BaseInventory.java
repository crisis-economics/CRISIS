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
package eu.crisis_economics.abm.inventory;

import eu.crisis_economics.utilities.StateVerifier;

class BaseInventory implements Inventory {
   private final Storage storage;
   private final FlowMemory flowMemory;
   private final Allocating allocator;
   
   /**
     * Construct an inventory structure with initialAmount units
     * of initial stock.
     */
   public BaseInventory(Storage storage) {
      StateVerifier.checkNotNull(storage);
      this.storage = storage;
      this.flowMemory = new SimpleFlowMemory();
      this.allocator = new SimpleStorageAllocator(storage);
   }
   
   public final void recordInflow(double positiveAmount) {
      flowMemory.recordInflow(positiveAmount);
   }
   
   public final void recordOutflow(double positiveAmount) {
      flowMemory.recordOutflow(positiveAmount);
   }
   
   public void recordFlow(double amount) {
      flowMemory.recordFlow(amount);
   }
   
   public final double netFlowAtThisTime() {
      return flowMemory.netFlowAtThisTime();
   }
   
   public final double totalInflowAtThisTime() {
      return flowMemory.totalInflowAtThisTime();
   }
   
   public final double totalOutflowAtThisTime() {
      return flowMemory.totalOutflowAtThisTime();
   }
   
   public final double getMaximumAllocatable() {
      return allocator.getMaximumAllocatable();
   }

   public final double getAllocated() {
      return allocator.getAllocated();
   }

   public final double setAllocated(double posiveAmount) {
      return allocator.setAllocated(posiveAmount);
   }

   public final double setUnallocated(double positiveAmount) {
      return allocator.setUnallocated(positiveAmount);
   }

   public final double changeAllocatedBy(double volume) {
      return allocator.changeAllocatedBy(volume);
   }

   public final double getUnallocated() {
      return allocator.getUnallocated();
   }
   
   public final void allocateAll() {
      allocator.allocateAll();
   }
   
   public final void disallocateAll() {
      allocator.disallocateAll();
   }
   
   public final double getStoredQuantity() {
      return storage.getStoredQuantity();
   }
   
   public final boolean isEmpty() {
      return storage.isEmpty();
   }
   
   public final double pull(double positiveAmount) {
      positiveAmount = Math.min(allocator.getUnallocated(), positiveAmount);
      double result = storage.pull(positiveAmount);
      if(result > 0.)
         flowMemory.recordOutflow(result);
      return result;
   }
   
   public final void push(double positiveAmount) {
      storage.push(positiveAmount);
      if(positiveAmount > 0.)
         flowMemory.recordInflow(positiveAmount);
   }
   
   public final void pushWithDelay(double positiveAmount) {
      storage.pushWithDelay(positiveAmount);
   }
   
   @Override
   public final double setStoredQuantity(double amount) {
      return storage.setStoredQuantity(amount);
   }
   
   public final void flush() {
      allocator.disallocateAll();
      flowMemory.flush();
      storage.flush();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Inventory, storage:" + storage + ", flow memory: " 
            + flowMemory + ", allocator: " + allocator + ".";
   }
}
