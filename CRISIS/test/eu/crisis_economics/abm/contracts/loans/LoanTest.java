/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import sim.util.Bag;
import eu.crisis_economics.abm.CrisisMasonUtils;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author  olaf
 */
public class LoanTest {
    private final double delta = 0.00001;
    private final double initFonds = 1000.0;
    private final double principal = 1000.0;
    private final int installments = 12;
    private final double interest = 0.0215;
    private SimState simState;
    private MyBorrower borrower;
    private MyLender lender;
    private Loan loan;
    
    /**
     * @author  olaf
     */
    private class MyBorrower implements Borrower {

        private double funds;
        private final Bag liabilities;
        private boolean bankrupt;
        private String uniqueID;

        public MyBorrower(final double funds) {
            super();
            this.funds = funds;
            this.liabilities = new Bag();
            this.bankrupt = false;
            this.uniqueID = java.util.UUID.randomUUID().toString();
        }

        @Override
        public double credit(final double amt) throws InsufficientFundsException {
            try {
                if (funds < amt) {
                    throw new InsufficientFundsException("Insufficient Funds!");
                } 
                funds -= amt;
                return (amt);
            }
            finally {}
        }

        @Override
        public void debit(final double amt) {
            funds += amt;           
        }

        @Override
        public void addLiability(final Contract loan) {
            liabilities.add(loan);
            
        }

        @Override
        public boolean removeLiability(final Contract loan) {
            return liabilities.remove(loan);
        }
        
        @Override
        public List<Contract> getLiabilities() {
            return CrisisMasonUtils.bagToList( liabilities );
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleBankruptcy() {
            double totalLiabilities=0;
            for(final Contract contract : new ArrayList<Contract>(liabilities)){
                totalLiabilities+=contract.getFaceValue();  
            }
            System.out.println("Borrower is going into bankrupcy. All loans are terminated. Available funds of " + this.funds + " distributed among Lenders of outstanding loans ( " + totalLiabilities + " ).");
            for(final RepaymentPlanLoan contract : new ArrayList<RepaymentPlanLoan>(liabilities)){
                System.out.println("Lender " + contract.getLender().toString() + " receives "+ contract.getFaceValue()*this.funds/totalLiabilities);
                try {
                    contract.loanSettlement.transfer(contract.getFaceValue()*this.funds/totalLiabilities);
                } catch (InsufficientFundsException e) {
                    e.printStackTrace();
                }
                contract.terminateContract();
            }
            this.bankrupt = true;
        }

        @Override
        public boolean isBankrupt() {
            return bankrupt;
        }

        @Override
        public void cashFlowInjection(double amt) {
            // TODO Auto-generated method stub
        }
        
        @Override
        public void registerNewLoanTransfer(
           double principalCashValue, Lender lender, double interestRate) { }
        
        @Override
        public void registerNewLoanRepaymentInstallment(
           double installmentCashValue, Lender lender, double interestRate) { }

      @Override
      public String getUniqueName() {
         return uniqueID;
      }
    }
    
    /**
     * @author  olaf
     */
    private class MyLender implements Lender {

        private double funds;
        /**
         * @uml.property  name="assets"
         * @uml.associationEnd  
         */
        private final Bag assets;
        private final String uniqueID;

        public MyLender(final double funds) {
            super();
            this.funds = funds;
            this.assets = new Bag();
            this.uniqueID = UUID.randomUUID().toString();
        }

        @Override
        public double credit(final double amt) throws InsufficientFundsException {
            try {
                if (funds < amt) {
                    throw new InsufficientFundsException("Insufficient Funds!");
                } 
                funds -= amt;
                return (amt);
            }
            finally {}
        }

        @Override
        public void debit(final double amt) {
            funds += amt;           
        }

        @Override
        public void addAsset(final Contract loan) {
            assets.add(loan);
            
        }

        @Override
        public boolean removeAsset(final Contract loan) {
            return assets.remove(loan);
        }
        
        @Override
        public List<Contract> getAssets() {
            return Collections.unmodifiableList( CrisisMasonUtils.<Contract>bagToList( assets ) );
        }

        @Override
        public void addOrder(Order order) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean removeOrder(Order order) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        @Deprecated
        public void updateState(Order order) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void cashFlowInjection(double amt) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public double getUnallocatedCash() {
           throw new UnsupportedOperationException();
        }

      @Override
      public String getUniqueName() {
         return uniqueID;
      }
    }
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        System.out.println("Test: " + this.getClass().getSimpleName());
        this.borrower = new MyBorrower(initFonds);
        this.lender = new MyLender(initFonds);
        this.simState = new EmptySimulation(1L);
        this.simState.start();
        this.loan = LoanFactory.createFixedRateMortgage(
           borrower, lender, principal, interest, installments);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        simState.finish();
    }

    /**
     * Test method for {@link eu.crisis_economics.abm.contracts.loans.Loan#instalmentPayment()}.
     */
    @Test
    public final void testInstalmentPayment() {
        System.out.println("LoanTest.testInstalmentPayment:");
        final double payment = loan.getNextInstallmentPayment();
        System.out.println(payment);
        AssertJUnit.assertEquals( 95.43284563320496, payment );
        System.out.println("LoanTest.testInstalmentPayment complete.");
    }

    /**
     * Test method for {@link eu.crisis_economics.abm.contracts.loans.Loan#totalPayment()}.
     */
    @Test
    public final void testTotalPayment() {
        System.out.println("LoanTest.testTotalPayment:");
        final double payment = loan.getTotalAmountToRepayAtInception();
        System.out.println(payment);
        AssertJUnit.assertEquals( 1145.1941475984595, payment );
        System.out.println("LoanTest.testTotalPayment complete.");
    }

    /**
     * Test method for {@link eu.crisis_economics.abm.contracts.loans.Loan#overallInterest()}.
     */
    @Test
    public final void testOverallInterest() {
        System.out.println("LoanTest.testOverallInterest:");
        final double payment = loan.getOverallLoanInterest();
        System.out.println(payment);
        AssertJUnit.assertEquals( 0.1451941475984595, payment );
        System.out.println("LoanTest.testOverallInterest complete.");
    }

    /**
     * Test method for {@link eu.crisis_economics.abm.contracts.loans.Loan#step()}.
     */
    @Test
    public final void testStep() {
        System.out.println("LoanTest.testStep:");
        AssertJUnit.assertEquals(initFonds+principal, borrower.funds);
        AssertJUnit.assertEquals(initFonds-principal, lender.funds);
        AssertJUnit.assertTrue(borrower.liabilities.contains(loan));
        AssertJUnit.assertTrue(lender.assets.contains(loan));
        long lastStep = Simulation.getCycleIndex();
        do{
            while(lastStep == Simulation.getCycleIndex())
               if(!simState.schedule.step(simState)) {
                  AssertJUnit.assertEquals(installments, lastStep);
                  break;
               }
            lastStep = Simulation.getCycleIndex();
            System.out.println(
               "Steps: " + lastStep + 
               " Time: " + simState.schedule.getTime() + 
               " Borrower: " + borrower.funds + 
               " Lender: " + lender.funds
               );
            AssertJUnit.assertEquals(initFonds+principal-(lastStep*loan.getNextInstallmentPayment()), borrower.funds, delta);
            AssertJUnit.assertEquals(initFonds-principal+(lastStep*loan.getNextInstallmentPayment()), lender.funds, delta);
        } while(lastStep < installments);
        simState.finish();
        AssertJUnit.assertFalse(borrower.liabilities.contains(loan));
        AssertJUnit.assertFalse(lender.assets.contains(loan));
        System.out.println("LoanTest.testStep complete.");
    }
    
    /**
     * Test method for {@link eu.crisis_economics.abm.contracts.loans.Loan#step()}.
     */
    @Test(enabled = false)
    public final void testStepBorrowerInsufficientFunds() {
        borrower.funds = principal;
        AssertJUnit.assertEquals(initFonds-principal, lender.funds);
        AssertJUnit.assertTrue(borrower.liabilities.contains(loan));
        AssertJUnit.assertTrue(lender.assets.contains(loan));        
        long steps = 0;
        do{
            if (!simState.schedule.step(simState)) {
                break;
            }
            steps = simState.schedule.getSteps();
            System.out.println("Steps: " + steps + " Time: " + simState.schedule.getTime() + " Borrower: " + borrower.funds + " Lender: " + lender.funds + " value: " + loan.getValue() + " faceValue: " + loan.getFaceValue());
            if (steps>=11) {
                AssertJUnit.assertEquals(0.0,borrower.funds,delta);
                AssertJUnit.assertEquals(1000.0,lender.funds,delta);
            }
        } while(true);
        simState.finish();
        AssertJUnit.assertFalse(borrower.liabilities.contains(loan));
        AssertJUnit.assertFalse(lender.assets.contains(loan));        
    }
    
    @Test
    public void testVisitorLoanOperations() {
       LoanOperation<Boolean> isCashInjection = LoanOperations.createIsCashInjectionQuery();
       Assert.assertFalse(loan.acceptOperation(isCashInjection));
       loan.terminateContract();
       
       try {
          lender.debit(principal);
          loan = LoanFactory.createBulletLoan(borrower, lender, principal, interest);
       } catch (LenderInsufficientFundsException e) {
          Assert.fail();
       }
       Assert.assertFalse(loan.acceptOperation(isCashInjection));
       loan.terminateContract();
       
       try {
          lender.debit(principal);
          loan = LoanFactory.createCouponBond(borrower, lender, principal, interest, installments);
       } catch (LenderInsufficientFundsException e) {
          Assert.fail();
       }
       Assert.assertFalse(loan.acceptOperation(isCashInjection));
       
       try {
          lender.debit(principal);
          loan = LoanFactory.createCashInjectionLoan(
             borrower, lender, principal, interest, installments);
       } catch (LenderInsufficientFundsException e) {
          Assert.fail();
       }
       Assert.assertTrue(loan.acceptOperation(isCashInjection));
    }
    
    public static void main(String[] args) {
       try {
          LoanTest loanTest = new LoanTest();
          loanTest.setUp();
          loanTest.testStep();
          loanTest.tearDown();
       }
       catch(Exception e) {
          Assert.fail();
       }
       
       try {
          LoanTest loanTest = new LoanTest();
          loanTest.setUp();
          loanTest.testInstalmentPayment();
          loanTest.tearDown();
       }
       catch(Exception e) {
          Assert.fail();
       }
       
       try {
          LoanTest loanTest = new LoanTest();
          loanTest.setUp();
          loanTest.testOverallInterest();
          loanTest.tearDown();
       }
       catch(Exception e) {
          Assert.fail();
       }
       
       try {
          LoanTest loanTest = new LoanTest();
          loanTest.setUp();
          loanTest.testTotalPayment();
          loanTest.tearDown();
       }
       catch(Exception e) {
          Assert.fail();
       }
       
       try {
          LoanTest loanTest = new LoanTest();
          loanTest.setUp();
          loanTest.testVisitorLoanOperations();
          loanTest.tearDown();
       }
       catch(Exception e) {
          Assert.fail();
       }
    }
}
