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

import com.google.common.base.Preconditions;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.plugins.CustomFirmDecisionRule;
import eu.crisis_economics.abm.firm.plugins.FirmDecisionRule;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration
   .AbstractDoubleModelParameterConfiguration;

@ConfigurationComponent(
   DisplayName = "Custom",
   Description =
      "A customizable firm decision rule. This module creates a firm decision rule which "
    + "delegates its decision outcomes to a model parameter. The value of the model parameter, "
    + "whether or not in changes in time, can be customized."
   )
public final class CustomFirmDecisionRuleConfiguration
   extends AbstractPrivateConfiguration
   implements
      FirmLabourWageAskPriceDecisionRuleConfiguration,
      FirmGoodsPricingDecisionRuleConfiguration,
      FirmLiquidityTargetDecisionRuleConfiguration,
      FirmTargetProductionDecisionRuleConfiguration {
   
   private static final long serialVersionUID = 5165280353761527411L;
   
   @Submodel
   @Layout(
      FieldName = "Decision Values",
      VerboseDescription = "Decision rule value generator",
      Order = 0.0
      )
   @Parameter(
      ID = "CUSTOM_FIRM_DECISION_RULE_VALUE"
      )
   private AbstractDoubleModelParameterConfiguration
      decisionRuleGenerator;
   
   public AbstractDoubleModelParameterConfiguration getDecisionRuleGenerator() {
      return decisionRuleGenerator;
   }
   
   public void setDecisionRuleGenerator(final AbstractDoubleModelParameterConfiguration value) {
      this.decisionRuleGenerator = value;
   }
   
   /**
     * Create a {@link CustomFirmDecisionRuleConfiguration} object with default 
     * parameters.
     */
   public CustomFirmDecisionRuleConfiguration() {
      this(new ConstantDoubleModelParameterConfiguration(0.));
   }
   
   /**
     * Create a {@link CustomFirmDecisionRuleConfiguration} object with custom 
     * parameters.
     * 
     * @param decisionGenerator
     *        A configurator for a {@link Parameter}{@code <Double>}. An instance of
     *        a {@link Parameter} created by this configurator will be the delegate on
     *        which {@link Firm} decision rules depend.
     */
   public CustomFirmDecisionRuleConfiguration(
      final AbstractDoubleModelParameterConfiguration decisionGenerator) {
      this.decisionRuleGenerator = Preconditions.checkNotNull(decisionGenerator);
   }
   
   @Override
   protected void addBindings() {
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(CustomFirmDecisionRule.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
