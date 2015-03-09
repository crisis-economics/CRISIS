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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import eu.crisis_economics.abm.simulation.injection.CollectionProvider;
import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;

/**
  * A lightweight utility class for updating {@link Portfolio} clearing market investment
  * options. This class contains:
  * 
  * <ul>
  *   <li> a static method {@link #updateInvestmentOptions} accepting as input: {@code (a)}
  *        a {@link Portfolio} object, {@code (b)} a {@link CollectionProvider} of
  *        {@link ClearingStockMarket}{@code s}, and {@code (c)} a {@link CollectionProvider}
  *        of {@link ClearingLoanMarket}{@code s}. For each {@link ClearingMarket} in 
  *        {@code (b)} and {@code (c)}, an corresponding investment option is registered
  *        with the {@link Portfolio}. If the portfolio contains loan and stock investment
  *        options not listed by {@code (b)} and {@code (c)}, these investment options
  *        are removed from the {@link Portfolio}.
  *   <li> a static method {@link #updateLoanMarketReturnEstimates} accepting as input:
  *        {@code (a)} a {@link Portfolio} object, {@code (b)} a {@link CollectionProvider} of
  *        {@link ClearingStockMarket}{@code s} and {@code (c)} a {@link Collection} of
  *        {@link String}{@code s}. For each {@link ClearingLoanMarket} in {@code (b)}, the
  *        {@link ClearingInstrument}{@code s} of the market are obtained. If any 
  *        {@link ClearingInstrument} has a name that contains a string in {@code (c)}, then
  *        the historical trade-weighted return rate of that instrument is summed against
  *        all other such instruments to form an expected return rate for future market
  *        participation and the portfolio is updated accordingly. Concretely: if 
  *        {@code "Commercial Loan"} and {@code "Gilt"} instruments are to be considered for a 
  *        market {@code X}, then each {@link ClearingInstrument} in market {@code X} is tested for
  *        {@code instrument.getName().contains("Commercial Loan")} and 
  *        {@code instrument.getName().contains("Gilt")}. The last trade-weighted return
  *        rate over all such instruments is taken to be the expected return {@code r} of 
  *        future market participation in market {@code X}. This method updates the portfolio
  *        in argument {@code (a)} with the expected returns {@code r}.
  * </ul>
  * 
  * @author phillips
  */
public final class ClearingMarketPortfolioUpdater {
   
   private ClearingMarketPortfolioUpdater() { }   // Uninstantiable
   
   public static void updateInvestmentOptions(
      Portfolio portfolio,
      final CollectionProvider<ClearingStockMarket> stockMarketsToInvestIn,
      final CollectionProvider<ClearingLoanMarket> loanMarketsToInvestIn
      ) {
      
      final Set<String>
         loanTypesInPortfolio = portfolio.getLoans(),
         stockTypesInPortfolio = portfolio.getStocks();
      
      // Remove loan instruments not to be considered
      for(final String record : loanTypesInPortfolio) {
         boolean found = false;
         for(final ClearingMarket market : loanMarketsToInvestIn)
            if(market.getMarketName().equals(record)) { found = true; break; }
         if(!found) portfolio.removeLoan(record);
      }
      // Add new loan instruments to be considered
      for(final ClearingLoanMarket market : loanMarketsToInvestIn)
         if(!portfolio.hasLoan(market.getMarketName()))
            portfolio.addLoan(market.getMarketName(), 0.);
      
      // Update the list of stock investments to be considered
      for(final String record : stockTypesInPortfolio) {
         boolean found = false;
         for(final ClearingStockMarket market : stockMarketsToInvestIn)
            if(market.getStockReleaserName().equals(record)) { found = true; break; }
         if(!found) portfolio.removeStock(record);
      }
      // Add new stock instruments to be considered
      for(final ClearingStockMarket market : stockMarketsToInvestIn)
         if(!portfolio.hasStock(market.getStockReleaserName()))
            portfolio.addStock(market.getStockReleaserName());
   }
   
   public static void updateLoanMarketReturnEstimates(
      Portfolio portfolio,
      final CollectionProvider<ClearingLoanMarket> loanMarketsToInvestIn,
      final Collection<String> instruments
      ) {
      // Update return rate estimates for loan instruments to be considered
      for(final ClearingMarket market : loanMarketsToInvestIn) {
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
         portfolio.setLoanReturn(market.getMarketName(), returnRateOverAllInstruments);
      }
   }
}
