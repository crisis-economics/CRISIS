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

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
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
  * by the lognormal distribution.<br><br>
  * 
  * This {@link ComponentConfiguration} provides a {@link Parameter}{@code <Double>}
  * implemented by a lognormal distribution. The profile of the distribution
  * is customizable.<br><br>
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Lognormal Distribution",
   Description = 
      "A lognormal distribution is a continuous probability distribution of a random variable"
    + " whose logarithm is normally distributed. Thus, if the random variable X is log-normally"
    + " distributed, then Y = log(X) has a normal distribution. Likewise, if Y has a normal"
    + " distribution, then X = exp(Y) has a log-normal distribution. A random variable which "
    + "is log-normally distributed takes only positive real values"
    + " [see http://en.wikipedia.org/wiki/Log-normal_distribution]"
   )
public final class LogNormalDistributionModelParameterConfiguration
   extends AbstractDistributionModelParameterConfiguration
   implements SampledDoubleModelParameterConfiguration,
      TimeseriesDoubleModelParameterConfiguration {
   
   private static final long serialVersionUID = 2333324898887658221L;
   
   private static final double
      DEFAULT_MU = 1.0,
      DEFAULT_SIGMA = 0.1;
   
   @Layout(
      FieldName = "Mu",
      Order = 1.0
      )
   @Parameter(
      ID = "LOGNORMAL_DISTRIBUTION_MU"
      )
   private double
      mu;
   @Layout(
      FieldName = "Sigma",
      Order = 1.1
      )
   @Parameter(
      ID = "LOGNORMAL_DISTRIBUTION_SIGMA"
      )
   private double
      sigma;
   
   public double getMu() {
      return mu;
   }
   
   public void setMu(
      final double mu) {
      this.mu = mu;
   }
   
   public double getSigma() {
      return sigma;
   }
   
   public void setSigma(
      final double sigma) {
      this.sigma = sigma;
   }
   
   /**
     * Create a {@link LogNormalDistributionModelParameterConfiguration} object with default
     * parameters.
     */
   public LogNormalDistributionModelParameterConfiguration() {
      this(DEFAULT_MU, DEFAULT_SIGMA);
   }
   
   /**
     * Create a {@link LogNormalDistributionModelParameterConfiguration} object with custom
     * parameters.<br><br>
     * 
     * See also {@link LogNormalDistributionModelParameterConfiguration}.
     * 
     * @param mu
     *        The mean of the underlying normal distribution.
     * @param sigma
     *        The deviation of the underlying normal distribution. This argument must be
     *        non-negative.
     */
   public LogNormalDistributionModelParameterConfiguration(
      final double mu,
      final double sigma
      ) {
      this.mu = mu;
      this.sigma = sigma;
   }
   
   /**
     * Create a {@link LogNormalDistributionModelParameterConfiguration} object with
     * custom parameters. This static method differs from the class constructor with
     * the same arguments in that the arguments to this method are the {@code mean}
     * and the {@code standard deviation} of the actual lognormal distribution, not the 
     * {@code mean} and {@code standard deviation} of the log of the lognormal distribution
     * (namely, the underlying normal distribution).
     * 
     * @param actualMean
     *        The mean of the lognormal distribution. This argument must be 
     *        strictly positive.
     * @param actualSigma
     *        The standard deviation of the lognormal distribution. This argument 
     *        must be strictly positive.
     */
   public static LogNormalDistributionModelParameterConfiguration create(
      final double actualMean,
      final double actualSigma
      ) {
      final double
         actualMean2 = actualMean * actualMean,
         actualSigma2 = actualSigma * actualSigma,
         muLogDist = .5 * Math.log(actualMean2/(actualSigma2 / actualMean2 + 1.)),
         sigmaLogDist = Math.sqrt(Math.log(actualSigma2 / actualMean2 + 1.));
      return new LogNormalDistributionModelParameterConfiguration(muLogDist, sigmaLogDist);
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(sigma >= 0.,
         toParameterValidityErrMsg("Sigma is negative."));
   }
   
   /**
     * A lightweight injection wrapper for the {@link LogNormalDistribution} class.
     * 
     * @author phillips
     */
   private static final class LogNormalDistributionImpl extends LogNormalDistribution {
      @Inject
      LogNormalDistributionImpl(
      @Named("LOGNORMAL_DISTRIBUTION_MU")
         final double mu,
      @Named("LOGNORMAL_DISTRIBUTION_SIGMA")
         final double sigma
         ) {
         super(mu, sigma);
      }
      
      private static final long serialVersionUID = 4334368995463319167L;
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "LOGNORMAL_DISTRIBUTION_MU",      mu,
         "LOGNORMAL_DISTRIBUTION_SIGMA",   sigma
         ));
      bind(RealDistribution.class)
         .to(LogNormalDistributionImpl.class);
   }
   
   /**
     * A lightweight test for this {@link ConfigurationComponent}. This snippet
     * creates a {@link Parameter}{@code <Double>} using an instance of
     * {@link LogNormalDistributionModelParameterConfiguration} and then tests
     * whether the logarithm of {@link Double} values drawn from this {@link Parameter}
     * have the expected mean.
     */
   public static void main(String[] args) {
      {
      final LogNormalDistributionModelParameterConfiguration
         configuration = new LogNormalDistributionModelParameterConfiguration();
      configuration.setMu(5.0);
      configuration.setSigma(0.1);
      final ModelParameter<Double>
         distribution = configuration.createInjector().getInstance(
            Key.get(new TypeLiteral<ModelParameter<Double>>(){}));
      double
         mean = 0.;
      final int
         numSamples = 10000000;
      for(int i = 0; i< numSamples; ++i) {
         final double
            value = Math.log(distribution.get());
         mean += value;
      }
      mean /= numSamples;
      Assert.assertTrue(Math.abs(mean - 5.) < 1.e-3);
      }
      {
      final LogNormalDistributionModelParameterConfiguration
         configuration = LogNormalDistributionModelParameterConfiguration.create(5., .1);
      final ModelParameter<Double>
         distribution = configuration.createInjector().getInstance(
            Key.get(new TypeLiteral<ModelParameter<Double>>(){}));
      final Variance
         variance = new Variance();
      double
         mean = 0.;
      final int
         numSamples = 10000000;
      for(int i = 0; i< numSamples; ++i) {
         final double
            value = distribution.get();
         mean += value;
         variance.increment(value);
      }
      mean /= numSamples;
      final double
         observedSigma = Math.sqrt(variance.getResult());
      Assert.assertTrue(Math.abs(mean - 5.) < 1.e-3);
      Assert.assertTrue(Math.abs(observedSigma - .1) < 1.e-3);
      }
   }
}
