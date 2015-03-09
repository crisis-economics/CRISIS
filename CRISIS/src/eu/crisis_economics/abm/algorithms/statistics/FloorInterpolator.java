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
package eu.crisis_economics.abm.algorithms.statistics;

import java.util.TreeMap;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

import com.google.common.base.Preconditions;

/**
  * A custom implementation of the {@link UnivariateInterpolator} interface.<br><br>
  * 
  * This implementation generates a {@link UnivariateFunction} {@code F} which
  * evaluates as follows: the value {@code F(t)} is taken to be {@code y[x[t]]}
  * where {@code x[t]} is the closest interpolation key less than, or equal to, 
  * {@code t}. {@code F} performs an interpolation by simply rounding down the
  * argument to the closest interpolation keypoint {@code x} not greater than 
  * {@code t}.
  * 
  * @author phillips
  */
public final class FloorInterpolator implements UnivariateInterpolator {
   
   public FloorInterpolator() { }   // Stateless
   
   @Override
   public UnivariateFunction interpolate(
      final double[] x,
      final double[] y
      ) throws MathIllegalArgumentException, DimensionMismatchException {
      Preconditions.checkNotNull(x, y);
      if(x.length != y.length)
         throw new DimensionMismatchException(x.length, y.length);
      final int
         seriesLength = x.length;
      if(seriesLength == 0)
         throw new IllegalArgumentException(
            "FloorInterpolator.interpolate: input data is empty. Interpolation cannot continue.");
      final TreeMap<Double, Double>
         tree = new TreeMap<Double, Double>();
      for(int i = 0; i< seriesLength; ++i)
         tree.put(x[i], y[i]);
      final UnivariateFunction result = 
         new UnivariateFunction() {
            @Override
            public double value(final double t) {
               if(t < x[0]) return y[0];
               else if(t > x[seriesLength - 1]) return y[seriesLength - 1];
               else return tree.floorEntry(t).getValue();
            }
         };
      return result;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Floor Interpolator";
   }
}
