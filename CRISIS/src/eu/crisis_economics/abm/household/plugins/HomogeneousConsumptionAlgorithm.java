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

import com.google.inject.Inject;

/**
  * A simple implementation of the {@link HouseholdConsumptionFunction} interface.<br><br>
  * This implementation treats all ({@code N}) types of goods equally, irrespective of their
  * price. For a consumption budget of size {@code £B}, {@code £(B/N)} worth of 
  * each good is desired.
  * 
  * @author phillips
  */
public final class HomogeneousConsumptionAlgorithm implements HouseholdConsumptionFunction {
   
   @Inject
   public HomogeneousConsumptionAlgorithm() { }   // Stateless
   
   /**
     * Compute the desired consumption (cash value) for a set of goods given their
     * unit prices and the maximum size of the consumption budget. The same amount
     * of cash is allocated to each type of goods.
     * 
     * @param comsumptionBudget
     *        The maxium cash value of the consumption budget. This argument should
     *        be non-negative. In the event that this argument is negative, it will
     *        be silently trimmed to zero.
     * @param goodsUnitPrices
     *        An array of goods unit prices (cost per unit good to buy). This method
     *        will return a {@link Double}{@code []} of the same dimension as this
     *        argument.
     */
   @Override
   public Double[] calculateConsumption(
      final double comsumptionBudget,
      final Double[] goodsUnitPrices
      ) {
      if(goodsUnitPrices.length == 0)
         return new Double[0];
      final double
         homogeneousConsumption = comsumptionBudget / goodsUnitPrices.length;
      final Double[]
         result = new Double[goodsUnitPrices.length];
      for(int i = 0; i< result.length; ++i)
         result[i] = homogeneousConsumption / goodsUnitPrices[i];
      return result;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Homogeneous Consumption Function.";
   }
}
