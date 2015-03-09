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
package eu.crisis_economics.abm.simulation;

/**
  * Financial system failure exception with compulsory error message or cause.
  * @author phillips
  */
public final class FinancialSystemHasCollapsedException extends RuntimeException {
   private static final long serialVersionUID = -6059800585822022663L;
   
   public FinancialSystemHasCollapsedException(final String s) {
      super("Financial System Has Collapsed Exception: " + s);
   }
   
   public FinancialSystemHasCollapsedException(final String s, final Throwable cause) {
      super("Financial System Has Collapsed Exception: " + s, cause);
   }
   
   public FinancialSystemHasCollapsedException(final Throwable cause) {
      super(cause);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Financial System Has Collapsed Exception. The financial system has "
           + "reached an inoperable state (for instance, but not necessarily " 
           + "limited to, the bankrupty and liquidation of all banks).";
   }
}
