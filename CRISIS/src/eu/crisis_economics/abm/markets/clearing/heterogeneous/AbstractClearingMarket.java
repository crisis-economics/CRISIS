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

import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * Base/simple implementation of a clearing instrument. This implementation
  * stores a reference to the clearing house with which the instrument is
  * registered.
  * 
  * @author phillips
  */
public abstract class AbstractClearingMarket implements ClearingMarket {
   
   private final String
      marketName;
   private final ClearingHouse
      market;
   
   /**
     * Create a {@link ClearingMarket} with the specified name.
     * 
     * @param marketName
     *        The name of the market to create.
     * @param market
     *        The {@link ClearingHouse} with which the market belongs.
     */
   protected AbstractClearingMarket(
      final String marketName,
      final ClearingHouse market
      ) {
      StateVerifier.checkNotNull(marketName, market);
      this.marketName = marketName;
      this.market = market;
   }
   
   @Override
   public abstract void process();
   
   @Override
   public final String getMarketName() {
      return marketName;
   }
   
   @Override
   public final ClearingHouse getClearingHouse() {
      return market;
   }
   
   @Override
   public final ClearingInstrument getInstrument(final String name) {
      if(name == null) return null;
      final ClearingInstrument
         instrumentToQuery = new ClearingInstrument(getMarketName(), name);
      if(getInstruments().contains(instrumentToQuery))
         return instrumentToQuery;
      return null;
   }
   
   
   @Override
   public String toString() {
      return "Clearing Market, market name:" + marketName + ".";
   }
}