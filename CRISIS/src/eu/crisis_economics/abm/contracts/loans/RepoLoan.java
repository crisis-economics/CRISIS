/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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
import java.util.List;

import sim.util.Bag;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.bank.central.CentralBankStrategy;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.FixedValueContract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;


/**
 * This is a fixed rate mortgage loan, i.e. borrower pays back same amount (principal + interest) in every time step:
 * 
 * Principal owed goes as (stored in field "value"):
 * 
 * p_n = (1+r)^n P - ((1+r)^n -1) c /r
 *
 * c - fixed payment
 * r - interest rate
 * n - time steps since start of loan (first time step n = 0)
 * P - total amount borrowed, i.e. principal
 * 
 * fixed payment for a loan of maturity N, interest rate r and principal P:
 * 
 * c = (1+r)^N P r /((1+r)^N - 1)
 * 
 * For further reference please refer to: "Fixed rate mortgage" on Wikipedia
 * 
 * 
 * @author olaf
 */
public class RepoLoan extends FixedValueContract implements Loan {
    
    private Bag collateral;
    private double principal;
    private final int installments;
    private final Borrower borrower;
    private Lender lender;
    private Settlement settlement;
    private int payedInstallments = 0;
    private List<String> collateralRegister = new ArrayList<String>();
    private double totalPaymentsMade;

    private boolean terminated;

    private double collateralValue;
        
    boolean cashInjection = false;
        
    /**
     * constructs a loan, registers the loan in borrowers liabilities and lenders assets and schedules repayments.
     *  
     * @param borrower
     * @param lender
     * @param collateral
     * @param interest rate per installment period
     * @param installments total # of partial repayments periods
     * @throws Exception if RepoLoan is set up with the CentralBank and the collateral is already collateralized.
     */
    public RepoLoan(final Borrower borrower, final Lender lender, final double principal, final Bag collateral, final double interest, final int installments) throws Exception {
        this(borrower, lender, principal, collateral, interest, installments, true, true, false);
    }
    
    /**
     * constructs a loan, registers the loan in borrowers liabilities and lenders assets and schedules repayments.
     *  
     * @param borrower
     * @param lender
     * @param collateral
     * @param interest rate per installment period
     * @param installments total # of partial repayments periods
     * @param true if this is a cash injection (non-interbank market)
     * @throws Exception if RepoLoan is set up with the CentralBank and the collateral is already collateralized.
     */
    public RepoLoan(final Borrower borrower, final Lender lender, final double principal, final Bag collateral, final double interest, final int installments, final boolean isThisCashInjection) throws Exception {
        this(borrower, lender, principal, collateral, interest, installments, true, true, isThisCashInjection);
    }
    
    /**
     * @param setupByDefault if <code>true</code>, then the initial money transfer (principal) is transfered automatically,
     *      the <code>value</code>, <code>faceValue</code> fields initialized, and it is scheduled right after initialization;
     *      otherwise, it is the responsibility of the subclass
     * @throws Exception if RepoLoan is set up with the CentralBank and the collateral is already collateralized.
     */
    protected RepoLoan(final Borrower borrower, final Lender lender, final double principal, 
    		final Bag collateral, final double interest, final int installments, 
    		final boolean setupByDefault, final boolean scheduleByDefault, final boolean isThisCashInjection) throws Exception {
        super(Simulation.getTime(),Simulation.getTime()+installments);
        this.borrower = borrower;
        this.lender = lender;
        this.terminated = false;
        this.collateral = collateral;
    //  this.principal = principal;
        
        this.collateralValue = this.getCollateralValue();
//      this.principal = this.collateralValue; this is a bug
        
        this.principal = principal; // fix by christoph
        
        this.cashInjection = isThisCashInjection;
        
        boolean collateralIsOK = true;
        if (lender instanceof CentralBank){
            List<String> cR = ((CentralBankStrategy) ((CentralBank) lender).getStrategy()).getCollateralRegister();
            for (int i=0;i<this.collateralRegister.size();i++){
                if (cR.contains(this.collateralRegister.get(i))){
                    collateralIsOK = false;
                } else {
                    cR.add(this.collateralRegister.get(i));
                }
                
            }
            if (collateralIsOK) ((CentralBankStrategy) ((CentralBank) lender).getStrategy()).setCollateralRegister(cR);
        }
        
        if (collateralIsOK && collateralValue >= principal) {
        this.setInterest( interest );
        this.installments = installments;
        this.settlement = SettlementFactory.createDirectSettlement(borrower, lender);
        borrower.addLiability(this);
        lender.addAsset(this);
        
        try {
            if (setupByDefault) {
                getSettlement().bidirectionalTransfer(-principal);
                
                //this.principal = principal;
                
                super.setValue(this.principal); // TODO This could cause some issues with the subclasses
                //this.faceValue = totalPayment();
                super.setFaceValue(super.getValue());
            }
            
            if (scheduleByDefault) {
               Simulation.once(this, "step", NamedEventOrderings.REPO_LOAN_PAYMENTS);
            }
        } catch (final InsufficientFundsException e) {
            throw new LenderInsufficientFundsException("Lender insufficient funds."); 
        }
        } else {
            throw new Exception("(RepoLoan) Could not set up RepoLoan with CentralBank, because collateral is already collateralized!");
        }
    }
    
    // We need terminate to be public to allow clients to cancel it
    public void terminateContract() {
        this.terminated = true;
        getBorrower().removeLiability(this);
        getLender().removeAsset(this);
      if (lender instanceof CentralBank){
          List<String> cR = ((CentralBankStrategy) ((CentralBank) lender).getStrategy()).getCollateralRegister();
          for (int i=0;i<this.collateralRegister.size();i++){
              if (cR.contains(this.collateralRegister.get(i))){
                  cR.remove(this.collateralRegister.get(i));
              } 
          }
          ((CentralBankStrategy) ((CentralBank) lender).getStrategy()).setCollateralRegister(cR);
      }
    }

    public double getNextInstallmentPayment(){
        //return getCollateralValue()*getInterest()*Math.pow(1+getInterest(),getInstallments())/(Math.pow(1+getInterest(),getInstallments())-1);
        return getLoanPrincipalValue()*getInterest()*Math.pow(1+getInterest(),installments)/(Math.pow(1+getInterest(),installments)-1);
    }
    
    public double getLoanPrincipalValue() {
        return principal;
    }
    
    public double totalPayment(){
        return getNextInstallmentPayment()*installments;
    }

    public double getOverallLoanInterest(){
        return totalPayment()/getCollateralValue()-1;
    }

    public void step() {
        if (!terminated) {
            try {
                getSettlement().transfer(getNextInstallmentPayment());
                totalPaymentsMade += getNextInstallmentPayment();
                setPayedInstallments(getPayedInstallments() + 1);

                this.informLenderInterestPayment();

                super.setValue(
                   super.getValue() * (1 + this.getInterestRate()) - getNextInstallmentPayment());
                super.incrementFaceValue(-getNextInstallmentPayment());
                if (getPayedInstallments() < installments) {
                   Simulation.once(this, "step", NamedEventOrderings.REPO_LOAN_PAYMENTS);
                } else {
                    terminateContract();
                }
            } catch (final InsufficientFundsException e) {
            //  borrower.handleBankruptcy();
                ((Agent) borrower).liquidateAtAfterAll = true;
            }
        }
    }

    
    @Deprecated
    public double getInterest() {
        return getInterestRate();
    }

    @Override
    public int getTotalNumberOfInstallmentsToRepaySinceInception() {
        return installments;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public Lender getLender() {
        return lender;
    }

    public void transferToNewLender(Lender lender) {
        this.lender.removeAsset(this);
        this.lender=lender;
        this.lender.addAsset(this);
        this.settlement = SettlementFactory.createDirectSettlement(borrower, lender);
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public int getPayedInstallments() {
        return payedInstallments;
    }
    
    @Deprecated
    public void setInterest(final double interest) {
        this.setInterestRate(interest);
    }

    public void setPayedInstallments(final int payedInstallments) {
        this.payedInstallments = payedInstallments;
    }
    // returns the value of the loan after the next installment has been paid, allows banks to anticipate their balance sheet in next time period
    public double getLoanValueAfterNextInstallment() {
        return super.getValue()*(1+this.getInterestRate()) - getNextInstallmentPayment();
    }
    
    // returns principal payment
    public double getPrincipalPayment() {
        return getNextInstallmentPayment() - super.getValue()*this.getInterestRate();
    }
    
    private void informLenderInterestPayment() {
    }
    private double getCollateralValue(){
        double value = 0;
        for (int i=0;i<this.collateral.size();i++){
            value += ((Contract) this.collateral.get(i)).getValue();
            this.collateralRegister.add(((Contract) this.collateral.get(i)).getUniqueId().toString());
        }
        return value;
    }
    
    @Override
    public boolean hasBeenTerminated() {
        return terminated;
    }
    
    public void extendLoan(double positiveAmount) {
       throw new UnsupportedOperationException();
    }
    
    @Override
    public void writeDownLoan(double positiveAmount) {
       throw new UnsupportedOperationException();
    }
    
    @Override
    public double getTotalAmountToRepayAtInception() {
       return totalPaymentsMade;
    }
    
    @Override
    public <T> T acceptOperation(LoanOperation<T> operation) {
       return operation.operateOn(this);
    }
}
