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

import eu.crisis_economics.abm.fund.FundInvestmentDecisionRule;

/**
  * An abstract configuration base class for {@link Fund} investment account
  * decision rules.
  * 
  * @author phillips
  */
public abstract class AbstractFundInvestmentDecisionRuleConfiguration
   extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = -4847611204012885574L;
   
   /**
     * Create a {@link AbstractPrivateConfiguration} object. Implementations
     * of this base class must provide the following bindings:
     * 
     * <ul>
     *   <li> An implementation of {@link FundInvestmentDecisionRule}{@code .class}.
     * </ul>
     */
   protected AbstractFundInvestmentDecisionRuleConfiguration() { }
   
   @Override
   protected final void setRequiredBindings() {
      requireBinding(FundInvestmentDecisionRule.class);
   }
}
