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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.TargetValueNoBuyingStockMarketResponseFunction;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;

/**
  * An implementation of the {@link ClearingMarketResponseFactory} interface.
  * This implementation creates a stock instrument {@link MarketResponseFunction}
  * for a {@link StockHolder} {@link Agent} who intends to gradually sell shares
  * it owns in a stock instrument. This implementation specifies:
  * <ul>
  *   <li> The rate {@code m} at which existing shares should be sold
  *        (of {@code N} shares with value {@code V} before the market session,
  *        a value worth {@code (1 - m) * V} of these are to be sold).
  *   <li> The threshold {@code n} below which all remaining shares will
  *        offered for sale immediately.
  * </ul>
  * If the calling {@link Agent} is not a {@link StockHolder}, the
  * {@link MarketResponseFunction} yielded by this factory is {@code null}.
  * 
  * @author phillips
  */
public final class GraduallySellSharesMarketResponseFunctionFactory
   extends SimpleAbstactAgentOperation<MarketResponseFunction> {
   
   public final static double
      DEFAULT_SHARE_SELLER_RATE_AT_WHICH_TO_SELL_SHARES = .2,
      DEFAULT_SHARE_SELLER_THRESHOLD_AT_WHICH_TO_SELL_ALL_SHARES = 1.e-4;
   
   private final static boolean
      VERBOSE_MODE = true;
   
   private final ClearingStockMarket
      market;
   
   private final double
      rateAtWhichToSellShares,
      thresholdAtWhichToSellAllShares;
   
   /**
     * Create a {@link GraduallySellSharesMarketResponseFunctionFactory}
     * with custom parameters. This factory creates a {@link MarketResponseFunction}
     * for a {@link ClearingStockMarket}. The response is to gradually sell existing
     * shares without attempting to buy new shares.
     * 
     * @param market
     *        The {@link ClearingMarket} for which a {@link MarketResponseFunction}
     *        should be generated. This argument must be an instance of a 
     *        {@link ClearingStockMarket}, or else {@link IllegalArgumentException}
     *        is raised.
     * @param rateAtWhichToSellShares
     *        The rate ({@code m}) at which to sell shares already owned. This
     *        argument should be non-negative and not greater than 1.0. In 
     *        each clearing market session, this factory will generate a stock
     *        market response for selling {@code (1-m)*N} shares, where {@code N}
     *        is the number of shares held by the calling {@link Agent}.
     * @param thresholdAtWhichToSellAllShares
     *        The threshold ({@code n}) at which to sell all remaining shares.
     *        If the calling {@link StockHolder} owns fewer than {@code n} shares,
     *        then the market response will detail a desire to sell all outstanding 
     *        shares. This argument should be non-negative.
     */
   @Inject
   public GraduallySellSharesMarketResponseFunctionFactory(
   @Assisted
      final ClearingMarket market,
   @Named("SHARE_SELLER_RATE_AT_WHICH_TO_SELL_SHARES")
      final double rateAtWhichToSellShares,
   @Named("SHARE_SELLER_THRESHOLD_AT_WHICH_TO_SELL_ALL_SHARES")
      final double thresholdAtWhichToSellAllShares
      ) {
      Preconditions.checkNotNull(market);
      Preconditions.checkArgument(
         rateAtWhichToSellShares >= 0. && rateAtWhichToSellShares <= 1.0);
      Preconditions.checkArgument(thresholdAtWhichToSellAllShares >= 0.);
      Preconditions.checkArgument(market instanceof ClearingStockMarket);
      this.market = (ClearingStockMarket) market;
      this.rateAtWhichToSellShares = rateAtWhichToSellShares;
      this.thresholdAtWhichToSellAllShares = thresholdAtWhichToSellAllShares;
   }
   
   /**
     * Create a {@link GraduallySellSharesMarketResponseFunctionFactory} with
     * default parameters.
     */
   public GraduallySellSharesMarketResponseFunctionFactory(final ClearingMarket market) {
      this(
         market,
         DEFAULT_SHARE_SELLER_RATE_AT_WHICH_TO_SELL_SHARES,
         DEFAULT_SHARE_SELLER_THRESHOLD_AT_WHICH_TO_SELL_ALL_SHARES
         );
   }
   
   @Override
   public MarketResponseFunction operateOn(Agent agent) {
      if(!(agent instanceof StockHolder))
         return null;
      
      final StockHolder
         participant = (StockHolder) agent;
      final double
         numberOfSharesOwned = participant.getNumberOfSharesOwnedIn(market.getStockReleaserName()),
         stockPrice = UniqueStockExchange.Instance.getStockPrice(market.getStockReleaserName()),
         targetValue =
            (numberOfSharesOwned < thresholdAtWhichToSellAllShares) ?
               0. : stockPrice * numberOfSharesOwned * rateAtWhichToSellShares;
      
      if(numberOfSharesOwned == 0.)
         return null;
      
      if(VERBOSE_MODE)
         System.out.printf(
            agent.getClass().getSimpleName() + ": desired value of shares: %16.10g, "
          + "owned shares: %16.10g\n",
            targetValue, numberOfSharesOwned
            );
      
      return new TargetValueNoBuyingStockMarketResponseFunction(
         numberOfSharesOwned,
         targetValue
         );
   }
   
   public ClearingMarket getMarket() {
      return market;
   }
   
   public double getRateAtWhichToSellShares() {
      return rateAtWhichToSellShares;
   }
   
   public double getThresholdAtWhichToSellAllShares() {
      return thresholdAtWhichToSellAllShares;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Gradually Sell Shares Stock Market Response Factory," +
             "market: " + market.getMarketName() + ", rate at which to sell shares: " +
            rateAtWhichToSellShares + ", threshold for sell-all: " 
             + thresholdAtWhichToSellAllShares + ".";
   }
}
