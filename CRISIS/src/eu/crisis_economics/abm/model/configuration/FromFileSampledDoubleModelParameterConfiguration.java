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

import java.io.File;

import com.google.common.base.Preconditions;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration
   .AbstractDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.parameters.FromFileDoubleParameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;

@ConfigurationComponent(
   DisplayName = "Data Column"
   )
public final class FromFileSampledDoubleModelParameterConfiguration
   extends AbstractDoubleModelParameterConfiguration
   implements SampledDoubleModelParameterConfiguration,
      TimeseriesDoubleModelParameterConfiguration{
   
   private static final long serialVersionUID = -9049735401656686343L;
   
   @Layout(
      FieldName = "Samples",
      Order = 0,
      VerboseDescription = "A datafile containing manual samples (one per line) for this parameter."
      )
   private File
      samples = new File("");
   
   public File getSamples() {
      return samples;
   }
   
   public void setSamples(final File value) {
      this.samples = value;
   }
   
   public FromFileSampledDoubleModelParameterConfiguration() { }
   
   public FromFileSampledDoubleModelParameterConfiguration(
      final String sourceFilename) {
      this.samples = new File(Preconditions.checkNotNull(sourceFilename));
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()))
         .to(FromFileDoubleParameter.class);
      bind(String.class)
         .annotatedWith(Names.named("FROM_FILE_SAMPLED_MODEL_PARAMETER_FILENAME"))
         .toInstance(samples.getAbsolutePath());
      bind(String.class)
         .annotatedWith(Names.named("FROM_FILE_SAMPLED_MODEL_PARAMETER_NAME"))
         .toInstance(getScopeString());
      expose(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()));
   }
}
