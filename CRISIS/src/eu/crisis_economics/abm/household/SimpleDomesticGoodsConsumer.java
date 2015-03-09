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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.nonclearing.Order;

/**
  * A simple implementation of the {@link DomesticGoodsConsumer} interface.
  * This implementation provides
  * 
  * <ul>
  *   <li> A simple lightweight internal memory for goods types to be considered by
  *        the implementing agent;
  *   <li> otherwise delegates all operations to an implementation of a {@link GoodsBuyer}
  * </ul>
  * 
  * @author phillips
  */
public final class SimpleDomesticGoodsConsumer implements DomesticGoodsConsumer {
   
   private final GoodsBuyer
      goodsBuyer;
   private final Set<String>
      goodsTypesToConsider;
   
   /**
     * Create a {@link SimpleDomesticGoodsConsumer} with custom parameters.
     * 
     * @param delegate
     *        A {@link GoodsBuyer} to which to delegate all {@link GoodsBuyer}
     *        operations. This argument must be non-<code>null</code>.
     */
   public SimpleDomesticGoodsConsumer(
      final GoodsBuyer delegate) {
      this.goodsBuyer = Preconditions.checkNotNull(delegate);
      this.goodsTypesToConsider = new HashSet<String>();
   }
   
   public double allocateCash(final double positiveAmount) {
      return goodsBuyer.allocateCash(positiveAmount);
   }
   
   public String getUniqueName() {
      return goodsBuyer.getUniqueName();
   }
   
   public void registerIncomingTradeVolume(
      final String goodsType,
      final double goodsAmount,
      final double unitPricePaid
      ) {
      goodsBuyer.registerIncomingTradeVolume(
         goodsType,
         goodsAmount,
         unitPricePaid
         );
   }
   
   public void addAsset(final Contract asset) {
      goodsBuyer.addAsset(asset);
   }
   
   public double calculateAllocatedCashFromActiveOrders() {
      return goodsBuyer.calculateAllocatedCashFromActiveOrders();
   }
   
   public GoodsRepository getGoodsRepository() {
      return goodsBuyer.getGoodsRepository();
   }
   
   public double credit(final double amt) throws InsufficientFundsException {
      return goodsBuyer.credit(amt);
   }
   
   public boolean removeAsset(final Contract asset) {
      return goodsBuyer.removeAsset(asset);
   }
   
   public double disallocateCash(
      final double positiveAmount) {
      return goodsBuyer.disallocateCash(positiveAmount);
   }
   
   public void addOrder(final Order order) {
      goodsBuyer.addOrder(order);
   }
   
   public void debit(final double amt) {
      goodsBuyer.debit(amt);
   }
   
   public boolean removeOrder(final Order order) {
      return goodsBuyer.removeOrder(order);
   }
   
   public void cashFlowInjection(final double amt) {
      goodsBuyer.cashFlowInjection(amt);
   }
   
   public List<Contract> getAssets() {
      return goodsBuyer.getAssets();
   }
   
   @SuppressWarnings("deprecation")
   public void updateState(
      final Order order) {
      goodsBuyer.updateState(order);                  // TODO: this method may need to be removed
   }
   
   public double getAllocatedCash() {
      return goodsBuyer.getAllocatedCash();
   }
   
   public double getUnallocatedCash() {
      return goodsBuyer.getUnallocatedCash();
   }
   
   @Override
   public void addGoodToConsider(final String type) {
      if(type == null)
         return;
      goodsTypesToConsider.add(type);
   }
   
   @Override
   public void addGoodsToConsider(final List<String> types) {
      for(final String record : types)
         addGoodToConsider(record);
   }
   
   @Override
   public void removeGoodToConsider(final String type) {
      if(type == null)
         return;
      goodsTypesToConsider.remove(type);
   }
   
   @Override
   public void removeGoodsToConsider(final List<String> types) {
      for(final String record : types)
         removeGoodToConsider(record);
   }
   
   @Override
   public List<String> getGoodsTypesConsidered() {
      return new ArrayList<String>(goodsTypesToConsider);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Simple Domestic Goods Consumer, goods to consider: "
            + goodsTypesToConsider + ".";
   }
}
