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
package eu.crisis_economics.abm.algorithms.portfolio.returns;

import java.util.Map;

import org.apache.commons.collections15.map.HashedMap;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * An implementation of the {@link StockReturnExpectationFunction} interface. This implementation
  * provides an exponentially smoothed stock return process:
  * 
  *<code><center>
  * r*(t) = a * r(t-1) + (1 - a) * r*(t-1) &nbsp;&nbsp; [r*(0) = r(0)]
  *</center></code><br>
  *
  * where <code>r*(t)</code> is the stock return estimate formulated by this algorithm,
  * <code>a</code> is the exponential smoothing factor, and <code>r(t)</code> is the stock return
  * formulated by an algorithm to which this class delegates. <br><br>
  * 
  * @author phillips
  */
public final class ExponentialSmoothingStockReturnExpectationFunction
   implements StockReturnExpectationFunction {
   
   private final static double
      DEFAULT_ALPHA = 0.9;
   
   private double
      alpha = DEFAULT_ALPHA;
   
   final StockReturnExpectationFunction
      expectationFunction;
   
   /**
     * Create a {@link ExponentialSmoothingStockReturnExpectationFunction} object with
     * custom parameters.<br><br>
     * 
     * See also {@link ExponentialSmoothingStockReturnExpectationFunction}.
     * 
     * @param expectationFunction (<code>r(t)</code>) <br>
     *        The {@link StockReturnExpectationFunction} whose values are to be subject to
     *        an exponential smoothing process.
     * @param alpha (<code>a</code>) <br>
     *        The exponential smoothing parameter to use. it is expected that this argument
     *        should lie in the range <code>[0, 1]</code>, but this is not a requirement.
     */
   @Inject
   public ExponentialSmoothingStockReturnExpectationFunction(
   @Named("EXPONENTIAL_SMOOTHING_STOCK_RETURN_FUNCTION_DELEGATE")
      final StockReturnExpectationFunction expectationFunction,
   @Named("EXPONENTIAL_SMOOTHING_STOCK_RETURN_EXPECTATION_FUNCTION_ALPHA")
      final double alpha
      ) {
      this.expectationFunction = Preconditions.checkNotNull(expectationFunction);;
      this.alpha = alpha;
   }
   
   /**
    * Create a {@link ExponentialSmoothingStockReturnExpectationFunction} with default
    * parameters.
    */
   public ExponentialSmoothingStockReturnExpectationFunction(
      StockReturnExpectationFunction expectationFunction
      ) {
      this(expectationFunction, DEFAULT_ALPHA);
   }
   
   private class ExponentiallySmoothedStockReturn {
      
      private double
         lastSmoothedValue,
         lastObservedValue;
      private boolean
         firstCycle = true,
         computedThisCycle = false;
      private String
         stockName;
      
      ExponentiallySmoothedStockReturn(
         final String stockName
         ) {
         this.stockName = stockName;
         
         scheduleSelf();
      }
      
      public double computeNext() {
         if(!computedThisCycle) {
            final double
               r = expectationFunction.computeExpectedReturn(this.stockName);
            if(firstCycle) {
               lastSmoothedValue = r;
               lastObservedValue = r;
               firstCycle = false;
            }
            else
               lastSmoothedValue =
                  alpha * lastObservedValue + (1. - alpha) * lastSmoothedValue;
            lastObservedValue = r;
            computedThisCycle = true;
         }
         return lastSmoothedValue; 
      }
      
      private void scheduleSelf() {
         Simulation.repeat(this, "flushMemories", NamedEventOrderings.AFTER_ALL);
      }
      
      @SuppressWarnings("unused")   // Scheduled
      private void flushMemories() {
         this.computedThisCycle = false;
      }
   }
   
   private final Map<String, ExponentiallySmoothedStockReturn>
      records = new HashedMap<String, ExponentiallySmoothedStockReturn>();
   
   @Override
   public double computeExpectedReturn(final String stockName) {
      if(!records.containsKey(stockName))
         records.put(stockName, new ExponentiallySmoothedStockReturn(stockName));
      return records.get(stockName).computeNext();
   }
   
   /**
     * Get the underlying expectation function, over which median
     * returns are computed.
     */
   public StockReturnExpectationFunction getExpectationFunction() {
      return expectationFunction;
   }
   
   /**
     * Get the exponential smoothing parameter.
     */
   public double getAlpha() {
      return alpha;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Exponential Smoothing Stock Return Expectation Function, "
          + "expectation function: " + expectationFunction + ", smoothing factor: " 
          + alpha + ".";
   }
}