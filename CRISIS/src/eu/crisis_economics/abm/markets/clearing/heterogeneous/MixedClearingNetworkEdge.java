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

import java.util.Iterator;

import eu.crisis_economics.utilities.Pair;

abstract class MixedClearingNetworkEdge
   implements Iterable<Pair<String, String>> {
   
   private double
      edgeRate;
   
   protected MixedClearingNetworkEdge() { }
   
   /**
     * Update responses by Nodes belonging to this edge.
     */
   abstract void flagNodesResponsesForUpdate();
   
   abstract void updateNodeResponses();
   
   /**
     * Get the value of the optimization objective for this edge.
     */
   public double getEdgeRate() { 
      return edgeRate;
   }
   
   /**
    * Set the rate attached to this node.
    */
   public void setEdgeRate(final double rate) {
      this.edgeRate = rate;
   }
   
   /**
     * Get the square residual cost, (demand responde - supply
     * response)^2, for the nodes attached to this edge.
     */
   final double getSquareCost() {
      return Math.pow(getCost(), 2.);
   }
   
   /**
    * Get the square residual cost, (demand responde - supply
    * response), for the nodes attached to this edge.
    */
  abstract double getCost();
   
   /**
     * Get the edge demand response.
     */
   abstract double getDemandResponse();
   
   /**
     * Get the edge supply response.
     */
   abstract double getSupplyResponse();
   
   /**
     * Get the maximum admissible edge rate according to the supply domain.
     */
   abstract double getMaximumRateInSupplyDomain();
   
   /**
     * Get the maximum admissible edge rate according to the demand domain.
     */
   abstract double getMaximumRateInDemandDomain();
   
   /**
     * Get the smallest edge rate admissible all participants.
     */
   final double getMaximumRateAdmissibleByBothParties() {
      return Math.min(getMaximumRateInSupplyDomain(), getMaximumRateInDemandDomain());
   }
   
   /**
     * Invoke a contract creation delegate for this edge.
     */
   abstract void invokeContractCreationDelgate();
   
   abstract boolean touchesEdge(final MixedClearingNetworkEdge other);
   
   @Override
   public abstract Iterator<Pair<String, String>> iterator();
}
