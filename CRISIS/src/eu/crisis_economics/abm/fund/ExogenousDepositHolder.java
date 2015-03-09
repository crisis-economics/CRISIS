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
package eu.crisis_economics.abm.fund;

import java.util.List;

import com.google.inject.Inject;

import eu.crisis_economics.abm.HasLiabilities;
import eu.crisis_economics.abm.SimpleHasLiabilities;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.LiquidityException;

/**
  * A simple implementation of an exogenous {@link DepositHolder}. This 
  * implementation describes a {@link DepositHolder} that stores its
  * deposit liabilities in an unmodelled, exogenous location. This entity:
  * 
  * <ul>
  *   <li> has an unlimited, exogenous cash reserve;
  *   <li> cannot be bankrupted;
  *   <li> can hold any number of {@link DepositAccount} liabilities;
  *   <li> takes no action when {@link #cashFlowInjection(double)} is called.
  * </ul>
  * 
  * @author phillips
  */
public final class ExogenousDepositHolder implements DepositHolder {
   
   private HasLiabilities
      hasLiabilities;
   
   /**
     * Create a {@link ExogenousDepositHolder} entity. See {@link ExogenousDepositHolder}.
     */
   @Inject
   public ExogenousDepositHolder() {
      this.hasLiabilities = new SimpleHasLiabilities();
   }
   
   @Override
   public void addLiability(final Contract liability) {
      hasLiabilities.addLiability(liability);
   }
   
   @Override
   public void decreaseCashReserves(final double amount) throws LiquidityException {
      // No action.
   }
   
   @Override
   public boolean removeLiability(final Contract liability) {
      return hasLiabilities.removeLiability(liability);
   }
   
   @Override
   public void increaseCashReserves(final double amount) {
      // No action.
   }
   
   @Override
   public final List<Contract> getLiabilities() {
      return hasLiabilities.getLiabilities();
   }
   
   @Override
   public final void cashFlowInjection(final double amount) {
      // No action.
   }
   
   @Override
   public boolean isBankrupt() {
      return false;
   }
   
   @Override
   public void handleBankruptcy() {
      // No action.
   }
}
