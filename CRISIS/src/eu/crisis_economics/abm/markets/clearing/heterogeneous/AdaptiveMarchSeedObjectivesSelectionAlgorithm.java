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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.ArrayList;
import java.util.List;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.
       MarchingHeterogeneousClearingAlgorithm.MeritFunction;

/**
  * A seed objective (edge rate) selection algorithm for an ascent march 
  * heterogeneous clearing algorihtm.
  * 
  * This algorithm will attempt to find small nonzero seed rates that
  * satisfy the preconditions of an ascending marching heterogeneous clearing
  * algorithm. If a small nonzero set of seed rates cannot be identified, 
  * the algorithm will instead test the vector \vec{0} as a valid seed.
  * 
  * @author phillips
  */
final class AdaptiveMarchSeedObjectivesSelectionAlgorithm
   implements SeedObjectivesSelectionAlgorithm {
   
   AdaptiveMarchSeedObjectivesSelectionAlgorithm() { } // Stateless
   
   @Override
   public List<Double> findSeedRates(final MixedClearingNetwork network) {
      final List<Double> rateSeeds = new ArrayList<Double>();
      for(final MixedClearingNetworkEdge edge : network.getEdges()) {
         final double testRate = edge.getMaximumRateAdmissibleByBothParties();
         final MeritFunction costFunction = new MeritFunction(edge);
         final double
            costAtMinimumRate = costFunction.functionValue(0.),
            costAtMaximumRate = costFunction.functionValue(testRate);
         final boolean
            edgeHasDescendingCost = (costAtMaximumRate <= costAtMinimumRate ? true : false);
         if(edgeHasDescendingCost)
            rateSeeds.add(0.);
         else
            rateSeeds.add(edge.getMaximumRateAdmissibleByBothParties());
      }
      return rateSeeds;
   }
}
