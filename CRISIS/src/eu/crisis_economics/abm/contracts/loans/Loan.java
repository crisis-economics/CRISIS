/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Olaf Bochmann
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

public interface Loan {
   /**
     * Terminate the loan contract. If the loan has already been
     * terminated, this method raises an IllegalStateException. 
     * Otherwise the loan will be removed, immediately, from Lender
     * and Borrower balance sheets. Following termination, subsequent
     * calls to getBorrower() and getLender() yield null.
     */
   public void terminateContract();
   
   /**
     * Has this loan contract been terminated?
     */
   public boolean hasBeenTerminated();
   
   /**
     * Get the loan contract value. The loan contract value is the
     * immediate Borrower debt: the amount that the Borrower would
     * need to repay, were this possible, to instantly settle 
     * the loan contract.
     */
   public double getValue();
   
   /**
     * Set the loan contract value. The loan contract value is the
     * immediate Borrower debt: the amount that the Borrower would
     * need to repay, were this possible, to instantly settle 
     * the loan contract. This method does not remove the loan 
     * contact from the balance sheet of either the borrower or 
     * the lender unless the argument is zero. The argument must
     * be non-negative or else no action is taken.
     * 
     * Calls to this method will change the state of the loan 
     * contract, including the sum of future repayments and 
     * the structure of future repayments. Successfully calling
     * getValue() immediately after setValue(x) is expected to 
     * yield x. It is further expected that, for positive x > 0, 
     * setValue(x) is equivalent to either (a) extendLoan(x - y) 
     * [x > y], or (b) writeDownLoan(y - x) [y > x], where y is 
     * the value of the loan immediately before setValue(x) is
     * called.
     * 
     * If the argument is negative, the argument is silently changed
     * to zero. 
     * 
     * If the value of the loan is to be increased, and the Lender
     * is unable or is unwilling to transfer additional cash, 
     * ContractModificationException is raised. If the value of the 
     * loan is to be decreased, any compensations received by the 
     * Lender (if any) are specified by the implementation.
     */
   public void setValue(double newValue);
   
   /**
     * Get the face value of the loan contract. The face value of 
     * the loan is the sum of all pending future Borrower payments, 
     * as indicated by the loan repayment scheme, that will ultimately
     * settle the Borrower loan liability and terminate the contract
     * without default.
     */
   public double getFaceValue();
   
   /**
    * Note that it is not possible to manually set the face value 
    * of a loan contract. This is because the amount owing is
    * determined by the repayment scheme, interest rate, and the 
    * duration of the loan contract.
    */
   
   public double getInterestRate();
   
   /**
     * Get the sum that the Borrower has gained access to as a 
     * result of securing the loan.
     */
   public double getLoanPrincipalValue();
   
   /**
     * Try to extend the loan via an additional Lender-to-Borrower
     * transfer. This method complements writeDownLoan(double 
     * positiveAmount).
     * 
     * The argument positiveAmount should be non-negative,
     * or else no action is taken. 
     * 
     * If the Lender cannot extend the existing loan principal, or
     * declines to do so, LenderInsufficientFundsException is raised.
     * Otherwise, the loan repayment scheme is extended to accommodate
     * the additional sum transferred to the Borrower.
     */
   public void extendLoan(double positiveAmount)
      throws LenderInsufficientFundsException;
   
   /**
     * Write down (cancel) some of the value of a loan. This method
     * complements extendLoan(double positiveAmount).
     * 
     * The argument positiveAmount should be non-negative, or else
     * no action is taken. 
     * 
     * Calling this function with a nonzero argument will result in 
     * some debt being written off. It is expected that calls to
     * getValue() immediately after writeDownLoan(x) will yield
     * y - x where y was the value of the loan immediately before 
     * writeDownLoan(x) was called. If x is greater than y, the 
     * argument x is silently trimmed to y before any action is
     * taken.
     * @param positiveAmount
     *        The amount of loan value to remove.
     */
   public void writeDownLoan(double positiveAmount);
   
   /**
     * Get the loan borrower (the party who obtained cash via the
     * loan).
     */
   public Borrower getBorrower();
   
   /**
     * Get the loan lender (the party who gave cash via the loan).
     */
   public Lender getLender();
   
   /**
     * Transfer this loan to a new Lender. The loan contract is 
     * removed from the balance sheet of the existing Lender.
     */
   public void transferToNewLender(Lender newLender);
   
   /**
     * Get the next loan installment repayment. In event that the
     * loan has been fully repaid, this method returns zero.
     */
   public double getNextInstallmentPayment();
   
   /**
     * Get the total number of installment steps required to repay
     * the loan since the inception of the loan contract. This
     * method returns the total number of installments to repay the
     * loan since the last time that the loan repayment scheme was
     * modified.
     */
   public int getTotalNumberOfInstallmentsToRepaySinceInception();
   
   /**
     * Get the total amount that the Borrower must repay, over all
     * time, in order to comply with the terms of the loan repayment
     * scheme.
     */
   public double getTotalAmountToRepayAtInception();
   
   /**
    * Get the overall interest rate payable on the loan. The overall
    * interest rate is defined to be the multiplier r such that 
    * (1 + r) converts the principal loan sum borrowed to the sum 
    * of all repayments made until the loan contract expires (the
    * original face value). Note that in general this measurement
    * is not the same as the loan interest rate.
    */
   public double getOverallLoanInterest();
   
   /**
     * Get the value of the loan contact after the next installment 
     * payment has been received in full.
     */
   public double getLoanValueAfterNextInstallment();
   
   /**
    * Generic visitor pattern entry point.
    */
  public <T> T acceptOperation(final LoanOperation<T> operation);
}