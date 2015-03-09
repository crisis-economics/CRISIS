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

import ec.util.MersenneTwisterFast;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * @author phillips
  */
final class ShotgunSeedObjectivesSelectionAlgorithm
   implements SeedObjectivesSelectionAlgorithm {
   
   ShotgunSeedObjectivesSelectionAlgorithm() { } // Stateless
   
   @Override
   public List<Double> findSeedRates(final MixedClearingNetwork network) {
      final List<Double> rateSeeds = new ArrayList<Double>();
      final List<MixedClearingNetworkEdge> networkEdges = network.getEdges();
      MersenneTwisterFast dice = Simulation.getRunningModel().random;
      for(int i = 0; i< network.getNumberOfEdges(); ++i) {
         final MixedClearingNetworkEdge edge = networkEdges.get(i);
         final double
            maximumAdmissibleRate = edge.getMaximumRateAdmissibleByBothParties(),
            seed = (maximumAdmissibleRate / 2.) * ((dice.nextDouble() - .5) * .2 + 1.);
         edge.setEdgeRate(seed);
         rateSeeds.add(seed);
      }
      return rateSeeds;
   }
}
