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

import com.google.common.base.Preconditions;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.returns.ExponentialSmoothingStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.StockReturnExpectationFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Exponential Smoothing"
   )
public final class ExponentialSmoothingStockReturnExpectationFunctionConfiguration 
   extends AbstractPrivateConfiguration
   implements StockReturnExpectationFunctionConfiguration {
   
   @Parameter(
      ID = "EXPONENTIAL_SMOOTHING_STOCK_RETURN_EXPECTATION_FUNCTION_ALPHA"
      )
   @Layout(
      Order = 0.0,
      VerboseDescription = "a"
      )
   private double
      alpha = 0.9;
   
   public double getAlpha() {
      return alpha;
   }
   
   public void setAlpha(
      final double alpha) {
      this.alpha = alpha;
   }
   
   @Submodel({
      FundamentalistStockReturnExpectationFunctionConfiguration.class, 
      TrendFollowerStockReturnExpectationFunctionConfiguration.class
      })
   @Parameter(
      ID = "EXPONENTIAL_SMOOTHING_STOCK_RETURN_FUNCTION_DELEGATE"
      )
   @Layout(
      Order = 0.0,
      VerboseDescription = "r(t)"
      )
   private StockReturnExpectationFunctionConfiguration
      delegateStockReturnExpectationFunction = 
         new FundamentalistStockReturnExpectationFunctionConfiguration();
   
   public StockReturnExpectationFunctionConfiguration getDelegateStockReturnExpectationFunction() {
      return delegateStockReturnExpectationFunction;
   }
   
   public void setDelegateStockReturnExpectationFunction(
      final StockReturnExpectationFunctionConfiguration value) {
      this.delegateStockReturnExpectationFunction = Preconditions.checkNotNull(value);
   }
   
   /**
     * Create an {@link ExponentialSmoothingStockReturnExpectationFunctionConfiguration}
     * object with default parameters. By default, this constructor will create a
     * configurator which delegates to a Fundamentalist stock return expectation function
     * and whose exponential smoothing parameter has value <code>0.9</code>.
     */
   public ExponentialSmoothingStockReturnExpectationFunctionConfiguration() { }
   
   /**
     * Create an {@link ExponentialSmoothingStockReturnExpectationFunctionConfiguration}
     * object with custom parameters.<br><br>
     * 
     * The arguments to this method are configurators for arguments to the class
     * {@link ExponentialSmoothingStockReturnExpectationFunction}. See also
     * {@link ExponentialSmoothingStockReturnExpectationFunction#
     * ExponentialSmoothingStockReturnExpectationFunction(StockReturnExpectationFunction, double)}.
     * 
     * @param alpha (<code>a</code>) <br>
     *        The exponential smoothing parameter for algorithms configured by this
     *        component.
     * @param delegate (<code>r(t)</code>) <br>
     *        The delegate {@link StockReturnExpectationFunction} to be used by
     *        algorithms configured by this component.
     *        
     */
   public ExponentialSmoothingStockReturnExpectationFunctionConfiguration(
      final double alpha,
      final StockReturnExpectationFunctionConfiguration delegate
      ) {
      this.alpha = alpha;
      this.delegateStockReturnExpectationFunction = Preconditions.checkNotNull(delegate);
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "EXPONENTIAL_SMOOTHING_STOCK_RETURN_EXPECTATION_FUNCTION_ALPHA", alpha
         ));
      bind(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(ExponentialSmoothingStockReturnExpectationFunction.class);
      expose(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()));
   }
   
   private static final long serialVersionUID = 2830054020079676272L;
}
