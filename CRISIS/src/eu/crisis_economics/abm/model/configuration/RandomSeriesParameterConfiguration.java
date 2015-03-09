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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration
   .AbstractDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.model.parameters.RandomSeriesParameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Random Series"
   )
public final class RandomSeriesParameterConfiguration
   extends
      AbstractDoubleModelParameterConfiguration
   implements 
      SampledDoubleModelParameterConfiguration, 
      TimeseriesDoubleModelParameterConfiguration
      {
   
   private static final long serialVersionUID = 5182825365026085370L;
   
   @Submodel
   @Layout(
      Order = 0.0,
      FieldName = "Series",
      VerboseDescription = "Select the type of random series to use"
      )
   @Parameter(
      ID = "RANDOM_SERIES_PARAMETER_IMPLEMENTATION"
      )
   private AbstractRandomSeriesConfiguration
      randomSeriesParameterImplementation = new OrsteinUhlenbeckSeriesConfiguration();
   
   public AbstractRandomSeriesConfiguration getRandomSeriesParameterImplementation() {
      return randomSeriesParameterImplementation;
   }
   
   public void setRandomSeriesParameterImplementation(
      final AbstractRandomSeriesConfiguration randomSeriesParameterImplementation) {
      this.randomSeriesParameterImplementation = randomSeriesParameterImplementation;
   }
   
   public RandomSeriesParameterConfiguration() { }
   
   public RandomSeriesParameterConfiguration(
      final AbstractRandomSeriesConfiguration implementation) {
      this.randomSeriesParameterImplementation = Preconditions.checkNotNull(implementation);
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()))
         .to(new TypeLiteral<RandomSeriesParameter>(){}
         );
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "RANDOM_SERIES_PARAMETER_NAME", getScopeString()
         ));
      expose(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()));
   }
}
