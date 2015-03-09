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

import eu.crisis_economics.abm.household.plugins.FixedWageExpectationAlgorithm;
import eu.crisis_economics.abm.household.plugins.HouseholdDecisionRule;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Fixed Wage Ask Price"
   )
public final class FixedWageAskPriceDecisionRuleConfiguration
   extends AbstractPrivateConfiguration
   implements HouseholdLabourWageDecisionRuleConfiguration {
   
   private static final long serialVersionUID = -1495420314084219838L;
   
   public final static double
      DEFAULT_LABOUR_ASK_PRICE_PER_UNIT = 6.75e7;
   
   @Parameter(
      ID = "FIXED_WAGE_ASK_PRICE_ALGORITHM_LABOUR_WAGE_PER_UNIT"
      )
   private double
      labourWageAskPricePerUnit = DEFAULT_LABOUR_ASK_PRICE_PER_UNIT;
   
   public double getLabourAskPricePerUnit() {
      return labourWageAskPricePerUnit;
   }
   
   public void setLabourAskPricePerUnit(final double value) {
      labourWageAskPricePerUnit = value;
   }
   
   public String desLabourAskPricePerUnit() {
      return 
         "The fixed ask price per unit household labour (the desired salary per unit "
       + "household labour throughout the lifetime of the simulation).";
   }
   
   /**
     * Create a {@link FixedWageAskPriceDecisionRuleConfiguration} object with default
     * parameters.
     */
   public FixedWageAskPriceDecisionRuleConfiguration() { }
   
   /**
     * Create a {@link FixedWageAskPriceDecisionRuleConfiguration} object with custom
     * parameters.
     * 
     * @param labourWageAskPrice <br>
     *        The ask price per unit labour for ask price rules to be created by this
     *        configuration component. This argument must be non-negative.
     */
   public FixedWageAskPriceDecisionRuleConfiguration(
      final double labourWageAskPrice) {
      Preconditions.checkArgument(labourWageAskPrice >= 0.);
      this.labourWageAskPricePerUnit = labourWageAskPrice;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(labourWageAskPricePerUnit >= 0,
         toParameterValidityErrMsg("Initial Labour Ask Price Per Unit is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FIXED_WAGE_ASK_PRICE_ALGORITHM_LABOUR_WAGE_PER_UNIT",      labourWageAskPricePerUnit
         ));
      bind(
         HouseholdDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(FixedWageExpectationAlgorithm.class);
      expose(
         HouseholdDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
