/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * 
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
package eu.crisis_economics.abm.markets.nonclearing;

import java.util.Locale;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.GoodHolder;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

public class GoodsMarket extends Market {
   
   public GoodsMarket() {
      super(NamedEventOrderings.GOODS_CONSUMPTION_MARKET_MATCHING);
   }
   
   public GoodsMarketOrder addOrder(
      final Party party,
      final String goodsType,
      final double quantity,
      final double price
      ) throws OrderException, AllocationException {
      return addOrder(
         party,
         goodsType,
         quantity,
         price,
         DefaultFilters.any());
   }

    /**
      * Add a new goods market order. This method accepts a quantity (the number of 
      * units of goods) and a price per unit for goods. If the party is required to
      * allocate cash (for a buy order), the size of the order will be reduced based
      * on amount of cash that the party could actually allocate. For example if the 
      * buyer was able to allocate 50% of the cash cost of the order, the size (number
      * of units) of the order is reduced by 50% to match the amount that the buyer
      * could afford. If the cash allocation fails entirely, AllocationException is
      * raised. Similarly, if a seller can only allocate 40% of the stated goods
      * units, the size of the sell order is reduced by 40%. If the seller cannot
      * allocate any goods, AllocationException is raised.
      */
    public GoodsMarketOrder addOrder(
       final Party party,
       final String goodsType,
       double quantity,
       final double price,
       final Filter filter
       ) throws OrderException, AllocationException {
       if (null == party)
          throw new IllegalArgumentException("party == null");
       
       if (!(party instanceof GoodHolder)) {
          throw new IllegalArgumentException(
             "Only GoodHolder instances can trade with goods, current class does not implement it: "
                + party.getClass());
       }
       
       if (Double.compare(quantity, 0) == 0)
          throw new IllegalArgumentException("quantity == 0");
       
       if (price < 0)
          throw new IllegalArgumentException(price + " == price < 0");
       
       // Sell order: allocate goods.
       if(quantity > 0) { 
          final double ownedQuantity =
             ((GoodHolder) party).getGoodsRepository().getStoredQuantity(goodsType);
          if (ownedQuantity < quantity) {
             throw new OrderException(
               String.format(
                  Locale.US,
                  "Insufficient goods. Tried to sell %f out of owned %f units of good %s.",
                  quantity, ownedQuantity, goodsType));
          }
          final double allocatedGoods =
             ((GoodsSeller) party).getGoodsRepository().changeAllocatedQuantityBy(
                 goodsType, +quantity);
          if(allocatedGoods == 0.)
             throw new AllocationException();
          quantity = allocatedGoods;
       } 
       else { // Buy order: allocate cash
          if (price == 0){    
             // for market order get estimated price from instrument
             final double bidAmount =
                getGoodsInstrument(goodsType).getBidPriceAtVolume(-quantity),
                allocatedCash = ((GoodsBuyer) party).allocateCash(bidAmount);
             if(allocatedCash == 0.)
                throw new AllocationException();
             quantity *= Math.min(allocatedCash / bidAmount, 1.0);
         } else {
             final double
                anticipatedCost = price*-quantity,
                allocatedCash = ((GoodsBuyer) party).allocateCash(anticipatedCost);
             if(allocatedCash == 0.)
                throw new AllocationException();
             quantity *= Math.min(allocatedCash / anticipatedCost, 1.0);
         }
      }
      // System.out.println(quantity + " " + goodsType);
      GoodsInstrument instrument = getGoodsInstrument(goodsType);
      
      if (null == instrument) {
         addInstrument(goodsType); // Christoph changed this
         instrument = getGoodsInstrument(goodsType);
      }
      
      return new GoodsMarketOrder(party, instrument, quantity, price, filter);
   }
   
   /**
     * Gets a goods instrument by name.
     * 
     * @param goodsType
     *        the instrument name; cannot be <code>null</code>
     * @return the stock instrument; might be <code>null</code> if the given
     *         there is no <code>StockInstrument</code> associated with the
     *         specified instrument name
     */
   public GoodsInstrument getGoodsInstrument(
      final String goodsType) {
      return (GoodsInstrument) getInstrument(goodsType);
   }
   
   public void addInstrument(
      final String goodsType
      ) {
      if (getInstrument(goodsType) == null) {
         if (instrumentMatchingMode == InstrumentMatchingMode.DEFAULT) {
            instruments.put(
               goodsType,
               new GoodsInstrument(goodsType, updatedOrders)
               );
         } else if (instrumentMatchingMode == InstrumentMatchingMode.ASYNCHRONOUS) {
            instruments.put(
               goodsType,
               new GoodsInstrument(goodsType,
                  updatedOrders,
                  Instrument.MatchingMode.ASYNCHRONOUS,
                  listeners)
               );
         } else if (instrumentMatchingMode == InstrumentMatchingMode.SYNCHRONOUS) {
            instruments.put(
               goodsType,
               new GoodsInstrument(goodsType,
                  updatedOrders,
                  Instrument.MatchingMode.SYNCHRONOUS,
                  listeners)
               );
         }
      }
   }
   
   public GoodsInstrument getInstrument(
      final int goodsType) {
      return (GoodsInstrument) getInstrument(goodsType);
      
   }
   
   public static final int DEFAULT_MATURITY = 1;
}
