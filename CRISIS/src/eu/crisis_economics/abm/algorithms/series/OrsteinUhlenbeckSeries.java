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
  * describes as Orstein-Uhlenbeck process.<br><br>
  * 
  * Successive samples <code>A(i)</code> of this model parameter obey:
  * 
  * <center><code>
  * A(i+1) = r * A(i) + N(i, s) + m
  * </code></center><br>
  * 
  * where <code>N(i, s)</code> is sampled from a Gaussian distribution with customizable  
  * standard deviation <code>s</code>, and <code>m</code> is the long term mean.<br><br>
  * 
  * @author phillips
  */
public final class OrsteinUhlenbeckSeries implements RandomSeries {
   
   private final double
      rho,
      sigma,
      mean,
      lowerBound,
      upperBound;
   
   private MersenneTwisterFast
      dice;
   
   private double
      value;
   
   /**
     * Create a {@link OrsteinUhlenbeckSeries} object with custom parameters.<br><br>
     * 
     * See also {@link OrsteinUhlenbeckSeries} and {@link RandomSeries}.
     * 
     * @param rho <code>(r)</code> <br>
     *        The decay rate for past samples
     * @param mean <code>(m)</code> <br>
     *        The mean value of Gaussian noise to apply.
     * @param sigma <code>(s)</code> <br>
     *        The standard deviation of Gaussian noise to apply.
     * @param lowerBound <br>
     *        The least value possible for the random series.
     * @param upperBound <br>
     *        The greatest value possible for the random series. The value must
     *        not be less than {@code lowerBound}.
     * @param initialValue <br>
     *        The initial value of the {@link RandomSeries}.
     * @param randomSeed <br>
     *        A {@link Long} seed to be used by the random number generator.
     */
   @Inject
   public OrsteinUhlenbeckSeries(
   @Named("ORSTEIN_UHLENBECK_SERIES_RHO")
      final double rho,
   @Named("ORSTEIN_UHLENBECK_SERIES_SIGMA")
      final double sigma,
   @Named("ORSTEIN_UHLENBECK_SERIES_MEAN")
      final double mean,
   @Named("ORSTEIN_UHLENBECK_SERIES_LOWER_BOUND")
      final double lowerBound,
   @Named("ORSTEIN_UHLENBECK_SERIES_UPPER_BOUND")
      final double upperBound,
   @Named("ORSTEIN_UHLENBECK_SERIES_INITIAL_VALUE")
      final double initialValue,
   @Named("ORSTEIN_UHLENBECK_SERIES_RANDOM_SEED")
      final long seed
      ) {
      if(upperBound < lowerBound)
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": lower bound is greater than upper bound.");
      
      this.rho = rho;
      this.sigma = sigma;
      this.mean = mean;
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.value = initialValue;
      
      this.dice = new MersenneTwisterFast(seed);
   }
   
   /**
     * Create an {@link OrsteinUhlenbeckSeries} {@link RandomSeries} from a set of 
     * desired long-term moments (mean and standard deviation).<br><br>
     * 
     * Arguments to this method have the same meanings as those listed in the default
     * class constructor {@link #OrsteinUhlenbeckSeries(double, double, double, double, 
     * double, double, long)}, with the exception of <code>desiredMean</code> and
     * <code>desiredStd</code>.
     * 
     * @param desiredMean <br>
     *        The desired long term mean for the {@link RandomSeries}.
     * @param desiredStd <br>
     *        The desired long term standard-deviation for the {@link RandomSeries}.
     *        This argument must be non-negative.
    */
   public static OrsteinUhlenbeckSeries fromMoments(
      final double desiredMean,
      final double desiredStd,
      final double rho,
      final double lowerBound,
      final double upperBound,
      final double initialValue,
      final long seed
      ) {
      final double
         m = (1. - rho) * desiredMean,
         s = desiredStd * Math.sqrt(2. * (1. - rho));
      return new OrsteinUhlenbeckSeries(rho, s, m, lowerBound, upperBound, initialValue, seed);
   }
   
   public double getRho() {
      return rho;
   }
   
   public double getSigma() {
      return sigma;
   }
   
   public double getMean() {
      return mean;
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
      value = rho * value + sigma * dice.nextGaussian() + mean;
      return NumberUtil.clamp(lowerBound, result, upperBound);
   }
   
   @Override
   public String toString() {
      return "Orstein Uhlenbeck Random Series, rho: " + rho + ", sigma: " + sigma
            + ", mean: " + mean + ", lowerBound: " + lowerBound + ", upperBound: "
            + upperBound + ", current value: " + value + ".";
   }
}
