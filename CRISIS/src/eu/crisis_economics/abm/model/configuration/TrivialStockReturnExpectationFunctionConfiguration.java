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
import eu.crisis_economics.abm.algorithms.portfolio.returns.TrivialStockReturnExpectationFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A simple model configuration component for the {@link TrivialStockReturnExpectationFunction}
  * class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Manual"
   )
public final class TrivialStockReturnExpectationFunctionConfiguration 
   extends AbstractPrivateConfiguration
   implements StockReturnExpectationFunctionConfiguration {
   
   private static final long serialVersionUID = -4839981317772477882L;
   
   @Parameter(
      ID = "TRIVIAL_STOCK_RETURN_EXPECTATION_FUNCTION_FIXED_RETURN_VALUE"
      )
   private double
      fixedStockReturnRate = TrivialStockReturnExpectationFunction.DEFAULT_FIXED_RETURN_RATE;
   
   public double getFixedStockReturnRate() {
      return fixedStockReturnRate;
   }
   
   /**
     * Set the fixed stock return rate estimate for instances of 
     * {@link TrivialStockReturnExpectationFunctionConfiguration} 
     * created by this configuration component. The argument must
     * be non-negative.
     */
   public void setFixedStockReturnRate(
      final double fixedStockReturnRate) {
      this.fixedStockReturnRate = fixedStockReturnRate;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(fixedStockReturnRate >= 0.);
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "TRIVIAL_STOCK_RETURN_EXPECTATION_FUNCTION_FIXED_RETURN_VALUE", fixedStockReturnRate
         ));
      bind(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(TrivialStockReturnExpectationFunction.class);
      expose(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
