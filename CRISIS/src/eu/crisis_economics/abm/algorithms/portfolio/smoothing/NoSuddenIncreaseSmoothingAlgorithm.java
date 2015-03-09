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
package eu.crisis_economics.abm.algorithms.portfolio.smoothing;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * A simple implementation of the {@link SmoothingAlgorithm} interface. This 
  * {@link SmoothingAlgorithm}:
  * 
  * <ul>
  *   <li> allows sudden decreases in value,
  *   <li> exponentially smoothes all increases in value. For targets {@code T} > {@code C}
  *        where {@code C} is the current value to be smoothed, the respone {@code R} is
  *        <center><code>
  *           {@code R} = {@code C} * m + {@code T} * (1 - m)
  *        </center></code>
  * </ul>
  * 
  * @author phillips
  */
public final class NoSuddenIncreaseSmoothingAlgorithm implements SmoothingAlgorithm {
   
   private double
      smoothingWeight;
   
   /**
     * Create a {@link NoSuddenIncreaseSmoothingAlgorithm} object with custom
     * parameters.<br><br>
     * 
     * See also {@link NoSuddenIncreaseSmoothingAlgorithm}.
     * 
     * @param smoothingWeight
     *        {@code m} in the specificiation of this algorithm. See 
     *        {@link NoSuddenIncreaseSmoothingAlgorithm}. This argument
     *        should be non-negative and not greater than {@code 1.0}.
     */
   @Inject
   public NoSuddenIncreaseSmoothingAlgorithm(
      @Named("NO_SUDDEN_INCREASES_SMOOTHING_ALGORITHM_WEIGHT")
      final double smoothingWeight
      ) {
      this.smoothingWeight = smoothingWeight;
   }
   
   @Override
   public double applySmoothing(
      double target,
      double current
      ) {
      if(target < current)
         return target;
      else
         return current * smoothingWeight + target * (1. - smoothingWeight);
   }
   
   public double getSmoothingWeight() {
      return smoothingWeight;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "No Sudden Increase Smoothing Algorithm, smoothing weight: "
            + smoothingWeight + ".";
   }
}
