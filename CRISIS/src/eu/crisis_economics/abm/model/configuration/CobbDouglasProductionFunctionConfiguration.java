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

import java.util.Map;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.io.InputOutputMatrix;
import eu.crisis_economics.abm.firm.io.InputOutputMatrixFactory;
import eu.crisis_economics.abm.firm.plugins.CobbDouglasProductionFunction;
import eu.crisis_economics.abm.firm.plugins.FirmProductionFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.FirmProductionFunctionFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.abm.simulation.injection.factories.TechnologicalWeightsFirmProductionFunctionFactory;

@ConfigurationComponent(
   DisplayName = "Cobb Douglas"
   )
public final class CobbDouglasProductionFunctionConfiguration
   extends AbstractPrivateConfiguration
   implements FirmProductionFunctionConfiguration {
   
   private static final long serialVersionUID = -7736761737506974678L;
   
   public final static double
      IO_COBB_DOUGLAS_PRODUCTION_FUNCTION_ALPHA = .7,
      IO_COBB_DOUGLAS_PRODUCTION_FUNCTION_TOTAL_FACTOR_PRODUCTIVITY =
         Math.pow(21., IO_COBB_DOUGLAS_PRODUCTION_FUNCTION_ALPHA);
      
   @Layout(
      Order = 0,
      FieldName = "Total Factor Productivity"
      )
   @Submodel
   @Parameter(
      ID = "COBB_DOUGLAS_PRODUCTION_FUNCTION_TOTAL_FACTOR_PRODUCTIVITY"
      )
   private TimeseriesDoubleModelParameterConfiguration
      cobbDouglasTotalFactorProductivity;
   @Layout(
      Order = 1,
      FieldName = "Alpha"
      )
   @Parameter(
      ID = "COBB_DOUGLAS_PRODUCTION_FUNCTION_ALPHA"
      )
   private double
      alpha;
   
   public TimeseriesDoubleModelParameterConfiguration getCobbDouglasTotalFactorProductivity() {
      return cobbDouglasTotalFactorProductivity;
   }
   
   public void setCobbDouglasTotalFactorProductivity(
      final TimeseriesDoubleModelParameterConfiguration value) {
      this.cobbDouglasTotalFactorProductivity = value;
   }
   
   public final String desCobbDouglasTotalFactorProductivity() {
      return
         "The firm Total Factor Productivity (the TFP Factor, Z). TFP is used as a linear "
       + "multiplier for firm production yields. For example: increasing the TFP factor "
       + " from 1.0 to 2.0 will allow firms to produce twice at much with the exact same "
       + "amount of labour and input goods.";
   }
   
   public double getAlpha() {
      return alpha;
   }
   
   public void setAlpha(
      final double alpha) {
      this.alpha = alpha;
   }
   
   /**
     * Create a {@link CobbDouglasProductionFunctionConfiguration} object with default
     * parameters.
     */
   public CobbDouglasProductionFunctionConfiguration() {
      this(IO_COBB_DOUGLAS_PRODUCTION_FUNCTION_TOTAL_FACTOR_PRODUCTIVITY);
   }
   
   /**
     * Create a {@link CobbDouglasProductionFunctionConfiguration} object with custom
     * parameters.<br><br>
     * 
     * See also {@link #CobbDouglasProductionFunctionConfiguration(double, double)}.
     * 
     * @param fixedTFPValue (<code>TFP</code>)<br>
     *        A fixed value for the Cobb-Douglas Total Factor Productivity.
     */
   public CobbDouglasProductionFunctionConfiguration(
      final double fixedTFPValue
      ) {
      this(fixedTFPValue, IO_COBB_DOUGLAS_PRODUCTION_FUNCTION_ALPHA);
   }
   
   /**
     * Create a {@link CobbDouglasProductionFunctionConfiguration} object with custom
     * parameters.<br><br>
     * 
     * See also {@link #CobbDouglasProductionFunctionConfiguration(double)}.
     * 
     * @param alpha (<code>A</code>)<br>
     *        The alpha exponent of the Cobb-Douglas production function. See also
     *        {@link CobbDouglasProductionFunction}.
     */
   public CobbDouglasProductionFunctionConfiguration(
      final double fixedTFPValue,
      final double alpha
      ) {
      this.cobbDouglasTotalFactorProductivity =
         new ConstantDoubleModelParameterConfiguration(fixedTFPValue);
      this.alpha = alpha;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         alpha >= 0.,
         toParameterValidityErrMsg("Alpha factor is negative."));
   }
   
   private static class IONetworkFirmProductionFunctionFactory
      implements FirmProductionFunctionFactory {
      
      private @Inject InputOutputMatrixFactory
         inputOutputMatrixFactory;
      private @Inject TechnologicalWeightsFirmProductionFunctionFactory
         factory;
      
      @Override
      public FirmProductionFunction create(final String goodsType) {
         final InputOutputMatrix
            matrix = inputOutputMatrixFactory.createInputOutputMatrix();
         final Map<String, Double>
            technologicalWeights = matrix.getColumnWithNames(
               matrix.getIndexByName(goodsType));
         return create(technologicalWeights);
      }
      
      private FirmProductionFunction create(
         final Map<String, Double> technologicalWeights) {
         return factory.create(technologicalWeights);
      }
   }
   
   @Override
   protected void addBindings() {
      install(
         InjectionFactoryUtils.asPrimitiveParameterModule(
            "COBB_DOUGLAS_PRODUCTION_FUNCTION_ALPHA", alpha
            ));
      install(new FactoryModuleBuilder()
         .implement(FirmProductionFunction.class, CobbDouglasProductionFunction.class)
         .build(TechnologicalWeightsFirmProductionFunctionFactory.class)
         );
      bind(
         FirmProductionFunctionFactory.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(IONetworkFirmProductionFunctionFactory.class);
      expose(
         FirmProductionFunctionFactory.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
