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
import java.util.BitSet;
import java.util.List;

import org.apache.commons.math3.exception.NullArgumentException;

/**
  * A node for a heterogenous clearing network. This class
  * computes, and stores, the response of the node to the
  * connections (edges) between itself and neighbording nodes.
  * For nodes on the supply side, edges connect to a subset
  * of nodes on the demand side. Conversely for demand nodes.
  */
class MixedClearingNetworkNode implements Node {
   private Object representedObject;
   private MarketResponseFunction responseFunction;
   private String uniqueID;
   
   private List<SingletonEdge> edges;
   private List<Double> edgeResponses;
   
   protected MixedClearingNetworkNode(
      final Object representedObject,
      final MarketResponseFunction responseFunction,
      final String uniqueID
      ) {
      if(representedObject == null ||
         responseFunction == null ||
         uniqueID == null)
         throw new NullArgumentException();
      if(uniqueID.isEmpty())
         throw new IllegalArgumentException(
            "HeterogeneousClearingNode: specified ID is an emptry string.");
      this.representedObject = representedObject;
      this.responseFunction = responseFunction;
      this.edges = new ArrayList<SingletonEdge>();
      this.edgeResponses = new ArrayList<Double>();
      this.uniqueID = uniqueID;
   }
   
   // Collect connected edge rates into an array.
   private MarketResponseFunction.TradeOpportunity[] collectEdgeRates() {
      final MarketResponseFunction.TradeOpportunity[] result =
         new MarketResponseFunction.TradeOpportunity[edges.size()];
      if(edgeIndicesForCompleteResponseUpdate == null) {
         edgeIndicesForCompleteResponseUpdate = new int[edges.size()];
         for(int i = 0; i< edges.size(); ++i)
            edgeIndicesForCompleteResponseUpdate[i] = i;
      }
      for(int i = 0; i< result.length; ++i) {
         final SingletonEdge edge = edges.get(i);
         result[i] = MarketResponseFunction.TradeOpportunity.create(
            getEdgeRate(edge),
            edge.getResource(),
            getPartyOverConnection(edge)
            );
      }
      return result;
   }
   
   @Override
   public int getConnectionIndexOfEdge(final SingletonEdge edge) {
      return edge.getConnectionIndex(this);
   }
   
   @Override
   public String getPartyOverConnection(final SingletonEdge edge) {
      return edge.getUniquePartyOverConnection(this);
   }
   
   @Override
   public final double getEdgeRate(SingletonEdge edge) {
      return edge.getEdgeRate();
   }
   
   /**
     * Compute, and commit, the response of the node to the
     * edge with the given connection index.
     */
   private void updateResponseForEdges(
      final int[] edgeIndicesToRecalculate,
      final MarketResponseFunction.TradeOpportunity[] edgeRates
      ) {
      if(edgeIndicesToRecalculate.length == 0) return;
      double[] result = responseFunction.getValue(edgeIndicesToRecalculate, edgeRates);
      for(int i = 0; i< edgeIndicesToRecalculate.length; ++i)
         edgeResponses.set(
            edgeIndicesToRecalculate[i],
            result[i]
            );
   }
   
   private MarketResponseFunction.TradeOpportunity[] knownEdgeRates;
   private int[] edgeIndicesForCompleteResponseUpdate;
   private BitSet edgeRateIsUpdated;
   
   /**
     * Update (recalculate) the response of this node to a 
     * given connecting edge.
     */
   @Override
   public final void flagEdgeResponseForUpdate(final SingletonEdge edge) {
      final int connectionIndex = getConnectionIndexOfEdge(edge);
      if(knownEdgeRates == null) {
         knownEdgeRates = collectEdgeRates();
         edgeRateIsUpdated = new BitSet(knownEdgeRates.length);
         edgeRateIsUpdated.flip(0, knownEdgeRates.length);
      }
      else {
         knownEdgeRates[connectionIndex].setRate(getEdgeRate(edge));
         edgeRateIsUpdated.set(connectionIndex);
      }
   }
   
   /**
     * Recompute responses for all flagged connections.
     */
   @Override
   public final void recomputePendingEdgeResponses() {
      int[] edgesToUpdate = new int[edgeRateIsUpdated.cardinality()];
      int counter = 0;
      for(int nextConnection = edgeRateIsUpdated.nextSetBit(0);
          nextConnection >= 0; 
          nextConnection = edgeRateIsUpdated.nextSetBit(nextConnection+1)) {
         edgesToUpdate[counter++] = nextConnection;
         nextConnection = edgeRateIsUpdated.nextSetBit(nextConnection);
      }
      updateResponseForEdges(edgesToUpdate, knownEdgeRates);
      edgeRateIsUpdated.clear();
   }
   
   /**
     * Update (recalculate) the response of this node to all
     * connecting edges.
     */
   @Override
   public final void updateAllEdgeResponses() {
      MarketResponseFunction.TradeOpportunity[] edgeRates = collectEdgeRates();
      updateResponseForEdges(edgeIndicesForCompleteResponseUpdate, edgeRates);
   }
   
   /**
     * Get the (stored) response of the node to the given edge.
     */
   @Override
   public final Double getResponseToEdge(SingletonEdge edge) {
      return edgeResponses.get(getConnectionIndexOfEdge(edge));
   }
   
   /** Latch an edge to this node. */
   @Override
   public final int connectEdge(SingletonEdge edge) {
      if(edge == null)
         throw new NullArgumentException();
      edges.add(edge);
      edgeResponses.add(0.);
      return (edges.size() - 1);
   }
   
   /**
     * Get the object this node represents. This method cannot
     * return null.
     */
   @Override
   public final Object getObject() {
      return representedObject;
   }
   
   /**
     * Get the response function of this node.
     */
   @Override
   public final MarketResponseFunction getResponseFunction() {
      return responseFunction;
   }
   
   /**
     * Get the unique ID of this node.
     **/
   @Override
   public final String getUniqueID() {
      return uniqueID;
   }
}
