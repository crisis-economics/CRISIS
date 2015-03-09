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

import eu.crisis_economics.abm.algorithms.portfolio.weighting.LogitPortfolioWeighting;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.PortfolioWeighting;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   Description =
     "For the Logit Portfolio Weighting algorithm, if the maximum return over all "
   + "distinct instruments weighted by the portfolio is r_max, then the (un-normalised)"
   + " weight applied to each instrument is \nw -> exp(beta * (r - r_max)) \nwhere r"
   + " is the expected return for the instrument and beta is the intensity of choice, a "
   + "non-negative parameter that can be adjusted below.",
   DisplayName = "Logit"
   )
public final class LogitPortfolioWeightingConfiguration 
   extends AbstractPrivateConfiguration
   implements PortfolioWeightingAlgorithmConfiguration {
   
   private static final long serialVersionUID = 4092440557810558318L;
   
   public static final double
      DEFAULT_BANK_LOGIT_PORTFOLIO_BETA = 20.;
   
   @Layout(
      Order = 1,
      FieldName = "Intensity of Choice (beta)"
      )
   @Parameter(
      ID = "LOGIT_PORTFOLIO_WEIGHTING_BETA"
      )
   private double
      logitExponentBeta = DEFAULT_BANK_LOGIT_PORTFOLIO_BETA;
   
   public double getLogitExponentBeta() {
      return logitExponentBeta;
   }
   
   public void setLogitExponentBeta(final double value) {
      logitExponentBeta = value;
   }
   
   public String desLogitExponentBeta() {
      return "The intensity of choice parameter (beta) used in the portfolio weighting algorithm"
           + "\nw -> exp(beta * (r - r_max)) \nFor beta > 0, instruments with larger expected "
           + "returns will be allocated more weighting in the portfolio.  For beta = 0, all "
           + "instruments will receive the same portfolio weighting. Beta should be non-negative.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         logitExponentBeta >= 0.,
         toParameterValidityErrMsg("Beta is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "LOGIT_PORTFOLIO_WEIGHTING_BETA", logitExponentBeta));
      bind(
         PortfolioWeighting.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(LogitPortfolioWeighting.class);
      expose(
         PortfolioWeighting.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
