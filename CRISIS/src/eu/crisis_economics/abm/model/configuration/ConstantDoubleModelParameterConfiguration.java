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

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration
   .AbstractDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.parameters.FixedValue;
import eu.crisis_economics.abm.model.parameters.ModelParameter;

@ConfigurationComponent(
   DisplayName = "Fixed"
   )
public final class ConstantDoubleModelParameterConfiguration
   extends AbstractDoubleModelParameterConfiguration
   implements SampledDoubleModelParameterConfiguration, 
      TimeseriesDoubleModelParameterConfiguration{
   
   private static final long serialVersionUID = -2538385066901834461L;
   
   public ConstantDoubleModelParameterConfiguration() { }
   
   public ConstantDoubleModelParameterConfiguration(final double value) {
      this.fixedParameterValue = value;
   }
   
   private double
      fixedParameterValue = 0.;
   
   public double getFixedParameterValue() {
      return fixedParameterValue;
   }
   
   public void setFixedParameterValue(final double value) {
      this.fixedParameterValue = value;
   }
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()))
         .to(new TypeLiteral<FixedValue<Double>>(){}
         );
      bind(String.class)
         .annotatedWith(Names.named("FIXED_VALUE_MODEL_PARAMETER_NAME"))
         .toInstance(getScopeString());
      bind(Double.class)
         .annotatedWith(Names.named("FIXED_VALUE_MODEL_PARAMETER_VALUE"))
         .toInstance(fixedParameterValue);
      expose(
         new TypeLiteral<ModelParameter<Double>>(){})
         .annotatedWith(Names.named(getScopeString()));
   }
}
