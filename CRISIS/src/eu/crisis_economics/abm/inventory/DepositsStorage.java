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
package eu.crisis_economics.abm.inventory;

import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * @author phillips
  */
public final class DepositsStorage extends AbstractStorage {
   private final DepositAccount account;
   
   public DepositsStorage(final DepositAccount account) {
      StateVerifier.checkNotNull(account);
      this.account = account;
   }
   
   @Override
   public double getStoredQuantity() {
      return account.getValue();
   }
   
   @Override
   public boolean isEmpty() {
      return account.getValue() <= 0.;
   }
   
   @Override
   public double pull(double positiveAmount) {
      if(positiveAmount <= 0.) return 0.;                                       // Silent
      positiveAmount = Math.min(positiveAmount, getStoredQuantity());
      try {
         account.withdraw(positiveAmount);
         return positiveAmount;
      } catch (final InsufficientFundsException cannotWithdraw) {
         return 0.;                                                             // No withdrawal
      }
   }
   
   @Override
   public void push(double positiveAmount) {
      if(positiveAmount <= 0.) return;                                          // Silent
      account.deposit(positiveAmount);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Deposit Account storage, account value: " + getStoredQuantity() + ".";
   }
}
