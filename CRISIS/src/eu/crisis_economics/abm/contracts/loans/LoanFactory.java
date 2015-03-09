/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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

import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.SimulatedEventOrder;

/**
  * @author phillips
  */
public final class LoanFactory {
   
   /**
     * Create a Coupon Bond contract with the specified arguments.
     * If the principal value of the contract is negative or zero,
     * no action is taken and this method returns null.
     * 
     * @param borrower
     *        The borrower, who obtains access to loan cash.
     * @param lender
     *        The lender, who gives access to loan cash.
     * @param principalValue
     *        The initial lender sum to transfer. If this sum is 
     *        not available, InsufficientFundsException is raised.
     * @param interestRate
     *        The interest rate of the loan.
     * @param numberOfInstalments
     *        The number of instalments to repay.
     * @param repaymentOrder
     *        The time in the simulation cycle at which to make
     *        repayments.
     * @param instalmentInterval
     *        The interval between repayments.
     * @return
     *        A valid, fully formed loan contract or null (illegal
     *        argument).
     * @throws LenderInsufficientFundsException
     */
   static public Loan createCouponBond(
      Borrower borrower,
      Lender lender,
      double principalValue,
      double interestRate,
      int numberOfInstalments,
      SimulatedEventOrder repaymentOrder,
      ScheduleIntervals instalmentInterval
      ) throws LenderInsufficientFundsException {
      if(principalValue <= 0.) return null;
      return new Bond(
         borrower,
         lender,
         principalValue,
         interestRate,
         numberOfInstalments,
         repaymentOrder,
         instalmentInterval
         );
   }
   
   /**
     * This method is as {@link LoanFactory.createCouponBond}, using
     * default values for the final two parameters.
     */
   static public Loan createCouponBond(
      Borrower borrower,
      Lender lender,
      double principalValue,
      double interestRate,
      int numberOfInstalments
      ) throws LenderInsufficientFundsException {
      if(principalValue == 0.) return null;
      return createCouponBond(
         borrower,
         lender,
         principalValue,
         interestRate,
         numberOfInstalments,
         DEFAULT_REPAYMENT_ORDER,
         DEFAULT_INSTALMENT_SCHEDULE_INTERVAL
         );
   }
   
   /**
     * Create a Bullet Loan contract with the specified arguments.
     * If the principal value of the contract is negative or zero,
     * no action is taken and this method returns null.
     * 
     * @param borrower
     *        The borrower, who obtains access to loan cash.
     * @param lender
     *        The lender, who gives access to loan cash.
     * @param principalValue
     *        The initial lender sum to transfer. If this sum is 
     *        not available, InsufficientFundsException is raised.
     * @param interestRate
     *        The interest rate of the loan.
     * @param repaymentOrder
     *        The time in the simulation cycle at which to make
     *        repayments.
     * @param instalmentInterval
     *        The interval between repayments.
     * @return
     *        A valid, fully formed loan contract or null (illegal
     *        argument).
     * @throws LenderInsufficientFundsException
     */
   static public Loan createBulletLoan(
      Borrower borrower,
      Lender lender,
      double principalValue,
      double interestRate,
      SimulatedEventOrder repaymentOrder,
      ScheduleIntervals instalmentInterval
      ) throws LenderInsufficientFundsException {
      if(principalValue == 0.) return null;
      return new BulletLoan(
         borrower,
         lender,
         principalValue,
         interestRate,
         repaymentOrder,
         instalmentInterval
         );
   }
   
   /**
     * This method is as {@link LoanFactory.createBulletLoan}, using
     * default values for the final two parameters.
     */
   static public Loan createBulletLoan(
      Borrower borrower,
      Lender lender,
      double principalValue,
      double interestRate
      ) throws LenderInsufficientFundsException {
      if(principalValue == 0.) return null;
      return createBulletLoan(
         borrower,
         lender,
         principalValue,
         interestRate,
         DEFAULT_REPAYMENT_ORDER,
         DEFAULT_INSTALMENT_SCHEDULE_INTERVAL
         );
   }
   
   /**
     * Create a Fixed Rate Mortgage contract with the specified
     * arguments. If the principal value of the contract is negative
     * or zero, no action is taken and this method returns null.
     * 
     * @param borrower
     *        The borrower, who obtains access to loan cash.
     * @param lender
     *        The lender, who gives access to loan cash.
     * @param principalValue
     *        The initial lender sum to transfer. If this sum is 
     *        not available, InsufficientFundsException is raised.
     * @param interestRate
     *        The interest rate of the loan.
     * @param numberOfInstalments
     *        The number of instalments to repay.
     * @param repaymentOrder
     *        The time in the simulation cycle at which to make
     *        repayments.
     * @param instalmentInterval
     *        The interval between repayments.
     * @return
     *        A valid, fully formed loan contract or null (illegal
     *        argument).
     * @throws LenderInsufficientFundsException
     */
   static public Loan createFixedRateMortgage(
      Borrower borrower,
      Lender lender,
      double principalValue,
      double interestRate,
      int numberOfInstalments,
      SimulatedEventOrder repaymentOrder,
      ScheduleIntervals instalmentInterval
      ) throws LenderInsufficientFundsException {
      if(principalValue == 0.) return null;
      /**
        * Fixed rate mortgages have singular interest repayment expressions for 
        * r ~ 0.0. For this reason the mortgage loan is interpreted as a non-
        * accumulating interest loan for small values of the interest rate:
        */
      if(interestRate <= 1.e-8)
         return new NonAccumulatingInterestLoan(
            borrower,
            lender,
            principalValue,
            interestRate,
            numberOfInstalments,
            repaymentOrder,
            instalmentInterval
            );
      else
         return new FixedRateMortgage(
            borrower,
            lender,
            principalValue,
            interestRate,
            numberOfInstalments,
            repaymentOrder,
            instalmentInterval
            );
   }
   
   /**
     * This method is as {@link LoanFactory.createFixedRateMortgage}, using
     * default values for the final two parameters.
     */
   static public Loan createFixedRateMortgage(
      Borrower borrower,
      Lender lender,
      double principalValue,
      double interestRate,
      int numberOfInstalments
      ) throws LenderInsufficientFundsException {
      if(principalValue == 0.) return null;
      return createFixedRateMortgage(
         borrower,
         lender,
         principalValue,
         interestRate,
         numberOfInstalments,
         DEFAULT_REPAYMENT_ORDER,
         DEFAULT_INSTALMENT_SCHEDULE_INTERVAL
         );
   }
   

   /**
     * Create an interbank loan contract with the specified
     * arguments. If the principal value of the contract is negative
     * or zero, no action is taken and this method returns null.
     * 
     * @param borrower
     *        The borrower {@link Bank}, who obtains access to loan cash.
     * @param lender
     *        The lender {@link Bank}, who gives access to loan cash.
     * @param principalValue
     *        The initial lender sum to transfer. If this sum is 
     *        not available, InsufficientFundsException is raised.
     * @param interestRate
     *        The interest rate of the loan. This argument must be 
     *        strictly positive, or else no {@link Loan} contract is formed
     *        and no action is taken.
     * @param numberOfInstalments
     *        The number of instalments to repay.
     * @param repaymentOrder
     *        The time in the simulation cycle at which to make
     *        repayments.
     * @param instalmentInterval
     *        The interval between repayments.
     * @return
     *        A valid, fully formed loan contract or null (illegal
     *        argument).
     * @throws LenderInsufficientFundsException
     */
   static public Loan createInterbankLoan(
      Bank borrower,
      Bank lender,
      double principalValue,
      double interestRate,
      int numberOfInstalments,
      SimulatedEventOrder repaymentOrder,
      ScheduleIntervals instalmentInterval
      ) throws LenderInsufficientFundsException {
      if(principalValue == 0. || interestRate == 0.) return null;
      return new InterbankLoan(
         borrower,
         lender,
         principalValue,
         interestRate,
         numberOfInstalments,
         repaymentOrder,
         instalmentInterval
         );
   }
   
   /**
     * Create a Cash Injection Loan contract with the specified
     * arguments. If the principal value of the contract is negative
     * or zero, no action is taken and this method returns null.
     * 
     * @param borrower
     *        The borrower, who obtains access to loan cash.
     * @param lender
     *        The lender, who gives access to loan cash.
     * @param principalValue
     *        The initial lender sum to transfer. If this sum is 
     *        not available, InsufficientFundsException is raised.
     * @param interestRate
     *        The interest rate of the loan.
     * @param numberOfInstalments
     *        The number of instalments to repay.
     * @param repaymentOrder
     *        The time in the simulation cycle at which to make
     *        repayments.
     * @param instalmentInterval
     *        The interval between repayments.
     * @return
     *        A valid, fully formed loan contract or null (illegal
     *        argument).
     * @throws LenderInsufficientFundsException
     */
   static public Loan createCashInjectionLoan(
      Borrower borrower,
      Lender lender,
      double principalValue,
      double interestRate,
      int numberOfInstalments,
      SimulatedEventOrder repaymentOrder,
      ScheduleIntervals instalmentInterval
      ) throws LenderInsufficientFundsException {
      if(principalValue == 0.) return null;
      return new CashInjectionLoan(
         borrower,
         lender,
         principalValue,
         interestRate,
         numberOfInstalments,
         repaymentOrder,
         instalmentInterval
         );
   }
   
   /**
     * This method is as {@link LoanFactory.createCashInjectionLoan}, using
     * default values for the final two parameters.
     */
   static public Loan createCashInjectionLoan(
      Borrower borrower,
      Lender lender,
      double principalValue,
      double interestRate,
      int numberOfInstalments
      ) throws LenderInsufficientFundsException {
      if(principalValue == 0.) return null;
      return createCashInjectionLoan(
         borrower,
         lender,
         principalValue,
         interestRate,
         numberOfInstalments,
         DEFAULT_REPAYMENT_ORDER,
         DEFAULT_INSTALMENT_SCHEDULE_INTERVAL
         );
   }
   
   private LoanFactory() { }   // Uninstantiatable
   
   private static final SimulatedEventOrder
      DEFAULT_REPAYMENT_ORDER = NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS;
   private static final ScheduleIntervals
      DEFAULT_INSTALMENT_SCHEDULE_INTERVAL = ScheduleIntervals.ONE_DAY;
}