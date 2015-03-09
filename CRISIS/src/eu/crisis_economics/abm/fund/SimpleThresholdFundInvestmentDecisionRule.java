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
package eu.crisis_economics.abm.fund;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;

/**
  * A simple decision rule for {@link Fund} investments. This decision operates as
  * follows:
  * 
  * <ul>
  *   <li> a target fund investment threshold, {@code T}, is specified;
  *   <li> a {@link Fund} investment rate, {@code I}, is specified;
  *   <li> a {@link Fund} withdrawal rate, {@code W}, is specified;
  *   <li> if the value of {@link Fund} investments is greater than {@code T},
  *        the decision made by this rule is to withdraw the difference at a rate 
  *        {@code I*}. The exact amount withdrawn is:<br><br>
  *        <center><code>W * ((account value) - T)</code></center><br>
  *   <li> otherwise, if the value of {@link Fund} investments is less than {@code T}, 
  *        invest:<br><br>
  *        <center><code>max(T - (account value), I * (unallocated cash))</code></center><br>
  *        into {@link Fund}{@code s}.
  * </ul>
  */
public final class SimpleThresholdFundInvestmentDecisionRule
   extends SimpleAbstactAgentOperation<Double> implements FundInvestmentDecisionRule {
   
   private double
      fundWithdrawalThreshold,
      proportionOfUnalloctaedDepositsToInvest,
      proportionOfExcessInvestmentValueToWithdraw;
   
   /**
     * Create a {@link SimpleThresholdFundInvestmentDecisionRule} object.
     * 
     * @param fundWithdrawalThreshold
     *        The target value of fund investments
     * @param proportionOfExcessInvestmentValueToWithdraw
     *        The rate at which to withdraw excess fund investments (over and above
     *        the investment target threshold)
     * @param proportionOfUnalloctaedDepositsToInvest
     *        The rate at which to invest unallocated deposits into funds accounts
     */
   @Inject
   public SimpleThresholdFundInvestmentDecisionRule(
   @Named("TARGET_VALUE_FUND_CONTRIBUTION_RULE_WITHDRAWAL_THRESHOLD")
      final double fundWithdrawalThreshold,
   @Named("TARGET_VALUE_FUND_CONTRIBUTION_RULE_EXCESS_INVESTMENT_WITHDRAWAL_RATE")
      final double proportionOfExcessInvestmentValueToWithdraw,
   @Named("TARGET_VALUE_FUND_CONTRIBUTION_RULE_DEFICIENT_INVESTMENT_CONTRIBUTION_RATE")
      final double proportionOfUnalloctaedDepositsToInvest
      ) {
      Preconditions.checkArgument(fundWithdrawalThreshold >= 0.);
      Preconditions.checkArgument(proportionOfUnalloctaedDepositsToInvest >= 0.);
      Preconditions.checkArgument(proportionOfUnalloctaedDepositsToInvest <= 1.);
      Preconditions.checkArgument(proportionOfExcessInvestmentValueToWithdraw >= 0.);
      this.fundWithdrawalThreshold =
         fundWithdrawalThreshold;
      this.proportionOfUnalloctaedDepositsToInvest =
         proportionOfUnalloctaedDepositsToInvest;
      this.proportionOfExcessInvestmentValueToWithdraw =
         proportionOfExcessInvestmentValueToWithdraw;
   }
   
   @Override
   public Double operateOn(final Agent agent) {
      if(!(agent instanceof FundInvestor))
         return 0.;
      final FundInvestor
         investor = (FundInvestor) agent;
      final double
         fundAccountValue = investor.getFund().getBalance(investor);
      double result = 0.;
      if(investor.getFund().getBalance(investor) > fundWithdrawalThreshold)
         result = -(fundAccountValue - fundWithdrawalThreshold) 
            * proportionOfExcessInvestmentValueToWithdraw;
      else
         result = +Math.max(0.,
            Math.min(
               investor.getUnallocatedCash() * proportionOfUnalloctaedDepositsToInvest, 
               fundWithdrawalThreshold - fundAccountValue
               )
            );
      return result;
   }
   
   /**
     * Get the fund withdrawal threshold.
     */
   public double getFundWithdrawalThreshold() {
      return fundWithdrawalThreshold;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Simple Threshold Fund Contribution Algorithm, fund withdrawal threshold: "
           + getFundWithdrawalThreshold() + ".";
   }
}
