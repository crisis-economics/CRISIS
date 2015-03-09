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
  * A smoothing function which penalizes both sharp increases and sharp decreases.
  * <br><br>
  * 
  * If the ratio of target {@code T} and current {@code C} values is near {@code 1.0},
  * this smoothing function returns a value close to {@code T}. If the ratio of target
  * {@code T} and current {@code C} values is large or small, this method returns
  * <code>w * T + (1 - w) * C</code> where {@code w} is a customizable term called the
  * <i>minimum weight</i>.
  */
public final class GaussianWeightSmoothingAlgorithm implements SmoothingAlgorithm {
   
   public final static double
      DEFAULT_MINIMUM_WEIGHT = .1;
   
   private double
      minimumWeight;
   
   /**
     * Create a {@link GaussianWeightSmoothingAlgorithm} object with custom parameters.
     * 
     * @param minimumWeight
     *        The term {@code w} appearing in {@link GaussianWeightSmoothingAlgorithm}.
     */
   @Inject
   public GaussianWeightSmoothingAlgorithm(
   @Named("GAUSSIAN_WEIGHT_SMOOTHING_MINIMUM_WEIGHT")
      final double minimumWeight
      ) {
      this.minimumWeight = minimumWeight;
   }
   
   public GaussianWeightSmoothingAlgorithm() {
      this(DEFAULT_MINIMUM_WEIGHT);
   }
   
   @Override
   public double applySmoothing(
      double target,
      double current) {
      final double
         ratio = Math.max(target, 1.e-10) / Math.max(current,  1.e-10),
         weight = (1. - minimumWeight) * 
            Math.exp((-(1./ratio + ratio) / 2. + 1.) * 50.) + minimumWeight;
      return
         weight * target + (1. - weight) * current;
   }
}
