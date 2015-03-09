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
import eu.crisis_economics.utilities.StateVerifier;

/**
  * @author phillips
  */
final class CouponBondRepaymentScheme
   extends AbstractRepaymentScheme {
   
   private ScheduleIntervals
      intervalBetweenCoupons;
   private final double
      interestPerCoupon;
   private double
      debtNow;
   
   private final SimulatedEventOrder
      couponRepaymentScheduleOrder;
   
   /**
     * Create a new fixed-interval fixed-rate coupon repayment scheme.
     */
   CouponBondRepaymentScheme(
      final double bondPrincipalValue,
      final double interestPerCoupon,
      final int numberOfCouponPayments,
      final SimulatedEventOrder couponRepaymentScheduleOrder,
      final ScheduleIntervals intervalBetweenCoupons
      ) {
      super(
         bondPrincipalValue,
         computeTotalRepaymentDueFor(
            bondPrincipalValue, interestPerCoupon, numberOfCouponPayments),
         numberOfCouponPayments
         );
      Preconditions.checkArgument(interestPerCoupon >= 0.);
      StateVerifier.checkNotNull(couponRepaymentScheduleOrder, intervalBetweenCoupons);
      this.intervalBetweenCoupons = intervalBetweenCoupons;
      this.interestPerCoupon = interestPerCoupon;
      this.couponRepaymentScheduleOrder = couponRepaymentScheduleOrder;
      this.debtNow = bondPrincipalValue;
   }
   
   /**
     * Compute the anticipated total repayment (coupons plus principal) of a bond.
     */
   public static double computeTotalRepaymentDueFor(
      final double principalValue,
      final double interestPerCoupon,
      final int numberOfCouponPayments
      ) {
      return principalValue * (1. + interestPerCoupon * numberOfCouponPayments);
   }
   
   @Override
   public double getNextInstalmentPaymentDue() {
      if(isFullyRepaid()) return 0.;
      return (interestPerCoupon + (isLastPhase() ? 1 : 0)) * debtNow;
   }
   
   @Override
   public double getEstimatedTotalRepaymentRemaining() {
      return
         isFullyRepaid() ? 0. :
         (getNumberOfPhasesRemaining() * interestPerCoupon + 1.) * debtNow;
   }
   
   @Override
   public void advanceRepaymentPhase() {
      super.advanceRepaymentPhase();
      if(isFullyRepaid()) debtNow = 0.;
   }
   
   @Override
   public double getDebtNow() {
      return debtNow;
   }
   
   @Override
   public double getDebtInNextPhase() {
      final int couponsRemaining = super.getNumberOfPhasesRemaining();
      if(couponsRemaining <= 1) return 0.;
      else return debtNow;
   }
   
   @Override
   public double getOverallInterest() {
      return interestPerCoupon * super.getTotalNumberOfRepaymentPhases();
   }
   
   @Override
   public SimulatedEventOrder getRepaymentEvent() {
      return couponRepaymentScheduleOrder;
   }
   
   @Override
   public ScheduleIntervals getInstalmentRepaymentInterval() {
      return intervalBetweenCoupons;
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
     * Increment the value of this coupon bond repayment scheme.
     * If:
     *   (a) the argument is negative, the outstanding borrower debt is
     *       decremented by max(abs(debtAmount), existing debt),
     *   (b) the argument is positive, the outstanding borrower debt is
     *       incremented by debtAmount, or
     *   (c) if the argument is zero, no action is taken.
     * In cases (a) and (b), the total number of repayment phases (coupons) 
     * due on the bond is reset to the number of phases remaining at the time
     * incrementScheme() was called. If the scheme is complete (Ie. fully
     * repaid) at the time incrementScheme() was called, 
     * IllegalStateException is raised.
     */
   private void incrementScheme(double debtAmount) {
      if(debtAmount == 0.) return;
      else if(debtAmount < 0.) debtAmount = Math.max(debtAmount, -debtNow);
      else if(isFullyRepaid())
         throw new IllegalStateException(
            "CouponBondRepaymentScheme.extendRepaymentScheme: scheme has been fully repaid.");
      if(debtAmount > 0.)                                       // Extension to Principal
         super.incrementTotalPrincipalPayment(debtAmount);
      debtNow = Math.max(debtNow + debtAmount, 0.);
      if(debtNow == 0.)
         super.setNumberOfPhasesRemaining(0);
      else
         super.setTotalNumberOfRepaymentPhases(super.getNumberOfPhasesRemaining());
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and as such should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "CouponBondRepaymentScheme, total repayment remaining: "
            + getEstimatedTotalRepaymentRemaining() + ", overall interest: "
            + getOverallInterest() + ".";
   }
}
