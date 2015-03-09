/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Milan Lovric
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
package eu.crisis_economics.abm.contracts.stocks;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;


/**
 * An interface for stock releasers (i.e., firms).
 * 
 * <p>
 * A stock releaser is a party that issues stocks. Then it pays dividends periodically based on its income to the stock
 * holders.
 * </p>
 * 
 * <p>
 * The stock releaser keeps track of all owners (more specifically: all stock accounts it is associated with). The API
 * notifies the releaser through specific functions about all new stock account creations and removals.
 * </p>
 * 
 * <p>
 * Releases stocks to one initial StockHolder. In general it cannot act on a stock market. 
 * It needs to keep track of all shareholder to pay dividends. 
 * </p>
 * 
 * @author rlegendi
 * @since 1.0
 */
public interface StockReleaser
		extends SettlementParty, UniquelyIdentifiable {
	
	/**
	 * Notifies the releaser about a new stock account creation.
	 * 
	 * <p>
	 * Every time a new stock account for stocks of this firm is created, it needs to be added here. This allows the <code>StockReleaser</code>
	 * to keep track of all shareholders and the number of shares they hold.
	 * </p>
	 * 
	 * @param stockAccount the stock account
	 */
	public void addStockAccount(StockAccount stockAccount);
	
	/**
	 * Notifies the releaser about a new stock account removal (i.e., it has been sold by the owner).
	 * 
	 * <p>
	 * Every time a stock account for stocks of this firm is removed, it needs to be removed here too. This allows the <code>StockReleaser</code>
	 * to keep track of all shareholders and the number of shares they hold.
	 * </p>
	 * 
	 * @param stockAccount the stock account
	 * @return <code>true</code>, if the given stock account was recognized and removed successfully; <code>false</code>
	 *         otherwise
	 */
	public boolean removeStockAccount(StockAccount stockAccount);
	
	/**
	 * The releaser computes the dividends to pay for the specified stock account.
	 * 
	 * <p>
	 * This is called from the associated stock account every time step. It should return the dividends to be payed per share and per time step.
	 * </p>
	 * 
	 * @param stockAccount the stock account for which to determine the dividends
	 * @return the evaluated dividend for the specified dividend
	 */
	// TODO The problem here is that dividends are not payed per time step (day). Even if we can change the interval, synchronization remains a problem.
	// The solution could be to schedule dividends by the firm. 
	public double computeDividendFor(StockAccount stockAccount);
	
	/**
	  * This method creates a number of new shares and attempts to sell these 
	  * new shares to the {@link StockHolder} at the specified price.
	  * @param numberOfShares
	  *        The number of new shares to create.
	  * @param pricePerShare
	  *        The price per share at which to bill the {@link StockHolder}.
	  * @param shareHolder
	  *        The {@link StockHolder} who will buy, or be given, the shares.
	  */
	public void releaseShares(
	   double numberOfShares,
	   double pricePerShare,
	   StockHolder shareHolder
	   ) throws InsufficientFundsException;
	
	/**
	 * set dividend to be payed per share at each time step. is set to >=0.
	 * @param dividendPerShare
	 */
	public void setDividendPerShare(double dividendPerShare);
	
	/**
	 * if the firm is traded on the stock market, it returns the last traded price, otherwise the emission price.
	 * 
	 * @return averaged price on all the markets
	 */
	public double getMarketValue();

	/**
	 * DT: Added in order to make ClearingBankStrategy work with any stock-releaser.
	 *     Needed for calculating divided per share.
	 * 
	 * @return The total number of shares that have been issued.
	 */
//	public double getNumberOfEmittedShares();
	
	/***
	 * DT: Added in order to make ClearingBankStrategy work with any stock-releaser.
	 *     Needed for calculating fundamentalist expected returns.
	 * 
	 * @return The current dividend per share
	 */
	public double getDividendPerShare();
	
	/***
	 * DT: Added in order to make ClearingBankStrategy work with any stock-releaser.
	 *     Needed for submitting orders to the clearing market.
	 *     (This may be better in the instrument, or, even better get rid
	 *     of ticker symbols all-together and replace with references to objects)
	 * 
	 * @return The current dividend per share
	 */
//	public String getStockTicker();
	
	
	public void registerOutgoingDividendPayment(
	    double dividendPayment, 
	    double pricePerShare,
	    double numberOfSharesHeldByInvestor
	    );
}
