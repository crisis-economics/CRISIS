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
public final class BulletRepaymentSchemeTest {
  final double
     principalValue = 1.,
     overallInterestRate = .25;
  final ScheduleIntervals
     repaymentInterval = ScheduleIntervals.ONE_DAY;
  final double
     expectedInstalmentRepayment = 1.25;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Test: " + this.getClass().getSimpleName());
   }
   
   @Test
   public void testBulletRepaymentStateBeforeAndAfterRepayment() {
      RepaymentScheme repaymentScheme = new BulletRepaymentScheme(
         principalValue / 2.,
         overallInterestRate,
         NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
         repaymentInterval
         );
      repaymentScheme.extendDebt(principalValue / 4.);
      repaymentScheme.extendDebt(principalValue / 8.);
      repaymentScheme.extendDebt(principalValue / 16.);
      repaymentScheme.extendDebt(principalValue / 16.);
      
      // Before payment
      {
         final double instalmentPayment = 
            repaymentScheme.getNextInstalmentPaymentDue();
         System.out.printf("Instalment payment: %16.10g, debt: %16.10g\n", 
            instalmentPayment, repaymentScheme.getDebtNow());
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(
            repaymentScheme.getEstimatedTotalRepaymentRemaining(),
            expectedInstalmentRepayment,
            1.e-8
            );
         Assert.assertEquals(
            repaymentScheme.getDebtNow(),
            principalValue,                     // If no interest were payable/immediate settlement.
            1.e-8
            );
         Assert.assertEquals(
            repaymentScheme.getDebtInNextPhase(),
            0.,
            0.
            );
         Assert.assertEquals(
            repaymentScheme.getNumberOfPhasesRemaining(),
            1
            );
         Assert.assertEquals(
            repaymentScheme.getOverallInterest(),
            overallInterestRate
            );
         Assert.assertEquals(
            repaymentScheme.getTotalPrincipalPayment(),
            principalValue
            );
         Assert.assertEquals(
            repaymentScheme.getInstalmentRepaymentInterval(),
            repaymentInterval
            );
         repaymentScheme.advanceRepaymentPhase();
      }
      
      // After payment
      {
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), true);
         Assert.assertEquals(
            repaymentScheme.getEstimatedTotalRepaymentRemaining(),
            0.,
            0.
            );
         Assert.assertEquals(
            repaymentScheme.getDebtNow(),
            0.,
            0.
            );
         Assert.assertEquals(
            repaymentScheme.getDebtInNextPhase(),
            0.,
            0.
            );
         Assert.assertEquals(
            repaymentScheme.getNumberOfPhasesRemaining(),
            0
            );
         Assert.assertEquals(
            repaymentScheme.getOverallInterest(),
            overallInterestRate
            );
         Assert.assertEquals(
            repaymentScheme.getTotalPrincipalPayment(),
            principalValue
            );
         Assert.assertEquals(
            repaymentScheme.getInstalmentRepaymentInterval(),
            repaymentInterval
            );
         try {
            repaymentScheme.advanceRepaymentPhase(); // Throws IllegalStateException
            Assert.fail();
         }
         catch(IllegalStateException e) { }
      }
   }
   
   @Test
   /**
     * Test the effect of repaymentScheme.reduceScheme for a 
     * bullet loan repayment scheme.
     */
   public void testBulletRepaymentSetValueMethod() {
      { // Part I: direct calls to repaymentScheme.reduceScheme
         RepaymentScheme repaymentScheme = new BulletRepaymentScheme(
            1.5 * principalValue,
            overallInterestRate,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            repaymentInterval
            );
         Assert.assertEquals(repaymentScheme.getDebtNow(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 1);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
            1.5 * principalValue * (1. + overallInterestRate), 1.e-10);
         repaymentScheme.reduceDebt(principalValue);
         Assert.assertEquals(repaymentScheme.getDebtNow(), .5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 1);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
            .5 * principalValue * (1. + overallInterestRate), 1.e-10);
         repaymentScheme.advanceRepaymentPhase();
         Assert.assertEquals(repaymentScheme.getDebtNow(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), true);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 0);
      }
      { // Part II: calls to repaymentScheme.setSchemeValue
         RepaymentScheme repaymentScheme = new BulletRepaymentScheme(
            1.5 * principalValue,
            overallInterestRate,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            repaymentInterval
            );
         Assert.assertEquals(repaymentScheme.getDebtNow(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 1);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
            1.5 * principalValue * (1. + overallInterestRate), 1.e-10);
         repaymentScheme.setDebtValue(.5 * principalValue);
         Assert.assertEquals(repaymentScheme.getDebtNow(), .5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 1);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
            .5 * principalValue * (1. + overallInterestRate), 1.e-10);
         repaymentScheme.advanceRepaymentPhase();
         Assert.assertEquals(repaymentScheme.getDebtNow(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), true);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 0);
      }
      { // Part III: clear all debt on a repayment scheme.
         RepaymentScheme repaymentScheme = new BulletRepaymentScheme(
            1.5 * principalValue,
            overallInterestRate,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            repaymentInterval
            );
         Assert.assertEquals(repaymentScheme.getDebtNow(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 1);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
            1.5 * principalValue * (1. + overallInterestRate), 1.e-10);
         repaymentScheme.setDebtValue(-1.);
         Assert.assertEquals(repaymentScheme.getDebtNow(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(), 0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(), 1.5 * principalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), true);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 0);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(), 0., 1.e-10);
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
      System.out.println("Testing BulletRepaymentScheme..");
      BulletRepaymentSchemeTest test = new BulletRepaymentSchemeTest();
      test.testBulletRepaymentStateBeforeAndAfterRepayment();
      test.testBulletRepaymentSetValueMethod();
      System.out.println("BulletRepaymentScheme tests pass.");
   }
}
