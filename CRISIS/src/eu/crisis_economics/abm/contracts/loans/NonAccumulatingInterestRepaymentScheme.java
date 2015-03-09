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
  * A repayment scheme with non-accumulating interest. In this repayment
  * scheme, the {@link Borrower} repays a principal sum P, plus overall
  * interest r * P, in multiple instalments of equal size. This scheme 
  * differs from a fixed rate mortgage in that the total amount to repay
  * does not grow in time; the total final repayment does not depend on 
  * the number of instalments.
  * 
  * @author phillips
  */
final class NonAccumulatingInterestRepaymentScheme
   extends AbstractRepaymentScheme {
   
   private double
      amountDueThisInstalment,
      debtNow;
   private final double
      overallInterestRate;
   
   private ScheduleIntervals
      repaymentScheduleInterval;
   private final SimulatedEventOrder
      paymentScheduleOrder;
   
   /**
     * Create a new repayment scheme with the following properties:<br><br>
     *    (a) a principal of size P is transferred to the borrower;<br>
     *    (b) the total amount the borrower must repay is equal to<br>
     *        (1 + r) * P, where r is the overall interest rate;
     *    (c) the borrower repays the sum in (b) in numberOfInstalmentsToRepay
     *        equal instalments.
     */
   NonAccumulatingInterestRepaymentScheme(
      final double principalValue,
      final double overallInterestRate,
      final int numberOfInstalmentsToRepay,
      final SimulatedEventOrder paymentScheduleOrder,
      final ScheduleIntervals repaymentScheduleInterval
      ) {
      super(
         principalValue,
         NonAccumulatingInterestRepaymentScheme.computeTotalRepaymentDueFor(
            principalValue,
            overallInterestRate
            ),
         numberOfInstalmentsToRepay
         );
      Preconditions.checkNotNull(repaymentScheduleInterval);
      this.repaymentScheduleInterval = repaymentScheduleInterval;
      this.overallInterestRate = overallInterestRate;
      this.amountDueThisInstalment = computeFixedInstalmentPayment();
      this.debtNow = computeTotalRepaymentDueFor(principalValue, overallInterestRate);
      this.paymentScheduleOrder = paymentScheduleOrder;
   }
   
   public static double computeTotalRepaymentDueFor(
      final double mortgagePrincipalValue,
      final double mortgageInterestRate
      ) {
      return (1. + mortgageInterestRate) * mortgagePrincipalValue;
   }
   
   /**
     * Get the cash size of each repayment instalment for this scheme.
     * The repayment per instalment is simply P * (1 + r) / N, where P is
     * the total pricipal payment, N is the number of instalments in which to 
     * repay the loan, and r is the overall interest rate.
     */
   private double computeFixedInstalmentPayment() {
      return super.getTotalPrincipalPayment() * (1 + overallInterestRate) /
         getTotalNumberOfRepaymentPhases();
   }
   
   @Override
   public double getNextInstalmentPaymentDue() {
      return amountDueThisInstalment;
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
      else return debtNow - amountDueThisInstalment;
   }
   
   @Override
   public double getOverallInterest() {
      return overallInterestRate;
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
     * Increment the value of this repayment scheme.
     */
   private void incrementScheme(double debtAmount) {
      if(debtAmount == 0.) return;
      else if(debtAmount < 0.) debtAmount = Math.max(debtAmount, -debtNow);
      else if(isFullyRepaid())
         throw new IllegalStateException(
            "NonAccumulatingInterestRepaymentScheme.incrementScheme: scheme has been " 
          + "fully repaid.");
      if(debtAmount > 0.) {                                     // Extension to Principal
         final double
            principalExtension = debtAmount / (1 + overallInterestRate);
         super.incrementTotalPrincipalPayment(principalExtension);
         debtNow += debtAmount;
      }
      else {
         debtNow -= debtAmount;
         debtNow = Math.max(debtNow, 0.);
      }
      if(debtNow <= 0.) {
         super.setNumberOfPhasesRemaining(0);
         amountDueThisInstalment = 0.;
      }
      else {
         super.setTotalNumberOfRepaymentPhases(super.getNumberOfPhasesRemaining());
         amountDueThisInstalment = computeFixedInstalmentPayment();
      }
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return
         "Non-Accumulating Interest Repayment Scheme, overall interest rate: " 
            + getOverallInterest() + ", " + "instalment value: " 
            + getFixedInstalmentPayment() + ", "
            + "original principal: " + getTotalPrincipalPayment() + ", "
            + "debt now: " + getDebtNow() + ".";
   }
}
