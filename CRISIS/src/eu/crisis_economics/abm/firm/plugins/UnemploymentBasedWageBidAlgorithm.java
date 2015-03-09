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

import java.util.Random;

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author      JKP
 * @category    Firms
 * @see         
 * @since       1.0
 * @version     1.0
 */
public final class UnemploymentBasedWageBidAlgorithm 
    extends FirmDecisionRule {
    
    public static interface EmploymentProportionCallback {
        /** Get the total employment. */
        public double getTotalEmployment();
        /** Get the total market demand for labour. */
        public double getTotalMarketLabourDemand();
        /** Get the total market supply of labour. */
        public double getTotalMarketLabourSupply();
        /** Get the (trade-weighted) mean labour unit bid price. */
        public double getMeanLabourBidPrice();
    }
    
    private final double
        adaptationRate,
        minimumWage;
    
    private Random dice;
    
    private EmploymentProportionCallback marketCallback;
    
    public UnemploymentBasedWageBidAlgorithm(
        double initialLabourWage,
        double wageAdaptationRate,
        double minimumWage,
        EmploymentProportionCallback marketCallback
        ) {
        super(initGuard(initialLabourWage));
        if(marketCallback == null)
            throw new NullArgumentException();
        this.adaptationRate = 1.e-2; // wageAdaptationRate;
        this.minimumWage = minimumWage;
        this.dice = new Random(
            Simulation.getSimState().random.nextLong());
        this.marketCallback = marketCallback;
    }
    
    static private double initGuard(double initialValue) {
        if(initialValue < 0)
        throw new IllegalArgumentException(
            "UnemploymentBasedWageBidAlgorithm: labour wage is negative.");
        return initialValue;
    }
    
    @Override
    public double computeNext(FirmState state) {
        double
            lastWageProposition = super.getLastValue(),
            nextWageProposition = lastWageProposition,
            sAgg = marketCallback.getTotalMarketLabourSupply(),
            dAgg = marketCallback.getTotalMarketLabourDemand();
        if(sAgg > dAgg)
            nextWageProposition *= 
               1 - adaptationRate * dice.nextDouble();
        else
            nextWageProposition *= 
               1 + adaptationRate * dice.nextDouble();
        nextWageProposition += dice.nextGaussian() * minimumWage * .02;
        nextWageProposition = Math.max(minimumWage, nextWageProposition);
        super.recordNewValue(nextWageProposition);
        return nextWageProposition;
    }
    
    /** Get the (fixed) labour wage. */
    public double getLastLabourWage() {
        return super.getLastValue();
    }
    
    /** Get the (fixed) minumum wage. */
    public double getMinimumWage() {
       return minimumWage;
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'unemployment based labour wage bid algorithm,
     * minimum wage = ' + getMinimumWage()
     */
    @Override
    public String toString() {
        return
           "unemployment based labour wage bid algorithm, " +
           "minimum wage = " + getMinimumWage();
    }
}
