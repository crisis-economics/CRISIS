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
  * Create a complete (all-to-all) network market, trading a 
  * a single resource in pure heterogeneous mode.
  * 
  * @author phillips
  */
public final class PureHomogeneousNetworkMarket
extends CompleteNetworkMarket {
   
   public PureHomogeneousNetworkMarket(
      final ClearingInstrument resource,
      final ResourceExchangeDelegate resourceCreationDelegate
      ) {
      super(resource, resourceCreationDelegate);
   }
   
   @Override
   protected MixedClearingNetwork buildNetwork() {
      MixedClearingNetwork.Builder builder = 
         super.createNetworkBuilderWithVertices();
      builder.addHyperEdge(super.getTradeResource().getUUID());
      for(NetworkMarket.Participant buyer : getBuyerParticipants()) {
         for(NetworkMarket.Participant seller : getSellerParticipants()) {
            builder.addToHyperEdge(
               buyer.getUniqueID(), seller.getUniqueID(),
               getResourceExchangeDelegate(),
               super.getTradeResource().getUUID(),
               super.getTradeResource()
               );
         }
      }
      return builder.build();
   }
}
