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

import java.util.List;

/**
  * @author phillips
  */
interface SeedObjectivesSelectionAlgorithm {
   /**
     * Identify seed (starting) objectives (edge rates) for
     * a MCN clearing algorithm. This method returns a list
     * of seed rates for each edge. The order of records in
     * the return value is the same as the order of edges
     * appearing in the argument.
     * 
     * This method is not guaranteed to identify a valid set
     * of seed rates. In this event, this method returns null.
     */
   public List<Double> findSeedRates(MixedClearingNetwork network);
}
