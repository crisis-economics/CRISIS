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

import java.io.File;

import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.FirmLiquidityTargetDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.FixedValueFirmDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.FromFileFirmSectorProviderConfiguration;
import eu.crisis_economics.abm.model.configuration.FromFileSampledDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.FromFileSectorNameFactoryConfiguration;
import eu.crisis_economics.abm.model.configuration.MacroeconomicFirmsSubEconomy;
import eu.crisis_economics.abm.model.configuration.MarketsSubEconomy;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.ModelParameterAgentNameFactoryConfiguration;
import eu.crisis_economics.abm.model.configuration.SampledDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.SectorNameProviderConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * A model with FTSE100 firms and sectors.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "FTSE100 Model"
   )
public class FTSE100Model extends AbstractModel {
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public FTSE100Model() {
      this(1L);
   }
   
   public FTSE100Model(final long seed) {
      super(seed);
   }
   
   @ConfigurationComponent(
      DisplayName = "Configure"
      )
   public static class FTSE100EconomyConfiguration {
      
      @Layout(
         Title = "FTSE100 Sectors",
         Order = 1,
         FieldName = "Sectors Names"
         )
      private File
         ftse100SectorNames = new File("./data/FTSE100-sector-names.dat");
      
      public File getFtse100SectorNames() {
         return ftse100SectorNames;
      }
      
      public void setFtse100SectorNames(
         final File ftse100SectorNames) {
         this.ftse100SectorNames = ftse100SectorNames;
      }
      
      @Layout(
         Order = 1.1,
         FieldName = "Firm Sectors"
         )
      private File
         ftse100FirmSectors = new File("./data/FTSE100-firm-sectors.dat");
      
      public File getFtse100FirmSectors() {
         return ftse100FirmSectors;
      }
      
      public void setFtse100FirmSectors(
         final File ftse100FirmSectors) {
         this.ftse100FirmSectors = ftse100FirmSectors;
      }
      
      @Layout(
         Title = "Firm Names",
         Order = 2.0,
         FieldName = "Names"
         )
      private File
         ftse100FirmNames = new File("./data/FTSE100-firm-names.dat");
      
      public File getFtse100FirmNames() {
         return ftse100FirmNames;
      }
      
      public void setFtse100FirmNames(
         final File ftse100FirmNames) {
         this.ftse100FirmNames = ftse100FirmNames;
      }
      
      public FTSE100EconomyConfiguration() { }
   }
   
   @Layout(
      Order = 1,
      Title = "FTSE100 Model",
      FieldName = "Configure Model",
      VerboseDescription = 
         "An economy with FTSE100 initial conditions for (100) firms. Firms in this model:"
       + "<ul>" 
       + "   <li> Are grouped according to the same input-output sectors as real FTSE100 firms,<br>" 
       + "   <li> Have initial stock emission values comparable to real FTSE100 firms,<br>"
       + "   <li> Have real FTSE100 firm names<br>"
       + "</ul>"
       + "These initial conditions are read from configuration files. These files are customizable."
      )
   @Submodel
   private FTSE100EconomyConfiguration
      economy = new FTSE100EconomyConfiguration();
   
   public FTSE100EconomyConfiguration getEconomy() {
      return economy;
   }
   
   public void setEconomy(
      final FTSE100EconomyConfiguration economy) {
      this.economy = economy;
   }

   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      final MacroeconomicFirmsSubEconomy
         firmsSubEconomy = new MacroeconomicFirmsSubEconomy();
      
      firmsSubEconomy.setNumberOfFirms(100);
      
      final FromFileFirmSectorProviderConfiguration
         sectorProvider = new FromFileFirmSectorProviderConfiguration(
            economy.getFtse100FirmSectors().getAbsolutePath());
      
      firmsSubEconomy.setSectors(sectorProvider);
      
      final ModelParameterAgentNameFactoryConfiguration
         namingConvention = new ModelParameterAgentNameFactoryConfiguration(
            economy.getFtse100FirmNames().getAbsolutePath());
      
      firmsSubEconomy.setNamingConvention(namingConvention);
      
      final SampledDoubleModelParameterConfiguration
         stockEmissionValues = new FromFileSampledDoubleModelParameterConfiguration(
            "./data/FTSE100-firm-stock-emission-values.dat");
      
      final FirmLiquidityTargetDecisionRuleConfiguration
         liquidityTargets = new FixedValueFirmDecisionRuleConfiguration(
            new FromFileSampledDoubleModelParameterConfiguration(
               "./data/FTSE100-firm-liquidity-targets.dat"));
      
      firmsSubEconomy.setInitialFirmPricePerShare(stockEmissionValues);
      
      firmsSubEconomy.setLiquidityTargetDecisionRule(liquidityTargets);
      
      agentsConfiguration.setFirmsSubEconomy(firmsSubEconomy);
      
      final MarketsSubEconomy
         marketsSubEconomy = new MarketsSubEconomy();
      
      final SectorNameProviderConfiguration
         sectorNamesProvider = new FromFileSectorNameFactoryConfiguration(
            economy.getFtse100SectorNames().getAbsolutePath());
      
      marketsSubEconomy.setSectorNames(sectorNamesProvider);
      
      agentsConfiguration.setMarketsSubEconomy(marketsSubEconomy);
      
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
      doLoop(FTSE100Model.class, argv);
      System.out.println("CRISIS FTSE100 Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -8722004641065434287L;
}
