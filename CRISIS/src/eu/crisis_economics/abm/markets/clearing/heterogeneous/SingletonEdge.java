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
import java.util.List;

import com.google.common.collect.ImmutableList;

import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
final class SingletonEdge extends MixedClearingNetworkEdge {
   private final Node
      demandSideNode;
   private final Node
      supplySideNode;
   private final ResourceExchangeDelegate
      contractCreationDelegate;
   
   private final int
      demandSideConnectionIndex,
      supplySideConnectionIndex;
   
   private final ClearingInstrument
      resource;
   
   SingletonEdge(
      final Node demandSideNode,
      final Node supplySideNode,
      final ResourceExchangeDelegate contractCreationDelegate,
      final ClearingInstrument resource
      ) {
      this.demandSideNode = demandSideNode;
      this.supplySideNode = supplySideNode;
      this.contractCreationDelegate = contractCreationDelegate;
      this.demandSideConnectionIndex = demandSideNode.connectEdge(this);
      this.supplySideConnectionIndex = supplySideNode.connectEdge(this);
      this.resource = resource;
   }
   
   @Override
   public void flagNodesResponsesForUpdate() {
      demandSideNode.flagEdgeResponseForUpdate(this);
      supplySideNode.flagEdgeResponseForUpdate(this);
   }
   
   @Override void updateNodeResponses() {
      demandSideNode.recomputePendingEdgeResponses();
      supplySideNode.recomputePendingEdgeResponses();
   }
   
   @Override
   public double getCost() {
      final double
         supply = supplySideNode.getResponseToEdge(this),
         demand = demandSideNode.getResponseToEdge(this);
      return (supply + demand);
   }
   
   /**
    * Get the index of this edge among all connections to the
    * node on the demand side.
    */
   int getConnectionIndex(final Node node) {
      if(demandSideNode == node)
         return demandSideConnectionIndex;
      else if(supplySideNode == node)
         return supplySideConnectionIndex;
      else throw new IllegalStateException();
   }
   
   @Override
   double getDemandResponse() {
      return demandSideNode.getResponseToEdge(this);
   }
  
   @Override
   double getSupplyResponse() {
      return supplySideNode.getResponseToEdge(this);
   }
   
   @Override
   double getMaximumRateInSupplyDomain() {
      return supplySideNode.getResponseFunction().getMaximumInDomain();
   }
   
   @Override
   double getMaximumRateInDemandDomain() {
      return demandSideNode.getResponseFunction().getMaximumInDomain();
   }
   
   @Override
   public Iterator<Pair<String, String>> iterator() {
      List<Pair<String, String>> list = ImmutableList.of(
         new Pair<String, String>(demandSideNode.getUniqueID(), supplySideNode.getUniqueID()));
      return list.iterator();
   }
   
   @Override
   void invokeContractCreationDelgate() {
      MixedClearingNetworkResult result = new MixedClearingNetworkResult(
         demandSideNode.getObject(), 
         demandSideNode.getUniqueID(), 
         supplySideNode.getObject(),
         supplySideNode.getUniqueID(),
         super.getEdgeRate(),
         demandSideNode.getResponseToEdge(this),
         supplySideNode.getResponseToEdge(this)
         );
      this.contractCreationDelegate.commit(result);
   }
   
   ClearingInstrument getResource() {
      return resource;
   }
   
   String getUniquePartyOverConnection(final Node node) {
      if(node == demandSideNode)
         return getSupplySideUniquePartyName();
      else if(node == supplySideNode)
         return getDemandSideUniquePartyName();
      else throw new IllegalArgumentException();
   }
   
   /**
     * Get the unique name of the node on the demand side.
     */
   private String getDemandSideUniquePartyName() {
      return demandSideNode.getUniqueID();
   }
   
   /**
     * Get the unique name of the node on the supply side.
     */
   private String getSupplySideUniquePartyName() {
      return supplySideNode.getUniqueID();
   }
   
   @Override
   boolean touchesEdge(final MixedClearingNetworkEdge other) {
      for(Pair<String, String> links : other) {
         if(links.getFirst().equals(getSupplySideUniquePartyName()) ||
            links.getFirst().equals(getDemandSideUniquePartyName()) ||
            links.getSecond().equals(getSupplySideUniquePartyName()) ||
            links.getSecond().equals(getDemandSideUniquePartyName()))
            return true;
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
         "SingletonEdge clearing edge, rate: " + super.getEdgeRate() + 
         ", connection indices: demand: " + demandSideConnectionIndex + 
         ", supply: " + supplySideConnectionIndex + ".";
   }
}
