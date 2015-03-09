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

import com.google.inject.assistedinject.FactoryModuleBuilder;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.household.MacroHousehold;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.HouseholdFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Macroeconomic Households",
   Description = "Macroeconomic households with customizable consumption aggregation, "
               + "consumption budget allocation, mutual fund investment decisions and "
               + "wage ask price per unit labour. The cash and employable workforce "
               + "endowment of each household agent is configurable. Households are the "
               + "owners of funds, who are, in turn, the initial stock holders of banks."
   )
public final class MacroeconomicHouseholdSubEconomy extends AbstractHouseholdSubEconomy {
   private static final long serialVersionUID = -8962656490758735645L;
   /*
    * Submodels
    */
   @Layout(
      Order = 1,
      FieldName = "Wage Ask Price Decision Rule"
      )
   @Submodel
   @Parameter(
      ID = "HOUSEHOLD_LABOUR_WAGE_ASK_RULE"
      )
   private HouseholdLabourWageDecisionRuleConfiguration
      labourWageAskPriceAlgorithm = new FixedWageAskPriceDecisionRuleConfiguration();
   
   public final HouseholdLabourWageDecisionRuleConfiguration getLabourWageAskPriceAlgorithm() {
      return labourWageAskPriceAlgorithm;
   }
   
   public final void setLabourWageAskPriceAlgorithm(
      final HouseholdLabourWageDecisionRuleConfiguration value) {
      this.labourWageAskPriceAlgorithm = value;
   }
   
   @Layout(
      Order = 2,
      Title = "Consumption",
      VerboseDescription = "Durable & non-durable goods purchasing",
      FieldName = "Consumption Budget Allocation Rule"
      )
   @Submodel
   @Parameter(
      ID = "HOUSEHOLD_BUDGET_DECISION_RULE"
      )
   private AbstractConsumptionBudgetDecisionRuleConfiguration
      consumptionBudgetAllocationRule = new ConsumptionPropensityBudgetDecisionRuleConfiguration();
   
   public final AbstractConsumptionBudgetDecisionRuleConfiguration
      getConsumptionBudgetAllocationRule() {
      return consumptionBudgetAllocationRule;
   }
   
   public final void setConsumptionBudgetAllocationRule(
      final AbstractConsumptionBudgetDecisionRuleConfiguration value) {
      this.consumptionBudgetAllocationRule = value;
   }
   
   @Layout(
      Order = 3,
      FieldName = "Consumption Function"
      )
   @Submodel
   @Parameter(
      ID = "HOUSEHOLD_CONSUMPTION_FUNCTION"
      )
   private AbstractHouseholdConsumptionFunctionConfiguration
      consumptionFunctionAlgorithm = new CESConsumptionFunctionConfiguration();
   
   public AbstractHouseholdConsumptionFunctionConfiguration getConsumptionFunctionAlgorithm() {
      return consumptionFunctionAlgorithm;
   }
   
   public void setConsumptionFunctionAlgorithm(
      final AbstractHouseholdConsumptionFunctionConfiguration consumptionFunctionAlgorithm) {
      this.consumptionFunctionAlgorithm = consumptionFunctionAlgorithm;
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "HOUSEHOLD_DECISION_RULE_NOISE_LOWER_BOUND",    -0.4,
         "HOUSEHOLD_DECISION_RULE_NOISE_UPPER_BOUND",    +0.4,
         "HOUSEHOLD_DECISION_RULE_NOISE_AGGRESSIVENESS", +0.5
         ));
      install(new FactoryModuleBuilder()
         .implement(DepositingHousehold.class, MacroHousehold.class)
         .build(HouseholdFactory.class)
         );
      expose(HouseholdFactory.class);
      super.addBindings();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Household Subeconomy Component: " + super.toString();
   }
}
