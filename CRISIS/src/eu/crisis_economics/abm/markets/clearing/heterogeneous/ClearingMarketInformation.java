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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import eu.crisis_economics.utilities.StateVerifier;

/**
  * A queryable {@link ClearingMarket} summary information object.<br><br>
  * 
  * This object is created by the {@link ClearingMarket} to which it is associated, 
  * and is passed to all participants in that {@link ClearingMarket} when their 
  * market responses are required.
  * 
  * @author phillips
  */
public final class ClearingMarketInformation {
   
   private final ClearingMarket
      market;
   private final Set<ClearingInstrument>
      instruments;
   private final Map<ClearingInstrument, Double>
      lastTradeWeightedReturnRates;
   private final Map<ClearingInstrument, Double>
      lastTransactionVolumes;
   
   /**
     * Create an empty {@link ClearingMarketInformation} object for the stated
     * {@link ClearingMarket}. This method should retain package private visibility.
     */
   ClearingMarketInformation(final ClearingMarket market) {
      StateVerifier.checkNotNull(market);
      this.market = market;
      this.instruments = new HashSet<ClearingInstrument>();
      this.lastTradeWeightedReturnRates = new HashMap<ClearingInstrument, Double>();
      this.lastTransactionVolumes = new HashMap<ClearingInstrument, Double>();
   }
   
   /**
     * Add information about a {@link ClearingInstrument} to this object. This
     * method should retain package private visibility.
     * 
     * @param instrument
     *        The {@link ClearingInstrument} to which to attach metadata.
     * @param lastReturnRate
     *        The last trade-weighted return rate for the stated {@link ClearingInstrument}.
     * @param lastSaleVolume
     *        The total sale (transaction) volume for the stated {@link ClearingInstrument}
     *        when it was last processed. This argument must be non-negative.
     */
   void add(
      final ClearingInstrument instrument,
      final double lastReturnRate,
      final double lastSaleVolume
      ) {
      StateVerifier.checkNotNull(instrument);
      Preconditions.checkArgument(lastSaleVolume >= 0.);
      instruments.add(instrument);
      lastTradeWeightedReturnRates.put(instrument, lastReturnRate);
      lastTransactionVolumes.put(instrument, lastSaleVolume);
   }
   
   /**
     * Get the name of the {@link ClearingMarket} to which this {@link ClearingMarketInformation}
     * object corresponds.
     */
   public String getMarketName() {
      return market.getMarketName();
   }
   
   /**
     * Get a set of {@link ClearingInstrument} objects traded on the {@link ClearingMarket}
     * to which this {@link ClearingMarketInformation} object corresponds. Adding or removing
     * values from the return value will not affect this object.
     */
   public Set<ClearingInstrument> getInstruments() {
      return new HashSet<ClearingInstrument>(instruments);
   }
   
   /**
     * Get the number of {@link ClearingInstrument}{@code s} traded on the {@link ClearingMarket}
     * to which this object corresponds.
     */
   public int getNumberOfInstruments() {
      return instruments.size();
   }
   
   /**
     * Get the last trade-weighted return rates for {@link ClearingInstrument}{@code s}
     * processed on the {@link ClearingMarket} to which this object belongs.
     */
   public Map<ClearingInstrument, Double> getLastTradeWeightedReturnRates() {
      return new HashMap<ClearingInstrument, Double>(lastTradeWeightedReturnRates);
   }
   
   /**
     * Get the last instrument trade volumes for {@link ClearingInstrument}{@code s}
     * processed on the {@link ClearingMarket} to which this object belongs.
     */
   public Map<ClearingInstrument, Double> getLastTransactionVolumes() {
      return new HashMap<ClearingInstrument, Double>(lastTransactionVolumes);
   }
   
   /**
     * Get the {@link Class} of the {@link ClearingMarket} to which this object
     * belongs.
     */
   public Class<? extends ClearingMarket> getMarketType() {
      return market.getClass();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Clearing Market Information, market name: " + market.getMarketName() + " with "
           + instruments.size() + " instruments.";
   }
}
