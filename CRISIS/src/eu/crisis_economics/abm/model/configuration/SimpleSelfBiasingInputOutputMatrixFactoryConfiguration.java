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

import eu.crisis_economics.abm.firm.io.InputOutputMatrixFactory;
import eu.crisis_economics.abm.firm.io.SimpleSelfBiasingInputOutputMatrixFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link SimpleSelfBiasingInputOutputMatrixFactory}
  * class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Self-Biasing Network"
   )
public final class SimpleSelfBiasingInputOutputMatrixFactoryConfiguration
   extends AbstractInputOutputMatrixFactoryConfiguration
   implements InputOutputMatrixFactoryConfiguration {
   
   private static final long serialVersionUID = -8807371640275058402L;
   
   public final static double
      DEFAULT_SELF_BIAS = 2.;
   
   @Parameter(
      ID = "SIMPLE_SELF_BIASING_INPUT_OUTPUT_MATRIX_FACTORY_BIAS_FACTOR"
      )
   private double
      selfBias = DEFAULT_SELF_BIAS;
   
   public double getSelfBias() {
      return selfBias;
   }
   
   /**
     * Set the self-bias term for instances of {@link SimpleSelfBiasingInputOutputMatrixFactory}
     * objects created with this configuration component. The argument must be non-negative.
     */
   public void setSelfBias(
      double selfBias) {
      this.selfBias = selfBias;
   }

   public SimpleSelfBiasingInputOutputMatrixFactoryConfiguration() { }
   
   /**
     * Create a {@link SimpleSelfBiasingInputOutputMatrixFactoryConfiguration} with custom
     * parameters.
     * 
     * @param selfBias
     *        An double used to configure the self-bias factor for intances of
     *        {@link SimpleSelfBiasingInputOutputMatrixFactory} created with 
     *        this configuration component.
     */
   public SimpleSelfBiasingInputOutputMatrixFactoryConfiguration(
      final double selfBias) { 
      this.selfBias = selfBias;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(selfBias >= 0);
   }
   
   @Override
   protected void addBindings() {
      install(
         InjectionFactoryUtils.asPrimitiveParameterModule(
            "SIMPLE_SELF_BIASING_INPUT_OUTPUT_MATRIX_FACTORY_BIAS_FACTOR", selfBias));
      bind(InputOutputMatrixFactory.class)
         .to(SimpleSelfBiasingInputOutputMatrixFactory.class);
      super.addBindings();
   }
}
