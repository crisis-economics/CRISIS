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
  * A {@link HouseholdDecisionRule} for wage ask prices (per unit labour).
  * This algorithm returns a fixed, customizable, wage ask price per unit
  * labour.
  * @author phillips
  */
public final class FixedWageExpectationAlgorithm extends HouseholdDecisionRule {
   
   private double
      expectedLabourWage;
   
   @Inject
   public FixedWageExpectationAlgorithm(
   @Named("FIXED_WAGE_ASK_PRICE_ALGORITHM_LABOUR_WAGE_PER_UNIT")
      final double expectedLabourWage
      ) {
      super(expectedLabourWage);
      Preconditions.checkArgument(expectedLabourWage >= 0.,
         "FixedWageExpectationAlgorithm: labour wage must " + "be non-negative.");
      this.expectedLabourWage = expectedLabourWage;
   }
   
   @Override
   public double computeNext(HouseholdState state) {
      return expectedLabourWage;
   }
   
   /**
     * Get the fixed labour wage (wage per unit labour).
     */
   public double getFixedLabourWage() {
      return expectedLabourWage;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Fixed Wage Ask Price Household Decision Rule, fixed wage = " 
         + getFixedLabourWage();
   }
}
