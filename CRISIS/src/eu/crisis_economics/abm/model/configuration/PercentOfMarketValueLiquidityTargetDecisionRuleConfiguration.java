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

import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.plugins.FirmDecisionRule;
import eu.crisis_economics.abm.firm.plugins.PercentOfMarketValueLiquidityTarget;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link PercentOfMarketValueLiquidityTarget}
  * {@link Firm} liquidity decision rule.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Percent Of Stock Value"
   )
public final class PercentOfMarketValueLiquidityTargetDecisionRuleConfiguration
   extends AbstractFirmLiquidityTargetDecisionRuleConfiguration
   implements FirmLiquidityTargetDecisionRuleConfiguration {
   
   private static final long serialVersionUID = -1609200920760310835L;
   
   public static final double
      DEFAULT_PERCENTILE = 10.,
      DEFAULT_INITIAL_LIQUIDITY_TARGET = 7.e7;
   
   @Parameter(
      ID = "MARKET_VALUE_LIQUIDITY_TARGET_PERCENTILE"
      )
   private double
      marketValuePercentile = DEFAULT_PERCENTILE;
   @Parameter(
      ID = "MARKET_VALUE_LIQUIDITY_TARGET_INITIAL_VALUE_TARGET"
      )
   private double
      initialLiquidityTarget = DEFAULT_INITIAL_LIQUIDITY_TARGET;
   
   public double getMarketValuePercentile() {
      return marketValuePercentile;
   }
   
   /**
     * Set the market (stock) value percentile target for liquidity algorithms
     * configured using this component. The argument must be non-negative.
     */
   public void setMarketValuePercentile(
      final double marketValuePercentile) {
      this.marketValuePercentile = marketValuePercentile;
   }
   
   public double getInitialLiquidityTarget() {
      return initialLiquidityTarget;
   }
   
   /**
     * Set the initial liquidity target for firm liquidity decision rules
     * algorithms configured using this component. The argument must be
     * non-negative.
     */
   public void setInitialLiquidityTarget(
      final double initialLiquidityTarget) {
      this.initialLiquidityTarget = initialLiquidityTarget;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(marketValuePercentile >= 0.);
      Preconditions.checkArgument(initialLiquidityTarget >= 0.);
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
      "MARKET_VALUE_LIQUIDITY_TARGET_PERCENTILE",
         marketValuePercentile,
      "MARKET_VALUE_LIQUIDITY_TARGET_INITIAL_VALUE_TARGET",
         initialLiquidityTarget
      ));
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(PercentOfMarketValueLiquidityTarget.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
