/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.markets;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;

/**
  * An interface for entities with stock selling behaviour. This implementation
  * assumes that shares are continuous (non-integer) quantities.
  * 
  * @author olaf
  * @author phillips
  */
public interface StockSeller extends Seller, StockHolder {
	/**
	 * For each submitted stock sell order, the seller needs to block the required shares in such a way that a particular share can not be offered in different orders at the same time.
	 * If blocked shares exceed the total amount of shares AllocationException is thrown.
	 * This is usually called from Market.addOrder().
	 * @param tickerSymbol
	 * @param volume
	 * @throws AllocationException
	 */
	public void allocateShares(String tickerSymbol, double volume) throws AllocationException;

	/**
	 * Disallocates blocked shares, e.g. after order matching or order cancel. 
	 * If volume > blocked shares then blocked shares is set to 0 and IllegalArgumentException is thrown.
	 * @param tickerSymbol
	 * @param volume
	 * @throws IllegalArgumentException
	 */
	void disallocateShares(String tickerSymbol, double volume);
}
