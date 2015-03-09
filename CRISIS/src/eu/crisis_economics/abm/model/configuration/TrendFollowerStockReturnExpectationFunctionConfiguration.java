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

import eu.crisis_economics.abm.algorithms.portfolio.returns.StockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.TrendFollowerStockReturnExpectationFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Chartist Returns"
   )
public final class TrendFollowerStockReturnExpectationFunctionConfiguration 
   extends AbstractPrivateConfiguration
   implements StockReturnExpectationFunctionConfiguration 
   {
   
   private static final long serialVersionUID = 117892030041854909L;
   
   @Parameter(
      ID = "TREND_FOLLOWER_STOCK_RETURN_EXPECTATION_LAG"
      )
   private double
      lag = TrendFollowerStockReturnExpectationFunction.
         DEFAULT_TREND_FOLLOWER_STOCK_RETURN_EXPECTATION_LAG;
   
   public double getTrendFollowerLag() {
      return lag;
   }
   
   public void setTrendFollowerLag(final double value) {
      lag = value;
   }
   
   public String desTrendFollowerLag() {
      return "The lag to apply to trend-follower stock return expectation processes.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(lag >= 0., toParameterValidityErrMsg("Lag is negative."));
   }
   
   public TrendFollowerStockReturnExpectationFunctionConfiguration() { }
   
   public TrendFollowerStockReturnExpectationFunctionConfiguration(final int lag) {
      this.lag = lag;
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "TREND_FOLLOWER_STOCK_RETURN_EXPECTATION_LAG", lag
         ));
      bind(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(TrendFollowerStockReturnExpectationFunction.class);
      expose(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
