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

import eu.crisis_economics.abm.household.plugins.ConsumptionPropensityBudgetAlgorithm;
import eu.crisis_economics.abm.household.plugins.HouseholdDecisionRule;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Consumption Propensity"
   )
public final class ConsumptionPropensityBudgetDecisionRuleConfiguration
   extends AbstractConsumptionBudgetDecisionRuleConfiguration {
   
   private static final long serialVersionUID = 5473427917787956994L;
   
   public final static double
      DEFAULT_CONSUMPTION_PROPENSITY_MULTIPLIER = 0.8,
      DEFAULT_CONSUMPTION_PROPENSITY_WEALTH_EXPONENT = 1.;
   
   @Layout(
      Order = 0,
      FieldName = "Consumption Propensity Multiplier"
      )
   @Parameter(
      ID = "CONSUMPTION_PROPENSITY_MULTIPLIER"
      )
   private double
      consumptionPropensityMultiplier =
         DEFAULT_CONSUMPTION_PROPENSITY_MULTIPLIER;
   @Layout(
      Order = 1,
      FieldName = "Wealth Exponent"
      )
   @Parameter(
      ID = "CONSUMPTION_PROPENSITY_WEALTH_EXPONENT"
      )
   private double
      consumptionBudgetWealthExponent =
         DEFAULT_CONSUMPTION_PROPENSITY_WEALTH_EXPONENT;
   
   public final double getHouseholdConsumptionPropensity() {
      return consumptionPropensityMultiplier;
   }
   
   public final void setHouseholdConsumptionPropensity(
      final double value) {
      consumptionPropensityMultiplier = value;
   }
   
   public final String desHouseholdConsumptionPropensity() {
      return
         "This parameter specifies the degree to which households are willing to spend "
       + "their deposits on consumption. If a household has cash C immediately before "
       + "submitting goods market orders, then the total consumption budget B (to be divided "
       + "between all types of goods), is B = min(C, P * C**A), were P is this parameter, "
       + "and A is an exponent term, which can also be set via this model.";
   }
   
   public final double getHouseholdConsumptionBudgetWealthExponent() {
      return consumptionBudgetWealthExponent;
   }
   
   public final void setHouseholdConsumptionBudgetWealthExponent(
      final double value) {
      consumptionBudgetWealthExponent = value;
   }
   
   public final String desHouseholdConsumptionBudgetWealthExponent() {
      return
         "This parameter specifies the degree to which households are willing to spend "
       + "their deposits on consumption. If a household has cash C immediately before "
       + "submitting goods market orders, then the total consumption budget B (to be divided "
       + "between all types of goods), is B = min(C, P * C**A), were A is this parameter, "
       + "and P is an multiplier term called the consumption propensity, which can also be "
       + "set via this model.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(consumptionPropensityMultiplier >= 0.,
         toParameterValidityErrMsg("Consumption Propensity Multiplier is negative."));
      Preconditions.checkArgument(consumptionBudgetWealthExponent >= 0.,
         toParameterValidityErrMsg("Consumption Propensity Wealth Exponent is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "CONSUMPTION_PROPENSITY_MULTIPLIER",      consumptionPropensityMultiplier,
         "CONSUMPTION_PROPENSITY_WEALTH_EXPONENT", consumptionBudgetWealthExponent
         ));
      bind(
         HouseholdDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(ConsumptionPropensityBudgetAlgorithm.class);
      expose(
         HouseholdDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
