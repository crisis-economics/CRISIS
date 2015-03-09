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

import java.util.HashSet;

import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * @author phillips
  */
abstract class NetworkMarket {
   
   protected final static class Participant {
      private Object object;
      private String uniqueID;
      private MarketResponseFunction responseFunction;
      
      Participant(
         final Object object,
         final String uniqueID,
         final MarketResponseFunction responseFunction
         ) {
         this.object = object;
         this.uniqueID = uniqueID;
         this.responseFunction = responseFunction;
      }
      
      Object getObject() {
         return object;
      }
      
      String getUniqueID() {
         return uniqueID;
      }
      
      MarketResponseFunction getResponseFunction() {
         return responseFunction;
      }
      
      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((uniqueID == null) ? 0 : uniqueID.hashCode());
         return result;
      }
      
      @Override
      public boolean equals(Object obj) {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         Participant other = (Participant) obj;
         if (uniqueID == null) {
            if (other.uniqueID != null)
               return false;
         } else if (!uniqueID.equals(other.uniqueID))
            return false;
         return true;
      }
   }
   
   private HashSet<NetworkMarket.Participant>
      buyerParticipants,
      sellerParticipants;
   
   protected NetworkMarket() {
      this.buyerParticipants = new HashSet<NetworkMarket.Participant>();
      this.sellerParticipants = new HashSet<NetworkMarket.Participant>();
   }
   
   public final void schedule(
      final NamedEventOrderings clearingEventOrder,
      final ScheduleIntervals intervalBetweenClearances
      ) {
      Simulation.repeat(this, "matchAllOrders", clearingEventOrder, intervalBetweenClearances);
   }
   
   public final void matchAllOrders(
       final int maxIterationsPerEdge,
       final int maxIterationsOverNetwork
       ) {
      matchAllOrders(
          maxIterationsPerEdge,
          maxIterationsOverNetwork,
          new TargetResidualOrMaximumIterationsStoppingCondition(1.e-4, maxIterationsOverNetwork)
          );
   }
   
   public final void matchAllOrders(
      final int maxIterationsPerEdge,
      final int maxIterationsOverNetwork,
      final AbstractIterativeClearingAlgorithmStoppingCondition stoppingCondition
      ) {
      MixedClearingNetwork network = buildNetwork();
      processNetwork(network, maxIterationsPerEdge, maxIterationsOverNetwork, stoppingCondition);
      network.createContracts();
      clearParticipants();
   }
   
   protected abstract MixedClearingNetwork buildNetwork();
   
   protected final MixedClearingNetwork.Builder
      createNetworkBuilderWithVertices() {
      MixedClearingNetwork.Builder builder = new MixedClearingNetwork.Builder();
      for(NetworkMarket.Participant buyer : getBuyerParticipants()) {
         builder.addNetworkNode(
            buyer.getObject(),
            buyer.getResponseFunction(),
            buyer.getUniqueID()
            );
      }
      for(NetworkMarket.Participant seller : getSellerParticipants()) {
         builder.addNetworkNode(
            seller.getObject(),
            seller.getResponseFunction(),
            seller.getUniqueID()
            );
      }
      return builder;
   }
   
   private final void processNetwork(
      final MixedClearingNetwork network,
      final int maxIterationsPerEdge,
      final int maxNetworkIterations,
      final AbstractIterativeClearingAlgorithmStoppingCondition stoppingCondition
      ) {
      MixedClearingNetworkAlgorithm clearingAlgorithm = 
         new AscentMarchHeterogeneousClearingAlgorithm(
            maxIterationsPerEdge, 1.e-10, stoppingCondition);
      network.applyClearingAlgorithm(clearingAlgorithm);
   }
   
   private final void clearParticipants() {
      this.buyerParticipants.clear();
      this.sellerParticipants.clear();
   }
   
   protected final HashSet<NetworkMarket.Participant> getBuyerParticipants() {
      return buyerParticipants;
   }
   
   protected final HashSet<NetworkMarket.Participant> getSellerParticipants() {
      return sellerParticipants;
   }
}
