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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.plugins.FirmProductionFunction;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.model.configuration.AR1ConsumptionBudgetDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.AbstractPrivateConfiguration;
import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.configuration.BuyFromOneInNSectorsHouseholdConsumptionConfiguration;
import eu.crisis_economics.abm.model.configuration.ChainedInputOutputMatrixFactoryConfiguration;
import eu.crisis_economics.abm.model.configuration.CobbDouglasProductionFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.ConstantDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.CustomFirmDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.FirmProductionFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.FixedInstrumentWeightPortfolioWeightingConfiguration;
import eu.crisis_economics.abm.model.configuration.FixedValueFirmDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.FromMathExpressionDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.LabourOnlyProductionFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.MacroeconomicFirmsSubEconomy;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.ReducePriceIfUnsoldPricingAlgorithmConfiguration;
import eu.crisis_economics.abm.model.configuration.StickyDividendDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import eu.crisis_economics.abm.simulation.injection.factories.FirmProductionFunctionFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * A model with a {@link Firm} production chain. This model creates a macroeconomy with
  * the following structure:
  * 
  * <ul>
  *   <li> {@link Firm} sectors are divided in into {@code N} groups;
  *   <li> The first such group does not require input goods for production. This special
  *        group only requires {@link Labour} for production;
  *   <li> Every other group, with index {@code 0 < n < N}, requires (a) input goods from 
  *        group {@code n - 1} and (b) a small about of {@link Labour} for production;
  *   <li> {@link Household} {@link Agent}{@code s} buy goods exclusively from group {@code N-1}
  *        (namely, the last group in the production chain).
  * </ul>
  * 
  * @author phillips
  */
public class ProductionChainModel extends AbstractModel {
   
   private static final long serialVersionUID = -1320267970890821275L;
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   @ConfigurationComponent(
      DisplayName = "Configure Model..",
      Description = 
         "A model with a firm production chain.\n\n"
       + "This model creates a macroeconomy with the following structure:"
       + "<ul>"
       + "   <li> Firm sectors (industrial sectors) are divided in into N groups (with indices "
       + "0, 1, ... N-1)"
       + "   <li> The first such group does not require input goods for production."
       + "        This special group only requires labour for production."
       + "   <li> Every other group (with index 0 &lt; n) requires: (a) input goods from"
       + "        group (n-1), and (b) a small about of labour for production."
       + "   <li> Household agents buy goods exclusively from group (N-1)"
       + "        (namely, the last group in the production chain)."
       + "</ul>"
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
      
      @Layout(
         Order = 1.3,
         Title = "Market Structure",
         FieldName = "Input-Output Network",
         VerboseDescription = "The firm production input-output network"
         )
      @Submodel
      protected ChainedInputOutputMatrixFactoryConfiguration
         ioMatrixConfiguration = new ChainedInputOutputMatrixFactoryConfiguration(4);
      
      public ChainedInputOutputMatrixFactoryConfiguration getIoMatrixConfiguration() {
         return ioMatrixConfiguration;
      }
      
      public void setIoMatrixConfiguration(
         ChainedInputOutputMatrixFactoryConfiguration ioMatrixConfiguration) {
         this.ioMatrixConfiguration = ioMatrixConfiguration;
      }
      
      @Layout(
         Order = 1.4,
         FieldName = "Sectors To Buy From",
         VerboseDescription = "Domestic Retailers"
         )
      @Submodel
      private BuyFromOneInNSectorsHouseholdConsumptionConfiguration
         sectorsToBuyFrom = new BuyFromOneInNSectorsHouseholdConsumptionConfiguration(4, 1);
      
      public BuyFromOneInNSectorsHouseholdConsumptionConfiguration getSectorsToBuyFrom() {
         return sectorsToBuyFrom;
      }
      
      public void setSectorsToBuyFrom(
         final BuyFromOneInNSectorsHouseholdConsumptionConfiguration sectorsToBuyFrom) {
         this.sectorsToBuyFrom = sectorsToBuyFrom;
      }
      
      @Layout(
         Order = 2.0,
         Title = "Retailer && Intermediary Firm Production Functions",
         FieldName = "Retailer",
         VerboseDescription = "Firms consuming input goods"
         )
      @Submodel
      private CobbDouglasProductionFunctionConfiguration
         retailerProductionFunction = new CobbDouglasProductionFunctionConfiguration(
            54. / getChainLength(), 0.05);
      
      public CobbDouglasProductionFunctionConfiguration
         getRetailerProductionFunction() {
         return retailerProductionFunction;
      }
      
      public void setRetailerProductionFunction(
         CobbDouglasProductionFunctionConfiguration retailerProductionFunction) {
         this.retailerProductionFunction = retailerProductionFunction;
      }
      
      @Layout(
         Order = 2.1,
         FieldName = "Root Producer",
         VerboseDescription = "Root producers at the start of the production chain"
         )
      @Submodel
      @Parameter(
         ID = "ROOT_PRODUCER_PRODUCTION_FUNCTION_FACTORY"
         )
      private LabourOnlyProductionFunctionConfiguration
         rootProducerProductionFunction = new LabourOnlyProductionFunctionConfiguration(
            new ConstantDoubleModelParameterConfiguration(.39));
      
      public LabourOnlyProductionFunctionConfiguration
            getRootProducerProductionFunction() {
         return rootProducerProductionFunction;
      }
      
      public void setRootProducerProductionFunction(
         LabourOnlyProductionFunctionConfiguration rootProducerProductionFunction) {
         this.rootProducerProductionFunction = rootProducerProductionFunction;
      }
      
      /**
        * Get the production chain length for this model.
        */
      private int getChainLength() {
         return ioMatrixConfiguration.getChainLength();
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
   
   public ProductionChainModel() {
      this(1L);
   }
   
   public ProductionChainModel(long seed) {
      super(seed);
   }
   
   @Singleton
   static class FirmProductionFunctionProvider implements FirmProductionFunctionFactory {
      private static int
         counter = 0;
      
      private final int
         chainLength;
      
      private final FirmProductionFunctionFactory
         rootProducerProductionFunctionFactory,
         retailerProductionFunctionFactory;
      
      @Inject
      FirmProductionFunctionProvider(
      @Named("PRODUCTION_CHAIN_LENGTH")
         int chainLength,
      @Named("ROOT_PRODUCER_PRODUCTION_FUNCTION_FACTORY")
         FirmProductionFunctionFactory rootProducerProductionFunctionFactory,
      @Named("RETAILER_PRODUCTION_FUNCTION_FACTORY")
         FirmProductionFunctionFactory retailerProductionFunctionFactory
         ) {
         Preconditions.checkArgument(chainLength > 0);
         this.chainLength = chainLength;
         this.rootProducerProductionFunctionFactory = rootProducerProductionFunctionFactory;
         this.retailerProductionFunctionFactory = retailerProductionFunctionFactory;
      }
      
      @Override
      public FirmProductionFunction create(final String productionGoodsType) {
         if(counter++ % chainLength == 0)
            return rootProducerProductionFunctionFactory.create(productionGoodsType);
         else
            return retailerProductionFunctionFactory.create(productionGoodsType);
      }
   }
   
   private class CustomFirmProductionFunction
      extends AbstractPrivateConfiguration implements FirmProductionFunctionConfiguration {
      
      private static final long serialVersionUID = 8375734897634527L;
      
      @Submodel
      @Parameter(
         ID = "RETAILER_PRODUCTION_FUNCTION_FACTORY"
         )
      private CobbDouglasProductionFunctionConfiguration
         retailerProductionFunction = components.retailerProductionFunction;
      
      @Submodel
      @Parameter(
         ID = "ROOT_PRODUCER_PRODUCTION_FUNCTION_FACTORY"
         )
      private LabourOnlyProductionFunctionConfiguration
         rootProducerProductionFunction = components.rootProducerProductionFunction;
      
      @Override
      protected void addBindings() {
         install(InjectionFactoryUtils.asPrimitiveParameterModule(
            "PRODUCTION_CHAIN_LENGTH", components.getChainLength()));
         bind(FirmProductionFunctionFactory.class)
            .annotatedWith(Names.named(getScopeString()))
            .to(FirmProductionFunctionProvider.class);
         expose(FirmProductionFunctionFactory.class)
            .annotatedWith(Names.named(getScopeString()));
      }
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      final MacroeconomicFirmsSubEconomy
         firmsSubEconomy = new MacroeconomicFirmsSubEconomy();
      
      firmsSubEconomy.setGoodsPricingAlgorithm(
         components.goodsPricingDecisionRule);
      
      firmsSubEconomy.setLiquidityTargetDecisionRule(
         components.stickyDividendDecisionRule);
      
      firmsSubEconomy.setProductionFunction(new CustomFirmProductionFunction());
      
      agentsConfiguration.setFirmsSubEconomy(firmsSubEconomy);
      
      ModelUtils.apply(
         agentsConfiguration, true, "setTargetProductionDecisionRuleGraceFactor", 0.03);
      ModelUtils.apply(
         agentsConfiguration, true, "setLabourToOfferPerHousehold",
         new ConstantDoubleModelParameterConfiguration(10.));
      ModelUtils.apply(
         agentsConfiguration, true, "setConsumptionBudgetAllocationRule",
            components.householdConsumptionBudgetRule);
      
      ModelUtils.apply(agentsConfiguration, true, "setDoBanksBuyBankStocks", false);
      
      ModelUtils.apply(agentsConfiguration, true, "setBankPortfolioWeightingAlgorithm",
         new FixedInstrumentWeightPortfolioWeightingConfiguration("Loan", .6));
      
      ModelUtils.apply(
         agentsConfiguration, true, "setSectorsToBuyFrom",
            components.sectorsToBuyFrom);
      ModelUtils.apply(
         agentsConfiguration, true, "setInputOutputNetwork",
            components.ioMatrixConfiguration);
      
      ModelUtils.apply(
         agentsConfiguration, true, "setGoodsPricingAlgorithm",
         new FixedValueFirmDecisionRuleConfiguration(
            new FromMathExpressionDoubleModelParameterConfiguration(
               "1.7307e8 * pow(1.2, X % 4 + 1)", "X"))
         );
      
      ModelUtils.apply(agentsConfiguration, true, "setInitialFirmPricePerShare",
         new ConstantDoubleModelParameterConfiguration(1.520004257608744E10 / 2.));
      
      ModelUtils.apply(
         agentsConfiguration, true, "setLiquidityTargetDecisionRule",
         new FixedValueFirmDecisionRuleConfiguration(
            new FromMathExpressionDoubleModelParameterConfiguration(
               "(1.520004257608744E10 / 3.)", "X"))
         );
      
      ModelUtils.apply(
         agentsConfiguration, true, "setIndifferenceThresholdFractionOfMarkup", 1./3.);
      
      ModelUtils.apply(
         agentsConfiguration, true, "setTargetProductionAlgorithm",
         new CustomFirmDecisionRuleConfiguration(
            new ConstantDoubleModelParameterConfiguration(53.))
         );
      
      ModelUtils.apply(agentsConfiguration, true, "setBankEquityTarget", 3.25e10 / 2.);  //
      
      ModelUtils.apply(agentsConfiguration, true, "setInitialStockEmissionValuePerBank", 3.25e10);
      
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
      doLoop(ProductionChainModel.class, argv);
      System.out.println("CRISIS Production Chain Model");
      System.exit(0);
   }
   
   
}
