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
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.plugins.FirmProductionFunction;
import eu.crisis_economics.abm.firm.plugins.LabourOnlyProductionFunction;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.FirmProductionFunctionFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A {@link ConfigurationComponent} for the {@link LabourOnlyProductionFunction}
  * {@link FirmProductionFunction} algorithm.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Labour Only",
   Description = 
      "A firm production function algorithm which uses labour resources only " + 
      "for production. This production function does not require input goods for" + 
      "production.\n" + 
      "\n" + 
      "This production function has the form:\n" + 
      "\n" + 
      "<center><code>" + 
      "P = Z(t) * L^Q" + 
      "</code></center>\n\n" + 
      "" + 
      "Where <code>P</code> is total production (yield), <code>Z(t)</code> is the total" + 
      "factor productivity (TFP factor, as a functio of time t), <code>L</code> is the labour" +
      " input for production and, <code>Q</code> is a customizable labour productivity exponent."
   )
public final class LabourOnlyProductionFunctionConfiguration
   extends AbstractPrivateConfiguration
   implements FirmProductionFunctionConfiguration {
   
   private static final long serialVersionUID = 6481522935333824790L;
   
   public final static double
      DEFAULT_TFP_FACTOR = 1.2,
      DEFAULT_LABOUR_PRODUCTIVITY_EXPONENT = 1.0;
   
   @Layout(
      Order = 1.0,
      FieldName = "Z"
      )
   @Submodel
   @Parameter(
      ID = "LABOUR_ONLY_PRODUCTION_FUNCTION_TFP"
      )
   private TimeseriesDoubleModelParameterConfiguration
      labourOnlyProductionFunctionTFPFactor;
   @Layout(
      Order = 1.1,
      FieldName = "Q"
      )
   @Parameter(
      ID = "LABOUR_ONLY_PRODUCTION_FUNCTION_EXPONENT"
      )
   private double
      labourOnlyProductionFunctionProductivityExponent = DEFAULT_LABOUR_PRODUCTIVITY_EXPONENT;
   
   public TimeseriesDoubleModelParameterConfiguration getLabourOnlyProductionFunctionTFPFactor() {
      return labourOnlyProductionFunctionTFPFactor;
   }
   
   /**
     * Set the Total Factor Productivity (TFP Factor, Z) for production functions 
     * configured by this component. The argument must be non-negative.
     */
   public void setLabourOnlyProductionFunctionTFPFactor(
      final TimeseriesDoubleModelParameterConfiguration value) {
      this.labourOnlyProductionFunctionTFPFactor = value;
   }

   public double getLabourOnlyProductionFunctionProductivityExponent() {
      return labourOnlyProductionFunctionProductivityExponent;
   }
   
   /**
     * Set the productivity exponent for production functions configured by this component.
     * The argument must be non-negative.
     */
   public void setLabourOnlyProductionFunctionProductivityExponent(
      final double value) {
      this.labourOnlyProductionFunctionProductivityExponent = value;
   }

   public final String desTotalFactorProductivity() {
      return
         "The firm Total Factor Productivity (the TFP Factor, Z). TFP is used as a linear "
       + "multiplier for firm production yields. For example: increasing the TFP factor "
       + " from 1.0 to 2.0 will allow firms to produce twice at much with the exact same "
       + "amount of labour and input goods.";
   }
   
   /**
     * Create a {@link LabourOnlyProductionFunctionConfiguration} object with default
     * parameters.
     */
   public LabourOnlyProductionFunctionConfiguration() {
      this(new ConstantDoubleModelParameterConfiguration(DEFAULT_TFP_FACTOR));
   }
   
   /**
     * Create a {@link LabourOnlyProductionFunctionConfiguration} object with custom
     * parameters.
     * 
     * @param tfp <br>
     *        A configurator for the Total Factor Productivity (TFP) factor to be used
     *        by instances of {@link LabourOnlyProductionFunction} configured by this
     *        component. See also {@link LabourOnlyProductionFunction}.
     */
   public LabourOnlyProductionFunctionConfiguration(
      final TimeseriesDoubleModelParameterConfiguration tfp
      ) {
      this.labourOnlyProductionFunctionTFPFactor = Preconditions.checkNotNull(tfp);
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         labourOnlyProductionFunctionProductivityExponent >= 0.,
         toParameterValidityErrMsg("Labour productivity exponent is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(
         InjectionFactoryUtils.asPrimitiveParameterModule(
            "LABOUR_ONLY_PRODUCTION_FUNCTION_EXPONENT",
            labourOnlyProductionFunctionProductivityExponent
            ));
      install(new FactoryModuleBuilder()
         .implement(FirmProductionFunction.class, LabourOnlyProductionFunction.class)
         .build(Key.get(FirmProductionFunctionFactory.class, Names.named(getScopeString())))
          );
      expose(Key.get(FirmProductionFunctionFactory.class, Names.named(getScopeString())));
   }
}
