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
package eu.crisis_economics.abm.model.configuration;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.common.base.Preconditions;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;
import eu.crisis_economics.abm.algorithms.portfolio.SimpleStockAndLoanPortfolio;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;

@ConfigurationComponent(
   DisplayName = "Default",
   Description = 
      "A portfolio consisting of (a) a weighting algorithm w and (b) a set of stock return "
    + "estimation functions, r. This portfolio is divided between instruments according to a "
    + "set of weights w(I). Each weight w(I) corresponds to an instrument I in which to invest, "
    + "and the size of w(I) depends on the expected profitability of I. The sum of all such "
    + "weights (including the null-weight; the portfolio weight corresponding to cash not "
    + "invested in any instrument) is 1.0. The stock return function r describes how to "
    + "estimate the profitability (the return rate) of stock investments."
      )
public class SimpleStockAndLoanPortfolioConfiguration
   extends AbstractPortfolioConfiguration {
   
   @Layout(
      Order = 0.0,
      Title = "Portfolio Weights",
      FieldName = "w"
      )
   @Submodel
   @Parameter(
      ID = "SIMPLE_STOCK_AND_LOAN_PORTFOLIO_WEIGHTING"
      )
   private PortfolioWeightingAlgorithmConfiguration
      portfolioWeighting = new ExponentialAdaptivePortfolioWeightingConfiguration();
   
   public PortfolioWeightingAlgorithmConfiguration getPortfolioWeighting() {
      return portfolioWeighting;
   }
   
   public void setPortfolioWeighting(
      final PortfolioWeightingAlgorithmConfiguration value) {
      this.portfolioWeighting = Preconditions.checkNotNull(value);
   }
   
   @Layout(
      Order = 0.1,
      Title = "Stock Returns",
      FieldName = "r"
      )
   @Submodel
   @Parameter(
      ID = "SIMPLE_STOCK_AND_LOAN_STOCK_RETURN_EXPECTATION_FUNCTION"
      )
   private StockReturnExpectationFunctionConfiguration
      stockReturnExpectationFunction =
      new MovingMedianStockReturnExpectationFunctionConfiguration();
   
   public StockReturnExpectationFunctionConfiguration getStockReturnExpectationFunction() {
      return stockReturnExpectationFunction;
   }
   
   public void setStockReturnExpectationFunction(
      StockReturnExpectationFunctionConfiguration value) {
      this.stockReturnExpectationFunction = Preconditions.checkNotNull(value);
   }
   
   public SimpleStockAndLoanPortfolioConfiguration() { }
   
   public SimpleStockAndLoanPortfolioConfiguration(
      final PortfolioWeightingAlgorithmConfiguration portfolioWeighting,
      final StockReturnExpectationFunctionConfiguration stockReturnExpectationFunction
      ) {
      this.portfolioWeighting = Preconditions.checkNotNull(portfolioWeighting);
      this.stockReturnExpectationFunction = Preconditions.checkNotNull(
         stockReturnExpectationFunction);
   }
   
   @Override
   protected void addBindings() {
      bind(Portfolio.class).annotatedWith(
         Names.named(getScopeString())).to(SimpleStockAndLoanPortfolio.class);
      super.addBindings();
   }
   
   private static final long serialVersionUID = -4459122527433830028L;
}
