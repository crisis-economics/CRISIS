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

/**
  * Skeletal implementation of the Allocating interface.
  * 
  * Inheritance: implement getMaximumAllocatable. All other methods
  * are finalized.
  * @author phillips
  */
public abstract class AbstractAllocator implements Allocating {
   private double reservedQuantity;
   
   public AbstractAllocator() {
      this.reservedQuantity = 0.;
   }
   
   /**
     * Get the maximum quantity that can currently be allocated.
     * This method should never return a negative value.
     */
   @Override
   public abstract double getMaximumAllocatable();
   
   @Override
   public final double setAllocated(double posiveAmount) {
      posiveAmount = Math.min(getMaximumAllocatable(), posiveAmount);
      reservedQuantity = Math.max(posiveAmount, 0.);
      return reservedQuantity;
   }
   
   @Override
   public final double setUnallocated(double positiveAmount) {
      setAllocated(getMaximumAllocatable() - positiveAmount);
      return getUnallocated();
   }
   
   @Override
   public final double changeAllocatedBy(double amount) {
      final double before = reservedQuantity;
      setAllocated(reservedQuantity + amount);
      return reservedQuantity - before;
   }
   
   @Override
   public final double getAllocated() {
      return reservedQuantity;
   }
   
   @Override
   public final double getUnallocated() {
      return Math.max(getMaximumAllocatable() - reservedQuantity, 0.);
   }
   
   @Override
   public final void allocateAll() {
      setAllocated(getMaximumAllocatable());
   }
   
   @Override
   public final void disallocateAll() {
      setAllocated(0.);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Allocator, reserved amount: " + getAllocated() + ", "
           + "unreserved amount: " + getUnallocated() + ", "
           + "maximum allocatable: " + getMaximumAllocatable() + ".";
   }
}
