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
package eu.crisis_economics.abm.household;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.SimpleResourceInventory;
import eu.crisis_economics.abm.inventory.goods.GoodsInventory;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.inventory.goods.SimpleGoodsRepository;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A simple, exogenous implementation of the GoodsSeller interface.
  * This object can generate goods, for free, and provide these goods
  * to the market for sale. The seller has access to a virtually
  * unlimited, exogenous cash supply.
  * @author phillips
  */
public final class ExogenousGoodsSeller implements GoodsSeller {
   
   private final Inventory cash;
   private final String uniqueName;
   private List<Order> orders;
   private GoodsRepository goodsRepository;
   private List<Contract> assets;
   private SimpleGoodsMarket goodsMarket;
   
   public ExogenousGoodsSeller(
      SimpleGoodsMarket goodsMarket
      ) {
      StateVerifier.checkNotNull(goodsMarket);
      this.cash = new SimpleResourceInventory(Double.MAX_VALUE / 2.);
      this.uniqueName = java.util.UUID.randomUUID().toString();
      this.orders = new ArrayList<Order>();
      this.goodsRepository = new SimpleGoodsRepository();
      this.assets = new ArrayList<Contract>();
      this.goodsMarket = goodsMarket;
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "submitGoodsSellOrders", 
         NamedEventOrderings.SELL_ORDERS);
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void submitGoodsSellOrders() {
      for(final Entry<String, GoodsInventory> record : goodsRepository.entrySet()) {
         if(goodsMarket.getInstrument(record.getKey()) == null)
            continue;                                                   // No such instrument
         final String
            goodsType = record.getKey();
         final double
            amountOwned = record.getValue().getUnallocated();
         if (amountOwned > 0.) {
            try {
               goodsMarket.addOrder(
                  this,
                  goodsType,
                  amountOwned, 
                  goodsMarket.getInstrument(goodsType).getWorstAskPriceAmongSellers()
                  );
            } catch (AllocationException e) {
               // Abandon
            } catch (OrderException e) {
               // Abandon
            }
         }
      }
   }
   
   @Override
   public void addOrder(Order order) {
      orders.add(order);
   }
   
   @Override
   public boolean removeOrder(final Order order) {
      return orders.remove(order);
   }

   @Override
   public void updateState(final Order order) {
      // No Action.
   }
   
   @Override
   public String getUniqueName() {
      return uniqueName;
   }
   
   @Override
   public GoodsRepository getGoodsRepository() {
      return goodsRepository;
   }
   
   @Override
   public double credit(double amount) throws InsufficientFundsException {
      cash.pull(amount);
      return amount;
   }
   
   @Override
   public void debit(double amount) {
      cash.push(amount);
   }
   
   @Override
   public void cashFlowInjection(double amt) {
      // No Action.
   }
   
   private double
      sellingPricePerUnit;
   
   @Override
   public double getGoodsSellingPrice() {
      return sellingPricePerUnit;
   }
   
   public void setGoodsSellingPrice(final double value) {
      Preconditions.checkArgument(value >= 0.);
      this.sellingPricePerUnit = value;
   }
   
   @Override
   public void addAsset(Contract asset) {
      assets.add(asset);
   }

   @Override
   public boolean removeAsset(Contract asset) {
      return assets.remove(asset);
   }
   
   @Override
   public List<Contract> getAssets() {
      return Collections.unmodifiableList(new ArrayList<Contract>(assets));
   }
   
   @Override
   public void registerOutgoingTradeVolume(
      String goodsType,
      double goodsAmount,
      double unitPricePaid) {
      // TODO Auto-generated method stub
      
   }
}
