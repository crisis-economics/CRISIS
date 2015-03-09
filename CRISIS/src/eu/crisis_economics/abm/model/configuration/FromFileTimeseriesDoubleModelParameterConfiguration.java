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
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration
   .AbstractDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.parameters.FromFileTimeseriesParameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;

@ConfigurationComponent(
   DisplayName = "Interpolated Timeseries"
   )
public final class FromFileTimeseriesDoubleModelParameterConfiguration
   extends AbstractDoubleModelParameterConfiguration
   implements TimeseriesDoubleModelParameterConfiguration{
   
   private static final long serialVersionUID = 1881655558512495141L;
   
   @Layout(
      FieldName = "Timeseries Datafile",
      Order = 0,
      VerboseDescription = "A datafile containing a manual timeseries for this parameter"
      )
   private File
      timeseries = new File("");
   
   public File getTimeseries() {
      return timeseries;
   }
   
   public void setTimeseries(final File value) {
      this.timeseries = value;
   }
   
   @Layout(
      FieldName = "Interpolation Method",
      Order = 1,
      VerboseDescription = "How to interpolate input data"
      )
   @Submodel
   @Parameter(
      ID = "FROM_FILE_TIMESERIES_PARAMETER_INTERPOLATOR"
      )
   private AbstractUnivariateInterpolatorConfiguration interpolator
      = new FloorUnivariateInterpolatorConfiguration();
   
   public AbstractUnivariateInterpolatorConfiguration getInterpolator() {
      return interpolator;
   }
   
   public void setInterpolator(
      final AbstractUnivariateInterpolatorConfiguration interpolator) {
      this.interpolator = interpolator;
   }
   
   public FromFileTimeseriesDoubleModelParameterConfiguration() { }
   
   FromFileTimeseriesDoubleModelParameterConfiguration(
      final String sourceFilename) {
      this.timeseries = new File(sourceFilename);
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()))
         .to(FromFileTimeseriesParameter.class);
      bind(String.class)
         .annotatedWith(Names.named("FROM_FILE_TIMESERIES_PARAMETER_FILENAME"))
         .toInstance(timeseries.getAbsolutePath());
      bind(String.class)
         .annotatedWith(Names.named("FROM_FILE_TIMESERIES_PARAMETER_NAME"))
         .toInstance(getScopeString());
      expose(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()));
   }
}
