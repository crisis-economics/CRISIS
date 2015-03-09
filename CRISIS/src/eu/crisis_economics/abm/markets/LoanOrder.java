/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.markets;

import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters;
import eu.crisis_economics.abm.markets.nonclearing.Instrument;
import eu.crisis_economics.abm.markets.nonclearing.LimitOrder;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;

public abstract class LoanOrder
		extends LimitOrder {
	
	public LoanOrder(final Party party, final Instrument instrument, final double size, final double price)
			throws OrderException {
		this( party, instrument, size, price, DefaultFilters.any() );
	}
	
	public LoanOrder(final Party party, final Instrument instrument, final double size, final double price, final Filter filter)
			throws OrderException {
		super( party, instrument, size, price, filter );
	}
	
	@Override
	public boolean isRegisteredSeller(final Party party) {
		return party instanceof Lender;
	}
	
	@Override
	public boolean isRegisteredBuyer(final Party party) {
		return party instanceof Borrower;
	}
}
