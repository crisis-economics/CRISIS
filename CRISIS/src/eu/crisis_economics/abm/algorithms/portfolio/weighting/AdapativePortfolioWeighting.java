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
package eu.crisis_economics.abm.algorithms.portfolio.weighting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

/**
  * A base class for adapative portfolio weighting algorithms.
  * 
  * @author phillips
  */
class AdapativePortfolioWeighting extends AbstractPortfolioWeighting {
   
   private double
      adaptationRate,
      initialReturn;
      
   private Map<String, Double>
      weights;
   
   /**
     * Create an {@link AdapativePortfolioWeighting} object with custom parameters.
     * 
     * @param initReturn
     *        The initial return rate for each instrument to consider.
     * @param adaptRate
     *        The adaptation rate to use when modifying portfolio weights.
     */
   protected AdapativePortfolioWeighting(
      final double initReturn,
      final double adaptRate
      ) {
      Preconditions.checkArgument(adaptRate >= 0.);
      weights = new HashMap<String, Double>();
      this.initialReturn = initReturn;
      this.adaptationRate = adaptRate;
   }
   
   public void computeWeights() {
      if(weights.size() != super.size()) {
         weights = new HashMap<String, Double>();
         for(final Entry<String, Double> record : super.getReturns().entrySet())
            weights.put(record.getKey(), initialReturn);
         normalizeWeights();
      }
   }
   
   /**
     * Normalize portfolio weights. If all portfolio weights are positive
     * and at least one weight is nonzero, then the sum of all weights will
     * be equal to <code>1.0</code> after this method is called.
     */
   public final void normalizeWeights() {
      double norm = 0.;
      for(double w : weights.values())
         norm += w;
      for(Entry<String, Double> record : weights.entrySet())
         record.setValue(record.getValue() / norm);
   }
   
   /**
     * Get the portfolio weight for the instrument with the specified reference.
     */
   @Override
   public final double getWeight(final String reference) {
      return weights.get(reference);
   }
   
   /**
     * Set the portfolio weight for the instrument with the specified reference.
     */
   public final double setWeight(
      final String reference,
      final double value
      ) {
      return weights.put(reference, value);
   }
   
   /**
     * Get a copy of the portfolio weights.
     */
   public final Map<String, Double> getWeights() {
      return new HashMap<String, Double>(weights);
   }
   
   /**
     * Set the market adaptation rate for this algorithm.
     */
   public final void setAdaptationRate(final double m) {
      adaptationRate = m;
   }
   
   /**
     * Get the market adaptation rate for this algorithm.
     */
   public final double getAdaptationRate() {
      return adaptationRate;
   }
}