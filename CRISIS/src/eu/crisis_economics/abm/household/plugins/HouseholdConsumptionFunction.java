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
package eu.crisis_economics.abm.household.plugins;

/**
  * An interface for goods consumption budget decision algorithms.
  * 
  * @author phillips
  */
public abstract interface HouseholdConsumptionFunction {
    
   /**
     * Compute input demands given a consumption budget and an 
     * array of prices per unit goods.<br><br>
     * 
     * This method must return <b>an array of quantities to buy</b>
     * not <b>an array of budgets</b> for each goods type.
     * 
     * @param comsumptionBudget
     *        The maximum budget to spend. This argument should be
     *        non-negative.
     * @param goodsUnitPrices
     *        An array of prices per unit goods to buy.
     */
   public Double[] calculateConsumption(
       double comsumptionBudget,
       Double[] goodsUnitPrices
       );
}
