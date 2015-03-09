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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.markets.clearing.CentralPaymentShareDisributionAlgorithm;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * @author phillips
  */
public final class ClearingStockMarket extends AbstractClearingMarket {
   
   private final ClearingInstrument
      instrument;
   private final StockReleaser
      stockReleaser;
   private double
      lastStockPrice,
      lastTotalStockTradeNumberOfShares;
   
   /**
     * Create a {@link ClearingStockMarket} for the stated {@link StockReleaser},
     * belonging to the stated {@link ClearingHouse}.<br><br>
     * 
     * This {@link ClearingStockMarket} will process exactly one {@link ClearingInstrument}.
     * The instrument is shares in the stated {@link StockReleaser}.
     * 
     * @param stockReleaser
     *        The {@link StockReleaser} whose shares are to be traded on this instrument.
     * @param market
     *        The {@link ClearingHouse} to which this {@link ClearingMarket} belongs.
     */
   public ClearingStockMarket(
      final StockReleaser stockReleaser,
      final ClearingHouse house
      ) {
      super("Clearing Stock Market for " + stockReleaser.getUniqueName(), house);
      Preconditions.checkNotNull(stockReleaser);
      this.instrument = instrumentFor(stockReleaser);
      this.stockReleaser = stockReleaser;
   }
   
   @Override
   public void process() {
      ResourceExchangeAggregator desiredTradesAggregator = 
         new AbstractResourceExchangeAggregator() {
            @Override
            protected double computeViableTradeForClearingResult(
               final MixedClearingNetworkResult result) {
               return result.getDemandVolume();
            }
            @Override
            public double getTotalDesiredTrade() {
               double result = 0.;
               for(final MixedClearingNetworkResult record : getPendingTransferVolumes())
                  result += Math.abs(record.getDemandVolume());
               return result;
            }
         };
      lastStockPrice = UniqueStockExchange.Instance.getStockPrice(stockReleaser);
      CompleteNetworkMarket network =
         new PureHomogeneousNetworkMarket(
            instrument, desiredTradesAggregator);
      if(addStockHoldersToNetwork(network) <= 1 || 
         UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockReleaser) == 0.) {
         System.out.printf(
            "---------------------------------------------\n" +
            "Stock Market Cleared [Time: %g]\n" +
            " Releaser: %s\n" +
            " Price                                %8.3g *\n" +
            " No trade in this session.\n" +
            "---------------------------------------------\n",
            Simulation.getTime(),
            stockReleaser.getUniqueName(),
            UniqueStockExchange.Instance.getStockPrice(stockReleaser)
            );
         System.out.flush();
         return;
      }
      
      addStockReleasersToNetwork(network);
      network.matchAllOrders(75, 1);
      
      // Commit the new price per share.
      final double clearingPricePreShare = 
         desiredTradesAggregator.iterator().next().getSecond().getSecond();
      
      lastTotalStockTradeNumberOfShares = desiredTradesAggregator.getTotalDesiredTrade();
      
      UniqueStockExchange.Instance.setStockPrice(
         stockReleaser.getUniqueName(), clearingPricePreShare);
      
      updatePortfolios(desiredTradesAggregator);
      
      { // Stdout
         double
            totalStockSupply = desiredTradesAggregator.getTotalSupplierSupply(),
            totalStockDemand = desiredTradesAggregator.getTotalConsumerDemand();
         System.out.printf(
            "---------------------------------------------\n" +
            "Stock Market Cleared [Time: %g]\n" +
            " Releaser: %s\n" +
            " Price Selected:                      %8.3g *\n" +
            " Demand:                             %8.3g\n" +
            " Supply:                             %8.3g\n" +
            " Abs. Diff.:                         %8.3g\n" +
            "---------------------------------------------\n",
            Simulation.getTime(),
            stockReleaser.getUniqueName(),
            clearingPricePreShare, 
            totalStockDemand, 
            totalStockSupply,
            Math.abs(totalStockSupply - totalStockDemand)
            );
         System.out.flush();
      }
   }
   
   private int addStockHoldersToNetwork(final CompleteNetworkMarket network) {
      final List<ClearingInstrument> resources =
         new ArrayList<ClearingInstrument>();
      resources.add(instrument);
      int numberOfParticipants = 0;
      System.out.printf("Stock Market Participants:\n");
      for(final Entry<String, ClearingMarketParticipant> record : 
         getClearingHouse().getStockMarketParticipants().entrySet()) {
         final MarketResponseFunction marketResponseFunction =
            record.getValue().getMarketResponseFunction(
               getSummaryInformation());
         if(marketResponseFunction == null)                             // Participant sits out.
            continue;
         network.addBuyOrder(
            record.getValue(),
            record.getKey(),
            marketResponseFunction
            );
         ++numberOfParticipants;
      }
      if(numberOfParticipants == 0)
         System.out.printf("[no participants]\n");
      return numberOfParticipants;
   }
   
   private void addStockReleasersToNetwork(final CompleteNetworkMarket network) {
      final List<ClearingInstrument> resources =
         new ArrayList<ClearingInstrument>();
      resources.add(instrument);
      /**
        * The {@link StockReleaser} may already be registered
        * as a market participant for share trading. Degenerate
        * singleton edges connecting a node to itself are not
        * supported. The Exchange itself an appropriate node
        * for the supply side.
        */
      network.addSellOrder(
         UniqueStockExchange.Instance,
         "Stock Exchange",
         new AbstractResponseFunction() {
            @Override
            public double[] getValue(
               final int[] queries, final TradeOpportunity[] opportunities) {
               return new double[queries.length];
            }
         }
      );
   }
   
   @SuppressWarnings("unchecked")
   public void updatePortfolios(
      final ResourceExchangeAggregator desiredTradesAggregator) {
      ResourceDistributionAlgorithm<StockHolder, StockReleaser>
         assignmentAlgorithm = new CentralPaymentShareDisributionAlgorithm();
      final Map<String, StockReleaser> releaser = new HashMap<String, StockReleaser>();
      releaser.put(stockReleaser.getUniqueName(), stockReleaser);
      assignmentAlgorithm.distributeResources(
         (Map<String, StockHolder>)(Object)
            desiredTradesAggregator.getKeyedConsumers(),
         releaser,
         desiredTradesAggregator.getUnmodifiableKeyedResourceExchanges()
         ).getFirst();
   }
   
   public double getStockPrice() {
      return UniqueStockExchange.Instance.getStockPrice(stockReleaser);
   }
   
   public double getCurrentStockPrice() {
      return getStockPrice();
   }
   
   public double getPreviousStockPrice() {
      return lastStockPrice;
   }
   
   public double getLastChangeInStockPrice() {
      return getStockPrice() - lastStockPrice;
   }
   
   public double getLogStockReturn() {
      return Math.log(getStockPrice() / lastStockPrice);
   }
   
   @Override
   public ClearingMarketInformation getSummaryInformation() {
      final ClearingMarketInformation
         result = new ClearingMarketInformation(this);
      result.add(instrument, 0., lastTotalStockTradeNumberOfShares);
      return result;
   }
   
   public String getStockReleaserName() {
      return stockReleaser.getUniqueName();
   }
   
   /**
     * Generate a {@link ClearingInstrument} for stocks in the specified 
     * {@link StockReleaser}. The resulting {@link ClearingInstrument} is
     * identical to the instrument that would be processed by an instance 
     * of this {@link ClearingStockMarket}.
     * 
     * @param stockReleaser
     *        The {@link StockReleaser} whose shares would be traded over 
     *        the resulting {@link ClearingInstrument}.
     */
   public static ClearingInstrument instrumentFor(final StockReleaser stockReleaser) {
      return new ClearingInstrument(
         stockReleaser.getUniqueName() + " Clearing Stock Market", "Shares");
   }
   
   @Override
   public Set<ClearingInstrument> getInstruments() {
      final Set<ClearingInstrument>
         result = new HashSet<ClearingInstrument>();
      result.add(instrument);
      return result;
   }
}
