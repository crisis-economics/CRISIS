/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.firm.plugins;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.optimization.OptimalProduction;
import eu.crisis_economics.abm.algorithms.optimization.WeightedCobbDouglasAlgorithm;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;

/**
  * A implementation of the Cobb-Douglas Firm production function, with
  * technological weights. Technological weights are invariant in time 
  * in this implementation.
  * @author phillips
  */
public final class CobbDouglasProductionFunction 
   extends FirmProductionFunction {
   
   private TimeseriesParameter<Double>
      tfp;
   private double
      alpha;
   
   /**
     * Enable {@code VERBOSE_MODE = true} for verbose console analysis.
     */
   private static final boolean
      VERBOSE_MODE = false;
   
   /**
     * Create a {@link CobbDouglasProductionFunction}.
     * @param tfp
     *        The Total Factor Productivity (TFP factor). This parameter
     *        should take strictly positive values.
     * @param alpha
     *        A parameter describing the firm preference for labour
     *        as compared to input goods. This parameter should be in
     *        the range [0, 1].
     * @param technologicalWeights
     *        Technological weights for each differentiated market good.
     */
   @AssistedInject
   public CobbDouglasProductionFunction(
   @Named("COBB_DOUGLAS_PRODUCTION_FUNCTION_TOTAL_FACTOR_PRODUCTIVITY")
      final TimeseriesParameter<Double> tfp,
   @Named("COBB_DOUGLAS_PRODUCTION_FUNCTION_ALPHA")
      final double alpha,
   @Assisted
      final Map<String, Double> technologicalWeights
      ) {
      super(technologicalWeights);
      this.tfp = tfp;
      this.alpha = alpha;
   }
   
   /**
     * Determine input demands by optimising a Cobb-Douglas problem.
     * 
     * @param productionTarget
     *        The desired production (to optimize for)
     * @param labourUnitCost
     *        The anticipated price per unit labour
     * @param economicCosts
     *        The anticipated cost per unit goods to buy. <u>Economic costs
     *        must be specified for every technological weight</u>.
     */
   @Override
   public OptimalProduction computeInputsDemand(
      final double productionTarget,
      final double labourUnitCost,
      final Map<String, Double> economicCosts
      ) {
      final Map<String, Double>
         weightsToUse = super.getWeights();
      final WeightedCobbDouglasAlgorithm
         cobbDouglasAlgorithm = new WeightedCobbDouglasAlgorithm(
            labourUnitCost,
            weightsToUse,
            economicCosts,
            Math.pow(tfp.get(), 1./alpha),
            alpha
            );
      final OptimalProduction
         result = cobbDouglasAlgorithm.solve(productionTarget);
      if(VERBOSE_MODE) {
         System.out.printf(
            "Cobb Douglas Production Function: computed input demands for production target: "
          + "%16.10g, labour unit cost: %16.10g. Goods economic costs: " + economicCosts + "."
          + "Desired labour: " + result.getDesiredUnitsOfLabour() + ", desired goods inputs: "
          + result.getDemandAmounts() + ", total economic cost: "
          + result.getTotalEconomicCost() + ".\n", 
            productionTarget, labourUnitCost
            );
      }
      return result;
   }
   
   /**
     * Get the yield (goods produced) and the total cash cost of production.
     * 
     * @param goodsInputQuantities
     *        The intended quantity of goods to use for each goods type.
     *        <u>Quantities must be specified for each technological weight</u>.
     * @param unitPrices
     *        The anticipated cost per unit goods to buy. <u>Economic costs
     *        must be specified for every technological weight</u>.
     */
   @Override
   public FirmProductionFunction.Yield produce(
      final double labourInput,
      final Map<String, Double> goodsInputQuantities,
      final double labourUnitCost,
      final Map<String, Double> unitPrices
      ) {
      final WeightedCobbDouglasAlgorithm
         algorithm = new WeightedCobbDouglasAlgorithm(
            labourUnitCost,
            super.getWeights(),
            unitPrices,
            Math.pow(tfp.get(), 1./alpha),
            alpha
            );
      final double
         totalProduction =
            algorithm.evaluateProductionFor(labourInput, goodsInputQuantities),
         totalCashCost = algorithm.evaluateTotalCostFor(labourInput, goodsInputQuantities);
      if(VERBOSE_MODE) {
         System.out.printf(
            "Cobb Douglas Production Function: computed production yield: labour input: %16.10g, "
          + "labour unit cost: %16.10g, goods inputs: " + goodsInputQuantities + ", unit prices: "
          + unitPrices + ".\n",
            labourInput,
            labourUnitCost
            );
      }
      return super.createYield(totalProduction, totalCashCost);
   }
   
   /**
     * Get the (instantaneous) TFP factor for this production function.
     */
   public double getTFPFactor() {
      return tfp.get();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Cobb Douglas Firm Production Function, tfp: " + tfp + ", alpha:" + alpha + ".";
   }
}
