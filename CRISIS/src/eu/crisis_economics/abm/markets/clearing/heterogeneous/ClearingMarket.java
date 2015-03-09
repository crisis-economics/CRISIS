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

import java.util.Set;

import eu.crisis_economics.abm.markets.clearing.ClearingHouse;

/**
  * A simple interface for a clearing market.
  * 
  * {@link ClearingMarket}{@code s} are registered with {@link ClearingHouse}
  * object, whose responsibility is to collect market participants and call 
  * {@link ClearingMarket}.{@link #ClearingMarket.process()} at specific times
  * in the simulation schedule.
  * 
  * @author phillips
  */
public interface ClearingMarket {
   /**
     * Query all participants for market responses and process a clearing session
     * for this market.
     */
   public void process();
   
   /**
     * @return the tickerSymbol
     */
   public String getMarketName();
   
   /**
     * Get a collection of all {@link ClearingInstrument}{@code s} processed by
     * this {@link ClearingMarket}. Adding or removing elements from the return
     * value will not affect the state of this {@link ClearingMarket}.
     */
   public Set<ClearingInstrument> getInstruments();
   
   /**
     * Get a {@link ClearingInstrument} belonging to this {@link ClearingMarket}
     * by name. If no such {@link ClearingInstrument} exists, this method returns
     * {@code null}.
     * 
     * @param name
     *        The {@link String} name of the {@link ClearingInstrument} to find.
     */
   public ClearingInstrument getInstrument(String name);
   
   /**
     * Get a summary of market information for this {@link ClearingMarket}.
     */
   public ClearingMarketInformation getSummaryInformation();
   
   /**
     * Get the {@link ClearingHouse} to which this {@link ClearingMarket}
     * is attached.
     */
   public ClearingHouse getClearingHouse();
}
