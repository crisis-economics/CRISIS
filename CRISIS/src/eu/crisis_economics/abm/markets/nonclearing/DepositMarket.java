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

import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.markets.nonclearing.DepositInstrument.AccountType;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

/**
 * @author Tamás Máhr
 */
public class DepositMarket extends Market {
	
	public DepositMarket() {
      super(NamedEventOrderings.DEPOSIT_MARKET_MATCHING);
   }

   @Override
	public void addInstrument(final String tickerSymbol) {
		if (getInstrument(tickerSymbol) == null){
			if (instrumentMatchingMode == InstrumentMatchingMode.DEFAULT) {
				instruments.put(tickerSymbol, new DepositInstrument(updatedOrders, AccountType.valueOf(tickerSymbol.substring(tickerSymbol.indexOf('_') + 1))));
			} else if (instrumentMatchingMode == InstrumentMatchingMode.ASYNCHRONOUS) {
				instruments.put(tickerSymbol, new DepositInstrument(updatedOrders, AccountType.valueOf(tickerSymbol.substring(tickerSymbol.indexOf('_') + 1)), Instrument.MatchingMode.ASYNCHRONOUS,listeners));
			} else if (instrumentMatchingMode == InstrumentMatchingMode.SYNCHRONOUS) {
				instruments.put(tickerSymbol, new DepositInstrument(updatedOrders, AccountType.valueOf(tickerSymbol.substring(tickerSymbol.indexOf('_') + 1)), Instrument.MatchingMode.SYNCHRONOUS,listeners));
			}
		}
	}
	
	/**
	 * Creates a new {@link DepositOrder} object and adds it to this
	 * {@link DepositMarket}. A buy order (with positive <code>size</code>)
	 * should be submitted by parties wishing to deposit money. The
	 * <code>size</code> in this case doesn't matter, any amount should be
	 * depositable. A sell order (with negative <code>size</code>) should be
	 * submitted by parties wishing the hold deposits (e.g. banks). The
	 * <code>size</code> in this case should be a very large negative number, so
	 * that it can accommodate any sell order.
	 * 
	 * @param party the party submitting the order
	 * @param type the type of the deposit (see {@link AccountType})
	 * @param size any positive number of a sell order, and a very negative number for a buy order
	 * @param price the limit interest rate
	 * @return the created new order as a <code>DepositOrder</code>
	 * @throws OrderException if the give <code>size</code> is zero, or the <code>price</code> is negative
	 */
	public DepositOrder addOrder(final Party party, final AccountType type, final double size, final double price) throws OrderException {
		return addOrder( party, type, size, price, DefaultFilters.any() );
	}
	
	public DepositOrder addOrder(final Party party, final AccountType type, final double size, final double price, final Filter filter) throws OrderException {
		// there is only one type of deposit instruments at the moment
		final String ticker = DepositInstrument.generateTicker(type);
		DepositInstrument instrument = (DepositInstrument) getInstrument(ticker);
		
		if (instrument == null){
			instrument = new DepositInstrument(updatedOrders, type);
			instruments.put(ticker, instrument);
		}
		
		return new DepositOrder(party, instrument, size, price, filter);
	}

	public DepositOrder addBuyOrder(final Party party, final AccountType type, final double size, final double price) throws OrderException {
		return addBuyOrder(party, type, size, price, DefaultFilters.any());
	}

	public DepositOrder addBuyOrder(final Party party, final AccountType type, final double size, final double price, final Filter filter) throws OrderException {
		if (size < 0){
			throw new IllegalArgumentException("size should be positive (" + size + ")");
		}
		return addOrder(party, type, -size, price, filter);
	}

	public DepositOrder addSellOrder(final Party party, final AccountType type, final double size, final double price) throws OrderException {
		return addSellOrder(party, type, size, price, DefaultFilters.any());
	}

	public DepositOrder addSellOrder(final Party party, final AccountType type, final double size, final double price, final Filter filter) throws OrderException {
		if (size < 0){
			throw new IllegalArgumentException("size should be positive (" + size + ")");
		}
		return addOrder(party, type, size, price, filter);
	}
}
