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
package eu.crisis_economics.abm.bank.strategies.clearing;

import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.bank.strategies.BankStrategy;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;

/**
  * A simple implementation of the {@link ClearingBankStrategy} and {@link BankStrategy}
  * interfaces. This implementation describes what is perhaps the simplest possible 
  * set of decision rules for a {@link ClearingBank} {@link Agent}. {@link ClearingBank}
  * {@link Agent}{@code s} using this strategy will pay no dividends and will aim for
  * a target portfolio of size zero. This strategy specifies a target investment of
  * size zero in all stock instruments and all loan markets. The target leverage of the
  * user is set to zero (as the desired risky portfolio size is zero).
  * 
  * @author phillips
  */
public final class TrivialClearingStrategy
   extends EmptyBankStrategy implements ClearingBankStrategy {
   
   public TrivialClearingStrategy(final StrategyBank bank) {
      super(bank);
   }
   
   @Override
   public void decidePorfolioSize() {
      // No action.
   }
   
   @Override
   public void decidePortfolioDistribution() {
      // No action.
   }
   
   @Override
   public void decideDividendPayment() {
      // No action.
   }
   
   @Override
   public double getDesiredStockInvestmentIn(final ClearingStockMarket market) {
      return 0;
   }
   
   @Override
   public double getDesiredLoanInvestmentIn(final ClearingLoanMarket market) {
      return 0.;
   }
   
   @Override
   public void setStocksToInvestIn(final CollectionProvider<ClearingStockMarket> markets) {
      // No action
   }
   
   @Override
   public void setLoansToInvestIn(final CollectionProvider<ClearingLoanMarket> markets) {
      // No action
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "Trivial Clearing Strategy, bank: " + getBank().getUniqueName() + ".";
   }
}
