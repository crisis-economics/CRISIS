/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.abm.algorithms.statistics.DiscreteTimeSeries;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.abm.contracts.settlements.SettlementFriendClasses;
import eu.crisis_economics.abm.contracts.stocks.StockMarketDataLogger.StockRecord;
import eu.crisis_economics.abm.contracts.stocks.StockMarketDataLogger.TimestampedMarketRecord;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
public enum UniqueStockExchange implements StockExchange {
   Instance();
   
   private final double
      NUMBER_OF_SHARES_PER_STOCK = 1.0,
      ZERO_INVESTMENT_PRICE_PER_SHARE = 1.e-6;
   
   private Map<String, StockOwnershipTracker> stockOwnershipTrackers;
   private Map<String, Double> stockPrices;
   private StockMarketDataLogger stockMarketLogger;
   
   private UniqueStockExchange() { }
   
   public void initialize() {
      this.stockOwnershipTrackers = new HashMap<String, StockOwnershipTracker>();
      this.stockPrices = new HashMap<String, Double>();
      this.stockMarketLogger = new StockMarketDataLogger(100.);         // 100 cycles of memory.
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(
        Instance, "takeStockMarketSnapshot", NamedEventOrderings.POST_CLEARING_MARKET_MATCHING);
   }
   
   @SuppressWarnings("unused")
   /*
    * Record a snapshot of the market.
    */
   private void takeStockMarketSnapshot() {
      if(true) {
         System.out.println();
      }
      for(final StockOwnershipTracker record : stockOwnershipTrackers.values())
         stockMarketLogger.commitNewRecordFor(record.getStockReleaser());
   }
   
   /**
     * Get a timeseries of historical price per share data.
     */
   public DiscreteTimeSeries getHistoricalPricePerShareData(
      final String stockUniqueName,
      final double timeCutoff
      ) {
      return stockMarketLogger.getStockPricePerShareTimeSeries(stockUniqueName, timeCutoff);
   }
   
   /**
     * Get a timeseries of historical dividend per share data.
     */
   public DiscreteTimeSeries getHistoricalDividendPerShareData(
      final String stockUniqueName,
      final double timeCutoff
      ) {
      return stockMarketLogger.getDividendPerShareTimeSeries(stockUniqueName, timeCutoff);
   }
   
   @Override
   public void addStock(
      final StockReleaser stockReleaser,
      final double initialStockPrice
      ) {
      final String stockUniqueName = stockReleaser.getUniqueName();
      if(stockOwnershipTrackers.containsKey(stockUniqueName))
         throw new IllegalArgumentException(
            "StockExchange: a stock of unique name " + stockUniqueName + " already exists.");
      final StockOwnershipTracker newStockTracker =
         new StockOwnershipTracker(
            stockUniqueName, NUMBER_OF_SHARES_PER_STOCK, stockReleaser);
      this.stockOwnershipTrackers.put(
         newStockTracker.getStockName(), newStockTracker);
      this.stockPrices.put(newStockTracker.getStockName(), initialStockPrice);
   }
   
   @Override
   public boolean removeStock(final StockReleaser stockReleaser) {
      final String stockUniqueName = stockReleaser.getUniqueName();
      stockOwnershipTrackers.remove(stockUniqueName);
      return stockPrices.remove(stockUniqueName) != null;
   }
   
   /**
     * Does this exchange process shares in the given stock releaser?
     */
   @Override
   public boolean hasStock(final StockReleaser stockReleaser) {
      return hasStock(stockReleaser.getUniqueName());
   }
   
   /**
     * Count the total number of shares owned by sharesholders, if the
     * stock of the specified type is known to this exhchange.
     */
   @Override
   public double getNumberOfEmittedSharesIn(final String stockUniqueName) {
      assertHasStock(stockUniqueName);
      final StockOwnershipTracker tracker = stockOwnershipTrackers.get(stockUniqueName);
      return tracker.getTotalNumberOfSharesEmitted();
   }
   
   @Override
   public double getNumberOfEmittedSharesIn(final StockReleaser stockReleaser) {
      if(!hasStock(stockReleaser))
         return 0.;
      return getNumberOfEmittedSharesIn(stockReleaser.getUniqueName());
   }
   
   /**
     * An implementation of {@link #disownShares(StockHolder, String, double)}. This
     * implementation removes shares from the stated {@link StockHolder} and distributes
     * these shares equally among all <i>other</i> {@link StockHolder}{@code s} who currently
     * own shares of this type. If no other {@link StockHolder}{@code s} exist for
     * this purpose, then the shares are destroyed.
     */
   @Override
   public void disownShares(
      final StockHolder holder,
      final String stockName,
      double numberOfSharesToDisown
      ) {
      if(holder == null || stockName == null) return;
      numberOfSharesToDisown = Math.max(numberOfSharesToDisown, 0.);
      if(numberOfSharesToDisown == 0.) return;
      double
         numberOfSharesOwned = holder.getNumberOfSharesOwnedIn(stockName);
      if(numberOfSharesOwned <= 0.) return;
      numberOfSharesToDisown = Math.min(numberOfSharesToDisown, numberOfSharesOwned);
      if(numberOfSharesToDisown == numberOfSharesOwned) {
         holder.getStockAccount(stockName).terminateAccountWithoutCompensatingStockHolder();
         numberOfSharesOwned = 0.;
      }
      else {
         StockAccount account = holder.getStockAccount(stockName);
         account.incrementQuantity(-numberOfSharesToDisown);
         numberOfSharesOwned = account.getQuantity();
      }
      final StockOwnershipTracker
         participants = stockOwnershipTrackers.get(stockName);
      final int
         numberOfOtherParticipants = participants.getNumberOfStockHolders() -
         (numberOfSharesOwned > 0. ? 1 : 0);
      if(numberOfOtherParticipants == 0) return;
      final double
         sharesToEndowPerOtherStockHolder = numberOfSharesToDisown / numberOfOtherParticipants;
      for(StockHolder other : participants) {
         if(other == holder) continue;
         other.getStockAccount(stockName).incrementQuantity(sharesToEndowPerOtherStockHolder);
      }
      return;
   }
   
   /**
     * Does this exchange process stocks with the given name?
     */
   @Override
   public boolean hasStock(final String stockUniqueName) {
      return stockOwnershipTrackers.containsKey(stockUniqueName);
   }
   
   /**
     * Get the stock releaser for the given stock type. The return
     * value of this method is null if and only if the exchange does
     * not process stocks with the given name.
     */
   @Override
   public StockReleaser getStockReleaser(final String uniqueStockName) {
      if(!hasStock(uniqueStockName)) return null;
      return stockOwnershipTrackers.get(uniqueStockName).getStockReleaser();
   }
   
   public List<StockReleaser> getStockReleasers() {
      List<StockReleaser> result = new ArrayList<StockReleaser>();
      for(final String stockName : stockPrices.keySet())
         result.add(getStockReleaser(stockName));
      return result;
   }
   
   /**
     * Get the current (unique) market stock price for the given stock
     * releaser. If it is not the case that this exchange processes
     * stocks in the given releaser, IllegalArgumentException is raised.
     */
   @Override
   public double getStockPrice(final StockReleaser stockReleaser) {
      return getStockPrice(stockReleaser.getUniqueName());
   }
   
   @Override
   public double getStockPrice(final String stockName) {
      if(stockName == null)
         throw new NullArgumentException();
      assertHasStock(stockName);
      return stockPrices.get(stockName);
   }
   
   /**
     * Set the price per share for the specified stock type. The
     * price per share cannot be negative or zero, otherwise
     * IllegalArgumentException is raised and the existing stock
     * price is unchanged.
     */
   @Override
   public void setStockPrice(
      final String stockName, 
      double positivePricePerShare
      ) {
      if(!stockPrices.containsKey(stockName))
         throw new IllegalArgumentException(
            "StockExchange: no stock of type " + stockName + " is known to this exchange.");
      if(positivePricePerShare < 0.)
         throw new IllegalArgumentException(
            "StockExchange.setStockPrice: new price setting (value " + positivePricePerShare +
            ") is negative.");
      if(positivePricePerShare < ZERO_INVESTMENT_PRICE_PER_SHARE) {
         System.err.println(
            "StockExchange.setPricePerShare: price per share " + positivePricePerShare + 
            " is less that the minimum threshold " + ZERO_INVESTMENT_PRICE_PER_SHARE +
            ". New price per share has been revised to " + ZERO_INVESTMENT_PRICE_PER_SHARE + ".");
         positivePricePerShare = ZERO_INVESTMENT_PRICE_PER_SHARE;
      }
      stockPrices.put(stockName, positivePricePerShare);
   }
   
   /**
     * Get the minimum price per share for stocks of the specified type.
     */
   @Override
   public double getMinimumStockPrice(final String stockUniqueName) {
      return ZERO_INVESTMENT_PRICE_PER_SHARE;
   }
   
   /**
     * Erase a replace an entire stock. This method terminates all existing
     * stock accounts in the stock type issued by the releaser. Stock holders
     * whose accounts are terminated are not compensated for the loss of their
     * share assets. 
     * 
     * Arguments to this method cannot be null, and it should be the case that
     * List<Pair<StockHolder, Double>> newShareOwnerships is both (a) non-empty,
     * and (b) composed of non-null, non-negative values; otherwise 
     * IllegalArgumentException is raised and no stock accounts or share 
     * prices are modified. double newPricePerShare cannot be negative or
     * zero.
     */
   @Override
   public void eraseAndReplaceStockWithoutCompensation(
      final String uniqueStockName,
      final List<Pair<StockHolder, Double>> stockOwnerships,
      final double newPricePerShare
      ) {
      if(uniqueStockName == null || stockOwnerships == null)
         throw new NullArgumentException();
      assertHasStock(uniqueStockName);
      if(newPricePerShare <= 0. || stockOwnerships.isEmpty()) {
         /*
          * Price per share cannot be zero in this edition. In the current implementation,
          * share price can drop to zero only if aggregate stockholder investment is zero.
          * In this eventuality, existing stockholders are permitted to retain their
          * existing shares at a small nonzero price. The restorative price per share is 
          * the value ZERO_INVESTMENT_PRICE_PER_SHARE.
          */
         final String warningMsg =
            "StockExchange: price per share for stock type " + uniqueStockName + " has reached" +
            "the lower bound price " + ZERO_INVESTMENT_PRICE_PER_SHARE + ".";
         System.err.println(warningMsg);
         setStockPrice(uniqueStockName, ZERO_INVESTMENT_PRICE_PER_SHARE);
         return;
      }
      for(final Pair<StockHolder, Double> record : stockOwnerships) {
         if(record.getSecond() < 0.)
            throw new IllegalArgumentException(
               "StockExchange.eraseAndReplaceStockWithoutCompensation: stockholder (" +
               record.getFirst() + ") is null or has an invalid share endowment (value " +
               record.getSecond() + ").");
      }
      final StockOwnershipTracker tracker = 
         stockOwnershipTrackers.get(uniqueStockName);
      tracker.terminateAllExistingShareAccountsWithoutCompensation();
      for(Pair<StockHolder, Double> record : stockOwnerships)
         StockAccount.create(record.getFirst(), tracker.getStockReleaser(), record.getSecond());
      setStockPrice(uniqueStockName, newPricePerShare);
   }
   
   /**
     * Generate shares for a stock holder at a custom price. The
     * price priceToPayPerShare need not be the market price of 
     * existing shares. This method generates new shares without
     * affecting the value of existing stock accounts held by other
     * shareholders.
     */
   @Override
   public void generateSharesFor(
      final String uniqueStockName,
      final StockHolder stockHolder,
      final double numberOfShares,
      final double priceToPayPerShare
      ) throws InsufficientFundsException {
      if(numberOfShares < 0. || priceToPayPerShare < 0.)
         throw new IllegalArgumentException(
            "StockExchange.generateFreeShares: cannot generate " + numberOfShares + " at " +
            "price per share " + priceToPayPerShare + ".");
      assertHasStock(uniqueStockName);
      if(numberOfShares == 0) return;
      final StockOwnershipTracker tracker = stockOwnershipTrackers.get(uniqueStockName);
      final double transactionValue = (priceToPayPerShare * numberOfShares);
      final Settlement settlement = 
         SettlementFactory.createDirectSettlement(stockHolder, tracker.getStockReleaser());
      if(priceToPayPerShare > 0.)
         settlement.transfer(transactionValue);
      if(tracker.doesOwnShares(stockHolder))
         stockHolder.getStockAccount(uniqueStockName).incrementQuantity(numberOfShares);
      else
         StockAccount.create(stockHolder, tracker.getStockReleaser(), numberOfShares);
   }
   
   @Override
   public void generateFreeSharesFor(
      final String uniqueStockName,
      final StockHolder stockHolder,
      final double numberOfShares
      ) {
      try {
         generateSharesFor(uniqueStockName, stockHolder, numberOfShares, 0.);
      } catch(final InsufficientFundsException neverThrows) {
         System.err.println(neverThrows);
         Simulation.getSimState().finish();
      }
   }
   
   private void assertHasStock(final String uniqueStockName) {
      if(!hasStock(uniqueStockName))
         throw new IllegalArgumentException(
            "StockExchange: stocks of type " + uniqueStockName + " are " + 
            "unknown to this exchange.");
   }
   
   /**
     * Get the share ownership tracker for stocks with the unique name of the argument.
     */
   @Override
   public StockOwnershipTracker getOwnershipTracker(final String uniqueStockName) {
      return stockOwnershipTrackers.get(uniqueStockName); 
   }
   
   public void flush() {
      if(stockPrices != null)
         stockPrices.clear();
      if(stockOwnershipTrackers != null) {
         for(final StockOwnershipTracker tracker : stockOwnershipTrackers.values())
            tracker.terminateAllExistingShareAccountsWithoutCompensation();
         stockOwnershipTrackers.clear();
      }
   }
   
   // Privilaged access methods
   
   static {
      SettlementFriendClasses.Instance.grantAccess(new PrivilatedAccessor());
   }
   
   /*
    * Add the dividend transaction sum to the most recent stock
    * market data snapshot.
    */
   void notifyDividendPayment(final StockReleaser releaser, double dividendAmountPaid) {
      final TimestampedMarketRecord record =
         stockMarketLogger.getEarliestRecordBeforeTime(Simulation.getTime());
      if(record == null) return;
      final StockRecord snapshot = record.getRecord(releaser.getUniqueName());
      if(snapshot == null) return;
      snapshot.increaseDividendPaid(dividendAmountPaid);
   }
   
   public static final class PrivilatedAccessor {
      private PrivilatedAccessor() { }
      
      /**
        * Notify the stock exchange of a dividend payment made.
        */
      public void notifyDividendPayment(final StockReleaser releaser, double dividendAmountPaid) {
         UniqueStockExchange.Instance.notifyDividendPayment(releaser, dividendAmountPaid);
      }
   }
}
