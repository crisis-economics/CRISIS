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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;

/**
  * A forwarding implementation of the {@link ClearingBankStrategy} class.
  * This class accepts an existing {@link ClearingBankStrategy} to which
  * all contracted methods are delegated, with the exception of
  * {@link #getDesiredStockInvestmentIn}, which is supplemented with 
  * a target-investment smoothing algorithm. 
  * 
  * @author phillips
  */
public class SmoothingClearingStrategy implements ClearingBankStrategy {
   
   private final ClearingBankStrategy
      implementation;
   private final SmoothingAlgorithm
      stockSmoothingAlgorithm;
   
   /**
     * Create a {@link SmoothingClearingStrategy} with custom parameters.
     * 
     * @param implementation
     *        An existing {@link ClearingBankStrategy} object to which to delegate.
     * @param stockSmoothingAlgorithm
     *        A {@link SmoothingAlgorithm} used to smooth target stock investment
     *        targets for the specified delegate.
     */
   @Inject
   public SmoothingClearingStrategy(
   @Named("SMOOTHING_CLEARING_BANK_STRATEGY_IMPLEMENTATION")
      final ClearingBankStrategy implementation,
   @Named("SMOOTHING_CLEARING_BANK_STRATEGY_SMOOTHING_ALGORITHM")
      final SmoothingAlgorithm stockSmoothingAlgorithm
      ) {
      this.implementation = Preconditions.checkNotNull(implementation);
      this.stockSmoothingAlgorithm = Preconditions.checkNotNull(stockSmoothingAlgorithm);
   }
   
   @Override
   public void considerDepositMarkets() {
      implementation.considerDepositMarkets();
   }
   
   @Override
   public void considerCommercialLoanMarkets() {
      implementation.considerCommercialLoanMarkets();
   }
   
   @Override
   public void considerStockMarkets() {
      implementation.considerStockMarkets();
   }
   
   @Override
   public void considerInterbankMarkets() {
      implementation.considerInterbankMarkets();
   }
   
   public void decidePorfolioSize() {
      implementation.decidePorfolioSize();
   }
   
   @Override
   public void decidePortfolioDistribution() {
      implementation.decidePortfolioDistribution();
   }
   
   @Override
   public void afterInterbankMarkets() {
      implementation.afterInterbankMarkets();
   }
   
   @Override
   public void decideDividendPayment() {
      implementation.decideDividendPayment();
   }
   
   @Override
   public double getDepositRate() {
      return implementation.getDepositRate();
   }
   
   @Override
   public double getLendingRate() {
      return implementation.getLendingRate();
   }
   
   @Override
   public double getInterLendingRate() {
      return implementation.getInterLendingRate();
   }
   
   @Override
   public double getDesiredStockInvestmentIn(
      final ClearingStockMarket market) {
      final double
         ideal = implementation.getDesiredStockInvestmentIn(market),
         actual = getBank().getStockInvestmentIn(market.getStockReleaserName()),
         target = stockSmoothingAlgorithm.applySmoothing(ideal, actual);
      return target;
   }
   
   @Override
   public void registerNewOrder(final Order newOrder) {
      implementation.registerNewOrder(newOrder);
   }
   
   @Override
   public double getLoanSize() {
      return implementation.getLoanSize();
   }
   
   @Override
   public double getCommercialLoanOrderSize() {
      return implementation.getCommercialLoanOrderSize();
   }
   
   @Override
   public double getDesiredLoanInvestmentIn(final ClearingLoanMarket market) {
      return implementation.getDesiredLoanInvestmentIn(market);
   }
   
   @Override
   public void setStocksToInvestIn(
      final CollectionProvider<ClearingStockMarket> markets) {
      implementation.setStocksToInvestIn(markets);
   }
   
   @Override
   public void setLoansToInvestIn(
      final CollectionProvider<ClearingLoanMarket> markets) {
      implementation.setLoansToInvestIn(markets);
   }
   
   @Override
   public StrategyBank getBank() {
      return implementation.getBank();
   }
   
   /**
     * Get the stock target investment smoothing algorithm for this object.
     */
   public SmoothingAlgorithm getStockSmoothingAlgorithm() {
      return stockSmoothingAlgorithm;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Smoothing Clearing Strategy, stock smoothing algorithm: "
            + stockSmoothingAlgorithm + ".";
   }
}
