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
  * A solution to the Cobb-Douglas equation with technological weights.
  * 
  * @author phillips
  */
public class WeightedCobbDouglasAlgorithm 
   extends ProductionCostMinimizationAlgorithm {
   
   private final static double 
      MANDATORY_LABOUR_CAP = 0.;
   
   private final double
      alpha, z;
   
   public WeightedCobbDouglasAlgorithm(
      final double labourUnitCost,
      final Map<String, Double> technologicalWeights,
      final Map<String, Double> economicUnitCosts,
      final double z,
      final double alpha
      ) {
      super(labourUnitCost, technologicalWeights, economicUnitCosts);
      this.alpha = alpha;
      this.z = z;
   }
   
   /** 
     * Evaluate the Cobb-Douglas production function.<br><br>
     */
   @Override
   public double evaluateProductionFor(
      final double labourInput, 
      final Map<String, Double> goodsInputVolumes
      ) {
      if(goodsInputVolumes.size() + 1 != super.getProblemDimension())
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": the dimension of the input goods demands "
          + "does not correspond to the dimension of the optimization problem."
            );
      double 
         totalProduction = Math.pow(z, alpha) * Math.pow(labourInput, alpha),
         __1minusAlpha = (1 - alpha);
      for(final Entry<String, Double> record : goodsInputVolumes.entrySet()) {
         final GoodsDescriptor
            goodsType = super.getTypeDescriptor(record.getKey());
         totalProduction *= Math.pow(
            record.getValue(), __1minusAlpha * goodsType.getWeight()
            );
      }
      return totalProduction;
   }
    
    /** Solve the Cobb-Douglas optimisation problem. */
    @Override
    public ProductionCostMinimizationAlgorithm.OptimizationSoluton 
        solve(final double productionTarget) {
        double
            __powZAlpha = Math.pow(z, alpha), 
            __1minusAlpha = (1.-alpha),
            __prod = this.computePiFactor(),
            labourDemandResult = productionTarget/(__powZAlpha * 
                Math.pow(__1minusAlpha * super.getLabourUnitCost()/alpha, 
                         __1minusAlpha)*__prod);
        double desiredLabourResult = 
            Math.max(labourDemandResult, MANDATORY_LABOUR_CAP);
        
        double 
            totalEconomicCost = (desiredLabourResult * super.getLabourUnitCost()),
            __prefix = 
               __powZAlpha * __prod *
               Math.pow(__1minusAlpha * super.getLabourUnitCost()/alpha, -alpha); 
        
        final Map<String, Double>
           typeDemands = new HashMap<String, Double>();
        
        for(final Entry<String, GoodsDescriptor> record :
            super.getTypeDescriptors().entrySet()) {
           final GoodsDescriptor
              type = record.getValue();
           
           // This expression does evaluate to 0.0 for goods whose 
           //  technological weights are zero.
           final double demandResult = productionTarget /
               (__prefix * type.getEconomicUnitCost()/type.getWeight());
           
           if(demandResult == 0.)
              continue;
           totalEconomicCost += demandResult * type.getEconomicUnitCost();
           typeDemands.put(record.getKey(), demandResult);
        }
        
        final ProductionCostMinimizationAlgorithm.OptimizationSoluton
           result = new OptimizationSoluton(typeDemands, totalEconomicCost, desiredLabourResult);
        return result;
    }
    
    private double computePiFactor() {
        double 
            result = 1.,
            __1minusAlpha = (1.-alpha);
        for(final GoodsDescriptor type : super.getTypeDescriptors().values()) {
           if(type.getWeight() == 0.)
              continue;
           double
              base = type.getWeight()/type.getEconomicUnitCost(),
              exponent = __1minusAlpha * type.getWeight();
           result *= Math.pow(base, exponent);
        }
        return result;
    }
}
