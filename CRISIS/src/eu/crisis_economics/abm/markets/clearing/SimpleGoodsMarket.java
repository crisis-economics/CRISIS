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
package eu.crisis_economics.abm.markets.clearing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.matching.MatchingAlgorithm;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.GoodHolder;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository.GoodsClassifier;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.StateVerifier;

public class SimpleGoodsMarket implements Iterable<SimpleGoodsInstrument> {
    
    private Map<String, SimpleGoodsInstrument>
       instruments;
    
    private GoodsClassifier
       goodsClassifier;
    
    private MatchingAlgorithm
       matchingAlgorithm;
    
    @Inject
    public SimpleGoodsMarket(
       final GoodsClassifier goodsClassifier,
    @Named("SIMPLE_GOODS_MARKET_MATCHING_ALGORITHM")
       final MatchingAlgorithm matchingAlgorithm
       ) {
       StateVerifier.checkNotNull(goodsClassifier, matchingAlgorithm);
       this.instruments = new HashMap<String, SimpleGoodsInstrument>();
       this.goodsClassifier = goodsClassifier;
       this.matchingAlgorithm = matchingAlgorithm;
       
       ScheduleSelf();
    }
    
    private void ScheduleSelf() {
        Simulation.repeat(this, "matchAllOrders",
           NamedEventOrderings.GOODS_INPUT_MARKET_MATCHING);
        Simulation.repeat(this, "cancelAllOrders",
           NamedEventOrderings.POST_GOODS_CONSUMPTION_MARKET);
        Simulation.repeat(this, "matchAllOrders",
           NamedEventOrderings.GOODS_CONSUMPTION_MARKET_MATCHING);
    }
    
    @SuppressWarnings("unused") // Scheduled
    private void matchAllOrders() {
       for (final SimpleGoodsInstrument instrument : instruments.values())
          instrument.matchOrders();
    }
    
    @SuppressWarnings("unused") // Scheduled
    private void cancelAllOrders() {
       for(final SimpleGoodsInstrument instrument : instruments.values())
          instrument.cancelOrders();
    }
    
    /** 
      * Add a goods bid/ask order to the market. The behaviour of
      * this method is as follows:<br><br>
      * 
      *    {@code (a)} 
      *        If the party submits an ask order, the party must be an
      *        {@link GoodsSeller}, otherwise {@link OrderException} 
      *        is raised;<br>
      *    {@code (b)}
      *        if the party submits a bid order, the party must be a
      *        {@link GoodsBuyer}, otherwise {@link OrderException} is
      *        raised;<br>
      *    {@code (c)}
      *        sellers will be asked to allocate goods. If the seller
      *        allocates a lesser, nonzero, quantity than the argument,
      *        the order size is reduced accordingly. If the seller
      *        cannot allocate any goods, {@link AllocationException} is
      *        raised;
      *        <br>
      *    {@code (d)}
      *        buyers will be asked to allocate cash at the price per
      *        unit rate specified in the argument. If a lesser, nonzero, 
      *        amount is allocated, the order size is reduced to the
      *        level the Buyer can afford. If the Buyer cannot allocate
      *        any cash, {@link AllocationException} is raised;<br>
      *    {@code (e)}
      *        if the price argument is negative, or size is zero (neither
      *        a bid nor an ask order) then {@link OrderException} is
      *        raised;<br>
      *    {@code (f)}
      *        in all cases, the party must be a {@link GoodHolder}, or
      *        else {@link OrderException} is raised.<br>
      */
    public SimpleGoodsMarketOrder addOrder(final Party party,
       final String goodsType,
       double quantity,
       final double price
       )
       throws OrderException, AllocationException {
       Preconditions.checkNotNull(party);
       if(quantity == 0 || price < 0.)
           throw new OrderException(
               "SimpleGoodsMarketOrder.addOrder: price is negative, or order size is zero. "
             + "Details follow: specified quantity: " + quantity + "; specified price per "
             + "unit: " + price + "."
             );
        if (!(party instanceof GoodHolder))
           throw new OrderException(
              "SimpleGoodsMarketOrder.addOrder: party is not a GoodHolder, as required.");
        if(quantity > 0) {                                              // Sell Order
           GoodsSeller seller = null;
           try {
              seller = (GoodsSeller)party;
           }
           catch(final ClassCastException e) {
              throw new OrderException(
                 "SimpleGoodsMarketOrder.addOrder: seller is not a GoodsSeller, as required.");
           }
           final double goodsAllocated =
              seller.getGoodsRepository().changeAllocatedQuantityBy(goodsType, quantity);
           if(goodsAllocated == 0.)
              throw new AllocationException(
                 "SimpleGoodsMarketOrder.addOrder: seller could not allocate any goods.");
           quantity = goodsAllocated;
        } 
        else {                                                          // Buy Order
           GoodsBuyer buyer = null;
           try {
              buyer = (GoodsBuyer)party;
           }
           catch(final ClassCastException e) {
              throw new OrderException(
                 "SimpleGoodsMarketOrder.addOrder: party is not a GoodsBuyer, as required.");
           }
           final double
              anticipatedCost = (price * -quantity),
              cashAllocated = buyer.allocateCash(anticipatedCost);
            if(cashAllocated <= 0.)
               throw new AllocationException(
                  "SimpleGoodsMarketOrder.addOrder: buyer could not allocate any cash.");
            quantity *= cashAllocated / anticipatedCost;
        }
        
        // get the instrument or create it and send the order
        SimpleGoodsInstrument instrument = getGoodsInstrument(goodsType);
        if(instrument == null) {
            addInstrument(goodsType);
            instrument = getGoodsInstrument(goodsType);
        }
        
        SimpleGoodsMarketOrder.Side orderSide = 
           (quantity > 0. ? SimpleGoodsMarketOrder.Side.SELL : SimpleGoodsMarketOrder.Side.BUY);
        SimpleGoodsMarketOrder existingOrder = instrument.getOrder(party, orderSide);
        if(existingOrder == null) {
           SimpleGoodsMarketOrder order = new SimpleGoodsMarketOrder(
              party, instrument, quantity, price);
           instrument.insertOrder(order);
           return order;
        }
        else {
           existingOrder.addAdditionalQuantityAndUpdatePrice(
              Math.abs(quantity), price);
           return existingOrder;
        }
    }

    /**
     * Gets the stock instrument.
     * 
     * @param  goodsType
     *         the instrument name. This argument should not be <code>null</code>
     * @return the stock instrument; might be <code>null</code> if the given
     *         there is no <code>StockInstrument</code> associated with the
     *         specified instrument name
     */
    public SimpleGoodsInstrument getGoodsInstrument(final String goodsType) {
       return getInstrument(goodsType);
    }
    
    /**
      * Returns <code>true</code> if and only if this {@link SimpleGoodsMarket} contains
      * an instrument for goods of the specified type.
      * 
      * @param goodsType
      *        The type of goods (sector name) to query. This argument must not be
      *        <code>null</code>.
      */
    public boolean hasInstrument(final String goodsType) {
       return instruments.containsKey(goodsType);
    }
    
    /** 
      * Add a new goods instrument to this market.
      */
    public void addInstrument(final String goodsType) {
       if(instruments.get(goodsType) == null)
          instruments.put(goodsType, new SimpleGoodsInstrument(goodsType, matchingAlgorithm));
       else
          throw new IllegalStateException(
             "GoodsMarket.addInstrument: this market already contains an "
           + "instrument of type " + goodsType + ".");
       if(!instruments.get(goodsType).getGoodsType().equals(goodsType))
          throw new IllegalStateException();
    }
    
    public SimpleGoodsInstrument getInstrument(final String goodsType) {
       return instruments.get(goodsType);
    }
    
    /**
      * Get a list of all goods instruments known to this market. Adding or removing
      * elements from the return value will not affect this object.
      */
    public List<String> getInstrumentNames() {
       return new ArrayList<String>(instruments.keySet());
    }
    
    /** 
      * Get the last market demand for an instrument.
      */
    public double getLastAggregateDemand(final String goodsType) {
        final SimpleGoodsInstrument
           instrument = this.getGoodsInstrument(goodsType);
        if(instrument == null)
           return 0.;
        return instrument.getLastAggregateDemand();
    }
    
    /**
      * Get an object that classifies goods traded on this market.
      */
    public GoodsClassifier getGoodsClassifier() {
       return goodsClassifier;
    }

   @Override
   public Iterator<SimpleGoodsInstrument> iterator() {
      return new ArrayList<SimpleGoodsInstrument>(instruments.values()).iterator();
   }
}
