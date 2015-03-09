/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 James Porter
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

import java.util.List;
import java.util.Set;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;

public interface IAgent extends HasBalanceSheet, UniquelyIdentifiable {

	public abstract void addOrder(Order order);
	
	public abstract double getTotalAssets();
	
	public abstract double getTotalLiabilities();

	public abstract boolean removeOrder(Order order);

	public abstract void updateState(Order order);

	/**
	 * Adds the given market to the list of markets.
	 * 
	 * @param market the new market to register
	 * @return true if the market was not added to this agent before, false otherwise
	 */
	public abstract boolean addMarket(Market market);

	/**
	 * Removes the given market from the list of markets.
	 * 
	 * @param market the market to remove
	 * @return true if the given market was in the list, false otherwise
	 */
	public abstract boolean removeMarket(Market market);

	public abstract <T extends Market> Set<T> getMarketsOfType(Class<T> clazz);

	/**
	 * returns a list of asset loan contracts
	 * @return loan contracts
	 */
	public abstract List<Loan> getAssetLoans();

	/**
	 * returns a list of liability loan contracts
	 * @return loan contracts
	 */
	public abstract List<Loan> getLiabilitiesLoans();

	/**
	 * returns a list of asset stock account contracts
	 * @return StockAccount contracts
	 */
	public abstract List<StockAccount> getAssetStockAccounts();

	/**
	 * returns a list of asset deposit contracts (holding cash of firms and households)
	 * @return deposit contracts
	 */
	public abstract List<DepositAccount> getAssetDeposits();

	/**
	 * returns a list of liability deposit contracts (customer deposits of banks)
	 * @return deposit contracts
	 */
	public abstract List<DepositAccount> getLiabilitiesDeposits();

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.HasLiabilities#handleBankruptcy()
	 */
	public abstract List<? extends Order> getOrders();
	
	/**
     * Is the balance 
     * @return
     */
    boolean isBankrupt();
   
    /**
      * This method implements a liquidation where the agent is brought to an end. 
      * The assets and property of the agent is redistributed.
      */
    void handleBankruptcy();

	/**
	  * Perform a custom operation on the agent.
	  */
	public abstract <T> T accept(final AgentOperation<T> operation);
}