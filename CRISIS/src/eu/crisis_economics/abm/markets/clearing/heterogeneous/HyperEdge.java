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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
public final class HyperEdge extends MixedClearingNetworkEdge {
   private List<SingletonEdge> singletonEdges;
   
   HyperEdge() {
      singletonEdges = new ArrayList<SingletonEdge>();
   }
   
   void addEdge(final SingletonEdge edge) {
      singletonEdges.add(edge);
   }
   
   @Override
   public void setEdgeRate(double rate) {
      for(SingletonEdge edge : singletonEdges)
         edge.setEdgeRate(rate);
      super.setEdgeRate(rate);
   }
   
   @Override
   void flagNodesResponsesForUpdate() {
      for(SingletonEdge edge : singletonEdges)
         edge.flagNodesResponsesForUpdate();
   }
   
   @Override
   void updateNodeResponses() {
      for(SingletonEdge edge : singletonEdges)
         edge.updateNodeResponses();
   }
   
   @Override
   double getCost() {
      double result = 0.;
      for(SingletonEdge edge : singletonEdges)
         result += edge.getCost();
      return result;
   }
   
   @Override
   double getDemandResponse() {
      double result = 0.;
      for(SingletonEdge edge : singletonEdges)
         result += edge.getDemandResponse();
      return result;
   }
  
   @Override
   double getSupplyResponse() {
      double result = 0.;
      for(SingletonEdge edge : singletonEdges)
         result += edge.getSupplyResponse();
      return result;
   }
   
   @Override
   double getMaximumRateInSupplyDomain() {
      double result = -1.;
      for(SingletonEdge edge : singletonEdges)
         result = Math.max(result, edge.getMaximumRateInSupplyDomain());
      return result;
   }
   
   @Override
   double getMaximumRateInDemandDomain() {
      double result = -1.;
      for(SingletonEdge edge : singletonEdges)
         result = Math.max(result, edge.getMaximumRateInDemandDomain());
      return result;
   }
   
   @Override
   public Iterator<Pair<String, String>> iterator() {
      @SuppressWarnings("unchecked")
      Pair<String, String>[] pairs = new Pair[singletonEdges.size()];
      int counter = 0;
      for(SingletonEdge edge : singletonEdges)
         pairs[counter++] = edge.iterator().next();
      return Arrays.asList(pairs).iterator();
   }
   
   /**
     * Get the components of the hyperedge.
     */
   List<SingletonEdge> getComponents() {
      return Collections.unmodifiableList(singletonEdges);
   }
   
   @Override
   void invokeContractCreationDelgate() {
      for(SingletonEdge edge : singletonEdges)
         edge.invokeContractCreationDelgate();
   }
   
   @Override
   boolean touchesEdge(final MixedClearingNetworkEdge other) {
      for(final Pair<String, String> localLinks : other) {
         for(final Pair<String, String> foreignLinks : this) {
            if(localLinks.getFirst() == foreignLinks.getFirst() ||
               localLinks.getFirst() == foreignLinks.getSecond() ||
               localLinks.getSecond() == foreignLinks.getFirst() ||
               localLinks.getSecond() == foreignLinks.getSecond())
            return true;
         }
      }
      return false;
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return
         "Hyper clearing edge, rate: " + super.getEdgeRate() + 
         ", number of components: " + singletonEdges.size() + ".";
   }
}
