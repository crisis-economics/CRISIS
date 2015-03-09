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
package eu.crisis_economics.abm.fund;

import java.util.List;

import eu.crisis_economics.abm.HasLiabilities;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.LiquidityException;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * Simple implementation the DepositHolder interface.
  */
public class MDepositHolder implements DepositHolder {
   
   public MDepositHolder(
      final HasLiabilities parent,
      final Contract cashReserveContract
      ) {
      StateVerifier.checkNotNull(parent, cashReserveContract);
      this.iHasLiabilities = parent;
      this.cashReserve = cashReserveContract;
   }
   
   Contract
      cashReserve;
   
   // /////////////////////////////////////////////////////////////////////////////////////
   // DepositHolder interface
   // /////////////////////////////////////////////////////////////////////////////////////
   
   @Override
   public void decreaseCashReserves(
      final double amount
      ) throws LiquidityException {
      final double cash = cashReserve.getValue();
      if(amount <= 0.) return;
      if(cash < amount) {
         throw new LiquidityException(
            "DepositHolder.decreaseCashReserves: deposit holder is experiencing a liquidity "
          + "crisis. Size of cash reserves: " + cash + ", decrease requested: " + amount + "."
          );
      }
      if (cashReserve instanceof DepositAccount) { // TODO: must be a better alternative to this.
         try {
            ((DepositAccount) cashReserve).withdraw(amount);
         } catch (final InsufficientFundsException e) {
            throw (new LiquidityException());
         }
      } else {
         cashReserve.setValue(cash - amount);
      }
   }
   
   @Override
   public void increaseCashReserves(
      final double amount
      ) {
      final double
         cash = cashReserve.getValue();
      if (amount <= 0.) return;
      if (cashReserve instanceof DepositAccount) { // TODO: must be a better alternative to this.
         ((DepositAccount) cashReserve).deposit(amount);
      } else {
         cashReserve.setValue(cash + amount);
      }
   }
   
    @Override
    public void cashFlowInjection(double amount) {
       if (amount <= 0.)
          return;
       System.err.println(
          "DepositHolder.cashFlowInjection: cash flow injection is not implemented" + 
          " for this type.");
       System.err.flush();
    }
   
   ///////////////////////////////////////////////////////////////////////////////////////
   // HasLiabilities forwarder
   ///////////////////////////////////////////////////////////////////////////////////////
   
   HasLiabilities
      iHasLiabilities;
   
   @Override
   public void addLiability(final Contract liability) {
      iHasLiabilities .addLiability(liability);
   }
   
   @Override
   public boolean removeLiability(final Contract liability) {
      return (iHasLiabilities.removeLiability(liability));
   }
   
   @Override
   public List<Contract> getLiabilities() {
      return (iHasLiabilities.getLiabilities());
   }
   
   @Override
   public boolean isBankrupt() {
      return (iHasLiabilities.isBankrupt());
   }
   
   @Override
   public void handleBankruptcy() {
      iHasLiabilities.handleBankruptcy();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Deposit Holder, cash reserve value: " + cashReserve.getValue() + ".";
   }
}
