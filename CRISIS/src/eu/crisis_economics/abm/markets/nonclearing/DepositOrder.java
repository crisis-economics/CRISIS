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

import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;

/**
 * @author Tamás Máhr
 */
public class DepositOrder extends LimitOrder {
	
	public DepositOrder(final Party party, final DepositInstrument instrument, final double size,
	                    final double price) throws OrderException {
		this(party, instrument, size, price, DefaultFilters.any());
	}
	
	public DepositOrder(final Party party, final DepositInstrument instrument, final double size,
	                    final double price, final Filter filter) throws OrderException {
		super(party, instrument, size, price, filter);
	}
	
	@Override
	public boolean isRegisteredSeller(final Party party) {
		return party instanceof Depositor;
	}
	
	@Override
	public boolean isRegisteredBuyer(final Party party) {
		return party instanceof DepositHolder;
	}

	@Override
	protected void disallocatePartyAsset() {
		// TODO Auto-generated method stub
		
	}
	
}
