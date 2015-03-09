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
package eu.crisis_economics.abm.fund;

import com.google.common.base.Preconditions;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.portfolio.SimpleStockAndLoanPortfolio;
import eu.crisis_economics.abm.algorithms.portfolio.returns.FundamentalistStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.MedianOverHistoryStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.NoisyPortfolioWeighting;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;

/**
  * An implementation of the {@link Fund} interface, using a
  * {@link NoisyPortfolioWeighting} rule to divide portfolio
  * investments.
  * 
  * @author phillips
  */
public class NoiseTraderFund extends PortfolioFund {
   /**
     * Create a {@link NoiseTraderFund} object.
     * 
     * @param myBank
     *        The {@link DepositHolder} for this {@link Fund}.
     * @param rho
     *        The degree of influence of the underlying portfolio
     *        weighting strategy.
     * @param noiseScale
     *        The aggressiveness of the noise to be added to the
     *        underlying decision rule.
     */
   @AssistedInject
   public NoiseTraderFund(
   @Assisted
      final DepositHolder myBank,
   @Named("NOISE_TRADER_FUND_RHO")
      final double rho,
   @Named("NOISE_TRADER_FUND_NOISE_AMPLITUDE")
      final double noiseScale,
   @Named("FUNDS_CLEARING_MARKET_RESPONSES")
      final ClearingMarketParticipantFactory marketParticipationFactory,
   @Named("FUNDS_STOCK_INVESTMENT_SMOOTHING_ALGORITHM")
      final SmoothingAlgorithm targetStockInvestmentSmoothingAlgorithm
      ) {
      super(
         myBank,
         new SimpleStockAndLoanPortfolio(
            new NoisyPortfolioWeighting(rho, noiseScale),
            new MedianOverHistoryStockReturnExpectationFunction(
               new FundamentalistStockReturnExpectationFunction())
            ),
            marketParticipationFactory,
            Preconditions.checkNotNull(targetStockInvestmentSmoothingAlgorithm)
         );
   }
}

