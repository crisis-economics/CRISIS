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
package eu.crisis_economics.abm.strategy.leverage;

import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;

/**
  * An interface for target-leverage selection algorithm. No implementation
  * of this interface should require exclusive use by {@link Bank}
  * {@link Agent}{@code s}.
  * 
  * @author phillips
  */
public interface LeverageTargetAlgorithm {
   /**
     * Compute a new leverage target. The argument is not modified
     * by this operation.
     */
   public abstract void computeTargetLeverage(Portfolio portfolio);
   
   /** 
     * Get the most recent leverage target.
     */
   public abstract double getTargetLeverage();
   
   /** 
     * Set the value of leverage target. The argument should be non-negative.
     * If the argument is negative, it is silently trimmed to zero.
     */
   public abstract void setTargetLeverage(double value);
}