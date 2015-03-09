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
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * An implementation of the {@link Parameter} interface. This implementation
  * provides a simple undecorated {@link Parameter} with a fixed (mutable)
  * value. 
  * 
  * @author phillips
  */
public final class FixedValue<T> extends AbstractParameter<T> {
   
   private T
      parameterValue;
   
   /**
     * Create a {@link FixedValue} {@link Parameter} object with custom parameters.<br><br>
     * 
     * @param parameterName <br>
     *        The name of the resulting {@link Parameter}. It is acceptable
     *        for this argument to be the empty string.
     * @param parameterValue (<code>V</code>) <br>
     *        The fixed (mutable) value of this parameter. The reference <code>V</code>
     *        can be modified via {@link FixedValue#set(Object)}.
     */
   @Inject
   public FixedValue(   // Mutable
   @Named("FIXED_VALUE_MODEL_PARAMETER_NAME")
      final String parameterName,
   @Named("FIXED_VALUE_MODEL_PARAMETER_VALUE")
      final T parameterValue
      ) {
      super(parameterName);
      this.parameterValue = Preconditions.checkNotNull(parameterValue);
   }
   
   @Override
   public T get() {
      return parameterValue;
   }
   
   /**
     * Modify the value of this {@link Parameter}. The argument will be returned by
     * {@link #get()} unless it ceases to exist or another call is made to {@link #set(Object)}.
     * 
     * @param value (<code>V</code>) <br>
     *        The value to be returned by {@link #get()}.
     */
   public void set(final T value) {
      this.parameterValue = value;
   }
   
   @Override
   public Class<?> getParameterType() {
      return parameterValue.getClass();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Fixed Value Model Parameter, name: " + super.getName()
            + ", fixed value:" + parameterValue + ", type: "
            + getParameterType().toString() + ".";
   }
}
