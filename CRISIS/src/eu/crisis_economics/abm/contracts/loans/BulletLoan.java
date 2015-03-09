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
  * A Bullet Loan contract.
  * 
  * Bullet Loan contracts involve one single repayment of 
  * the loan principal (P) plus an overall interest rate (r) 
  * at some predetermined future time. The Borrower gains
  * access to the sum P, from the Lender, as a result of 
  * securing the loan, and the Borrower must repay the sum 
  * (1+r)*P in a single installment at maturity.
  * 
  * @author phillips
  */
public final class BulletLoan extends RepaymentPlanLoan {
   BulletLoan(
      Borrower borrower,
      Lender lender,
      final double principalValue,
      final double interestRate,
      SimulatedEventOrder repaymentOrder,
      ScheduleIntervals installmentInterval
      ) throws LenderInsufficientFundsException {
      super(
         borrower,
         lender,
         new BulletRepaymentScheme(
            principalValue,
            interestRate,
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
      return "BulletLoan, value: " + getValue() + ", interest rate: "
            + getInterestRate() + ", next instalment: "
            + getNextInstallmentPayment() + ", principal value: "
            + getLoanPrincipalValue() + ".";
   }
}
