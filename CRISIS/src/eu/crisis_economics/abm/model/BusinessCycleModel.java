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
package eu.crisis_economics.abm.model;

import eu.crisis_economics.abm.model.configuration.AR1ConsumptionBudgetDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.configuration.ConstantDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.MacroeconomicFirmsSubEconomy;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.ReducePriceIfUnsoldPricingAlgorithmConfiguration;
import eu.crisis_economics.abm.model.configuration.StickyDividendDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * A simple model with an endogenous business cycle. This model inherits default 
  * configuration options from the Crisis Master model. {@link Firm} decision
  * rules are modified to induce a business cycle. The length and the severity of
  * depressions in the business cycle is customizable.
  * 
  * @author phillips
  */
public class BusinessCycleModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public BusinessCycleModel() {
      this(1L);
   }
   
   @ConfigurationComponent(
      DisplayName = "Configure Business Cycle Model..",
      Description = "An economy with a natural, endogeneous macroeconomic business cycle."
      )
   public static class BusinessCycleConfiguration {
      
      @Layout(
         Title = "Firm Decision Rules",
         Order = 1.,
         FieldName = "Goods Prices",
         VerboseDescription = "Ask price decision rules for producers"
         )
      @Submodel
      protected ReducePriceIfUnsoldPricingAlgorithmConfiguration
         goodsPricingDecisionRule = new ReducePriceIfUnsoldPricingAlgorithmConfiguration();
      
      public ReducePriceIfUnsoldPricingAlgorithmConfiguration getGoodsPricingDecisionRule() {
         return goodsPricingDecisionRule;
      }
      
      public void setGoodsPricingDecisionRule(
         final ReducePriceIfUnsoldPricingAlgorithmConfiguration goodsPricingDecisionRule) {
         this.goodsPricingDecisionRule = goodsPricingDecisionRule;
      }
      
      @Layout(
         Order = 1.1,
         FieldName = "Dividends & Liquidity Targets",
         VerboseDescription = "Dividend and liquidity decision rules for producers"
         )
      @Submodel
      protected StickyDividendDecisionRuleConfiguration
         stickyDividendDecisionRule = new StickyDividendDecisionRuleConfiguration();
      
      public StickyDividendDecisionRuleConfiguration getStickyDividendDecisionRule() {
         return stickyDividendDecisionRule;
      }
      
      public void setStickyDividendDecisionRule(
         final StickyDividendDecisionRuleConfiguration stickyDividendDecisionRule) {
         this.stickyDividendDecisionRule = stickyDividendDecisionRule;
      }
      
      public BusinessCycleConfiguration() {
         this.goodsPricingDecisionRule.setAdaptationRate(0.08);
      }
      
      @Layout(
         Order = 1.2,
         FieldName = "Household Consumption",
         VerboseDescription = "Household goods consumption budget decisions"
         )
      @Submodel
      protected AR1ConsumptionBudgetDecisionRuleConfiguration
         householdConsumptionBudgetRule = new AR1ConsumptionBudgetDecisionRuleConfiguration();
      
      public AR1ConsumptionBudgetDecisionRuleConfiguration
         getHouseholdConsumptionBudgetRule() {
         return householdConsumptionBudgetRule;
      }
      
      public void setHouseholdConsumptionBudgetRule(
         AR1ConsumptionBudgetDecisionRuleConfiguration value) {
         this.householdConsumptionBudgetRule = value;
      }
   }
   
   @Layout(
      Order = 0,
      Title = "Business Cycle Economy",
      FieldName = "Options"
      )
   @Submodel
   private BusinessCycleConfiguration
      components = new BusinessCycleConfiguration();
   
   public BusinessCycleConfiguration getComponents() {
      return components;
   }
   
   public void setComponents(
      final BusinessCycleConfiguration components) {
      this.components = components;
   }
   
   public BusinessCycleModel(long seed) {
      super(seed);
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      final MacroeconomicFirmsSubEconomy
         firmsSubEconomy = new MacroeconomicFirmsSubEconomy();
      
      firmsSubEconomy.setGoodsPricingAlgorithm(
         components.goodsPricingDecisionRule);
      
      firmsSubEconomy.setLiquidityTargetDecisionRule(
         components.stickyDividendDecisionRule);
      
      agentsConfiguration.setFirmsSubEconomy(firmsSubEconomy);
      
      ModelUtils.apply(
         agentsConfiguration, true, "setTargetProductionDecisionRuleGraceFactor", 0.03);
      ModelUtils.apply(
         agentsConfiguration, true, "setLabourToOfferPerHousehold",
         new ConstantDoubleModelParameterConfiguration(1.5));
      ModelUtils.apply(
         agentsConfiguration, true, "setConsumptionBudgetAllocationRule",
            components.householdConsumptionBudgetRule);
      
      return agentsConfiguration;
   }
   
   @Override
   protected AbstractPlumbingConfiguration getPlumbing() {
      return plumbingConfiguration;
   }
   
   /**
     * Entry Point
     */
   public static void main(final String[] argv) {
      doLoop(BusinessCycleModel.class, argv);
      System.out.println("CRISIS Business Cycle Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -7009274420318006531L;
}
