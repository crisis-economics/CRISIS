/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Ermanno Catullo
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
package eu.crisis_economics.abm.markets.clearing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingInstrument;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.MapUtil;

/**
  * A structure to:<br>
  * {@code (a)}
  *    aggregate clearing market participants, and<br>
  * {@code (b)}
  *   process multiple {@link ClearingMarket}{@code s} at time 
  *   {@link CLEARING_MARKET_MATCHING} in the simulation
  *   cycle.<br><br>
  * 
  * {@link ClearingMarket}{@code s} are attached to this {@link ClearingHouse}
  * by calling 
  * {@link ClearingMarket}{@code s} are processed in the same order
  * they are registered 
  * 
  * @author phillips
  * @author aymanns
  *
  */
// TODO: this needs work.
public final class ClearingHouse {
   
    private Map<String, ClearingMarket>
       markets;
    
    private Map<String, StockHolder>
       stockHolders;
    private Map<String, Borrower>
       borrowers;
    private Map<String, Lender>
       lenders;
    private Map<String, LoanStrategyFirm>
       firms;
    private Map<String, StrategyBank>
       banks;
    private Map<String, Fund>
       funds;
    
    private Map<String, ClearingMarketParticipant>
       allParticipants;
    
    /**
      * Create a {@link ClearingHouse} with no participants and no registered
      * {@link ClearingInstrument}{@code s}. <br><br>
      * 
      * All {@link ClearingMarket}s added to this {@link ClearingHouse}
      * will be executed at time {@link NamedEventOrderings.CLEARING_MARKET_MATCHING}
      * in the simulation cycle. {@link ClearingMarket}{@code s} will be processed
      * in the order they were added.
      */
    @Inject
    public ClearingHouse(){
       this.markets = new LinkedHashMap<String, ClearingMarket>();
       this.stockHolders = new LinkedHashMap<String, StockHolder>();
       this.borrowers = new LinkedHashMap<String, Borrower>();
       this.lenders = new LinkedHashMap<String, Lender>();
       this.firms = new LinkedHashMap<String, LoanStrategyFirm>();
       this.banks = new LinkedHashMap<String, StrategyBank>();
       this.funds = new LinkedHashMap<String, Fund>();
       this.allParticipants = new LinkedHashMap<String, ClearingMarketParticipant>();
       
       scheduleSelf();
    }
    
    private void scheduleSelf() {
       Simulation.repeat(this, "processAllInstruments",
          NamedEventOrderings.CLEARING_MARKET_MATCHING);
    }
    
    /**
      * Add a {@link ClearingMarket} to this {@link ClearingHouse}. The argument
      * must not be {@code null}.
      */
    public void addMarket(final ClearingMarket market) {
       Preconditions.checkNotNull(market);
       markets.put(market.getMarketName(), market);
    }
    
    /**
      * Process all {@link ClearingMarket}{@code s} in the order they
      * were installed.
      */
    @SuppressWarnings("unused") // Scheduled
    private void processAllInstruments() {
       for(final Entry<String, ClearingMarket> record : markets.entrySet())
          record.getValue().process();
    }
    
    /**
      * Checks whether this {@link ClearingHouse} contains a {@link ClearingMarket}
      * with the specified name.
      * 
      * @return <code>true</code> if a maket was found with the specified name, 
      *         <code>false</code> otherwise.
      */
    public boolean hasMarket(final String marketName) {
       if(marketName == null) return false;
       return markets.containsKey(marketName);
    }
    
    /**
      * Returns a {@link ClearingMarket} with the specified name. If no such
      * {@link ClearingMarket} has been registered with this {@link ClearingHouse},
      * this method returns {@code null}.
      * 
      * @param name
      *        The {@link String} name of the market to return.
      */
    public ClearingMarket getMarket(final String name) {
       if(name == null) return null;
       return markets.get(name);
    }
    
    /**
      * Get a {@link List} of {@link ClearingMarket}{@code s} of the specified type.
      * This method can return an empty list but cannot return {@code null}.
      * 
      * @param type
      *        The {@link ClearingMarket} type to search for.
      */
    @SuppressWarnings("unchecked")
    public <T extends ClearingMarket> List<T> getMarketsOfType(final Class<T> type) {
       final List<T>
          result = new ArrayList<T>();
       for(final ClearingMarket market : markets.values())
          if(type.isInstance(market))
             result.add((T) market);
       return result;
    }
    
    /**
      * Add a {@link StockHolder}s to this {@link ClearingHouse}. If the 
      * {@link StockHolder} has already been added, no action is taken.
      */
    public <T extends StockHolder & ClearingMarketParticipant>
       void addStockMarketParticipant(final T stockHolder) {
       if(stockHolders.containsKey(stockHolder.getUniqueName())) return;
       stockHolders.put(stockHolder.getUniqueName(), stockHolder);
       allParticipants.put(stockHolder.getUniqueName(), stockHolder);
    }
    
    /**
      * Get a list of {@link StockHolder}{@code s} known to this {@link ClearingHouse}.
      * Adding or removing elements from the return value will not affect
      * this {@link ClearingHouse}.
      */
    public Map<String, ClearingMarketParticipant> getStockMarketParticipants() {
       return MapUtil.castMap(stockHolders);
    }
    
    /**
      * Remove a {@link StockHolder}s from this {@link ClearingHouse}. If the 
      * {@link StockHolder} is not known to this {@link ClearingHouse}, no 
      * action is taken.
      */
   public <T extends StockHolder & ClearingMarketParticipant>
      boolean removeStockMarketParticipant(final T stockHolder) {
      return (stockHolders.remove(stockHolder.getUniqueName()) != null);
   }

   /**
     * Get a list of {@link Borrower}{@code s} known to this {@link ClearingHouse}.
     * Adding or removing elements from the return value will not affect
     * this {@link ClearingHouse}.
     */
   public Map<String, ClearingMarketParticipant> getBorrowers() {
      return MapUtil.castMap(borrowers);
   }
   
   /**
     * Add a {@link Borrower}s to this {@link ClearingHouse}. If the 
     * {@link Borrower} has already been added, no action is taken.
     */
   public <T extends Borrower & ClearingMarketParticipant>
      void addBorrower(final T borrower) {
      if(borrowers.containsKey(borrower.getUniqueName())) return;
      borrowers.put(borrower.getUniqueName(), borrower);
      allParticipants.put(borrower.getUniqueName(), borrower);
   }
   
   /**
     * Remove a {@link Borrower}s from this {@link ClearingHouse}. If the 
     * {@link Borrower} is not known to this {@link ClearingHouse}, no 
     * action is taken.
     */
   public <T extends Borrower & ClearingMarketParticipant>
      boolean removeBorrower(final T borrower) {
      return (borrowers.remove(borrower.getUniqueName()) != null);
   }
   
   /**
     * Get a list of {@link Lender}{@code s} known to this {@link ClearingHouse}.
     * Adding or removing elements from the return value will not affect
     * this {@link ClearingHouse}.
     */
   public Map<String, ClearingMarketParticipant> getLenders() {
      return MapUtil.castMap(lenders);
   }
   
   /**
     * Add a {@link Lender}{@code s} to this {@link ClearingHouse}. If the 
     * {@link Lender} has already been added, no action is taken.
     */
   public <T extends Lender & ClearingMarketParticipant> 
      void addLender(final T lender) {
      if(lenders.containsKey(lender.getUniqueName())) return;
      lenders.put(lender.getUniqueName(), lender);
      allParticipants.put(lender.getUniqueName(), lender);
   }
   
   /**
     * Remove a {@link Lender}{@code s} from this {@link ClearingHouse}.
     * If the {@link Lender} is not known to this {@link ClearingHouse}, no 
     * action is taken.
     */
   public <T extends Lender & ClearingMarketParticipant> 
      boolean removeLender(final T lender) {
      return (lenders.remove(lender.getUniqueName()) != null);
   }
   
   /**
     * Add a {@link ClearingBank}{@code s} to this {@link ClearingHouse}. If the 
     * {@link ClearingBank} has already been added, no action is taken.
     */
   public void addBank(final ClearingBank bank) {
      if(banks.containsKey(bank.getUniqueName())) return;
      banks.put(bank.getUniqueName(), bank);
      allParticipants.put(bank.getUniqueName(), bank);
   }
  
   /**
     * Remove a {@link ClearingBank} from this {@link ClearingHouse}. If the 
     * {@link StrategyBank} is not known to this {@link ClearingHouse}, no 
     * action is taken.
     */
   public boolean removeBank(final ClearingBank bank) {
     return (banks.remove(bank.getUniqueName()) != null);
   }
   
   /**
     * Get a list of {@link ClearingBank}{@code s} known to this {@link ClearingHouse}.
     * Adding or removing elements from the return value will not affect
     * this {@link ClearingHouse}.
     */
   public Map<String, ClearingMarketParticipant> getBanks() {
      return MapUtil.castMap(banks);
   }
  
   /**
     * Add a {@link LoanStrategyFirm}s to this {@link ClearingHouse}. If the 
     * {@link LoanStrategyFirm} has already been added, no action is taken.
     */
   public <T extends LoanStrategyFirm & ClearingMarketParticipant>
      void addFirm(final T firm) {
      if(firms.containsKey(firm.getUniqueName())) return;
      firms.put(firm.getUniqueName(), firm);
      addBorrower(firm);
      allParticipants.put(firm.getUniqueName(), firm);
   }
   
   /**
     * Remove a {@link LoanStrategyFirm} from this {@link ClearingHouse}. If
     * the {@link LoanStrategyFirm} is not known to this {@link ClearingHouse}, no 
     * action is taken.
     */
   public <T extends LoanStrategyFirm & ClearingMarketParticipant>
      boolean removeFirm(final T firm) {
      return (firms.remove(firm.getUniqueName()) != null);
   }
   
   /**
     * Get a list of {@link LoanStrategyFirm}{@code s} known to this {@link ClearingHouse}.
     * Adding or removing elements from the return value will not affect
     * this {@link ClearingHouse}.
     */
   public Map<String, ClearingMarketParticipant> getFirms() {
      return MapUtil.castMap(firms);
   }
   
   /**
     * Add a {@link Fund}{@code s} to this {@link ClearingHouse}. If the 
     * {@link Fund} has already been added, no action is taken.
     */
   public void addFund(final Fund fund) {
      if(funds.containsKey(fund.getUniqueName())) return;
      funds.put(fund.getUniqueName(), fund);
      allParticipants.put(fund.getUniqueName(), fund);
   }
   
   /**
     * Remove a {@link Fund} from this {@link ClearingHouse}. If the 
     * {@link Fund} is not known to this {@link ClearingHouse}, no 
     * action is taken.
     */
   public boolean removeFund(final Fund fund) {
     return (funds.remove(fund.getUniqueName()) != null);
   }
    
   /**
     * Get a list of {@link Fund}{@code s} known to this {@link ClearingHouse}.
     * Adding or removing elements from the return value will not affect
     * this {@link ClearingHouse}.
     */
   public Map<String, ClearingMarketParticipant> getFunds() {
      return MapUtil.castMap(funds);
   }
   
   public Map<String, ClearingMarketParticipant> getAllParticipants() {
      return new HashMap<String, ClearingMarketParticipant>(allParticipants);
   }
    
   /**
     * Get a list of all participants currently registered with this
     * {@link ClearingHouse}. Adding or removing elements from the return 
     * value will not affect this {@link ClearingHouse}.
     */
   public Map<String, ClearingMarketParticipant> getParticipants() {
      return new HashMap<String, ClearingMarketParticipant>(allParticipants);
   }
   
   /**
     * Check whether this {@link ClearingHouse} contains the specified 
     * {@link ClearingMarketParticipant}.
     */
   public boolean hasParticipant(
      final ClearingMarketParticipant participant) {
      return allParticipants.containsKey(participant.getUniqueName());
   }
   
   /**
     * Entirely remove a {@link ClearingMarketParticipant} from this 
     * {@link ClearingHouse}. The participant will not be included in 
     * any {@link ClearingMarket}{@code s} processed by this {@link ClearingHouse}
     * until it is readded.
     * 
     * @param SettlementParty settlementParty
     */
   public boolean removeParticipant(final ClearingMarketParticipant participant) {
      final boolean result =
         (allParticipants.remove(participant.getUniqueName()) != null);
      stockHolders.remove(participant.getUniqueName());
      borrowers.remove(participant.getUniqueName());
      lenders.remove(participant.getUniqueName());
      firms.remove(participant.getUniqueName());
      banks.remove(participant.getUniqueName());
      funds.remove(participant.getUniqueName());
      return result;
   }
}
