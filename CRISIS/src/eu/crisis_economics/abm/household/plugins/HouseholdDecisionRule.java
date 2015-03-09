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

import eu.crisis_economics.utilities.EmpiricalDistribution;

/**
  * @author phillips
  */
public abstract class HouseholdDecisionRule {
    
    public static final int MEMORY_LENGTH = 100;
    
    private EmpiricalDistribution pastValues;
    
    protected HouseholdDecisionRule(double initialValue) {
        pastValues = new EmpiricalDistribution(MEMORY_LENGTH);
        pastValues.add(initialValue);
    }
    
    protected HouseholdDecisionRule() {
       pastValues = new EmpiricalDistribution(MEMORY_LENGTH);
       pastValues.add(0.);
   }
    
    public static final class HouseholdState implements Cloneable { // Immutable, Builder
        
        public static final double UNKNOWN_VALUE = -Double.MAX_VALUE;
        
        private double 
            lastLabourEmployed          = UNKNOWN_VALUE,
            maximumLabourOffered        = UNKNOWN_VALUE,
            lastLabourUnitWage          = UNKNOWN_VALUE,
            mostRecentConsumptionBudget = UNKNOWN_VALUE,
            previousTotalConsumption    = UNKNOWN_VALUE,
            totalDeposits               = UNKNOWN_VALUE,
            fundAccountValue            = UNKNOWN_VALUE,
            previousFundAccountValue    = UNKNOWN_VALUE,
            householdEquity             = UNKNOWN_VALUE,
            previousDividendReceived    = UNKNOWN_VALUE,
            totalAssets                 = UNKNOWN_VALUE,
            totalLiabilities            = UNKNOWN_VALUE,
            unreservedDeposits          = UNKNOWN_VALUE,
            previousMeanWage            = UNKNOWN_VALUE;
        
        public static class Builder {
            private HouseholdState state = new HouseholdState();
            
            public Builder lastLabourEmployed(double value) {
                state.lastLabourEmployed = value;
                return this;
            }
            
            public Builder maximumLabourOffered(double value) {
                state.maximumLabourOffered = value;
                return this;
            }
            
            public Builder lastLabourUnitWage(double value) {
                state.lastLabourUnitWage = value;
                return this;
            }
            
            public Builder mostRecentConsumptionBudget(double value) {
                state.mostRecentConsumptionBudget = value;
                return this;
            }
            
            public Builder previousTotalConsumption(double value) {
                state.previousTotalConsumption = value;
                return this;
            }
            
            public Builder totalDeposits(double value) {
                state.totalDeposits = value;
                return this;
            }
            
            public Builder fundAccountValue(double value) {
                state.fundAccountValue = value;
                return this;
            }
            
            public Builder previousFundAccountValue(double value) {
                state.previousFundAccountValue = value;
                return this;
            }
            
            public Builder householdEquity(double value) {
                state.householdEquity = value;
                return this;
            }
            
            public Builder previousDividendReceived(double value) {
               state.previousDividendReceived = value;
               return this;
            }
            
            public Builder totalAssets(double value) {
                state.totalAssets = value;
                return this;
            }
            
            public Builder totalLiabilities(double value) {
                state.totalLiabilities = value;
                return this;
            }
            
            public Builder unreservedDeposits(double value) {
                state.unreservedDeposits = value;
                return this;
            }
            
            public Builder previousMeanWage(double value) {
               state.previousMeanWage = value;
               return this;
            }
            
            public HouseholdState build() {
                try {
                    return (HouseholdState)state.clone();
                } catch(Exception e) { return null; }
            }
        }
        
        public double getLastLabourEmployed() {
            return lastLabourEmployed;
        }
        
        public double getMaximumLabourOffered() {
            return maximumLabourOffered;
        }
        
        public double getLastLabourUnitWage() {
            return lastLabourUnitWage;
        }
        
        public double getMostRecentConsumptionBudget() {
            return mostRecentConsumptionBudget;
        }
        
        public double getPreviousTotalConsumption() {
            return previousTotalConsumption;
        }
        
        public double getTotalDeposits() {
            return totalDeposits;
        }
        
        public double getFundAccountValue() {
           return fundAccountValue;
        }
        
        public double getPreviousFundAccountValue() {
           return previousFundAccountValue;
        }
        
        public double getPreviousHouseholdEquity() {
            return householdEquity;
        }
        
        public double getPreviousDividendReceived() {
           return previousDividendReceived;
        }
        
        public double getTotalAssets() {
            return totalAssets;
        }
        
        public double getTotalLiabilities() {
            return totalLiabilities;
        }
        
        public double getUnreservedDeposits() {
            return unreservedDeposits;
        }
        
        public double getPreviousMeanWage() {
           return previousMeanWage;
        }
    }
    
    /** Compute a new value from historical values.
     *  To commit a new value to memory, derived types must (at 
     *  their option) call recordNewValue(double newValue). If not, 
     *  historical data will not be available to select future 
     *  production targets.
     * */
    public abstract double computeNext(HouseholdState currentState);
    
    /** Commit a new value to memory. */
    protected final void recordNewValue(double value) {
        pastValues.add(value);
    }
    
    /** Get the newest stored value. */
    public final double getLastValue() {
        return pastValues.getLastAdded();
    }
    
    public final int memoryUsed() { return pastValues.size(); }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'state machine with 10 units of memory (8 used)'.
     */
    @Override
    public String toString() {
        return "state machine with " + pastValues.maxSize() + 
            " units of memory (" + this.memoryUsed() + " used)";
    }
    
    static { // Assert static pastValues
        if(MEMORY_LENGTH < 0 || Boolean.valueOf(false))
            throw new java.lang.ExceptionInInitializerError(
                "StateMachineHouseholdAttribute.MEMORY_LENGTH < 0");
    }
}
