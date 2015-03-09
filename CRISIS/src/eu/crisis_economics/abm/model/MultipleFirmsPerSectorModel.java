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

import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.configuration.ConstantDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.MacroeconomicFirmsSubEconomy;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.MultipleFirmsPerSectorProviderConfiguration;
import eu.crisis_economics.abm.model.configuration.TransientLiquidityBufferDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * A customizable model with Multiple Firms Per Sector (MFPS).
  * 
  * @author phillips
  */
public class MultipleFirmsPerSectorModel extends AbstractModel {
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public MultipleFirmsPerSectorModel() {
      this(1L);
   }
   
   public MultipleFirmsPerSectorModel(long seed) {
      super(seed);
   }
   
   @ConfigurationComponent(
      DisplayName = "Configure"
      )
   public static class MultipleFirmsPerSectorEconomyConfiguration {
      @Layout(
         FieldName = "Number of Firms Per Sector",
         Order = 1
         )
      private int
         numberOfFirmsPerSector = 2;
      
      public int getNumberOfFirmsPerSector() {
         return numberOfFirmsPerSector;
      }
      
      /**
        * Set the number of firms per sector for this model. The argument must be
        * non-negative.
        */
      public void setNumberOfFirmsPerSector(
         final int numberOfFirmsPerSector) {
         this.numberOfFirmsPerSector = numberOfFirmsPerSector;
      }
      
      public MultipleFirmsPerSectorEconomyConfiguration() { }
   }
   
   @Layout(
      Order = 0,
      Title = "Multiple Firms Per Sector",
      FieldName = "Configure Model",
      VerboseDescription = 
         "An economy with multiple producers (firms) per sector. This model provides a"
       + " customizable number of firms competing amongst themselves to sell undifferentiated"
       + " goods. For a simulation with <code>50</code> input-output sectors, there may be"
       + " <code>50 * M</code>, where M is a positive integer, distinct firms each competing"
       + " amongst <code>M</code> peers to create and sell the goods provided by its sector."
       + " Firm initial cash endowments, stock emission values, target production quantities,"
       + " inventory endowments and liquidity targets are downscaled according to the value of"
       + " <code>M</code>. This model otherwise inherits default values for all configuration"
       + " settings specified by the default Master CRISIS model."
      )
   @Submodel
   private MultipleFirmsPerSectorEconomyConfiguration
      configuration = new MultipleFirmsPerSectorEconomyConfiguration();
   
   public MultipleFirmsPerSectorEconomyConfiguration getConfiguration() {
      return configuration;
   }
   
   public void setConfiguration(
      final MultipleFirmsPerSectorEconomyConfiguration leverageAlgorithm) {
      this.configuration = leverageAlgorithm;
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      final MacroeconomicFirmsSubEconomy
      firmsSubEconomy = new MacroeconomicFirmsSubEconomy();
      
      final int
         numberOfFirmsPerSector = configuration.numberOfFirmsPerSector;
      
      final double
         cashEndowmentPerFirm = 
            firmsSubEconomy.getInitialCashEndowmentPerFirm();
      firmsSubEconomy.setInitialCashEndowmentPerFirm(
         cashEndowmentPerFirm / numberOfFirmsPerSector);
      
      final ConstantDoubleModelParameterConfiguration
         fixedStockPricePerShare =
            new ConstantDoubleModelParameterConfiguration(
               MacroeconomicFirmsSubEconomy.DEFAULT_INITIAL_FIRM_PRICE_PER_SHARE /
                  numberOfFirmsPerSector);
      
      firmsSubEconomy.setInitialFirmPricePerShare(fixedStockPricePerShare);
      
      final TransientLiquidityBufferDecisionRuleConfiguration
         firmLiquidityTargetConfiguration = new TransientLiquidityBufferDecisionRuleConfiguration();
      
      firmLiquidityTargetConfiguration.setFinalLiquidityTarget(
         firmLiquidityTargetConfiguration.getFinalLiquidityTarget() /
            numberOfFirmsPerSector);
      firmLiquidityTargetConfiguration.setStartingLiquidityTarget(
         firmLiquidityTargetConfiguration.getStartingLiquidityTarget() /
            numberOfFirmsPerSector);
      
      firmsSubEconomy.setLiquidityTargetDecisionRule(firmLiquidityTargetConfiguration);
      
      final FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration
         targetProductionRuleConfiguration = 
            new FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration();
      
      targetProductionRuleConfiguration.setInitialProductionTarget(
         targetProductionRuleConfiguration.getInitialProductionTarget() /
            numberOfFirmsPerSector);
      
      firmsSubEconomy.setTargetProductionAlgorithm(targetProductionRuleConfiguration);
      
      firmsSubEconomy.setInitialGoodsOwned(
         firmsSubEconomy.getInitialGoodsOwned() / numberOfFirmsPerSector);
      
      final MultipleFirmsPerSectorProviderConfiguration
         sectorProvider = new MultipleFirmsPerSectorProviderConfiguration(
            numberOfFirmsPerSector);
      
      firmsSubEconomy.setSectors(sectorProvider);
      
      firmsSubEconomy.setNumberOfFirms(
         firmsSubEconomy.getNumberOfFirms() * numberOfFirmsPerSector);
      
      agentsConfiguration.setFirmsSubEconomy(firmsSubEconomy);
      
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
      doLoop(MultipleFirmsPerSectorModel.class, argv);
      System.out.println("CRISIS Multiple Firms Per Sector Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = 3781246514394895964L;
}
