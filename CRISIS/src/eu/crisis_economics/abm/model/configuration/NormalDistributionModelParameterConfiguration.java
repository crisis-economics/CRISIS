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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Gaussian"
   )
public final class NormalDistributionModelParameterConfiguration
   extends AbstractDistributionModelParameterConfiguration
   implements SampledDoubleModelParameterConfiguration,
      TimeseriesDoubleModelParameterConfiguration {
   
   private static final long serialVersionUID = -6925128128000962331L;
   
   private static final double
      DEFAULT_MEAN = 0.,
      DEFAULT_VARIANCE = 1.;
   
   @Layout(
      FieldName = "Mean",
      Order = 1.0
      )
   @Parameter(
      ID = "NORMAL_DISTRIBUTION_MEAN"
      )
   private double
      mean;
   @Layout(
      FieldName = "Variance",
      Order = 1.1
      )
   @Parameter(
      ID = "NORMAL_DISTRIBUTION_VARIANCE"
      )
   private double
      variance;
   
   public double getMean() {
      return mean;
   }
   
   public void setMean(
      final double mean) {
      this.mean = mean;
   }
   
   public double getVariance() {
      return variance;
   }
   
   /**
     * Set the variance of distributions to be created by this component. The argument
     * should be non-negative.
     */
   public void setVariance(final double variance) {
      this.variance = variance;
   }
   
   /**
     * Create a {@link NormalDistributionModelParameterConfiguration} object with default
     * parameters.
     */
   public NormalDistributionModelParameterConfiguration() {
      this(DEFAULT_MEAN, DEFAULT_VARIANCE);
   }
   
   /**
     * Create a {@link NormalDistributionModelParameterConfiguration} object with custom
     * parameters.
     * 
     * @param mean
     *        The mean of distributions to be created by this component.
     * @param sigmaSquared
     *        The Gaussian variance of distributions to be created by this component.
     *        This argument must be non-negative.
     */
   public NormalDistributionModelParameterConfiguration(
      final double mean,
      final double sigmaSquared
      ) {
      Preconditions.checkArgument(sigmaSquared >= 0.);
      this.mean = mean;
      this.variance = sigmaSquared;
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(variance >= 0.,
         toParameterValidityErrMsg("Variance is negative."));
   }
   
   /**
     * A lightweight injection wrapper for the {@link NormalDistribution} class.
     * 
     * @author phillips
     */
   private static final class NormalDistributionImpl extends NormalDistribution {
      @Inject
      NormalDistributionImpl(
      @Named("NORMAL_DISTRIBUTION_MEAN")
         final double mean,
      @Named("NORMAL_DISTRIBUTION_STD")
         final double std
         ) {
         super(mean, std);
      }
      private static final long serialVersionUID = -2721697893670278736L;
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NORMAL_DISTRIBUTION_MEAN",    mean,
         "NORMAL_DISTRIBUTION_STD",     Math.sqrt(variance)
         ));
      bind(RealDistribution.class)
         .to(NormalDistributionImpl.class);
   }
}
