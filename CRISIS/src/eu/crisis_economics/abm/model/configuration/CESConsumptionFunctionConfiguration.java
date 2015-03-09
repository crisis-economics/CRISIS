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

import com.google.inject.name.Names;

import eu.crisis_economics.abm.household.plugins.CESConsumptionAggregatorAlgorithm;
import eu.crisis_economics.abm.household.plugins.HouseholdConsumptionFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A {@link ComponentConfiguration} for the {@link CESConsumptionAggregatorAlgorithm}
  * type {@link HouseholdConsumptionFunction}. This class provides class bindings and
  * parameter customization for the {@link CESConsumptionFunctionConfiguration} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "CES Aggregator"
   )
public final class CESConsumptionFunctionConfiguration
   extends AbstractHouseholdConsumptionFunctionConfiguration {
   
   private static final long serialVersionUID = -1272444091628577996L;
   
   public final static double
      DEFAULT_HOUSEHOLD_CES_EXPONENT = 0.8;
   
   @Parameter(
      ID = "HOUSEHOLD_CONSUMPTION_FUNCTION_CES_RHO"
      )
   private double
      householdCESExponent = DEFAULT_HOUSEHOLD_CES_EXPONENT;
   
   public final double getHouseholdCESExponent() {
      return householdCESExponent;
   }
   
   public final void setHouseholdCESExponent(final double value) {
      householdCESExponent = value;
   }
   
   public final String desHouseholdCESExponent() {
      return "The exponent to be used for the household consumption CES rule.";
   }
   
   @Override
   protected void addBindings() {
      install(
         InjectionFactoryUtils.asPrimitiveParameterModule(
            "HOUSEHOLD_CONSUMPTION_FUNCTION_CES_RHO", householdCESExponent
            ));
      bind(
         HouseholdConsumptionFunction.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(CESConsumptionAggregatorAlgorithm.class);
      expose(HouseholdConsumptionFunction.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
