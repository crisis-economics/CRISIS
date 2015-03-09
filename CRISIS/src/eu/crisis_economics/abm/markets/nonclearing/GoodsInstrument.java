/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 AITIA International, Inc.
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

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import sim.util.Bag;
import eu.crisis_economics.abm.contracts.settlements.GoodsForCashTransaction;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

public class GoodsInstrument extends Instrument {
   private static final long serialVersionUID = 4554494921451817965L;
   private String goodsType;
   
   public GoodsInstrument(
      final String goodsType,
      final BlockingQueue<Order> updatedOrders
      ) {
      super(goodsType, updatedOrders);
      this.goodsType = goodsType;
      scheduleSelf();
   }
   
   public GoodsInstrument(
      final String goodsType,
      final BlockingQueue<Order> updatedOrders,
      final MatchingMode matchingMode,
      final Set<InstrumentListener> listener
      ) {
      super(goodsType, updatedOrders, matchingMode, listener);
      this.goodsType = goodsType;
      scheduleSelf();
      
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "cancelAllOrders", NamedEventOrderings.BEFORE_ALL);
   }
   
   @Override
   protected void setupContract(
      final Order buyOrder,
      final Order sellOrder,
      final double quantity,
      final double price
      ) {
      if (null == buyOrder)
         throw new IllegalArgumentException("buyOrder == null");
      
      if (null == sellOrder)
         throw new IllegalArgumentException("sellOrder == null");
      
      if (quantity < 0)
         throw new IllegalArgumentException(quantity + " == quantity < 0");
      
      if (price < 0)
         throw new IllegalArgumentException(price + " == pricePerShare < 0");
      
      final GoodsSeller seller = (GoodsSeller) sellOrder.getParty();
      
      final GoodsBuyer buyer = (GoodsBuyer) buyOrder.getParty();
      
      GoodsForCashTransaction.process(
         price,
         quantity,
         goodsType,
         seller,
         buyer
         );
   }
   
   public String getGoodsType() {
      return goodsType;
   }
   
   @Override
   public Bag getAskLimitOrders() {
      return super.getAskLimitOrders();
   }
   
   @Override
   public double getAskVolumeAtPrice(
      double price) {
      return super.getAskVolumeAtPrice(price);
   }
}
