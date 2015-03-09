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
public final class ExtendedCouponBondRepaymentSchemeTest {
   /** 
    * The following expected coupon repayment values 
    * are specified in terms of:
    * (a) double bondPrincipalValue - the initial transfer
    *     principal of the bond.
    * (b) double couponInterestRate - the proportion of the
    *     bond principal to be repaid in each coupon.
    * (c) int numBondCoupons - the total number of coupons
    *     to be repaid. If this value is 1, then the bond
    *     principal and exactly one coupon are repaid
    *     simultaneously in future.
    */
   final double
      bondPrincipalValue = 1.,
      couponInterestRate = .2;
   final int
      numBondCoupons = 5;
   final ScheduleIntervals
      couponPaymentInterval = ScheduleIntervals.ONE_DAY;
   final double
      expectedNonfinalCouponValue = 
         couponInterestRate * bondPrincipalValue,
      expectedTotalRepayment =
         (1. + numBondCoupons * couponInterestRate) * bondPrincipalValue;
   double
      expectedOutstandingRepayment = expectedTotalRepayment;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Test: " + this.getClass().getSimpleName());
   }
   
   @Test
   public void testFivePhaseCouponBondRepaymentScheme() {
      {
         RepaymentScheme repaymentScheme = new CouponBondRepaymentScheme(
            bondPrincipalValue / 2.,
            couponInterestRate,
            numBondCoupons,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            couponPaymentInterval
            );
         repaymentScheme.extendDebt(bondPrincipalValue / 4.);
         repaymentScheme.extendDebt(bondPrincipalValue / 8.);
         repaymentScheme.extendDebt(bondPrincipalValue / 16.);
         repaymentScheme.extendDebt(bondPrincipalValue / 16.);
         
         double
            totalRepaymentsMade = 0.;
         for(int i = 0; i< numBondCoupons; ++i) {
            final double instalmentPayment = 
               repaymentScheme.getNextInstalmentPaymentDue();
            System.out.printf("Instalment %1d, payment: %16.10g, debt: %16.10g\n", 
               i+1, instalmentPayment, repaymentScheme.getDebtNow());
            if(i != numBondCoupons - 1)
               Assert.assertEquals(
                  expectedNonfinalCouponValue, instalmentPayment, 1.e-8);
            else
               Assert.assertEquals(
                  expectedNonfinalCouponValue + bondPrincipalValue, instalmentPayment, 1.e-8);
            Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
            Assert.assertEquals(
               repaymentScheme.getEstimatedTotalRepaymentRemaining(),
               expectedOutstandingRepayment,
               1.e-8
               );
            Assert.assertEquals(
               repaymentScheme.getNumberOfPhasesRemaining(),
               numBondCoupons - i
               );
            // Advance the repayment phase.
            repaymentScheme.advanceRepaymentPhase();
            totalRepaymentsMade += instalmentPayment;
            Assert.assertEquals(
               repaymentScheme.getNumberOfPhasesRemaining(),
               numBondCoupons - (i+1)
               );
            expectedOutstandingRepayment -= instalmentPayment;
         }
         System.out.println("Total repayments made: " + totalRepaymentsMade);
         System.out.println("Total debt remaining: " + repaymentScheme.getDebtNow());
         Assert.assertEquals(repaymentScheme.getDebtNow(), 0., 1.e-8);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), true);
         Assert.assertEquals(
            repaymentScheme.getTotalPrincipalPayment(),
            bondPrincipalValue
            );
         Assert.assertEquals(
            repaymentScheme.getInstalmentRepaymentInterval(),
            couponPaymentInterval
            );
         try {
            // Cannot advance repayment phases on a fully repaid loan.
            repaymentScheme.advanceRepaymentPhase(); // Throws IllegalStateException
            Assert.fail();
         }
         catch(IllegalStateException e) { }
      }
      
      { // Extending an existing bond coupon repayment scheme.
         RepaymentScheme repaymentScheme = new CouponBondRepaymentScheme(
            bondPrincipalValue,
            couponInterestRate,
            numBondCoupons,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            couponPaymentInterval
            );
            
         for(int i = 0; i< 2; ++i) {
            final double
               instalmentPayment = repaymentScheme.getNextInstalmentPaymentDue();
            System.out.printf("Instalment %1d, payment: %16.10g, debt: %16.10g\n", 
               i+2, instalmentPayment, repaymentScheme.getDebtNow());
            repaymentScheme.advanceRepaymentPhase();
         }
         repaymentScheme.extendDebt(bondPrincipalValue);
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
            bondPrincipalValue * 2.
            );
         Assert.assertEquals(
            repaymentScheme.getInstalmentRepaymentInterval(),
            couponPaymentInterval
            );
      }
   }
   
   /**
    * Test the effect of repaymentScheme.reduceScheme for a 
    * bullet loan repayment scheme.
    */
   public void testBulletRepaymentSetValueMethod() {
      { // Part I: direct calls to repaymentScheme.reduceScheme
         RepaymentScheme repaymentScheme = new CouponBondRepaymentScheme(
            1.5 * bondPrincipalValue,
            couponInterestRate,
            numBondCoupons,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            couponPaymentInterval
            );
         Assert.assertEquals(repaymentScheme.getDebtNow(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), numBondCoupons);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
            1.5 * bondPrincipalValue * couponInterestRate, 1.e-10);
         repaymentScheme.reduceDebt(bondPrincipalValue);
         Assert.assertEquals(repaymentScheme.getDebtNow(),
            .5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(),
            .5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), numBondCoupons);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
           .5 * bondPrincipalValue * couponInterestRate, 1.e-10);
         repaymentScheme.advanceRepaymentPhase();
         Assert.assertEquals(repaymentScheme.getDebtNow(), 
           .5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), numBondCoupons-1);
      }
      { // Part II: calls to repaymentScheme.setDebtValue
         RepaymentScheme repaymentScheme = new CouponBondRepaymentScheme(
            1.5 * bondPrincipalValue,
            couponInterestRate,
            numBondCoupons,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            couponPaymentInterval
            );
         Assert.assertEquals(repaymentScheme.getDebtNow(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), numBondCoupons);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
            1.5 * bondPrincipalValue * couponInterestRate, 1.e-10);
         repaymentScheme.setDebtValue(.5 * bondPrincipalValue);
         Assert.assertEquals(repaymentScheme.getDebtNow(),
            .5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(),
            .5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), numBondCoupons);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
           .5 * bondPrincipalValue * couponInterestRate, 1.e-10);
         repaymentScheme.advanceRepaymentPhase();
         Assert.assertEquals(repaymentScheme.getDebtNow(), 
           .5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), numBondCoupons-1);
      }
      { // Part III: calls to repaymentScheme.setDebtValue
         RepaymentScheme repaymentScheme = new CouponBondRepaymentScheme(
            1.5 * bondPrincipalValue,
            couponInterestRate,
            numBondCoupons,
            NamedEventOrderings.COMMERCIAL_LOAN_PAYMENTS,
            couponPaymentInterval
            );
         Assert.assertEquals(repaymentScheme.getDebtNow(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), false);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), numBondCoupons);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
            1.5 * bondPrincipalValue * couponInterestRate, 1.e-10);
         repaymentScheme.setDebtValue(-1.);
         Assert.assertEquals(repaymentScheme.getDebtNow(),
            0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getDebtInNextPhase(),
            0., 1.e-10);
         Assert.assertEquals(repaymentScheme.getTotalPrincipalPayment(),
            1.5 * bondPrincipalValue, 1.e-10);
         Assert.assertEquals(repaymentScheme.isFullyRepaid(), true);
         Assert.assertEquals(repaymentScheme.getNumberOfPhasesRemaining(), 0);
         Assert.assertEquals(repaymentScheme.getNextInstalmentPaymentDue(),
           0., 1.e-10);
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
      System.out.println("Testing CouponBondRepaymentScheme..");
      ExtendedCouponBondRepaymentSchemeTest test = new ExtendedCouponBondRepaymentSchemeTest();
      test.testFivePhaseCouponBondRepaymentScheme();
      test.testBulletRepaymentSetValueMethod();
      System.out.println("CouponBondRepaymentScheme tests pass.");
   }
}
