/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.markets.nonclearing;

import eu.crisis_economics.abm.markets.LoanOrder;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;

/**
 * A marker class to be able to differentiate between commercial loans orders
 * and interbank loan orders.
 * 
 * @author Tamás Máhr
 */
public class InterbankLoanOrder extends LoanOrder {
	
	public InterbankLoanOrder(final Party party, final LoanInstrument instrument,
	                          final double size, final double price) throws OrderException {
		this(party, instrument, size, price, DefaultFilters.any());
	}
	
	public InterbankLoanOrder(final Party party, final LoanInstrument instrument,
	                          final double size, final double price, final Filter filter) throws OrderException {
		super(party, instrument, size, price, filter);
	}

	@Override
	protected void disallocatePartyAsset() {
		if (this.getSide()==Order.Side.SELL) {
			((LoanSeller)this.getParty()).disallocateCash(getOpenSize());
		} 
	}
	
}
