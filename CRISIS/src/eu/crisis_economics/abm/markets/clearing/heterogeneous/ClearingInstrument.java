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

import com.google.common.base.Preconditions;

/**
  * An instrument to be processed on a {@link ClearingMarket}.<br><br>
  * 
  * {@link ClearingInstrument}{@code s} are differentiated by:<br>
  *   {@code (a)} the {@link ClearingMarket} on which they are traded, and <br>
  *   {@code (b)} the resource being traded by the instrument.<br><br>
  *   
  * @author phillips
  */
public final class ClearingInstrument {
   private final String
      market,
      name,
      uuid;
   
   /**
     * Create an immutable {@link ClearingInstrument} object.<br><br>
     * 
     * Two {@link ClearingInstrument}{@code s} with the same parent 
     * {@link ClearingMarket} and the same instrument name are considered
     * to be equal.
     * 
     * @param market
     *        The name of the {@link ClearingMarket} over which this instrument
     *        is to be processed.
     * @param name
     *        The name of this instrument.
     */
   public ClearingInstrument(   // Immutable
      final String market,
      final String name 
      ) {
      Preconditions.checkNotNull(market, name);
      this.market = market;
      this.name = name;
      this.uuid = java.util.UUID.nameUUIDFromBytes((market + name).getBytes()).toString();
   }
   
   public String getMarket() {
      return market;
   }
   
   public String getName() {
      return name;
   }
   
   public String getUUID() {
      return uuid;
   }
   
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((market == null) ? 0 : market
         .hashCode());
      result = prime * result + ((name == null) ? 0 : name
         .hashCode());
      return result;
   }
      
   @Override
   public boolean equals(
      Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj
         .getClass())
         return false;
      ClearingInstrument other = (ClearingInstrument) obj;
      if (market == null) {
         if (other.market != null)
            return false;
      } else if (!market
         .equals(other.market))
         return false;
      if (name == null) {
         if (other.name != null)
            return false;
      } else if (!name
         .equals(other.name))
         return false;
      return true;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "ClearingInstrument, name: " + name + ", market: " + market + ".";
   }
}
