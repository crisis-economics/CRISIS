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
package eu.crisis_economics.abm.bank.strategies.clearing;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.algorithms.portfolio.SimpleStockAndLoanPortfolio;
import eu.crisis_economics.abm.algorithms.portfolio.returns.FundamentalistStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.LinearCombinationStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.MedianOverHistoryStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.TrendFollowerStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.PortfolioWeighting;
import eu.crisis_economics.abm.bank.BankStub;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;

/**
  * A specialization of the {@link PortfolioClearingStrategy} class for use with 
  * {@link BankStub} {@link Agent}{@code s}.
  * 
  * @author phillips
  */
public final class BankStubClearingBankStrategy extends SmoothingClearingStrategy {
   
   @Inject
   public BankStubClearingBankStrategy(
   @Assisted
      final ClearingBank bank,
   @Named("BANK_STUB_STRATEGY_PORTFOLIO_SIZE_TARGETS")
      final TimeseriesParameter<Double> portfolioSizeTargets,
   @Named("BANK_STUB_STRATEGY_TOTAL_DIVIDEND_PAYMENTS")
      final TimeseriesParameter<Double> totalDividendPayments,
   @Named("BANK_STUB_STRATEGY_PORTFOLIO_WEIGHTING_ALGORITHM")
      final PortfolioWeighting portfolioWeightingAlgorithm,
   @Named("BANK_STUB_STRATEGY_PORTFOLIO_SMOOTHING_ALGORITHM")
      final SmoothingAlgorithm smoothingAlgorithm,
   @Named("BANK_STUB_STRATEGY_STOCK_RISK_PREMIUM")
      final double stockRiskPremium,
   @Named("BANK_STUB_STRATEGY_LAG")
      final int chartistLag,
   @Named("BANK_STUB_STRATEGY_WEIGHT")
      final TimeseriesParameter<Double> weight
      ) {
      super(
         new CustomizableClearingStrategy(
            bank,
            new SimpleStockAndLoanPortfolio(
               portfolioWeightingAlgorithm,
               new MedianOverHistoryStockReturnExpectationFunction(
                  new LinearCombinationStockReturnExpectationFunction(
                     new FundamentalistStockReturnExpectationFunction(stockRiskPremium),
                     new TrendFollowerStockReturnExpectationFunction(chartistLag),
                     weight
                  )
               )), 
            portfolioSizeTargets,
            totalDividendPayments
            ),
            smoothingAlgorithm
         );
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Bank Stub Strategy.";
   }
}
