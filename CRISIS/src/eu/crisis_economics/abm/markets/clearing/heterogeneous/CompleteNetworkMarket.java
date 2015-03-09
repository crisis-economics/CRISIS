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

import org.apache.commons.math3.exception.NullArgumentException;

/**
  * @author phillips
  */
abstract class CompleteNetworkMarket extends NetworkMarket {
   
   private final ClearingInstrument
      resource;
   private ResourceExchangeDelegate
      resourceExchangeDelegate;
   
   public CompleteNetworkMarket(
      final ClearingInstrument resource,
      final ResourceExchangeDelegate resourceExchangeDelegate
      ) {
      if(resource == null || resourceExchangeDelegate == null)
         throw new NullArgumentException();
      this.resource = resource;
      this.resourceExchangeDelegate = resourceExchangeDelegate;
   }
   
   protected abstract MixedClearingNetwork buildNetwork();
   
   /**
     * Add a buy order, as specified by a response function, to the network.
     */
   public final void addBuyOrder(
      Object participant,
      String uniqueBuyerID,
      MarketResponseFunction responseFunction
      ) {
      CompleteNetworkMarket.Participant registeredParticipant = 
         new CompleteNetworkMarket.Participant(
            participant, uniqueBuyerID, responseFunction);
      super.getBuyerParticipants().add(registeredParticipant);
   }
   
   /**
     * Add a sell order, as specified by a response function, to the network.
     */
   public final void addSellOrder(
      Object participant,
      String uniqueSellerID,
      MarketResponseFunction responseFunction
      ) {
      CompleteNetworkMarket.Participant registeredParticipant = 
         new CompleteNetworkMarket.Participant(
            participant, uniqueSellerID, responseFunction);
      super.getSellerParticipants().add(registeredParticipant);
   }
   
   /**
     * Get the (unique) resource exchange delegate for this network.
     */
   protected ResourceExchangeDelegate getResourceExchangeDelegate() {
      return resourceExchangeDelegate;
   }
   
   /**
     * Get the underlying trade resource.
     */
   public final ClearingInstrument getTradeResource() {
      return resource;
   }
}
