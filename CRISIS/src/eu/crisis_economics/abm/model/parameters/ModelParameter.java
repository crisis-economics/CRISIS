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
package eu.crisis_economics.abm.model.parameters;

import com.google.inject.Provider;

/**
  * An interface for a named model parameter. The {@link Parameter} interface
  * satisfies the bounds of a Guice {@link Provider} object.<br><br>
  * 
  * Key to implementations of this interface is the {@link #get()} method, which
  * returns an instance of the underlying model parameter type.
  * 
  * @author phillips
  */
public interface ModelParameter<T> extends Provider<T> {
   /**
     * Get the {@link String} name of the model parameter.
     */
   public String getName();
   
   /**
     * Get the type of this model parameter. This method allows runtime type queries despite 
     * type erasure.
     */
   public Class<?> getParameterType();
}
