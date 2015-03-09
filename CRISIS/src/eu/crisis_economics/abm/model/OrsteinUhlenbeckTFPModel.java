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
import eu.crisis_economics.abm.model.configuration.AssetThresholdFirmBankruptcyHandlerConfiguration;
import eu.crisis_economics.abm.model.configuration.CobbDouglasProductionFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.ConstantDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.ExponentialAdaptivePortfolioWeightingConfiguration;
import eu.crisis_economics.abm.model.configuration.ExponentialSmoothingStockReturnExpectationFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.FundamentalistStockReturnExpectationFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.LinearCombinationStockReturnExpectationFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.OrsteinUhlenbeckSeriesConfiguration;
import eu.crisis_economics.abm.model.configuration.RandomSeriesParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.SimpleStockAndLoanPortfolioConfiguration;
import eu.crisis_economics.abm.model.configuration.TrendFollowerStockReturnExpectationFunctionConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * An Input-Output (IO) economy with Cobb Douglas firms experiencing 
  * Orstein-Uhlenbeck Total Factor Productivity noise.<br><br>
  * 
  * This model additionally provides {@code 4} Noise-Trader Mutual Funds.
  * 
  * @author phillips
  */
public class OrsteinUhlenbeckTFPModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public OrsteinUhlenbeckTFPModel() {
      this(1L);
   }
   
   public OrsteinUhlenbeckTFPModel(long seed) {
      super(seed);
   }
   
   /**
     * A lightweight configurator for the {@link OrsteinUhlenbeckTFPModel} model.
     * 
     * @author phillips
     */
   @ConfigurationComponent(
      DisplayName = "Configure Model",
      Description =
      "An Input-Output (IO) Economy with Cobb-Douglas Macrofirms experiencing "
    + "Orstein-Uhlenbeck Total Factor Productivity (TFP) noise. This model additionally "
    + "provides <code>4</code> Noise-Trader Mutual Funds.\n\n"
    + "Increasing the variance of the Orstein-Uhlenbeck process is expected to destabilize "
    + "the economy and result in firm bankruptcies. Further increasing the variance of the "
    + "Orstein-Uhlenbeck process is expected to result in bank bankruptcies. The default "
    + "configuration for this model results in frequent firm bankruptcies and infrequent or "
    + "rare bank bankruptcies."
      )
   protected static class OrsteinUhlenbeckTFPModelConfiguration {
      @Layout(
         Order = 0.0,
         Title = "Firms",
         FieldName = "Firm Production Function"
         )
      @Submodel
      private CobbDouglasProductionFunctionConfiguration
         firmProductionFunctionConfiguration = new CobbDouglasProductionFunctionConfiguration();
      
      public CobbDouglasProductionFunctionConfiguration
         getFirmProductionFunctionConfiguration() {
         return firmProductionFunctionConfiguration;
      }
      
      public void setFirmProductionFunctionConfiguration(
         final CobbDouglasProductionFunctionConfiguration value) {
         this.firmProductionFunctionConfiguration = value;
      }
      
      public OrsteinUhlenbeckTFPModelConfiguration() {
         firmProductionFunctionConfiguration.setCobbDouglasTotalFactorProductivity(
            new RandomSeriesParameterConfiguration(
               new OrsteinUhlenbeckSeriesConfiguration(
                  Math.pow(21., 0.9),
                  0.9, 
                  Math.pow(21., 0.9),
                  .22 * Math.pow(21., 0.9),
                  Math.pow(1., 0.9),
                  Math.pow(30., 0.9),
                  1
                  )));
      }
   }
   
   @Submodel
   @Layout(
      Order = 0.0,
      FieldName = "Configure"
      )
   private OrsteinUhlenbeckTFPModelConfiguration
      components = new OrsteinUhlenbeckTFPModelConfiguration();
   
   public OrsteinUhlenbeckTFPModelConfiguration getComponents() {
      return components;
   }
   
   public void setComponents(
      final OrsteinUhlenbeckTFPModelConfiguration value) {
      this.components = value;
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      
      ModelUtils.apply(agentsConfiguration, true, "setLabourToOfferPerHousehold",
         new ConstantDoubleModelParameterConfiguration(2.5));
      
      ModelUtils.apply(agentsConfiguration, true, "setIndifferenceThresholdFractionOfMarkup", 0.);
      
      ModelUtils.apply(agentsConfiguration, true, "setDoBanksBuyBankStocks", false);
      
      ModelUtils.apply(agentsConfiguration, true, "setProductionFunction", 
          components.firmProductionFunctionConfiguration);
      
      ModelUtils.apply(
         agentsConfiguration, "NUMBER_OF_FUNDS_ARE_NOISE_TRADERS", 0, false);
      
      ModelUtils.apply(
         agentsConfiguration,
         "BANK_PORTFOLIO",
         new SimpleStockAndLoanPortfolioConfiguration(
            new ExponentialAdaptivePortfolioWeightingConfiguration(0.6),
            new ExponentialSmoothingStockReturnExpectationFunctionConfiguration(
               0.5,
               new LinearCombinationStockReturnExpectationFunctionConfiguration(
                  new FundamentalistStockReturnExpectationFunctionConfiguration(4.0), 
                  new TrendFollowerStockReturnExpectationFunctionConfiguration(2),
                  new ConstantDoubleModelParameterConfiguration(1.0)
                  )
            )
         ),
         true
         );
      
      ModelUtils.apply(
         agentsConfiguration,
         "FUNDS_PORTFOLIO",
         new SimpleStockAndLoanPortfolioConfiguration(
            new ExponentialAdaptivePortfolioWeightingConfiguration(0.6),
            new ExponentialSmoothingStockReturnExpectationFunctionConfiguration(
               0.5,
               new LinearCombinationStockReturnExpectationFunctionConfiguration(
                  new FundamentalistStockReturnExpectationFunctionConfiguration(4.0), 
                  new TrendFollowerStockReturnExpectationFunctionConfiguration(2),
                  new ConstantDoubleModelParameterConfiguration(1.0)
                  )
            )
         ),
         true
         );
      
      ModelUtils.apply(
         agentsConfiguration,
         "FIRM_BANKRUPTCY_HANDLER",
         new AssetThresholdFirmBankruptcyHandlerConfiguration(), true
         );
      
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
      doLoop(OrsteinUhlenbeckTFPModel.class, argv);
      System.out.println("CRISIS OU-TFP Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -5669567742635775111L;
}
