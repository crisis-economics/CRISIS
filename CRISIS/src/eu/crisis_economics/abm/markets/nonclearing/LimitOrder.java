/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Olaf Bochmann
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

/**
 * a simple limit order
 * @author olaf
 *
 */
public abstract class LimitOrder extends Order {

	/**
	 * creates a limit order object
	 * @param party the market party placing the order (Seller or Buyer)
	 * @param size to be traded (offered or requested)
	 * @throws OrderException 
	 */
	public LimitOrder (final Party party, final Instrument instrument, final double size, final double price, final Filter filter) throws OrderException {
		super(party, instrument, size, price, filter);
//		double epsilon = 0.000001;
//		if (Math.abs(price % Instrument.TICKSIZE) > epsilon) 
//			throw new OrderException("price ("+ price +") is not multiple of ticksize ("+Instrument.TICKSIZE+")");
//		if (((size - Instrument.LOTSIZEMINIMUM)/Instrument.LOTSIZEINCREMENT) % 1 > epsilon) 
//			throw new OrderException("size ("+ size +") is not compatible with lotsize minimum ("+Instrument.LOTSIZEMINIMUM+") and lotsize increment ("+Instrument.LOTSIZEINCREMENT+")");
	}

}
