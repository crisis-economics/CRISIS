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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;

/**
  * @author phillips
  */
public final class FixedRateMortgageRepaymentSchemeTest {
   /** 
    * The following fixed rate mortgage repayment values have 
    * been computed manually. Double principalValue (P) is the 
    * sum transferred to the borrower when the loan was
    * established. Double expectedInstalmentRepayment is the
    * termly loan repayment liability c = r*P/(1-(1+r)**-N),
    * where N is the number of loan instalments.
    */
   final double
      principalValue = 1.,
      interestRate = .2;
   final int
      numRepaymentInstalments = 5;
   final ScheduleIntervals
      repaymentInterval = ScheduleIntervals.ONE_DAY;
   final double
      expectedInstalmentRepayment = 0.33437970328961514,
      expectedTotalRepayment = 1.6718985164480757;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Test: " + this.getClass().getSimpleName());
   }
   
   @Test
   public void testFivePhaseFixedRateMortgageRepaymentScheme() {
      {
         RepaymentScheme repaymentScheme = new FixedRateMortgageRepaymentScheme(
            principalValue / 2.,
            interestRate,
            numRepaymentInstalments,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            repaymentInterval
            );
         repaymentScheme.extendDebt(principalValue / 4.);
         repaymentScheme.extendDebt(principalValue / 8.);
         repaymentScheme.extendDebt(principalValue / 16.);
         
         repaymentScheme.extendDebt(2. * principalValue / 16.);
         repaymentScheme.reduceDebt(principalValue / 16.);
         
         double
            totalRepaymentsMade = 0.,
            expectedTotalRepaymentRemaining = expectedTotalRepayment;
         for(int i = 0; i< numRepaymentInstalments; ++i) {
            repaymentScheme.extendDebt(.1);
            repaymentScheme.reduceDebt(.1);
            final double instalmentPayment = 
               repaymentScheme.getNextInstalmentPaymentDue();
            System.out.printf(
               "Instalment %1d, payment: %16.10g, debt: %16.10g instalments remain: %d\n", 
               i+1, instalmentPayment, repaymentScheme.getDebtNow(), 
               repaymentScheme.getNumberOfPhasesRemaining()
               );
            Assert.assertEquals(expectedInstalmentRepayment, instalmentPayment, 1.e-8);
            Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
            Assert.assertEquals(
               repaymentScheme.getEstimatedTotalRepaymentRemaining(),
               expectedTotalRepaymentRemaining,
               1.e-8
               );
            Assert.assertEquals(
               repaymentScheme.getNumberOfPhasesRemaining(),
               numRepaymentInstalments - i
               );
            // Advance the repayment phase.
            repaymentScheme.advanceRepaymentPhase();
            totalRepaymentsMade += expectedInstalmentRepayment;
            expectedTotalRepaymentRemaining -= expectedInstalmentRepayment;
            Assert.assertEquals(
               repaymentScheme.getEstimatedTotalRepaymentRemaining(),
               expectedTotalRepaymentRemaining,
               1.e-8
               );
            Assert.assertEquals(
               repaymentScheme.getNumberOfPhasesRemaining(),
               numRepaymentInstalments - (i+1)
               );
         }
         System.out.println("Total repayments made: " + totalRepaymentsMade);
         System.out.println("Total debt remaining: " + repaymentScheme.getDebtNow());
         Assert.assertEquals(repaymentScheme.getDebtNow(), 0., 1.e-8);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), true);
         Assert.assertEquals(
            repaymentScheme.getTotalPrincipalPayment(),
            principalValue + principalValue / 16. + .1 * numRepaymentInstalments,
            1.e-10
            );
         Assert.assertEquals(
            repaymentScheme.getInstalmentRepaymentInterval(),
            repaymentInterval
            );
         try {
            // Cannot advance repayment phases on a fully repaid loan.
            repaymentScheme.advanceRepaymentPhase(); // Throws IllegalStateException
            Assert.fail();
         }
         catch(IllegalStateException e) { }
      }
      
      { // Extending an existing fixed rate mortgage.
         FixedRateMortgageRepaymentScheme repaymentScheme =
            new FixedRateMortgageRepaymentScheme(
               principalValue,
               interestRate,
               numRepaymentInstalments,
               NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
               repaymentInterval
               );
            
         for(int i = 0; i< 2; ++i) {
            final double
               instalmentPayment = repaymentScheme.getNextInstalmentPaymentDue();
            System.out.printf("Instalment %1d, payment: %16.10g, debt: %16.10g\n", 
               i+2, instalmentPayment, repaymentScheme.getDebtNow());
            repaymentScheme.advanceRepaymentPhase();
         }
         repaymentScheme.extendDebt(principalValue);
         for(int i = 0; i< 3; ++i) {
            final double
               instalmentPayment = repaymentScheme.getNextInstalmentPaymentDue();
            System.out.printf("Instalment %1d, payment: %16.10g, debt: %16.10g\n", 
               i+2, instalmentPayment, repaymentScheme.getDebtNow());
            repaymentScheme.advanceRepaymentPhase();
         }
         System.out.println("Total debt remaining: " + repaymentScheme.getDebtNow());
         Assert.assertEquals(repaymentScheme.getDebtNow(), 0., 1.e-8);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), true);
         Assert.assertEquals(
            repaymentScheme.getTotalPrincipalPayment(),
            principalValue * 2.
            );
         Assert.assertEquals(
            repaymentScheme.getInstalmentRepaymentInterval(),
            repaymentInterval
            );
      }
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("Test " + this.getClass().getSimpleName() + " is complete.");
   }
   
   /**
    * Manual entry point.
    */
   public static void main(String[] args) {
      System.out.println("Testing FixedRateMortgageRepaymentScheme..");
      FixedRateMortgageRepaymentSchemeTest test = new FixedRateMortgageRepaymentSchemeTest();
      test.testFivePhaseFixedRateMortgageRepaymentScheme();
      System.out.println("FixedRateMortgageRepaymentScheme tests pass.");
   }
}
