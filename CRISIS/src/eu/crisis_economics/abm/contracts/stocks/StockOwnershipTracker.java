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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.NullArgumentException;

/**
  * @author phillips
  */
public final class StockOwnershipTracker implements Iterable<StockHolder> {
   
   private final String stockUniqueName;
   private Map<String, StockHolder> currentStockHolders;
   private StockReleaser stockReleaser;
   
   /**
     * Create a dedicated stock ownership tracker. At construction,
     * this class will verify that every StockHolder object indicated
     * in initialStockHolders does own shares in the stock with the 
     * unique name String stockUniqueName.
     * 
     * Upon construction, this class will count the total number of 
     * shares in existence (N). At all times thereafter, N will be 
     * regarded as a constant.
     */
   public StockOwnershipTracker(
      final String stockUniqueName,
      final double originalTotalNumberOfShares,
      final StockReleaser stockReleaser
      ) {
      if(stockUniqueName == null ||
         stockReleaser == null)
         throw new NullArgumentException();
      this.stockUniqueName = stockUniqueName;
      this.stockReleaser = stockReleaser;
      this.currentStockHolders = new HashMap<String, StockHolder>();
      if(originalTotalNumberOfShares <= 0.)
         throw new IllegalStateException(
            "StockOwnershipTracker: the total number of shares emitted (value " +
            originalTotalNumberOfShares + ") is non-positive.");
   }
   
   /**
     * Terminate all stock accounts with all shareholders.
     */
   public void terminateAllExistingShareAccountsWithoutCompensation() {
      Map<String, StockHolder> currentStockHoldersCopy =
         new HashMap<String, StockHolder>(currentStockHolders);
      for(Entry<String, StockHolder> record : currentStockHoldersCopy.entrySet()) {
         final StockAccount stockAccount = record.getValue().getStockAccount(stockUniqueName);
         stockAccount.terminateAccountWithoutCompensatingStockHolder();
      }
      currentStockHolders.clear();
   }
   
   void trackStockHolder(final StockHolder stockHolder) {
      currentStockHolders.put(stockHolder.getUniqueName(), stockHolder);
   }
   
   void discontinueStockHolderTracking(final StockHolder stockHolder) {
      currentStockHolders.remove(stockHolder.getUniqueName());
   }
   
   /**
     * Get the stock releaser.
     */
   public StockReleaser getStockReleaser() {
      return stockReleaser;
   }
   
   /**
     * Count the total number of shares currently held by shareholders.
     */
   private double countTotalSharesHeldByShareHolders() {
      double result = 0.;
      for(Entry<String, StockHolder> record : currentStockHolders.entrySet())
         result += record.getValue().getStockAccount(stockUniqueName).getQuantity();
      return result;
   }
   
   /**
     * Does the holder own shares in this stock type?
     */
   public boolean doesOwnShares(final StockHolder stockHolder) {
      return doesOwnShares(stockHolder.getUniqueName());
   }
   
   public boolean doesOwnShares(final String stockHolderName) {
      return currentStockHolders.containsKey(stockHolderName);
   }
   
   /**
     * What value of shares does the holder currently own in this stock type?
     */
   public double getValueOfSharesOwned(final StockHolder stockHolder) {
      return getValueOfSharesOwned(stockHolder.getUniqueName());
   }
   
   /**
     * What value of shares does the holder currently own in this stock type?
     */
   public double getValueOfSharesOwned(final String stockHolderName) {
      if(!doesOwnShares(stockHolderName))
         return 0.0;
      else return
         currentStockHolders.get(
            stockHolderName).getNumberOfSharesOwnedIn(stockReleaser.getUniqueName()) *
         UniqueStockExchange.Instance.getStockPrice(stockReleaser);
   }
   
   /**
     * Get the (fixed) total number of shares in existence.
     */
   public double getTotalNumberOfSharesEmitted() {
      return countTotalSharesHeldByShareHolders();
   }
   
   /**
     * Get the unique name of the stock resource.
     */
   public String getStockName() {
      return stockUniqueName;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      String result = "StockOwnershipTracker, stock unique name: " + stockUniqueName + ", " +
         "share distribution: ";
      for(Entry<String, StockHolder> record : currentStockHolders.entrySet())
         result += String.format("[%s %g] ", 
            record.getKey(), record.getValue().getStockAccount(stockUniqueName).getQuantity());
      return result;
   }
   
   /**
     * Get the number of {@link StockHolder}{@code s} known to hold shares in 
     * the stock instrument to which this tracker belongs.
     */
   public int getNumberOfStockHolders() {
      return currentStockHolders.size();
   }
   
   /**
     * Get an iterator to an unmodifiable list of stock holders.
     */
   @Override
   public Iterator<StockHolder> iterator() {
      List<StockHolder> copy = new ArrayList<StockHolder>();
      copy.addAll(currentStockHolders.values());
      return Collections.unmodifiableList(copy).iterator();
   }
}
