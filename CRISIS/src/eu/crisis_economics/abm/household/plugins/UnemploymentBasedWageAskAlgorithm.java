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

import java.util.Random;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author      JKP
 * @category    Firms
 * @see         
 * @since       1.0
 * @version     1.0
 */
public final class UnemploymentBasedWageAskAlgorithm 
    extends HouseholdDecisionRule {
    
    public static interface EmploymentProportionCallback {
        /** Get total employment / maximum labour supply. */
        public double getEmploymentProportion();
    }
    
    private double
       wagePerturbation,
       minimumWage;
    
    private Random dice;
    
    private EmploymentProportionCallback employmentPropCallback;
    
    private CircularFifoBuffer<Double> wageConfidence;
    
    public UnemploymentBasedWageAskAlgorithm(
        double initialLabourWage,
        double wageAdaptationRate,
        double minimumWage,
        EmploymentProportionCallback employmentProportionCallback
        ) {
        super(initGuard(initialLabourWage));
        if(employmentProportionCallback == null)
            throw new NullArgumentException();
        this.wagePerturbation = wageAdaptationRate * initialLabourWage;
        this.dice = new Random(
            Simulation.getSimState().random.nextLong());
        this.employmentPropCallback = employmentProportionCallback;
        this.wageConfidence = new CircularFifoBuffer<Double>(5);
        this.minimumWage = minimumWage;
        wageConfidence.add(0.0);
    }
    
    static double initGuard(double initialValue) {
        if(initialValue < 0)
        throw new IllegalArgumentException(
            "UnemploymentBasedWageAskAlgorithm: labour wage is negative.");
        return initialValue;
    }
    
    @Override
    public double computeNext(HouseholdState state) {
        double employmentDeficit = 
            (state.getMaximumLabourOffered() - 
                state.getLastLabourEmployed()),
            lastWageProposition = super.getLastValue(),
            nextWageProposition,
            __factor = dice.nextDouble();
        boolean doRaiseWageProposition = 
            (employmentDeficit == 0.0);
        double
            employmentProportion = 
                employmentPropCallback.getEmploymentProportion(),
            __aggregateBalanceFactor = 
                (1. - employmentProportion) / employmentProportion,
            nextWageConfidence = 
                (doRaiseWageProposition ? +__aggregateBalanceFactor : -1.)
                    * __factor;
        wageConfidence.add(nextWageConfidence);
        double avConfidence = getSmoothedConfidence();
        nextWageProposition =
            lastWageProposition + avConfidence * wagePerturbation;
        nextWageProposition = Math.max(nextWageProposition, minimumWage);
        super.recordNewValue(nextWageProposition);
        return nextWageProposition;
    }
    
    // Compound wage confidence
    private double getSmoothedConfidence() {
        double result = 0.;
        for(Double record : wageConfidence)
            result += record;
        return result / wageConfidence.size();
    }
    
    /** Get the last labour wage. */
    public double getLastLabourWage() {
       return super.getLastValue();
    }
    
    /** Get the characteristic wage perturbation. */
    public double getWagePerturbation() {
       return wagePerturbation;
    }
    
    /** Get the (fixed) minimum wage. */
    public double getMinimumWage() {
       return getMinimumWage();
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'unemployment based labour wage ask algorithm,
     * adaptation rate = 0.01, minimum wage = 1.5e7'
     */
    @Override
    public String toString() {
        return
            "unemployment based labour wage ask algorithm," +
            " adaptation rate = " + getWagePerturbation() + ", " +
            "minimum wage = " + getMinimumWage();
    }
}
