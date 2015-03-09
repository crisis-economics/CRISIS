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

import eu.crisis_economics.abm.firm.io.ChainedInputOutputMatrixFactory;
import eu.crisis_economics.abm.firm.io.InputOutputMatrixFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link ChainedInputOutputMatrixFactoryConfiguration}
  * class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Cyclical Network"
   )
public final class ChainedInputOutputMatrixFactoryConfiguration
   extends AbstractInputOutputMatrixFactoryConfiguration
   implements InputOutputMatrixFactoryConfiguration {
   
   private static final long serialVersionUID = -1451936353215428138L;
   
   public final static int
      DEFAULT_CHAIN_LENGTH = 3;
   
   @Parameter(
      ID = "CHAINED_INPUT_OUTPUT_MATRIX_FACTORY_CHAIN_LENGTH"
      )
   private int
      chainLength = DEFAULT_CHAIN_LENGTH;
   
   public int getChainLength() {
      return chainLength;
   }
   
   /**
     * Set the chain length for {@link ChainedInputOutputMatrixFactory}
     * objects configured with this component. The argument must be strictly
     * positive.
     */
   public void setChainLength(
      final int chainLength) {
      this.chainLength = chainLength;
   }
   
   public ChainedInputOutputMatrixFactoryConfiguration() { }
   
   /**
     * Create a {@link ChainedInputOutputMatrixFactoryConfiguration} with custom
     * parameters.
     * 
     * @param chainLength
     *        An integer used to configure the chain length of a
     *        {@link ChainedInputOutputMatrixFactory} object. This argument
     *        should be strictly positive.
     */
   public ChainedInputOutputMatrixFactoryConfiguration(
      final int chainLength) { 
      this.chainLength = chainLength;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(chainLength > 0);
   }
   
   @Override
   protected void addBindings() {
      install(
         InjectionFactoryUtils.asPrimitiveParameterModule(
            "CHAINED_INPUT_OUTPUT_MATRIX_FACTORY_CHAIN_LENGTH", chainLength));
      bind(InputOutputMatrixFactory.class)
         .to(ChainedInputOutputMatrixFactory.class);
      super.addBindings();
   }
}
