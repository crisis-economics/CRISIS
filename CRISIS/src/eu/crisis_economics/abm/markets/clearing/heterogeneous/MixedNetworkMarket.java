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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.utilities.Pair;

/** Create a complete (all-to-all) network market, trading
  * multiple differentiated resources. Each individual resource
  * can be traded in heterogeneous mode or in homogeneous mode.
  * 
  * @author phillips
  */
final class MixedNetworkMarket extends NetworkMarket {
   
   enum SubnetworkClearingMode {
      HETEROGENEOUS {
         @Override
         void setup(
            final String resourceName,
            final MixedClearingNetwork.Builder builder
            ) { }
      },
      HOMOGENEOUS {
         @Override
         void setup(
            final String resourceName,
            final MixedClearingNetwork.Builder builder
            ) {
            builder.addHyperEdge(resourceName);
         }
      };
      
      abstract void setup(
         final String resourceName,
         final MixedClearingNetwork.Builder builder
         );
   }
   
   private Map<
      ClearingInstrument,
      Pair<ResourceExchangeDelegate, SubnetworkClearingMode>
      > subnetworks;
   
   /**
     * Create a mixed network market.
     */
   public MixedNetworkMarket(
      final Map<
         ClearingInstrument, 
         Pair<ResourceExchangeDelegate, SubnetworkClearingMode>
         > subnetworks
      ) {
      if(subnetworks == null)
         throw new NullArgumentException();
      if(subnetworks.isEmpty())
         throw new IllegalArgumentException(
            "MixedNetworkMarket: no subnetworks were specified.");
      this.subnetworks = subnetworks;
   }
   
   private void addHeterogeneousEdges(
      final MixedClearingNetwork.Builder builder,
      final ClearingInstrument resource,
      final ResourceExchangeDelegate resourceExchangeDelegate
      ) {
      for(NetworkMarket.Participant consumer : super.getBuyerParticipants()) {
         for(NetworkMarket.Participant supplier : super.getSellerParticipants()) {
            builder.addEdge(
               consumer.getUniqueID(), supplier.getUniqueID(), resourceExchangeDelegate, resource);
         }
      }
   }
   
   private void addHomogeneousEdges(
      final MixedClearingNetwork.Builder builder,
      final ClearingInstrument resource,
      final ResourceExchangeDelegate resourceExchangeDelegate
      ) {
      builder.addHyperEdge(resource.getUUID());
      for(NetworkMarket.Participant consumer : super.getBuyerParticipants()) {
         for(NetworkMarket.Participant supplier : super.getSellerParticipants()) {
            builder.addToHyperEdge(
               consumer.getUniqueID(), supplier.getUniqueID(),
               resourceExchangeDelegate, resource.getUUID(),
               resource);
         }
      }
   }
   
   @Override
   protected MixedClearingNetwork buildNetwork() {
      MixedClearingNetwork.Builder builder = 
         super.createNetworkBuilderWithVertices();
      for(Entry<
            ClearingInstrument,
            Pair<ResourceExchangeDelegate, SubnetworkClearingMode>
            > record : subnetworks.entrySet() ) {
         final Pair<ResourceExchangeDelegate, SubnetworkClearingMode> subnetworkType =
            record.getValue();
         final ClearingInstrument resource = record.getKey();
         if(subnetworkType.getSecond() == SubnetworkClearingMode.HETEROGENEOUS)
            addHeterogeneousEdges(builder, resource, subnetworkType.getFirst());
         else
            addHomogeneousEdges(builder, resource, subnetworkType.getFirst());
      }
      return builder.build();
   }
   
   /**
     * Add a buy order, as specified by a response function, to the network.
     */
   public final void addBuyOrder(
      final Object participant,
      final String uniqueBuyerID,
      final MarketResponseFunction responseFunction
      ) {
      if(responseFunction == null)
         throw new NullArgumentException();
      MixedNetworkMarket.Participant registeredParticipant = 
         new MixedNetworkMarket.Participant(
            participant, uniqueBuyerID, responseFunction);
      super.getBuyerParticipants().add(registeredParticipant);
   }
   
   /**
     * Add a sell order, as specified by a response function, to the network.
     */
   public final void addSellOrder(
      final Object participant,
      final String uniqueSellerID,
      final MarketResponseFunction responseFunction
      ) {
      if(responseFunction == null)
         throw new NullArgumentException();
      MixedNetworkMarket.Participant registeredParticipant = 
         new MixedNetworkMarket.Participant(
            participant, uniqueSellerID, responseFunction);
      super.getSellerParticipants().add(registeredParticipant);
   }
   
   /**
     * Get the number of resources processed by this network.
     */
   public int getNumberOfResources() {
      return subnetworks.size();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "MixedNetworkMarket with " + getNumberOfResources() + " resources.";
   }
}
