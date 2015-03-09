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
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.MutualFundSubEconomy;
import eu.crisis_economics.abm.model.configuration.ValueAtRiskTargetLeverageConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * The default, configurable model file for use with the dashboard system. This
  * model file provies an extended and flexible drilldown structure when launched
  * via the dashboard. Subsections of the simulated economy can be enabled and
  * disabled at will, as can the relationships between {@link Agent}{@code s}.
  * 
  * @author phillips
  */
public class Basel2ModelWithNoiseTraders extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public Basel2ModelWithNoiseTraders() {
      this(1L);
   }
   
   @ConfigurationComponent(
      DisplayName = "Configure Basel II Economy.."
      )
   public static class Basel2WithNoiseTradersConfiguration
      extends AbstractPublicConfiguration {
      
      private static final long serialVersionUID = -8821138475246355174L;
      
      @Layout(
        FieldName = "Fund Agents",
        Order = 0,
        Title = "Mutual Funds and Noise Traders"
        )
      @Submodel
      @Parameter(
         ID = "MUTUAL_FUNDS",
         Scoped = false
         )
      private MutualFundSubEconomy
         fundsConfiguration = new MutualFundSubEconomy();
      
      @Layout(
         FieldName = "Leverage Regulation",
         Order = 1,
         Title = "Basel II Constraints"
         )
      @Submodel
      @Parameter(
         ID = "BANK_LEVERAGE_TARGET_ALGORITHM",
         Scoped = false
         )
      private ValueAtRiskTargetLeverageConfiguration
         leverageConfiguration = new ValueAtRiskTargetLeverageConfiguration();
      
      public MutualFundSubEconomy getFundsConfiguration() {
         return fundsConfiguration;
      }
      
      public void setFundsConfiguration(
         final MutualFundSubEconomy fundsConfiguration) {
         this.fundsConfiguration = fundsConfiguration;
      }
      
      public ValueAtRiskTargetLeverageConfiguration getLeverageConfiguration() {
         return leverageConfiguration;
      }
      
      public void setLeverageConfiguration(
         final ValueAtRiskTargetLeverageConfiguration leverageConfiguration) {
         this.leverageConfiguration = leverageConfiguration;
      }
      
      @Override
      protected void addBindings() { }
      
      public Basel2WithNoiseTradersConfiguration() { }
   }
   
   @Layout(
      Order = 0,
      Title = "Basel II Economy",
      FieldName = "Basel II Options",
      VerboseDescription = 
         "An economy subject to Basel II Value-at-Risk (VaR) regulatory constraints.\n\n This "
       + "model provides mutual funds, macroeconomic households with goods consumption and "
       + "a workforce, productive industrial agents, and commercial banks investing in firm "
       + "loans and stock markets. The specifications of the VaR-Leverage setup for this model "
       + "are configurable. In addition to Basel II constraints, this model provides noise "
       + "traders with mean-reverting random stock market investments."
      )
   @Submodel
   private Basel2WithNoiseTradersConfiguration
      configuration = new Basel2WithNoiseTradersConfiguration();
   
   public Basel2WithNoiseTradersConfiguration getConfiguration() {
      return configuration;
   }
   
   public void setConfiguration(
      final Basel2WithNoiseTradersConfiguration leverageAlgorithm) {
      this.configuration = leverageAlgorithm;
   }
   
   public Basel2ModelWithNoiseTraders(long seed) {
      super(seed);
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      ModelUtils.apply(
         agentsConfiguration, true, "setLeverageAlgorithm", configuration.leverageConfiguration);
      
      agentsConfiguration.getHouseholdSubEconomy().setLabourToOfferPerHousehold(
         new ConstantDoubleModelParameterConfiguration(2.5));
      
      final MutualFundSubEconomy
         fundsSubeconomy = new MutualFundSubEconomy();
      
      fundsSubeconomy.setNumberOfFundsAreNoiseTraders(4);
      
      agentsConfiguration.setFundsSubEconomy(fundsSubeconomy);
      
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
      doLoop(Basel2ModelWithNoiseTraders.class, argv);
      System.out.println("CRISIS Basel II Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -3582148240143753851L;
}
