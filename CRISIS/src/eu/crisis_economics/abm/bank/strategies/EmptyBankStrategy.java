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
package eu.crisis_economics.abm.bank.strategies;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.simulation.injection.factories.BankStrategyFactory;

public class EmptyBankStrategy implements BankStrategy {
   
   private StrategyBank
      bank;
   
   public EmptyBankStrategy(final StrategyBank bank) {
      Preconditions.checkNotNull(bank);
      this.bank = bank;
   }
   
   /**
    * Subclasses should implement the bank's deposit-market strategy by overriding this function. 
    * This method is called once in each turn to allow the bank to make its move. The default 
    * implementation does nothing.
    */
   @Override
   public void considerDepositMarkets() { }
   
   /**
    * Subclasses should implement the bank's commercial loan strategy here. This method is called 
    * once in each turn to allow the bank to make its move. The default implementation does
    * nothing.
    */
   @Override
   public void considerCommercialLoanMarkets() { }
   
   /**
    * Subclasses should implement the bank's stock-market strategy here. This method is called 
    * once in each turn to allow the bank to make its move. The default implementation does
    * nothing.
    */
   @Override
   public void considerStockMarkets() { }
   
   /**
    * Subclasses should implement the bank's interbank-loan-market strategy here. This method is
    * called once in each turn to allow the bank to make its move. The default implementation 
    * does nothing.
    */
   @Override
   public void considerInterbankMarkets() { }
   
   @Override
   public void afterInterbankMarkets() { }
   
   /**
    * Gets the current deposit rate.
    */
   public double getDepositRate() { return 0.; }
   
   /**
    * Gets the current lending rate.
    */
   public double getLendingRate() { return 0.; }
   
   /**
    * Gets the current interbank lending rate.
    */
   public double getInterLendingRate() { return 0.; }
   
   /**
    * Register (respond to as appropriate a new bank order).
    */
   public void registerNewOrder(Order newOrder) { }
   
   /**
    * Gets the current loan size. 
    */
   public double getLoanSize() { return 0.; }
   
   /**
    * Return the order size placed on the commercial loan market.
    */
   public double getCommercialLoanOrderSize() { return 0.; }
   
   /**
     * Get the {@link Bank} associated with this {@link BankStrategy}.
     */
   @Override
   public StrategyBank getBank() { return bank; }
   
   /**
     * Create a {@link BankStrategyFactory} yielding an {@link EmptyBankStrategy} object.
     */
   public static BankStrategyFactory factory() {
      return new BankStrategyFactory() {
         @Override
         public BankStrategy create(final StrategyBank bank) {
            return new EmptyBankStrategy(bank);
         }
      };
   }
}
