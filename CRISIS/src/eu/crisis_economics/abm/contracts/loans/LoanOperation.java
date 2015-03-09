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

/**
  * Interface for custom visitor operations on loan contracts.
  * @author phillips
  */
interface LoanOperation<T> {
   public abstract T operateOn(Bond bond);
   
   public abstract T operateOn(FixedRateMortgage mortgage);
   
   public abstract T operateOn(BulletLoan bulletLoan);
   
   public abstract T operateOn(CashInjectionLoan cashInjectionLoan);
   
   public abstract T operateOn(RepoLoan repoLoan);
   
   public abstract T operateOn(final Object object);
}