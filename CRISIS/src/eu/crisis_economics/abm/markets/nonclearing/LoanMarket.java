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
import eu.crisis_economics.abm.markets.LoanOrder;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;


/**
 * This class is abstract because commercial and inter-bank loan markets are
 * basically the same, but we want to have two separate classes for them (to be
 * able to tell them by instanceof). Thus the two loan-markets are derived from
 * this abstract class.
 * 
 * @author olaf
 * @author Tamás Máhr
 */
public abstract class LoanMarket extends Market  {

    public LoanMarket(NamedEventOrderings orderMatchingStep) {
       super(orderMatchingStep);
    }
	/**
	 * Adds a LoanOrder to the market.
	 *
	 * @param party the market party submitting the order
	 * @param maturity the requested maturity of the loan
	 * @param size the requested amount of the loan 
	 * @param price the interest rate limit
	 * @return loanOrder
	 * @throws OrderException if the <code>size</code> is zero or the <code>price</code> is negative.
	 * @throws AllocationException 
	 */
	public abstract LoanOrder addOrder(Party party, int maturity, double size, double price) throws OrderException, AllocationException;
	
	public abstract LoanOrder addOrder(Party party, int maturity, double size, double price, Filter filter) throws OrderException, AllocationException;

	public abstract LoanOrder addBuyOrder(Party party, int maturity, double size, double price) throws OrderException, AllocationException;
	
	public abstract LoanOrder addBuyOrder(Party party, int maturity, double size, double price, Filter filter) throws OrderException, AllocationException;

	public abstract LoanOrder addSellOrder(Party party, int maturity, double size, double price) throws OrderException, AllocationException;
	
	public abstract LoanOrder addSellOrder(Party party, int maturity, double size, double price, Filter filter) throws OrderException, AllocationException;

	public void addInstrument(final int maturity) {
		//super.addInstrument(LoanInstrument.generateTicker(maturity));
		if (getInstrument(maturity)==null) {
			if (instrumentMatchingMode == InstrumentMatchingMode.DEFAULT) {
				instruments.put(LoanInstrument.generateTicker(maturity), new LoanInstrument(maturity,updatedOrders));
			} else if (instrumentMatchingMode == InstrumentMatchingMode.ASYNCHRONOUS) {
				instruments.put(LoanInstrument.generateTicker(maturity), new LoanInstrument(maturity,updatedOrders,Instrument.MatchingMode.ASYNCHRONOUS,listeners));
			} else if (instrumentMatchingMode == InstrumentMatchingMode.SYNCHRONOUS) {
				instruments.put(LoanInstrument.generateTicker(maturity), new LoanInstrument(maturity,updatedOrders,Instrument.MatchingMode.SYNCHRONOUS,listeners));
			}
		}
	}

	public LoanInstrument getInstrument(final int maturity) {
		return (LoanInstrument) getInstrument(LoanInstrument.generateTicker(maturity));
	}

	@Override
	public void addInstrument(final String tickerSymbol) {
		if (getInstrument(tickerSymbol)==null) {
			instruments.put(LoanInstrument.generateTicker(DEFAULTMATURITY), new LoanInstrument(DEFAULTMATURITY,updatedOrders));
		}
	}
	
	/**
	 * default maturity of the loan in time steps.
	 */
	public static final int DEFAULTMATURITY = 1;

}
