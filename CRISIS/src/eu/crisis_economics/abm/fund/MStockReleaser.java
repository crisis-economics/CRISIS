/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Milan Lovric
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
package eu.crisis_economics.abm.fund;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import eu.crisis_economics.abm.IAgent;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.markets.nonclearing.StockInstrument;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.Simulation;

/****************************************************************************************
 * An agent-module that provides the functionality of a StockReleaser interface. It also
 * provides an IInvestmentAccount interface for use by agents that buy and sell shares
 * directly from the stock releaser (e.g. households from funds).
 * 
 * @author DT
 *
 */
class MStockReleaser implements StockReleaser {

	public <T extends SettlementParty & IAgent> MStockReleaser(T parent) {
		this(parent, parent);
	}
	
	public MStockReleaser(IAgent agent, SettlementParty settlement) {
		iSettlement = settlement;
		iAgent 		= agent;
		this.emissionPrice = 0;
		this.numberOfEmittedShares = 0;
		this.dividendPerShare = 0.0;
		this.marketValueRecordedAt= -2.0;
	}	

	
	public double getEmissionPrice() {
		return emissionPrice;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// StockReleaser interface implementation
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void addStockAccount(final StockAccount stockAccount) {
		stockAccounts.put(stockAccount.getHolder(), stockAccount);
	}

	@Override
	public boolean removeStockAccount(final StockAccount stockAccount) {
		return stockAccounts.remove(stockAccount.getHolder()) != null;
	}

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#grantStocks(int, eu.crisis_economics.abm.contracts.StockHolder)
	 */
//	@Override	
//	public void grantStocks(final double amount, final StockHolder stockHolder) {
//	}

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#computeDividendFor(eu.crisis_economics.abm.contracts.StockAccount)
	 */
	@Override
	public double computeDividendFor(final StockAccount stockAccount) {
//		System.out.println("Number of shares: "+stockAccount.getQuantity());
		return stockAccount.getQuantity()*this.dividendPerShare;
	}

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#releaseShares(int, double, eu.crisis_economics.abm.contracts.StockHolder)
	 */
	@Override
	public void releaseShares(final double numberOfShares, final double pricePerShare,
			final StockHolder shareHolder) {
		try {
			shareHolder.credit(numberOfShares*pricePerShare);
			this.debit(numberOfShares*pricePerShare);
			this.emissionPrice = pricePerShare;
			releaseShares(numberOfShares, shareHolder);
			// TODO this.dividendPerShare = 0.0;
	//		this.marketValueRecordedAt= -2.0;
		} catch (final InsufficientFundsException e) {
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#setDividendPerShare(double)
	 */
	@Override
	public void setDividendPerShare(final double dividendPerShare) {
		this.dividendPerShare = Math.max(dividendPerShare, 0.0);
	}

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#getMarketValue()
	 */
	@Override
	@RecorderSource("StockPrice")
	public double getMarketValue() {
		if (Simulation.getSimState() != null && marketValueRecordedAt < Simulation.getSimState().schedule.getTime()){
			marketValueRecordedAt = Simulation.getSimState().schedule.getTime();
			final Set<StockMarket> stockMarkets = new HashSet<StockMarket>(); 

			for (StockAccount stockAccount : stockAccounts.values()) {
				Set<StockMarket> markets = stockAccount.getHolder().getStockMarkets();
				if (markets != null){
					stockMarkets.addAll(markets);
				}     
			}

			if (stockMarkets.size() == 0){
				cachedMarketValue = emissionPrice;
				return emissionPrice;
			}

			double sum = 0.0;
			for (final StockMarket act : stockMarkets) {
				sum += getPriceOfInstrumentAtMarket( act );
			}

			//return sum / numberOfEmittedShares / stockMarkets.size();
			cachedMarketValue = sum / stockMarkets.size();
			return cachedMarketValue;
		} else {
			return cachedMarketValue;
		}
	}

//	@Override
//	public String getStockTicker() {
//		return("Stock_" + iAgent.getUniqueName());
//	}
	
	@Override
    public void registerOutgoingDividendPayment(
 	       double dividendPayment,
 	       double pricePerShare, 
 	       double numberOfSharesHeldByInvestor
 	       ) {
	}

//	@Override
	public double getNumberOfEmittedShares() {
		return numberOfEmittedShares;
	}

//	@Override
	@RecorderSource("DividendPerShare")
	public double getDividendPerShare() {
		return dividendPerShare;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// Settlement party redirection
	///////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public double credit (double amt) throws InsufficientFundsException {
		return(iSettlement.credit(amt));
	}
	
	@Override
	public void debit (double amt) {
		iSettlement.debit(amt);
	}
	
	@Override
	public void cashFlowInjection(double amt) {
		iSettlement.cashFlowInjection(amt);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper functions
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// a function that can release shares without the need to pay for them, christoph changed this to public... probably not optimal but needed it for Mark1Model
	public void releaseShares(final double numberOfShares, final StockHolder shareHolder) {
		StockAccount account = stockAccounts.get(shareHolder);
		
		if (account == null){
			// TODO: Implement this for Mixed Clearing Network Market 
			account = StockAccount.create(shareHolder, this, numberOfShares);
			account.setValue(emissionPrice * account.getQuantity());
		} else {
			account.setQuantity(account.getQuantity()+numberOfShares);
			account.setValue(emissionPrice * account.getQuantity());
		}
		numberOfEmittedShares += numberOfShares;
	}
	
	
	private double getPriceOfInstrumentAtMarket(final StockMarket stockMarket) {
		final StockInstrument stockInstrument = stockMarket.getStockInstrument(iAgent.getUniqueName() );
		if ( stockInstrument == null ) {
			return this.emissionPrice; //return emission price.
		} else if (stockInstrument.getLastPrice() == 0) {
			return this.emissionPrice; //return emission price.
		} else {
			return stockInstrument.getLastPrice(); //returns the last traded price			
		}
	}
	
	public void setTotalStockValue(double totalVal) {
		setEmissionPrice(totalVal/getNumberOfEmittedShares());
	}


	public void setEmissionPrice(double emissionPrice) {
		this.emissionPrice = emissionPrice;
	}
	
   @Override
   public String getUniqueName() {
      return (iAgent.getUniqueName());
   }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Variable declarations
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	protected static final double 					INITIAL_EMISSION_PRICE 	= 1000; // this has to be adjusted if you want to change the initial stock price, also needs to be changed in priceStrategy

	protected final Map<StockHolder, StockAccount> 	stockAccounts 			= new HashMap<StockHolder, StockAccount>();
	
	protected SettlementParty 	iSettlement;
	protected IAgent			iAgent;
	protected double 			dividendPerShare;
	protected double 			marketValueRecordedAt;		// Time that the chachedMarketValue was last cached
	protected double 			cachedMarketValue;			// cached price of shares averaged across all markets
	protected double 			numberOfEmittedShares;
	private   double 			emissionPrice;
	

	///////////////////////////////////////////////////////////////////////////////////////////////	
	// Unused ??
	///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	public Collection<StockAccount> getStockAccounts() {
		return stockAccounts.values();
	}


	public void setNumberOfEmittedShares(int numberOfEmittedShares) {
		this.numberOfEmittedShares = numberOfEmittedShares;
	}

  ****/
	/**
	 * returns the number of shares in all stock accounts. It should be equivalent to numberOfEmittedShares and serves as consistency check. Do not use it in production system because it is inefficient. Use numberOfEmittedShares.
	 * @see eu.crisis_economics.abm.StockReleasingFirm#numberOfEmittedShares
	 * @return number of shares in stock accounts
	 * @exception RuntimeException if numberOfEmittedShares != number of shares in stock accounts.
	 */
	/***
	public double computeNumberOfShares() {
		double nShares = 0.0;
		for (final StockAccount stockAccount : stockAccounts.values()) {
			nShares += stockAccount.getQuantity();
		}
		if (nShares != this.numberOfEmittedShares) {
			throw new RuntimeException("Stockflow inconsistency: number of emitted shares ( "+this.numberOfEmittedShares+" ), number of shares in stock accounts ( "+nShares+" )");
		}
		return nShares;
	}


	@RecorderSource("Dividend")
	public double getCurrentDividend(){
		return dividendPerShare * numberOfEmittedShares;
	}
	
    	    
    	***/    

	/**
	 * Iterates through {@link #stockAccounts}, calls the
	 * {@link #computeDividendFor(StockAccount)} method to calculate the amount
	 * to be paid to each {@link StockHolder}, and makes the appropriate payment. 
	 * 
	 * @return the sum of all payments made
	 * @throws InsufficientFundsException 
	 * @throws InverseTransferException 
	 */
/***
	protected double payDividends(final StockAccount stockAccount) throws InverseTransferException, InsufficientFundsException{
		double dividends = 0;
//		for (final StockAccount stockAccount : stockAccounts.values()) {
			final double dividend = computeDividendFor( stockAccount );
			stockAccount.makeDividentPayment(dividend);
			//System.out.println(getUniqueName() + " payed a dividend of " + dividend + " is payed to " + stockAccount.getHolder());
			dividends += dividend;
//		}
		
		return dividends;
	}
****/
	

}
