/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
  * A loan with non-accumulating interest. The {@link Borrower} repays 
  * a principal sum P, plus overall interest r * P, in multiple installments
  * of equal size. This scheme differs from a fixed rate mortgage in that 
  * the total amount to repay does not grow in time; the total final repayment
  * does not depend on the number of installments.
  * 
  * @author phillips
  */
public final class NonAccumulatingInterestLoan extends RepaymentPlanLoan {
   NonAccumulatingInterestLoan(
      Borrower borrower,
      Lender lender,
      final double principalValue,
      final double interestRate,
      final int numberOfInstalments,
      SimulatedEventOrder repaymentOrder,
      ScheduleIntervals installmentInterval
      ) throws LenderInsufficientFundsException {
      super(
         borrower,
         lender,
         new NonAccumulatingInterestRepaymentScheme(
            principalValue,
            interestRate,
            numberOfInstalments,
            repaymentOrder,
            installmentInterval
            ),
         new LiquidateBorrowerDefaultScheme()
         );
   }
   
   @Override
   public <T> T acceptOperation(LoanOperation<T> operation) {
      return operation.operateOn(this);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Non Accumulating Interest Loan, value: " + getValue() + ", interest rate: "
            + getInterestRate() + ", next instalment: "
            + getNextInstallmentPayment() + ", principal value: "
            + getLoanPrincipalValue() + ".";
   }
}
