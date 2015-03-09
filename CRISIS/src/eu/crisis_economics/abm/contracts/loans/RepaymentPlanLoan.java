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

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.ContractModificationException;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * @author phillips
  */
abstract class RepaymentPlanLoan extends Contract implements Loan {
   
   private Borrower borrower;
   private Lender lender;
   
   private RepaymentScheme repaymentScheme;
   private DefaultScheme defaultScheme;
   
   Settlement loanSettlement;
   
   private double
      totalPaymentsMade;
   
   RepaymentPlanLoan(
      final Borrower borrower,
      final Lender lender,
      final RepaymentScheme repaymentScheme,
      final DefaultScheme defaultScheme
      )
      throws LenderInsufficientFundsException {
      super(RepaymentPlanLoan.getExpirationTime(repaymentScheme));
      StateVerifier.checkNotNull(borrower, lender, repaymentScheme, defaultScheme);
      this.borrower = borrower;
      this.lender = lender;
      this.repaymentScheme = repaymentScheme;
      this.defaultScheme = defaultScheme;
      this.loanSettlement = SettlementFactory.createDirectSettlement(borrower, lender);
      
      // Transfer the principal from Lender to Borrower.
      final double totalPrincipalSumToTransfer = 
         repaymentScheme.getTotalPrincipalPayment();
      try {
         transferFromLenderToBorrower(
            totalPrincipalSumToTransfer,
            lender,
            borrower,
            loanSettlement
            );
      } catch (InsufficientFundsException princialPaymentFailure) {
         throw new LenderInsufficientFundsException(princialPaymentFailure);
      }
      
      // Update balance sheets.
      borrower.addLiability(this);
      lender.addAsset(this);
      
      // Success. Notify Borrower.
      borrower.registerNewLoanTransfer(
         totalPrincipalSumToTransfer,
         lender,
         repaymentScheme.getOverallInterest()
         );
      
      Simulation.onceCustom(this, "tryHonorRepaymentInstalment",
         repaymentScheme.getRepaymentEvent(),
         repaymentScheme.getInstalmentRepaymentInterval()
         );
   }
   
   private static double getExpirationTime(
      final RepaymentScheme repaymentScheme) {
      return Math.round(Simulation.getTime() + 
         repaymentScheme.getNumberOfPhasesRemaining() * 
            repaymentScheme.getInstalmentRepaymentInterval().getValue());
   }
   
   /**
     * Transfer a cash sum from Lender to Borrower. If the underlying
     * loan sum cannot be transferred, an InsufficientFundsException
     * is raised.
     */
   private static void transferFromLenderToBorrower(
      final double sumToTransfer, 
      final Lender lender,
      final Borrower borrower,
      final Settlement loanSettlement
      ) throws LenderInsufficientFundsException {
      try {
         loanSettlement.bidirectionalTransfer(-sumToTransfer);
      } catch (InsufficientFundsException cashTransferFailure) { 
         throw new LenderInsufficientFundsException(cashTransferFailure);
      }
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void tryHonorRepaymentInstalment() {
      if(hasBeenTerminated()) return;                   // Bankruptcy
      if (repaymentScheme.isFullyRepaid())
         throw new IllegalStateException(
            "Loan.tryHonorRepaymentInstalment: repayment scheme indicates that this loan " +
            "has already been fully repaid.");
      final double expectedInstalmentRepayment = 
         repaymentScheme.getNextInstalmentPaymentDue();
      try {
         loanSettlement.transfer(expectedInstalmentRepayment);
         totalPaymentsMade += expectedInstalmentRepayment;
         borrower.registerNewLoanRepaymentInstallment(
            expectedInstalmentRepayment,
            lender, getInterestRate()
            );
         repaymentScheme.advanceRepaymentPhase();
         if(repaymentScheme.isFullyRepaid()) 
            terminateContract();
         else {
            Simulation.onceCustom(
               this, "tryHonorRepaymentInstalment",
               repaymentScheme.getRepaymentEvent(),
               repaymentScheme.getInstalmentRepaymentInterval()
               );
         }
      } catch (final InsufficientFundsException e) { // Borrower Default
         /** Attempt to resolve the default. */
         final boolean resolutionSuccess = 
            defaultScheme.applyDefaultResolution(
               this, borrower, lender, expectedInstalmentRepayment, 0.);
         if(resolutionSuccess)
            Simulation.onceCustom(
               this, "tryHonorRepaymentInstalment",
               repaymentScheme.getRepaymentEvent(),
               repaymentScheme.getInstalmentRepaymentInterval()
               );
      }
   }
   
   @Override
   public final void terminateContract() {
      if(hasBeenTerminated())
         throw new IllegalStateException(
            "Loan.terminate: error: loan has already been terminated.");
      borrower.removeLiability(this);
      lender.removeAsset(this);
      borrower = null;
      lender = null;
      loanSettlement = null;
   }
   
   @Override
   public boolean hasBeenTerminated() {
      return (loanSettlement == null);
   }

   @Override
   public double getValue() {
      return repaymentScheme.getDebtNow();
   }
   
   @Override
   public void setValue(double newValue) {
      if(newValue < 0.) newValue = 0.;                              // Silent
      final double
         currentValue = getValue(),
         valueChange = newValue - currentValue;
      if(newValue == 0.)
         terminateContract();                                       // Terminate - No Value
      else if(valueChange > 0.)
         try {
            extendLoan(valueChange);                                // Extend
         } catch (final LenderInsufficientFundsException e) {
            throw new ContractModificationException(
               "Loan.setValue: failed to change the value of a loan from " + currentValue
             + " to " + newValue + ".");
         }
      else if(valueChange < 0.)
         writeDownLoan(-valueChange);                               // Write Down
   }
   
   /*
    * Note that it is not possible to manually set the face value 
    * of a loan contract. This is because the amount oweing is
    * determined by the repayment scheme, interest rate, and the 
    * duration of the loan contract.
    */
   @Override
   public double getFaceValue() {
      return repaymentScheme.getEstimatedTotalRepaymentRemaining();
   }

   @Override
   public void setFaceValue(double value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public double getInterestRate() {
      return repaymentScheme.getOverallInterest();
   }
   
   @Override
   public void setInterestRate(double interestRate) {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public void writeDownLoan(final double positiveAmount) {
      if(positiveAmount <= 0.) return;                              // Silent
      repaymentScheme.reduceDebt(positiveAmount);                   // No Lender Compensation
   }
   
   @Override
   public void extendLoan(double positiveAmount) 
      throws LenderInsufficientFundsException {
      if(positiveAmount <= 0.) return;                              // Silent
      RepaymentPlanLoan.transferFromLenderToBorrower(                  // Raises
         positiveAmount,
         lender,
         borrower,
         loanSettlement
         );
      repaymentScheme.extendDebt(positiveAmount);
   }
   
   @Override
   public final Borrower getBorrower() {
      return borrower;
   }
   
   @Override
   public final Lender getLender() {
      return lender;
   }
   
   @Override
   public double getNextInstallmentPayment() {
      return repaymentScheme.getNextInstalmentPaymentDue();
   }
   
   @Override
   public int getTotalNumberOfInstallmentsToRepaySinceInception() {
      return repaymentScheme.getNumberOfPhasesRemaining();
   }
   
   @Override
   public double getLoanPrincipalValue() {
      return repaymentScheme.getTotalPrincipalPayment();
   }
   
   @Override
   public double getTotalAmountToRepayAtInception() {
      return totalPaymentsMade + repaymentScheme.getEstimatedTotalRepaymentRemaining();
   }
   
   @Override
   public double getOverallLoanInterest() {
      return repaymentScheme.getOverallInterest();
   }
   
   @Override
   public double getLoanValueAfterNextInstallment() {
      return repaymentScheme.getDebtInNextPhase();
   }
   
   @Override
   public void transferToNewLender(final Lender newLender) {
      if(newLender == null)
         throw new NullArgumentException();
      this.lender.removeAsset(this);
      this.lender = newLender;
      this.lender.addAsset(this);
      this.loanSettlement = SettlementFactory.createDirectSettlement(borrower, lender);
   }
   
   @Override
   public String toString() {
      return
         "Loan, borrower:" + borrower.getClass() + ", lender:" + lender
         + ", repaymentScheme:" + repaymentScheme.getClass() + ", defaultScheme:"
         + defaultScheme + ".";
   }
}