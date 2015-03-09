/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 James Porter
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
 * Copyright (C) 2015 Ariel Y. Hoffman
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import sim.util.Bag;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.Takeover;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.model.Mark2Model;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.Simulation.ScheduledMethodInvokation;

/**
 * Simple Crisis agent with a balance sheet.
 * 
 * @author Tamás Máhr
 * @author bochmann
 *
 */
public class Agent implements Party, IAgent {

    private static long serial = 0;

    /**
     * A unique name for the agent. This generation of the name assumes single
     * threaded execution.
     */
    private String
       id = getClass().getSimpleName() + serial++;
    
    public Boolean liquidateAtAfterAll = false;  
    public static void resetSerial() {
        serial = 0;
    }
    
    private ScheduledMethodInvokation afterAllInvokation;
    
    public Agent() {
        afterAllInvokation = Simulation.once(this, "afterAll", NamedEventOrderings.AFTER_ALL);
    }
    
    /**
     * This is a recursive scheduling method to handle some clean up at the end
     * of each time step. Here we check for negative equity condition
     * (bankruptcy).
     */
    protected void afterAll() {
        if (getEquity() < 0 || this.liquidateAtAfterAll == true) {
        	this.liquidateAtAfterAll = false;
            handleBankruptcy();
        }
        Simulation.enqueue(afterAllInvokation);
    }

    /**
     * A unique ID of the agent
     * 
     * @return id
     */
    @Override
    @RecorderSource("UniqueName")
    public String getUniqueName() {
        return id;
    }
    
    protected void setUniqueName(final String name) {
       this.id = Preconditions.checkNotNull(name);
    }
    
    /**
     * The assets of the agent.
     */
    protected List<Contract> assets = new ArrayList<Contract>();

    /**
     * The liabilities of the agent.
     */
    protected List<Contract> liabilities = new ArrayList<Contract>();

    /**
     * A registry of markets this agent can submit orders to. The markets are
     * registered in a map where the key is the market class.
     */
    // This field is hidden by intention, do not change it
    // Use the addMarket() and getMarketsOfType() methods instead
    private final Map<Class<? extends Market>, Set<Market>> markets = new HashMap<Class<? extends Market>, Set<Market>>();
    
    /**
     * The existing orders of the agent.
     */
    protected List<Order> orders = new ArrayList<Order>();

    protected Takeover takeover;

    @Override
    public void addAsset(final Contract asset) {
        assets.add(asset);
    }

    @Override
    public boolean removeAsset(final Contract asset) {
        return assets.remove(asset);
    }

    @Override
    public List<Contract> getAssets() {
       return Collections.unmodifiableList(new ArrayList<Contract>(assets));
    }

    @Override
    public void addLiability(final Contract liability) {
        liabilities.add(liability);
    }

    @Override
    public boolean removeLiability(final Contract liability) {
        return liabilities.remove(liability);
    }

    @Override
    public List<Contract> getLiabilities() {
        return Collections.unmodifiableList(new ArrayList<Contract>(liabilities));
    }

    @Override
    public void addOrder(final Order order) {
        orders.add(order);
    }

    @Override
    public boolean removeOrder(final Order order) {
        return orders.remove(order);
    }

    @Override
    public void updateState(final Order order) {
        // Leave this empty for a default implementation
    }

    /**
     * Adds the given market to the list of markets.
     * 
     * @param market
     *            the new market to register
     * @return true if the market was not added to this agent before, false
     *         otherwise
     */
    @Override
   public boolean addMarket(final Market market) {
        HashSet<Market> marketList = (HashSet<Market>) markets.get(market.getClass());
        if (marketList == null) {
            marketList = new HashSet<Market>();
            markets.put(market.getClass(), marketList);
        }
        return marketList.add(market);
    }

    /**
     * Removes the given market from the list of markets.
     * 
     * @param market
     *            the market to remove
     * @return true if the given market was in the list, false otherwise
     */
    @Override
   public boolean removeMarket(final Market market) {
        final HashSet<Market> marketList = (HashSet<Market>) markets.get(market.getClass());
        if (marketList == null) {
            return false;
        }
        return marketList.remove(market);
    }

    @Override
   @SuppressWarnings("unchecked")
    public <T extends Market> Set<T> getMarketsOfType(final Class<T> clazz) {
        final HashSet<T> ret = new HashSet<T>();

        for (final Entry<Class<? extends Market>, Set<Market>> entry : markets.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                ret.addAll((Set<T>) entry.getValue());
            }
        }

        return Collections.unmodifiableSet(ret);
    }

    /**
     * returns a list of asset loan contracts
     * 
     * @return loan contracts
     */
    @Override
   public List<Loan> getAssetLoans() {
        final ArrayList<Loan> ret = new ArrayList<Loan>();

        for (final Contract act : getAssets()) {
            if (act instanceof Loan) {
                final Loan loan = (Loan) act;
                ret.add(loan);
            }
        }

        return Collections.unmodifiableList(ret);
    }

    /**
     * returns a list of liability loan contracts
     * 
     * @return loan contracts
     */
    @Override
   public List<Loan> getLiabilitiesLoans() {
        final ArrayList<Loan> ret = new ArrayList<Loan>();

        for (final Contract act : getLiabilities()) {
            if (act instanceof Loan) {
                final Loan loan = (Loan) act;
                ret.add(loan);
            }
        }

        return Collections.unmodifiableList(ret);
    }

    /**
     * returns a list of asset stock account contracts
     * 
     * @return StockAccount contracts
     */
    @Override
   public List<StockAccount> getAssetStockAccounts() {
        final ArrayList<StockAccount> ret = new ArrayList<StockAccount>();

        for (final Contract act : getAssets()) {
            if (act instanceof StockAccount) {
                final StockAccount stockAccount = (StockAccount) act;
                ret.add(stockAccount);
            }
        }

        return Collections.unmodifiableList(ret);
    }

    /**
     * returns a list of asset deposit contracts (holding cash of firms and
     * households)
     * 
     * @return deposit contracts
     */
    @Override
   public List<DepositAccount> getAssetDeposits() {
        final ArrayList<DepositAccount> ret = new ArrayList<DepositAccount>();

        for (final Contract act : getAssets()) {
            if (act instanceof DepositAccount) {
                final DepositAccount depositAccount = (DepositAccount) act;
                ret.add(depositAccount);
            }
        }

        return Collections.unmodifiableList(ret);
    }

    /**
     * returns a list of liability deposit contracts (customer deposits in the
     * banks)
     * 
     * @return deposit contracts
     */
    @Override
   public List<DepositAccount> getLiabilitiesDeposits() {
        final ArrayList<DepositAccount> ret = new ArrayList<DepositAccount>();

        for (final Contract act : getLiabilities()) {
            if (act instanceof DepositAccount) {
                final DepositAccount depositAccount = (DepositAccount) act;
                ret.add(depositAccount);
            }
        }

        return Collections.unmodifiableList(ret);
    }

    /**
     * equity is the difference of assets and liabilities
     * 
     * @return the equity
     */
    @Override
   @ RecorderSource("Equity")
    public double getEquity() {
        return (getTotalAssets() - getTotalLiabilities());
    }

    @RecorderSource("TotalAssets")
    public double getTotalAssets() {
        double totalAssets = 0;
        for (final Contract contract : assets) {
            totalAssets += contract.getValue();
        }
        return totalAssets;
    }

    public double getTotalLiabilities() {
        double totalLiabilities = 0;
        for (final Contract contract : liabilities) {
            totalLiabilities += contract.getValue();
        }
        return totalLiabilities;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.crisis_economics.abm.HasLiabilities#handleBankruptcy()
     */
    @Override
    public void handleBankruptcy() {
        // it is assumed a random bank takes over all assets
        try {
            Bag banks = new Bag(((Mark2Model) Simulation.getSimState()).getBanks());
            if (banks.contains(this)) {
                banks.remove(this);
            }
            this.takeover = new Takeover(this, (Bank) banks.get(Simulation.getSimState().random
                    .nextInt(banks.size())));
        } catch (ClassCastException e) {
            // if there is no Mark2Model create a new bank
            this.takeover = new Takeover(this, new Bank(1000));
            ;
        } catch (IndexOutOfBoundsException e) {
            // if there is no bank create a new bank
            this.takeover = new Takeover(this, new Bank(1000));
        }
    }

    @Override
    public boolean isBankrupt() {
        return (this.takeover != null) || liquidateAtAfterAll;
    }
    
    @Override
    public List<? extends Order> getOrders() {
        return Collections.unmodifiableList(new ArrayList<Order>(orders));
    }
    
    /**
      * Generic visitor pattern entry point.
      */
    @Override
    public <T> T accept(
       final AgentOperation<T> operation) {
       return operation.operateOn(this);
    }
}
