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
package eu.crisis_economics.abm.contracts;

/**
 * @author bochmann
 * @author phillips
 */
public class FixedValueContract extends Contract {
   private double
      valueOfContract,
      faceValueOfContract,
      interestRate;
   
   static private final double DEFAULT_INTEREST_RATE = 0.;
   
   /** Contract with no expiry. */
   public FixedValueContract() {
      this(Double.POSITIVE_INFINITY);
   }
   
   public FixedValueContract(
      final double expirationTime) {
      super(expirationTime);
      this.interestRate = DEFAULT_INTEREST_RATE;
   }
   
   public FixedValueContract(
      final double expirationTime,
      final double interestRate
      ) {
      super(expirationTime);
      this.interestRate = interestRate;
   }
	
   public FixedValueContract(
      final double expitationTime,
      final double interestRate,
      final double value,
      final double faceValue
      ) {
      super(expitationTime);
      this.valueOfContract = value;
      this.faceValueOfContract = faceValue;
      this.interestRate = interestRate;
   }
   
   public synchronized final double getValue() {
      return valueOfContract;
   }
   
   public synchronized final void setValue(final double newValue) {
      this.faceValueOfContract = this.valueOfContract = newValue;
   }
   
   public synchronized final double getFaceValue() {
      return faceValueOfContract;
   }
   
   public synchronized final void setFaceValue(final double newValue) {
      faceValueOfContract = newValue;
   }
   
   public synchronized final double getInterestRate() {
      return interestRate;
   }
   
   public synchronized final void setInterestRate(final double newValue) {
      interestRate = newValue;
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "FixedValueContract [value=" + valueOfContract + ", faceValue=" + faceValueOfContract
            + ", interestRate=" + interestRate + "], Super: " + super.toString();
   }
}
