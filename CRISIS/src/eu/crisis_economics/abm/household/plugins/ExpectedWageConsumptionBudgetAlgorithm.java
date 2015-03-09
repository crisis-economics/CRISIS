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

import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;

/**
  * A {@link HouseholdDecisionRule} for household consumption budget sizes.
  * This rule is based on expected effective household income.<br><br>
  * 
  * This consumption budget algorithm queries the labour market for
  * the employment proportion e ~ [0, 1] (e=1 implies 0% unemployment)
  * and forms the expected wage Y*e, where Y is wage per cycle assuming
  * full employment. The final consumption budget is C = Y*e.
  * @author phillips
  */
public final class ExpectedWageConsumptionBudgetAlgorithm 
   extends HouseholdDecisionRule {
   
   private final double
      expectedWageAssumingFullEmployment;
   private final SimpleLabourMarket
      labourMarket;
   
   /**
     * Create a {@link ExpectedWageConsumptionBudgetAlgorithm}.
     * @param expectedIncomeAssumingFullEmployment
     *        The expected {@link Household} income, per cycle, assuming full
     *        employment.
     * @param labourMarket
     *        The market on which the {@link Household} offers its labour.
     */
   @Inject
   public ExpectedWageConsumptionBudgetAlgorithm(
   @Named("HOUSEHOLD_EXPECTED_INCOME_ASSUMING_FULL_EMPLOYMENT")
      final double expectedIncomeAssumingFullEmployment,
      final SimpleLabourMarket labourMarket
      ) {
      Preconditions.checkArgument(expectedIncomeAssumingFullEmployment >= 0.);
      Preconditions.checkNotNull(labourMarket);
      this.expectedWageAssumingFullEmployment = expectedIncomeAssumingFullEmployment;
      this.labourMarket = labourMarket;
   }
   
   @Override
   public double computeNext(final HouseholdState state) {
      final double
         lastTotalLabourSupply = labourMarket.getLastLabourTotalSupply(),
         lastTotalEmployment = labourMarket.getLastTotalEmployedLabour(),
         e = (lastTotalLabourSupply == 0. ? 0. : lastTotalEmployment/lastTotalLabourSupply),
         result = expectedWageAssumingFullEmployment * e;
      super.recordNewValue(result);
      return result;
  }
    
    /** 
      * Get the expected wage assuming full employment.
      */
    public double getExpectedWageAssumingFullEmployment() {
       return expectedWageAssumingFullEmployment;
    }
    
    /**
      * Returns a brief description of this object. The exact details of the
      * string are subject to change, and should not be regarded as fixed.
      */
    @Override
    public String toString() {
       return
          "Expected Wage Consumption Budget Algorithm, " + 
          "expected wage: " + expectedWageAssumingFullEmployment;
    }
}
