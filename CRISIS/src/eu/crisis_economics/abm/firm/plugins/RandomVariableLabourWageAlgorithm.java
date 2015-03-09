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
package eu.crisis_economics.abm.firm.plugins;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.exception.NullArgumentException;

/**
 * @author      JKP
 * @category    Firms
 * @see         
 * @since       1.0
 * @version     1.0
 */
public final class RandomVariableLabourWageAlgorithm extends
    FirmDecisionRule {
    
    private AbstractRealDistribution labourWageRandomVariable;
    
    public RandomVariableLabourWageAlgorithm(
        AbstractRealDistribution labourWageDistribution) {
        super(initGuard(labourWageDistribution));
        labourWageRandomVariable = labourWageDistribution;
    }
    
    static private double initGuard(
        AbstractRealDistribution labourWageDistribution) {
        if(labourWageDistribution == null)
            throw new NullArgumentException();
        return labourWageDistribution.sample();
    }
    
    @Override
    public double computeNext(FirmState currentState) {
        double wageSelection = labourWageRandomVariable.sample();
        super.recordNewValue(wageSelection);
        return wageSelection;
    }
    
    /** Get the mean of the labour wage distribution variable. */
    public double getDistributionMean() { 
        return labourWageRandomVariable.getNumericalMean();
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'random variable labour wage algorithm, mean 10.0'
     */
    @Override
    public String toString() {
        return "random variable labour wage algorithm, " + 
             labourWageRandomVariable.getNumericalMean();
    }
}
