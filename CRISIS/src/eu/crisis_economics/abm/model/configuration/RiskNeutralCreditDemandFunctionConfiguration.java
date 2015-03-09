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

import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction;
import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction.RiskAverseCreditDemandFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Risk Neutral Borrower"
   )
public final class RiskNeutralCreditDemandFunctionConfiguration
   extends AbstractPrivateConfiguration
   implements CreditDemandFunctionConfiguration {
   
   private static final long serialVersionUID = 2023812830459392334L;
   
   public static final double
      DEFAULT_INTEREST_RATE_INDIFFERENCE_THRESHOLD = 0.;
   
   @Parameter(
      ID = "INTEREST_RATE_INDIFFERENCE_THRESHOLD"
      )
   private double
      interestRateIndifferenceThreshold =
         DEFAULT_INTEREST_RATE_INDIFFERENCE_THRESHOLD;
   
   public final double getInterestRateIndifferenceThreshold() {
      return interestRateIndifferenceThreshold;
   }
   
   public final void setInterestRateIndifferenceThreshold(final double value) {
      this.interestRateIndifferenceThreshold = value;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         interestRateIndifferenceThreshold >= 0,
         toParameterValidityErrMsg("Interest Rate Indifference Threshold is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "INTEREST_RATE_INDIFFERENCE_THRESHOLD",   interestRateIndifferenceThreshold
         ));
      bind(
         CreditDemandFunction.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(RiskAverseCreditDemandFunction.class);
      expose(
         CreditDemandFunction.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
