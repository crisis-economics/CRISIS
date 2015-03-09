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

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;
import eu.crisis_economics.abm.fund.CustomFundInvestmentDecisionRule;
import eu.crisis_economics.abm.fund.FundInvestmentDecisionRule;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;

public final class CustomFundInvestmentDecisionRuleConfiguration
   extends AbstractFundInvestmentDecisionRuleConfiguration {
   
   private static final long serialVersionUID = 5128201025364158637L;
   
   @Layout(
      Order = 0,
      FieldName = "Fund Investment Decisions"
      )
   @Submodel
   @Parameter(
      ID = "REMOTE_CONTROL_FUND_INVESTMENT_ACCOUNT_DECISION_RULE"
      )
   private TimeseriesDoubleModelParameterConfiguration
      fundInvestmentDecisions = new ConstantDoubleModelParameterConfiguration(1.e7);
   
   public final TimeseriesDoubleModelParameterConfiguration
      getFundInvestmentDecisions() {
      return fundInvestmentDecisions;
   }
   
   public final void setFundInvestmentDecisions(
      final TimeseriesDoubleModelParameterConfiguration value) {
      fundInvestmentDecisions = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getLabourToOfferPerHousehold}.
     */
   public final String desFundInvestmentDecisions() {
      return "A timeseries specifyin the fund investment account contribution, or"
           + " withdrawal, decision to be made by an agent at all future times.";
   }
   
   @Override
   protected void addBindings() {
      bind(FundInvestmentDecisionRule.class).to(CustomFundInvestmentDecisionRule.class);
   }
}
