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

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.bank.Bank;

/**
  * A simple escalatory chain of responsibility class object for
  * {@link Bank} bankruptcy resolution. In this implementation, the
  * bankruptcy resolution policy will attempt to execute a 
  * {@link BankBankruptyHandler} to resolve the {@link Bank}
  * bankruptcy. Should the {@link BankBankruptyHandler} fail to 
  * resolve the bankruptcy, this policy will execute another handler
  * as specified by the policy chain.
  * 
  * @author phillips
  */
public final class BankBankruptcyPolicy {
   
   private final BankBankruptyHandler
      handler;
   private final BankBankruptcyPolicy
      next;
   
   /**
     * Create a {@link BankBankruptcyPolicy} object.
     * 
     * @param handler
     *        The bankruptcy resolution algorithm to be applied by this
     *        policy.
     * @param next
     *        The next {@link BankBankruptcyPolicy} policy to use, should
     *        the above handler fail. It is acceptable for this argument
     *        to be {@code null}.
     */
   public BankBankruptcyPolicy(
      final BankBankruptyHandler handler,
      final BankBankruptcyPolicy next
      ) {
      Preconditions.checkNotNull(handler);
      this.handler = handler;
      this.next = next;
   }
   
   /**
     * This constructor is equivalent to {@code BankBankruptcyPolicy(handler, null)}.
     */
   public BankBankruptcyPolicy(final BankBankruptyHandler handler) {
      this(handler, null);
   }
   
   /**
     * Apply a {@link BankBankruptyHandler} to the argument. If the handler
     * fails to resolve the bankruptcy, this {@link BankBankruptcyPolicy} 
     * will delegate the bankruptcy resolution to the next handler in the 
     * chain. If there is no next handler in the resolution chain, 
     * {@link IllegalStateException} is raised. This behaviour is subject 
     * to change.
     * 
     * @param bank
     *        The {@link Agent} to which to apply the bankruptcy resolution
     *        process.
     */
   public void applyTo(Bank bank) {
      final boolean
         result = handler.operateOn(bank);
      if(result) return;
      else {
         if(next == null)
            throw new IllegalStateException(
               "Bank Bankruptcy Policy: no bankrupty resolution handler succeeded to resolve "
             + "the bank bankruptcy. This behaviour is not expected in the current implementation."
               );
         next.applyTo(bank);
      }
   }
}
