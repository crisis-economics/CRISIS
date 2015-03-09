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
import eu.crisis_economics.abm.model.configuration.BailoutBankBankruptcyPolicyConfiguration;
import eu.crisis_economics.abm.model.configuration.BankBankruptcyPolicyConfiguration;
import eu.crisis_economics.abm.model.configuration.ConstantDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.LabourOnlyProductionFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.OrsteinUhlenbeckSeriesConfiguration;
import eu.crisis_economics.abm.model.configuration.RandomSeriesParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.TransientLiquidityBufferDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * A simplified model with no Input-Output Network (firm production network).<br><br>
  * 
  * This model provides a simplified simulation with the following properties:
  * 
  * <ul>
  *   <li> There is virtually no cap (upper bound) on labour supply (workforce size).
  *   <li> Mutual fund portfolio investments are divided equally among all stock instruments.
  *   <li> Bank portfolio investments are divided unequally among all loan instruments, 
  *        however the relative investment size target for all instruments remains fixed.
  *   <li> Firms do not require input goods for production. Firms require only labour for
  *        production.
  *   <li> The Total Factor Productivity (<code>Z</code>) for each firm production function
  *        is implemented as a mean reverting lognormal random walk process with customizable
  *        standard deviation.
  *   <li> <code>4</code> noise-trader funds are provided.
  * </ul>
  * 
  * @author phillips
  */
public class NoInputOutputNetworkModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public NoInputOutputNetworkModel() {
      this(1L);
   }
   
   public NoInputOutputNetworkModel(long seed) {
      super(seed);
   }
   
   /**
     * A lightweight configurator for the {@link NoInputOutputNetworkModel} model.
     * 
     * @author phillips
     */
   @ConfigurationComponent(
      DisplayName = "Configure Model",
      Description =
      "A simplified model with no Input-Output Network (firm production network).\n" + 
      "\n" + 
      "This model provides a simplified simulation with the following properties:\n" + 
      "<ul>" + 
      "<li> There is virtually no cap (upper bound) on labour supply (workforce size)." + 
      "<li> Mutual fund portfolio investments are divided equally among all stock instruments." + 
      "<li> Bank portfolio investments are divided unequally among all loan instruments," + 
      "     however the relative investment size target for all instruments remains fixed." + 
      "<li> Firms do not require input goods for production. Firms require only labour for" + 
      "     production." + 
      "<li> The Total Factor Productivity (<code>Z</code>) for each firm production function" + 
      "     is implemented as a mean reverting lognormal random walk process with customizable" + 
      "     standard deviation." + 
      "<li> <code>4</code> noise-trader funds are provided." + 
      "</ul>"
      )
   protected static class LabourOnlyModelConfiguration {
      @Layout(
         Order = 0.0,
         Title = "Firms",
         FieldName = "Firm Production Function"
         )
      @Submodel
      private LabourOnlyProductionFunctionConfiguration
         firmProductionFunctionConfiguration = new LabourOnlyProductionFunctionConfiguration();
      
      public LabourOnlyProductionFunctionConfiguration
         getFirmProductionFunctionConfiguration() {
         return firmProductionFunctionConfiguration;
      }
      
      public void setFirmProductionFunctionConfiguration(
         final LabourOnlyProductionFunctionConfiguration value) {
         this.firmProductionFunctionConfiguration = value;
      }
      
      @Layout(
         Order = 0.1,
         Title = "Banks",
         FieldName = "Bank Bankruptcy Policy"
         )
      @Submodel
      private BankBankruptcyPolicyConfiguration
         bankBankruptcyResolutionPolicy = new BailoutBankBankruptcyPolicyConfiguration();
      
      public BankBankruptcyPolicyConfiguration
         getBankBankruptcyResolutionPolicy() {
         return bankBankruptcyResolutionPolicy;
      }
      
      public void setBankBankruptcyResolutionPolicy(
         final BankBankruptcyPolicyConfiguration value) {
         this.bankBankruptcyResolutionPolicy = value;
      }
      
      public LabourOnlyModelConfiguration() {
         firmProductionFunctionConfiguration.setLabourOnlyProductionFunctionTFPFactor(
            new RandomSeriesParameterConfiguration(
               new OrsteinUhlenbeckSeriesConfiguration(
                  1.2, 0.9, 1.2, 0.22, 0.1, 2.5, 1)));
      }
   }
   
   @Submodel
   @Layout(
      Order = 0.0,
      FieldName = "Configure"
      )
   private LabourOnlyModelConfiguration
      components = new LabourOnlyModelConfiguration();
   
   public LabourOnlyModelConfiguration getComponents() {
      return components;
   }
   
   public void setComponents(
      final LabourOnlyModelConfiguration value) {
      this.components = value;
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {

      ModelUtils.apply(agentsConfiguration, true, "setLabourToOfferPerHousehold",
         new ConstantDoubleModelParameterConfiguration(10.0));
      
      ModelUtils.apply(agentsConfiguration, true, "setIndifferenceThresholdFractionOfMarkup", 0.);
      
      ModelUtils.apply(agentsConfiguration, true, "setProductionFunction",
         components.firmProductionFunctionConfiguration);
      
      ModelUtils.apply(agentsConfiguration, true, "setLiquidityTargetDecisionRule",
         new TransientLiquidityBufferDecisionRuleConfiguration());
      
      ModelUtils.apply(agentsConfiguration, true, "setDoEnableGovernment", true);
      
      ModelUtils.apply(agentsConfiguration, true, "setNumberOfFundsAreNoiseTraders", 4);
      
      ModelUtils.apply(agentsConfiguration, true, "setDoBanksBuyBankStocks", false);
      
      ModelUtils.apply(agentsConfiguration, true, "setHouseholdTargetFundInvestmentValue", 
         6.75e7 * 4.);
      
      ModelUtils.apply(agentsConfiguration, true, "setBankBankruptcyResolutionPolicy",
         components.bankBankruptcyResolutionPolicy);
      
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
      doLoop(NoInputOutputNetworkModel.class, argv);
      System.out.println("CRISIS No Input-Output Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = 3085148818163957256L;
}
