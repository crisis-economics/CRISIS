/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
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

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;
import eu.crisis_economics.abm.algorithms.portfolio.SimpleStockAndLoanPortfolio;
import eu.crisis_economics.abm.algorithms.portfolio.returns.StockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.LogitPortfolioWeighting;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.PortfolioWeighting;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;

/**
  * An implementation of the {@link Fund} interface, using a
  * {@link LogitPortfolioWeighting} rule to divide portfolio
  * investments.
  * 
  * @author tang
  * @author phillips
  */
public final class MutualFund extends PortfolioFund {
   
   /**
     * Create a {@link MutualFund} {@link Agent}. This constructor creates a
     * {@link MutualFund} {@link Agent} with default {@link ClearingMarket}
     * responses. For details of these default settings, see {@link 
     * PortfolioFund#PortfolioFund(DepositHolder, Portfolio, ClearingHouse)}.
     * <br><br>
     * 
     * @param portfolioWeighting
     *        The {@link PortfolioWeighting} algorithm used to distribute
     *        {@link Fund} investments over instruments (eg. stocks, loans)
     * @param stockReturnExpectationFunction
     *        The {@link StockReturnExpectationFunction} used to formulate
     *        stock return estimates for stock instruments.
     */
   public MutualFund(
      final DepositHolder depositHolder,
      final PortfolioWeighting portfolioWeighting,
      final StockReturnExpectationFunction stockReturnExpectationFunction,
      final ClearingHouse clearingHouse,
      final SmoothingAlgorithm targetStockInvestmentSmoothingAlgorithm
      ) {
      super(
         depositHolder,
         new SimpleStockAndLoanPortfolio(
            portfolioWeighting,
            stockReturnExpectationFunction
            ),
         clearingHouse,
         targetStockInvestmentSmoothingAlgorithm
         );
   }
   
   /**
     * Create a {@link MutualFund} {@link Agent}. This constructor fully exposes the 
     * {@link ClearingMarketParticipant} component of the {@link MutualFund}.
     * For a simplified constructor, use {@link MutualFund}(DepositHolder, PortfolioWeighting, 
     * StockReturnExpectationFunction, ClearingHouse).
     * 
     * @param depositHolder
     *        The link {@link DepositHolder} who will manage {@link Fund} 
     *        deposits
     * @param portfolioWeighting
     *        The {@link PortfolioWeighting} algorithm used to distribute
     *        {@link Fund} investments over instruments (eg. stocks, loans)
     * @param stockReturnExpectationFunction
     *        The {@link StockReturnExpectationFunction} used to formulate
     *        stock return estimates for stock instruments.
     * @param marketParticipationFactory
     *        A factory object to create a {@link ClearingMarketParticipant}.
     *        The {@link ClearingMarketParticipant} is used in composition to
     *        define the response of this {@link MutualFund} to all clearing
     *        markets.
     * @param stockReturnExpectationFunction
     *        The {@link StockReturnExpectationFunction} used to formulate
     *        stock return estimates for stock instruments.
     */
   @AssistedInject
   public MutualFund(
   @Assisted
      final DepositHolder depositHolder,
   @Named("FUNDS_CLEARING_MARKET_RESPONSES")
      final ClearingMarketParticipantFactory marketParticipationFactory,
   @Named("FUNDS_PORTFOLIO")
      final Portfolio portfolio,
   @Named("FUNDS_STOCK_INVESTMENT_SMOOTHING_ALGORITHM")
      final SmoothingAlgorithm targetStockInvestmentSmoothingAlgorithm
      ) {
      super(
         depositHolder,
         portfolio,
         marketParticipationFactory,
         Preconditions.checkNotNull(targetStockInvestmentSmoothingAlgorithm)
         );
   }
}
