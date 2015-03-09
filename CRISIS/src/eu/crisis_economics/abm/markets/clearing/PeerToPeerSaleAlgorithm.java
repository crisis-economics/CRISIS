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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.NullArgumentException;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.ResourceDistributionAlgorithm;
import eu.crisis_economics.utilities.Pair;

/**
  * Perform an abstract peer-to-peer sale process, using a
  * delegate method to process each sale. 
  * @author phillips
  */
public final class PeerToPeerSaleAlgorithm<Consumer, Supplier> implements
   ResourceDistributionAlgorithm<Consumer, Supplier> {
   
   public interface SaleExecutionDelegate<Consumer, Supplier> {
      double processSale(
         final Consumer consumer,
         final Supplier supplier,
         final Pair<Double, Double> resourceExchange
         );
   }
   
   private SaleExecutionDelegate<Consumer, Supplier> delegate;
   
   public PeerToPeerSaleAlgorithm(
      final SaleExecutionDelegate<Consumer, Supplier> saleExecutionDelegate) {
      Preconditions.checkNotNull(saleExecutionDelegate);
      this.delegate = saleExecutionDelegate;
   }
   
   @Override
   public Pair<Double, Double> distributeResources(
      final Map<String, Consumer> marketConsumers,
      final Map<String, Supplier> marketSuppliers,
      final Map<Pair<String, String>, Pair<Double, Double>> desiredResourceExchanges
      ) {
      double
         totalTrade = 0.,
         tradeWeightedInterestRate = 0.;
      for(final Entry<Pair<String, String>, Pair<Double, Double>> record : 
          desiredResourceExchanges.entrySet()) {
         final Consumer consumer = marketConsumers.get(record.getKey().getFirst());
         final Supplier supplier = marketSuppliers.get(record.getKey().getSecond());
         if(consumer == null || supplier == null)
            throw new NullArgumentException();
         final double trade = 
            delegate.processSale(consumer, supplier, record.getValue());
         totalTrade += trade;
         tradeWeightedInterestRate += trade * record.getValue().getSecond();
      }
      return Pair.create(totalTrade, totalTrade == 0. ? 0 : tradeWeightedInterestRate/totalTrade);
   }
}
