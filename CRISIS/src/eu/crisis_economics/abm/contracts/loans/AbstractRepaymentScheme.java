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
package eu.crisis_economics.abm.contracts.loans;

import com.google.common.base.Preconditions;

/**
  * @author phillips
  */
abstract class AbstractRepaymentScheme
   implements RepaymentScheme {
   
   private int
      numberOfPhasesRemaining,
      numberOfPhasesTotal;
   
   private double
      principalSumTransfered;
   
   protected AbstractRepaymentScheme(
      final double principalSumTransfered,
      final double originalTotalRepaymentEstimate,
      final int numberOfPhasesInTotal
      ) {
      Preconditions.checkArgument(principalSumTransfered > 0.);
      Preconditions.checkArgument(originalTotalRepaymentEstimate > 0.);
      Preconditions.checkArgument(numberOfPhasesInTotal > 0);
      this.principalSumTransfered = principalSumTransfered;
      this.numberOfPhasesTotal = numberOfPhasesInTotal;
      this.numberOfPhasesRemaining = numberOfPhasesInTotal;
   }
   
   @Override
   public final int getNumberOfPhasesRemaining() {
      return numberOfPhasesRemaining;
   }
   
   /**
     * Set the number of repayment phases remaining.
     * @param value
     *        The number of repayment phases remaining.
     */
   final void setNumberOfPhasesRemaining(int value) {
      numberOfPhasesRemaining = value;
   }
   
   /**
     * Get the total number of repayment phases for this scheme.
     */
   final int getTotalNumberOfRepaymentPhases() {
      return numberOfPhasesTotal;
   }
   
   /**
     * Set the total number of repayment phases.
     * @param value
     *        The total number of repayment phases.
     */
   final void setTotalNumberOfRepaymentPhases(int value) {
      numberOfPhasesTotal = value;
   }
   
   @Override
   public final double getTotalPrincipalPayment() {
      return principalSumTransfered;
   }
   
   protected final void incrementTotalPrincipalPayment(double value) {
      principalSumTransfered = Math.max(principalSumTransfered + value, 0.);
   }
   
   @Override
   public final boolean isFullyRepaid() {
      return (numberOfPhasesRemaining == 0);
   }
   
   final boolean isLastPhase() {
      return (numberOfPhasesRemaining == 1);
   }
   
   /**
     * When implementing this method in derived classes, call
     * super.advanceRepaymentStage as a first instruction. This 
     * implementation raises an IllegalStateException if called
     * when isLoanFullyRepaid() is true.
     */
   @Override
   public void advanceRepaymentPhase() {
      if(isFullyRepaid())
         throw new IllegalStateException(
            "RepaymentScheme.advanceRepaymentPhase: loan has been fully repaid.");
      --numberOfPhasesRemaining;
   }
   
   @Override
   public final void setDebtValue(final double newValue) {
      final double
         debtNow = getDebtNow();
      if(debtNow < newValue)
         extendDebt(newValue - debtNow);
      else if(debtNow > newValue)
         reduceDebt(debtNow - newValue);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Repayment Scheme, principal sum transferred: " +
         getTotalPrincipalPayment() + ".";
   }
}
