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
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

@ConfigurationComponent(
   DisplayName = "Sampled"
   )
public final class SampledIntegerModelParameterConfiguration
   extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = -3041251308847122366L;
   
   @Submodel(
      {CountingIntegerModelParameterConfiguration.class,
       FromFileSampledIntegerModelParameterConfiguration.class}
      )
   @Parameter(
      ID = "SAMPLED_INTEGER_PARAMETER_IMPLEMENTATION",
      Scoped = false
      )
   private IntegerModelParameterConfiguration
      parameter = new FromFileSampledIntegerModelParameterConfiguration();
   
   public IntegerModelParameterConfiguration getParameter() {
      return parameter;
   }
   
   public void setParameter(
      final IntegerModelParameterConfiguration parameter) {
      this.parameter = parameter;
   }
   
   public SampledIntegerModelParameterConfiguration() { }
   
   public SampledIntegerModelParameterConfiguration(
      final IntegerModelParameterConfiguration implementation) {
      this.parameter = Preconditions.checkNotNull(implementation);
   }
   
   static final class SamplingProvider implements Provider<Integer> {
      @Inject @Named("SINGLE_SAMPLE_MODEL_PARAMETER_IMPLEMENTATION")
      ModelParameter<Integer>
         source;
      
      @Override
      public Integer get() {
         return source.get();
      }
   }
   
   @Override
   protected void addBindings() {
      bind(Integer.class)
         .annotatedWith(Names.named(getScopeString()))
         .toProvider(SamplingProvider.class);
      expose(Integer.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}