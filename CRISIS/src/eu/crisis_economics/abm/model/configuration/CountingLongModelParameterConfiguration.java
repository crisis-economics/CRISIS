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
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration.AbstractLongModelParameterConfiguration;
import eu.crisis_economics.abm.model.parameters.LongCounter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Blocks"
   )
public final class CountingLongModelParameterConfiguration
   extends AbstractLongModelParameterConfiguration {
   
   private static final long serialVersionUID = -1015345635359365935L;
   
   private final static long
      DEFAULT_BLOCK_SIZE = 1;
   
   @Layout(
      Order = 0,
      FieldName = "Block Size"
      )
   private long
      blockSize = DEFAULT_BLOCK_SIZE;
   @Layout(
      Order = 0.1,
      FieldName = "Initial Value"
      )
   private long
      initialValue = 0L;
   
   public CountingLongModelParameterConfiguration() { }
   
   public CountingLongModelParameterConfiguration(
      final long blockSize,
      final long initialValue
      ) {
      this.blockSize = blockSize;
      this.initialValue = initialValue;
   }
   
   public long getBlockSize() {
      return blockSize;
   }
   
   /**
     * Set the block size (counting step width) for the {@link Long} {@link Parameter}
     * to be configured. The argument should be nonzero.
     */
   public void setBlockSize(
      final long blockSize
      ) {
      this.blockSize = blockSize;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(blockSize != 0,
         "Counting Long Model Parameter Configuration: block width "
       + "(counting step size) is zero.");
   }
   
   @Override
   protected void addBindings() {
      bind(
         new TypeLiteral<ModelParameter<Long>>(){})
         .annotatedWith(Names.named(getScopeString()))
         .to(LongCounter.class)
         .in(Singleton.class);
      bind(String.class)
         .annotatedWith(Names.named("LONG_COUNTER_MODEL_PARAMETER_NAME"))
         .toInstance(getScopeString());
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "LONG_COUNTER_MODEL_PARAMETER_BLOCK_SIZE",      blockSize,
         "LONG_COUNTER_MODEL_PARAMETER_INITIAL_VALUE",   initialValue
         ));
      expose(
         new TypeLiteral<ModelParameter<Long>>(){})
         .annotatedWith(Names.named(getScopeString()));
      super.addBindings();
   }
}
