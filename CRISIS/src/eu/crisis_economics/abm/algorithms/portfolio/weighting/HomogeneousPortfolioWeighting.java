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

import com.google.inject.Inject;

/**
  * A simple implementation of the {@link PortfolioWeighting} interface. This
  * implementation specifies a trivial portfolio weighting in which all known
  * instruments receive the same investment weight.
  * 
  * @author phillips
  */
public final class HomogeneousPortfolioWeighting extends AbstractPortfolioWeighting {
   
   /**
     * Create a {@link HomogeneousPortfolioWeighting} object.
     */
   @Inject
   public HomogeneousPortfolioWeighting() { }   // Statelesss
   
   public void computeWeights() {
   }
   
   @Override
   public double getWeight(final String reference) {
      if(super.getReturns().containsKey(reference))
         return 1. / super.size();
      else 
         return 0.;
   }

   @Override
   public Map<String, Double> getWeights() {
      Map<String, Double>
         result = new HashMap<String, Double>();
      for(final Entry<String, Double> record : super.getReturns().entrySet())
         result.put(record.getKey(), 1./super.size());
      return result;
   }
}