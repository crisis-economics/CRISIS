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

import eu.crisis_economics.abm.firm.plugins.FirmDecisionRule;
import eu.crisis_economics.abm.firm.plugins.FixedWageBidPriceAlgorithm;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Fixed Bid Price"
   )
public final class FixedWageBidPriceAlgorithmConfiguration
   extends AbstractPrivateConfiguration
   implements FirmLabourWageAskPriceDecisionRuleConfiguration {
   
   private static final long serialVersionUID = 5370183653091684487L;
   
   public static final double
      DEFAULT_FIXED_WAGE_BID_PRICE = 6.75e7;
   
   @Parameter(
      ID = "FIXED_WAGE_BID_PRICE_ALGORITHM"
      )
   private double
      fixedLabourWageBidPrice = DEFAULT_FIXED_WAGE_BID_PRICE;
   
   public double getFixedLabourWageBidPrice() {
      return fixedLabourWageBidPrice;
   }
   
   public void setFixedLabourWageBidPrice(
      final double fixedLabourWageBidPrice) {
      this.fixedLabourWageBidPrice = fixedLabourWageBidPrice;
   }
   
   public FixedWageBidPriceAlgorithmConfiguration() { }
   
   public FixedWageBidPriceAlgorithmConfiguration(
      final double fixedWageBidPrice) {
      this.fixedLabourWageBidPrice = fixedWageBidPrice;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         fixedLabourWageBidPrice >= 0,
         toParameterValidityErrMsg("Labour Wage Bid Price is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FIXED_WAGE_BID_PRICE_ALGORITHM",
         fixedLabourWageBidPrice
         ));
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(FixedWageBidPriceAlgorithm.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
