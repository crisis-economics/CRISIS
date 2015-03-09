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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.StockBuyer;
import eu.crisis_economics.abm.markets.StockSeller;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;

/**
 * Implementation for the stock market.
 * 
 * <p>
 * Among the usual functions the <code>StockMarket</code> allows access to all instrument names that are present on the
 * market.
 * </p>
 * 
 * @author rlegendi
 * @since 1.0
 * @see StockOrder
 */
public class StockMarket extends Market {
    public StockMarket() {
      super(NamedEventOrderings.STOCK_MARKET_MATCHING);
    }

   /**
	 * Creates a new {@link StockOrder} for the given <code>stockName</code> and adds it to the {@link StockMarket}.
	 * 
	 * <p>
	 * The new order is a sell order if the given <code>quantity</code> is positive, it is a buy order if the
	 * <code>quantity</code> is negative, and an {@link OrderException} is thrown otherwise. The new order is a limit order,
	 * if the given <code>pricePerShare</code> is positive, a market order if it is zero, and an OrderException is thrown if
	 * it is negative.
	 * </p>
	 * 
	 * @param party the party submitting the order to the market, must be a {@link StockHolder}; cannot be <code>null</code>
	 * @param stockName the name of the stock; cannot be <code>null</code>
	 * @param quantity the amount of stock to be sold (if positive) or bought (if negative); cannot be zero
	 * @param pricePerShare the limit price; cannot be negative
	 * @return the stock order created
	 * @throws OrderException if the <code>quantity</code> is zero, or the <code>pricePerShare</code> is negative.
	 * @throws AllocationException 
	 */
	public StockOrder addOrder(
	   final Party party,
	   final String stockName,
	   final double quantity,
	   final double pricePerShare
	   )
	   throws OrderException, AllocationException {
		return addOrder( party, stockName, quantity, pricePerShare, DefaultFilters.any() );
	}
	
	/**
	  * Add a new stock market order. This method accepts a quantity (the number of
	  * shares) and a price per share (PPS) value. If the party is required to allocate
	  * cash (for a buy order), the size of the order will be reduced based on
	  * amount of cash that the party could actually allocate. For example if the 
	  * buyer was able to allocate 50% of the cash cost of the order, the size of the 
	  * order is reduced by 50% to match the amount that the buyer could afford. If
	  * the cash allocation fails entirely, AllocationException is raised.
	  */
	public StockOrder addOrder(
	   final Party party,
	   final String stockName,
	   double quantity,
	   final double pricePerShare,
	   final Filter filter
	   )
	   throws OrderException, AllocationException {
		if ( null == party ) {
			throw new IllegalArgumentException( "party == null" );
		}
		
		if ( ! ( party instanceof StockHolder ) ) {
			throw new IllegalArgumentException( "Only StockHolder instances can trade with stocks, current class does not implement it: " +
					party.getClass() );
		}
		
		if ( null == stockName ) {
			throw new IllegalArgumentException( "stockName == null" );
		}
		
		if ( Double.compare( quantity, 0 ) == 0 ) {
			throw new IllegalArgumentException( "quantity == 0" );
		}
		
		if ( pricePerShare < 0 ) {
			throw new IllegalArgumentException( pricePerShare + " == pricePerShare < 0" );
		}
		
		if ( quantity > 0 ) {	// sell order: allocate shares
			final double ownedShares = ((StockSeller) party).getNumberOfSharesOwnedIn( stockName );
			// TODO Double comparison here
			if ( ownedShares < quantity ) {
				throw new OrderException( String.format( Locale.US,
						"Insufficient shares. Tried to sell %f out of owned %f shares of %s.", quantity, ownedShares,
						stockName ) );
			}
			((StockSeller) party).allocateShares(stockName, quantity);
		} else {	// buy order: allocate cash
			if (pricePerShare == 0){	
				// for market order get estimated price from instrument
			    final double bidAmount =
			       getStockInstrument(stockName).getBidPriceAtVolume(-quantity),
			       allocatedCash = ((StockBuyer) party).allocateCash(bidAmount);
			    if(allocatedCash == 0.)
			       throw new AllocationException();
			    quantity *= Math.min(allocatedCash / bidAmount, 1.0);
			} else {
			    final double
			       anticipatedCost = pricePerShare*-quantity,
			       allocatedCash = ((StockBuyer) party).allocateCash(anticipatedCost);
			    if(allocatedCash == 0.)
			       throw new AllocationException();
			    quantity *= Math.min(allocatedCash / anticipatedCost, 1.0);
			}
		}
		
		StockInstrument instrument = getStockInstrument( stockName );
		if ( null == instrument ) {
			//instrument = new StockInstrument( stockName, updatedOrders, listeners );
			//instruments.put( instrument.getTickerSymbol(), instrument );
			instrument = addNewInstrument(StockInstrument.generateTickerSymbol(stockName)); // Christoph changed this
		}
		
		return new StockOrder( party, instrument, quantity, pricePerShare, filter );
	}
	
	public StockOrder addBuyOrder(final Party party, final String stockName, final double quantity, final double pricePerShare)
			throws OrderException, AllocationException {
		return addBuyOrder( party, stockName, quantity, pricePerShare, DefaultFilters.any() );
	}
	
	public StockOrder addBuyOrder(final Party party, final String stockName, final double quantity, final double pricePerShare,
								final Filter filter) throws OrderException, AllocationException {
		if (quantity < 0){
			throw new IllegalArgumentException("size should be positive (" + quantity + ")");
		}
		return addOrder(party, stockName, -quantity, pricePerShare, filter);
	}

	public StockOrder addSellOrder(final Party party, final String stockName, final double quantity, final double pricePerShare)
			throws OrderException, AllocationException {
		return addSellOrder( party, stockName, quantity, pricePerShare, DefaultFilters.any() );
	}
	
	public StockOrder addSellOrder(final Party party, final String stockName, final double quantity, final double pricePerShare,
								final Filter filter) throws OrderException, AllocationException {
		if (quantity < 0){
			throw new IllegalArgumentException("size should be positive (" + quantity + ")");
		}
		return addOrder(party, stockName, quantity, pricePerShare, filter);
	}

	
	/**
	 * Gets the stock instrument.
	 * 
	 * @param instrumentName the instrument name; cannot be <code>null</code>
	 * @return the stock instrument; might be <code>null</code> if the given there is no <code>StockInstrument</code>
	 *         associated with the specified instrument name
	 */
	public StockInstrument getStockInstrument(final String instrumentName) {
		if ( null == instrumentName ) {
			throw new IllegalArgumentException( "instrumentName == null" );
		}
		
		return (StockInstrument) getInstrument( StockInstrument.generateTickerSymbol( instrumentName ) );
	}
	
	/**
	 * Gets all instrument names that are present on the market.
	 * 
	 * @return the instrument names in an unmodifiable list
	 */
	public List<String> getInstrumentNames() {
		final ArrayList<String> ret = new ArrayList<String>( instruments.size() );
		
		for (final Instrument instrument : instruments.values()) {			
			ret.add( instrument.getInstrumentName() );
		}
		
		return Collections.unmodifiableList( ret );
	}
	
	// ======================================================================================================================
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.crisis_economics.abm.markets.nonclearing.Market#addInstrument(java.lang.String)
	 */
	@Override
	public void addInstrument(final String tickerSymbol) {
		if ( null == tickerSymbol ) {
			throw new IllegalArgumentException( "tickerSymbol == null" );
		}
		
		if ( null == getInstrument( tickerSymbol ) ) {
			final String instrumentName = StockInstrument.parseNameFromTicker( tickerSymbol );
			instruments.put(tickerSymbol, new StockInstrument( instrumentName, updatedOrders,listeners ) );
			
			if ( instrumentMatchingMode == InstrumentMatchingMode.DEFAULT ) {
				instruments.put(tickerSymbol, new StockInstrument( instrumentName, updatedOrders,listeners ) );
			} else if ( instrumentMatchingMode == InstrumentMatchingMode.ASYNCHRONOUS ) {
				instruments.put(tickerSymbol, new StockInstrument( instrumentName, updatedOrders, Instrument.MatchingMode.ASYNCHRONOUS ,listeners) );
			} else if ( instrumentMatchingMode == InstrumentMatchingMode.SYNCHRONOUS ) {
				instruments.put(tickerSymbol, new StockInstrument( instrumentName, updatedOrders, Instrument.MatchingMode.SYNCHRONOUS , listeners) );
			}
		}
	}
	
	/*
	 * Adds and creates new instruments that previously did not exist, Christoph wrote this function
	 */
	public StockInstrument addNewInstrument(final String tickerSymbol) {
		
		final String instrumentName = StockInstrument.parseNameFromTicker( tickerSymbol );
		StockInstrument instrument;
		
		if ( instrumentMatchingMode == InstrumentMatchingMode.DEFAULT ) {
			instrument = new StockInstrument( instrumentName, updatedOrders,listeners );
			instruments.put(tickerSymbol, instrument );
		}
		else if ( instrumentMatchingMode == InstrumentMatchingMode.ASYNCHRONOUS ) {
			instrument = new StockInstrument( instrumentName, updatedOrders, Instrument.MatchingMode.ASYNCHRONOUS ,listeners);
			instruments.put(tickerSymbol, instrument );
		}
			
		else if ( instrumentMatchingMode == InstrumentMatchingMode.SYNCHRONOUS ) {
			instrument = new StockInstrument( instrumentName, updatedOrders, Instrument.MatchingMode.SYNCHRONOUS , listeners);
			instruments.put(tickerSymbol, instrument );
		}
			
		else {
			instrument = new StockInstrument( instrumentName, updatedOrders,listeners );
			instruments.put(tickerSymbol, instrument );
		}
		return instrument;
		
	}
}
