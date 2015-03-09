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
package eu.crisis_economics.abm.model.parameters.catalogue;

import java.util.Map.Entry;

import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.simulation.injection.AbstractConfigurationModule;
import eu.crisis_economics.utilities.InjectionUtils;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * An base implementation of the {@link ParameterCatalogueModule}
  * interface.
  * @author phillips
  */
public class ParameterCatalogueConfigurationModule extends AbstractConfigurationModule {
   
   private ModelParameterCatalogue
      modelParameters;
   
   /**
     * Create a {@link ParameterCatalogueConfigurationModule} with an empty
     * {@link ModelParameterCatalogue}.
     */
   public ParameterCatalogueConfigurationModule() { 
      this.modelParameters = new ForwardingModelParameterCatalogue();
   }
   
   @SuppressWarnings({ "unchecked", "rawtypes" })
   protected void configure() {
      /**
        * Primitive number bindings.
        */
      for(final Entry<String, ModelParameter<?>> record : modelParameters.entrySet()) {
         Class parameterType = record.getValue().getParameterType();
         bind(parameterType).annotatedWith(
            Names.named(record.getKey())).toProvider(
               InjectionUtils.castProvider(record.getValue(), parameterType));
      }
      /**
        * {@link Parameter} bindings.
        */
      for(final Entry<String, ModelParameter<?>> record : modelParameters.entrySet()) {
         bind(ModelParameter.class).annotatedWith(
            Names.named(record.getKey())).toInstance(record.getValue());
      }
   }
   
   /**
     * Get a reference to the {@link ModelParameterCatalogue}.
     * @return
     */
   protected ModelParameterCatalogue getParameterCatalogue() {
      return modelParameters;
   }
   
   public void setParameterCatalogue(final ModelParameterCatalogue parameters) {
      StateVerifier.checkNotNull(parameters);
      this.modelParameters = parameters;
   }
}
