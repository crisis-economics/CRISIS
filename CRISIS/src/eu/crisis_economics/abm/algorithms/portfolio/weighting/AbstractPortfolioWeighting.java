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

/**
  * An abstract base class for portfolio weighting algorithms.
  * 
  * @author phillips
  */
public abstract class AbstractPortfolioWeighting implements PortfolioWeighting {
   
   private Map<String, Double>
      expectedReturns = new HashMap<String, Double>();
   
   public void addReturn(
      final String reference,
      final double expectedReturn
      ) {
      expectedReturns.put(reference, expectedReturn);
   }
   
   /**
     * Get a copy of all existing return rates for this algorithm. Modifying the return
     * value will not affect this object.
     */
   protected final Map<String, Double> getReturns() {
      return new HashMap<String, Double>(expectedReturns);
   }
   
   public final int size() {
      return expectedReturns.size();
   }
   
   public void clear() {
      expectedReturns.clear();
   }
   
   /**
     * Get the expected return for the instrument with the specified reference name.
     */
   protected final double getExpectedReturn(final String reference) {
      return expectedReturns.get(reference);
   }
   
   /**
    * Get the sum of all instrument expected returns known to this algorithm.
    */
   protected final double getSumOfExpectedReturns() {
      double result = 0.;
      for(final Entry<String, Double> record : expectedReturns.entrySet())
         result += record.getValue();
      return result;
   }
}