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

import eu.crisis_economics.abm.bank.strategies.BankStrategy;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;

/**
  * Minimal interface for {@link Bank} participation in clearing markets.
  * The minimal set of decisions to be made are:<br><br>
  * 
  *   (a) the overall size of the investment portfolio;<br>
  *   (b) the distribution of the investment portfolio across investment types;<br>
  *   (c) the size of the {@link Bank} dividend to be paid.<br><br>
  * 
  * (a) may include a decision regarding leverage targets. (c) may include
  * a decision regarding equity targets.
  * 
  * @author phillips
  */
public interface ClearingBankStrategy extends BankStrategy {
   
   /**
     * Decide the (target) size of the investment portfolio.
     */
   public void decidePorfolioSize();
   
   /**
     * Compute desired stock and loan investments in preparation for
     * clearing markets.
     */
   public void decidePortfolioDistribution();
   
   /**
     * Decide the {@link Bank} dividend payment and set the 
     * {@link Bank} dividend per share. This method should be called 
     * at time {@link NamedEventOrderings.BANK_SHARE_PAYMENTS} in
     * the simulation cycle.
     * @param bank
     */
   public void decideDividendPayment();
   
   /**
     * Get the desired investment in the specified stock market. This method should be 
     * called after time {@link NamedEventOrderings.CLEARING_MARKET_MATCHING} in the 
     * simulation cycle.
     */
   public double getDesiredStockInvestmentIn(ClearingStockMarket market);
   
   /**
     * Get the desired total size of loan investments in the specified market. This 
     * method should be called after time {@link NamedEventOrderings.
     * CLEARING_MARKET_MATCHING} in the simulation cycle.
     * @return
     */
   public double getDesiredLoanInvestmentIn(ClearingLoanMarket market);
   
   /**
     * Specify which stock markets this strategy will consider when formulating
     * stock investments.<br><br>
     * 
     * TODO: improving this setup may involve general changes to portfolios.
     * 
     * @param stocks
     *        A collection provider for {@link ClearingStockMarket}{@code s}.
     *        Each {@link ClearingStockMarket} provided by the argument
     *        corresponds to a type of stock to consider when formulating
     *        investments.
     */
   public void setStocksToInvestIn(CollectionProvider<ClearingStockMarket> markets);
   
   /**
     * Specify which loan markets this strategy should consider when formulating 
     * loan investments.<br><br>
     * 
     * TODO: improving this setup may involve general changes to portfolios.
     * 
     * @param markets
     *        A collection provider for {@link ClearingMarket}{@code s}.
     *        Each {@link ClearingMarket} provided by the argument
     *        corresponds to a type of loan market to consider when formulating
     *        investments.
     */
   public void setLoansToInvestIn(CollectionProvider<ClearingLoanMarket> markets);
}