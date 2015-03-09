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
package eu.crisis_economics.abm.events;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.contracts.stocks.StockHolder;

/**
  * An {@link Event} which signals that a {@link StockHolder} has sold or
  * bought shares via a stock market.
  * 
  * @author phillips
  */
public final class ShareTransactionEvent implements Event {
   
   final StockHolder
      stockHolder;
   final String
      stockName;
   final double
      cashFlow;
   
   /**
     * Create a {@link ShareTransactionEvent} object.
     * 
     * @param holder
     *        The {@link StockHolder} who bought or sold shares.
     * @param stockName
     *        The unique name of the stock involved in this transaction.
     * @param cashFlow
     *        The cash flow associated with the transaction. If this argument is
     *        positive, the {@link StockHolder} sold shares and received this amount
     *        of cash for the sale. If negative, the {@link StockHolder} bought shares
     *        and paid this amount of cash for the transaction.
     */
   public ShareTransactionEvent(   // Immutable
      final StockHolder holder,
      final String stockName,
      final double cashFlow
      ) {
      this.stockHolder = Preconditions.checkNotNull(holder);
      this.stockName = Preconditions.checkNotNull(stockName);
      this.cashFlow = cashFlow;
   }
   
   public StockHolder getStockHolder() {
      return stockHolder;
   }
   
   public String getStockName() {
      return stockName;
   }
   
   public double getCashFlow() {
      return cashFlow;
   }
}
