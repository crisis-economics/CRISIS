/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
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
package eu.crisis_economics.abm.markets;

import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;
import eu.crisis_economics.abm.markets.nonclearing.InstrumentListener;
import eu.crisis_economics.abm.markets.nonclearing.Order;


/**
 * This is the interface for a market party. A market party provides functions
 * for the order to add and remove active orders in a local order book. It
 * provides a function for the market to update the local state after matching.
 * 
 * @author olaf
 */
public interface Party extends UniquelyIdentifiable {

	/**
	 * called by the order to add the {@code order} to the local order book
	 * 
	 * @param order
	 */
	public void addOrder(Order order);
	
	

	/**
	 * called by the order to remove the {@code order} from the local order
	 * book
	 * 
	 * @param order
	 * @return true if the list of orders was modified during the call (i.e. the
	 *         list contained the given order), and false otherwise
	 */
	public boolean removeOrder(Order order);

	
	/**
	 * called by the market to inform a party about order execution after
	 * matching
	 * 
	 * @param order
	 * @deprecated use {@link InstrumentListener#orderUpdated(InstrumentEvent)} instead.
	 */
	@Deprecated
	public void updateState(Order order);
}
