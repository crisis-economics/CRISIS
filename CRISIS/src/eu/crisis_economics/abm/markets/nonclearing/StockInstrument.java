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

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockTrade;
import eu.crisis_economics.abm.markets.StockBuyer;
import eu.crisis_economics.abm.markets.StockSeller;

/**
 * Instrument implementation for stocks.
 * 
 * <p>
 * Stock instruments are identified by their <code>intstrumentName</code>. The instrument name can be anything, but it must
 * uniquely identify the stock resources.
 * </p>
 * 
 * <p>
 * The class offers static utility functions that can help users to convert <i>instrument names</i> to <i>ticker symbols</i>
 * and vice versa. The ticker symbols are unique identifiers used by the {@link Market}, see its documentation for the
 * details.
 * </p>
 * 
 * <p>
 * Note that the trading is a bit different from other instruments. The contract created during the
 * {@link #setupContract(Order, Order, double, double)} method creates a contract where the stock releaser pays dividend in
 * each time step for all of its owners, including the new buyer. However, during the setup, there is a stock seller and a
 * stock buyer. The trading is instantaneous, therefore there is no additional contract created for the transaction.
 * </p>
 * 
 * @author rlegendi
 * @since 1.0
 */
public class StockInstrument
		extends Instrument {

   private static final long serialVersionUID = -2400483101906213052L;

   // TODO Why we need this? Shouldn't we externalize to a common static utility function?
	/**
	 * Generate the ticker symbol used by the market for the specified instrument name.
	 * 
	 * @param instrumentName the instrument name; cannot be <code>null</code>
	 * @return the ticker symbol used by the market
	 */
	public static String generateTickerSymbol(final String instrumentName) {
		if ( null == instrumentName ) {
			throw new IllegalArgumentException( "instrumentName == null" );
		}
		
		return "Stock_" + instrumentName;
	}
	
	/**
	 * Parses the instrument name from the specified ticker symbol.
	 * 
	 * @param tickerSymbol the ticker symbol to parse; must be a well-formed ticker symbol and cannot be <code>null</code>
	 * @return the instrument name parsed from the specified ticker symbol
	 */
	public static String parseNameFromTicker(final String tickerSymbol) {
		if ( null == tickerSymbol ) {
			throw new IllegalArgumentException( "tickerSymbol == null" );
		}
		
		if ( ! tickerSymbol.startsWith( "Stock_" ) ) {
			throw new IllegalArgumentException( "Malformed ticker symbol: " + tickerSymbol );
		}
		
		return tickerSymbol.substring( "Stock_".length() );
	}
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	/** Unique instrument name (usually the name of the firm). */
	private final String instrumentName;
	
	/**
	 * Instantiates a new stock instrument.
	 * 
	 * @param instrumentName the instrument name; cannot be <code>null</code>
	 * @param updatedOrders the updated orders
	 */
	// TODO BlockingQueue?
	public StockInstrument(
	    final String instrumentName,
	    final BlockingQueue<Order> updatedOrders,
	    final Set<InstrumentListener> listeners
	    ) {
		super( generateTickerSymbol(instrumentName), updatedOrders , listeners);
		
		if (null == instrumentName)
			throw new IllegalArgumentException("tickerSymbol == null");
		this.instrumentName = instrumentName;
	}
	

	public StockInstrument(
	   final String instrumentName,
	   final BlockingQueue<Order> updatedOrders,
	   final MatchingMode matchingMode,
	   final Set<InstrumentListener> listeners
	   ) {
		super( generateTickerSymbol( instrumentName ), updatedOrders, matchingMode , listeners);
		if ( null == instrumentName )
			throw new IllegalArgumentException( "tickerSymbol == null" );
		this.instrumentName = instrumentName;
	}

	/**
	 * Gets the instrument name.
	 * 
	 * @return the instrument name; cannot be <code>null</code>
	 */
	@Override
	public String getInstrumentName() {
		return instrumentName;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.crisis_economics.abm.markets.nonclearing.Instrument#setupContract(eu.crisis_economics.abm.markets.nonclearing.Order,
	 * eu.crisis_economics.abm.markets.nonclearing.Order, double, double)
	 */
	// TODO The buyer and the seller are both StockHolders here! Banks trade stocks, the firms (StockReleasers) do not play a role here.
	// TODO Shouldn't we declare the InsufficientFundsException in the method to be thrown?
	@Override
	protected void setupContract(
	   final Order buyOrder,
	   final Order sellOrder,
	   final double quantity,
	   final double pricePerShare
	   ) {
		if ( null == buyOrder ) {
			throw new IllegalArgumentException( "buyOrder == null" );
		}
		
		if ( null == sellOrder ) {
			throw new IllegalArgumentException( "sellOrder == null" );
		}
		
		if ( quantity < 0 ) {
			throw new IllegalArgumentException( quantity + " == quantity < 0" );
		}
		
		if ( pricePerShare < 0 ) {
			throw new IllegalArgumentException( pricePerShare + " == pricePerShare < 0" );
		}
		
		try {
			final StockBuyer buyer = (StockBuyer) buyOrder.getParty();
			final StockSeller seller = (StockSeller) sellOrder.getParty();
			final StockAccount sellingStockAccount = seller.getStockAccount(instrumentName);
			
			buyer.disallocateCash(pricePerShare * quantity);
			seller.disallocateShares(instrumentName, quantity);
			
			sellingStockAccount.sellSharesTo(quantity, buyer);
			
			notifyListenersOfNewTrade(new StockTrade(instrumentName, pricePerShare, quantity, buyer, seller));
			
		} catch (final InsufficientFundsException e) {
			throw new RuntimeException( e );
		}
	}
}
