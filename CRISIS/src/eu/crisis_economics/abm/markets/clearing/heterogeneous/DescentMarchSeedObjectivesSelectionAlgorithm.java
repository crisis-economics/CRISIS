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
  * A seed objective (edge rate) selection algorithm for a descent march 
  * heterogeneous clearing algorihtm.
  * 
  * This algorithm will attempt to find seed rates that satisfy the
  * preconditions of a descending marching heterogeneous clearing
  * algorithm. Initially, seed rates very close to the maximum
  * admissible rate of each edge will be tested. If this selection 
  * is invalid, another set of edge rates, increasingly distant from 
  * the maximum admissible rate, will be tested.
  * 
  * @author phillips
  */
final class DescentMarchSeedObjectivesSelectionAlgorithm
   implements SeedObjectivesSelectionAlgorithm {
   
   DescentMarchSeedObjectivesSelectionAlgorithm() { } // Stateless
   
   @Override
   public List<Double> findSeedRates(final MixedClearingNetwork network) {
      final List<Double> rateSeeds = new ArrayList<Double>();
      final List<MixedClearingNetworkEdge> networkEdges = network.getEdges();
      for(int i = 0; i< network.getNumberOfEdges(); ++i) {
         MixedClearingNetworkEdge edge = networkEdges.get(i);
         final double
            maximumDemandDomain = edge.getMaximumRateInDemandDomain(),
            maximumSupplyDomain = edge.getMaximumRateInSupplyDomain();
         final double
            preSeedRate = (1.-1.e-11) * Math.min(maximumDemandDomain, maximumSupplyDomain);
         edge.setEdgeRate(preSeedRate);
      }
      for(int i = 0; i< network.getNumberOfEdges(); ++i) {
         MixedClearingNetworkEdge edge = networkEdges.get(i);
         final double
            maximumDemandDomain = edge.getMaximumRateInDemandDomain(),
            maximumSupplyDomain = edge.getMaximumRateInSupplyDomain();
         MeritFunction meritFunction = new MeritFunction(edge);
         final double
            maximumDomain = Math.min(maximumDemandDomain, maximumSupplyDomain);
         boolean foundSeed = false;
         for(int j = 22; j>= 1; --j) {
            double candidateSeed = maximumDomain * (1. - Math.pow(10, -j/2.));
            if(meritFunction.functionValue(candidateSeed) < 0.) {
               rateSeeds.add(candidateSeed);
               foundSeed = true;
               break;
            }
         }
         if(!foundSeed) return null;
      }
      return rateSeeds;
   }
}
