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

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration.AbstractLongModelParameterConfiguration;
import eu.crisis_economics.abm.model.parameters.FixedValue;
import eu.crisis_economics.abm.model.parameters.ModelParameter;

@ConfigurationComponent(
   DisplayName = "Fixed"
   )
public final class ConstantLongModelParameterConfiguration
   extends AbstractLongModelParameterConfiguration {
   
   private static final long serialVersionUID = 7158342725842913382L;
   
   private long
      fixedParameterValue = 0L;
   
   public long getFixedParameterValue() {
      return fixedParameterValue;
   }
   
   public void setFixedParameterValue(final long value) {
      this.fixedParameterValue = value;
   }
   
   public ConstantLongModelParameterConfiguration() { }
   
   public ConstantLongModelParameterConfiguration(final long value) {
      this.fixedParameterValue = value;
   }
   
   @Override
   protected void addBindings() {
      bind(
         new TypeLiteral<ModelParameter<Long>>(){})
         .annotatedWith(Names.named(getScopeString()))
         .to(new TypeLiteral<FixedValue<Long>>(){})
         .in(Singleton.class);
      bind(String.class)
         .annotatedWith(Names.named("FIXED_VALUE_MODEL_PARAMETER_NAME"))
         .toInstance(getScopeString());
      bind(Long.class)
         .annotatedWith(Names.named("FIXED_VALUE_MODEL_PARAMETER_VALUE"))
         .toInstance(fixedParameterValue);
      expose(
         new TypeLiteral<ModelParameter<Long>>(){})
         .annotatedWith(Names.named(getScopeString()));
      super.addBindings();
   }
}
