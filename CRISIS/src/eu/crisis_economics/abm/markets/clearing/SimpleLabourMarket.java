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
package eu.crisis_economics.abm.markets.clearing;

import java.util.ArrayList;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.matching.MatchingAlgorithm;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.CashAllocating;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/** 
  * A Labour market specific to the macro simulation model. 
  * In due course this model should be unified with the broader
  * (distinct) market/instrument framework.
  */
public final class SimpleLabourMarket {
   
   private ArrayList<SimpleLabourInstrument>
      instruments;
   
   private MatchingAlgorithm
      labourMatchingAlgorithm;
   
   @Inject
   public SimpleLabourMarket(
   @Named("SIMPLE_LABOUR_MARKET_MATCHING_ALGORITHM")
      final MatchingAlgorithm labourMatchingAlgorithm
      ) {
      Preconditions.checkNotNull(labourMatchingAlgorithm);
      this.instruments = new ArrayList<SimpleLabourInstrument>();
      this.labourMatchingAlgorithm = labourMatchingAlgorithm;
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "matchAllOrders",  NamedEventOrderings.LABOUR_MARKET_MATCHING);
      Simulation.repeat(this, "cancelAllOrders", NamedEventOrderings.POST_LABOUR_MARKET_MATCHING);
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void matchAllOrders() {
      for(SimpleLabourInstrument instrument : instruments)
         instrument.matchOrders();
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void cancelAllOrders() {
      for(SimpleLabourInstrument instrument : instruments)
         instrument.cancelOrders();
   }
   
   /** 
     * Add a labour bid/ask order to the market. The behaviour of
     * this method is as follows:
     *    (a) If the party submits an ask order, the party must be an
     *        Employee, otherwise OrderException is raised;
     *    (b) if the party submits a bid order, the party must be a Buyer,
     *        otherwise OrderException is raised;
     *    (b) employees must be able to allocate sufficient units of
     *        labour for the order, otherwise AllocationException is
     *        raised;
     *    (d) Buyers will be asked to allocate cash at the price per
     *        unit rate specified in the argument. If a lesser, nonzero, 
     *        amount is allocated, the order size is reduced to the
     *        level the Buyer can afford. If the Buyer cannot allocate
     *        any cash, AllocationException is raised;
     *    (e) if the price argument is negative, or size is zero (neither
     *        a bid nor an ask order) then OrderException is raised.
     */
   public SimpleLabourMarketOrder addOrder(
      Party party, int maturity, double size, double price)
      throws OrderException, AllocationException 
      {
      Preconditions.checkNotNull(party);
      if(size == 0 || price < 0.)
         throw new OrderException(
            "SimpleLabourMarket.addOrder: price is negative, or order size is zero. "
          + "Details follow: specified quantity: " + size + ", specified price per "
          + "unit: " + price + "."
            );
       
       if (size > 0)                                                   // Sell order
           try {
              ((Employee) party).allocateLabour(size);
           }
           catch(final ClassCastException e) {
               throw new OrderException(
                  "SimpleLabourMarket.addOrder: party is not an Employee: " + e);
            }
        else {                                                          // Buy order
           if(price > 0.) {
              final double
                 amountToAllocate = -price * size,
                 successfullyAllocated = ((CashAllocating) party).allocateCash(amountToAllocate);
              if(successfullyAllocated <= 0.)
                 throw new AllocationException(
                    "SimpleLabourMarket.addOrder: buyer failed to allocate any cash.");
              size *= successfullyAllocated / amountToAllocate;         // Reduce order size
           }
        }
        
        final SimpleLabourInstrument instrument = getInstrument(maturity);
        final SimpleLabourMarketOrder order = 
           new SimpleLabourMarketOrder(party, instrument, size, price);
        instrument.insertOrder(order);
        return order;
    }
    
    public SimpleLabourInstrument getInstrument(int instrumentMaturity) {
        if(instrumentMaturity <= 0)
            throw new IllegalArgumentException(
                "SimpleLabourMarket.getInstrument: instrument maturity is non-positive.");
        for(SimpleLabourInstrument instrument : instruments)
            if(instrument.getMaturity() == instrumentMaturity)
                return instrument;
        SimpleLabourInstrument newInstrument = 
            new SimpleLabourInstrument(instrumentMaturity, labourMatchingAlgorithm);
        instruments.add(newInstrument);
        return newInstrument;
    }
    
    /** 
      * Get the last known total employment over all labour instruments
      * for this {@link SimpleLabourMarket}.
      */
    public double getLastTotalEmployedLabour() {
        double result = 0;
        for(SimpleLabourInstrument instrument : instruments)
            result += instrument.getLastTotalEmployedLabour();
        return result;
    }
    
    /** 
      * Get the last known total labour supply over all labour instruments
      * for this {@link SimpleLabourMarket}
      */
    public double getLastLabourTotalSupply() {
        double result = 0;
        for(SimpleLabourInstrument instrument : instruments)
            result += instrument.getLastLabourTotalSupply();
        return result;
    }
    
    /** 
      * Get the last known total labour demand over all labour instruments
      * for this {@link SimpleLabourMarket}
      */
    public double getLastLabourTotalDemand() {
        double result = 0;
        for(SimpleLabourInstrument instrument : instruments)
            result += instrument.getLastLabourTotalDemand();
        return result;
    }
}
