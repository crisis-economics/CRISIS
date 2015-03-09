/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;

/**
 * a share holder that acts as a sink for dividends
 * @author bochmann
 *
 */
public class DummyStockHolder extends Agent implements StockHolder {

	protected Map<String, StockAccount> stocks = new HashMap<String, StockAccount>();

	@Override
	public double credit(double amt) throws InsufficientFundsException {
		throw new InsufficientFundsException();
	}

	@Override
	public void debit(double amt) {
		// thanks
	}

	@Override
	public void cashFlowInjection(double amt) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAsset(Contract asset) {
		super.addAsset(asset);
		if (asset instanceof StockAccount){
			stocks.put(((StockAccount)asset).getInstrumentName(), (StockAccount) asset);
		}
	}

	@Override
	public boolean removeAsset(Contract asset) {
		final boolean returnValue = super.removeAsset(asset);
		if (asset instanceof StockAccount) {
			stocks.remove(((StockAccount) asset).getInstrumentName());
		}
		return returnValue;
	}

	@Override
	public double getNumberOfSharesOwnedIn(String stockName) {
		if (stocks.containsKey(stockName))
		   return stocks.get(stockName).getQuantity();
		else 
		   return 0;
	}

	@Override
	public boolean hasShareIn(String stockName) {
		return stocks.get(stockName) != null;
	}

	@Override
	public StockAccount getStockAccount(String stockName) {
		return stocks.get(stockName);
	}

	@Override
	public Set<StockMarket> getStockMarkets() {
		return Collections.unmodifiableSet(getMarketsOfType(StockMarket.class));
	}

   @Override
   public Map<String, StockAccount> getStockAccounts() {
      return null;
   }

   @Override
   public void registerIncomingDividendPayment(
      final double dividendPayment,
      final double pricePerShare,
      final double numberOfSharesHeld
      ) {
      // No action.
   }
   
   @Override
   public <T> T accept(final AgentOperation<T> operation) {
      return operation.operateOn(this);
   }
}

