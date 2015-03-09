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
package eu.crisis_economics.abm.contracts.settlements;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;

/**
 * Minimal functionality requirements for a settlement party. 
 * 
 * @author olaf
 */
public interface SettlementParty extends UniquelyIdentifiable {
	
	/**
	 * Should implement withdraw of funds
	 *  
	 * @param amt amount of funds to be withdrawn
	 * @return amount of funds withdrawn
	 * @throws InsufficientFundsException should be thrown if credit is not possible
	 */
	public double credit (double amt) throws InsufficientFundsException;
	
	/**
	 * Should implement deposit of funds
	 *  
	 * @param amt amount of funds to be deposited
	 */
	public void debit (double amt);

	/**
	 * Should implement a cashFlowInjection procedure to handle {@link InsufficientFundsException}. 
	 * It should raise at least 
	 * amt-reserves cash in order to handle the exception. 
	 * @param amt amount of cash reserves required to handle the InsufficientFundsException.
	 *        The argument should be non-negative; otherwise, no action is taken.
	 */
	public void cashFlowInjection(double amt);
}
