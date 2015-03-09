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

import eu.crisis_economics.abm.algorithms.portfolio.returns.MedianOverHistoryStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.returns.StockReturnExpectationFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Moving Median"
   )
public final class MovingMedianStockReturnExpectationFunctionConfiguration 
   extends AbstractPrivateConfiguration
   implements StockReturnExpectationFunctionConfiguration {
   
   private static final long serialVersionUID = -6234087665222897119L;
   
   @Parameter(
      ID = "MEDIAN_OVER_HISTORY_STOCK_RETURN_EXPECTATION_FUNCTION_MEMORY_LENGTH"
      )
   private int
      movingMedianMemoryLength = MedianOverHistoryStockReturnExpectationFunction
         .DEFAULT_MEMORY_LENGTH_SLOTS;
   
   public int getMovingMedianMemoryLength() {
      return movingMedianMemoryLength;
   }
   
   /**
     * Set the moving median memory length. This argument should be strictly positive.
     */
   public void setMovingMedianMemoryLength(final int value) {
      movingMedianMemoryLength = value;
   }
   
   public String desMovingMedianMemoryLength() {
      return "The moving median memory length for this stock return expectation process.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(movingMedianMemoryLength > 0,
         toParameterValidityErrMsg("Memory Length is negative."));
   }
   
   @Submodel
   @Parameter(
      ID = "MEDIAN_OVER_HISTORY_PRIMITIVE_STOCK_RETURN_EXPECTATION_FUNCTION"
      )
   private StockReturnExpectationFunctionConfiguration
      underlyingStockReturnExpectationFunction = 
         new FundamentalistStockReturnExpectationFunctionConfiguration();
   
   public StockReturnExpectationFunctionConfiguration
      getUnderlyingStockReturnExpectationFunction() {
      return underlyingStockReturnExpectationFunction;
   }
   
   public void setUnderlyingStockReturnExpectationFunction(
      final StockReturnExpectationFunctionConfiguration underlyingStockReturnExpectationFunction) {
      this.underlyingStockReturnExpectationFunction = underlyingStockReturnExpectationFunction;
   }
   
   /**
     * Create a {@link MovingMedianStockReturnExpectationFunctionConfiguration} object
     * with default parameters. By default this constructor creates a configuration for
     * a {@link MovingMedianStockReturnExpectationFunctionConfiguration} whose delegate
     * {@link StockReturnExpectationFunctionConfiguration} is a fundamentalist stock 
     * return measurement.
     */
   public MovingMedianStockReturnExpectationFunctionConfiguration() { }
   
   /**
     * Create a {@link MovingMedianStockReturnExpectationFunctionConfiguration} with custom
     * parameters. This constructor creates a configurator with a custom 
     * confiurator for the underlying {@link StockReturnExpectationFunction}.
     */
   public MovingMedianStockReturnExpectationFunctionConfiguration(
      final StockReturnExpectationFunctionConfiguration delegate
      ) {
      this.underlyingStockReturnExpectationFunction = Preconditions.checkNotNull(delegate);
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "MEDIAN_OVER_HISTORY_STOCK_RETURN_EXPECTATION_FUNCTION_MEMORY_LENGTH", 
            movingMedianMemoryLength
         ));
      bind(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(MedianOverHistoryStockReturnExpectationFunction.class);
      expose(
         StockReturnExpectationFunction.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
