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

import java.util.Map;

import eu.crisis_economics.utilities.Pair;

public interface ResourceDistributionAlgorithm<Consumer, Supplier> {
   
   /**
     * Distribute resources between Consumers and Suppliers. This
     * interface describes a single method which accepts as input
     * the following arguments:
     *  (a) an immutable map of market consumers, keyed by unique
     *      Consumer IDs.
     *  (b) an immutable map of market suppliers, keyed by unique
     *      Supplier IDs.
     *  (c) an immutable map of desired resources to be exchanged
     *      between Consumers and Suppliers. Keys in this map have
     *      the form Pair<String, String> where the Pair consists 
     *      of one Consumer ID and one Supplier ID, in that order.
     *      Values in this map have the form Pair<Double, Double>
     *      where the Pair consists of one desired trade volume and
     *      one 'rate' describing the trade, in that order. The 
     *      interpretation of the 'rate' is to be specified by 
     *      the implementation. Concretely, a rate may represent an
     *      interest rate for a commercial loan.
     * 
     * The behavior of this method is undefined if Consumer/Supplier
     * keys appearing in (c) do not have values in (a) and (b).
     * 
     * Implementations accept arguments (a-c) as hints as to the
     * final distribution of resources created. This algorithm does 
     * not, in general, guarentee that resource distribution will 
     * satisfy any particular constraint implied by arguments (a-c), 
     * nor, in particular, that the rates indicated by (c) will 
     * necessarily be honored. Should it be the case that any such
     * contraints are honored, the implementation will clearly document
     * this intention.
     * 
     * This method returns a Pair<Double, Double> composed of: 
     *  (a) the sum over all trades executed, and
     *  (b) the resulting trade-weighted rate, if any trade 
     *      occured, or else zero,
     * in that order.
     */
   public Pair<Double, Double> distributeResources(
      final Map<String, Consumer> marketConsumers,
      final Map<String, Supplier> marketSuppliers,
      final Map<Pair<String, String>, Pair<Double, Double>> desiredResourceExchanges
      );
   
}