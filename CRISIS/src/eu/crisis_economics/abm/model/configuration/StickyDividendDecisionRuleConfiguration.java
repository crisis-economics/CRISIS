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
import eu.crisis_economics.abm.firm.plugins.StickyDividendDecisionRule;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A {@link ConfigurationComponent} for the {@link StickyDividendDecisionRule} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Sticky Dividend"
   )
public final class StickyDividendDecisionRuleConfiguration
   extends AbstractFirmLiquidityTargetDecisionRuleConfiguration
   implements FirmLiquidityTargetDecisionRuleConfiguration {
   
   private static final long serialVersionUID = 6835512274847829863L;
   
   @Parameter(
      ID = "STICKY_DIVIDEND_DECISION_RULE_ENABLE_TIME"
      )
   private double
      enableTime = StickyDividendDecisionRule.DEFAULT_ENABLE_TIME;
   @Parameter(
      ID = "STICKY_DIVIDEND_DECISION_RULE_MAX_ENVELOPE_CHANGE_UPPER"
      )
   private double
      maxEnvelopeChangeUpper = StickyDividendDecisionRule.DEFAULT_MAX_ENVELOPE_CHANGE_UPPER;
   @Parameter(
      ID = "STICKY_DIVIDEND_DECISION_RULE_MAX_ENVELOPE_CHANGE_LOWER"
      )
   private double
      maxEnvelopeChangeLower = StickyDividendDecisionRule.DEFAULT_MAX_ENVELOPE_CHANGE_LOWER;
   @Parameter(
      ID = "STICKY_DIVIDEND_DECISION_ADAPTATION_RATE"
      )
   private double
      adaptationRate = StickyDividendDecisionRule.DEFAULT_ADAPTATION_RATE;
   @Parameter(
      ID = "STICKY_DIVIDEND_DECISION_MINIMUM_ENVELOPE"
      )
   private double
      minimumEnvelope = StickyDividendDecisionRule.DEFAULT_MINIMUM_ENVELOPE;
   
   public double getEnableTime() {
      return enableTime;
   }
   
   public void setEnableTime(
      final double enableTime) {
      this.enableTime = enableTime;
   }
   
   public double getMaxEnvelopeChangeUpper() {
      return maxEnvelopeChangeUpper;
   }
   
   public void setMaxEnvelopeChangeUpper(
      final double maxEnvelopeChangeUpper) {
      this.maxEnvelopeChangeUpper = maxEnvelopeChangeUpper;
   }
   
   public double getMaxEnvelopeChangeLower() {
      return maxEnvelopeChangeLower;
   }
   
   public void setMaxEnvelopeChangeLower(
      final double maxEnvelopeChangeLower) {
      this.maxEnvelopeChangeLower = maxEnvelopeChangeLower;
   }
   
   public double getAdaptationRate() {
      return adaptationRate;
   }
   
   public void setAdaptationRate(
      final double adaptationRate) {
      this.adaptationRate = adaptationRate;
   }
   
   public double getMinimumEnvelope() {
      return minimumEnvelope;
   }

   public void setMinimumEnvelope(
      double minimumEnvelope) {
      this.minimumEnvelope = minimumEnvelope;
   }
   
   @Submodel
   @Parameter(
      ID = "STICKY_DIVIDEND_DECISION_RULE_COMPOSITE"
      )
   private FirmLiquidityTargetDecisionRuleConfiguration
      underlyingDecisionRule = new TransientLiquidityBufferDecisionRuleConfiguration();
   
   public FirmLiquidityTargetDecisionRuleConfiguration getUnderlyingDecisionRule() {
      return underlyingDecisionRule;
   }
   
   public void setUnderlyingDecisionRule(
      final FirmLiquidityTargetDecisionRuleConfiguration underlyingDecisionRule) {
      this.underlyingDecisionRule = underlyingDecisionRule;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(enableTime >= 0.);
      Preconditions.checkArgument(adaptationRate >= 0.);
      Preconditions.checkArgument(maxEnvelopeChangeLower >= 0.);
      Preconditions.checkArgument(maxEnvelopeChangeUpper >= 0.);
      Preconditions.checkArgument(minimumEnvelope >= 0.);
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
      "STICKY_DIVIDEND_DECISION_RULE_ENABLE_TIME",                enableTime,
      "STICKY_DIVIDEND_DECISION_RULE_MAX_ENVELOPE_CHANGE_UPPER",  maxEnvelopeChangeUpper,
      "STICKY_DIVIDEND_DECISION_RULE_MAX_ENVELOPE_CHANGE_LOWER",  maxEnvelopeChangeLower,
      "STICKY_DIVIDEND_DECISION_ADAPTATION_RATE",                 adaptationRate,
      "STICKY_DIVIDEND_DECISION_MINIMUM_ENVELOPE",                minimumEnvelope
      ));
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(StickyDividendDecisionRule.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
