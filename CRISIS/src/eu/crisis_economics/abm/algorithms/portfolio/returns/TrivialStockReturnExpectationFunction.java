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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * A trivial implementation of the {@link StockReturnExpectationFunction}
  * algorithm. This implementation always returns the same estimated
  * return rate.
  * 
  * @author phillips
  */
public class TrivialStockReturnExpectationFunction
   implements StockReturnExpectationFunction {
   
   public final static double
      DEFAULT_FIXED_RETURN_RATE = 0.;
   
   private final double
      fixedReturnRate;
   
   /**
     * Create a {@link TrivialStockReturnExpectationFunction} object with
     * custom parameters.
     * 
     * @param fixedReturnRate
     *        The fixed return rate to return forevermore. This argument
     *        should be non-negative.
     */
   @Inject
   public TrivialStockReturnExpectationFunction(
   @Named("TRIVIAL_STOCK_RETURN_EXPECTATION_FUNCTION_FIXED_RETURN_VALUE")
      final double fixedReturnRate
      ) {
      Preconditions.checkArgument(fixedReturnRate >= 0.);
      this.fixedReturnRate = fixedReturnRate;
   }
   
   /**
     * Create a {@link TrivialStockReturnExpectationFunction} object with
     * default parameters.
     */
   public TrivialStockReturnExpectationFunction() {
      this(DEFAULT_FIXED_RETURN_RATE);
   }
   
   @Override
   public double computeExpectedReturn(final String stockReleaserName) {
      return fixedReturnRate;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Trivial Stock Return Expectation Function, fixed return rate: "
            + fixedReturnRate + ".";
   }
   
}
