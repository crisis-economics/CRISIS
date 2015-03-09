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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ec.util.MersenneTwisterFast;
import eu.crisis_economics.utilities.NumberUtil;

/**
  * An implementation of the {@link RandomSeries} interface. This implementation
  * provides a lognormal mean reverting random walk.<br><br>
  * 
  * Successive samples <code>A(i), i &ge; 0</code> of this model parameter obey:
  * 
  * <center><code>
  * A(i+1) = A(i) ^ (1 + n(i, s)) 
  * </code></center><br>
  * 
  * where <code>N(i, s)</code> is sampled from a Gaussian distribution with customizable  
  * standard deviation <code>s</code>.<br><br>
  * 
  * @author phillips
  */
public class LognormalMeanRevertingRandomWalkSeries implements RandomSeries {

   private final double
      sigma,
      lowerBound,
      upperBound;
   
   private MersenneTwisterFast
      dice;
   
   private double
      value;
   
   /**
     * Create a {@link LognormalMeanRevertingRandomWalkSeries} with custom parameters.<br><br>
     * 
     * See also {@link LognormalMeanRevertingRandomWalkSeries} and {@link RandomSeries}.
     * 
     * @param initialValue
     *        The initial (seed) value of the parameter
     * @param sigma
     *        {@code sigma} is the standard deviation (the volatility) of noise
     * @param lowerBound
     *        The least value possible for the model parameter.
     * @param upperBound
     *        The greatest value possible for the model parameter. The value must
     *        not be less than {@code lowerBound}.
     * @param parameterName
     *        The {@link String} name of the parameter
     */
   @Inject
   public LognormalMeanRevertingRandomWalkSeries(
   @Named("LOGNORMAL_REVERTING_RANDOM_WALK_INITIAL_VALUE")
      final double initialValue,
   @Named("LOGNORMAL_REVERTING_RANDOM_WALK_SIGMA")
      final double sigma,
   @Named("LOGNORMAL_REVERTING_RANDOM_WALK_LOWER_BOUND")
      final double lowerBound,
   @Named("LOGNORMAL_REVERTING_RANDOM_WALK_UPPER_BOUND")
      final double upperBound,
   @Named("LOGNORMAL_REVERTING_RANDOM_WALK_SEED")
      final long seed
      ) {
      if(upperBound < lowerBound)
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": lower bound is greater than upper bound.");
      
      this.sigma = sigma;
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.value = initialValue;
      
      this.dice = new MersenneTwisterFast(seed);
   }
   
   public double getSigma() {
      return sigma;
   }
   
   public double getLowerBound() {
      return lowerBound;
   }
   
   public double getUpperBound() {
      return upperBound;
   }
   
   public double getValue() {
      return value;
   }
   
   @Override
   public double next() {
      double
         result = value;
      value = Math.pow(value, 1. + sigma * dice.nextGaussian());
      return NumberUtil.clamp(lowerBound, result, upperBound);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Lognormal Mean Reverting Random Walk Series, sigma:" + sigma
            + ", lowerBound:" + lowerBound + ", upperBound:" + upperBound
            + ", dice:" + dice + ", value:" + value + ".";
   }
}
