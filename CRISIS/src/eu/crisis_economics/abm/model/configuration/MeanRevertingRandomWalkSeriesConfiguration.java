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
package eu.crisis_economics.abm.model.configuration;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.series.MeanRevertingRandomWalkSeries;
import eu.crisis_economics.abm.algorithms.series.RandomSeries;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Mean Reverting Random"
   )
public final class MeanRevertingRandomWalkSeriesConfiguration
   extends AbstractRandomSeriesConfiguration {
   
   private static final long serialVersionUID = 6024108475727485168L;
   
   private static final double
      DEFAULT_ALPHA = 0.2,
      DEFAULT_SIGMA = 1.0,
      DEFAULT_INITIAL_VALUE = 1.0,
      DEFAULT_LOWER_BOUND = 0.2,
      DEFAULT_UPPER_BOUND = 1.8;
   
   private static final long
      DEFAULT_SEED = 1L;
   
   @Parameter(
      ID = "MEAN_REVERTING_RANDOM_WALK_ALPHA"
      )
   private double
      alpha = DEFAULT_ALPHA;
   @Parameter(
      ID = "MEAN_REVERTING_RANDOM_WALK_MEAN"
      )
   private double
      mean = 0.0;
   @Parameter(
      ID = "MEAN_REVERTING_RANDOM_WALK_SIGMA"
      )
   private double
      sigma = DEFAULT_SIGMA;
   @Parameter(
      ID = "MEAN_REVERTING_RANDOM_WALK_INITIAL_VALUE"
      )
   private double
      initialValue = DEFAULT_INITIAL_VALUE;
   @Parameter(
      ID = "MEAN_REVERTING_RANDOM_WALK_LOWER_BOUND"
      )
   private double
      lowerBound = DEFAULT_LOWER_BOUND;
   @Parameter(
      ID = "MEAN_REVERTING_RANDOM_WALK_UPPER_BOUND"
      )
   private double
      upperBound = DEFAULT_UPPER_BOUND;
   
   @Parameter(
      ID = "MEAN_REVERTING_RANDOM_WALK_SEED"
      )
   private long
      seed = DEFAULT_SEED;
   
   public double getAlpha() {
      return alpha;
   }
   
   public void setAlpha(final double alpha) {
      this.alpha = alpha;
   }
   
   public double getMean() {
      return mean;
   }
   
   public void setMean(final double mean) {
      this.mean = mean;
   }
   
   public double getSigma() {
      return sigma;
   }
   
   public void setSigma(final double sigma) {
      this.sigma = sigma;
   }
   
   public double getInitialValue() {
      return initialValue;
   }
   
   public void setInitialValue(final double initialValue) {
      this.initialValue = initialValue;
   }
   
   public double getLowerBound() {
      return lowerBound;
   }
   
   public void setLowerBound(final double lowerBound) {
      this.lowerBound = lowerBound;
   }
   
   public double getUpperBound() {
      return upperBound;
   }
   
   public void setUpperBound(final double upperBound) {
      this.upperBound = upperBound;
   }
   
   public long getSeed() {
      return seed;
   }
   
   public void setSeed(final long seed) {
      this.seed = seed;
   }
   
   public MeanRevertingRandomWalkSeriesConfiguration() { }
   
   public MeanRevertingRandomWalkSeriesConfiguration(final double mean) {
      this.mean = mean;
   }
   
   public MeanRevertingRandomWalkSeriesConfiguration(
      final double alpha,
      final double mean,
      final double sigma,
      final double initialValue,
      final double lowerBound,
      final double upperBound,
      final long seed
      ) {
      this.alpha = alpha;
      this.mean = mean;
      this.sigma = sigma;
      this.initialValue = initialValue;
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.seed = seed;
   }
   
   @Override
   protected void assertParameterValidity() {
      super.assertParameterValidity();
      Preconditions.checkArgument(
         upperBound >= lowerBound, super.toParameterValidityErrMsg(
            "upper bound must be greater than or equal to lower bound.")
         );
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(
         getScopeString().isEmpty() ? Key.get(RandomSeries.class) :
            Key.get(RandomSeries.class, Names.named(getScopeString()))).
         to(MeanRevertingRandomWalkSeries.class);
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "MEAN_REVERTING_RANDOM_WALK_ALPHA",             alpha,
         "MEAN_REVERTING_RANDOM_WALK_MEAN",              mean,
         "MEAN_REVERTING_RANDOM_WALK_SIGMA",             sigma,
         "MEAN_REVERTING_RANDOM_WALK_LOWER_BOUND",       lowerBound,
         "MEAN_REVERTING_RANDOM_WALK_UPPER_BOUND",       upperBound,
         "MEAN_REVERTING_RANDOM_WALK_INITIAL_VALUE",     initialValue,
         "MEAN_REVERTING_RANDOM_WALK_SEED",              seed
         ));
      super.addBindings();
   }
}
