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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.exception.NullArgumentException;

public interface MarketResponseFunction extends BoundedDomainMixin {
   
   public final class TradeOpportunity {
      private ClearingInstrument
         resource;
      private double
         rate;
      private String
         tradeParty;
      
      private TradeOpportunity(
         final ClearingInstrument resource,
         final double rate,
         final String tradeParty
         ) {
         if(resource == null || tradeParty == null)
            throw new NullArgumentException();
         this.resource = resource;
         this.rate = rate;
         this.tradeParty = tradeParty;
      }
      
      static TradeOpportunity create(
         final double rate,
         final ClearingInstrument resource,
         final String tradeParty
         ) {
         return new TradeOpportunity(resource, rate, tradeParty);
      }
      
      /*
       * Reset the rate attribute of this opportunity.
       */
      void setRate(final double newRate) { rate = newRate; }
      
      /**
        * Get the underlying trade resource.
        */
      public ClearingInstrument getInstrument() { return resource; }
      
      /**
        * Get the rate attribute of this opportunity.
        */
      public double getRate() { return rate; }
      
      /**
        * Get the unique identifier of the trade partner linked to this
        * opportunity. If there is no such trade partner, or if the trade
        * partner is unknown, this method returns the empty string.
        */
      public String getTradeParty() { return tradeParty; }
      
      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         long temp;
         temp = Double.doubleToLongBits(rate);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         result = prime * result
               + ((resource == null) ? 0 : resource.hashCode());
         result = prime * result
               + ((tradeParty == null) ? 0 : tradeParty.hashCode());
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
         TradeOpportunity other = (TradeOpportunity) obj;
         if (Double.doubleToLongBits(rate) != Double
               .doubleToLongBits(other.rate))
            return false;
         if (resource == null) {
            if (other.resource != null)
               return false;
         } else if (!resource.equals(other.resource))
            return false;
         if (tradeParty == null) {
            if (other.tradeParty != null)
               return false;
         } else if (!tradeParty.equals(other.tradeParty))
            return false;
         return true;
      }
      
      @Override
      public String toString() {
         return "TradeOpportunity, resource:" + resource + ", rate:" + rate
               + ", tradeParty:" + tradeParty + ".";
      }
   }
   
   static class Util {
      /**
        * Utility function. Convert a CandiateRate array to
        * an unmodifiable list of rates. The order of data in 
        * the resulting list is the same as the order of data
        * in the input array.
        */
      static public List<Double> toRateList(final TradeOpportunity[] candidates) {
         final List<Double> result = new ArrayList<Double>();
         for(TradeOpportunity candidate : candidates)
            result.add(candidate.getRate());
         return Collections.unmodifiableList(result);
      }
      
      /**
        * Utility function. Convert a CandiateRate array to
        * an array of rate doubles. The order of data in 
        * the resulting array is the same as the order of data
        * in the input array.
        */
      static public double[] toRateArray(final TradeOpportunity[] candidates) {
        final double[] result = new double[candidates.length];
        for(int i = 0; i< candidates.length; ++i)
           result[i] = candidates[i].getRate();
        return result;
      }
   }
   
   /**
     * Compute the value of a market response function for the
     * given arguments. int[] are the indices of the trade 
     * opportunities for which to evaluate the market response.
     * 
     * Concretely: a consumer response function R accepts as 
     * input a vector of candidate clearing rates { r1, r2 }. 
     * r1 and r2 represent distinct resources and/or trading
     * opportunities. The response R(r1, r2) is a vector of 
     * demands { d1, d2 }. The array { d_i } is the return value
     * of getValue(new double[] { i }, {r1, r2}).
     * 
     * If the length of the input arguments is zero, or if 
     * the value of any integer in the first argument 
     * (int[] queries) is greater than or equal to the length
     * of TradeOpportunity[] opportunities, the behaviour of 
     * getValue(final int dimension, final CandiateRate[] arguments) 
     * is undefined.
     */
   public double[] getValue(final int[] queries, final TradeOpportunity[] opportunities);
}
