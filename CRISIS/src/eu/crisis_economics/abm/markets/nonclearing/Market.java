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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * The market holds investment instruments.
 * 
 * @author olaf
 */
public abstract class Market {
	
	/**
	 * matching mode for instruments created by addOrder ...
	 * DEFAULT - uses the instruments default mode.
	 */
	public enum InstrumentMatchingMode{ DEFAULT, ASYNCHRONOUS, SYNCHRONOUS }

	private static final InstrumentMatchingMode DEFAULT_INSTRUMENT_MATCHING_MODE = InstrumentMatchingMode.DEFAULT;
	
	protected InstrumentMatchingMode instrumentMatchingMode = DEFAULT_INSTRUMENT_MATCHING_MODE;
	
	/**
	 * holds all updated orders that have been either fully or partially processed, these are pushed to subscribed clients
	 */
	// TODO A simple List would satisfy our needs
	protected BlockingQueue<Order> updatedOrders;
	//protected Bag instruments;// if this was a Map, we could querry it in constant time
	protected Map<String, Instrument> instruments;
	public Map<String, Instrument> getInstruments() {
		return instruments;
	}

	protected Set<InstrumentListener> listeners;

   public Market(NamedEventOrderings matchingStepOrdering) {
      this.updatedOrders = new LinkedBlockingQueue<Order>();
      this.instruments = new HashMap<String, Instrument>();
      this.listeners = new HashSet<InstrumentListener>(); // TODO do we need to synchronize this?
      
      Simulation.repeat(this, "matchOrders", matchingStepOrdering);
   }
   
   @SuppressWarnings("unused")
   // Scheduled
   private void matchOrders() {
      // Match orders (synchronized instruments)
      for (final Instrument instrument : instruments.values())
         if (instrument.isSynchronous())
            try {
               instrument.matchOrders();
            } catch (final InstrumentException e) {
               e.printStackTrace();
            }
	}
	
	/**
	 * Checks if the market has any instrument with the specified <i>tickerSymbol</i>.
	 * 
	 * @param tickerSymbol the unique instrument ID; <i>cannot be null</i>
	 * @return <code>true</code> if an instrument was found with the specified ID, <code>false</code> otherwise
	 */
	public boolean hasInstrument(final String tickerSymbol) {
		if ( null == tickerSymbol ) {
			throw new IllegalArgumentException( "null == tickerSymbol" );
		}
		
		return instruments.containsKey(tickerSymbol);
	}
	
	/**
	 * finds an investment instrument with a given ticker symbol on the market. returns null if instrument does not exists.
	 * @param tickerSymbol
	 * @return
	 */
	public Instrument getInstrument(final String tickerSymbol) {
		return instruments.get(tickerSymbol);
//		for(int i = 0; i < instruments.size(); i++) {
//			Instrument instrument = (Instrument)instruments.get(i);
//			if (instrument.getTickerSymbol().equals(tickerSymbol)) return instrument;
//		}
//		return null;
	}

	/**
	 * adds an investment instrument with given tickerSymbol
	 * @param tickerSymbol
	 */
	abstract public void addInstrument(String tickerSymbol);

	@Override
	public String toString() {
		return this.instruments.size() + " (instruments) "
				+ this.updatedOrders.size() + " (updatedOrders) "
				;
	}
	
	/**
	 * Removes all currently active ask offers, bid offers, all the partially filled orders, and also cleans up the list of
	 * updated orders.
	 */
	public void cancelAllOrders() {
		for (final Instrument act : instruments.values()) {
			act.cancelAllOrders();
		}
		
		// This is a temporal solution to solve some Mark1-related performance issues
		updatedOrders.clear();
	}

	public InstrumentMatchingMode getInstrumentMatchingMode() {
		return instrumentMatchingMode;
	}


	/**
	 * sets the matching mode for instruments created by addOrder ...
	 * @param instrumentMatchingMode
	 */
	public void setInstrumentMatchingMode(
			final InstrumentMatchingMode instrumentMatchingMode) {
		this.instrumentMatchingMode = instrumentMatchingMode;
	}
	
	public void addListener(final InstrumentListener listener) {
	    this.listeners.add(listener);
	}
	 
	public void removeListener(final InstrumentListener listener) {
	    this.listeners.remove(listener);
	}
}
