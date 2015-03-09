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

import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.SimulatedEventOrder;

/**
  * @author phillips
  */
final class FixedRateMortgageRepaymentScheme
   extends AbstractRepaymentScheme {
   
   private double
      amountDueThisInstalment,
      debtNow;
   private final double
      interestRate;
   
   private ScheduleIntervals
      repaymentScheduleInterval;
   private final SimulatedEventOrder
      paymentScheduleOrder;
   
   /**
     * Create a new Fixed Rate Mortgage repayment scheme.
     */
   FixedRateMortgageRepaymentScheme(
      final double principalValue,
      final double interestRate,
      final int numberOfInstalmentsToRepay,
      final SimulatedEventOrder paymentScheduleOrder,
      final ScheduleIntervals repaymentScheduleInterval
      ) {
      super(
         principalValue,
         FixedRateMortgageRepaymentScheme.computeTotalRepaymentDueFor(
            principalValue,
            interestRate,
            numberOfInstalmentsToRepay),
         numberOfInstalmentsToRepay
         );
      Preconditions.checkNotNull(repaymentScheduleInterval);
      this.repaymentScheduleInterval = repaymentScheduleInterval;
      this.interestRate = interestRate;
      this.amountDueThisInstalment = computeFixedInstalmentPayment(principalValue);
      this.debtNow = principalValue;
      this.paymentScheduleOrder = paymentScheduleOrder;
   }
   
   /**
     * Convert fixed rate mortgage parameters into a total anticipated
     * future debt.
     */
   public static double computeTotalRepaymentDueFor(
      final double mortgagePrincipalValue,
      final double mortgageInterestRate,
      final int numRepaymentInstalments
      ) {
      return mortgageInterestRate * numRepaymentInstalments * mortgagePrincipalValue /
         (1. - Math.pow(1. + mortgageInterestRate, -numRepaymentInstalments));
   }
   
   @Override
   public double getNextInstalmentPaymentDue() {
      return amountDueThisInstalment;
   }
   
   /**
     * Get the (fixed) instalment payment payable on this mortgage.
     * For a fixed rate mortgage with N repayment instalments,
     * the contribution to be repaid per instalment is c where:
     *    
     *    c = r * P / (1 - (1+r)**-N),
     *    
     * where P is the principal sum tranfered, r is the fixed mortgage
     * interest rate and N is the total number of repayment instalments 
     * at the time the mortgage contract was created. For an explicit
     * derivation, see "Fixed Rate Mortgage" on Wikipedia.
     * 
     */
   private double computeFixedInstalmentPayment(double amountOweingNow) {
      final double multiplier =
         Math.pow(1. + interestRate, super.getTotalNumberOfRepaymentPhases());
      return
         amountOweingNow * interestRate * multiplier / (multiplier - 1.);
   }
   
   public double getFixedInstalmentPayment() {
      return amountDueThisInstalment;
   }
   
   @Override
   public double getEstimatedTotalRepaymentRemaining() {
      return super.getNumberOfPhasesRemaining() * amountDueThisInstalment;
   }
   
   @Override
   public void advanceRepaymentPhase() {
      this.debtNow = getDebtInNextPhase();
      super.advanceRepaymentPhase();                                // True or Raise
   }
   
   @Override
   public double getDebtNow() {
      return debtNow;
   }
   
   @Override
   public double getDebtInNextPhase() {
      if(isFullyRepaid()) return 0.;
      else if(super.getNumberOfPhasesRemaining() == 1) return 0.;
      else return debtNow * (1. + interestRate) - amountDueThisInstalment;
   }
   
   @Override
   public double getOverallInterest() {
      return super.getTotalNumberOfRepaymentPhases() * amountDueThisInstalment /
         super.getTotalPrincipalPayment() - 1.;
   }
   
   @Override
   public SimulatedEventOrder getRepaymentEvent() {
      return paymentScheduleOrder;
   }
   
   @Override
   public ScheduleIntervals getInstalmentRepaymentInterval() {
      return repaymentScheduleInterval;
   }
   
   @Override
   public void extendDebt(double positiveAmount) {
      if(positiveAmount < 0) return;
      incrementScheme(positiveAmount);
   }
   
   @Override
   public void reduceDebt(double positiveAmount) {
      if(positiveAmount < 0) return;
      incrementScheme(-positiveAmount);
   }
   
   /**
     * Increment the value of this fixed rate mortgage repayment scheme.
     * If:
     *   (a) the argument is negative, the outstanding borrower debt is
     *       decremented by max(abs(debtAmount), existing debt),
     *   (b) the argument is positive, the outstanding borrower debt is
     *       incremented by debtAmount, or
     *   (c) if the argument is zero, no action is taken.
     * In cases (a) and (b), the total number of repayment phases due on 
     * the mortage is reset to the number of phases remaining at the time
     * incrementScheme() was called. If the scheme is complete (Ie. fully
     * repaid) at the time incrementScheme() was called, 
     * IllegalStateException is raised.
     */
   private void incrementScheme(double debtAmount) {
      if(debtAmount == 0.) return;
      else if(debtAmount < 0.) debtAmount = Math.max(debtAmount, -debtNow);
      else if(isFullyRepaid())
         throw new IllegalStateException(
            "FixedRateMortgage.incrementScheme: scheme has been fully repaid.");
      if(debtAmount > 0.)                                       // Extension to Principal
         super.incrementTotalPrincipalPayment(debtAmount);
      debtNow = Math.max(debtNow + debtAmount, 0.);
      if(debtNow == 0.) {
         super.setNumberOfPhasesRemaining(0);
         amountDueThisInstalment = 0.;
      }
      else {
         super.setTotalNumberOfRepaymentPhases(super.getNumberOfPhasesRemaining());
         amountDueThisInstalment = computeFixedInstalmentPayment(debtNow);
      }
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return
         "Fixed Rate Mortgage Loan, interest rate: " + getOverallInterest() + ", " +
         "instalment value: " + getFixedInstalmentPayment() + ", " +
         "original principal: " + getTotalPrincipalPayment() + ", " +
         "debt now: " + getDebtNow() + ".";
   }
}
