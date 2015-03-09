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

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;

/**
  * An {@link Event} which signals that a {@link Bank} {@link Agent} has
  * paid a dividend. This {@link Event} contains immutable information
  * about:
  * 
  * <ul>
  *   <li> The size of the dividend payment made;
  *   <li> The {@link Bank} which made the payment.
  * </ul>
  * 
  * @author phillips
  */
public final class BankDividendPaymentMadeEvent implements Event {
   final String
      bankName;
   final double
      dividendPaid;
   
   /**
     * Create an immutable {@link BankDividendPaymentMadeEvent} {@link Event}.
     * 
     * @param bankName
     *        The unique name of the {@link Bank} that has paid a dividend
     * @param dividendPaid
     *        The size of the dividend paid
     */
   public BankDividendPaymentMadeEvent(    // Immutable
      final String bankName,
      final double dividendPaid
      ) {
      this.bankName = Preconditions.checkNotNull(bankName);
      this.dividendPaid = dividendPaid;
   }
   
   public String getBankName() {
      return bankName;
   }
   
   public double getDividendPaid() {
      return dividendPaid;
   }
}
