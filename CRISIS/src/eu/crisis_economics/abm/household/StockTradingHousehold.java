/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.household;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.StockBuyer;
import eu.crisis_economics.abm.markets.StockSeller;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * implements stock trading feature in households (based on StockTradingBank)
 * @author olaf
 */
public class StockTradingHousehold extends DepositingHousehold
    implements StockHolder, StockSeller, StockBuyer {

    /**
     * stocks is a map of the stock owned by the bank, key is the UUID of the StockReleasingFirms
     */
    protected Map<String, StockAccount> stocks = new HashMap<String, StockAccount>();
    private double
       dividendReceivedThisCycle,
       dividendReceivedLastCycle;
    
    public StockTradingHousehold(final DepositHolder depositHolder, final double initialCash) {
        super(depositHolder, initialCash);
        
        Simulation.repeat(
           this, "resetStockTradingHouseholdMemories", NamedEventOrderings.BEFORE_ALL);
    }
    
    @SuppressWarnings("unused") // Scheduled
    private void resetStockTradingHouseholdMemories() {
       this.dividendReceivedLastCycle = dividendReceivedThisCycle;
       this.dividendReceivedThisCycle = 0.;
    }
    
    /** {@inheritDoc} 
     */
    @Override
    public double getNumberOfSharesOwnedIn(final String stockName) {

        if (stocks.containsKey(stockName)){
            return stocks.get(stockName).getQuantity();
        }

        return 0;
    }

    /** {@inheritDoc} 
     */
    @Override
    public boolean hasShareIn(final String stockName) {
        return stocks.get(stockName) != null;
    }

    /** {@inheritDoc} 
     */
    @Override
    public StockAccount getStockAccount(final String stockName) {
        return stocks.get(stockName);
    }
    
    /** {@inheritDoc} 
     */
    @Override
    public void addAsset(final Contract asset) {
        super.addAsset(asset);
        
        if (asset instanceof StockAccount){
            stocks.put(((StockAccount)asset).getInstrumentName(), (StockAccount) asset);
        }
    }
    
    @Override
    public boolean removeAsset(final Contract asset){
        
        final boolean returnValue = super.removeAsset(asset);
        
        if (asset instanceof StockAccount) {
            stocks.remove(((StockAccount) asset).getInstrumentName());
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
    
    /**
     * I just like to do some consistency check here.
     * @see eu.crisis_economics.abm.Agent#afterAll()
     */
    @Override
    public void afterAll() {
        super.afterAll();
    }

   @Override
   public Map<String, StockAccount> getStockAccounts() {
      return Collections.unmodifiableMap(stocks);
   }
   
   @Override
   public void registerIncomingDividendPayment(
      final double dividendPayment,
      final double pricePerShare,
      final double numberOfSharesHeld
      ) {
      dividendReceivedThisCycle += dividendPayment;
   }
   
   protected final double getDividendReceivedThisCycle() {
      return dividendReceivedThisCycle;
   }
   
   protected final double getDividendReceivedLastCycle() {
      return dividendReceivedLastCycle;
   }
   
   @Override
   public double allocateCash(double positiveAmount) {
      return cashLedger.changeAllocatedBy(+positiveAmount);
   }
   
   @Override
   public double disallocateCash(double positiveAmount) {
      return -cashLedger.changeAllocatedBy(-positiveAmount);
   }
   
   @Override
   public double getAllocatedCash() {
      return cashLedger.getAllocated();
   }
   
   @Override
   public double getUnallocatedCash() {
      return cashLedger.getUnallocated();
   }
   
   @Override
   public double calculateAllocatedCashFromActiveOrders() {
      throw new UnsupportedOperationException(); // TODO
   }
}
