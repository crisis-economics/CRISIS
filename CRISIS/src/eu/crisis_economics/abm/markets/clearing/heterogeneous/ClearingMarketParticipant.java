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

import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;

public interface ClearingMarketParticipant extends UniquelyIdentifiable {
   /**
    * Get a Consumer Response Function for this party. The 
    * response function is a multivalued multivariate function
    * of the market clearing objectives, specifying the
    * demand of the Consumer for each resource type.
    * 
    * It is possible that a consumer participates in more
    * than one clearing market (Ie. more than one MCN). 
    * For each MCN that the consumer does participate in,
    * it may furthermore be the case that multiple
    * differentiated resources are to be cleared via this
    * network. The argument List<String> resourceNames
    * specifies IDs for resources that are to be processed
    * using the return value of this method.
    * 
    * It is acceptable for this method to return null in 
    * the event that the party does not wish to respond
    * to clearing markets with the specified resource types.
    * In that eventuality, the callee will be excluded from
    * the calling market.
    * 
    * The argument marketInformation is the result of one
    * invokation of {@link ClearingMarket.#getMarketSummaryInformation
    * getMarketSummaryInformation} for the calling instrument.
    */
   MarketResponseFunction getMarketResponseFunction(
      final ClearingMarketInformation marketInformation
      );
}
