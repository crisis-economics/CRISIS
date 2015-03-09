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
import eu.crisis_economics.abm.model.configuration.ConstantDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * TODO
  * 
  * @author phillips
  */
public class GBCLModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public GBCLModel() {
      this(1L);
   }
   
   @ConfigurationComponent(
      DisplayName = "Configure GCBL Model..",
      Description = "An economy with a graded commercial loan clearing market, gilts and bonds."
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
      
      GBCLConfiguration() { }
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
   
   public GBCLModel(long seed) {
      super(seed);
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      ModelUtils.applyWithCloning(agentsConfiguration, "setLoanMarket",
         new ClearingGiltsBondsAndCommercialLoansMarketConfiguration());
      ModelUtils.applyWithCloning(agentsConfiguration, "setBankClearingMarketResponses",
         components.bankMarketInteraction);
      
      ModelUtils.apply(
         agentsConfiguration, true, "setHouseholdTargetFundInvestmentValue", 6.75e7 * 4.);
      ModelUtils.apply(
         agentsConfiguration, true, "setInitialCashEndowmentPerHousehold", 
            new ConstantDoubleModelParameterConfiguration(6.75e7 * 3. / 10.));
      ModelUtils.apply(
         agentsConfiguration.getFundsSubEconomy(), true,
         "setFundamentalistStockReturnRiskPremium", 10.);
      ModelUtils.apply(
         agentsConfiguration.getFundsSubEconomy(), true,
         "setAdaptationRate", 1./3.);
      
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
      doLoop(GBCLModel.class, argv);
      System.out.println("CRISIS GBCL Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -8083095083973183613L;
}
