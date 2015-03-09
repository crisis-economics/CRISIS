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
import eu.crisis_economics.abm.firm.plugins.FollowAggregateMarketDemandTargetProductionAlgorithm;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Follow Market Demand"
   )
public final class FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration
   extends AbstractPrivateConfiguration
   implements FirmTargetProductionDecisionRuleConfiguration {
   
   private static final long serialVersionUID = 7816339183060166231L;
   
   public static final double
      DEFAULT_FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_INITIAL_TARGET = 32,
      DEFAULT_FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_GRACE_FACTOR = 0.04,
      DEFAULT_FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_MARKET_ADAPTATION_RATE = 0.05;
   
   @Layout(
      Order = 1,
      FieldName = "Initial Production Target"
      )
   @Parameter(
      ID = "FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_INITIAL_TARGET"
      )
   private double
      initialProductionTarget =
         DEFAULT_FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_INITIAL_TARGET;
   
   @Layout(
      Order = 2,
      FieldName = "Grace Factor"
      )
   @Parameter(
      ID = "FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_GRACE_FACTOR"
      )
   private double
      targetProductionDecisionRuleGraceFactor = 
         DEFAULT_FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_GRACE_FACTOR;
   
   @Layout(
      Order = 3,
      FieldName = "Market Adaptation Rates"
      )
   @Parameter(
      ID = "FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_MARKET_ADAPTATION_RATE"
      )
   private double
      marketAdaptationRate =
         DEFAULT_FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_MARKET_ADAPTATION_RATE;
   
   public double getInitialProductionTarget() {
      return initialProductionTarget;
   }
   
   public void setInitialProductionTarget(
      final double followMarketDemandTargetProductionAlgorithmInitialTarget) {
      this.initialProductionTarget =
         followMarketDemandTargetProductionAlgorithmInitialTarget;
   }
   
   public double getTargetProductionDecisionRuleGraceFactor() {
      return targetProductionDecisionRuleGraceFactor;
   }
   
   public void setTargetProductionDecisionRuleGraceFactor(
      final double followMarketDemandTargetProductionAlgorithmGraceFactor) {
      this.targetProductionDecisionRuleGraceFactor =
         followMarketDemandTargetProductionAlgorithmGraceFactor;
   }
   
   public final String desTargetProductionDecisionRuleGraceFactor() {
      return 
         "The sales threshold for which unsold goods (relative to total production)" + 
         "will trigger production activities for firms. In example, an expansion " + 
         "grace of 0.04 (4%) will trigger expansion activities for firms whenever 96% of " +
         "production goods are sold.";
   }
   
   public double getMarketAdaptationRate() {
      return marketAdaptationRate;
   }
   
   public void setMarketAdaptationRate(
      final double value) {
      this.marketAdaptationRate = value;
   }
   
   public final String desMarketAdaptationRate() {
      return 
         "The maximum rate at which firms will adapt their production targets " + 
         "and sale prices to reflect the market state (namely, their ability to sell goods). " + 
         "In the case of production targets, the adaptation rate is the maximum rate " + 
         "at which production yield targets will be increased. In the case of goods " + 
         "unit sale prices, the adaptation rate is the maximal change in the unit ask price.";
   }
   
   /**
     * Create a {@link FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration}
     * object with default parameters.
     */
   public FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration() { }
   
   /**
     * Create a {@link FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration}
     * object with custom parameters.
     * 
     * @param initialProducitonTarget <br>
     *        The initial production target for rules configured by this component.
     * @param graceFactor <br>
     *        The optimism (overproduction propensity) for rules configured by this component.
     * @param marketAdaptationRate <br>
     *        The market adaptation rate for rules configured by this component.
     */
   public FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration(
      final double initialProducitonTarget,
      final double graceFactor,
      final double marketAdaptationRate
      ) {
      this.initialProductionTarget = initialProducitonTarget;
      this.targetProductionDecisionRuleGraceFactor = graceFactor;
      this.marketAdaptationRate = marketAdaptationRate;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         initialProductionTarget >= 0,
         "Follow Aggregate Market Demand Target Production Algorithm: the initial firm goods "
       + "production target is negative.");
      Preconditions.checkArgument(
         targetProductionDecisionRuleGraceFactor >= 0,
         "Follow Aggregate Market Demand Target Production Algorithm: the firm market adaptation "
       + "rate parameter is negative.");
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_INITIAL_TARGET",
         initialProductionTarget,
         "FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_GRACE_FACTOR",
         targetProductionDecisionRuleGraceFactor,
         "FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_MARKET_ADAPTATION_RATE",
         marketAdaptationRate
         ));
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(FollowAggregateMarketDemandTargetProductionAlgorithm.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
