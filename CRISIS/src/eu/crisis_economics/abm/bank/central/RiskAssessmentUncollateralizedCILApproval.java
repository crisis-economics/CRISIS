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
package eu.crisis_economics.abm.bank.central;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.agent.RaisingAgentOperation;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.strategies.riskassessment.BankRiskAssessment;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.LoanOperations;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A simple implementation of the {@link UncollateralizedInjectionLoanApprovalOperation}
  * interface.<br><br>
  * 
  * This object processes an uncollateralized CIL application as follows:<br><br>
  * 
  * (a) the {@link Bank} requesting the CIL is passed to a {@link BankRiskAssessment} 
  *     object;<br>
  * (b) the risk adjustment, R, specified by the {@link BankRiskAssessment}, is computed;<br>
  * (c) if R is less than a fixed threshold, the application is successful. Otherwise,
  *     the application is unsuccessful.<br>
  * 
  * @author phillips
  */
public final class RiskAssessmentUncollateralizedCILApproval
   extends RaisingAgentOperation<Boolean>
   implements UncollateralizedInjectionLoanApprovalOperation {
   
   private final BankRiskAssessment
      riskAssessment;
   private final double
      tolerance;
   
   /**
     * Create a {@link RiskAssessmentUncollateralizedCILApproval} object.
     * 
     * @param assessment
     *        The {@link BankRiskAssessment} scheme to use.
     * @param tolerance
     *        A (fixed) threshold, below which {@link BankRiskAssessment} results will
     *        result in uncollateralized CIL approvals.
     * @param sizeOfLoan
     *        The size of the loan to approve. This argument should be non-negative.
     */
   @Inject
   public RiskAssessmentUncollateralizedCILApproval(
   @Named("RISK_ASSESSMENT_UNCOLLATERALIZED_CASH_INJECTION_LOAN_APPROVAL_RISK_MEASURE")
      final BankRiskAssessment assessment,
   @Named("RISK_ASSESSMENT_UNCOLLATERALIZED_CASH_INJECTION_LOAN_APPROVAL_TOLERANCE")
      final double tolerance
      ) {
      StateVerifier.checkNotNull(assessment);
      this.riskAssessment = assessment;
      this.tolerance = tolerance;
   }
   
   /**
     * Assess an uncollateralized cash injection loan (CIL) application.
     * 
     * @param bank
     *        The {@link Bank} requesting the uncollateralized cash injection loan.
     * @param sum
     *        The size of the cash injection loan requested.
     * @return
     *        {@code True} when, and only when, the uncollateralized loan is approved
     *        according to this policy implementation.
     */
   @Override
   public Boolean operateOn(final Bank bank) {
      final double
         riskAdjustment = riskAssessment.getAdjustment(bank);
      final boolean 
         isEligible = (riskAdjustment < tolerance);
      if(!isEligible) return false;
      /**
        * Check whether the bank has an existing {@link CashInjectionLoan}
        * If so, do not approve an additional uncollateralized loan.
        */
      for(final Loan liability : bank.getLiabilitiesLoans()) {
         if(LoanOperations.isCashInjectionLoan(liability))
            return false;
      }
      return true;
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "Risk-Assessment Based Uncollateralized Cash Injection "
           + "Loan Approval, risk assessment: " + riskAssessment.getClass().getName() 
           + ", tolerance:" + tolerance + ".";
   }
}
