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

import java.util.List;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.utilities.Pair;

public interface StockExchange {
   /**
     * Add a new stock to the exchange.
     */
   public abstract void addStock(StockReleaser stockReleaser, double initialStockPrice);
   
   /**
     * Remove a stock to the exchange.
     */
  public abstract boolean removeStock(StockReleaser stockReleaser);
  
   /**
     * Does this exchange process shares in the given stock releaser?
     */
   public abstract boolean hasStock(StockReleaser stockReleaser);
   
   /**
     * Count the total number of shares owned by sharesholders, if the
     * stock of the specified type is known to this exhchange. If the
     * stock releaser is not know to the exchange, this method returns 
     * {@code 0.0}.
     */
   public abstract double getNumberOfEmittedSharesIn(String stockUniqueName);
   
   public abstract double getNumberOfEmittedSharesIn(StockReleaser stockReleaser);
   
   /**
     * Return (disown) shares and return these shares to the exchange. Any
     * action subsequently taken by the stock exchange is documented by the
     * implementing class.<br><br>
     * 
     * As a result of calling this methid, the {@link StockHolder} will immediately
     * lose the stated number of shares. They will not be compensated for their loss. 
     * If the number of shares to disown is greater than of equal to the number of 
     * shares currently held by the {@link StockHolder}, then the {@link StockHolder} 
     * {@link StockAccount} will be terminated as a result of calling this method.
     * 
     * @param holder
     *        The {@link StockHolder} who wishes to disown shares.
     * @param stockName
     *        The unique name of the stock type for which shares are to be disowned.
     * @param numberOfShares
     *        The number of shares to disown. This argument should be non-negative
     *        and less than the number of shares currently owned by the {@link StockHolder}.
     *        Otherwise, the value of this argument is silently clamped to the
     *        nearest value in the interval {@code [0, account.getQuantity()]}.
     */
   public abstract void disownShares(
      StockHolder holder, String stockName, double numberOfShares);
   
   /**
     * Does this exchange process stocks with the given name?
     */
   public abstract boolean hasStock(String stockUniqueName);
   
   /**
     * Get the stock releaser for the given stock type. The return
     * value of this method is null if and only if the exchange does
     * not process stocks with the given name.
     */
   public abstract StockReleaser getStockReleaser(String uniqueStockName);
   
   public abstract List<StockReleaser> getStockReleasers();
   
   /**
     * Get the current (unique) market stock price for the given stock
     * releaser. If it is not the case that this exchange processes
     * stocks in the given releaser, IllegalArgumentException is raised.
     */
   public abstract double getStockPrice(StockReleaser stockReleaser);
   
   public abstract double getStockPrice(String stockName);
   
   /**
     * Set the price per share for the specified stock type. The
     * price per share cannot be negative or zero, otherwise
     * IllegalArgumentException is raised and the existing stock
     * price is unchanged.
     */
   public abstract void setStockPrice(String stockName, double positivePricePerShare);
   
   /**
     * Get the minimum price per share for stocks of the specified type.
     */
   public abstract double getMinimumStockPrice(String stockUniqueName);
   
   /**
     * Erase a replace an entire stock. This method terminates all existing
     * stock accounts in the stock type issued by the releaser. Stock holders
     * whose accounts are terminated are not compensated for the loss of their
     * share assets. 
     * 
     * Arguments to this method cannot be null, and it should be the case that
     * List<Pair<StockHolder, Double>> newShareOwnerships is both (a) non-empty,
     * and (b) composed of non-null, non-negative values; otherwise 
     *  IllegalArgumentException is raised and no stock accounts or share 
     * prices are modified. double newPricePerShare cannot be negative or
     * zero.
     */
   public abstract void eraseAndReplaceStockWithoutCompensation(
      String uniqueStockName,
      List<Pair<StockHolder, Double>> stockOwnerships,
      double newPricePerShare
      );
   
   /**
     * Generate shares for a stock holder at a custom price. The
     * price priceToPayPerShare need not be the market price of 
     * existing shares. This method generates new shares without
     * affecting the value of existing stock accounts held by other
     * shareholders.
     */
   public abstract void generateSharesFor(
      String uniqueStockName,
      StockHolder stockHolder, double numberOfShares,
      double priceToPayPerShare
      ) throws InsufficientFundsException;
   
   public abstract void generateFreeSharesFor(
      String uniqueStockName,
      StockHolder stockHolder,
      double numberOfShares
      );
   
   /**
    * Get the share ownership tracker for stocks with the unique name of the argument.
    */
   public abstract StockOwnershipTracker getOwnershipTracker(String uniqueStockName);
}