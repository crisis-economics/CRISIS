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

/**
  * Stopping conditions for mixed clearing algorithms.
  * @author phillips
  */
public interface MixedClearingNetworkAlgorithmStoppingCondition {
   /**
     * This method returns true when, and only when, the state of the
     * mixed clearing network (the argument) satisifies the stopping
     * conditions of a clearing algorithm. Implementations of this
     * interface define the stopping condition.
     * 
     * This method should be called exactly once each time a clearing
     * algorithm iterates over a mixed clearing network.
     * 
     * Examples of stopping conditions include, but are not limited
     * to:
     *   (a) a fixed number of iterations have passed,
     *   (b) the network residual is below a known value,
     *   (c) the network residual has decreased by a known amount or
     *       fraction,
     *   (d) the network redidual has not changed in a set number of 
     *       iterations.
     * @param network
     *        The mixed clearing network to assess.
     */
   public boolean isStoppingConditionSatisfied(MixedClearingNetwork network);
}
