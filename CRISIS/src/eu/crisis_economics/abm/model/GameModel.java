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
import eu.crisis_economics.abm.model.configuration.LabourOnlyProductionFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.OrsteinUhlenbeckSeriesConfiguration;
import eu.crisis_economics.abm.model.configuration.RandomSeriesParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.TransientLiquidityBufferDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;

/**
  * A base class for the leverage game.
  * 
  * @author phillips
  */
public class GameModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public GameModel() {
      this(1L);
   }
   
   public GameModel(long seed) {
      super(seed);
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      
      ModelUtils.apply(agentsConfiguration, true, "setLabourToOfferPerHousehold",
         new ConstantDoubleModelParameterConfiguration(10.0));
      
//      ModelUtils.apply(agentsConfiguration, true, "setFundPortfolioWeightingAlgorithm",
//         new HomogeneousPortfolioWeightingConfiguration());
      
//      ModelUtils.apply(agentsConfiguration, true, "setBankPortfolioWeightingAlgorithm",
//         new FixedInstrumentWeightPortfolioWeightingConfiguration());
      
      ModelUtils.apply(agentsConfiguration, true, "setIndifferenceThresholdFractionOfMarkup", 0.);
      
      ModelUtils.apply(agentsConfiguration, true, "setProductionFunction",
         new LabourOnlyProductionFunctionConfiguration());
      
      ModelUtils.apply(agentsConfiguration, true, "setLabourOnlyProductionFunctionTFPFactor",
         new RandomSeriesParameterConfiguration(
            new OrsteinUhlenbeckSeriesConfiguration(
               1.2, 0.9, 1.2, 0.22, 0.1, 2.5, 1)));  // sigma = 0.2 is too high. 0.1 is too low.
      
      ModelUtils.apply(agentsConfiguration, true, "setLiquidityTargetDecisionRule",
         new TransientLiquidityBufferDecisionRuleConfiguration());
      
      ModelUtils.apply(agentsConfiguration, true, "setDoEnableGovernment", false);
      
      ModelUtils.apply(agentsConfiguration, true, "setNumberOfFundsAreNoiseTraders", 4);
      
      ModelUtils.apply(agentsConfiguration, true, "setDoBanksBuyBankStocks", false);
      
//      ModelUtils.apply(
//         agentsConfiguration, true, "setInitialCashEndowmentPerHousehold",
//         new ConstantDoubleModelParameterConfiguration(6.75e7 * 3. / 2.));
      
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
      doLoop(GameModel.class, argv);
      System.out.println("CRISIS Game Model Base");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -8105784616660048201L;
}
