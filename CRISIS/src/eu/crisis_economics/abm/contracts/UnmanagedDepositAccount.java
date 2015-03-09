/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.contracts;


/**
 * This is a deposit account whose <code>value</code> can go into negative without throwing an
 * {@link InsufficientFundsException}.
 * 
 * <p>
 * Otherwise, it behaves just as if it were a simple {@link DepositAccount}.
 * </p>
 * 
 * @author rlegendi
 */
public class UnmanagedDepositAccount
		extends DepositAccount {

	public UnmanagedDepositAccount(final DepositHolder depositHolder, final Depositor depositor, final double amount,
			final double interestRate) {
		super( depositHolder, depositor, amount, interestRate );
	}
	
	@Override
	public void withdraw(final double amount)
			throws InsufficientFundsException {
		super.getDepositHolder().decreaseCashReserves(amount);		
		decreaseValue( amount );
	}
}
