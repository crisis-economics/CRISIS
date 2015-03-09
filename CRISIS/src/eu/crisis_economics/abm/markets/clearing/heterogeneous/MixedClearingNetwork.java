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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.utilities.Pair;

final class MixedClearingNetwork {
   private final Map<String, Node>
      networkNodes;
   private final List<MixedClearingNetworkEdge>
      networkEdges;
   private final Map<String, HyperEdge>
      hyperEdgesByName;
   
   protected MixedClearingNetwork() {
      this.networkNodes = new HashMap<String, Node>();
      this.networkEdges = new ArrayList<MixedClearingNetworkEdge>();
      this.hyperEdgesByName = new HashMap<String, HyperEdge>();
   }
   
   static final class Builder {
      private MixedClearingNetwork network;
      
      Builder() {
         this.network = new MixedClearingNetwork();
      }
      
      /*
       * Add a new node to the network.
       */
      void addNetworkNode(
         final Object representedObject,
         final MarketResponseFunction responseFunction,
         final String uniqueID
         ) {
         Node node = new MixedClearingNetworkNode(
            representedObject, responseFunction, uniqueID);
         network.networkNodes.put(uniqueID, node);
      }
      
      void addEdge(
         final String demandNodeID,
         final String supplyNodeID,
         final ResourceExchangeDelegate resourceExchangeDelegate,
         final ClearingInstrument resource
         ) {
         if(demandNodeID == null ||
            supplyNodeID == null ||
            resourceExchangeDelegate == null)
            throw new NullArgumentException();
         Pair<Node, Node> nodes = 
            tryGetDemandSupplyNodePair(demandNodeID, supplyNodeID);
         MixedClearingNetworkEdge newEdge = new SingletonEdge(
            nodes.getFirst(),
            nodes.getSecond(),
            resourceExchangeDelegate,
            resource
            );
         network.networkEdges.add(newEdge);
      }
      
      void addHyperEdge(final String hyperEdgeName) {
         if(hyperEdgeName == null)
            throw new NullArgumentException();
         if(hyperEdgeName == "")
            throw new IllegalArgumentException(
               "HeterogeneousClearingNetwork.Builder.addHyperEdge: argument is empty.");
         HyperEdge hyperedge = network.hyperEdgesByName.get(hyperEdgeName);
         if(hyperedge != null)
            throw new IllegalStateException(
               "HeterogeneousClearingNetwork.Builder.addHyperEdge: a hyperedge with " +
               "the identifier " + hyperEdgeName + " already exists.");
         hyperedge = new HyperEdge();
         network.networkEdges.add(hyperedge);
         network.hyperEdgesByName.put(hyperEdgeName, hyperedge);
      }
      
      void addToHyperEdge(
         final String demandNodeID,
         final String supplyNodeID,
         final ResourceExchangeDelegate resourceExchangeDelegate,
         final String hyperedgeName,
         final ClearingInstrument resource
         ) {
         if(demandNodeID == null ||
            supplyNodeID == null ||
            resourceExchangeDelegate == null ||
            resource == null)
            throw new NullArgumentException();
         Pair<Node, Node> nodes = 
            tryGetDemandSupplyNodePair(demandNodeID, supplyNodeID);
         HyperEdge existingHyperedge = network.hyperEdgesByName.get(hyperedgeName);
         if(existingHyperedge == null)
            throw new IllegalStateException(
               "HeterogeneousClearingNetwork.Builder.addToHyperEdge: network contains " +
               "no hyperedge with name " + hyperedgeName);
         SingletonEdge newEdge = new SingletonEdge(
            nodes.getFirst(),
            nodes.getSecond(),
            resourceExchangeDelegate,
            resource
            );
         existingHyperedge.addEdge(newEdge);
      }
      
      private Pair<Node, Node> tryGetDemandSupplyNodePair(
         final String demandNodeID,
         final String supplyNodeID
         ) {
         final Node
            demandSideNode = network.networkNodes.get(demandNodeID),
            supplySideNode = network.networkNodes.get(supplyNodeID);
         if(demandSideNode == null )
            throw new IllegalArgumentException(
               "HeterogeneousClearingNetwork.Builder.tryGetDemandSupplyNodePair: " + 
               "no demand node with ID " + demandNodeID + " exists.");
         if(supplySideNode == null)
            throw new IllegalArgumentException(
               "HeterogeneousClearingNetwork.Builder.tryGetDemandSupplyNodePair: " + 
               "no supply node with ID " + supplyNodeID + " exists.");
         return new Pair<Node, Node>(demandSideNode, supplySideNode);
      }
      
      MixedClearingNetwork build() {
         return network;
      }
   }
   
   /*
    * Get the number of edges in this network. Each edge corresponds to
    * one optimisation variable in heterogeneous clearing, so the number
    * of network edges is equal to the dimension of the optimisation
    * problem.
    */
   int getNumberOfEdges() {
      return networkEdges.size();
   }
   
   /*
    * Get the number of demand nodes in the network. 
    */
   int getNumberOfNetworkNodes() {
      return networkNodes.size();
   }
   
   void updateAllVertexResponses() {
      for(Entry<String, Node> record : networkNodes.entrySet())
         record.getValue().updateAllEdgeResponses();
   }
   
   /**
     * Apply a clearing algorithm to the network. The clearing algorithm
     * will modify the state of edge rates in the network in an attempt
     * to match supply and demand for interacting network parties.
     */
   public double applyClearingAlgorithm(
      final MixedClearingNetworkAlgorithm clearingAlgorithm) {
      if(clearingAlgorithm == null)
         throw new NullArgumentException();
      final double residualCost = clearingAlgorithm.applyToNetwork(this);
      return residualCost;
   }
   
   /**
     * Get the residual cost of the network. If this method returns zero,
     * then the heterogeneous network has been cleared.
     */
   public final double getResidualCost() {
      double summand = 0.;
      for(MixedClearingNetworkEdge edge : networkEdges)
         summand += edge.getSquareCost();
      return summand / networkEdges.size();
   }
   
   /**
     * Create contracts for every supply-demand edge in the network.
     */
   public final void createContracts() {
      for(MixedClearingNetworkEdge edge : networkEdges)
         edge.invokeContractCreationDelgate();
   }
   
   /*
    * Get a list of supply-demand interaction edges for this network.
    */
   List<MixedClearingNetworkEdge> getEdges() {
      return networkEdges;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return
         "Heterogeneous clearing network, edges: " + getNumberOfEdges() + ", " +
         "number of nodes: " + getNumberOfNetworkNodes() + ".";
   }
}
