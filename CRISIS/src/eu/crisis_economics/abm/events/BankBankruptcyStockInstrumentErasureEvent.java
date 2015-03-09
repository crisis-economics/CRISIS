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
import com.google.common.eventbus.EventBus;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;

/**
  * An {@link EventBus} bank bankruptcy event. This event signals that a {@link Bank}
  * has been made subject to a bankruptcy resolution process which has erased and
  * reset its stock instrument.
  * 
  * @author phillips
  */
public final class BankBankruptcyStockInstrumentErasureEvent implements Event {
   private String
      stockReleaserName;
   
   /**
     * Create a {@link BankBankruptcyStockInstrumentErasureEvent} with custom parameters.
     * 
     * @param stockReleaserName
     *        The name of the {@link Bank} {@link StockReleaser} this event concerns.
     */
   public BankBankruptcyStockInstrumentErasureEvent(
      final String stockReleaserName) {
      this.stockReleaserName = Preconditions.checkNotNull(stockReleaserName);
   }
   
   /**
     * Get the name of the {@link Bank} {@link Agent} that was made subject to a 
     * bankruptcy resolution process which erased and replaced its stock instrument.
     */
   public String getBankName() {
      return stockReleaserName;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Bank Bankruptcy Stock Instrument Erasure Event, "
           + "Stock Releaser Name: " + stockReleaserName + ".";
   }
}
