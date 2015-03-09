/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm;

import java.util.List;

import eu.crisis_economics.abm.contracts.Contract;

/**
 * Classes implementing this interface should handle a list of liabilities.
 * 
 * @author Tamás Máhr
 */
public interface HasLiabilities {
	
	/**
	  * Adds a new liability to the object's list of liabilities
	  * 
	  * @param liability
	  *            the new liability to add
	  */
	public void addLiability(Contract liability);

	/**
	  * Removes the given liability from the object's list of liabilities. If the
	  * liability is not a liability of the object, this method does nothing.
	  * 
	  * @param liability
	  *            the liability to remove
	  * @return true if the list of liabilities was modified during the call
	  *          (i.e. the list contained the given liability), and false
	  *         otherwise
	  */
	public boolean removeLiability(Contract liability);
	
	/**
	  * Returns an (<i>unmodifiable</i>) view of the liabilities owned.
	  * 
	  * Note that the return value is an unmodifiable <i>container</i>, 
      * not an unmodifiable collection of <i>unmodifiable contacts</i>.
      * It is permitted to modify contracts in the return value, but it 
      * is not permitted to remove entries.
      * 
      * This method copies the underlying contract references before
      * constructing an unmodifiable view of the balance sheet. Thus, if
      * you terminate a contract by modifying it,
      * ConcurrentModificationException will not be raised.
	  * 
	  * @return list of owned contracts; an <i>empty list</i> otherwise
	  */
	public List<Contract> getLiabilities();
	
	boolean isBankrupt();

	/**
	  * This method implements a liquidation where the agent is brought to an end. 
	  * The assets and property of the agent is redistributed.
	  */
	void handleBankruptcy();
}
