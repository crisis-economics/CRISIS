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
package eu.crisis_economics.abm.algorithms.optimization;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author      JKP
 * @category    Algorithms.Optimization
 * @see         
 * @since       1.0
 * @version     1.0
 */
public final class NeumannLeontieffAlgorithm 
   extends ProductionCostMinimizationAlgorithm {
   
   private final double
      alpha,
      __oneMinusAlpha,
      tfpFactor;
   
   public NeumannLeontieffAlgorithm(
      final double labourUnitCost,
      final double alpha,
      final double tfpFactor,
      final Map<String, Double> vonNeumannLeontieffWeights,
      final Map<String, Double> economicUnitCosts) {
      super(labourUnitCost, vonNeumannLeontieffWeights, economicUnitCosts);
      this.alpha = alpha;
      this.__oneMinusAlpha = (1. - alpha);
      this.tfpFactor = tfpFactor;
   }
   
   /** 
     * Evaluate the Neumann-Leontieff production function.
     */
   @Override
   public double evaluateProductionFor(
      final double labourInput,
      final Map<String, Double> goodsInputVolumes
      ) {
      if (goodsInputVolumes.size() != super.getNumGoodsTypes())
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": goods types and input volumes do not correspond.");
      double totalProduction = Double.MAX_VALUE;
      totalProduction = Math.min(
         totalProduction,
         alpha * labourInput
         );
      for(final Entry<String, GoodsDescriptor> record :
         super.getTypeDescriptors().entrySet()) {
         final double
            quantity = goodsInputVolumes.containsKey(record.getKey()) ? 
               goodsInputVolumes.get(record.getKey()) : 0.;
         totalProduction = Math.min(
            totalProduction,
            __oneMinusAlpha * quantity * record.getValue().getWeight()
            );
      }
      return tfpFactor * totalProduction;
   }
   
   /**
     * Solve the Neumann-Leontieff optimisation problem.
     */
   @Override
   public ProductionCostMinimizationAlgorithm.OptimizationSoluton solve(
      final double productionTarget
      ) {
      final Map<String, Double>
         typeDemands = new HashMap<String, Double>();
      final double desiredLabour = Math.max(
         0., productionTarget / (tfpFactor * alpha));
      double totalEconomicCost = 0;
      totalEconomicCost += desiredLabour * super.getLabourUnitCost();
      for(final Entry<String, GoodsDescriptor> record :
         super.getTypeDescriptors().entrySet()) {
         final double
            demandForThisType = alpha * desiredLabour
               / (__oneMinusAlpha * record.getValue().getWeight());
         typeDemands.put(record.getKey(), demandForThisType);
         totalEconomicCost += demandForThisType * record.getValue().getEconomicUnitCost();
      }
      final ProductionCostMinimizationAlgorithm.OptimizationSoluton
         result = new OptimizationSoluton(
            typeDemands,
            totalEconomicCost,
            desiredLabour
            );
      return result;
   }
   
   /**
    * Get the alpha factor.
    */
   public double getAlpha() {
      return alpha;
   }
   
   /**
    * Get the total productivity multiplier.
    */
   public double getTFPFactor() {
      return tfpFactor;
   }
}
