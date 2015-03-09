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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ResourceDistributionAlgorithm;
import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
final public class EraseAndReplaceShareDisributionAlgorithm implements
   ResourceDistributionAlgorithm<StockHolder, StockReleaser> {
   
   public EraseAndReplaceShareDisributionAlgorithm() { } // Stateless
   
   /**
     * A share distribution algorithm based on competitive 
     * investment. This algorithm distributes shares to
     * investors according to the size of their own stock 
     * investment as compared to all others. The final ratio
     * of distributed shares is as close as possible to the
     * ratio of shareholder investments.
     * 
     * This algorithm will operate if marketSuppliers.size()>1,
     * however, it is expected that the market should consist
     * of one distinguished supplier only. If marketSuppliers.
     * size()>1, the first supplier will be treated as the sole
     * StockReleaser entity in the distribution system. All
     * other suppliers will be ignored.
     * 
     * This distribution algorithm returns a Pair<Double, 
     * Double> consisting of:
     * (a) the total investment in shares, summed over all
     *     shareholders, and
     * (b) the clearing share price, 
     * in that order.
     * @see ResourceDistributionAlgorithm.distributeResources
     */
   @Override
   public Pair<Double, Double> distributeResources(
      final Map<String, StockHolder> marketConsumers,
      final Map<String, StockReleaser> marketSuppliers,
      final Map<Pair<String, String>, Pair<Double, Double>> desiredResourceExchanges
      ) {
      if(marketSuppliers.size() > 1 || marketSuppliers.isEmpty())
         throw new IllegalArgumentException(
            "EraseAndReplaceShareDisributionAlgorithm.distributeResources: the caller "
            + "indicated that the stock does not have a unique releaser. This operating "
            + "mode is not supported."
            );
      
      final StockReleaser stockReleaser = marketSuppliers.values().iterator().next();
      final double pricePerShare = UniqueStockExchange.Instance.getStockPrice(stockReleaser);
      double totalAggregateInvestment = 0.;
      
      Map<String, Double> desiredStockHolderInvestments = new HashMap<String, Double>();
      
      for(Entry<Pair<String, String>, Pair<Double, Double>> record : 
         desiredResourceExchanges.entrySet()) {
         final StockHolder stockHolder = marketConsumers.get(record.getKey().getFirst());
         final String holderName = stockHolder.getUniqueName();
         final double additionalInvestment = record.getValue().getFirst();
         if(desiredStockHolderInvestments.containsKey(holderName)) {
            double position = desiredStockHolderInvestments.get(holderName);
            desiredStockHolderInvestments.put(holderName, position + additionalInvestment);
         } else
            desiredStockHolderInvestments.put(holderName, additionalInvestment);
         totalAggregateInvestment += additionalInvestment;
      }
      
      List<Pair<StockHolder, Double>> investments = new ArrayList<Pair<StockHolder,Double>>();
      
      for(Entry<String, Double> record : desiredStockHolderInvestments.entrySet())
         investments.add(Pair.create(marketConsumers.get(record.getKey()), 
             record.getValue()/totalAggregateInvestment));
      
      System.out.println("resource distribution algorithm:");
      System.out.println(investments);
      
      UniqueStockExchange.Instance.eraseAndReplaceStockWithoutCompensation(
         stockReleaser.getUniqueName(),
         investments,
         pricePerShare
         );
      
      return Pair.create(totalAggregateInvestment, pricePerShare);
   }
}
