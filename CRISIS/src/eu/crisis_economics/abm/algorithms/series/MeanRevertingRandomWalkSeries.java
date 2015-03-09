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
  * An simple stochastic implementation of the {@link RandomSeries} interface.<br><br>
  * 
  * The value, {@code V}, of the parameter evolves as follows:<br>
  * 
  * <center><code>
  *   V(next) = V(last) + a * (V* - V(last)) + s * epsilon_t
  * </code></center><br>
  *   
  * where:<br><br>
  * 
  * <ul>
  *    <li> {@code a} is the mean reversion rate;
  *    <li> {@code V*} is the mean reversion level (the long-run mean sample value);
  *    <li> {@code s} (sigma) is the standard deviation (the volatility) of noise, and
  *    <li> {@code epsilon_t} is a random shock.
  *    <li> <code>V(next)</code> is the next value of the parameter.
  * </ul>
  * 
  * The initial value of this parameter is <code>V*</code>.<br><br>
  * 
  * @author phillips
  */
public final class MeanRevertingRandomWalkSeries implements RandomSeries {

   private double
      alpha,
      mean,
      sigma,
      lowerBound,
      upperBound;
   
   private MersenneTwisterFast
      dice;
   
   private double
      value;
   
   /**
     * Create a {@link MeanRevertingRandomWalkSeries} with custom parameters.<br><br>
     * 
     * See also {@link MeanRevertingRandomWalkSeries} and {@link RandomSeries}.
     * 
     * @param alpha (<code>a</code>) <br>
     *        The mean reversion rate.
     * @param mean (<code>V*</code>) <br>
     *        The mean reversion level (the long-run mean sample value)
     * @param sigma (<code>s</code>) <br>
     *        {@code sigma} is the standard deviation (the volatility) of noise
     * @param lowerBound <br>
     *        The least value possible for the random series.
     * @param upperBound <br>
     *        The greatest value possible for the random series. The value must
     *        not be less than {@code lowerBound}.
     * @param initialValue <br>
     *        The initial value of the {@link RandomSeries}.
     * @param seed <br>
     *        A seed {@link Long} to use for the underlying random number generator.
     */
   @Inject
   public MeanRevertingRandomWalkSeries(
   @Named("MEAN_REVERTING_RANDOM_WALK_ALPHA")
      final double alpha,
   @Named("MEAN_REVERTING_RANDOM_WALK_MEAN")
      final double mean,
   @Named("MEAN_REVERTING_RANDOM_WALK_SIGMA")
      final double sigma,
   @Named("MEAN_REVERTING_RANDOM_WALK_LOWER_BOUND")
      final double lowerBound,
   @Named("MEAN_REVERTING_RANDOM_WALK_UPPER_BOUND")
      final double upperBound,
   @Named("MEAN_REVERTING_RANDOM_WALK_INITIAL_VALUE")
      final double intialValue,
   @Named("MEAN_REVERTING_RANDOM_WALK_SEED")
      final long seed
      ) {
      if(upperBound < lowerBound)
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": lower bound is greater than upper bound.");
      
      this.alpha = alpha;
      this.mean = mean;
      this.sigma = sigma;
      this.value = intialValue;
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      
      this.dice = new MersenneTwisterFast(seed);
   }
   
   public void setAlpha(final double alpha) {
      this.alpha = alpha;
   }
   
   public void setMean(final double mean) {
      this.mean = mean;
   }
   
   public void setSigma(final double sigma) {
      this.sigma = sigma;
   }
   
   public void setValue(final double value) {
      this.value = value;
   }
   
   public double getLowerBound() {
      return lowerBound;
   }
   
   public double getUpperBound() {
      return upperBound;
   }
   
   @Override
   public double next() {
      double
         result = value;
      value += alpha * (mean - value) + sigma * dice.nextGaussian();
      return NumberUtil.clamp(lowerBound, result, upperBound);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Mean Reverting Random Walk, alpha: " + alpha
            + ", mean: " + mean + ", sigma: " + sigma + " lower bound: " + lowerBound + ", "
            + "upper bound: " + upperBound + ".";
   }
}
