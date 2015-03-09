/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 AITIA International, Inc.
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

import java.util.Map;
import java.util.Set;

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;

/**
  * An interface for types that can buy stocks.
  * 
  * <p>
  * A stock holder is a party that buys and sells shares in a 
  * {@link StockReleaser}. The stock holder then receives dividends
  * on each share owned.
  * </p>
  * 
  * <p>
  * Stocks are specified by the stock names (e.g., the unique 
  * name of the <code>StockReleaser</code>).
  * </p>
  * 
  * @author rlegendi
  */
public interface StockHolder
   extends SettlementParty, HasAssets, UniquelyIdentifiable {
   
   /**
     * Gets the number of shares owned for the specified type of stock.
     * 
     * @param stockName The stock name; the unique name of the stock releaser.
     * @return The number of shares owned for the specified stock type.
     */
   public double getNumberOfSharesOwnedIn(String stockName);
   
   /**
     * Does this holder have any shares in the specified stock type?
     * 
     * @param stockName The stock name; the unique name of the stock releaser.
     * @return <code>true</code> if any shares owned; <code>false</code> 
     *        otherwise
     */
   public boolean hasShareIn(String stockName);
   
   /**
     * Gets the stock account for the specified stocks.
     * 
     * @param stockName The stock name; the unique name of the stock releaser.
     * @return The shareholder stock account for the specified stocks. 
     *         If the shareholder does not own any stocks of type stockName,
     *         this method may return null.
     */
   public StockAccount getStockAccount(String stockName);
   
   /**
     * @return Get an unmodifiable map of shareholder stock accounts, 
     *         keyed by the unique name of the stock releaser for each
     *         stock type.
     */
   public Map<String, StockAccount> getStockAccounts();
   
   /**
    * returns the stock market
    */
   public Set<StockMarket> getStockMarkets();
   
   public void registerIncomingDividendPayment(
      double dividendPayment, 
      double pricePerShare,
      double numberOfSharesHeld
      );
}
