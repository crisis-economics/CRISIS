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
import eu.crisis_economics.abm.model.parameters.FromFileStringParameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;

@ConfigurationComponent(
   DisplayName = "From Datafile"
   )
public final class FromFileSampledStringModelParameterConfiguration
   extends AbstractPrivateConfiguration implements StringModelParameterConfiguration {
   
   private static final long serialVersionUID = 1031569416566747949L;
   
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
   
   public FromFileSampledStringModelParameterConfiguration() { }
   
   public FromFileSampledStringModelParameterConfiguration(
      final String sourceFile) {
      this.samples = new File(Preconditions.checkNotNull(sourceFile));
   }
   
   @Override
   protected void addBindings() {
      bind(
         new TypeLiteral<ModelParameter<String>>(){})
         .annotatedWith(Names.named(getScopeString()))
         .to(FromFileStringParameter.class);
      bind(String.class)
         .annotatedWith(Names.named("FROM_FILE_SAMPLED_MODEL_PARAMETER_FILENAME"))
         .toInstance(samples.getAbsolutePath());
      bind(String.class)
         .annotatedWith(Names.named("FROM_FILE_SAMPLED_MODEL_PARAMETER_NAME"))
         .toInstance(getScopeString());
      expose(
         new TypeLiteral<ModelParameter<String>>(){})
         .annotatedWith(Names.named(getScopeString()));
   }
}
