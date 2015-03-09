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

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.junit.Assert;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A {@link ComponentConfiguration} for a {@link RealDistribution} implemented
  * by the uniform distribution (on a customizable interval).<br><br>
  * 
  * This {@link ComponentConfiguration} provides a {@link Parameter}{@code <Double>}
  * implemented by a uniform distribution. The profile of the distribution
  * is customizable.<br><br>
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Uniform Distribution",
   Description = 
      "A model parameter whose values are drawn at random uniformly from a customizable "
    + "contiguous interval."
   )
public final class UniformDistributionModelParameterConfiguration
   extends AbstractDistributionModelParameterConfiguration
   implements SampledDoubleModelParameterConfiguration,
      TimeseriesDoubleModelParameterConfiguration {
   
   private static final long serialVersionUID = -1281219507220139834L;
   
   private static final double
      DEFAULT_MINIMUM = 0.,
      DEFAULT_MAXIMUM = Double.MAX_VALUE;
   
   @Layout(
      FieldName = "Minimum",
      Order = 1.0
      )
   @Parameter(
      ID = "UNIFORM_REAL_DISTRIBUTION_MINIMUM"
      )
   private double
      minimum;
   @Layout(
      FieldName = "Maximum",
      Order = 1.1
      )
   @Parameter(
      ID = "UNIFORM_REAL_DISTRIBUTION_MAXIMUM"
      )
   private double
      maximum;
   
   public double getMinimum() {
      return minimum;
   }
   
   public void setMinimum(
      final double minimum) {
      this.minimum = minimum;
   }
   
   public double getMaximum() {
      return maximum;
   }
   
   public void setMaximum(
      final double maximum) {
      this.maximum = maximum;
   }
   
   /**
     * Create a {@link UniformDistributionModelParameterConfiguration} object with default
     * parameters.
     */
   public UniformDistributionModelParameterConfiguration() {
      this(DEFAULT_MINIMUM, DEFAULT_MAXIMUM);
   }
   
   /**
     * Create a {@link UniformDistributionModelParameterConfiguration} object with custom
     * parameters.<br><br>
     * 
     * See also {@link UniformDistributionModelParameterConfiguration}.
     * 
     * @param minimum
     *        The minimum in the range of the distribution.
     * @param maximum
     *        The maximum in the range of the distribution. This argument must not be 
     *        less than {@code minimum}.
     */
   public UniformDistributionModelParameterConfiguration(
      final double minimum,
      final double maximum
      ) {
      Preconditions.checkArgument(maximum >= minimum);
      this.minimum = minimum;
      this.maximum = maximum;
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(maximum >= minimum,
         toParameterValidityErrMsg("Domain maximum must not be less than domain minimum."));
   }
   
   /**
     * A lightweight injection wrapper for the {@link UniformRealDistribution} class.
     * 
     * @author phillips
     */
   private static final class UniformRealDistributionImpl extends UniformRealDistribution {
      
      @Inject
      UniformRealDistributionImpl(
      @Named("UNIFORM_REAL_DISTRIBUTION_MINIMUM")
         final double minimum,
      @Named("UNIFORM_REAL_DISTRIBUTION_MAXIMUM")
         final double maximum
         ) {
         super(minimum, maximum);
      }
      
      private static final long serialVersionUID = -2276076222570551094L;
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "UNIFORM_REAL_DISTRIBUTION_MINIMUM",    minimum,
         "UNIFORM_REAL_DISTRIBUTION_MAXIMUM",    maximum
         ));
      bind(RealDistribution.class)
         .to(UniformRealDistributionImpl.class);
   }
   
   /**
     * A lightweight test for this {@link ConfigurationComponent}. This snippet
     * creates a {@link Parameter}{@code <Double>} using an instance of
     * {@link UniformDistributionModelParameterConfiguration} and then tests
     * whether {@link Double} values drawn from this {@link Parameter} are 
     * strictly positive and have the expected mean.
     * 
     */
   public static void main(String[] args) {
      final UniformDistributionModelParameterConfiguration
         configuration = new UniformDistributionModelParameterConfiguration();
      configuration.setMinimum(.5);
      configuration.setMaximum(1.5);
      final ModelParameter<Double>
         distribution = configuration.createInjector().getInstance(
            Key.get(new TypeLiteral<ModelParameter<Double>>(){}));
      double
         mean = 0.;
      final int
         numSamples = 10000000;
      for(int i = 0; i< numSamples; ++i) {
         final double
            value = distribution.get();
         mean += value;
         Assert.assertTrue(value >= .5 && value <= 1.5);
      }
      mean /= numSamples;
      Assert.assertTrue(Math.abs(mean - 1.) < 1.e-3);
      System.out.println(mean);
   }
}
