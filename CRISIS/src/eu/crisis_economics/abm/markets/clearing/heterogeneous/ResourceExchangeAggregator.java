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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.List;
import java.util.Map;

import eu.crisis_economics.utilities.Pair;

interface ResourceExchangeAggregator extends
   ResourceExchangeDelegate,
   Iterable<Pair<Pair<Object, Object>, Pair<Double, Double>>> {
   
   /**
     * Get an unmodifiable view of all keyed resource exchanges.
     */
   public abstract Map<Pair<String, String>, Pair<Double, Double>> getUnmodifiableKeyedResourceExchanges();
   
   /**
     * Get an unmodiable view of all Consumers known to this aggregator.
     */
   public abstract List<Object> getConsumers();
   
   /**
     * Get an unmodifiable keyed map of Consumers.
     */
   public abstract Map<String, Object> getKeyedConsumers();
   
   /**
     * Get an unmodifiable view of all Suppliers known to this aggregator.
     */
   public abstract List<Object> getSuppliers();
   
   /**
     * Get an unmodifiable keyed map of Suppliers.
     */
   public abstract Map<String, Object> getKeyedSuppliers();
   
   /**
     * Get total Consumer demand.
     */
   public abstract double getTotalConsumerDemand();
   
   /**
     * Get total Supplier supply.
     */
   public abstract double getTotalSupplierSupply();
   
   /**
     * Get total desired trade.
     */
   public abstract double getTotalDesiredTrade();
   
   /**
     * Get the expected volume-weighted trade rate.
     */
   public abstract double getDesiredTradeWeightedRate();
   
   
   public abstract boolean isEmpty(); 
}