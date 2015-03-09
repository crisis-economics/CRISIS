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
  * A storage object for general resources and inventory.
  * @author phillips
  */
public interface Storage {
   /**
     * Get the quantity currently stored in this object.
     */
   public double getStoredQuantity();
   
   /**
     * Is this storage object empty?
     */
   public boolean isEmpty();
   
   /**
     * Immediately remove a quantity (x) from the store. The argument x
     * to this method:
     *   (a) should be non-negative, or else the state of the object 
     *       is unchanged, and this method returns zero;
     *   (b) should be less than y = getStoredQuantity() at the instant
     *       of the call. If x > y then x is silently trimmed to y and
     *       this method returns a number less than or equal to y.
     * @return
     *   The amount actually removed from the store.
     * 
     * It is not guaranteed that pull(x) will result in x units being
     * removed from storage. However it is guarenteed that the amount 
     * removed from storage will not be greater than x.
     */
   public double pull(final double positiveAmount);
   
   /**
     * Immediately add a quantity to the store. If the argument (x) is
     * negative or zero, no action is taken. Otherwise, if x > 0, this 
     * method cannot fail and x units will be immediately added to the 
     * store.
     */
   public void push(final double positiveAmount);
   
   /** 
     * This method behaves as Storage.push with a delay. The amount x
     * specified in the argument will be treated as a pending, and 
     * will not appear in the store until Storage.update() is called.
     */
   public void pushWithDelay(double positiveAmount);
   
   /**
     * Set the stored quantity. For arguments x:  if x < 0 then x is
     * treated as zero. This method returns the value of getStoredQuantity
     * for this object when the operation is complete.
     */
   public double setStoredQuantity(double amount);
   
   /**
     * Completely remove all contents (pending and otherwise) from 
     * this store.
     **/
   public void flush();
}