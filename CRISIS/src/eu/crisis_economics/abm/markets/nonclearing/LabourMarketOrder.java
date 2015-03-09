/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.markets.nonclearing;

import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;

public class LabourMarketOrder extends Order {

	public LabourMarketOrder(Party party, Instrument instrument, double size,
			double price, Filter filter) throws OrderException {
		super(party, instrument, size, price);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void disallocatePartyAsset() {
		if (this.getSide()==Order.Side.BUY) {
			((Employer)this.getParty()).disallocateLabour(this.getOpenSize());
		} else {
			((Employee)this.getParty()).disallocateLabour(this.getOpenSize());
		}		
	}

	@Override
	public boolean isRegisteredSeller(Party party) {
		if (party instanceof Employee) return true; 
		else return false;
	}

	@Override
	public boolean isRegisteredBuyer(Party party) {
		if (party instanceof Employer) return true; 
		else return false;
	}

}
