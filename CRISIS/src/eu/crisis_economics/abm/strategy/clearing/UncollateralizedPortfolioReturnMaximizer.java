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
package eu.crisis_economics.abm.strategy.clearing;

import java.util.List;

import eu.crisis_economics.utilities.Pair;

/**
  * An interface for profit maximizing portfolio investment algorithms.
  * Implementations of this algorithm do not, internally, consider
  * collateral assets when computing the optimal investment and debt
  * distribution.
  * 
  * @author phillips
  */
public interface UncollateralizedPortfolioReturnMaximizer {
   
   /**
     * Solve a portfolio profit maximization subject to a capital constraint,
     * minimal asset investments, minimal debts and market impact. Pre-existing 
     * cash owned by the investor is taken into account during the optimization.
     * 
     * @param assetRates
     * @param liabilityRates
     * @param minimalAssetHoldings
     *         The minimum asset holdings for each investment.
     *         All records in this list should be non-negative.
     * @param minimalLiabilityHoldings
     *         The minimum debt for each libaility type. All records
     *         in this list should be non-negative.
     * @param cashToSpend
     *         Existing cash that the portfolio investor has to spend.
     * @param capitalConstraint
     *         The upper bound on the sum of all asset investments.
     * @return
     */
   public List<Double> performOptimizationWithMinimumInvestments(
      final List<Pair<Double, Double>> assetRates,         // der. const.
      final List<Pair<Double, Double>> liabilityRates,     // der. const.
      final List<Double> minimalAssetHoldings,
      final List<Double> minimalLiabilityHoldings,
      final double cashToSpend,
      final double capitalConstraint
      );
   
   /**
     * Solve a portfolio profit maximization subject to a capital constraint,
     * Pre-existing cash owned by the investor is taken into account during the
     * optimization. In order to specify mandatory minimal asset investment 
     * and/or minimal liability debts, use {@link #performOptimizationWithMinimumInvestments}.
     * 
     * @param assetRates
     * @param liabilityRates
     * @param cashToSpend
     *         Existing cash that the portfolio investor has to spend.
     * @param capitalConstraint
     *         The upper bound on the sum of all asset investments.
     * @return
     */
   public List<Double> performOptimization(
      final List<Pair<Double, Double>> assetRates,         // der. const.
      final List<Pair<Double, Double>> liabilityRates,     // der. const.
      final double cashToSpend,
      final double capitalConstraint
      );
}