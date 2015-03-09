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
package eu.crisis_economics.abm.algorithms.portfolio.returns;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;

/***************************************************************
 * <p>
 * Implementation of a fundamentalist expectation
 * function where expected return is
 * </p>
 * <code>
 * e = (d/v)/p
 * </code>
 * <p>
 * where
 * </p>
 * <ul>
 * <li>d is the dividend per share</li>
 * <li>v is the current share value</li>
 * <li>p is a risk premium</li>
 * </ul>
 ***************************************************************/
public final class FundamentalistStockReturnExpectationFunction
   implements StockReturnExpectationFunction {
   
   public final static double
      DEFAULT_FUNDAMENTALIST_STOCK_RETURN_EXPECTATION_RISK_PREMIUM = 1.0;
   
   private double
      stockRiskPremium;
   
   /**
     * Create a {@link FundamentalistStockReturnExpectationFunction} return
     * estimation algorithm with custom parameters.
     * 
     * @param riskPremium
     *        The investment risk premium to apply. This argument should be
     *        strictly positive.
     */
   @Inject
   public FundamentalistStockReturnExpectationFunction(
   @Named("FUNDAMENTALIST_STOCK_RETURN_EXPECTATION_RISK_PREMIUM")
      final double riskPremium
      ) {
      Preconditions.checkArgument(riskPremium > 0.);
      this.stockRiskPremium = riskPremium;
   }
   
   /**
     * Create a {@link FundamentalistStockReturnExpectationFunction} return
     * estimation algorithm with default parameters.
     */
   public FundamentalistStockReturnExpectationFunction() {
      this(DEFAULT_FUNDAMENTALIST_STOCK_RETURN_EXPECTATION_RISK_PREMIUM);
   }
   
   @Override
   public double computeExpectedReturn(String stockName) {
      final StockReleaser
         releaser = UniqueStockExchange.Instance.getStockReleaser(stockName);
      double
         dividendPerShare = releaser.getDividendPerShare(),
         marketValue = releaser.getMarketValue();
      if(marketValue == 0.0)
         return 0.;
      else
         return((dividendPerShare/marketValue)/stockRiskPremium);
   }
   
   public void setStockRiskPremium(double r) {
       stockRiskPremium = r;
   }
   
   public double getStockRiskPremium() {
       return(stockRiskPremium);
   }
}
