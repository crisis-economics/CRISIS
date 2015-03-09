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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;

/**
  * An implementation of the exponential smoothing map for {@link DiscreteTimeSeries}
  * objects.<br><br>
  * 
  * This map accepts as input a {@link DiscreteTimeSeries} (T_i, X_i) and constructs 
  * a new <br><br> {@link DiscreteTimeSeries} (T_i, S_i) as follows:<br><br>
  * 
  *   (a) S_0 = X_0;<br>
  *   (b) S_j = X_j * A + (1 - A) * S_(j-1).<br><br>
  * 
  * where A is a customizable parameter to the algorithm. If the interval DT between 
  * T_i and T_(i+1) is not 1.0, then the following expression is used for step (b):<br><br>
  * 
  *   (b) S_j = X_j * (1. - (1 - A)**DT) + (1 - A)**DT * S_(j-1).
  * 
  * @author phillips
  */
public class DiscreteTimeSeriesExponentialSmoother implements DiscreteTimeSeriesMap {
   final double
      alpha,
      __1mAlpha;
   
   /**
     * Create an algorithm which converts a {@link DiscreteTimeSeries} into another
     * exponentially smoothed {@link DiscreteTimeSeries}. The parameter alpha is the
     * standard smoothing factor, and should be in the range [0, 1].
     * @param alpha
     *        The exponential smoothing factor. This parameter should be in the 
     *        range [0, 1].
     */
   public DiscreteTimeSeriesExponentialSmoother(final double alpha) {
      Preconditions.checkArgument(alpha >= 0. && alpha <= 1.0);
      this.alpha = alpha;
      this.__1mAlpha = (1. - alpha);
   }
   
   @Override
   public DiscreteTimeSeries applyTo(final DiscreteTimeSeries timeSeries) {
      final DiscreteTimeSeries
         result = new DiscreteTimeSeries();
      double
         T = timeSeries.firstKey(),
         deltaT = 0.,
         V = timeSeries.get(T),
         S = V;
      result.put(T, V);
      final Set<Entry<Double, Double>>
         entrySet = timeSeries.entrySet();
      Entry<Double, Double> q;
      final Iterator<Entry<Double, Double>>
         p = entrySet.iterator();
      p.next();
      while(p.hasNext()) {
         q = p.next();
         deltaT = q.getKey() - T;
         T = q.getKey();
         double pow =
            Math.pow(__1mAlpha, deltaT);
         V = q.getValue();
         V = V * (1. - pow) + pow * S;
         result.put(T, V);
         S = V;
      }
      return result;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Discrete Time Series Exponential Smoothing Algorithm, smoothing factor: " 
             + alpha + ".";
   }
}
