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

interface RepaymentScheme {
   /**
     * Get the (minimal) sum that must be repaid by the borrower when the
     * next loan instalment is due.
     */
   public double getNextInstalmentPaymentDue();
   
   /** 
     * Get the number of instalment payments remaining until the borrower
     * has fully repaid the loan. The expected overall number of instalment 
     * payments may change in time, and should not be regarded as constant.
     */
   public int getNumberOfPhasesRemaining();
   
   /**
     * Get the total principal sum tranferred - the sum that the borrower
     * has gained access to as a result of securing the loan.
     */
   public double getTotalPrincipalPayment();
   
   /**
     * Get the estimated overall cash sum that must still be repaid
     * to settle the loan contact. This sum depends on the amount of money
     * already repaid, the form of the loan repayment scheme, and the time
     * since the loan was initially taken out, among other factors.
     */
   public double getEstimatedTotalRepaymentRemaining();
   
   /**
     * Returns true if the loan has been fully repaid, otherwise returns
     * false.
     */
   public boolean isFullyRepaid();
   
   /**
     * Advance to the next repayment phase.
     */
   public void advanceRepaymentPhase();
   
   /**
     * Get the amount of cash that, if repaid now, were this possible, 
     * would fully settle the loan debt.
     */
   public double getDebtNow();
   
   /**
     * Get the result of getDebtNow() in the next repayment phase. 
     * Bank Agents often predict their future balance sheets when 
     * making portfolio investment decisions. This method accommodates
     * this behaviour.
     */
   public double getDebtInNextPhase();
   
   /**
     * Get the overall interest rate payable on the loan. The overall
     * interest rate is defined to be the multiplier r such that 
     * (1 + r) converts the principal loan sum borrowed to the sum 
     * of all repayments made until the loan contract expires (the
     * original face value). Note that in general this measurement
     * is not the same as the loan interest rate.
     */
   public double getOverallInterest();
   
   /**
     * Get the schedule order of repayment instalments in this scheme
     * (the event in the schedule ordering that corresponds to loan
     * repayments).
     */
   public SimulatedEventOrder getRepaymentEvent();
   
   /**
     * Modify the this repayment scheme to reflect a change in 
     * the immediate debt of the borrower. For y = getDebtNow(),
     * it is expected that setDebtValue(x) will behave as 
     * follows:
     *   (a) if x <= 0, subsequent calls to isFullyRepaid() shall
     *       return True,
     *   (a) if x > y, calls to this method are equivalent to
     *       extendDebt(x - y),
     *   (b) if x < y, calls to this method are equivalent to 
     *       reduceDebt(y - x).
     */
   public void setDebtValue(double newValue);
   
   /**
     * Extend a repayment scheme with an additional borrower 
     * liability. Extending the repayment scheme corresponds to
     * increasing the borrower debt by a fixed value (for instance
     * a supplementary principal transfer from the Lender).
     * 
     * The argument to this method should be positive, or else 
     * no action is taken.
     */
   public void extendDebt(double positiveAmount);
   
   /**
     * Modify the replayment scheme by reducing the borrower 
     * liability. Reducing the repayment scheme corresponds to
     * decreasing the immediate borrower debt by a fixed value 
     * (for instance a partial loan writeoff, or borrower debt
     * forgiveness).
     * 
     * The argument to this method should be positive, or else 
     * no action is taken.
     */
   public void reduceDebt(double positiveAmount);
   
   /**
     * Get the interval between the present/most recent
     * instalment payment and the next instalment payment. If there is
     * no instalment payment due after the most recent instalment, this
     * method returns null.
     */
   public ScheduleIntervals getInstalmentRepaymentInterval();
}