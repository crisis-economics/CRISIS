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

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.inject.name.Names;

import eu.crisis_economics.abm.household.plugins.AR1ConsumptionBudgetAlgorithm;
import eu.crisis_economics.abm.household.plugins.HouseholdDecisionRule;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A {@link ComponentConfiguration} for the {@link AR1ConsumptionBudgetAlgorithm}
  * {@link Household} consumption budget decision rule.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "AR1"
   )
public final class AR1ConsumptionBudgetDecisionRuleConfiguration
   extends AbstractConsumptionBudgetDecisionRuleConfiguration {
   
   private static final long serialVersionUID = -1669619802200024031L;
   
   private final static double
      DEFAULT_AR1_BUDGET_RULE_ADAPTATION_RATE = 0.1;
   
   @Layout(
      FieldName = "Memory / Adaptation Rate",
      Order = 1.0
      )
   @Parameter(
      ID = "AR1_CONSUMPTION_BUDGET_ALGORITHM_ADAPTATION_RATE"
      )
   private double
      ar1BudgetDecisionRuleAdaptationRate = DEFAULT_AR1_BUDGET_RULE_ADAPTATION_RATE;
   
   public double getAr1BudgetDecisionRuleAdaptationRate() {
      return ar1BudgetDecisionRuleAdaptationRate;
   }
   
   /**
     * Set the adaptation rate (memory) for {@link AR1ConsumptionBudgetAlgorithm} 
     * objects configured by this component. The argument can take any numerical 
     * value, but it is expected that the argument should be in the range {@code [0, 1]}.
     */
   public void setAr1BudgetDecisionRuleAdaptationRate(final double value) {
      this.ar1BudgetDecisionRuleAdaptationRate = value;
   }
   
   @Layout(
      FieldName = "Implementation",
      Order = 1.1
      )
   @Submodel
   @Parameter(
      ID = "AR1_CONSUMPTION_BUDGET_ALGORITHM_IMPLEMENTATION"
      )
   private AbstractConsumptionBudgetDecisionRuleConfiguration
      implementation = new ConsumptionPropensityBudgetDecisionRuleConfiguration();
   
   public AbstractConsumptionBudgetDecisionRuleConfiguration getImplementation() {
      return implementation;
   }
   
   public void setImplementation(
      final AbstractConsumptionBudgetDecisionRuleConfiguration value) {
      this.implementation = value;
   }
   
   /**
     * Create an {@link AR1ConsumptionBudgetDecisionRuleConfiguration} object with 
     * default parameters.
     */
   public AR1ConsumptionBudgetDecisionRuleConfiguration() { }
   
   /**
     * Create an {@link AR1ConsumptionBudgetDecisionRuleConfiguration} object with 
     * custom parameters.
     */
   public AR1ConsumptionBudgetDecisionRuleConfiguration(
      final double memory) {
      this.ar1BudgetDecisionRuleAdaptationRate = memory;
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "AR1_CONSUMPTION_BUDGET_ALGORITHM_ADAPTATION_RATE", 
         ar1BudgetDecisionRuleAdaptationRate
          ));
      bind(HouseholdDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(AR1ConsumptionBudgetAlgorithm.class);
      expose(HouseholdDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
