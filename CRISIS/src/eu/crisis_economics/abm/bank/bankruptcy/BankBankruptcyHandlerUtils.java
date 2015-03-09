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
package eu.crisis_economics.abm.bank.bankruptcy;

/**
  * Lightweight static utilities for {@link Bank} bankruptcy handlers.
  * 
  * @author phillips
  */
public final class BankBankruptcyHandlerUtils {
   /**
     * String together an array of {@link BankBankruptyHandler} objects
     * as a bankruptcy resolution policy. The argument should consist of 
     * an ordered sequence of {@link BankBankruptyHandler} handlers with
     * nonzero length.
     */
   public static BankBankruptcyPolicy orderedHandlersAsPolicy(
      final BankBankruptyHandler... handlers) {
      if(handlers.length == 0)
         throw new IllegalArgumentException(
            "Bank Bankruptcy Handler Utils: bankruptcy handler string has zero length.");
      BankBankruptcyPolicy policy = null;
      for(int i = handlers.length - 1; i>= 0; --i) {
         final BankBankruptyHandler
            handler = handlers[i];
         policy = new BankBankruptcyPolicy(handler, policy);
      }
      return policy;
   }
}
