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

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.junit.Assert;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.utilities.NumberUtil;

/**
  * A {@link ComponentConfiguration} for a {@link RealDistribution} implemented
  * by the Exponential distribution (see eg.
  * http://en.wikipedia.org/wiki/Exponential_distribution).<br><br>
  * 
  * This {@link ComponentConfiguration} provides a {@link Parameter}{@code <Double>}
  * implemented by an Exponential distribution. The profile of the distribution
  * is customizable.<br><br>
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Exponential Distribution",
   Description = 
      "The exponential distribution is the probability distribution that describes the time"
    + " between events in a Poisson process, i.e. a process in which events occur continuously"
    + " and independently at a constant average rate. It is the continuous analogue of the"
    + " geometric distribution, and it has the key property of being memoryless. In addition"
    + " to being used for the analysis of Poisson processes, it is found in various other"
    + " contexts [see http://en.wikipedia.org/wiki/Exponential_distribution]."
   )
public final class ExponentialDistributionModelParameterConfiguration
   extends AbstractDistributionModelParameterConfiguration
   implements SampledDoubleModelParameterConfiguration,
      TimeseriesDoubleModelParameterConfiguration {
   
   private static final long serialVersionUID = -8948850138000866074L;
   
   private static final double
      DEFAULT_LAMBDA = 1.,
      DEFAULT_MINIMUM = 0.,
      DEFAULT_MAXIMUM = Double.MAX_VALUE;
   
   @Layout(
      FieldName = "Lambda",
      Order = 1.0
      )
   private double
      lambda;
   @Layout(
      FieldName = "Minimum",
      Order = 1.1
      )
   private double
      minimum;
   @Layout(
      FieldName = "Maximum",
      Order = 1.2
      )
   private double
      maximum;
   
   public double getLambda() {
      return lambda;
   }
   
   public void setLambda(
      final double lambda) {
      this.lambda = lambda;
   }
   
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
     * Create a {@link ExponentialDistributionModelParameterConfiguration} object with default
     * parameters.
     */
   public ExponentialDistributionModelParameterConfiguration() {
      this(DEFAULT_LAMBDA, DEFAULT_MINIMUM, DEFAULT_MAXIMUM);
   }
   
   /**
     * Create a {@link ExponentialDistributionModelParameterConfiguration} object with custom
     * parameters.<br><br>
     * 
     * See also {@link ExponentialDistributionModelParameterConfiguration}.
     * 
     * @param lambda
     *        The lambda shape parameter for an {@link ExponentialDistribution}.
     *        This argument must be strictly positive. The value of {@code lambda}
     *        is {@code 1.0/mean} where {@code mean} is the expected value of samples
     *        drawn from the underlying {@link ExponentialDistribution}.
     * @param minimum
     *        The minimum in the range of the distribution.
     * @param maximum
     *        The maximum in the range of the distribution. This argument must not be 
     *        less than {@code minimum}.
     */
   public ExponentialDistributionModelParameterConfiguration(
      final double lambda,
      final double minimum,
      final double maximum
      ) {
      Preconditions.checkArgument(lambda > 0.);
      Preconditions.checkArgument(maximum >= minimum);
      this.lambda = lambda;
      this.minimum = minimum;
      this.maximum = maximum;
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(lambda > 0.,
         toParameterValidityErrMsg("Lambda must be strictly positive."));
      Preconditions.checkArgument(maximum >= minimum,
         toParameterValidityErrMsg("Domain maximum must not be less than domain minimum."));
   }
   
   /**
     * A lightweight injection wrapper for the {@link ExponentialDistribution} class.
     * 
     * @author phillips
     */
   private static final class ExponentialDistributionImpl extends ExponentialDistribution {
      private final double
         minimum,
         maximum;
      
      @Inject
      ExponentialDistributionImpl(
      @Named("DISTRIBUTION_LAMBDA")
         final double lambda,
      @Named("DISTRIBUTION_MINIMUM")
         final double minimum,
      @Named("DISTRIBUTION_MAXIMUM")
         final double maximum
         ) {
         super(1./lambda);
         this.minimum = minimum;
         this.maximum = maximum;
      }
      
      @Override
      public double sample() {
         return NumberUtil.clamp(minimum, super.sample(), maximum);
      }
      
      private static final long serialVersionUID = 2535360035715934138L;
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "DISTRIBUTION_LAMBDA",     lambda,
         "DISTRIBUTION_MINIMUM",    minimum,
         "DISTRIBUTION_MAXIMUM",    maximum
         ));
      bind(RealDistribution.class)
         .to(ExponentialDistributionImpl.class);
   }
   
   /**
     * A lightweight test for this {@link ConfigurationComponent}. This snippet
     * creates a {@link Parameter}{@code <Double>} using an instance of
     * {@link ExponentialDistributionModelParameterConfiguration} and then tests
     * whether {@link Double} values drawn from this {@link Parameter} are 
     * strictly positive and have the expected mean.
     * 
     */
   public static void main(String[] args) {
      final ExponentialDistributionModelParameterConfiguration
         configuration = new ExponentialDistributionModelParameterConfiguration();
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
         Assert.assertTrue(value >= 0.);
      }
      mean /= numSamples;
      Assert.assertTrue(Math.abs(mean - 1.) < 1.e-3);
      System.out.println(mean);
   }
}
