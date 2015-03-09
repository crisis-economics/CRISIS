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

/** 
  * An implementation of the Inventory interface with specialization to
  * deposit accounts. This implementation adds allocation, enqueueing
  * and cashflow memory features to an existing deposit account.
  * 
  * TODO: extend to include overdrafts, if necessary.
  * 
  * @author phillips
  */
public final class CashLedger extends BaseInventory {
   public CashLedger(DepositAccount account) {
      super(new DepositsStorage(account));
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Cash Ledger, account value: " + getStoredQuantity() + ".";
   }
}