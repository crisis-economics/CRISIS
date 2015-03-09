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
package eu.crisis_economics.abm.strategy.leverage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * An implementation of a VaR-type bank constraint via leverage control.
  */
public final class ValueAtRiskLeverageTargetAlgorithm extends AbstractLeverageTargetAlgorithm {
   
   /*
    * Default parameters.
    */
   
   public static final double
      DEFAULT_LEVERAGE_TARGET_WHEN_VAR_CONSTRAINT_IS_NOT_ENABLED = 20.,
      DEFAULT_MINIMAL_LEVERAGE_TARGET = 5.,
      DEFAULT_MAXIMUM_LEVERAGE_TARGET = 50.0,
      DEFAULT_PORTFOLIO_COVARIANCE_ADAPTATION_RATE = 1.4e-2,
      DEFAULT_TIME_TO_ENABLE_VAR_CONSTRAINT = 250.,
      DEFAULT_TIME_TO_DISABLE_VAR_CONSTRAINT = Double.MAX_VALUE,
      DEFAULT_BREAKIN_LENGTH = 50.,
      DEFAULT_LEVERAGE_SCALING_FACTOR = .075 * .4,
      DEFAULT_COVARIANCE_MATRIX_DIAGONAL_SEED = 0.;
   
   private double 
      leverageTargetWhenNotVaRConstrained =
         DEFAULT_LEVERAGE_TARGET_WHEN_VAR_CONSTRAINT_IS_NOT_ENABLED,
      minimalLeverage =
         DEFAULT_MINIMAL_LEVERAGE_TARGET,
      maximumLeverage =
         DEFAULT_MAXIMUM_LEVERAGE_TARGET,
      covarianceAdaptationRate =
         DEFAULT_PORTFOLIO_COVARIANCE_ADAPTATION_RATE,
      enableTime =
         DEFAULT_TIME_TO_ENABLE_VAR_CONSTRAINT,
      disableTime =
         DEFAULT_TIME_TO_DISABLE_VAR_CONSTRAINT,
      breakinTime =
         DEFAULT_BREAKIN_LENGTH,
      permanentLeverageScalingFactor =
         DEFAULT_LEVERAGE_SCALING_FACTOR,
      covarianceMatrixDiagonalInitialValue =
         DEFAULT_COVARIANCE_MATRIX_DIAGONAL_SEED;
   
   private List<Double>
      leverageScalingFactorLearningWorkspace;
   
   private DoubleMatrix2D
      meanStockCovarianceMatrix;
   
   private DoubleMatrix1D
      meanLogStockReturns;
   
   private List<Double>
      lastMarketValues,
      mostRecentMarketValues;
   
   private List<String>
      tradeableStocks;
   
   final List<Double>
      lastLeverages,
      lastPortfolioStdDevs;
   
   /**
     * Create a {@link ValueAtRiskLeverageTargetAlgorithm} object.
     * 
     * @param leverageTargetWhenNotVaRConstrained
     *        The leverage target when the VaR constraint is <i>not</i> enabled.
     *        This argument should be non-negative.
     * @param minimalLeverage
     *        The minimum leverage target to apply. This argument should be non-
     *        negative.
     * @param maximumLeverage
     *        The maximum leverage target to apply. This argument should be non-
     *        negative and greater than, or equal to, {@code minimalLeverage}.
     * @param covarianceAdaptationRate
     *        The rate at which to adapt to changes in portfolio variance. This 
     *        argument should be non-negative.
     * @param enableTime
     *        The simulation time at which to enable the VaR constraint. This argument
     *        should be non-negative.
     * @param disableTime
     *        The simulation time at which to disable the VaR constraint. This
     *        argument should be non-negative and greater than, or equal to, 
     *        {@code enableTime}.
     * @param breakinTime
     *        The breakin window (length in time). During this window after 
     *        <code>enableTime</code> this algorithm will smooth the transition from
     *        fixed leverage targets to Value-At-Risk constrained leverage targets.
     *        This argument must be non-negative and less than the time window between
     *        <code>disableTime</code> and <code>enableTime</code>.
     * @param permanentLeverageScalingFactor
     *        The scale factor used to convert variance to leverage targets. This
     *        argument should be non-negative.
     * @param covarianceMatrixDiagonalInitialValue
     *        The initial value of the portfolio covariance matrix leading diagonal.
     */
   @Inject
   public ValueAtRiskLeverageTargetAlgorithm(
   @Named("VAR_CONSTRAINT_LEVERAGE_TARGET_WHEN_INACTIVE")
      final double leverageTargetWhenNotVaRConstrained,
   @Named("VAR_CONSTRAINT_LEVERAGE_TARGET_LOWER_BOUND")
      final double minimalLeverage,
   @Named("VAR_CONSTRAINT_LEVERAGE_TARGET_UPPER_BOUND")
      final double maximumLeverage,
   @Named("VAR_CONSTRAINT_PORTFOLIO_COVARIANCE_ADAPTATION_RATE")
      final double covarianceAdaptationRate,
   @Named("VAR_CONSTRAINT_ENABLE_TIME")
      final double enableTime,
   @Named("VAR_CONSTRAINT_DISABLE_TIME")
      final double disableTime,
   @Named("VAR_CONSTRAINT_BREAKIN_TIME")
      final double breakinTime,
   @Named("VAR_CONSTRAINT_SCALING_FACTOR")
      final double permanentLeverageScalingFactor,
   @Named("VAR_COVARIANCE_MATRIX_DIAGONAL_INITIAL_VALUE")
      final double covarianceMatrixDiagonalInitialValue
      ) {
      super(leverageTargetWhenNotVaRConstrained);
      
      Preconditions.checkArgument(leverageTargetWhenNotVaRConstrained >= 0.);
      Preconditions.checkArgument(minimalLeverage >= 0.);
      Preconditions.checkArgument(maximumLeverage >= 0.);
      Preconditions.checkArgument(covarianceAdaptationRate >= 0.);
      Preconditions.checkArgument(enableTime >= 0.);
      Preconditions.checkArgument(disableTime >= 0.);
      Preconditions.checkArgument(permanentLeverageScalingFactor >= 0.);
      Preconditions.checkArgument(breakinTime >= 0.);
      
      if(enableTime > disableTime)
         throw new IllegalArgumentException(
            "ValueAtRiskLeverageTargetAlgorithm: illegal procyclical leverage time window: " + 
            " [enable: " + enableTime + ", disable:" + disableTime + "].");
      if(minimalLeverage > maximumLeverage)
         throw new IllegalArgumentException(
            "ValueAtRiskLeverageTargetAlgorithm: illegal leverage lower/upper bound domain: " + 
            " [lower bound: " + minimalLeverage + ", upper bound:" + maximumLeverage + "].");
      if(breakinTime > disableTime - enableTime)
         throw new IllegalArgumentException(
            "ValueAtRiskLeverageTargetAlgorithm: breakin window is longer than the duration "
          + "this algorithm will be enabled.");
      
      this.leverageTargetWhenNotVaRConstrained = leverageTargetWhenNotVaRConstrained;
      this.minimalLeverage = minimalLeverage;
      this.maximumLeverage = maximumLeverage;
      this.covarianceAdaptationRate = covarianceAdaptationRate;
      this.enableTime = enableTime;
      this.disableTime = disableTime;
      this.permanentLeverageScalingFactor = permanentLeverageScalingFactor;
      this.covarianceMatrixDiagonalInitialValue = covarianceMatrixDiagonalInitialValue;
      this.breakinTime = breakinTime;
      
      this.lastLeverages = new ArrayList<Double>();
      this.lastPortfolioStdDevs = new ArrayList<Double>();
   }
   
   /**
     * Create a {@link ValueAtRiskLeverageTargetAlgorithm} object. This method
     * calls {@link #ValueAtRiskLeverageTargetAlgorithm(double, double, double, 
     * double, double, double, double, double)} using default values specified
     * by static fields in this class.
     */
   public ValueAtRiskLeverageTargetAlgorithm() {
      this(
         DEFAULT_LEVERAGE_TARGET_WHEN_VAR_CONSTRAINT_IS_NOT_ENABLED,
         DEFAULT_MINIMAL_LEVERAGE_TARGET,
         DEFAULT_MAXIMUM_LEVERAGE_TARGET,
         DEFAULT_PORTFOLIO_COVARIANCE_ADAPTATION_RATE,
         DEFAULT_TIME_TO_ENABLE_VAR_CONSTRAINT,
         DEFAULT_TIME_TO_DISABLE_VAR_CONSTRAINT,
         DEFAULT_BREAKIN_LENGTH,
         DEFAULT_LEVERAGE_SCALING_FACTOR,
         DEFAULT_COVARIANCE_MATRIX_DIAGONAL_SEED
         );
   }
   
   private void initializeOnFirstRequest(final Portfolio portfolio) {
      final int
         numberOfStocksToInvestIn = portfolio.stockWeights().size();
      meanStockCovarianceMatrix =
         DoubleFactory2D.dense.diagonal(
            DoubleFactory1D.dense.make(
               numberOfStocksToInvestIn, covarianceMatrixDiagonalInitialValue));
      
      meanLogStockReturns = new DenseDoubleMatrix1D(numberOfStocksToInvestIn);
      leverageScalingFactorLearningWorkspace = new ArrayList<Double>();
      tradeableStocks = new ArrayList<String>(portfolio.stockWeights().keySet());
      mostRecentMarketValues = getStockMarketValues();
   }
   
   /**
     * Is the VaR constraint enabled?
     */
   private boolean isVarConstrained() {
      final double simulationTime = Simulation.getFloorTime();
      return (simulationTime >= enableTime && simulationTime <= disableTime);
   }
   
   @Override
   public void computeTargetLeverage(Portfolio portfolio) {
      if(meanStockCovarianceMatrix == null)
         initializeOnFirstRequest(portfolio);
      
      lastMarketValues = new ArrayList<Double>(mostRecentMarketValues);
      mostRecentMarketValues = getStockMarketValues();
      
      final double portfolioStdDeviation =
         CalculatePortfolioStandardDeviation(
            tradeableStocks.size(),
            getStockMarketValues(),
            lastMarketValues,
            getOrderedStockWeights(portfolio),
            covarianceAdaptationRate,
            meanLogStockReturns,
            meanStockCovarianceMatrix
            );
      
      double newLeverageTarget = 0.;
      if(isVarConstrained()) {
         final double
            time = Simulation.getTime(),
            varLeverageTarget = permanentLeverageScalingFactor / portfolioStdDeviation;
         if(time - enableTime < breakinTime) {
            double
               weight = (time - enableTime) / breakinTime;
            newLeverageTarget =
               (1. - weight) * leverageTargetWhenNotVaRConstrained + weight * varLeverageTarget;
         }
         else
            newLeverageTarget = varLeverageTarget;
      } else {
         newLeverageTarget = leverageTargetWhenNotVaRConstrained;
         leverageScalingFactorLearningWorkspace.add(
            newLeverageTarget * portfolioStdDeviation);
      }
      
//      double newLeverageTarget = 0.;
//      if(isVarConstrained()) {
//         newLeverageTarget =
//            permanentLeverageScalingFactor * Math.log(portfolioStdDeviation);
//      } else {
//         newLeverageTarget = super.getTargetLeverage();
//         leverageScalingFactorLearningWorkspace.add(
//            newLeverageTarget / Math.log(portfolioStdDeviation));
//      }
      
      newLeverageTarget = Math.max(newLeverageTarget, minimalLeverage);
      newLeverageTarget = Math.min(newLeverageTarget, maximumLeverage);
      
      super.setTargetLeverage(newLeverageTarget);
   }
   
   /**
     * Compute the stock portfolio return standard deviation (sigma, a number).
     * 
     * A. Compute the stock portfolio covariance matrix:
     *   1. Calculate log-returns: aR(t) = log(p(t)/p(t-1)).
     *   2. Estimate the log-returns: maR(t) = d*aR(t) + (1-d)*maR(t-1) where d is memory.
     *   3. Estimate the covariance matrix:
     *      Cov(t) = d*(outer(aR(t)-maR(t),aR(t)-maR(t)) + (1-d)*Cov(t-1).
     * 
     * B. Compute the deviation of portfolio returns:
     *   1. Variance sigma**2 of the stock portfolio : sigma**2 = w**T.Cov.w 
     *   2. Assume, for simplicity, that loans have same variance.
     *   3. Return sqrt(sigma**2 + (1-sum(w))**2*s**2).
     */
   static double CalculatePortfolioStandardDeviation(
      int numberOfFirmsToInvestIn,
      List<Double> firmMarketValuesNow,
      List<Double> firmMarketValuesLast,
      double[] firmStockWeights,
      double covarianceAdaptationRate,
      DoubleMatrix1D meanLogStockReturns,
      DoubleMatrix2D meanStockCovarianceMatrix
      ) {
      Algebra algebra = new Algebra();
      DenseDoubleMatrix1D logOfStockReturns = new DenseDoubleMatrix1D(numberOfFirmsToInvestIn);
      for (int i = 0; i < numberOfFirmsToInvestIn; ++i) {
         double presentOverLastValue = (firmMarketValuesNow.get(i) / firmMarketValuesLast.get(i));
         if (presentOverLastValue > 0.) 
            logOfStockReturns.set(i, Math.log(presentOverLastValue));
         else logOfStockReturns.set(i, 0.);
      }
      
      meanLogStockReturns.assign(
         logOfStockReturns.copy().assign(Functions.mult(covarianceAdaptationRate))
         .assign(meanLogStockReturns.copy().assign(Functions.mult(1. - covarianceAdaptationRate)), 
            Functions.plus));
      
      logOfStockReturns.assign(meanLogStockReturns, Functions.minus);
      DoubleMatrix2D newCovMatrix = 
         DoubleFactory2D.dense.make(numberOfFirmsToInvestIn, numberOfFirmsToInvestIn);
      algebra.multOuter(logOfStockReturns, logOfStockReturns, newCovMatrix);
      
      meanStockCovarianceMatrix.assign(
         newCovMatrix.copy().assign(Functions.mult(covarianceAdaptationRate))
         .assign(meanStockCovarianceMatrix.copy().assign(
            Functions.mult(1. - covarianceAdaptationRate)), Functions.plus));
      
      DoubleMatrix1D stockWeightsVector =
         new DenseDoubleMatrix1D(firmStockWeights);
      final double
         loanWeight = (1. - stockWeightsVector.zSum()),
         sigmaSquared = algebra.mult(stockWeightsVector,
            algebra.mult(meanStockCovarianceMatrix, stockWeightsVector));
      
      return Math.sqrt(sigmaSquared * (1. + loanWeight * loanWeight));
   }
   
   // Get an unmodifiable list of firm stock prices.
   private List<Double> getStockMarketValues() {
      final int
         numberOfStocksToInvestIn = tradeableStocks.size();
      List<Double> marketValues =
         Arrays.asList(new Double[numberOfStocksToInvestIn]);
      for(int i = 0; i< tradeableStocks.size(); ++i) {
         final double
            stockPrice = UniqueStockExchange.Instance.getStockPrice(
               tradeableStocks.get(i));
         marketValues.set(i, stockPrice);
      }
      return Collections.unmodifiableList(marketValues);
   }
   
   // Get an ordered list of strategy stock weights.
   private double[] getOrderedStockWeights(Portfolio portfolio) {
      final Map<String, Double>
         stockWeights = portfolio.stockWeights();
      double[] result = new double[tradeableStocks.size()];
      for (int i = 0; i < tradeableStocks.size(); ++i) {
         final String
            stockName = tradeableStocks.get(i);
         final Double
            firmStockWeight = stockWeights.get(stockName);
         if(firmStockWeight == null)
            throw new IllegalStateException(
               "ValueAtRiskLeverageTargetAlgorithm.getStockWeights: " + 
               "firm with ID " + stockName + " has no stock weight in this portfolio."
               );
         result[i] = firmStockWeight;
      }
      return result;
   }
   
   /**
     * Get the minimum target leverage.
     */
   public double getMinimumLeverageTarget() {
      return minimalLeverage;
   }
   
   /**
     * Get the maximum target leverage.
     */
   public double getMaximumLeverageTarget() {
      return maximumLeverage;
   }
   
   /**
     * Get the stock covariance matrix adaptation rate.
     */
   public double getCovarianceAdaptationRate() { 
      return covarianceAdaptationRate;
   }
   
   /** 
     * Get the enable time for VaR constaints.
     */
   public double getVaRConstraintEnableTime() {
      return enableTime;
   }
   
   /** 
     * Get the disable time for VaR constraints.
     */
   public double getVaRConstaintDisableTime() {
      return disableTime;
   }
   
   /** 
     * Get the breakin time.
     */
   public double getBreakinTime() {
      return breakinTime;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return
         "VaR Leverage Targeting Algorithm, maximum leverage: " + getMaximumLeverageTarget() + 
         ", minimum leverage: " + getMinimumLeverageTarget() + ", adaptation rate: " + 
         getCovarianceAdaptationRate() + ", VaR constraint window: [" + 
         getVaRConstraintEnableTime() + ", " + getVaRConstaintDisableTime() + "].";
   }
}
