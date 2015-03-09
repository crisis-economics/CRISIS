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

import eu.crisis_economics.abm.fund.FundInvestmentDecisionRule;
import eu.crisis_economics.abm.fund.SimpleThresholdFundInvestmentDecisionRule;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Contribution Threshold"
   )
public final class SimpleThresholdFundInvestmentDecisionRuleConfiguration
   extends AbstractFundInvestmentDecisionRuleConfiguration
   implements HouseholdFundContributionRuleConfiguration {
   
   private static final long serialVersionUID = 3284292035032881208L;
   
   public final static double
      DEFAULT_HOUSEHOLD_TARGET_FUND_INVESTMENT_VALUE = 6.75e7 * 2.,
      DEFAULT_EXCESS_INVESTMENT_WITHDRAWAL_RATE = 1.0,
      DEFAULT_DEFICIENT_INVESTMENT_CONTRIBUTION_RATE = .9;
   
   @Parameter(
      ID = "TARGET_VALUE_FUND_CONTRIBUTION_RULE_WITHDRAWAL_THRESHOLD"
      )
   private double
      householdTargetFundInvestmentValue =
         DEFAULT_HOUSEHOLD_TARGET_FUND_INVESTMENT_VALUE;
   @Parameter(
      ID = "TARGET_VALUE_FUND_CONTRIBUTION_RULE_EXCESS_INVESTMENT_WITHDRAWAL_RATE"
      )
   private double
      excessInvestmentWithdrawalRate =
         DEFAULT_EXCESS_INVESTMENT_WITHDRAWAL_RATE;
   @Parameter(
      ID = "TARGET_VALUE_FUND_CONTRIBUTION_RULE_DEFICIENT_INVESTMENT_CONTRIBUTION_RATE"
      )
   private double
      deficientInvestmentContributionRate =
         DEFAULT_DEFICIENT_INVESTMENT_CONTRIBUTION_RATE;
   
   public final double getHouseholdTargetFundInvestmentValue() {
      return householdTargetFundInvestmentValue;
   }
   
   public final void setHouseholdTargetFundInvestmentValue(final double value) {
      householdTargetFundInvestmentValue = value;
   }
   
   public final String desHouseholdTargetFundInvestmentValue() {
      return "The target value of fund investments for households.";
   }
   
   public double getExcessInvestmentWithdrawalRate() {
      return excessInvestmentWithdrawalRate;
   }
   
   public void setExcessInvestmentWithdrawalRate(
      double excessInvestmentWithdrawalRate) {
      this.excessInvestmentWithdrawalRate = excessInvestmentWithdrawalRate;
   }
   
   public double getDeficientInvestmentContributionRate() {
      return deficientInvestmentContributionRate;
   }
   
   public void setDeficientInvestmentContributionRate(
      double deficientInvestmentContributionRate) {
      this.deficientInvestmentContributionRate = deficientInvestmentContributionRate;
   }
   
   /**
     * Create a {@link SimpleThresholdFundInvestmentDecisionRuleConfiguration} object
     * with default parameters.
     */
   public SimpleThresholdFundInvestmentDecisionRuleConfiguration() { }
   
   /**
     * Create a {@link SimpleThresholdFundInvestmentDecisionRuleConfiguration} object
     * with custom parameters.<br><br>
     * 
     * See also {@link SimpleThresholdFundInvestmentDecisionRule}.
     * 
     * @param householdTargetFundInvestmentValue <br>
     *        The target fund account investment value for decision rules 
     *        configured by this component. This argument must be non-negative.
     * @param excessInvestmentWithdrawalRate <br>
     *        The rate at which to withdraw excess fund investment account value.
     * @param deficientInvestmentContributionRate <br>
     *        The rate at which to invest excess cash into the fund investment 
     *        account.
     */
   public SimpleThresholdFundInvestmentDecisionRuleConfiguration(
      final double householdTargetFundInvestmentValue,
      final double excessInvestmentWithdrawalRate,
      final double deficientInvestmentContributionRate
      ) {
      this.householdTargetFundInvestmentValue = householdTargetFundInvestmentValue;
      this.excessInvestmentWithdrawalRate = excessInvestmentWithdrawalRate;
      this.deficientInvestmentContributionRate = deficientInvestmentContributionRate;
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(householdTargetFundInvestmentValue >= 0.,
         toParameterValidityErrMsg("Target Fund Investment Account Value is negative."));
      Preconditions.checkArgument(excessInvestmentWithdrawalRate >= 0.,
         toParameterValidityErrMsg("Excess Fund Investment Withdrawal Rate is negative."));
      Preconditions.checkArgument(deficientInvestmentContributionRate >= 0.,
         toParameterValidityErrMsg("Deficient Fund Investment Contribution Rate is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "TARGET_VALUE_FUND_CONTRIBUTION_RULE_WITHDRAWAL_THRESHOLD",
         householdTargetFundInvestmentValue,
         "TARGET_VALUE_FUND_CONTRIBUTION_RULE_EXCESS_INVESTMENT_WITHDRAWAL_RATE",
         excessInvestmentWithdrawalRate,
         "TARGET_VALUE_FUND_CONTRIBUTION_RULE_DEFICIENT_INVESTMENT_CONTRIBUTION_RATE",
         deficientInvestmentContributionRate
         ));
      bind(
         FundInvestmentDecisionRule.class)
         .to(SimpleThresholdFundInvestmentDecisionRule.class);
      expose(FundInvestmentDecisionRule.class);
   }
}
