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

import java.util.Map;

/** 
  * An interface for a repository. A repository is a collection of differentiated
  * inventories. For example, a goods respository is a collection of a goods
  * stores. Each store is labelled by the type of goods it contains.
  * @author phillips
  */
public interface Repository<K, T> extends Map<K, T> {
   /**
     * Add a quantity to the inventory with the specified key.
     * @param inventoryType
     *        The inventory to use.
     * @param quantity (Q)
     *        The amount to add. If Q <= 0, no action is taken.
     */
   public void push(
      final K inventoryType,
      double quantity
      );
   
   /**
     * This method behaves as Repository.push with a delay. The amount
     * Q specified in the argument will be treated as pending, and 
     * will not appear in the store until a set amount of time has
     * elapsed. The delay period is specified by the inventory 
     * container with key K.
     *
     * @param inventoryType
     *        The inventory space use.
     * @param quantity (Q)
     *        The amount to add. If Q <= 0, no action is taken.
     */
   public void pushWithDelay(
      final K inventoryType,
      double quantity
      );
   
   /**
     * Add a quantity to the inventory with the specified key.
     * @param inventoryType
     *        The inventory to use.
     * @param quantity (Q)
     *        The amount to remove. If Q <= 0, no action is taken. If
     *        Q > M where M is the amount the store can release, Q
     *        is silently trimmed to M.
     * @return
     *        The amount actually removed from the repository. If no
     *        action was taken, or if no inventory with the specified
     *        key exists, this method returns 0.
     */
   public double pull(
      final K inventoryType,
      double quantity
      );
   
   /**
     * Get the amount of type K that is currently owned.
     */
   public double getStoredQuantity(final K inventoryType);
   
   /**
     * Get the maximum quantity of type K this object can currently allocate.
     * This method cannot return a negative value.
     */
   public double getMaximumAllocatable(final K inventoryType);
   
   /**
     * Get the amount of type K that is currently allocated (aka. reserved,
     * blocked from use).
     */
   public double getAllocated(final K inventoryType);
   
   /**
     * Set the amount x of type K that is currently allocated (aka. reserved,
     * blocked from use).
     * 
     * If the argument x to this method is less than y = getMaximumAllocatable(),
     * x is silently trimmed to y. If x is negative, x is replaced with 0.
     * 
     * @return
     *    The value of getAllocated(k), immediately after the operation.
     */
   public double setAllocated(final K inventoryType, double posiveAmount);
   
   /**
     * For arguments (k, x), this method is equivalent to setAllocated(k,
     * getMaximumAllocatable(k) - x)}.
     * 
     * @return
     *    The value of getUnallocated(k), immediately after the operation.
     */
   public double setUnallocated(final K inventoryType, double positiveAmount);
   
   /**
     * For arguments (k, x), this method is equivalent to setAllocated(k,
     * getAllocated(k) + x)}.
     * 
     * @return
     *    The signed amount that was allocated (negative return values
     *    indicate a disallocation).
     */
   public double changeAllocatedQuantityBy(final K inventoryType, double volume);
   
   /**
     * Get the amount of type K that is currently not allocated (aka. unreserved,
     * unblocked).
     */
   public double getUnallocated(final K inventoryType);
   
   /**
     * For argument k, this method is equivalent to setAllocated(k,
     * getMaximumAllocatable(k)).
     */
   public void allocateAll(final K inventoryType);
   
   /**
     * For argument k, this method is equivalent to setAllocatedQuantity(0.).
     */
   public void disallocateAll(final K inventoryType);
}
