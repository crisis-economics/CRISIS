/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.IAgent;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;

/**
  * A simple forwarding implementation of the StockHolder interface. 
  * This implementation keeps track of existing stock accounts in a
  * hashmap structure.
  * 
  * @author DT
  */
public final class SimpleStockHolder implements StockHolder {
    
    public <T extends SettlementParty & HasAssets & IAgent> SimpleStockHolder(T parent) {
        this(parent, parent);
    }
    
    public SimpleStockHolder(SettlementParty settlement, IAgent agent) {
        iSettlementParty= settlement;
        iAgent          = agent;
        stockAccounts   = new HashMap<String, StockAccount>();
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // StockHolder interface
    ///////////////////////////////////////////////////////////////////////////////////////////
   
    @Override
    public double getNumberOfSharesOwnedIn(String stockName) {
        if(!stockAccounts.containsKey(stockName)) return 0;
        return stockAccounts.get(stockName).getQuantity();
    }
       
    @Override
    public boolean hasShareIn(String stockName) {
        return stockAccounts.containsKey(stockName);
    }
           
    @Override
    public StockAccount getStockAccount(String stockName) {
        return stockAccounts.get(stockName);
    }
    /**
      * Get an unmodifiable collection of stock accounts linked to the MutualFund.
      */
    @Override
    public final Map<String, StockAccount> getStockAccounts() {
        return Collections.unmodifiableMap(stockAccounts);
    }

    @Override
    public Set<StockMarket> getStockMarkets() {
        return Collections.unmodifiableSet(iAgent.getMarketsOfType(StockMarket.class));
    }
    
    @Override
    public void registerIncomingDividendPayment(
       double dividendPayment, double pricePerShare, double numberOfSharesHeld) {
       // No action.
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // HasAssets implementation
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void addAsset(Contract asset) {
//      iAgent.addAsset(asset);
        if (asset instanceof StockAccount){
            stockAccounts.put(((StockAccount)asset).getInstrumentName(), (StockAccount) asset);
        }
    }

    @Override
    public boolean removeAsset(Contract asset) {
        if (asset instanceof StockAccount) {
            stockAccounts.remove(((StockAccount) asset).getInstrumentName());
        }
//      return(iAgent.removeAsset(asset));
        return(true);
    }

    @Override
    public List<Contract> getAssets() {
//      return(iAgent.getAssets());
        return(null);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // Settlement party redirection
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public double credit(double amt) throws InsufficientFundsException {
        return(iSettlementParty.credit(amt));
    }
    
    @Override
    public void debit(double amt) {
        iSettlementParty.debit(amt);
    }
    
    @Override
    public void cashFlowInjection(double amt) {
        iSettlementParty.cashFlowInjection(amt);
    }
    
    @Override
    public String getUniqueName() {
        return(iAgent.getUniqueName());
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    protected SettlementParty   iSettlementParty;
    protected IAgent            iAgent;
    private final Map<String, StockAccount> stockAccounts;
}
