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

import eu.crisis_economics.abm.algorithms.portfolio.weighting.LinearAdaptivePortfolioWeighting;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.PortfolioWeighting;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   Description =
     "For the Linear Adaptive Portfolio Weighting algorithm, if the average "
   + "expected"
   + "return over all distinct instruments weighted by the portfolio is r', then the "
   + "weight w applied to each instrument is perturbed according to "
   + "\nw -> w + m * (r - r') \nwhere r is the expected return for the instrument,"
   + " and m is a fixed constant non-negative "
   + "adaptation rate parameter. Both the adaptation rate m "
   + "and the initial value of r for all instruments can be adjusted below.",
   DisplayName = "Linear Adaptive"
   )
public final class LinearAdaptivePortfolioWeightingConfiguration 
   extends AbstractPrivateConfiguration
   implements PortfolioWeightingAlgorithmConfiguration {
   
   private static final long serialVersionUID = -2288246622925863966L;
   
   @Parameter(
      ID = "LINEAR_ADAPTIVE_PORTFOLIO_INITIAL_RETURN_ESTIMATE"
      )
   private double
      initialInstrumentReturnEstimate =
         LinearAdaptivePortfolioWeighting
            .DEFAULT_LINEAR_ADAPTIVE_PORTFOLIO_INITIAL_RETURN_ESTIMATE;
   @Parameter(
      ID = "LINEAR_ADAPTIVE_PORTFOLIO_ADAPTATION_RATE"
      )
   private double
      adaptationRate =
         LinearAdaptivePortfolioWeighting
            .DEFAULT_LINEAR_ADAPTIVE_PORTFOLIO_ADAPTATION_RATE;
   
   public double getInitialInstrumentReturnEstimate() {
      return initialInstrumentReturnEstimate;
   }
   
   public void setInitialInstrumentReturnEstimate(final double value) {
      initialInstrumentReturnEstimate = value;
   }
   
   public String desInitialInstrumentReturnEstimate() {
      return "The initial expected return estimates (at time t = 0) for all investments in this "
      		+ "portfolio (must be non-negative).";
   }
   
   public double getAdaptationRate() {
      return adaptationRate;
   }
   
   public void setAdaptationRate(final double value) {
      adaptationRate = value;
   }
   
   public String desAdaptationRate() {
      return "The adaptation rate for adjustments made during the lifetime of this portfolio "
            + "(must be non-negative).  For the Linear Adaptive Portfolio Weighting algorithm, "
            + "if the average expected"
            + "return over all distinct instruments weighted by the portfolio is r', then the "
            + "weight w applied to each instrument is perturbed according to "
            + "\nw -> w + m * (r - r') \nwhere r is the expected return for the instrument,"
            + " and m is this parameter - a fixed constant non-negative adaptation rate "
            + "parameter.";
      
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         adaptationRate >= 0.,
         toParameterValidityErrMsg("Portfolio Adaptation Rate is negative."));
      Preconditions.checkArgument(
         initialInstrumentReturnEstimate >= 0.,
         toParameterValidityErrMsg("Initial Instrument Return Rate is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "LINEAR_ADAPTIVE_PORTFOLIO_INITIAL_RETURN_ESTIMATE",
         initialInstrumentReturnEstimate,
         "LINEAR_ADAPTIVE_PORTFOLIO_ADAPTATION_RATE",
         adaptationRate
         ));
      bind(
         PortfolioWeighting.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(LinearAdaptivePortfolioWeighting.class);
      expose(
         PortfolioWeighting.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
