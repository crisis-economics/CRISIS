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
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;

public final class CustomFundInvestmentDecisionRule
   extends SimpleAbstactAgentOperation<Double> implements FundInvestmentDecisionRule {
   
   private TimeseriesParameter<Double>
      signedContributionDecisionRule;
   
   public CustomFundInvestmentDecisionRule(
   @Named("REMOTE_CONTROL_FUND_INVESTMENT_ACCOUNT_DECISION_RULE")
      final TimeseriesParameter<Double> signedContributionDecisionRule) {
      this.signedContributionDecisionRule =
         Preconditions.checkNotNull(signedContributionDecisionRule);
   }
   
   @Override
   public Double operateOn(final Agent agent) {
      if(!(agent instanceof FundInvestor))
         return 0.;
      return signedContributionDecisionRule.get();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Custom Fund Investment Decision Rule";
   }
}
