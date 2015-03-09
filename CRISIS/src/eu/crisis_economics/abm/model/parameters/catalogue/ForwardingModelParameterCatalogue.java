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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.crisis_economics.abm.model.parameters.FixedValue;
import eu.crisis_economics.abm.model.parameters.ModelParameter;

public final class ForwardingModelParameterCatalogue implements ModelParameterCatalogue {
   private Map<String, ModelParameter<?>>
      modelParameters;
   
   /**
     * Create an empty {@link ForwardingModelParameterCatalogue} object.
     */
   public ForwardingModelParameterCatalogue() {
      this.modelParameters = new HashMap<String, ModelParameter<?>>();
   }
   
   /**
     * A copy constructor for the {@link ForwardingModelParameterCatalogue} type.
     * The argument is unchanged by this operation.
     */
   public ForwardingModelParameterCatalogue(final ForwardingModelParameterCatalogue other) {
      if(other != null)
         this.modelParameters = new HashMap<String, ModelParameter<?>>(other);
      else
         this.modelParameters = new HashMap<String, ModelParameter<?>>();
   }
   
   /**
     * A sub-copy constructor for the {@link ForwardingModelParameterCatalogue} type.
     * The argument is unchanged by this operation.
     */
   public ForwardingModelParameterCatalogue(final Map<String, ModelParameter<?>> modelParameters) {
      if(modelParameters != null)
         this.modelParameters = new HashMap<String, ModelParameter<?>>(modelParameters);
      else
         this.modelParameters = new HashMap<String, ModelParameter<?>>();
   }
   
   @Override
   public void clear() {
      modelParameters.clear();
   }
   
   @Override
   public boolean containsKey(Object key) {
      return modelParameters.containsKey(key);
   }
   
   @Override
   public boolean containsValue(Object value) {
      return modelParameters.containsValue(value);
   }
   
   @Override
   public Set<java.util.Map.Entry<String, ModelParameter<?>>> entrySet() {
      return modelParameters.entrySet();
   }
   
   @Override
   public boolean equals(Object o) {
      return modelParameters.equals(o);
   }
   
   @Override
   public ModelParameter<?> get(Object key) {
      return modelParameters.get(key);
   }
   
   @Override
   public int hashCode() {
      return modelParameters.hashCode();
   }
   
   @Override
   public boolean isEmpty() {
      return modelParameters.isEmpty();
   }
   
   @Override
   public Set<String> keySet() {
      return modelParameters.keySet();
   }
   
   @Override
   public ModelParameter<?> put(String key, ModelParameter<?> value) {
      return modelParameters.put(key, value);
   }
   
   @Override
   public ModelParameter<?> put(final String key, final Integer value) {
      return modelParameters.put(
         key, new FixedValue<Integer>(key, value));
   }
   
   @Override
   public ModelParameter<?> put(final String key, final Double value) {
      return modelParameters.put(
         key, new FixedValue<Double>(key, value));
   }
   
   @Override
   public ModelParameter<?> put(final String key, final Boolean value) {
      return modelParameters.put(
         key, new FixedValue<Boolean>(key, value));
   }
   
   @Override
   public ModelParameter<?> put(final String key, final String value) {
      return modelParameters.put(
         key, new FixedValue<String>(key, value));
   }
   
   @Override
   public <T> ModelParameter<?> putGeneric(final String key, final T value) {
      return modelParameters.put(
         key, new FixedValue<T>(key, value));
   }
   
   @Override
   public void putAll(Map<? extends String, ? extends ModelParameter<?>> m) {
      modelParameters.putAll(m);
   }
   
   @Override
   public ModelParameter<?> remove(Object key) {
      return modelParameters.remove(key);
   }
   
   @Override
   public int size() {
      return modelParameters.size();
   }
   
   @Override
   public Collection<ModelParameter<?>> values() {
      return modelParameters.values();
   }
}
