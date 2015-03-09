/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
  * A {@link HouseholdDecisionRule} for household consumption budgets 
  * (maximum spending on consumption).<br><br>
  * 
  * This budget allocation algorithm returns a consumption budget of the form 
  * B = C*(W**A) where B is the consumption budget, W is household wealth, and
  * C, A are fixed customizable parameters. C is a multiplier called the
  * 'Consumption Propensity'. A is a the 'Wealth Exponent'. The terms C and A are
  * constructor arguments. It is expected that both C and A are nonnegative and
  * not greater than 1 (Ie. B <= W).
  * @author phillips
  */
public final class ConsumptionPropensityBudgetAlgorithm 
    extends HouseholdDecisionRule {
    
    private double
       consumptionPropensity,
       wealthExponent;          // Default value 1.0.
    
    public ConsumptionPropensityBudgetAlgorithm() {
    	this(0);
	}
    
    /**
      * This constructor behaves as {@link #ConsumptionPropensityBudgetAlgorithm(double, 
      * double)}, using the value 1.0 for the second parameter.
      * @param consumptionPropensity
      */
    public ConsumptionPropensityBudgetAlgorithm(
       final double consumptionPropensity) {
       this(consumptionPropensity, 1.0);
    }
    
    /**
      * Create a {@link ConsumptionPropensityBudgetAlgorithm}.
      * @param consumptionPropensity
      *        A linear multiplier, converting (wealth^exponent) to cash. This
      *        argument must be in the range (0, 1].
      * @param wealthExponent
      *        The exponent of wealth in the formula Budget = C*(Wealth**Exponent).
      *        This argument must be in the range [0, 1].
      */
    @Inject
    public ConsumptionPropensityBudgetAlgorithm(
    @Named("CONSUMPTION_PROPENSITY_MULTIPLIER") 
       double consumptionPropensity,
    @Named("CONSUMPTION_PROPENSITY_WEALTH_EXPONENT") 
       double wealthExponent
        ) {
       super(0.);
       Preconditions.checkArgument(
          wealthExponent >= 0.,
          "ConsumptionPropensityBudgetAlgorithm: wealth exponent (value " + wealthExponent + 
          ") has an illegal value. Expected 0 <= wealthExponent");
       Preconditions.checkArgument(
           consumptionPropensity >= 0. && consumptionPropensity <= 1.0,
           "ConsumptionPropensityBudgetAlgorithm: consumption " +
           "propensity must be in the range [0, 1] inclusive.");
       this.consumptionPropensity = consumptionPropensity;
       this.wealthExponent = wealthExponent;
    }
    
    @Override
    public double computeNext(HouseholdState state) {
        double
           wealth = state.getUnreservedDeposits(),
           result = consumptionPropensity * Math.pow(Math.max(wealth, 0.), wealthExponent);
        result = Math.min(result, wealth);
        super.recordNewValue(result);
        return result;
    }
    
    /** 
      * Get the consumption propensity.
      */
    public double getConsumptionPropensity() { 
       return consumptionPropensity; 
    }
    
    /** 
      * Get the wealth exponent factor.
      */
    public double getWealthExponent() {
       return wealthExponent;
    }
    
    /**
     /**
	 * @param consumptionPropensity the consumptionPropensity to set
	 */
	public void setConsumptionPropensity(double consumptionPropensity) {
		this.consumptionPropensity = consumptionPropensity;
	}
    
    /**
      * Returns a brief description of this object. The exact details of the
      * string are subject to change, and should not be regarded as fixed.
      */
    @Override
    public String toString() {
       return
          "Consumption Propensity Budget Algorithm: " + 
          "propensity: " + getConsumptionPropensity() + ", " +
          "wealth exponent: " + getWealthExponent() + ".";
    }
}
