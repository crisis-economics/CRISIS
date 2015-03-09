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
import com.google.inject.Key;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.returns.LinearCombinationStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.StockReturnExpectationFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;

@ConfigurationComponent(
   DisplayName = "Linear Composite",
   Description =
      "A composite stock return expectation function. This stock return function "
    + "computes the return rate <code>r(t)</code> on a stock through linearly combining the "
    + "return rate estimates <code>d1(t)</code>, <code>d2(t)</code> of two delegate "
    + "stock return expectation functions <code>d1</code> and <code>d2</code>. The "
    + "return rate estimate is\n\n"
    + "<center><code>r(t) = w(t) * d1(t) + (1 - w(t)) * d2(t)</code></center>\n\n"
    + "where w(t) is a combination weight varying in time."
   )
public final class LinearCombinationStockReturnExpectationFunctionConfiguration 
   extends AbstractPrivateConfiguration
   implements StockReturnExpectationFunctionConfiguration {
   
   @Submodel
   @Parameter(
      ID = "LINEAR_COMBINATION_STOCK_RETURN_EXPECTATION_FUNCTION_FIRST_IMPLEMENTATION"
      )
   @Layout(
      Title = "Delegates",
      Order = 0.0,
      FieldName = "First Delegate"
      )
   private StockReturnExpectationFunctionConfiguration
      firstDelegate = new FundamentalistStockReturnExpectationFunctionConfiguration();
   
   @Submodel
   @Parameter(
      ID = "LINEAR_COMBINATION_STOCK_RETURN_EXPECTATION_FUNCTION_SECOND_IMPLEMENTATION"
      )
   @Layout(
      Order = 0.1,
      FieldName = "Second Delegate"
      )
   private StockReturnExpectationFunctionConfiguration
      secondDelegate = new TrendFollowerStockReturnExpectationFunctionConfiguration();
   
   @Submodel
   @Parameter(
      ID = "LINEAR_COMBINATION_STOCK_RETURN_EXPECTATION_FUNCTION_WEIGHT"
      )
   @Layout(
      Order = 0.2,
      Title = "Mixing Weight",
      FieldName = "w(t)"
      )
   private TimeseriesDoubleModelParameterConfiguration
      weight = new ConstantDoubleModelParameterConfiguration(1.0);
   
   public StockReturnExpectationFunctionConfiguration getFirstDelegate() {
      return firstDelegate;
   }
   
   public void setFirstDelegate(
      final StockReturnExpectationFunctionConfiguration firstDelegate) {
      this.firstDelegate = firstDelegate;
   }
   
   public StockReturnExpectationFunctionConfiguration getSecondDelegate() {
      return secondDelegate;
   }
   
   public void setSecondDelegate(
      final StockReturnExpectationFunctionConfiguration secondDelegate) {
      this.secondDelegate = secondDelegate;
   }
   
   public TimeseriesDoubleModelParameterConfiguration getWeight() {
      return weight;
   }
   
   public void setWeight(
      final TimeseriesDoubleModelParameterConfiguration weight) {
      this.weight = weight;
   }
   
   /**
     * Create a {@link LinearCombinationStockReturnExpectationFunctionConfiguration} object
     * with default parameters. By default, this constructor will create a configurator
     * whose delegate stock return expectation functions are (a) a Fundamentalist 
     * stock return expectation function with default configuration, and (b) a Chartist
     * stock return expectation function with default configuration. The mixing weight
     * is taken to be w(t) = 0.5.
     */
   public LinearCombinationStockReturnExpectationFunctionConfiguration() { }
   
   /**
     * Create a {@link LinearCombinationStockReturnExpectationFunctionConfiguration} object
     * with custom parameters. The arguments to this method are configurators for the 
     * arguments to {@link LinearCombinationStockReturnExpectationFunction}. See also
     * {@link LinearCombinationStockReturnExpectationFunction#
     * LinearCombinationStockReturnExpectationFunction(StockReturnExpectationFunction,
     * StockReturnExpectationFunction, TimeseriesParameter)}.
     * 
     * @param firstDelegate <br>
     *        A configurator for the first {@link StockReturnExpectationFunctionConfiguration}
     *        to which the {@link LinearCombinationStockReturnExpectationFunction} should
     *        delegate.
     * @param secondDelegate <br>
     *        A configurator for the second {@link StockReturnExpectationFunctionConfiguration}
     *        to which the {@link LinearCombinationStockReturnExpectationFunction} should
     *        delegate.
     * @param mixingWeight <code>w(t)</code> <br>
     *        A configurator for the (time-dependent) delegate mixing weight <code>w(t)</code>.
     */
   public LinearCombinationStockReturnExpectationFunctionConfiguration(
      final StockReturnExpectationFunctionConfiguration firstDelegate,
      final StockReturnExpectationFunctionConfiguration secondDelegate,
      final TimeseriesDoubleModelParameterConfiguration mixingWeight
      ) {
      this.firstDelegate = Preconditions.checkNotNull(firstDelegate);
      this.secondDelegate = Preconditions.checkNotNull(secondDelegate);
      this.weight = Preconditions.checkNotNull(mixingWeight);
   }
   
   @Override
   protected void addBindings() {
      bind(
         Key.get(StockReturnExpectationFunction.class, Names.named(getScopeString())))
         .to(LinearCombinationStockReturnExpectationFunction.class);
      expose(
         Key.get(StockReturnExpectationFunction.class, Names.named(getScopeString())));
   }
   
   private static final long serialVersionUID = -3160981435035915474L;
}
