/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Olaf Bochmann
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

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.markets.Buyer;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

/**
 * The market for commercial loans, that is loans provided to firms.
 * 
 * @author Tamás Máhr
 */
public class CommercialLoanMarket extends LoanMarket {

    public CommercialLoanMarket() {
       super(NamedEventOrderings.COMMERCIAL_MARKET_MATCHING);
    }
	/**
	 * {@inheritDoc}
	 * @throws AllocationException 
	 */
	@Override
	public CommercialLoanOrder addOrder(final Party party, final int maturity, final double size,final double price) throws OrderException, AllocationException {
		return addOrder( party, maturity, size, price, DefaultFilters.any() );
	}

	@Override
	public CommercialLoanOrder addOrder(final Party party, final int maturity, final double size,
			final double price, final Filter filter) throws OrderException, AllocationException {
		if ( null == party ) {
			throw new IllegalArgumentException( "party == null" );
		}
		if ( Double.compare( size, 0 ) == 0 ) {
			throw new IllegalArgumentException( "size == 0" );
		}
		if ( size > 0 ) {	// sell order: allocate cash for principal
			((Buyer) party).allocateCash(size);
		}
		if ( price < 0 ) {
			throw new IllegalArgumentException( price + " == price < 0" );
		}
		LoanInstrument instrument = getInstrument(maturity);
		if (instrument == null) {
			addInstrument(maturity);
			instrument = getInstrument(maturity);
		}
		return new CommercialLoanOrder(party, instrument, size, price, filter);
	}
	
	@Override
	public CommercialLoanOrder addSellOrder(final Party party, final int maturity, final double size, final double price, final Filter filter) throws OrderException, AllocationException {
		if (size < 0){
			throw new IllegalArgumentException("size should be positive (" + size + ")");
		}
		return addOrder(party, maturity, size, price, filter);
	}

	@Override
	public CommercialLoanOrder addSellOrder(final Party party, final int maturity, final double size, final double price) throws OrderException, AllocationException {
		return addSellOrder(party, maturity, size, price, DefaultFilters.any());
	}
	
	@Override
	public CommercialLoanOrder addBuyOrder(final Party party, final int maturity, final double size, final double price, final Filter filter) throws OrderException, AllocationException {
		if (size < 0){
			throw new IllegalArgumentException("size should be positive (" + size + ")");
		}
		return addOrder(party, maturity, -size, price, filter);
	}

	@Override
	public CommercialLoanOrder addBuyOrder(final Party party, final int maturity, final double size, final double price) throws OrderException, AllocationException {
		return addBuyOrder(party, maturity, size, price, DefaultFilters.any());
	}

}
