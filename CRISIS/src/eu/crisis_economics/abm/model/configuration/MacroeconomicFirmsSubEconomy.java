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
import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.FirmFactory;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.firm.MacroFirm;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Macroeconomic Firms",
   Description = "Macroeconomic firms with customizable goods and cash endowments, "
               + "stock emission value, credit demand, goods pricing and target production "
               + "decision rules, wage bid price and dividend payments. The firm "
               + "production function, and input-output network technological weights "
               + "are customizable. The initial stockholders for firms are banks."
   )
public final class MacroeconomicFirmsSubEconomy extends AbstractFirmsSubEconomy {
   
   private static final long serialVersionUID = -4978820698005623800L;
   
   public static final double
      DEFAULT_FIRM_INITIAL_GOODS_ENDOWMENT = 32.;
   
   public static final boolean
      DEFAULT_DO_ENABLE_FIRM_PRODUCTION_NOISE = true;
   
   @Layout(
      Order = 2.4,
      VerboseDescription = "Goods & Services",
      FieldName = "Initial Goods Owned Per Firm"
      )
   @Parameter(
      ID = "FIRM_INITIAL_OWNED_GOODS"
      )
   private double
      initialGoodsOwned = DEFAULT_FIRM_INITIAL_GOODS_ENDOWMENT;
   
   /**
     * Verbose description for the parameter {@link #getFirmGoodsInitialSalePrice}.
     */
   public final String desFirmGoodsInitialSalePrice() {
      return "The initial selling price (per unit) of goods in firms' inventories.";
   }
   
   /**
     * Get the initial {@link Firm} goods endowment. The argument should be 
     * non-negative.
     */
   public final void setInitialGoodsOwned(final double value) {
      initialGoodsOwned = value;
   }
   
   /**
     * Set final the initial {@link Firm} goods endowment.
     */
   public double getInitialGoodsOwned() {
      return initialGoodsOwned;
   }
   
   /**
     * Verbose description for the parameter {@link #setInitialGoodsOwned}.
     */
   public final String desInitialGoodsOwned() {
      return 
         "The quantity of goods owned by firms at the start of the simulation.";
   }
   
   /*
    * Submodels
    */
   @Layout(
      Order = 3,
      Title = "Credit Demand",
      VerboseDescription = "Demand for Commercial Loans",
      FieldName = "Credit Demand Function"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_CREDIT_DEMAND_FUNCTION"
      )
   private CreditDemandFunctionConfiguration creditDemandFunction =
      new CustomRiskToleranceCreditDemandFunctionConfiguration();
   
   public final CreditDemandFunctionConfiguration getCreditDemandFunction() {
      return creditDemandFunction;
   }
   
   public final void setCreditDemandFunction(
      final CreditDemandFunctionConfiguration value) {
      this.creditDemandFunction = value;
   }
   
   @Layout(
      Order = 4,
      Title = "Selling Price",
      VerboseDescription = "Goods ask price decision rules",
      FieldName = "Goods Ask Price Decision Rule"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_PRODUCTION_GOODS_PRICING_RULE"
      )
   private FirmGoodsPricingDecisionRuleConfiguration goodsPricingAlgorithm =
      new FollowTargetProductionGoodsPricingConfiguration();
   
   public final FirmGoodsPricingDecisionRuleConfiguration getGoodsPricingAlgorithm() {
      return goodsPricingAlgorithm;
   }
   
   public final void setGoodsPricingAlgorithm(
      final FirmGoodsPricingDecisionRuleConfiguration value) {
      this.goodsPricingAlgorithm = value;
   }
   
   @Layout(
      Order = 5,
      Title = "Production",
      VerboseDescription = "Target Production Decision Rules and the Production Function",
      FieldName = "Production Function"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_PRODUCTION_FUNCTION"
      )
   private FirmProductionFunctionConfiguration productionFunction =
      new CobbDouglasProductionFunctionConfiguration();
   
   public final FirmProductionFunctionConfiguration getProductionFunction() {
      return productionFunction;
   }
   
   public final void setProductionFunction(
      final FirmProductionFunctionConfiguration value) {
      this.productionFunction = value;
   }
   
   @Layout(
      Order = 6,
      FieldName = "Target Production Decision Rule"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_TARGET_PRODUCTION_RULE"
      )
   private FirmTargetProductionDecisionRuleConfiguration targetProductionAlgorithm =
      new FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration();
   
   public final FirmTargetProductionDecisionRuleConfiguration getTargetProductionAlgorithm() {
      return targetProductionAlgorithm;
   }
   
   public final void setTargetProductionAlgorithm(
      final FirmTargetProductionDecisionRuleConfiguration value) {
      this.targetProductionAlgorithm = value;
   }
   
   @Layout(
      Order = 7,
      Title = "Wages && Labour",
      VerboseDescription = "Wage Bid Price (Per Unit Labour Per Quarter)",
      FieldName = "Bid Price Decision Rule"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_LABOUR_WAGE_BID_RULE"
      )
   FirmLabourWageAskPriceDecisionRuleConfiguration labourWageDecisionRule =
      new FixedWageBidPriceAlgorithmConfiguration();
   
   public final FirmLabourWageAskPriceDecisionRuleConfiguration getLabourWageDecisionRule() {
      return labourWageDecisionRule;
   }
   
   public final void setLabourWageDecisionRule(
      final FirmLabourWageAskPriceDecisionRuleConfiguration value) {
      this.labourWageDecisionRule = value;
   }
   
   @Layout(
      Order = 8,
      Title = "Dividends",
      VerboseDescription = "Dividend Issuance & Liquidity Targets",
      FieldName = "Liquidity Target Decision Rule"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_LIQUIDITY_TARGET_RULE_FUNCTION"
      )
   FirmLiquidityTargetDecisionRuleConfiguration liquidityTargetDecisionRule =
      new StickyDividendDecisionRuleConfiguration();
   
   public final FirmLiquidityTargetDecisionRuleConfiguration getLiquidityTargetDecisionRule() {
      return liquidityTargetDecisionRule;
   }
   
   public final void setLiquidityTargetDecisionRule(
      final FirmLiquidityTargetDecisionRuleConfiguration value) {
      this.liquidityTargetDecisionRule = value;
   }
   
   @Layout(
      Order = 9,
      Title = "Bankruptcy",
      VerboseDescription = "Bankruptcy Resolution",
      FieldName = "Bankruptcy Handler"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_BANKRUPTCY_HANDLER"
      )
   FirmBankruptcyHandlerConfiguration bankruptcyHandler =
      new DefaultFirmBankruptcyHandlerConfiguration();
   
   public final FirmBankruptcyHandlerConfiguration getBankruptcyHandler() {
      return bankruptcyHandler;
   }
   
   public final void setBankruptcyHandler(
      final FirmBankruptcyHandlerConfiguration value) {
      this.bankruptcyHandler = value;
   }
   
   @Override
   protected void assertParameterValidity() {
      super.assertParameterValidity();
      Preconditions.checkArgument(initialGoodsOwned >= 0,
         toParameterValidityErrMsg("Initial Firm Goods Endowment quantity is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
      "FIRM_INITIAL_OWNED_GOODS",               initialGoodsOwned
      ));
      install(new FactoryModuleBuilder()
         .implement(LoanStrategyFirm.class, MacroFirm.class)
         .build(FirmFactory.class)
         );
      expose(FirmFactory.class);
      super.addBindings();
   } 
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Macroeconomic Firms SubEconomy: " + super.toString();
   }
}
