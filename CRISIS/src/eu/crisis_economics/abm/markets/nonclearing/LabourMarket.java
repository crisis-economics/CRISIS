/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

public class LabourMarket extends Market{

	public LabourMarket() {
      super(NamedEventOrderings.HIRE_WORKERS);
    }

   public LabourMarketOrder addOrder(final Party party, final int maturity, final int size, final double price) throws OrderException, AllocationException {
		return addOrder(party, maturity, size, price, DefaultFilters.any());
	}
	
	public LabourMarketOrder addOrder(final Party party, final int maturity, final int size, final double price, final Filter filter) 
			throws OrderException, AllocationException {
		
		//System.out.println("line 17 of Labour Market " + party +  " orders labour quantity " + size + " price " + price);
		
		if ( null == party ) {
			throw new IllegalArgumentException( "party == null" );
		}
		if ( Double.compare( size, 0 ) == 0 ) {
			throw new IllegalArgumentException( "size == 0" );
		}
		if ( price < 0 ) {
			throw new IllegalArgumentException( price + " == price < 0" );
		}
		
		if (size >0){ //sell order, worker has to allocate labour, do we allocate labour demand?
			((Employee) party).allocateLabour(size);
		}
		LabourInstrument instrument = getInstrument(maturity);
		if (instrument == null) {
			addInstrument(maturity);
			instrument = getInstrument(maturity);
		}
		return new LabourMarketOrder(party, instrument, size, price, filter);
	}

	public void addInstrument(final int maturity) {
		//super.addInstrument(LoanInstrument.generateTicker(maturity));
		if (getInstrument(maturity)==null) {
			if (instrumentMatchingMode == InstrumentMatchingMode.DEFAULT) {
				instruments.put(LabourInstrument.generateTicker(maturity), new LabourInstrument(maturity,updatedOrders));
			} else if (instrumentMatchingMode == InstrumentMatchingMode.ASYNCHRONOUS) {
				instruments.put(LabourInstrument.generateTicker(maturity), new LabourInstrument(maturity,updatedOrders,Instrument.MatchingMode.ASYNCHRONOUS,listeners));
			} else if (instrumentMatchingMode == InstrumentMatchingMode.SYNCHRONOUS) {
				instruments.put(LabourInstrument.generateTicker(maturity), new LabourInstrument(maturity,updatedOrders,Instrument.MatchingMode.SYNCHRONOUS,listeners));
			}
		}
	}

	@Override
   public void addInstrument(final String tickerSymbol) {
		if (getInstrument(tickerSymbol)==null) {
			instruments.put(LabourInstrument.generateTicker(DEFAULTMATURITY), new LabourInstrument(DEFAULTMATURITY,updatedOrders));
		}
	}
	
	public LabourInstrument getInstrument(final int maturity) {
		return (LabourInstrument) getInstrument(LabourInstrument.generateTicker(maturity));
		
	}
	
	public static final int DEFAULTMATURITY =1;// 3;
}
