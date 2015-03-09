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
package eu.crisis_economics.abm.algorithms.portfolio.returns;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.simulation.Simulation;

/****************************************************************
 * <p>
 * Implementation of a trend-following expectation
 * function, where expected return
 * </p>
 * <code>
 * e = (p_t2 - p_t1)/(p_t1*(t2-t1))
 * </code>
 * <p>
 * where
 * <p><ul>
 * <li>t2 is the time now</li>
 * <li>t1 is now - a lag time</li>
 * <li>p_t2 is the price at t2</li>
 * <li>p_t1 is the price at t1</li>
 * </ul>
 ***************************************************************/
public class TrendFollowerStockReturnExpectationFunction
   implements StockReturnExpectationFunction {
   
   public final static double
      DEFAULT_TREND_FOLLOWER_STOCK_RETURN_EXPECTATION_LAG = 2.;
   
   private double
      lag;
   
   /**
     * Create a {@link TrendFollowerStockReturnExpectationFunction} return
     * estimation algorithm with custom parameters.
     * 
     * @param lag
     *        The lag for this trend-following process. This argument should
     *        be non-negative.
     */
   @Inject
   public TrendFollowerStockReturnExpectationFunction(
   @Named("TREND_FOLLOWER_STOCK_RETURN_EXPECTATION_LAG")
      final double lag
      ) {
      Preconditions.checkArgument(lag >= 0.);
      this.lag = lag;
   }
   
   /**
     * Create a {@link TrendFollowerStockReturnExpectationFunction} return
     * estimation algorithm with default parameters.
     */
   public TrendFollowerStockReturnExpectationFunction() {
      this(DEFAULT_TREND_FOLLOWER_STOCK_RETURN_EXPECTATION_LAG);
   }
   
   @Override
   public double computeExpectedReturn(String stockName) {
       TimestampedPrice
          currentPrice = new TimestampedPrice(),
          oldPrice;
       
       final StockReleaser releaser =
          UniqueStockExchange.Instance.getStockReleaser(stockName);
       
       currentPrice.time  = Simulation.getSimState().schedule.getTime();
       currentPrice.price = releaser.getMarketValue();
       
       recordPrice(stockName, currentPrice);
       oldPrice = getLaggedPrice(stockName, currentPrice.time);
       if(currentPrice.time == oldPrice.time) {
           return(0.0);    // expect no price change if we have no historic info.
       }
       return((currentPrice.price - oldPrice.price)/
           (oldPrice.price*(currentPrice.time - oldPrice.time)*MASON_TIME_UNIT));
   }
   
   public void setLag(double l) {
       lag = l;
   }
   
   public double getLag() {
       return(lag);
   }
   
   /***
    * Adds the supplied price of the supplied firm to the historic record of prices
    * 
    * @param firm      The firm whose price is to be recorded
    * @param price     The quoted price, stamped with the time of the quote
    */
   private void recordPrice(final String stockName, TimestampedPrice price) {
       if(!historicPrices.containsKey(stockName))
           historicPrices.put(stockName, new LinkedList<TimestampedPrice>());
       historicPrices.get(stockName).offer(price);
   }

   /***
    * returns the price of the supplied firm at time t-lag.
    * Requires that at least one price has been recorded
    * for this firm (i.e. recordPrice first).
    * 
    * @param firm      The firm to get the price for
    * @param timeNow   The current simulated time
    */
   private TimestampedPrice getLaggedPrice(String firm, double timeNow) {
       double laggedTime = timeNow - lag;
       Queue<TimestampedPrice> priceQueue = historicPrices.get(firm);
       TimestampedPrice oldPrice = priceQueue.peek();
       while(oldPrice.time < laggedTime) {
           oldPrice = priceQueue.poll();
       }
       return(oldPrice);
   }
   
   
   private class TimestampedPrice {
       public double time;
       public double price;
   }
   
   private Map<String, Queue<TimestampedPrice> > 
      historicPrices = new HashMap<String, Queue<TimestampedPrice> >();
   
   private static final double MASON_TIME_UNIT = 1./4.; // Number of years in a mason time 
                                                        // unit TODO: find this out
}
