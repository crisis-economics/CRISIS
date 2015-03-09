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

import eu.crisis_economics.abm.algorithms.portfolio.returns.FundamentalistStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.StockReturnExpectationFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Fundamentalist Returns"
   )
public final class FundamentalistStockReturnExpectationFunctionConfiguration 
   extends AbstractPrivateConfiguration
   implements StockReturnExpectationFunctionConfiguration {
   
   private static final long serialVersionUID = -7355304947542379158L;
   
   @Parameter(
      ID = "FUNDAMENTALIST_STOCK_RETURN_EXPECTATION_RISK_PREMIUM"
      )
   private double
      fundamentalistStockReturnRiskPremium;
   
   public double getFundamentalistStockReturnRiskPremium() {
      return fundamentalistStockReturnRiskPremium;
   }
   
   public void setFundamentalistStockReturnRiskPremium(final double value) {
      fundamentalistStockReturnRiskPremium = value;
   }
   
   public String desFundamentalistStockReturnRiskPremium() {
      return
          "A divisor (the 'risk premium') to apply to the dividend per share per unit stock price "
        + "measurement employed by the fundamentalist stock value calculation. For a risk premium "
        + "of 1.0, the return rate estimate of a stock with price p is d/p where d is the most"
        + "recent dividend per share. For a risk premium of 2.0 the return rate is half this value,"
        + " and so on.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(fundamentalistStockReturnRiskPremium >= 0.);
   }
   
   public FundamentalistStockReturnExpectationFunctionConfiguration() {
      this(FundamentalistStockReturnExpectationFunction
         .DEFAULT_FUNDAMENTALIST_STOCK_RETURN_EXPECTATION_RISK_PREMIUM);
   }
   
   public FundamentalistStockReturnExpectationFunctionConfiguration(
      final double stockReturnPremium) {
      this.fundamentalistStockReturnRiskPremium = stockReturnPremium;
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FUNDAMENTALIST_STOCK_RETURN_EXPECTATION_RISK_PREMIUM",
         fundamentalistStockReturnRiskPremium
         ));
      bind(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(FundamentalistStockReturnExpectationFunction.class);
      expose(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
