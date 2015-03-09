/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.contracts;

import eu.crisis_economics.abm.HasLiabilities;

/**
 * A DepositHolder is an entity (e.g. a bank) that keeps other entities' (e.g.
 * households') deposits in its book as a liability.
 * 
 * DT 30/07/14: Removed Buyer interface from DepositHolder. It is never used
 * 				by any code.
 * 
 * @author Tamás Máhr
 * @author olaf
 */
public interface DepositHolder extends HasLiabilities {

	/**
	 * any deposit withdraw goes along with a cash decrease of of the deposit holder.
	 * @param amount
	 *        The amount by which to reduce the {@link DepositHolder} cash reserve.
	 *        This argument should be non-negative; otherwise, no action is taken.
	 * 
	 * @throws LiquidityException in case the deposit holder runs into negative cash reserves
	 */
	void decreaseCashReserves(double amount) throws LiquidityException;

	/**
	 * any deposit "deposit" goes along with a cash increase of of the deposit holder 
	 * @param amount
	 */
	void increaseCashReserves(double amount);

	/**
	 * Should implement a cashFlowInjection procedure to handle {@link LiquidityException}. It should raise at least 
	 * amt-reserves cash in order to handle the exception. 
	 * @param amt amount of cash reserves required to handle the InsufficientFundsException
	 */
	void cashFlowInjection(double amount);
}
