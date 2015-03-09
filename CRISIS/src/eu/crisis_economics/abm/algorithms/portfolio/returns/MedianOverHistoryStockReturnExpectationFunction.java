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
import java.util.Map;

import kcl.waterloo.math.ArrayUtils;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * A forwarding class, supplementing an existing {@link 
  * StockReturnExpectationFunction} object with a "median value over 
  * history" feature.
  * 
  * This class accepts an existing {@link StockReturnExpectationFunction}
  * object and keeps track of its expected return values in a circular buffer. 
  * When {@link computeExpectedReturn} is called, the median of all previous
  * returns in the buffer is computed.
  * 
  * The amount of memory used by this class to compute medians, by default,
  * is {@link DEFAULT_MEMORY_LENGTH_SLOTS}.
  * 
  * @author phillips
  */
public final class MedianOverHistoryStockReturnExpectationFunction
   implements StockReturnExpectationFunction {
   
   final static public int
      DEFAULT_MEMORY_LENGTH_SLOTS = 10;
   
   final StockReturnExpectationFunction
      expectationFunction;
   
   final Map<String, CircularFifoBuffer<Double>>
      memory;
   
   final int
      sizeOfMemory;
   
   /**
     * Create a {@link MedianOverHistoryStockReturnExpectationFunction}
     * object with a custom memory length (as specified by the argument).
     * 
     * @param The {@link StockReturnExpectationFunction} to which to 
     *        apply the moving median.
     * @param sizeOfMemory
     *        The length of the circular buffer over which to compute
     *        moving median returns. This argument should be strictly
     *        positive.
     */
   @Inject
   public MedianOverHistoryStockReturnExpectationFunction(
   @Named("MEDIAN_OVER_HISTORY_PRIMITIVE_STOCK_RETURN_EXPECTATION_FUNCTION")
      StockReturnExpectationFunction expectationFunction,
   @Named("MEDIAN_OVER_HISTORY_STOCK_RETURN_EXPECTATION_FUNCTION_MEMORY_LENGTH")
      final int sizeOfMemory
      ) {
      Preconditions.checkNotNull(expectationFunction);
      Preconditions.checkArgument(sizeOfMemory > 0);
      this.expectationFunction = expectationFunction;
      this.memory = new HashMap<String, CircularFifoBuffer<Double>>();
      this.sizeOfMemory = sizeOfMemory;
   }
   
   /**
    * Create a {@link MedianOverHistoryStockReturnExpectationFunction}
    * object with a default memory length.
    */
   public MedianOverHistoryStockReturnExpectationFunction(
      StockReturnExpectationFunction expectationFunction
      ) {
      this(expectationFunction, DEFAULT_MEMORY_LENGTH_SLOTS);
   }
   
   @Override
   public double computeExpectedReturn(final String stockName) {
      if(!memory.containsKey(stockName))
         memory.put(
            stockName,
            new CircularFifoBuffer<Double>(sizeOfMemory)
            );
      final double
         returnEstimate = expectationFunction.computeExpectedReturn(stockName);
      memory.get(stockName).add(returnEstimate);
      return medianOfSeries(
         ArrayUtils.asDouble(memory.get(stockName).toArray(new Double[0]))
         );
   }
   
   private double medianOfSeries(final double[] series) {
      Median median = new Median();
      median.setData(series);
      return median.evaluate(50);
   }
   
   /**
     * Get the underlying expectation function, over which median
     * returns are computed.
     */
   public StockReturnExpectationFunction getExpectationFunction() {
      return expectationFunction;
   }
   
   /**
     * Get the length of memory used to compute medians.
     */
   public int getSizeOfMemory() {
      return sizeOfMemory;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Median-Over-History Stock Return Expectation Function, "
          + "expectation function: " + expectationFunction + ", size of "
          + "memory: " + sizeOfMemory + ".";
   }
}