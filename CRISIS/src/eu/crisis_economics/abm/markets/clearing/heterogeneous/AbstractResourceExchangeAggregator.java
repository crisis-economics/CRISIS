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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
abstract class AbstractResourceExchangeAggregator 
   implements ResourceExchangeAggregator {
   
   private Set<MixedClearingNetworkResult>
      pendingTransferVolumes;
   private Map<Pair<String, String>, Pair<Double, Double>>
      desiredResourceExchanges;
   private Map<String, Object>
      knownConsumers,
      knownSuppliers;
   
   public AbstractResourceExchangeAggregator() {
      this.pendingTransferVolumes = new HashSet<MixedClearingNetworkResult>();
      this.desiredResourceExchanges = new HashMap<Pair<String,String>, Pair<Double,Double>>();
      this.knownConsumers = new HashMap<String, Object>();
      this.knownSuppliers = new HashMap<String, Object>();
   }
   
   @Override
   public void commit(MixedClearingNetworkResult result) {
      pendingTransferVolumes.add(result);
      desiredResourceExchanges.put(
         new Pair<String, String>(
            result.getDemandSideID(), result.getSupplySideID()),
         new Pair<Double, Double>(
            computeViableTradeForClearingResult(result), result.getClearingRate())
         );
      if(!knownConsumers.containsKey(result.getDemandSideID()))
         knownConsumers.put(result.getDemandSideID(), result.getDemandSideObject());
      if(!knownSuppliers.containsKey(result.getSupplySideID()))
         knownSuppliers.put(result.getSupplySideID(), result.getSupplySideObject());
   }
   
   private class DesiredTradeIterator implements 
      Iterator<Pair<Pair<Object, Object>, Pair<Double, Double>>> {
      Iterator<Entry<Pair<String, String>, Pair<Double, Double>>> keyedIterator;
      
      DesiredTradeIterator() {
         keyedIterator =
            Collections.unmodifiableMap(desiredResourceExchanges).entrySet().iterator();
      }
      
      @Override
      public boolean hasNext() {
         return keyedIterator.hasNext();
      }
      @Override
      public Pair<Pair<Object, Object>, Pair<Double, Double>> next() {
         Entry<Pair<String, String>, Pair<Double, Double>> next = keyedIterator.next();
         return new Pair<Pair<Object, Object>, Pair<Double, Double>>(
            new Pair<Object, Object>(
               knownConsumers.get(next.getKey().getFirst()),
               knownSuppliers.get(next.getKey().getSecond())
               ),
            next.getValue()
            );
      }
      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
   
   /**
     * Get an iterator to all known resources exchanges.
     * The format of each entry in the resulting Iterator is a 
     * Pair composed of:
     * (a) one Pair<Object, Object> in the format (Consumer, Supplier), and
     * (b) one Pair<Double, Double> in the format (Desired Maximum Trade, Trade Rate),
     * in that order.
     */
   @Override
   public Iterator<Pair<Pair<Object, Object>, Pair<Double, Double>>> iterator() {
      return new DesiredTradeIterator();
   }
   
   /**
     * Get a keyed iterator to an unmodifiable view of all aggregated resources exchanges.
     * @return
     */
   public Iterator<Map.Entry<Pair<String, String>, Pair<Double, Double>>> keyedIterator() {
      return Collections.unmodifiableMap(desiredResourceExchanges).entrySet().iterator();
   }
   
   @Override
   public Map<Pair<String, String>, Pair<Double, Double>> 
      getUnmodifiableKeyedResourceExchanges() {
      return Collections.unmodifiableMap(desiredResourceExchanges);
   }
   
   @Override
   public List<Object> getConsumers() {
      return Collections.unmodifiableList(new ArrayList<Object>(knownSuppliers.values()));
   }
   
   @Override
   public Map<String, Object> getKeyedConsumers() {
      return Collections.unmodifiableMap(knownConsumers);
   }
   
   @Override
   public List<Object> getSuppliers() {
      return Collections.unmodifiableList(new ArrayList<Object>(knownConsumers.values()));
   }
   
   @Override
   public Map<String, Object> getKeyedSuppliers() {
      return Collections.unmodifiableMap(knownSuppliers);
   }
   
   @Override
   public double getTotalConsumerDemand() {
      double result = 0.;
      for(MixedClearingNetworkResult record : pendingTransferVolumes)
         result += record.getDemandVolume();
      return result;
   }
   
   @Override
   public double getTotalSupplierSupply() {
      double result = 0.;
      for(MixedClearingNetworkResult record : pendingTransferVolumes)
         result += record.getSupplyVolume();
      return result;
   }
   
   @Override
   public double getTotalDesiredTrade() {
      double result = 0.;
      for(MixedClearingNetworkResult record : pendingTransferVolumes)
         result += computeViableTradeForClearingResult(record);
      return result;
   }
   
   @Override
   public double getDesiredTradeWeightedRate() {
      double
         desiredTrade = 0.,
         result = 0.;
      for(MixedClearingNetworkResult record : pendingTransferVolumes) {
         final double trade = computeViableTradeForClearingResult(record);
         desiredTrade += trade;
         result += trade * record.getClearingRate();
      }
      return desiredTrade == 0 ? 0. : result / desiredTrade;
   }
   
   @Override
   public boolean isEmpty() {
      return pendingTransferVolumes.isEmpty();
   }
   
   protected final Set<MixedClearingNetworkResult> getPendingTransferVolumes() {
      return new HashSet<MixedClearingNetworkResult>(pendingTransferVolumes);
   }
   
   protected abstract double computeViableTradeForClearingResult(
      final MixedClearingNetworkResult result);
}
