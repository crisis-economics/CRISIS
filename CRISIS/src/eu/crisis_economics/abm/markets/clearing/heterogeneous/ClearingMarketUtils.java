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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
  * Static utility methods for {@link ClearingMarket} and {@link ClearingInstrument}
  * objects and associated operations. This utility class provides the following methods:
  * 
  * <ul>
  *   <li> {@link #estimateLastReturnRateForLoanMarkets(ClearingLoanMarket, Collection)}.<br> This
  *        method computes the expected return rate (return per unit cash invested in 
  *        a loan market) based on (a) the last observed trade volumes for each instrument
  *        traded on the loan market, and (b) the last observed interest rates for contracts
  *        formed by each such instrument. This return rate estimate is accordingly based
  *        solely on observations of the last processing session for the specified market. The
  *        market instruments to consider can be filtered by name.
  * </ul>
  * 
  * @author phillips
  */
public final class ClearingMarketUtils {
   
   /**
     * Estimate the trade-weighted return rate for {@link ClearingInstrument}{@code s} traded on a
     * {@link ClearingLoanMarket}. This method accepts as input (a) a {@link ClearingLoanMarket}
     * object and (b) a {@link Collection} of {@link String}{@code s}. The latter 
     * is a {@link Collection} of filters for {@link ClearingInstrument}{@code s} traded on the
     * market. For each {@link String} filter appearing in (b), any {@link ClearingInstrument}
     * whose name contains this {@link String} will contribute to the return rate estimate
     * to be formed.<br><br>
     * 
     * @param market
     *        A {@link ClearingLoanMarket} to consider.
     * @param instruments
     *        A {@link Collection} of {@link String} filters. Only
     *        {@link ClearingInstrument}{@code s} belonging to the specified market whose
     *        names contain at least one such filter {@link String} will be considered.
     */
   public static double estimateLastReturnRateForLoanMarkets(
      final ClearingLoanMarket market,
      final Collection<String> instruments
      ) {
      final Map<ClearingInstrument, Double>
         instrumentPerformances = 
            market.getSummaryInformation().getLastTradeWeightedReturnRates(),
         instrumentTrades = 
            market.getSummaryInformation().getLastTransactionVolumes();
      double
         returnRateOverAllInstruments = 0.,
         totalTradeOverAllInstruments = 0.;
      for(final Entry<ClearingInstrument, Double> record : instrumentPerformances.entrySet()) {
         final ClearingInstrument
            instrument = record.getKey();
         final String
            name = instrument.getName();
         for(final String instrumentType : instruments)
            if(name.contains(instrumentType)) {
               double
                  lastTradeVolume = instrumentTrades.get(instrument),
                  lastReturnRate = record.getValue();
               returnRateOverAllInstruments += lastTradeVolume * lastReturnRate;
               totalTradeOverAllInstruments += lastTradeVolume;
               break;
            }
      }
      if(totalTradeOverAllInstruments != 0.)
         returnRateOverAllInstruments /= totalTradeOverAllInstruments;
      else
         returnRateOverAllInstruments = 0.;                                // No data
      return returnRateOverAllInstruments;
   }
   
   private ClearingMarketUtils() { }
}
