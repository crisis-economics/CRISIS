/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 Marotta Luca
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Ariel Y. Hoffman
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
package eu.crisis_economics.abm.firm;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.inventory.CashLedger;

/**
  * A simple firm that can deposit money on a bank.
  * 
  * @author olaf
  * @author Luca Marotta
  */
public class Firm extends Agent implements Borrower, Depositor {
    
    /** Initial deposit interest rate for the deposit account associated to the firm by default. */
    public static final double DEPOSIT_INITIAL_INTEREST_RATE = 0.0;
    
    /** Account that stores the firm deposits. */
    private DepositAccount m_depositAccount;

    protected CashLedger cashLedger;
    
    /**
      * Creates a simple firm that can deposit money on a given bank.
      */
    public Firm(
       final DepositHolder depositHolder,
       final AgentNameFactory nameGenerator
       ) {
       this(depositHolder, 0., nameGenerator);
    }
    
    /**
     * Creates a simple firm that can deposit money on a given bank with a given initial deposit.
     */
    public Firm(
       final DepositHolder depositHolder,
       final double initialDeposit,
       final AgentNameFactory nameGenerator
       ) {
       super();
       super.setUniqueName(nameGenerator.generateNameFor(this));
       
       // Automatically added to the assets (!)
       m_depositAccount = new DepositAccount(
          Preconditions.checkNotNull(depositHolder), this,
          initialDeposit, DEPOSIT_INITIAL_INTEREST_RATE
          );
       cashLedger = new CashLedger(this.m_depositAccount);
    }
    
    /**
     * Creates a new firm without any initial deposit account.
     */
    public Firm() { 
        super();
    }
    
    // DOCME
    public void changeDepositAccountTo(final DepositAccount newDepositAccount) {
        synchronized(m_depositAccount) {
           synchronized(newDepositAccount) {
              if (m_depositAccount != null)
                 m_depositAccount.terminate();
              m_depositAccount = newDepositAccount;
              CashLedger newCashLedger = new CashLedger(newDepositAccount);
              newCashLedger.setAllocated(cashLedger.getAllocated());
              cashLedger = newCashLedger;
           }
        }
    }

    /**
     * Returns the total deposit value of the firm.
     * 
     * @return value of the deposit account associated with the household
     */
    public double getDepositValue() {
        return cashLedger.getStoredQuantity();
    }
    
    @Override
    public double credit(double cashAmount) 
       throws InsufficientFundsException {
       if(cashLedger.getUnallocated() < cashAmount)
          throw new InsufficientFundsException();
       return cashLedger.pull(cashAmount);
    }
    
    @Override
    public void debit(double cashAmount) {
        cashLedger.push(cashAmount);
    }
    
    public void terminateDepositAccount() {
        m_depositAccount.terminate();
        cashLedger = null;
    }
    
    /* (non-Javadoc)
     * @see eu.crisis_economics.abm.HasLiabilities#handleBankruptcy()
     */
    @Override
    public void handleBankruptcy() {
        throw new RuntimeException(); 
    }

    @Override
    public void cashFlowInjection(double amt) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void registerNewLoanTransfer(
       double principalCashValue,
       Lender lender, 
       double interestRate) { }
    
    @Override
    public void registerNewLoanRepaymentInstallment(
       double installmentCashValue,
       Lender lender,
       double interestRate) { }
    
   @Override
   public <T> T accept(final AgentOperation<T> operation) {
      return operation.operateOn(this);
   }
}
