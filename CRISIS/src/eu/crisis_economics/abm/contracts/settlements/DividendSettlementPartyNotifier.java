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
package eu.crisis_economics.abm.contracts.settlements;

import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * Dividend transaction notifications for stock releasers and stock holders.
  * @author phillips
  */
final class DividendSettlementPartyNotifier implements SettlementPartyNotifier {
   
   private final StockReleaser stockReleaser;
   private final StockHolder stockHolder;
   
   DividendSettlementPartyNotifier(
      final StockReleaser stockReleaser,
      final StockHolder stockHolder
      ) {
      StateVerifier.checkNotNull(stockReleaser, stockHolder);
      this.stockReleaser = stockReleaser;
      this.stockHolder = stockHolder;
   }
   
   @Override
   public void postCredit(final double amountCredited) {
      stockReleaser.registerOutgoingDividendPayment(
         amountCredited,
         UniqueStockExchange.Instance.getStockPrice(stockReleaser),
         stockHolder.getNumberOfSharesOwnedIn(stockReleaser.getUniqueName())
         );
   }
   
   @Override
   public void postDebit(final double amountDebited) {
      stockHolder.registerIncomingDividendPayment(
         amountDebited,
         UniqueStockExchange.Instance.getStockPrice(stockReleaser),
         stockHolder.getNumberOfSharesOwnedIn(stockReleaser.getUniqueName())
         );
      SettlementFriendClasses.Instance.getStockExchange().notifyDividendPayment(
         stockReleaser, amountDebited);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "DividendSettlementPartyNotifier, Stock Releaser:" 
           + stockReleaser.getUniqueName() + ", stock holder:" 
           + stockHolder.getUniqueName() + ".";
   }
}
