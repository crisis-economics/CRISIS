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

import eu.crisis_economics.abm.algorithms.series.LognormalMeanRevertingRandomWalkSeries;
import eu.crisis_economics.abm.algorithms.series.RandomSeries;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A {@link ConfigurationComponent} for a {@link Double} model parameter implemented by
  * a lognormal mean reverting random walk.<br><br>
  * 
  * Successive samples <code>A(i), i &ge; 0</code> of this model parameter obey:
  * 
  * <center><code>
  * A(i+1) = log(A(i) * (1 + n(i, s))) 
  * </code></center><br><br>
  * 
  * where <code>N(i, s)</code> is sampled from a Gaussian distribution with customizable" + 
  * standard deviation <code>s</code>."
  */
@ConfigurationComponent(
   DisplayName = 
      "Mean Reverting Lognormal",
   Description =
      "A module for a random series generator implemented by a lognormal mean"
    + " reverting random walk.<br><br>" + 
      "" + 
      "Successive samples <code>A(i), i &ge; 0</code> of this model parameter obey:\n" + 
      "\n" + 
      "<center><code>\n" + 
      "A(i+1) = A(i) ^ (1 + N(i, s)) \n" + 
      "</code></center><br><br>" + 
      "" + 
      "where <code>N(i, s)</code> is sampled from a Gaussian distribution with customizable\n" + 
      "standard deviation <code>s</code>. "
   )
public final class LognormalMeanRevertingRandomWalkSeriesConfiguration
   extends AbstractRandomSeriesConfiguration {
   
   private static final long serialVersionUID = -6605934450991242729L;
   
   private static final double
      DEFAULT_INITIAL_VALUE = 1.0,
      DEFAULT_SIGMA = 1.0,
      DEFAULT_LOWER_BOUND = 0.66,
      DEFAULT_UPPER_BOUND = 1.33;
   
   public static final long
      DEFAULT_SEED = 0L;
   
   @Layout(
      Order = 0.0,
      FieldName = "Initial Value"
      )
   @Parameter(
      ID = "LOGNORMAL_REVERTING_RANDOM_WALK_INITIAL_VALUE"
      )
   private double
      initialValue;
   @Layout(
      Order = 0.1,
      FieldName = "Standard Deviation"
      )
   @Parameter(
      ID = "LOGNORMAL_REVERTING_RANDOM_WALK_SIGMA"
      )
   private double
      standardDeviation;
   @Layout(
      Order = 0.2,
      FieldName = "Lower Bound"
      )
   @Parameter(
      ID = "LOGNORMAL_REVERTING_RANDOM_WALK_LOWER_BOUND"
      )
   private double
      lowerBound = DEFAULT_LOWER_BOUND;
   @Layout(
      Order = 0.3,
      FieldName = "Upper Bound"
      )
   @Parameter(
      ID = "LOGNORMAL_REVERTING_RANDOM_WALK_UPPER_BOUND"
      )
   private double
      upperBound = DEFAULT_UPPER_BOUND;
   @Layout(
      Order = 1.0,
      Title = "Random Number Generator",
      FieldName = "Random Seed"
      )
   @Parameter(
      ID = "LOGNORMAL_REVERTING_RANDOM_WALK_SEED"
      )
   private long
      seed = DEFAULT_SEED;
   
   public double getInitialValue() {
      return initialValue;
   }
   
   public void setInitialValue(
      final double initialValue) {
      this.initialValue = initialValue;
   }
   
   public double getStandardDeviation() {
      return standardDeviation;
   }
   
   public void setStandardDeviation(
      final double standardDeviation) {
      this.standardDeviation = standardDeviation;
   }
   
   public double getLowerBound() {
      return lowerBound;
   }
   
   public void setLowerBound(
      final double lowerBound) {
      this.lowerBound = lowerBound;
   }
   
   public double getUpperBound() {
      return upperBound;
   }
   
   public void setUpperBound(
      final double upperBound) {
      this.upperBound = upperBound;
   }
   
   public long getSeed() {
      return seed;
   }
   
   public void setSeed(final long seed) {
      this.seed = seed;
   }
   
   public LognormalMeanRevertingRandomWalkSeriesConfiguration() {
      this(DEFAULT_INITIAL_VALUE, DEFAULT_SIGMA);
   }
   
   public LognormalMeanRevertingRandomWalkSeriesConfiguration(final double sigma) {
      this(DEFAULT_INITIAL_VALUE, sigma);
   }
   
   public LognormalMeanRevertingRandomWalkSeriesConfiguration(
      final double initialValue,
      final double sigma
      ) {
      this.initialValue = initialValue;
      this.standardDeviation = sigma;
   }
   
   public LognormalMeanRevertingRandomWalkSeriesConfiguration(
      final double initialValue,
      final double sigma,
      final double lowerBound,
      final double upperBound,
      final long seed
      ) {
      this(initialValue, sigma);
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
         to(LognormalMeanRevertingRandomWalkSeries.class);
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "LOGNORMAL_REVERTING_RANDOM_WALK_INITIAL_VALUE",  initialValue,
         "LOGNORMAL_REVERTING_RANDOM_WALK_SIGMA",          standardDeviation,
         "LOGNORMAL_REVERTING_RANDOM_WALK_LOWER_BOUND",    lowerBound,
         "LOGNORMAL_REVERTING_RANDOM_WALK_UPPER_BOUND",    upperBound,
         "LOGNORMAL_REVERTING_RANDOM_WALK_SEED",           seed
         ));
      super.addBindings();
   }
}
