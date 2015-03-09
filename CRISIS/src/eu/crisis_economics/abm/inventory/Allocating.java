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
  * An interface for objects that allocate (aka. reserve, block) resources.
  * @author phillips
  */
public interface Allocating {
   /**
     * Get the maximum amount this object can currently allocate.
     * This method cannot return a negative value.
     */
   public double getMaximumAllocatable();
   
   /**
     * Get the amount that is currently allocated (aka. reserved,
     * blocked from use).
     */
   public double getAllocated();
   
   /**
     * Set the quantity currently allocated (aka. reserved, blocked
     * from use).
     * 
     * If the argument x to this method is less than y = 
     * getMaximumAllocatable(), x is silently trimmed to y. If x is
     * negative, x is replaced with 0.
     * 
     * @return
     *    The value of getAllocated, immediately after the operation.
     */
   public double setAllocated(double posiveAmount);
   
   /**
     * For arguments x, this method is equivalent to setAllocated(
     * getMaximumAllocatable() - x)}.
     * 
     * @return
     *    The value of getUnallocated, immediately after the operation.
     */
   public double setUnallocated(double positiveAmount);
   
   /**
     * For arguments x, this method is equivalent to setAllocated(
     * getAllocated() + x)}.
     * 
     * @return
     *    The signed amount that was allocated (negative return values
     *    indicate a disallocation).
     */
   public double changeAllocatedBy(double volume);
   
   /**
     * Get the amount that is currently not allocated (aka. unreserved,
     * unblocked).
     */
   public double getUnallocated();
   
   /**
     * This method is equivalent to setAllocated(getMaximumAllocatable()).
     */
   public void allocateAll();
   
   /**
     * This method is equivalent to setAllocatedQuantity(0.).
     */
   public void disallocateAll();
}
