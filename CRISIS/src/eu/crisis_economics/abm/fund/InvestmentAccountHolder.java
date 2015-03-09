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

import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;

interface InvestmentAccountHolder extends DepositHolder {
    /**
      * Invest the specified sum in this account.
      * @param account
      *        The deposit holder, who intends to invest.
      * @param cashInvestment
      *        The size of the cash investment. This argument should be positive.
      *        Should this argument be negative or zero, no action will be taken.
      * @throws InsufficientFundsException
      */
   public void invest(Depositor account, double cashInvestment)
      throws InsufficientFundsException;
   
   /**
     * Used when the releaser buys back previously issued shares and disposes of them,
     * reducing the total number of issued shared. The price paid is the current
     * emissionPrice.
     * 
     * @author DT
     * @param account
     *        The agent who is making the withdrawal request.
     * @param cashValue
     *        How much the agent would like to withdraw. This argument should be
     *        positive. If this argument is negative or zero, no action is taken.
     * @throws InsufficientFundsException
     */
   public void requestWithdrawal(Depositor account, double cashValue) 
      throws InsufficientFundsException;
   
   public double getEmissionPrice();
   public double getBalance(Depositor account);
   
   public void settleOrderBook(double maximumCashCanBePaid) throws InsufficientFundsException;
}
