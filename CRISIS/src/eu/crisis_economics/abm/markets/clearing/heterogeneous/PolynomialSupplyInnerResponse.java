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
  * A polynomial univariate inner response function for (supply)
  * nodes in a heterogeneous clearing network.
  */
public final class PolynomialSupplyInnerResponse
   extends AbstractBoundedUnivariateFunction {
   
   private double
      exponent,
      normalizationFactor,
      maxSupply;
   
   public PolynomialSupplyInnerResponse(
      final double exponent,
      final double normalizationFactor,
      final double maxSupply
      ) {
      super(new SimpleDomainBounds());
      this.exponent = exponent;
      this.normalizationFactor = normalizationFactor;
      this.maxSupply = maxSupply;
   }
   
   @Override
   public double value(double rate) {
      if(rate >= super.getMaximumInDomain()) return maxSupply;
      else if(rate < 0.)
         throw new IllegalArgumentException(
            "PolynomialSupplyResponse.value: argument " + rate + " is out of bounds. ");
//      {
//      double value = maxSupply*Math.pow(rate / normalizationFactor, exponent);
//      System.out.println("MutualFund supply (rate " + rate + "): " + value + ", " + 100.*value/maxSupply + ".");
//      }
      return -maxSupply*Math.pow(rate / normalizationFactor, exponent);
   }
   
   /**
     * Get the polynomial power of this supply function.
     */
   public double getExponent() {
      return exponent;
   }
   
   /**
     * Get the polynomial normalization factor of this function.
     */
   public double getNormalizationFactor() {
      return normalizationFactor;
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return
         "Heterogeneous clearing polynomial supply response function, " +
         "exponent: " + getExponent() + ", domain maximum: " + 
         super.getMaximumInDomain() + ".";
   }
}
