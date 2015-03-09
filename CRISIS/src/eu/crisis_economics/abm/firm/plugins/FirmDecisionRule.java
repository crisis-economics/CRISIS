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
package eu.crisis_economics.abm.firm.plugins;

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.utilities.EmpiricalDistribution;

/**
  * @author phillips
  */
public abstract class FirmDecisionRule {
    
    public static final int MEMORY_LENGTH = 100;
    
    private EmpiricalDistribution pastValues;
    
    public FirmDecisionRule(double initialValue) {
        pastValues = new EmpiricalDistribution(MEMORY_LENGTH);
        pastValues.add(initialValue);
    }
    
    public static final class FirmState implements Cloneable { // Immutable, Builder
        
        public static final double UNKNOWN_DOUBLE_VALUE = -Double.MAX_VALUE;
        public static final int UNKNOWN_INT_VALUE = -Integer.MAX_VALUE;
        
        private FirmState() { }
        
        private double 
            labourCurrentlyEmployed           = UNKNOWN_DOUBLE_VALUE,
            labourEmploymentTarget            = UNKNOWN_DOUBLE_VALUE,
            lastRecordedLabourWage            = UNKNOWN_DOUBLE_VALUE,
            targetGoodsProductionNow          = UNKNOWN_DOUBLE_VALUE,
            targetGoodsProductionLastCycle    = UNKNOWN_DOUBLE_VALUE,
            effectiveGoodsProduction          = UNKNOWN_DOUBLE_VALUE,
            effectiveGoodsProductionLastCycle = UNKNOWN_DOUBLE_VALUE,
            currentGoodsUnitPrice             = UNKNOWN_DOUBLE_VALUE,
            totalUnsoldGoodsThisCycle         = UNKNOWN_DOUBLE_VALUE,
            assetsTotalValue                  = UNKNOWN_DOUBLE_VALUE,
            totalLiquidityDemandNow           = UNKNOWN_DOUBLE_VALUE,
            totalLiquidityDemandLastCycle     = UNKNOWN_DOUBLE_VALUE,
            lastRecordedProfit                = UNKNOWN_DOUBLE_VALUE,
            loanRepaymentLiability            = UNKNOWN_DOUBLE_VALUE,
            unallocatedCash                   = UNKNOWN_DOUBLE_VALUE;
        
        private String
           goodsType = "",
           firmName = "";
        
        private SimpleGoodsMarket goodsMarket;
        
        public static class Builder {
            private FirmState state = new FirmState();
            
            public Builder labourCurrentlyEmployed(double value) {
                state.labourCurrentlyEmployed = value;
                return this;
            }
            
            public Builder labourEmploymentTarget(double value) {
                state.labourEmploymentTarget = value;
                return this;
            }
            
            public Builder targetGoodsProductionNow(double value) {
                state.targetGoodsProductionNow = value;
                return this;
            }
            
            public Builder targetGoodsProductionLastCycle(double value) {
               state.targetGoodsProductionLastCycle = value;
               return this;
            }
            
            public Builder effectiveGoodsProduction(double value) {
                state.effectiveGoodsProduction = value;
                return this;
            }
            
            public Builder effectiveGoodsProductionLastCycle(double value) {
               state.effectiveGoodsProductionLastCycle = value;
               return this;
           }
            
            public Builder currentGoodsUnitPrice(double value) {
                state.currentGoodsUnitPrice = value;
                return this;
            }
            
            public Builder totalUnsoldGoodsThisCycle(double value) {
                state.totalUnsoldGoodsThisCycle = value;
                return this;
            }
            
            public Builder assetsTotalValue(double value) {
                state.assetsTotalValue = value;
                return this;
            }
            
            public Builder totalLiquidityDemandNow(double value) {
                state.totalLiquidityDemandNow = value;
                return this;
            }
            
            public Builder totalLiquidityDemandLastCycle(double value) {
               state.totalLiquidityDemandLastCycle = value;
               return this;
            }
            
            public Builder lastRecordedProfit(double value) {
                state.lastRecordedProfit = value;
                return this;
            }
            
            public Builder lastRecordedLabourWage(double value) {
                state.lastRecordedLabourWage = value;
                return this;
            }
            
            public Builder goodsType(final String value) {
                state.goodsType = value;
                return this;
            }
            
            public Builder firmName(final String value) {
               state.firmName = value;
               return this;
            }
            
            public Builder goodsMarket(SimpleGoodsMarket goodsMarket) {
               if(goodsMarket == null)
                  throw new NullArgumentException();
               state.goodsMarket = goodsMarket;
               return this;
            }
            
            public Builder loanRepaymentLiability(double value) {
               state.loanRepaymentLiability = value;
               return this;
            }
            
            public Builder unallocatedCash(double value) {
               state.unallocatedCash = value;
               return this;
            }
            
            public FirmState build() {
                try {
                    return (FirmState) state.clone();
                } catch(Exception e) { return null; }
            } 
        }
        
        public double getLabourCurrentlyEmployed() {
            return labourCurrentlyEmployed;
        }
        
        public double getLabourEmploymentTarget() {
            return labourEmploymentTarget;
        }
        
        public double getTargetGoodsProductionNow() {
            return targetGoodsProductionNow;
        }
        
        public double getTargetGoodsProductionLastCycle() {
           return targetGoodsProductionLastCycle;
        }
        
        public double getEffectiveGoodsProduction() {
            return effectiveGoodsProduction;
        }
        
        public double getEffectiveGoodsProductionLastCycle() {
           return effectiveGoodsProductionLastCycle;
        }
        
        public double getCurrentGoodsUnitPrice() {
            return currentGoodsUnitPrice;
        }
        
        public double getTotalUnsoldGoodsThisCycle() {
            return totalUnsoldGoodsThisCycle;
        }
        
        public double getAssetsTotalValue() {
            return assetsTotalValue;
        }
        
        public double getTotalLiquidityDemandNow() {
            return totalLiquidityDemandNow;
        }
        
        public double getTotalLiquidityDemandLastCycle() {
           return totalLiquidityDemandLastCycle;
        }
        
        public double getLastRecordedProfit() {
            return lastRecordedProfit;
        }
        
        public double getLastRecordedLabourWage() {
            return lastRecordedLabourWage;
        }
        
        public String getGoodsType() {
            return goodsType;
        }
        
        public String getFirmName() {
           return firmName;
        }
        
        public SimpleGoodsMarket getGoodsMarket() {
           return goodsMarket;
        }
        
        public double getLoanLiability() {
           return loanRepaymentLiability;
        }
        
        public double getUnallocatedCash() {
           return unallocatedCash;
        }
    }
    
    /** Compute a new value from historical values.
     *  To commit a new value to memory, derived types must (at 
     *  their option) call recordNewValue(double newValue). If not, 
     *  historical data will not be available to select future 
     *  production targets.
     * */
    public abstract double computeNext(FirmState currentState);
    
    /** Commit a new value to memory. */
    protected final void recordNewValue(double value) {
        pastValues.add(value);
    }
    
    /** Get the newest stored value. */
    public final double getLastValue() {
        return pastValues.getLastAdded();
    }
    
    /** Get the average over recorded values. */
    public final double meanProductionTargetOverHistory() {
        return pastValues.meanOverHistory();
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
                "StateMachineFirmAttribute.MEMORY_LENGTH < 0");
    }
}
