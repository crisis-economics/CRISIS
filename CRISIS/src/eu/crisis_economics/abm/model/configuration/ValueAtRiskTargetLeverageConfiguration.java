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

import com.google.common.base.Preconditions;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.abm.strategy.leverage.LeverageTargetAlgorithm;
import eu.crisis_economics.abm.strategy.leverage.ValueAtRiskLeverageTargetAlgorithm;

@ConfigurationComponent(
      Description =
        "Banks using a Value at Risk (VaR) target leverage algorithm adjust their "
      + "leverage target over time to take into account their perception of risk, which "
      + "depends on the volatility of asset returns in their portfolio, and reflects Basel II"
      + " risk regulation.  In particular, "
      + "\nLeverage_Target = Scaling_Factor / Portfolio_Return_Standard_Deviation \nwhere the "
      + "Scaling_Factor is a parameter set below."
      + "\nWhenever a bank has a leverage below this target, it will try to"
      + " increase leverage (for example, by issuing more loans to firms), and whenver a "
      + "bank's leverage is above this target, it "
      + "will try to de-leverage (for example, by selling assets).\nThe definition of "
      + "leverage in this model"
      + " is Risky_Assets / Equity, so a bank's (non-risky) cash reserves are not included in"
      + " the Risky_Assets numerator."
      + "\n\nThe adaptation rate parameter is a smoothing factor used in the "
      + "exponentially weighted moving average estimator of the covariance "
      + "matrix of the stock portfolio, which is used to calculate the standard deviation of "
      + "the bank's portfolio."
      + "\n\nThe VaR leverage targeting can be enabled and subsequently disabled at a specific "
      + "time within the simulation, so that the user can observe the impact of changing the "
      + "leverage targeting strategy from a constant leverage to a VaR targeting strategy. "
      + "'Enable VaR targeting at time-step' parameter specifies when the "
      + "VaR targeting is turned on in the model, and the time-step at which it is disabled "
      + "is controlled by the 'Disable VaR targeting at time-step' parameter.  When the VaR "
      + "leverage targeting is turned off, the leverage target is a constant specified by the"
      + " 'Leverage Target when NOT VaR targeting' parameter.",
      DisplayName = "Value-at-Risk Constrained Leverage"
      )
public class ValueAtRiskTargetLeverageConfiguration
   extends AbstractBankLeverageTargetConfiguration {
   
   @Layout(
         Order = 2,
         FieldName = "Leverage Target when NOT VaR targeting")
   @Parameter(
      ID = "VAR_CONSTRAINT_LEVERAGE_TARGET_WHEN_INACTIVE"
      )
   private double
      leverageTargetWhenNotVaRConstrained =
         ValueAtRiskLeverageTargetAlgorithm.
            DEFAULT_LEVERAGE_TARGET_WHEN_VAR_CONSTRAINT_IS_NOT_ENABLED;
   @Layout(Order = 3,
         FieldName = "Minimum Leverage")
   @Parameter(
      ID = "VAR_CONSTRAINT_LEVERAGE_TARGET_LOWER_BOUND"
      )
   private double
      minimalLeverage =
         ValueAtRiskLeverageTargetAlgorithm.DEFAULT_MINIMAL_LEVERAGE_TARGET;
   @Layout(Order = 4,
         FieldName = "Maximum Leverage")
   @Parameter(
      ID = "VAR_CONSTRAINT_LEVERAGE_TARGET_UPPER_BOUND"
      )
   private double
      maximumLeverage =
         ValueAtRiskLeverageTargetAlgorithm.DEFAULT_MAXIMUM_LEVERAGE_TARGET;
   @Layout(Order = 5,
         FieldName = "Adaptation Rate")
   @Parameter(
      ID = "VAR_CONSTRAINT_PORTFOLIO_COVARIANCE_ADAPTATION_RATE"
      )
   private double
      covarianceAdaptationRate =
         ValueAtRiskLeverageTargetAlgorithm.DEFAULT_PORTFOLIO_COVARIANCE_ADAPTATION_RATE;
   @Layout(
         Order = 0,
         FieldName = "Enable VaR leverage targeting at time-step")
   @Parameter(
      ID = "VAR_CONSTRAINT_ENABLE_TIME"
      )
   private double
      enableTime =
         ValueAtRiskLeverageTargetAlgorithm.DEFAULT_TIME_TO_ENABLE_VAR_CONSTRAINT;
   @Layout(
         Order = 0.1,
         FieldName = "Disable VaR leverage targeting at time-step")
   @Parameter(
      ID = "VAR_CONSTRAINT_DISABLE_TIME"
      )
   private double
      disableTime =
         ValueAtRiskLeverageTargetAlgorithm.DEFAULT_TIME_TO_DISABLE_VAR_CONSTRAINT;
   @Layout(
         Order = 0.2,
         FieldName = "VaR breakin length")
   @Parameter(
      ID = "VAR_CONSTRAINT_BREAKIN_TIME"
      )
   private double
      breakinLength =
         ValueAtRiskLeverageTargetAlgorithm.DEFAULT_BREAKIN_LENGTH;
   @Layout(Order = 7,
         FieldName = "Scaling Factor")
   @Parameter(
      ID = "VAR_CONSTRAINT_SCALING_FACTOR"
      )
   private double
      permanentLeverageScalingFactor =
         ValueAtRiskLeverageTargetAlgorithm.DEFAULT_LEVERAGE_SCALING_FACTOR;
   @Layout(Order = 6)
   @Parameter(
      ID = "VAR_COVARIANCE_MATRIX_DIAGONAL_INITIAL_VALUE"
      )
   private double
      covarianceMatrixDiagonalInitialValue =
         ValueAtRiskLeverageTargetAlgorithm.DEFAULT_COVARIANCE_MATRIX_DIAGONAL_SEED;
   
   public double getLeverageTargetWhenNotVaRConstrained() {
      return leverageTargetWhenNotVaRConstrained;
   }
   
   public void setLeverageTargetWhenNotVaRConstrained(
      final double leverageTargetWhenNotVaRConstrained) {
      this.leverageTargetWhenNotVaRConstrained = leverageTargetWhenNotVaRConstrained;
   }
   
   public final String desLeverageTargetWhenNotVaRConstrained(){
      return "The leverage target when the VaR targeting is NOT enabled.  This argument should be "
            + "non-negative.  The 'Enable VaR targeting at time-step' parameter specifies when the "
            + "VaR targeting is turned on in the model, and the time-step at which it is disabled "
            + "is controlled by the 'Disable VaR targeting at time-step' parameter.  When the VaR "
            + "leverage targeting is turned off, the leverage target is a constant specified by the"
            + " 'Leverage Target when NOT VaR targeting' parameter.";
   }
   
   public double getMinimalLeverage() {
      return minimalLeverage;
   }
   
   public void setMinimalLeverage(
      final double minimalLeverage) {
      this.minimalLeverage = minimalLeverage;
   }
   
   public final String desMinimalLeverage() {
      return "The minimum bound that the banks can assign their leverage target to.  This value"
           + " should be non-negative.";
   }
   
   public double getMaximumLeverage() {
      return maximumLeverage;
   }
   
   public void setMaximumLeverage(
      final double maximumLeverage) {
      this.maximumLeverage = maximumLeverage;
   }
   
   public final String desMaximumLeverage() {
      return "The maximum bound that the banks can assign their leverage target to.  "
            + "This value should be non-negative and greater "
            + "than, or equal to, the 'Minimum Leverage' parameter.";
   }
   
   public double getCovarianceAdaptationRate() {
      return covarianceAdaptationRate;
   }
   
   public void setCovarianceAdaptationRate(
      final double covarianceAdaptationRate) {
      this.covarianceAdaptationRate = covarianceAdaptationRate;
   }
   
   public final String desCovarianceAdaptationRate() {
      return "The adaptation rate parameter (d) is a smoothing factor used in the "
            + "exponentially weighted moving average estimator of the covariance "
            + "matrix of the stock portfolio returns.  The number must be greater than 0 and less "
            + "than, or equal to, 1.\nWhen the adaptation rate is large, more "
            + "importance is given to the most recent measurement of log-returns in the "
            + "exponentially weighted moving average of log-returns, and to the most recent "
            + "estimate of log-return covariance.  Conversely, the previous estimates are given "
            + "less importance in the estimation of the covariance matrix and log-return "
            + "measurements. \nThe parameter is used to calculate the "
            + "standard deviation of the bank's portfolio returns as follows: "
            + "\n\nA. Compute the stock portfolio covariance matrix:"
            + "\n 1. Calculate log-returns: r(t) = log( p(t) / p(t-1) ) for each stock price p."
            + "\n 2. Estimate the exponentially weighted moving average of the log-returns for each"
            + " stock: \n    r_ewma(t) = d * r(t) + (1 - d) * r_ewma(t-1) "
            + "\n where d is the adaptation rate."
            + "\n 3. Estimate the covariance matrix, Cov:"
            + "\n    Cov(t) = d * (outer_product (r(t) - r_ewma(t), r(t) - r_ewma(t)) + (1 - d) * "
            + "Cov(t-1)."
            + "\n\nB. Compute the standard deviation of portfolio returns:"
            + "\n 1. Variance sigma^2 of the stock portfolio : sigma^2 = w' . Cov(t) . w, where w'"
            + " is the transpose of the column vector of stock weights in the portfolio, w, and the"
            + " dot operator corresponds to matrix multiplication."
            + "\n 2. Assume, for simplicity, that loan returns have same variance."
            + "\n 3. Return sqrt(sigma^2 + (1 - sum(w))^2 * s^2).";
   }
   
   public double getEnableTime() {
      return enableTime;
   }
   
   public void setEnableTime(
      final double enableTime) {
      this.enableTime = enableTime;
   }
   
   public final String desEnableTime() {
      return "The simulation time at which to enable the VaR constraint. This argument should be "
            + "non-negative.";
   }
   
   public double getDisableTime() {
      return disableTime;
   }
   
   public void setDisableTime(
      final double disableTime) {
      this.disableTime = disableTime;
   }
   
   public final String desDisableTime() {
      return "The simulation time at which to disable the VaR constraint. This argument should be "
            + "non-negative and greater than, or equal to, the time the VaR constraint was "
            + "enabled.";
   }
   
   public double getBreakinLength() {
      return breakinLength;
   }
   
   public void setBreakinLength(
      final double value) {
      this.breakinLength = value;
   }
   
   public final String desBreakinLength() {
      return "The length (time units) of the period after the VaR constraint is enabled during"
           + " which the transition from non-VaR constrained to VaR-constrained leverage targets "
           + "will be smoothed.";
   }
   
   public double getPermanentLeverageScalingFactor() {
      return permanentLeverageScalingFactor;
   }
   
   public void setPermanentLeverageScalingFactor(
      final double permanentLeverageScalingFactor) {
      this.permanentLeverageScalingFactor = permanentLeverageScalingFactor;
   }
   
   public final String desPermanentLeverageScalingFactor() {
      return "The scale factor used to convert the standard deviation of the bank's portfolio value"
            + " to its leverage target.  This value should be non-negative and is used in the "
            + "following way:"
            + "\nLeverage_Target = Scaling_Factor / Portfolio_Return_Standard_Deviation";
   }
   
   public double getCovarianceMatrixDiagonalInitialValue() {
      return covarianceMatrixDiagonalInitialValue;
   }
   
   public void setCovarianceMatrixDiagonalInitialValue(
      final double covarianceMatrixDiagonalInitialValue) {
      this.covarianceMatrixDiagonalInitialValue = covarianceMatrixDiagonalInitialValue;
   }
   
   public final String desCovarianceMatrixDiagonalInitialValue() {
      return "The initial value of the leading diagonal entries of the bank's stock portfolio "
            + "returns covariance matrix estimate.  This is the initial covariance matrix "
            + "Cov(t = 0) used in the calculation of the "
            + "standard deviation of the bank's portfolio returns as follows: "
            + "\n\nA. Compute the stock portfolio covariance matrix:"
            + "\n 1. Calculate log-returns: r(t) = log( p(t) / p(t-1) ) for each stock price p."
            + "\n 2. Estimate the exponentially weighted moving average of the log-returns for each"
            + " stock: \n    r_ewma(t) = d * r(t) + (1 - d) * r_ewma(t-1) "
            + "\n where d is the adaptation rate."
            + "\n 3. Estimate the covariance matrix, Cov:"
            + "\n    Cov(t) = d * (outer_product (r(t) - r_ewma(t), r(t) - r_ewma(t)) + (1 - d) * "
            + "Cov(t-1)."
            + "\n\nB. Compute the standard deviation of portfolio returns:"
            + "\n 1. Variance sigma^2 of the stock portfolio : sigma^2 = w' . Cov(t) . w, where w'"
            + " is the transpose of the column vector of stock weights in the portfolio, w, and the"
            + " dot operator corresponds to matrix multiplication."
            + "\n 2. Assume, for simplicity, that loan returns have same variance."
            + "\n 3. Return sqrt(sigma^2 + (1 - sum(w))^2 * s^2).";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(leverageTargetWhenNotVaRConstrained >= 0.,
         toParameterValidityErrMsg("Unconstrained Leverage Target is negative."));
      Preconditions.checkArgument(minimalLeverage >= 0.,
         toParameterValidityErrMsg("Minimum Leverage Target is negative."));
      Preconditions.checkArgument(maximumLeverage >= 0.,
         toParameterValidityErrMsg("Maximum Leverage Target is negative."));
      Preconditions.checkArgument(covarianceAdaptationRate > 0.,
         toParameterValidityErrMsg("Covariance Adaptation Rate is not strictly positive."));
      Preconditions.checkArgument(covarianceAdaptationRate < 1.,
            toParameterValidityErrMsg("Covariance Adaptation Rate is greater than, or equal to, 1."));
      Preconditions.checkArgument(enableTime >= 0.,
         toParameterValidityErrMsg("VaR Constraint Enable Time is negative."));
      Preconditions.checkArgument(disableTime >= 0,
         toParameterValidityErrMsg("VaR Constraint Disable Time is negative."));
      Preconditions.checkArgument(permanentLeverageScalingFactor >= 0.,
         toParameterValidityErrMsg("Permanent Leverage Scaling Factor is negative."));
      Preconditions.checkArgument(enableTime < disableTime,
         toParameterValidityErrMsg(
            "illegal procyclical leverage time window: "
          + " [enable: " + enableTime + ", disable:" + disableTime + "]."));
      Preconditions.checkArgument(minimalLeverage < maximumLeverage,
         toParameterValidityErrMsg(
            "illegal leverage lower/upper bound domain: "
          + " [lower bound: " + minimalLeverage + ", upper bound:" + maximumLeverage + "]."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
      "VAR_CONSTRAINT_LEVERAGE_TARGET_WHEN_INACTIVE",    leverageTargetWhenNotVaRConstrained,
      "VAR_CONSTRAINT_LEVERAGE_TARGET_LOWER_BOUND",      minimalLeverage,
      "VAR_CONSTRAINT_LEVERAGE_TARGET_UPPER_BOUND",      maximumLeverage,
      "VAR_CONSTRAINT_PORTFOLIO_COVARIANCE_ADAPTATION_RATE",
         covarianceAdaptationRate,
      "VAR_CONSTRAINT_ENABLE_TIME",                      enableTime,
      "VAR_CONSTRAINT_DISABLE_TIME",                     disableTime,
      "VAR_CONSTRAINT_BREAKIN_TIME",                     breakinLength,
      "VAR_CONSTRAINT_SCALING_FACTOR",                   permanentLeverageScalingFactor,
      "VAR_COVARIANCE_MATRIX_DIAGONAL_INITIAL_VALUE",    covarianceMatrixDiagonalInitialValue
      ));
      bind(
         LeverageTargetAlgorithm.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(ValueAtRiskLeverageTargetAlgorithm.class);
   }
   
   private static final long serialVersionUID = -7244654468263158551L;
}