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

import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory.CentralBankStrategyFactory;
import eu.crisis_economics.utilities.StateVerifier;

public final class CentralBank extends ClearingBank {
   
   private UncollateralizedInjectionLoanApprovalOperation
      ucilApprovalPolicy;
   
   /**
     * Create a {@link CentralBank} {@link Agent}.
     * 
     * @param initialCash
     *        The initial cash endowment of the {@link CentralBank}.
     * @param ucilApprovalPolicy
     *        The Uncollateralized Cash Injection Loan approval policy for
     *        the {@link CentralBank}.
     */
   @Inject
   public CentralBank(
   @Named("CENTRAL_BANK_INITIAL_CASH_ENDOWMENT")
      final double initialCash,
      final CentralBankStrategyFactory strategyFactory,
   @Named("CENTRAL_BANK_CLEARING_MARKET_PARTICIPATION")
      final ClearingMarketParticipantFactory marketParticipationFactory,
      final ClearingHouse clearingHouse,
   @Named("CENTRAL_BANK_UNCOLLATERALIZED_CASH_INJECTION_LOAN_POLICY")
      final UncollateralizedInjectionLoanApprovalOperation ucilApprovalPolicy
      ) {
      super(
         initialCash,
         strategyFactory,
         marketParticipationFactory,
         clearingHouse
         );
      StateVerifier.checkNotNull(ucilApprovalPolicy);
      this.ucilApprovalPolicy = ucilApprovalPolicy;
      
      clearingHouse.addStockMarketParticipant(this);
      UniqueStockExchange.Instance.removeStock(this);    // TODO: Troubled Bank hierarchy.
   }
   
   /**
     * Apply for a central bank uncollateralized cash injection loan (CIL).
     * The {@link CentralBank} may or may not approve the CIL.
     * 
     * @param bank
     *        The {@link Bank} requesting the CIL.
     * @param amount
     *        The cash size of the requested cash injection loan. This argument
     *        should be non-negative; otherwise, no action is taken and this 
     *        method returns {@code null}.
     * @return
     *        A {@link Loan} contract (the CIL) on success, or <code>null</code>,
     *        if the application is rejected. This method does not otherwise
     *        modify the {@link Bank} that requests the CIL.
     */
   public Loan applyForUncollateralizedCashInjectionLoan(
      final Bank bank,
      final double amountToBorrow
      ) {
      if(amountToBorrow <= 0.) {
         System.err.println(
            "CentralBank.applyForUncollateralizedCashInjectionLoan: cash injection amount "
          + "requested (value " + amountToBorrow + ") is negative. No action was taken.");
         System.err.flush();
         return null;
      }
      final boolean
         approved = ucilApprovalPolicy.operateOn(bank);
      if(!approved)
         return null;
      return tryExecuteUncollateralizedCashInjectionLoan(bank, amountToBorrow);
   }
   
   /**
     * Implementation detail. Try to create an uncollateralized cash injection
     * (CIL) loan for the specified {@link Borrower}. This method will create the 
     * CIL if the {@link CentralBank} has sufficient funds to create the {@link Loan}.
     * 
     * @param borrower
     *        The {@link Borrower} who desires the CIL.
     * @param amount
     *        The amount the {@link Borrower} wishes to borrow.
     */
   private Loan tryExecuteUncollateralizedCashInjectionLoan(
      final Borrower borrower,
      final double amount
      ) {
      try {
         Loan result = LoanFactory.createCashInjectionLoan(
            borrower, this, amount,
            getStrategy().getRefinancingRate(), 1);
         System.out.println(
            "CentralBank.tryExecuteUncollateralizedCashInjectionLoan: central bank has "
          + "approved an uncollateralized cash injection loan of size " + amount + " for "
          + borrower.getUniqueName() + ".");
         return result;
      } catch (final LenderInsufficientFundsException failed) {
         System.err.println(
            "CentralBank.tryExecuteUncollateralizedCashInjectionLoan: central bank approved "
          + "an uncollateralized cash injection loan, but has insufficient funds to execute "
          + "the contract. No action was taken."
            );
         System.err.flush();
         return null;
      }
   }
   
   /**
     * Get the {@link CentralBankStrategy} for this bank. This method is
     * equivalent to (CentralBankStrategy) getStrategy().
     */
   @Override
   public final CentralBankStrategy getStrategy() {
      return (CentralBankStrategy) super.getStrategy();
   }
}