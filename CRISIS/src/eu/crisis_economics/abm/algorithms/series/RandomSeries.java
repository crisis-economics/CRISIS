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
package eu.crisis_economics.abm.algorithms.series;

/**
  * A lightweight interface for random number series. Random number series are sequences
  * of randomly generated numbers satisfying mathematical constraints. Examples of 
  * random number series include, but are not limited to:
  * 
  * <ul>
  *   <li> Mean reverting random walks (see {@link MeanRevertingRandomWalkSeries}).
  *   <li> Mean reverting lognormal random walks (see {@link LognormalMeanRevertingRandomWalkSeries}).
  *   <li> Orstein-Uhlenbeck series (see {@link OrsteinUhlenbeckSeries}).
  * </ul>
  * 
  * @author phillips
  */
public interface RandomSeries {
   
   /**
     * Compute the next value is the series.
     * 
     * @return The next value (a {@link Double}) in the series.
     */
   public double next();
   
}
