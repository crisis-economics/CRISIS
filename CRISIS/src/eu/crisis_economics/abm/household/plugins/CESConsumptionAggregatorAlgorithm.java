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
package eu.crisis_economics.abm.household.plugins;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * A CES implementation of the {@link HouseholdConsumptionFunction}
  * interface with consumption budget constraints. This {@link Household}
  * goods consumption decision rule selects goods using a standard
  * CES formula (with exponent Rho).
  * @author phillips
  */
public final class CESConsumptionAggregatorAlgorithm
   implements HouseholdConsumptionFunction {
   
   private double
      rho;
   
   /**
     * Create a {@link CESConsumptionAggregatorAlgorithm} consumption function.
     */
   @Inject
   public CESConsumptionAggregatorAlgorithm(
   @Named("HOUSEHOLD_CONSUMPTION_FUNCTION_CES_RHO")
      final double rho) {   // Immutable
      Preconditions.checkArgument(rho < 1.0,
         "CESConsumptionAggregatorAlgorithm: the value rho >= 1.0 is inadmissible.");
      this.rho = rho;
   }
   
   @Override
   public Double[] calculateConsumption(
      double comsumptionBudget,
      Double[] goodsUnitPrices
      ) {
      if(goodsUnitPrices.length == 0)
         return new Double[0];
      double
         P = 0.,
         __rhoex = rho / (rho - 1.), 
         __rhodiv = 1. / (1. - rho);
      Double[] demandListResult = new Double[goodsUnitPrices.length];
      for (double price : goodsUnitPrices)
         P += Math.pow(price, __rhoex);
      P = Math.pow(P, 1. / __rhoex);
      for (int i = 0; i < demandListResult.length; i++) {
         if(goodsUnitPrices[i] == 0.)
            demandListResult[i] = Double.MAX_VALUE;
         else
            demandListResult[i] = comsumptionBudget / P
               * Math.pow(P / goodsUnitPrices[i], __rhodiv);
      }
      return demandListResult;
   }
   
   /**
     * Get the CES exponent (Rho) for this algorithm.
     */
   public double getRho() {
      return rho;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "CES Consumption Aggregator algorithm, rho = " + rho;
   }
}
