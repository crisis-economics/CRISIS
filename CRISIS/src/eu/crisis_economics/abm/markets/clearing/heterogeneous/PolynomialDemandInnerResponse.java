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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

/**
  * A polynomial univariate inner response function for (demand)
  * nodes in a heterogeneous clearing network.
  */
public final class PolynomialDemandInnerResponse
   extends AbstractBoundedUnivariateFunction {
   
   private double
      exponent,
      maxDemand;
   
   public PolynomialDemandInnerResponse(
      final double exponent,
      final double maximumDomainValue,
      final double maxDemand
      ) {
      super(new SimpleDomainBounds(0., maximumDomainValue));
      this.exponent = exponent;
      this.maxDemand = maxDemand;
   }
   
   @Override
   public double value(double rate) {
      if(rate >= super.getMaximumInDomain()) return 0.;
      else if(rate < 0.)
         throw new IllegalArgumentException(
            "PolynomialDemandResponse.value: argument " + rate + " is out of bounds. ");
      return maxDemand*(1.-Math.pow(rate / super.getMaximumInDomain(), exponent));
   }
   
   /**
     * Get the polynomial power of this demand function.
     */
   public double getExponent() {
      return exponent;
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return
         "Heterogeneous clearing polynomial demand response function, " +
         "exponent: " + getExponent() + ", crossing point: " + 
         super.getMaximumInDomain() + ".";
   }
}
