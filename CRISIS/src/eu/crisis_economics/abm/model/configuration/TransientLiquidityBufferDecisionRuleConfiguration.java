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

import eu.crisis_economics.abm.firm.plugins.FirmDecisionRule;
import eu.crisis_economics.abm.firm.plugins.TransientBufferLiquidityTarget;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Transient Liquidity Buffer"
   )
public final class TransientLiquidityBufferDecisionRuleConfiguration
   extends AbstractFirmLiquidityTargetDecisionRuleConfiguration
   implements FirmLiquidityTargetDecisionRuleConfiguration {
   
   private static final long serialVersionUID = -2953327648110518424L;
   
   public static final double
      DEFAULT_TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_STARTING_LIQUIDITY_TARGET = 1.0e9,
      DEFAULT_TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_FINAL_LIQUIDITY_TARGET = 1.0e8,
      DEFAULT_TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_EXPONENT = 1./5.;
   public static final int
      DEFAULT_TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_TRANSIENT_TIMEFRAME = 150;
   
   @Parameter(
      ID = "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_STARTING_LIQUIDITY_TARGET"
      )
   private double
      startingLiquidityTarget =
         DEFAULT_TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_STARTING_LIQUIDITY_TARGET;
   @Parameter(
      ID = "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_FINAL_LIQUIDITY_TARGET"
      )
   private double
      finalLiquidityTarget =
         DEFAULT_TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_FINAL_LIQUIDITY_TARGET;
   @Parameter(
      ID = "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_EXPONENT"
      )
   private double
      exponent = DEFAULT_TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_EXPONENT;
   @Parameter(
      ID = "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_TRANSIENT_TIMEFRAME"
      )
   private double
      transientDuration =
         DEFAULT_TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_TRANSIENT_TIMEFRAME;
   
   public double getStartingLiquidityTarget() {
      return startingLiquidityTarget;
   }
   
   public void setStartingLiquidityTarget(
      final double startingLiquidityTarget) {
      this.startingLiquidityTarget = startingLiquidityTarget;
   }
   
   public double getFinalLiquidityTarget() {
      return finalLiquidityTarget;
   }
   
   public void setFinalLiquidityTarget(
      final double finalLiquidityTarget) {
      this.finalLiquidityTarget = finalLiquidityTarget;
   }
   
   public double getTransientDuration() {
      return transientDuration;
   }
   
   public void setTransientDuration(
      final double transientDuration) {
      this.transientDuration = transientDuration;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         startingLiquidityTarget >= 0.,
         toParameterValidityErrMsg("Starting Liquidity Target is negative."));
      Preconditions.checkArgument(
         finalLiquidityTarget >= 0.,
         toParameterValidityErrMsg("Long-Term Liquidity Target is negative."));
      Preconditions.checkArgument(
         transientDuration >= 0,
         toParameterValidityErrMsg("Transient Duration is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
      "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_STARTING_LIQUIDITY_TARGET",  startingLiquidityTarget,
      "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_FINAL_LIQUIDITY_TARGET",     finalLiquidityTarget,
      "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_TRANSIENT_TIMEFRAME",        transientDuration,
      "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_EXPONENT",                   exponent
      ));
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(TransientBufferLiquidityTarget.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
