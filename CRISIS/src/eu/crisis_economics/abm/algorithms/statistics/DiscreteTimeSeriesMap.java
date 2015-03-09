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
package eu.crisis_economics.abm.algorithms.statistics;

/**
  * An interface for functions that accept, as input, a {@link DiscreteTimeSeries} 
  * object, and return, as output, a {@link DiscreteTimeSeries} obtained by applying
  * an operation to the input.
  * 
  * @author phillips
  */
public interface DiscreteTimeSeriesMap {
   
   /**
     * Apply a map to the input {@link DiscreteTimeSeries}. The argument is not
     * modified by this method, and changes to the return value do not affect 
     * the argument.
     * @param input
     *        The {@link DiscreteTimeSeries} over which to apply the map.
     */
   public DiscreteTimeSeries applyTo(final DiscreteTimeSeries input);
}
