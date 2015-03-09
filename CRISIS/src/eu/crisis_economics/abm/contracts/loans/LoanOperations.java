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
package eu.crisis_economics.abm.contracts.loans;

public final class LoanOperations {
   /**
     * Create a stateless object that can used to check whether
     * an existing loan contract is, or is not, a cash injection.
     */
   public static LoanOperation<Boolean> createIsCashInjectionQuery() {
      return new LoanOperation<Boolean>() {
         @Override
         public Boolean operateOn(final Object object) { return false; }
         
         @Override
         public Boolean operateOn(final RepoLoan repoLoan) { return repoLoan.cashInjection; }
         
         @Override
         public Boolean operateOn(final CashInjectionLoan cashInjectionLoan) { return true; }
         
         @Override
         public Boolean operateOn(final BulletLoan bulletLoan) { return false; }
         
         @Override
         public Boolean operateOn(final FixedRateMortgage mortgage) { return false; }
         
         @Override
         public Boolean operateOn(final Bond bond) { return false; }
      };
   }
   
   /**
     * Standalone method to check whether a loan contract is, or is 
     * not, a cash injection.
     */
   public static boolean isCashInjectionLoan(final Loan loan) {
      return loan.acceptOperation(createIsCashInjectionQuery());
   }
   
   private LoanOperations() { }   // Uninstantiable
}
