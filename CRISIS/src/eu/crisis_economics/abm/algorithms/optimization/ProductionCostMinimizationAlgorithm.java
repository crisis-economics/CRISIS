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
package eu.crisis_economics.abm.algorithms.optimization;

import java.util.Collections;
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
public abstract class ProductionCostMinimizationAlgorithm {
   
   private Map<String, GoodsDescriptor>
      goodsTypes;
   
   private final int
      problemDimension;
   
   private final double
      labourUnitCost;
   
   protected final class GoodsDescriptor {
      private final double
         weight,
         economicUnitCost;
      
      private GoodsDescriptor(   // Immutable
         final double weight,
         final double economicCost
         ) {
         this.weight = weight;
         this.economicUnitCost = economicCost;
      }
      
      public double getWeight() { 
         return weight;
      }
      
      public double getEconomicUnitCost() {
         return economicUnitCost;
      }
   }
   
   protected ProductionCostMinimizationAlgorithm(
      final double labourUnitCost,
      final Map<String, Double> technologicalWeights,
      final Map<String, Double> economicUnitCosts
      ) {
      if(technologicalWeights.size() != economicUnitCosts.size())
         throw new IllegalArgumentException(
            "ProductionCostMinimizationAlgorithm: technological weights"
          + " and economic unit costs do not correspond.");
      if(technologicalWeights.size() == 0)
         throw new IllegalArgumentException(
            "ProductionCostMinimizationAlgorithm: number of goods = 0.");
      this.problemDimension = technologicalWeights.size() + 1;
      this.goodsTypes = new HashMap<String, GoodsDescriptor>();
      for(final Entry<String, Double> record : technologicalWeights.entrySet()) {
         final GoodsDescriptor newType = new GoodsDescriptor(
            record.getValue(),
            economicUnitCosts.get(record.getKey())
            );
         goodsTypes.put(record.getKey(), newType);
      }
      this.labourUnitCost = labourUnitCost;
   }
   
   /**
    * Evaluate the production function.
    */
   public abstract double evaluateProductionFor(
      double labourInput,
      Map<String, Double> goodsInputVolumes
      );
   
   /**
     * Evaluate the total economic cost of production.
     */
   public final double evaluateTotalCostFor(
      final double labourInput,
      final Map<String, Double> goodsInputVolumes
      ) {
      double
         totalCostResult = labourInput * labourUnitCost;
      for(final Entry<String, Double> record : goodsInputVolumes.entrySet()) {
         final GoodsDescriptor
            type = goodsTypes.get(record.getKey());
         totalCostResult += type.economicUnitCost * record.getValue();
      }
      return totalCostResult;
   }
   
   /** 
     * Optimize for minimum costs.
     */
   public abstract ProductionCostMinimizationAlgorithm.OptimizationSoluton
      solve(double productionTarget);
   
   protected final Map<String, GoodsDescriptor> getTypeDescriptors() {
      return goodsTypes;
   }
   
   protected final GoodsDescriptor getTypeDescriptor(final String type) {
      return goodsTypes.get(type);
   }
   
   protected final int getProblemDimension() {
      return problemDimension;
   }
   
   protected final int getNumGoodsTypes() {
      return goodsTypes.size();
   }
   
   protected final double getLabourUnitCost() {
      return labourUnitCost;
   }
   
   /**
     * Result of the optimisation.
     */
   static public final class OptimizationSoluton extends OptimalProduction { // Immutable
   
      private final Map<String, Double>
         demandAmounts;
      private final double
         totalEconomicCost,
         desiredUnitsOfLabour;
      
      public OptimizationSoluton(
         final Map<String, Double> demandAmountsResult,
         final double totalEconomicCostResult,
         final double desiredUnitsOfLabour
         ) {
         this.demandAmounts = Collections.unmodifiableMap(demandAmountsResult);
         this.totalEconomicCost = totalEconomicCostResult;
         this.desiredUnitsOfLabour = desiredUnitsOfLabour;
      }
      
      @Override
      public Map<String, Double> getDemandAmounts() {
         return demandAmounts;
      }
      
      @Override
      public double getTotalEconomicCost() {
         return totalEconomicCost;
      }
      
      @Override
      public double getDesiredUnitsOfLabour() {
         return desiredUnitsOfLabour;
      }
   }
}
