/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Fabio Caccioli
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
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
package eu.crisis_economics.abm.bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.StockBuyer;
import eu.crisis_economics.abm.markets.StockSeller;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author Tamás Máhr
 * @author olaf
 */
public class StockTradingBank extends StockReleasingBank
   implements StockHolder, StockSeller, StockBuyer {
   /**
     * StockTradingBank.stocksOwned is a map of the stocks owned by the bank.
     * The keys in this map are UUIDs of stock-relasing Firms.
     */
   private Map<String, StockAccount>
      stocksOwned = new HashMap<String, StockAccount>();
   
   public StockTradingBank(final double initialCash) {
      super(initialCash);
   }
   
   @Override
   public Map<String, StockAccount> getStockAccounts() {
      return Collections.unmodifiableMap(stocksOwned);
   }
   
   @Override
   public double getNumberOfSharesOwnedIn(final String stockName) {
      if (stocksOwned.containsKey(stockName))
         return stocksOwned.get(stockName).getQuantity();
      else
         return 0;
   }
	
	public List<Double> getStockPortfolio(){
        final ArrayList<Double> numStocks = new ArrayList<Double>();
		for(final String name : stocksOwned.keySet()){
			numStocks.add(getNumberOfSharesOwnedIn(name));
		}
		return numStocks;
	}
	
	/** {@inheritDoc} 
	 */
	@Override
	public boolean hasShareIn(final String stockName) {
		return stocksOwned.get(stockName) != null;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public StockAccount getStockAccount(final String stockName) {
		return stocksOwned.get(stockName);
	}
	
	/** {@inheritDoc} 
	 */
	@Override
	public void addAsset(final Contract asset) {
		super.addAsset(asset);
		
		if (asset instanceof StockAccount){
			stocksOwned.put(((StockAccount)asset).getInstrumentName(), (StockAccount) asset);
		}
	}
	
	@Override
	public boolean removeAsset(final Contract asset){
		final boolean returnValue = super.removeAsset(asset);
		
		if (asset instanceof StockAccount) {
			stocksOwned.remove(((StockAccount) asset).getInstrumentName());
		}
		return returnValue;
	}

	@Override
	public Set<StockMarket> getStockMarkets() {
		return Collections.unmodifiableSet(getMarketsOfType(StockMarket.class));
	}

	/**
	 * Returns the number of allocated shares in active stock sell orders for a given ticker. It should return the same as ({@link computeActiveStockSellVolume}) but it is more efficient.
	 * @see eu.crisis_economics.abm.contracts.stocks.StockAccount#allocatedShares
	 * @param tickerSymbol
	 * @return allocated shares due to stock sell orders or 0 if share is not owned
	 */
	public double getAllocatedShares(String tickerSymbol) {
		try {
			return this.getStockAccount(tickerSymbol).allocatedShares;
		} catch (NullPointerException e) {
			return 0;
		}
	}
	
	@Override
	public void allocateShares(String tickerSymbol, double volume)
		throws AllocationException {
		if ( this.getStockAccount(tickerSymbol).getQuantity() - this.getStockAccount(tickerSymbol).allocatedShares < volume) {
			throw new AllocationException("unallocated shares ( "+(this.getStockAccount(tickerSymbol).getQuantity() - this.getStockAccount(tickerSymbol).allocatedShares)+" ) < volume ( "+volume+" )"+" at step "+ Simulation.getSimState().schedule.getSteps());
		} else this.getStockAccount(tickerSymbol).allocatedShares += volume;		
	}

	@Override
	public void disallocateShares(String tickerSymbol, double volume) {
		StockAccount stockAccount = this.getStockAccount(tickerSymbol);
		if (stockAccount != null) { // if null then probably all soled
			if ( stockAccount.allocatedShares < volume) {
				throw new IllegalArgumentException("allocated shares ( "+stockAccount.allocatedShares+" ) < volume ( "+volume+" )");
			} else this.getStockAccount(tickerSymbol).allocatedShares -= volume;		

		}
	}

   @Override
   public void registerIncomingDividendPayment(
      final double dividendPayment,
      final double pricePerShare,
      final double numberOfSharesHeld
      ) {
      // No action.
   }
}
