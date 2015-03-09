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
import eu.crisis_economics.abm.model.configuration.ClearingGiltsBondsAndCommercialLoansMarketConfiguration;
import eu.crisis_economics.abm.model.configuration.CommercialBankMarketResponseFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.ValueAtRiskTargetLeverageConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * TODO
  * 
  * @author phillips
  */
public class GBCLModelWithBasel2NoiseTradersAndBusinessCycle extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public GBCLModelWithBasel2NoiseTradersAndBusinessCycle() {
      this(1L);
   }
   
   @ConfigurationComponent(
      DisplayName = "Configure Supplemented GCBL Model..",
      Description =
         "An economy with a graded commercial loan clearing market, gilts, bonds, "
       + "Basel II financial leverage regulation, a macroeconomic business cycle and "
       + "Noise-Trader mutual funds."
      )
   protected static class GBCLConfiguration {
      
      @Layout(
         FieldName = "Bank Market Interaction",
         Order = 1.0
         )
      @Submodel
      private CommercialBankMarketResponseFunctionConfiguration
         bankMarketInteraction = new CommercialBankMarketResponseFunctionConfiguration();
      
      public CommercialBankMarketResponseFunctionConfiguration
            getBankMarketInteraction() {
         return bankMarketInteraction;
      }
      
      public void setBankMarketInteraction(
         final CommercialBankMarketResponseFunctionConfiguration value) {
         this.bankMarketInteraction = value;
      }
      
      @Layout(
         FieldName = "Basel II Regulation",
         Order = 2.0
         )
      @Submodel
      private ValueAtRiskTargetLeverageConfiguration
         basel2Regulation = new ValueAtRiskTargetLeverageConfiguration();
      
      public ValueAtRiskTargetLeverageConfiguration getBasel2Regulation() {
         return basel2Regulation;
      }
      
      public void setBasel2Regulation(
         final ValueAtRiskTargetLeverageConfiguration value) {
         this.basel2Regulation = value;
      }
      
      public GBCLConfiguration() {
         basel2Regulation.setPermanentLeverageScalingFactor(.075 * .55);
      }
   }
   
   @Layout(
      Order = 0,
      Title = "GCBL Economy",
      FieldName = "Options"
      )
   @Submodel
   private GBCLConfiguration
      components = new GBCLConfiguration();
   
   public GBCLConfiguration getComponents() {
      return components;
   }
   
   public void setComponents(
      final GBCLConfiguration components) {
      this.components = components;
   }
   
   public GBCLModelWithBasel2NoiseTradersAndBusinessCycle(long seed) {
      super(seed);
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      ModelUtils.applyWithCloning(agentsConfiguration, "setLoanMarket",
         new ClearingGiltsBondsAndCommercialLoansMarketConfiguration());
      ModelUtils.applyWithCloning(agentsConfiguration, "setBankClearingMarketResponses",
         components.bankMarketInteraction);
      ModelUtils.apply(agentsConfiguration, true, "setLeverageAlgorithm",
         components.basel2Regulation);
      
      ModelUtils.applyWithCloning(
         agentsConfiguration, "setNumberOfFundsAreNoiseTraders", 4);
      ModelUtils.apply(
         agentsConfiguration, true, "setTargetProductionDecisionRuleGraceFactor", 0.03);
      
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
      doLoop(GBCLModelWithBasel2NoiseTradersAndBusinessCycle.class, argv);
      System.out.println("CRISIS GBCL Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -8083095083973183613L;
}
