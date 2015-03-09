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
final class AscentMarchSeedObjectivesSelectionAlgorithm
   implements SeedObjectivesSelectionAlgorithm {
   
   AscentMarchSeedObjectivesSelectionAlgorithm() { } // Stateless
   
   @Override
   public List<Double> findSeedRates(final MixedClearingNetwork network) {
      final List<Double> rateSeeds = new ArrayList<Double>();
      final List<MixedClearingNetworkEdge> networkEdges = network.getEdges();
//      try { 
//         /*
//          * Try with rate seeds == edge.getMaximumRateAdmissibleByBothParties() / 1.e7.
//          * Small, nonzero seed rates are preferable if such rates can be identified.
//          */
//         for(int i = 0; i< network.getNumberOfEdges(); ++i) {
//            final MixedClearingNetworkEdge edge = networkEdges.get(i);
//            final double testRate = edge.getMaximumRateAdmissibleByBothParties() / 1.e7;
//            edge.setEdgeRate(testRate);
//            rateSeeds.add(testRate);
//         }
//         for(int i = 0; i< network.getNumberOfEdges(); ++i) {
//            MixedClearingNetworkEdge edge = networkEdges.get(i);
//            MeritFunction meritFunction = new MeritFunction(edge);
//            double value = meritFunction.functionValue(rateSeeds.get(i));
//            if(value < 0.) throw new IllegalStateException();
//         }
//      } catch(final IllegalStateException firstAttemptFailed) {
//         /*
//          * Try again with seed rates equal to zero.
//          */
//         rateSeeds.clear();
         for(MixedClearingNetworkEdge edge : networkEdges) {
            edge.setEdgeRate(0.);
            edge.flagNodesResponsesForUpdate();
            rateSeeds.add(0.);
         }
         for(int i = 0; i< network.getNumberOfEdges(); ++i) {
            MixedClearingNetworkEdge edge = networkEdges.get(i);
            MeritFunction meritFunction = new MeritFunction(edge);
            double value = meritFunction.functionValue(rateSeeds.get(i));
            if(value < 0.) {
               return null;
            }
         }
//      }
      return rateSeeds;
   }
}
