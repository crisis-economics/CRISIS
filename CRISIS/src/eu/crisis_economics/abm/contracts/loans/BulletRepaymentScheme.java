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

import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.SimulatedEventOrder;

/**
  * @author phillips
  */
final class BulletRepaymentScheme implements RepaymentScheme {
   private CouponBondRepaymentScheme
      delegate;
   
   public BulletRepaymentScheme(
      final double principalValue,
      final double overallInterest,
      final SimulatedEventOrder paymentScheduleOrder,
      final ScheduleIntervals timeUntilRepaymentIsDue
      ) {
      this.delegate = new CouponBondRepaymentScheme(
         principalValue,
         overallInterest,
         1,
         paymentScheduleOrder,
         timeUntilRepaymentIsDue
         );
   }
   
   public void advanceRepaymentPhase() {
      delegate.advanceRepaymentPhase();
   }
   
   public final int getNumberOfPhasesRemaining() {
      return delegate.getNumberOfPhasesRemaining();
   }
   
   public final double getTotalPrincipalPayment() {
      return delegate.getTotalPrincipalPayment();
   }
   
   public final boolean isFullyRepaid() {
      return delegate.isFullyRepaid();
   }
   
   public double getNextInstalmentPaymentDue() {
      return delegate.getNextInstalmentPaymentDue();
   }
   
   public double getEstimatedTotalRepaymentRemaining() {
      return delegate.getEstimatedTotalRepaymentRemaining();
   }
   
   public double getDebtNow() {
      return delegate.getDebtNow();
   }
   
   public double getDebtInNextPhase() {
      return delegate.getDebtInNextPhase();
   }
   
   public final void setDebtValue(double newValue) {
      delegate.setDebtValue(newValue);
   }
   
   public double getOverallInterest() {
      return delegate.getOverallInterest();
   }
   
   public SimulatedEventOrder getRepaymentEvent() {
      return delegate.getRepaymentEvent();
   }
   
   public ScheduleIntervals getInstalmentRepaymentInterval() {
      return delegate.getInstalmentRepaymentInterval();
   }
   
   public void extendDebt(double positiveAmount) {
      delegate.extendDebt(positiveAmount);
   }
   
   public void reduceDebt(double positiveAmount) {
      delegate.reduceDebt(positiveAmount);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "BulletRepaymentScheme, total repayment remaining: "
            + getEstimatedTotalRepaymentRemaining() + ", overall interest: "
            + getOverallInterest() + ".";
   }
}
