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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;

/**
  * A simple forwarding implementation of the {@link StockReturnExpectationFunction}
  * interface.<br><br>
  * 
  * This implementation accepts two existing {@link StockReturnExpectationFunction}
  * objects and computes a linearly-weighted sum of stock returns from these objects.
  * For existing {@link StockReturnExpectationFunction} objects {@code A} and {@code B},
  * the return {@code r} rate computed by this implementation is:
  * 
  * <code><center>
  *   r(A) * w + r(B) * (1 - w)
  * </center></code><br>
  * 
  * Where {@code w} is a fixed customizable linear weight term in the range {@code [0, 1]}.
  * <br><br>
  * 
  * @author phillips
  */
public final class LinearCombinationStockReturnExpectationFunction
   implements StockReturnExpectationFunction {
   
   private final StockReturnExpectationFunction
      first,
      second;
   private final TimeseriesParameter<Double>
      weight;
   
   /**
     * Create a {@link LinearCombinationStockReturnExpectationFunction} with custom
     * parameters.<br><br>
     * 
     * See also {@link LinearCombinationStockReturnExpectationFunction}.
     * 
     * @param first ({@code A})
     *        The first delegate implementation
     * @param second ({@code B})
     *        The second delegate implementation
     * @param weight
     *        The linear combination weight for {@code A} and {@code B}. This argument
     *        must be in the range {@code [0, 1]}.
     */
   @Inject
   public LinearCombinationStockReturnExpectationFunction(
   @Named("LINEAR_COMBINATION_STOCK_RETURN_EXPECTATION_FUNCTION_FIRST_IMPLEMENTATION")
      final StockReturnExpectationFunction first,
   @Named("LINEAR_COMBINATION_STOCK_RETURN_EXPECTATION_FUNCTION_SECOND_IMPLEMENTATION")
      final StockReturnExpectationFunction second,
   @Named("LINEAR_COMBINATION_STOCK_RETURN_EXPECTATION_FUNCTION_WEIGHT")
      final TimeseriesParameter<Double> weight
      ) {
      this.first = Preconditions.checkNotNull(first);
      this.second = Preconditions.checkNotNull(second);
      this.weight = weight;
   }
   
   @Override
   public double computeExpectedReturn(final String stockReleaserName) {
      final double
         firstEvaluation = first.computeExpectedReturn(stockReleaserName),
         secondEvaluation = second.computeExpectedReturn(stockReleaserName),
         currentWeight = weight.get();
      return
         firstEvaluation * currentWeight + secondEvaluation * (1. - currentWeight);
   }
   
   public StockReturnExpectationFunction getFirst() {
      return first;
   }
   
   public StockReturnExpectationFunction getSecond() {
      return second;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Linear Combination Stock Return Expectation Function, first:" + first
            + ", second:" + second + ", weight:" + weight.get() + ".";
   }
}
