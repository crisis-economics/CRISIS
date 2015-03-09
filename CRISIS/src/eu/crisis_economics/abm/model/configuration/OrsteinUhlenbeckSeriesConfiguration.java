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

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.series.OrsteinUhlenbeckSeries;
import eu.crisis_economics.abm.algorithms.series.RandomSeries;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration.AbstractLongModelParameterConfiguration;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A {@link ConfigurationComponent} for a {@link Double} {@link Parameter} implemented by
  * an Orstein-Uhlenbeck timeseries process.<br><br>
  * 
  * See {@link OrsteinUhlenbeckTimeseriesParameter} for documentation regarding the 
  * mathematical description of this process.
  */
@ConfigurationComponent(
   DisplayName = "Orstein Uhlenbeck Process",
   Description =
      "A timeseries model parameter implemented by an Orstein-Uhlenbeck process.<br><br>" + 
      "Successive samples <code>A(i)</code> of this model parameter obey:\n" + 
      "\n" + 
      "<center><code>\n" + 
      "A(i+1) = rho * A(i) + N(i, s) + m\n" + 
      "</code></center><br>\n" + 
      "" + 
      "where <code>N(i, s)</code> is sampled from a Gaussian distribution with customizable " + 
      "standard deviation <code>s</code>, and <code>m</code> is the long term mean <code>m</code>."
    + "\n\n" +
      "The expected value E[A] of this process is m / (1 - r). The standard deviation s(A) of " +
      "this process is s / sqrt(2*(1-r))."
   )
public final class OrsteinUhlenbeckSeriesConfiguration
   extends AbstractRandomSeriesConfiguration {
   
   private static final long serialVersionUID = -5223436567594179192L;
   
   private static final double
      DEFAULT_INITIAL_VALUE = 1.2,
      DEFAULT_RHO = 0.9,
      DEFAULT_EXPECTED_VALUE = 1.2,
      DEFAULT_STANDARD_DEVIATION = 1.e-2,
      DEFAULT_LOWER_BOUND = 0.1,
      DEFAULT_UPPER_BOUND = 2.5;
   
   @Layout(
      Order = 0.0,
      FieldName = "Initial Value",
      VerboseDescription = "Initial Conditions"
      )
   @Parameter(
      ID = "ORSTEIN_UHLENBECK_SERIES_INITIAL_VALUE"
      )
   private double
      initialValue = DEFAULT_INITIAL_VALUE;
   @Layout(
      Order = 0.1,
      FieldName = "Rho",
      VerboseDescription = "Expected Values"
      )
   @Parameter(
      ID = "ORSTEIN_UHLENBECK_SERIES_RHO"
      )
   private double
      rho = DEFAULT_RHO;
   @Layout(
      Order = 0.2,
      FieldName = "E[A]"
      )
   @Parameter(
      ID = "ORSTEIN_UHLENBECK_SERIES_NOISE_MEAN"
      )
   private double
      expectedValue = DEFAULT_EXPECTED_VALUE;
   @Layout(
      Order = 0.3,
      FieldName = "s(A)"
      )
   @Parameter(
      ID = "ORSTEIN_UHLENBECK_SERIES_NOISE_SIGMA"
      )
   private double
      standardDeviation = DEFAULT_STANDARD_DEVIATION;
   @Layout(
      Order = 0.4,
      FieldName = "Lower Bound",
      VerboseDescription = "Bounds"
      )
   @Parameter(
      ID = "ORSTEIN_UHLENBECK_SERIES_LOWER_BOUND"
      )
   private double
      lowerBound = DEFAULT_LOWER_BOUND;
   @Layout(
      Order = 0.5,
      FieldName = "Upper Bound"
      )
   @Parameter(
      ID = "ORSTEIN_UHLENBECK_SERIES_UPPER_BOUND"
      )
   private double
      upperBound = DEFAULT_UPPER_BOUND;
   
   @Layout(
      Order = 1.0,
      Title = "Random Number Generator",
      FieldName = "Random Seed"
      )
   @Submodel
   @Parameter(
      ID = "ORSTEIN_UHLENBECK_SERIES_RANDOM_SEED"
      )
   private AbstractLongModelParameterConfiguration
      seed = new CountingLongModelParameterConfiguration(1L, 0L);
   
   public double getInitialValue() {
      return initialValue;
   }
   
   public void setInitialValue(
      final double initialValue) {
      this.initialValue = initialValue;
   }
   
   public double getRho() {
      return rho;
   }
   
   public void setRho(
      final double rho) {
      this.rho = rho;
   }
   
   public double getExpectedValue() {
      return expectedValue;
   }
   
   public void setExpectedValue(
      final double expectedValue) {
      this.expectedValue = expectedValue;
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
   
   public AbstractLongModelParameterConfiguration getSeed() {
      return seed;
   }
   
   public void setSeed(final AbstractLongModelParameterConfiguration value) {
      this.seed = value;
   }
   
   /**
     * Create a {@link OrsteinUhlenbeckSeriesConfiguration} object with default
     * parameters.
     */
   public OrsteinUhlenbeckSeriesConfiguration() { }
   
   /**
     * Create a {@link OrsteinUhlenbeckSeriesConfiguration} object with custom
     * parameters.<br><br>
     * 
     * By default this constructor will assign a different seed to each 
     * {@link OrsteinUhlenbeckSeries} object to be configured. To modify
     * this behaviour, specify a constant integer parameter seed via
     * {@link #setSeed(IntegerModelParameterConfiguration)}.
     */
   public OrsteinUhlenbeckSeriesConfiguration(
      final double initialValue,
      final double rho,
      final double expectedValue,
      final double standardDeviation,
      final double lowerBound,
      final double upperBound,
      final int seed
      ) {
      this.initialValue = initialValue;
      this.rho = rho;
      this.expectedValue = expectedValue;
      this.standardDeviation = standardDeviation;
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.seed = new CountingLongModelParameterConfiguration(1L, 0L);
   }
   
   @Override
   protected void assertParameterValidity() {
      super.assertParameterValidity();
      Preconditions.checkArgument(
         upperBound >= lowerBound, super.toParameterValidityErrMsg(
            "upper bound must be greater than or equal to lower bound.")
         );
      Preconditions.checkArgument(
         rho <= 1.0, super.toParameterValidityErrMsg(
            "Rho parameter must be less than 1.0.")
         );
   }
   
   @Override
   protected void addBindings() {
      bind(
         getScopeString().isEmpty() ? Key.get(RandomSeries.class) :
            Key.get(RandomSeries.class, Names.named(getScopeString()))).
         to(OrsteinUhlenbeckSeries.class);
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "ORSTEIN_UHLENBECK_SERIES_INITIAL_VALUE",             initialValue,
         "ORSTEIN_UHLENBECK_SERIES_RHO",                       rho,
         "ORSTEIN_UHLENBECK_SERIES_SIGMA",                     
            standardDeviation * Math.sqrt(2. * (1. - rho)),    
         "ORSTEIN_UHLENBECK_SERIES_MEAN",                      
            (1. - rho) * expectedValue,                        
         "ORSTEIN_UHLENBECK_SERIES_LOWER_BOUND",               lowerBound,
         "ORSTEIN_UHLENBECK_SERIES_UPPER_BOUND",               upperBound
         ));
      super.addBindings();
   }
}
