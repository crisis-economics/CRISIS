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

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;

/**
  * A skeletal implementation of the {@link LeverageTargetAlgorithm} interface.
  * This implementation provides:<br><br>
  * 
  *    (a) memory of the last leverage target;<br>
  *    (b) safety to ensure that leverage targets are non-negative;<br>
  *    (c) a constructor accepting the initial leverage target.<br><br>
  * 
  * This implementation does not specify how leverage targets are subsequently
  * computed.
  * 
  * @author phillips
  */
public abstract class AbstractLeverageTargetAlgorithm implements LeverageTargetAlgorithm {
   private double
      targetLeverageNow;
   
   /**
     * Create a {@link AbstractLeverageTargetAlgorithm} object.
     * 
     * @param initialLeverageTarget
     *        The initial leverage target. This argument should be
     *        non-negative.
     */
   protected AbstractLeverageTargetAlgorithm(
      final double initialLeverageTarget
      ) {
      Preconditions.checkArgument(initialLeverageTarget >= 0.);
      targetLeverageNow = initialLeverageTarget;
   }
   
   /**
     * Compute a new leverage target. The argument is not modified
     * by this operation.
     */
   @Override
   public abstract void computeTargetLeverage(final Portfolio portfolio);
   
   /** 
     * Get the most recent leverage target.
     */
   @Override
   public final double getTargetLeverage() {
      return targetLeverageNow;
   }
   
   /** 
     * Set the value of leverage target. The argument should be non-negative.
     * If the argument is negative, it is silently trimmed to zero.
     */
   @Override
   public final void setTargetLeverage(final double value) {
      targetLeverageNow = Math.max(value, 0.);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Target Leverage Algorithm, current target: " + getTargetLeverage() + ".";
   }
}
