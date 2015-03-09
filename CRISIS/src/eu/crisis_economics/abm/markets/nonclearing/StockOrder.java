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

import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.Buyer;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.StockSeller;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;

/**
 * Class that represents a StockOrder in the limit order book market.
 * 
 * <p>
 * Note that the effective trading happens between {@link StockHolder} instances, while
 * {@link eu.crisis_economics.abm.contracts.stocks.StockReleaser} pays dividends for the {@link StockHolder} in each time step. For
 * the detail, refer to the description in {@link eu.crisis_economics.abm.contracts.stocks.StockAccount}.
 * </p>
 * 
 * @author rlegendi
 * @since 1.0
 */
public class StockOrder
		extends LimitOrder {
	
	public StockOrder(final Party party, final StockInstrument instrument, final double size, final double price)
			throws OrderException {
		super( party, instrument, size, price, DefaultFilters.any() );
	}
	
	/**
	 * Instantiates a new stock order.
	 * 
	 * @param party the party; cannot be <code>null</code>
	 * @param instrument the instrument; cannot be <code>null</code>
	 * @param size the size; cannot be zero
	 * @param price the price; cannot be negative
	 * @throws OrderException the order exception; if either the buyer or seller is unregistered, or the specified size or
	 *             price is invalid as specified in {@link Order}
	 */
	// TODO Superclass should validate its input...
	public StockOrder(final Party party, final StockInstrument instrument, final double size, final double price,
			final Filter filter)
			throws OrderException {
		super( party, instrument, size, price, filter );
	}
	
	/**
	 * Determines if the specified <code>party</code> is allowed to sell stocks.
	 * 
	 * <p>
	 * Note that the effective trading happens between {@link StockHolder} instances. For the details, refer to the
	 * description in {@link eu.crisis_economics.abm.contracts.stocks.StockAccount}.
	 * </p>
	 * 
	 * @param party the party; cannot be <code>null</code>
	 * @return <code>true</code>, if is registered seller; <code>false</code> otherwise
	 */
	@Override
	public boolean isRegisteredSeller(final Party party) {
		if ( null == party ) {
			throw new IllegalArgumentException( "party == null" );
		}
		
		return party instanceof StockHolder;
	}
	
	/**
	 * Determines if the specified <code>party</code> is allowed buy stocks.
	 * 
	 * <p>
	 * Note that the effective trading happens between {@link StockHolder} instances. For the details, refer to the
	 * description in {@link eu.crisis_economics.abm.contracts.stocks.StockAccount}.
	 * </p>
	 * 
	 * @param party the party; cannot be <code>null</code>
	 * @return <code>true</code>, if is registered buyer; <code>false</code> otherwise
	 */
	@Override
	public boolean isRegisteredBuyer(final Party party) {
		if ( null == party ) {
			throw new IllegalArgumentException( "party == null" );
		}
		
		return party instanceof StockHolder;
	}

	@Override
	protected void disallocatePartyAsset() {
		if (this.getSide()==Order.Side.BUY) {
			((Buyer)this.getParty()).disallocateCash(getOpenSize()*this.getPrice());
		} else {
			((StockSeller)this.getParty()).disallocateShares(
			   this.getInstrument().getInstrumentName(),getOpenSize());
		}		
	}
}
