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
import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction.CustomRiskToleranceCreditDemandFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Custom Risk Tolerance Borrower",
   Description = 
        "A Credit Demand Function (CDF) with customizable risk.\n"
      + "\n" 
      + "For this CDF, borrowers will refuse to take out any loans at rates above their "
      + "production markup rate <code>mu</code>. For interest rates <code>r</code> > "
      + "</code>r_min</code> (where <code>r_min</code> is as follows), borrowers will submit "
      + "orders for their entire ideal liquidity shortfall in loans. For interest rates "
      + "<code>r</code> > <code>r_min</code> and less than <code>mu</code>, the loan profile "
      + "is a polynomial function with a root at <code>r_max</code>.\n"
      + "\n" 
      + "The exact profile of the credit demand, <code>C</code>, as a function of interest" 
      + "rate, <code>r</code>, is:\n"
      + "\n"
      + "<code><center>\n"
      +  "C(r) = L  Min(1.0, Max((1 - ((r - r_min)/(r_max-r_min))B, 0)),\n"
      + "</code></center>\n"
      + "\n"
      + "where <code>C(r)</code> is the credit demand, <code>L</code> is the ideal credit, "
      + "<code>r_min</code> is the lower bound below which the borrower will request "
      + "<code>C(R) = L</code>, and <code>B</code> is the customizable risk-bias exponent.\n"
      + "\n"
      + "The expression <code>r_min</code> is taken to be\n"
      + "\n"
      + "<code><center>\n"
      + "r_min = max(r*, f * mu)\n"
      + "</code></center>\n"
      + "\n"
      + "where <code>r*</code> is a customizable constant, <code>f</code> is a constant "
      + "faction in the range <code>[0, 1]</code> and <code>mu</code> is the production "
      + "markup rate."
   )
public final class CustomRiskToleranceCreditDemandFunctionConfiguration
   extends AbstractPrivateConfiguration
   implements CreditDemandFunctionConfiguration {
   
   private static final long serialVersionUID = -2593891453751717393L;
   
   public static final double
      DEFAULT_CREDIT_RISK_AFFINITY = 1.5,
      DEFAULT_INTEREST_RATE_INDIFFERENCE_THRESHOLD = 0.,
      DEFAULT_INTEREST_RATE_INDIFFERENCE_THRESHOLD_PROP_OF_MARKUP = 0.;
   
   @Layout(
      FieldName = "B",
      Order = 1.0
      )
   @Parameter(
      ID = "CREDIT_RISK_AFFINITY"
      )
   private double
      creditRiskAffinity = DEFAULT_CREDIT_RISK_AFFINITY;
   @Layout(
      FieldName = "r*",
      Order = 1.1
      )
   @Parameter(
      ID = "INTEREST_RATE_INDIFFERENCE_THRESHOLD"
      )
   private double
      interestRateIndifferenceThreshold =
         DEFAULT_INTEREST_RATE_INDIFFERENCE_THRESHOLD;
   @Layout(
      FieldName = "f",
      Order = 1.2
      )
   @Parameter(
      ID = "INDIFFERENCE_THRESHOLD_PROPORTION_OF_MARKUP"
      )
   private double
      indifferenceThresholdFractionOfMarkup =
         DEFAULT_INTEREST_RATE_INDIFFERENCE_THRESHOLD_PROP_OF_MARKUP;
   
   public final double getCreditRiskAffinity() {
      return creditRiskAffinity;
   }
   
   public final void setCreditRiskAffinity(final double value) {
      this.creditRiskAffinity = value;
   }
   
   public final double getInterestRateIndifferenceThreshold() {
      return interestRateIndifferenceThreshold;
   }
   
   public final void setInterestRateIndifferenceThreshold(final double value) {
      this.interestRateIndifferenceThreshold = value;
   }
   
   public double getIndifferenceThresholdFractionOfMarkup() {
      return indifferenceThresholdFractionOfMarkup;
   }
   
   public void setIndifferenceThresholdFractionOfMarkup(
      final double value) {
      this.indifferenceThresholdFractionOfMarkup = value;
   }
   
   public final String desCreditRiskAffinity() {
      return "A parameter describing firm tolerance to risk in their loan demand strategy. "
           + "Credit_Demand = Desired_Ideal_Credit * (1 - ((interestRate - rMin)/(rMax-rMin))^"
           + "CREDIT_RISK_AFFINITY), where Desired_Ideal_Demand is the amount of credit a firm "
           + "needs to fulfill its desired production quantity, rMax is equal to the mark-up "
           + "rate a firm can achieve when selling its product, and rMin is a fixed argument "
           + "corresponding to the miniumum interest rate at which a bank is willing to lend.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         creditRiskAffinity >= 0.,
         toParameterValidityErrMsg("Credit Risk Affinity parameter is negative."));
      Preconditions.checkArgument(
         interestRateIndifferenceThreshold >= 0.,
         toParameterValidityErrMsg("Interest Rate Indifference Threshold is negative."));
      Preconditions.checkArgument(
         indifferenceThresholdFractionOfMarkup >= 0. &&
         indifferenceThresholdFractionOfMarkup <= 1.0,
         toParameterValidityErrMsg("Interest Rate Indifference Threshold must be in the range [0, 1]."));
   }
   
   @Override
   protected void addBindings() { 
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "CREDIT_RISK_AFFINITY",                         creditRiskAffinity,
         "INTEREST_RATE_INDIFFERENCE_THRESHOLD",         interestRateIndifferenceThreshold,
         "INDIFFERENCE_THRESHOLD_PROPORTION_OF_MARKUP",  indifferenceThresholdFractionOfMarkup
         ));
      bind(
         CreditDemandFunction.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(CustomRiskToleranceCreditDemandFunction.class);
      expose(
         CreditDemandFunction.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
