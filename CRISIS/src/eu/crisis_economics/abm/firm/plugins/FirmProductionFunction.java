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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.algorithms.optimization.OptimalProduction;
import eu.crisis_economics.abm.firm.Firm;

/**
  * An abstract base class for {@link Firm} Production Functions. Production
  * functions determine input demands (Ie. units of goods and labour) given a 
  * target production yield. Typically, the production function will optimize the 
  * input goods selection problem in such a way as to minimize the total
  * cost suffered by the producer.
  * 
  * @author phillips
  */
public abstract class FirmProductionFunction {
   
   private Map<String, Double>
      weightTerms;
   
   /**
     * Create a {@link FirmProductionFunction} object with custom parameters.
     * 
     * @param weightTerms
     *        Weights (Eg. technological weights) corresponding to each named
     *        sector. This {@link Map} must be populated with key-value pairs
     *        corresponding to sector names and their corresponding numerical 
     *        weights. This argument must be non-<code>null</code>.
     */
   public FirmProductionFunction(
      final Map<String, Double> weightTerms
      ) {
      this.weightTerms = Preconditions.checkNotNull(weightTerms);
   }
   
   /** 
     * Compute the optimal demand for input goods and labour.
     */
   public abstract OptimalProduction computeInputsDemand(
      double productionTarget, 
      double labourUnitCost,
      Map<String, Double> economicCosts
      );
   
   /** 
     * Generate production goods using input goods and labour.
     */
   public abstract Yield produce(
      double labourInput, 
      Map<String, Double> goodsInputVolumes,
      double labourUnitCost,
      Map<String, Double> cashCostsPerUnitGoods
      );
   
   static protected FirmProductionFunction.Yield createYield(
      double goodsYield,
      double totalCashProductionCost
      ) {
      return new FirmProductionFunction.Yield(
         goodsYield,
         totalCashProductionCost
         );
   }
   
   static public final class Yield { // Immutable
      private double 
         goodsYield,
         totalCashProductionCost;
      
      private Yield(double goodsYield, double totalCashProductionCost) {
         this.goodsYield = goodsYield;
         this.totalCashProductionCost = totalCashProductionCost;
      }
      
      public double getGoodsYield() {
         return goodsYield;
      }
      
      public double getCashProductionCost() { 
         return totalCashProductionCost;
      }
   }
   
   /**
     * Get the technological weight corresponding to a good.
     */
   protected final double getWeight(final String goodsType) {
      return weightTerms.get(goodsType);
   }
   
   /**
     * Return a copy of all weight terms known to this production function.
     */
   protected final Map<String, Double> getWeights() {
      return new HashMap<String, Double>(weightTerms);
   }
   
   /**
     * Filter out key-value pairs from a {@link Map}. This method returns a
     * new {@link Map} containing only the specified keys. Zeros elements will
     * be created for technological weights with no corresponding key in
     * the input {@link Map}.
     * 
     * @param map
     *        The {@link Map} from which to draw records
     * @param keys
     *        A {@link Collection} of keys to accept
     */
   protected final Map<String, Double> filterKeys(
      final Map<String, Double> map,
      final Collection<String> keys
      ) {
      final Map<String, Double>
         result = new HashMap<String, Double>();
      for(final String key : keys)
         if(map.containsKey(key))
            result.put(key, map.get(key));
         else
            result.put(key, 0.);
      return result;
   }
}
