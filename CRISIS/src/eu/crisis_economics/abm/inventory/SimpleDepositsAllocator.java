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

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.contracts.DepositAccount;

/**
  * Simple implementation of the Allocating interface for Deposit
  * Account objects.
  * @author phillips
  */
final class SimpleDepositsAllocator extends AbstractAllocator {
   private final DepositAccount depositAccount;
   
   public SimpleDepositsAllocator(final DepositAccount depositAccount) {
      Preconditions.checkNotNull(depositAccount);
      this.depositAccount = depositAccount;
   }
   
   @Override
   public double getMaximumAllocatable() {
      return depositAccount.getValue();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Deposits Allocator, maximum allocatable: " + getMaximumAllocatable() + ", "
           + "allocated: " + super.getAllocated() + ".";
   }
}
