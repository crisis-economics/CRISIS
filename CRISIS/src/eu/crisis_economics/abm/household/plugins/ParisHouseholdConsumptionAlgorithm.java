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
 * @author      JKP
 * @category    Households
 * @see         
 * @since       1.0
 * @version     1.0
 */
public final class ParisHouseholdConsumptionAlgorithm
    implements HouseholdConsumptionFunction {
    
    private double beta;
    
    public ParisHouseholdConsumptionAlgorithm(double beta) { // Immutable
        this.beta = beta;
    }
    
    @Override
    public Double[] calculateConsumption(
        double comsumptionBudget,
        Double[] goodsUnitPrices
        ) {
        Double[] result = new Double[goodsUnitPrices.length];
        double
            Pbar = this.getPBar(goodsUnitPrices),
            Z = 0;
        for(int i = 0; i< result.length; ++i) {
            double __expfac = Math.exp(-beta * goodsUnitPrices[i] / Pbar);
            result[i] = comsumptionBudget * __expfac / goodsUnitPrices[i];
            Z += __expfac;
        }
        for(int i = 0; i< result.length; ++i)
            result[i] /= Z;
        return result;
    }
    
    // \bar{P}
    private double getPBar(Double[] goodsUnitPrices) {
        Double[] Y = this.getProductionAllSectors();
        if(goodsUnitPrices.length != Y.length)
            throw new IllegalStateException(
                "ParisHouseholdConsumptionAlgorithm.getPBar: " + 
                "goods unit prices and sector production to not" + 
                " correspond.");
        double
            result = 0,
            YSum = 0;
        for(int i = 0; i< Y.length; ++i) {
            result += Y[i] * goodsUnitPrices[i];
            YSum += Y[i];
        }
        return result / YSum;
    }
    
    // Y
    private Double[] getProductionAllSectors() {            // TODO: link
        return null;
    }
    
    /** Beta parameter */
    public double getBeta() { return beta; }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * 'Paris household consumption algorithm, beta = ..'.
     */
    @Override
    public String toString() {
        return "Paris household consumption algorithm, beta = " + beta;
    }
}