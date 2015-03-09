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
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * 
  * 
  * @author phillips
  */
public class RepoLoanModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   @ConfigurationComponent(
      DisplayName = "Configure Repo Loan Demo..",
      Description =
         "An economy with sustained central bank repo loan activity."
      )
   public static class RepoLoanConfiguration {
      
      private final double
         DEFAULT_INITIAL_HOUSEHOLD_CASH_ENDOWMENT = 6.75e7 * 3. / 2.;
      
      @Layout(
         FieldName = "Household Initial Cash",
         Order = 1.0
         )
      private double
         householdInitialCashEndowment = DEFAULT_INITIAL_HOUSEHOLD_CASH_ENDOWMENT;
      
      public double getHouseholdInitialCashEndowment() {
         return householdInitialCashEndowment;
      }
      
      public void setHouseholdInitialCashEndowment(double value) {
         this.householdInitialCashEndowment = value;
      }
      
      public RepoLoanConfiguration() { }
   }
   
   @Layout(
      Order = 0,
      Title = "Repo Loan Economy",
      FieldName = "Repo Loan Options"
      )
   @Submodel
   private RepoLoanConfiguration
      configuration = new RepoLoanConfiguration();
   
   public RepoLoanConfiguration getConfiguration() {
      return configuration;
   }
   
   public void setConfiguration(
      final RepoLoanConfiguration leverageAlgorithm) {
      this.configuration = leverageAlgorithm;
   }
   
   public RepoLoanModel() {
      this(1L);
   }
   
   public RepoLoanModel(long seed) {
      super(seed);
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      ModelUtils.apply(
         agentsConfiguration, true, "setInitialCashEndowmentPerHousehold",
         new ConstantDoubleModelParameterConfiguration(
            configuration.getHouseholdInitialCashEndowment()));
      
      // Enable for interbank market:
      ModelUtils.apply(
         agentsConfiguration, "IS_INTERBANK_MARKET_ENABLED", false, false);
      
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
      doLoop(RepoLoanModel.class, argv);
      System.out.println("CRISIS Repo Loan Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -6899863952130166783L;
}
