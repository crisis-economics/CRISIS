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

import org.apache.commons.math3.exception.NullArgumentException;

/**
  * @author phillips
  */
public final class PartitionedResponseFunction
   implements MarketResponseFunction {
   
   private MarketResponseFunction partitionFunction;
   private BoundedUnivariateFunction univariateTradeDemand;
   
   public PartitionedResponseFunction(
      final MarketResponseFunction partitionFunction,
      final BoundedUnivariateFunction univariateTradeDemand
      ) {
      if(partitionFunction == null ||
         univariateTradeDemand == null)
         throw new NullArgumentException();
      this.partitionFunction = partitionFunction;
      this.univariateTradeDemand = univariateTradeDemand;
   }
   
   @Override
   public double[] getValue(
      final int[] queries,
      final TradeOpportunity[] arguments
      ) {
      final double[] partitionResponse =
         partitionFunction.getValue(queries, arguments);
      final double[] result = new double[queries.length];
      for(int i = 0; i< result.length; ++i)
         result[i] = partitionResponse[i] * 
            univariateTradeDemand.value(arguments[queries[i]].getRate());
      return result;
   }
   
   @Override
   public double getMinimumInDomain() {
      return Math.max(
         partitionFunction.getMinimumInDomain(),
         univariateTradeDemand.getMinimumInDomain()
         );
   }

   @Override
   public double getMaximumInDomain() {
      return Math.min(
         partitionFunction.getMaximumInDomain(),
         univariateTradeDemand.getMaximumInDomain()
         );
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return
         "PartitionedResponseFunction.";
   }
}
