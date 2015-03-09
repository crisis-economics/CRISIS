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

/**
  * An implementation of the inverse exponential Intensity of Choice partition function.
  * For arguments (r_1, r_2, r_3 ... r_N), the jth coordinate of this partition
  * function has value <code>exp(+r_j * F) / \sum_{j = 1}^N exp(+r_j * F)</code>
  * where <code>F</code> is a customizable normalization factor.
  * 
  * @author phillips
  */
public final class InverseExpIOCPartitionFunction
   extends AbstractResponseFunction {
   
   private final static double
      DEFAULT_NORMALIZATION = 1.0;
   
   private final double
      normalization;
   
   /**
     * Create a {@link InverseExpIOCPartitionFunction} with default parameters.
     */ 
   public InverseExpIOCPartitionFunction() {
      this(DEFAULT_NORMALIZATION);
   }
   
   /**
     * Create a {@link InverseExpIOCPartitionFunction} with custom parameters.
     * 
     * @param normalization
     *        The normalization factor (a multiplier) to use for exponential
     *        expressions evaluated by this algorithm. This argument must be
     *        non-negative.
     */
   public InverseExpIOCPartitionFunction(final double normalization) {
      Preconditions.checkArgument(normalization >= 0.);
      this.normalization = normalization;
   }
   
   public double[] getValue(int[] queries, TradeOpportunity[] arguments)  {
      double
         summand = 0.;
      for(int i = 0; i< arguments.length; ++i)
         summand += Math.exp(-arguments[i].getRate() * normalization);
      final double[] result = new double[arguments.length];
      for(int i = 0; i< queries.length; ++i)
         result[i] = Math.exp(-arguments[queries[i]].getRate() * normalization) / summand;
      return result;
   }
}
