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
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.weighting.NoisyPortfolioWeighting;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.PortfolioWeighting;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
     Description =
     "For the Noisy Portfolio Weighting algorithm, the natural logarithm of the "
   + "portfolio weights are evolved as an autoregressive AR(1) process with Gaussian white"
   + "noise such that \nlog_w -> log_w * rho + Z \nwhere log_w is the log-weight of a "
   + "particular instrument, rho is the lag-1 autocorrelation "
   + "parameter and Z is a random variable"
   + " sampled from a Normal Distribution with zero mean and variance equal to the "
   + "noise scale parameter, Z ~ N(0, noise scale).  The weights are subsequently "
   + "normalised.  Both rho and the noise scale are non-negative parameters that can be "
   + "adjusted below.",
   DisplayName = "Noisy"
   )
public final class NoisyPortfolioWeightingConfiguration 
   extends AbstractPrivateConfiguration
   implements PortfolioWeightingAlgorithmConfiguration
   {
   
   private static final long serialVersionUID = 6038050378018589710L;

   public final static double
      DEFAULT_NOISY_PORTFOLIO_WEIGHTING_RHO = .9,
      DEFAULT_NOISY_PORTFOLIO_WEIGHTING_NOISE_SCALE = 1.;
   
   @Layout(
      Order = 2,
      FieldName = "Rho"
      )
   @Parameter(
      ID = "NOISY_PORTFOLIO_WEIGHTING_RHO"
      )
   private double portfolioWeightingRho = DEFAULT_NOISY_PORTFOLIO_WEIGHTING_RHO;
   
   @Layout(
      Order = 1,
      FieldName = "Noise Scale"
      )
   @Parameter(
      ID = "NOISY_PORTFOLIO_WEIGHTING_NOISE_SCALE"
      )
   private double
      portfolioWeightingNoiseScale = DEFAULT_NOISY_PORTFOLIO_WEIGHTING_NOISE_SCALE;
   
   public double getPortfolioWeightingRho() {
      return portfolioWeightingRho;
   }
   
   public void setPortfolioWeightingRho(final double value) {
      portfolioWeightingRho = value;
   }
   
   public String desPortfolioWeightingRho() {
      return "Rho is a non-negative autoregressive process parameter. "
            + "The log-weight of each instrument in the portfolio is"
            + " modelled as an autoregressive AR(1) process with Gaussian white noise "
            + "such that \nlog_w -> log_w * rho + Z \nwhere log_w is the log-weight of a particular"
            + "instrument, Z  ~ N(0, noise scale) is a random "
            + "variable sampled from a "
            + "Normal Distribution with mean zero and variance scaled by the noise scale "
            + "parameter.  \nIn this regard, rho specifies the lag-1 autocorrelation of the "
            + "log-weight processes.  \nUnlike most AR(1) processes, rho must be non-negative "
            + "to prevent "
            + "oscillatory portfolio weights, and can be greater than 1 as the weights are "
            + "subsequently normalised.  A rho greater than 1 merely results in the noise term "
            + "having less impact on the portfolio weight evolution, which could equivalently be "
            + "captured by decreasing the noise scale parameter for Z.";
   }
   
   public double getPortfolioWeightingNoiseScale() {
      return portfolioWeightingNoiseScale;
   }
   
   public void setPortfolioWeightingNoiseScale(final double value) {
      portfolioWeightingNoiseScale = value;
   }
   
   public String desPortfolioWeightingNoiseScale() {
      return "The non-negative parameter specifying the variance of "
           + "the Normal Distribution that the random variable noise term Z ~ N(0, noise scale)"
           + " is sampled from.  It is "
           + "used in the evolution of the portfolio weight for each "
           + "instrument, which is modelled as an autoregressive AR(1) process with Gaussian white "
           + "noise \nlog_w -> log_w * rho + Z \nwhere log_w is the log-weight of a particular"
           + "instrument and rho is a parameter that specifies the"
           + " lag-1 autocorrelation of the processes.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(portfolioWeightingNoiseScale >= 0.,
         toParameterValidityErrMsg("Noise Scale is negative."));
      Preconditions.checkArgument(portfolioWeightingRho >= 0.,
         toParameterValidityErrMsg("Noise Exponent (Rho) is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NOISY_PORTFOLIO_WEIGHTING_RHO",          portfolioWeightingRho,
         "NOISY_PORTFOLIO_WEIGHTING_NOISE_SCALE",  portfolioWeightingNoiseScale
         ));
      bind(
         PortfolioWeighting.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(NoisyPortfolioWeighting.class);
      expose(
         PortfolioWeighting.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
