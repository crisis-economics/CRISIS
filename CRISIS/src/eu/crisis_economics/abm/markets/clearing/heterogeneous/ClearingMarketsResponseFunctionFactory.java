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

import eu.crisis_economics.abm.Agent;

/**
  * An interface for {@link MarketResponseFunction} factories. Implementations of
  * this interface accept, as input, an {@link Agent} (the clearing market participant)
  * and a {@link ClearingMarketInformation} object, detailing the market in which the
  * {@link Agent} will participate. The factory either returns {@code null}, if no 
  * response to the market is specified, or else a valid {@link MarketResponseFunction}
  * object.
  * 
  * @author phillips
  */
public interface ClearingMarketsResponseFunctionFactory {
   /**
     * Create a {@link MarketResponseFunction} for the specified {@link Agent}
     * in the specified market.
     * 
     * @param participant
     *        The market participant. This argument is not modified.
     * @param market
     *        Summary information for the market in which the {@link Agent}
     *        intends to participate.
     */
   public MarketResponseFunction create(
      Agent participant, ClearingMarketInformation market);
}
