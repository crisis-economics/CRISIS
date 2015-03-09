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

import org.apache.commons.math3.analysis.UnivariateFunction;

import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.bank.strategies.TimeSeriesCatalogue;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * {@link FixedLoanAndDividendPerShareClearingBankStrategy} is an implementation 
  * of {@link ClearingBankStrategy} describing a {@link Bank} strategy with customizable
  * and predetermined loan market portfolios and dividend per share (DPS) payments.
  * This strategy offers the following:<br><br>
  * 
  *    (a) a custom loan market portfolio size (fixed in time, or predetermined);<br>
  *    (b) a fixed DPS offering (fixed in time, or predetermined).<br><br>
  * 
  * In both (a) and (b), negative values will be treated as zero. The same portfolio
  * size will be offered to every available loan market.<br><br>
  * 
  * Predetermined non-constant timeseries are specified as univariate functions (f) of
  * the simulation time (t). When evaluated, floor(t) is used rather than t: f(floor(t)).
  * <br><br>
  * 
  * This strategy describes a bank with no particular target leverage and no particular
  * target equity. For this reason, {@link #getTargetEquity()} will return the current
  * {@link Bank} equity, and {@link #getTargetLeverage()} will return 1.0 always.
  * 
  * @author lovric
  * @author phillips
  */
public final class FixedLoanAndDividendPerShareClearingBankStrategy
   extends EmptyBankStrategy implements ClearingBankStrategy {
   
   private static final double 
      FIXED_DEFAULT_LOAN_PORTFOLIO_SIZE_TARGET = 1.e11,
      FIXED_DEFAULT_DIVIDEND_PER_SHARE_TARGET = 1.e9;
   
   private UnivariateFunction
      loanSupplyTimeSeries,
      dividendPerShareTimeSeries;
   
   /** 
     * Create a {@link FixedLoanAndDividendPerShareClearingBankStrategy} with
     * a default and constant loan portfolio size target and default constant
     * dividend per share target.
     */
   public FixedLoanAndDividendPerShareClearingBankStrategy(
      final StrategyBank bank
      ) {
      this(
         bank,
         TimeSeriesCatalogue.constantFunction(FIXED_DEFAULT_LOAN_PORTFOLIO_SIZE_TARGET),
         TimeSeriesCatalogue.constantFunction(FIXED_DEFAULT_DIVIDEND_PER_SHARE_TARGET)
         );
   }
   
   /** 
     * Create a {@link FixedLoanAndDividendPerShareClearingBankStrategy} with
     * a custom and constant loan portfolio size target and custom constant
     * dividend per share target.
     */
   public FixedLoanAndDividendPerShareClearingBankStrategy(
      final StrategyBank bank,
      final double fixedLoanPortfolioSize,
      final double fixedDefaultDividendPerShare
      ) {
      this(
         bank,
         TimeSeriesCatalogue.constantFunction(initGuard(fixedLoanPortfolioSize)),
         TimeSeriesCatalogue.constantFunction(initGuard(fixedDefaultDividendPerShare))
         );
   }
   
   /** 
     * Create a {@link FixedLoanAndDividendPerShareClearingBankStrategy} with
     * a custom and non-constant loan portfolio size target and custom non-constant
     * dividend per share target.
     */
   public FixedLoanAndDividendPerShareClearingBankStrategy(
      final StrategyBank bank,
      final UnivariateFunction loanSupplyTimeSeries,
      final UnivariateFunction dividendPerShareTimeSeries
      ) {
      super(bank);
      StateVerifier.checkNotNull(loanSupplyTimeSeries, dividendPerShareTimeSeries);
      this.loanSupplyTimeSeries = loanSupplyTimeSeries;
      this.dividendPerShareTimeSeries = dividendPerShareTimeSeries;
   }
   
   /**
     * Assert the sign of the argument.
     */
   private static double initGuard(final double value) {
      if(value < 0.) 
         throw new IllegalArgumentException(
            "FixedLoanAndDividendPerShareClearingBankStrategy: error: argument is negative (value " 
            + value + ").");
      return value;
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
      final double
         numberOfSharesInExistence = getBank().getNumberOfEmittedShares(),
         dividendToPay = dividendPerShareTimeSeries.value(Simulation.getFloorTime());
      if(dividendToPay <= 0. || numberOfSharesInExistence <= 0.)
         getBank().setDividendPerShare(0.);
      else
         getBank().setDividendPerShare(dividendToPay / numberOfSharesInExistence);
   }
   
   @Override
   public double getDesiredStockInvestmentIn(final ClearingStockMarket market) {
      return 0;
   }
   
   @Override
   public double getDesiredLoanInvestmentIn(final ClearingLoanMarket market) {
      return loanSupplyTimeSeries.value(Simulation.getFloorTime());
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
      return "Fixed Loan And Dividend Per Share Clearing Bank Strategy,"
            + " loan supply timeseries:" + loanSupplyTimeSeries
            + ", dividend per share timeseries: "
            + dividendPerShareTimeSeries + ", bank: "
            + getBank() + ".";
   }
}
