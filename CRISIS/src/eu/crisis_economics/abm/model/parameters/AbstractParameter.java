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

import com.google.common.base.Preconditions;

/**
  * An abstract base class for implementations of the {@link Parameter} interface.<br><br>
  * 
  * Implementations of this class must provide the {@link #get()} and {@link #getParameterType()}
  * methods.
  * 
  * @author phillips
  */
public abstract class AbstractParameter<T> implements ModelParameter<T> {
   
   private final String
      parameterName;
   
   /**
     * Create a custom {@link AbstractParameter} object.<br><br>
     * 
     * See also {@link AbstractParameter} and {@link Parameter}.
     * 
     * @param parameterName <br>
     *        The name of this parameter. This argument may be the empty
     *        {@link String}, but may not be <code>null</code>.
     */
   protected AbstractParameter(
      final String parameterName
      ) {
      this.parameterName = Preconditions.checkNotNull(parameterName);
   }
   
   @Override
   public final String getName() {
      return parameterName;
   }
}
