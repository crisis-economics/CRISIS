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

import java.util.Map;

import eu.crisis_economics.abm.model.parameters.ModelParameter;

/**
  * Typedef for the {@link Map} interface. This structure stores
  * a keyed collection of {@link Parameter<?>}s. The key of each
  * {@link Parameter<?>} is the name of the parameter.
  * @author phillips
  */
public interface ModelParameterCatalogue extends Map<String, ModelParameter<?>> {
   /**
     * Add a model parameter with a fixed integer value to this object.
     * @param key
     *        The name of the model parameter.
     * @param value
     *        The fixed numerical value of the model parameter.
     * @return
     */
   public ModelParameter<?> put(String key, Integer value);
   
   /**
     * Add a model parameter with a fixed double value to this object.
     * @param key
     *        The name of the model parameter.
     * @param value
     *        The fixed numerical value of the model parameter.
     * @return
     */
   public ModelParameter<?> put(String key, Double value);
   
   /**
     * Add a model parameter with a fixed boolean value to this object.
     * @param key
     *        The name of the model parameter.
     * @param value
     *        The fixed boolean value of the model parameter.
     * @return
     */
   public ModelParameter<?> put(String key, Boolean value);
   
   /**
     * Add a model parameter with a fixed string value to this object.
     * @param key
     *        The name of the model parameter.
     * @param value
     *        The fixed string value of the model parameter.
     * @return
     */
   public ModelParameter<?> put(String key, String value);
   
   /**
     * Add a model parameter with a fixed, generic value to this object.
     * @param key
     *        The name of the model parameter.
     * @param value
     *        The fixed value of the model parameter.
     * @return
     */
   public <T> ModelParameter<?> putGeneric(final String key, final T value);
}
