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

import eu.crisis_economics.abm.firm.plugins.FirmDecisionRule;
import eu.crisis_economics.abm.firm.plugins.FixedValueFirmDecisionRule;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;

@ConfigurationComponent(
   DisplayName = "Fixed Value",
   Description =
      "A customizable firm decision rule. This module creates a firm decision rule which "
    + "returns a fixed customizable decision outcome permanently. The outcome of firm decision "
    + "rules does not depend on time or on the circumstances of the firm."
   )
public final class FixedValueFirmDecisionRuleConfiguration
   extends AbstractPrivateConfiguration
   implements
      FirmLabourWageAskPriceDecisionRuleConfiguration,
      FirmGoodsPricingDecisionRuleConfiguration,
      FirmLiquidityTargetDecisionRuleConfiguration,
      FirmTargetProductionDecisionRuleConfiguration {
   
   private static final long serialVersionUID = -4799928527498899193L;
   
   @Submodel
   @Layout(
      FieldName = "Decision Values",
      VerboseDescription = "Decision rule values",
      Order = 0.0
      )
   @Parameter(
      ID = "FIXED_VALUE_FIRM_DECISION_RULE_VALUE"
      )
   private SampledDoubleModelParameterConfiguration
      decisionRuleGenerator;
   
   public SampledDoubleModelParameterConfiguration getDecisionRuleGenerator() {
      return decisionRuleGenerator;
   }
   
   public void setDecisionRuleGenerator(final SampledDoubleModelParameterConfiguration value) {
      this.decisionRuleGenerator = value;
   }
   
   /**
     * Create a {@link FixedValueFirmDecisionRuleConfiguration} object with default 
     * parameters.
     */
   public FixedValueFirmDecisionRuleConfiguration() {
      this(new ConstantDoubleModelParameterConfiguration(0.));
   }
   
   /**
     * Create a {@link FixedValueFirmDecisionRuleConfiguration} object with custom 
     * parameters.
     * 
     * @param decisionOutcome
     *        A configurator for a {@link SampledDoubleModelParameterConfiguration}.
     *        This configurator will be used to create an instance of a {@link Parameter}
     *        whose values are to be the results of {@link FixedValueFirmDecisionRule}{@code s}
     *        created by this module. This argument must be non-<code>null</code>.
     */
   public FixedValueFirmDecisionRuleConfiguration(
      final SampledDoubleModelParameterConfiguration decisionOutcome) {
      this.decisionRuleGenerator = Preconditions.checkNotNull(decisionOutcome);
   }
   
   @Override
   protected void addBindings() {
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(FixedValueFirmDecisionRule.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
