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

/**
  * A simple interface for a smoothing algorithm. Implementations of this interface
  * provide a single method accepting, as input, a <i>target</i> and an <i>existing</i>
  * numerical value. The meaning of this numerical value is not specified. The method
  * returns a smoothed transition between the existing numerical value and the target.
  * 
  * @author phillips
  */
public interface SmoothingAlgorithm {
   /**
     * Apply a smoothing algorithm to the specified quantity.
     * 
     * @param target
     *        The target numerical value
     * @param current
     *        The existing numerical value
     * @return
     *        The smoothed numerical value between the existing value and the target
     */
   public double applySmoothing(
      final double target,
      final double current
      );
}
