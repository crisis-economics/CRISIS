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
package eu.crisis_economics.abm.firm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.SimpleResourceInventory;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.inventory.goods.SimpleGoodsRepository;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A simple, exogenous implementation of the {@link GoodsBuyer} interface.
  * This object has a virtually unlimited cash reserve with which to buy
  * goods and services from the market. The demand for goods, in the next
  * market session, is set via {@link #
  * @author phillips
  */
public class ExogenousGoodsBuyer implements GoodsBuyer {
   
   private final Inventory cash;
   private final String uniqueName;
   private List<Order> orders;
   private GoodsRepository goodsRepository;
   private List<Contract> assets;
   private SimpleGoodsMarket goodsMarket;
   private Map<String, Double> goodsDemands;
   
   public ExogenousGoodsBuyer(final SimpleGoodsMarket market) {
      StateVerifier.checkNotNull(market);
      this.cash = new SimpleResourceInventory(Double.MAX_VALUE / 2.);
      this.uniqueName = java.util.UUID.randomUUID().toString();
      this.orders = new ArrayList<Order>();
      this.goodsRepository = new SimpleGoodsRepository();
      this.assets = new ArrayList<Contract>();
      this.goodsMarket = market;
      this.goodsDemands = new HashMap<String, Double>();
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "submitGoodsBuyOrders",
         NamedEventOrderings.SELL_ORDERS);
      Simulation.repeat(this, "submitGoodsBuyOrders",
         NamedEventOrderings.FIRM_BUY_INPUTS);
      Simulation.repeat(this, "reset", NamedEventOrderings.AFTER_ALL);
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void submitGoodsBuyOrders() {
      for(Entry<String, Double> record : goodsDemands.entrySet()) {
         final double
            amountDesired = record.getValue();
         if (amountDesired > 0.) {
            try {
               goodsMarket.addOrder(
                  this,
                  record.getKey(),
                  -amountDesired, 
                  goodsMarket.getInstrument(record.getKey()).getWorstAskPriceAmongSellers()
                  );
            } catch (AllocationException e) {
               // Abandon
            } catch (OrderException e) {
               // Abandon
            }
         }
      }
      goodsDemands.clear();
   }
   
   /**
     * Reset goods demands for the next market session.
     */
   @SuppressWarnings("unused")   // Scheduled
   private void reset() {
      goodsDemands.clear();
   }
   
   /**
     * Set the demand for goods of the specified type.
     * @param type
     *        The type of goods to buy.
     * @param quantity
     *        The quantity of goods to buy. This argument should be
     *        non-negative.
     */
   public void setGoodsDemand(final String type, final double quantity) {
      goodsDemands.put(type, quantity);
   }
   
   @Override
   public double calculateAllocatedCashFromActiveOrders() {
      return 0.;
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
   public void updateState(Order order) {
      // No action.
   }
   
   @Override
   public String getUniqueName() {
      return uniqueName;
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
      // No action.
   }
   
   @Override
   public double allocateCash(double positiveAmount) {
      return cash.changeAllocatedBy(positiveAmount);
   }

   @Override
   public double disallocateCash(double positiveAmount) {
      return -cash.changeAllocatedBy(-positiveAmount);
   }
   
   @Override
   public double getAllocatedCash() {
      return cash.getAllocated();
   }
   
   @Override
   public GoodsRepository getGoodsRepository() {
      return goodsRepository;
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
   public void registerIncomingTradeVolume(
      String goodsType, double goodsAmount, double unitPricePaid) {
      // No action.
   }
   
   @Override
   public double getUnallocatedCash() {
      return cash.getUnallocated();
   }
}
