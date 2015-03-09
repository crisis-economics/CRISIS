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
package eu.crisis_economics.abm;

public interface HasBalanceSheet extends HasAssets, HasLiabilities {
   /**
     * Get the balance sheet equity; the difference between assets and
     * liabilities.
     * @return The balance sheet equity.
     */
   public abstract double getEquity();
   
   boolean isBankrupt();
   
   /**
    * This method implements a liquidation where the agent is brought to an end. 
    * The assets and property of the agent is redistributed.
    */
   void handleBankruptcy();
}
