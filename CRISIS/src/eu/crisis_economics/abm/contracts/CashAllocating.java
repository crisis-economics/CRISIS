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
package eu.crisis_economics.abm.contracts;

/**
  * @author phillips
  */
public interface CashAllocating {
   /**
     * Try to allocate (block from alternate use) an amount of cash. 
     * The argument x to this method:
     *   (a) should be positive (x > 0), or else no action is taken;
     *   (b) be less than the cash liqudity, y, of the holder. If x > y,
     *       x is silently trimmed to y.
     * @return
     *    The amount allocated. This value cannot be negative.
     */
   public double allocateCash(double positiveAmount);
   
   /**
     * This method is the reverse of {@link CashAllocating.allocateCash}.
     * The argument x to this method:
     *   (a) should be positive (x > 0), or else no action is taken;
     *   (b) be less than the allocated cash, y, of the holder. If x > y,
     *       x is silently trimmed to y.
     * @return
     *    The amount disallocated. This value cannot be negative.
     */
   public double disallocateCash(double positiveAmount);
   
   /**
     * Get the amount of allocated cash. This value cannot be negative.
     */
   public double getAllocatedCash();
   
   /**
     * Get the amount of unallocated cash. This value cannot be negative.
     */
   public double getUnallocatedCash();
}
