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

import eu.crisis_economics.abm.algorithms.optimization.NeumannLeontieffAlgorithm;
import eu.crisis_economics.abm.algorithms.optimization.OptimalProduction;

/**
 * A von-Neumann-Leontieff {@link FirmProductionFunction} with contribution
 * weights.
 * 
 * @author phillips
 */
public final class NeumannLeontieffProductionFunction
   extends FirmProductionFunction {
   
   double
      alpha, tfpFactor;
   
   /**
     * Create a {@link NeumannLeontieffProductionFunction}.<br><br>
     * 
     * The {@link NeumannLeontieffProductionFunction} has the following 
     * form:<br><br>
     * 
     * Minimize costs, C, subject to:<br>
     *  C = w * L + sum of (goods price, per unit, per goods type) * (amount of goods to buy)<br>
     *  Yield = Z * min(alpha * L, (1 - alpha) * g1 * w1, (1 - alpha) * g2 * w2 ...)<br><br>
     * 
     * where w is the wage per unit labour, L is the amount of labour employed,
     * Z is the Total Factor Productivity, alpha is a bias term controlling the 
     * bias between labour and input goods for production, g_i is the volume of
     * input goods of type i, and w_i is the technological weight applied to 
     * goods type i.
     * @author phillips
     */
   @AssistedInject
   public NeumannLeontieffProductionFunction(
   @Named("NEUMANN_LEONTIEFF_PRODUCTION_FUNCTION_ALPHA")
      final double alpha,
   @Named("NEUMANN_LEONTIEFF_PRODUCTION_FUNCTION_TOTAL_FACTOR_PRODUCTIVITY")
      final double tfpFactor,
   @Assisted
      final Map<String, Double> neumannLeontieffGoodsWeights
      ) {
      super(neumannLeontieffGoodsWeights);
      this.alpha = alpha;
      this.tfpFactor = tfpFactor;
   }
   
   /**
     * Determine input demands by optimising a von-Neumann-Leontieff problem.
     */
   @Override
   public OptimalProduction computeInputsDemand(
      final double productionTarget,
      final double labourUnitCost,
      final Map<String, Double> economicCosts
      ) {
      NeumannLeontieffAlgorithm neumannLeontieffAlgorithm =
         new NeumannLeontieffAlgorithm(
            labourUnitCost,
            alpha,
            tfpFactor,
            super.getWeights(),
            economicCosts
            );
      return neumannLeontieffAlgorithm.solve(productionTarget);
   }
   
   /**
    * Get the yield (goods produced) and total cash cost of production.
    */
   @Override
   public FirmProductionFunction.Yield produce(
      final double labourInput,
      final Map<String, Double> goodsInputVolumes,
      final double labourUnitCost,
      final Map<String, Double> cashGoodsUnitCosts
      ){
      final NeumannLeontieffAlgorithm
         algorithm = new NeumannLeontieffAlgorithm(
            labourUnitCost,
            alpha,
            tfpFactor,
            super.getWeights(),
            cashGoodsUnitCosts
            );
      double
         totalProduction = algorithm.evaluateProductionFor(labourInput, goodsInputVolumes),
         totalCashCost = algorithm.evaluateTotalCostFor(labourInput, goodsInputVolumes);
      return super.createYield(totalProduction, totalCashCost);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Neumann Leontieff Production Function, alpha:" + alpha
            + ", tfpFactor:" + tfpFactor + ".";
   }
}
