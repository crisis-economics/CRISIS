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

import org.apache.commons.math3.analysis.UnivariateFunction;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * A simple implementation of the {@link Parameter}{@code <Double>} interface.<br><br>
  * 
  * This implementation accepts a real {@link UnivariateFunction}, {@code f}, as input,
  * and uses <code>f</code> to resolve queries to {@link #get}. For this implementation, 
  * the {@link #get} method is equivalent to <code>f(i)</code> where <code>i</code> initially
  * takes the value <code>0L</code> and is incremented by <code>1L</code> for each call made
  * to {@link #get()}.
  * 
  * @author phillips
  */
public final class FunctionParameter extends AbstractParameter<Double> {
   
   private UnivariateFunction
      function;
   private long
      useCounter;
   
   /**
     * Create a {@link FunctionParameter} object with custom parameters.<br><br>
     * 
     * See also {@link FunctionParameter} and {@link Parameter}.
     * 
     * @param function <br>
     *        The underlying {@link UnivariateFunction} to be used to resolve
     *        calls to {@link FunctionParameter#get}.
     * @param parameterName <br>
     *        The {@link String} name of this model parameter.
     */
   @Inject
   public FunctionParameter(
   @Named("FUNCTION_PARAMETER_IMPLEMENTATION")
      final UnivariateFunction function,
   @Named("FUNCTION_PARAMETER_NAME")
      final String parameterName
      ) {
      super(parameterName);
      this.function = Preconditions.checkNotNull(function);;
      this.useCounter = 0L;
   }
   
   @Override
   public Double get() {
      return function.value(useCounter++);
   }
   
   /**
     * Get the underlying {@link UnivariateFunction} for this object.
     */
   public UnivariateFunction getFunction() {
      return function;
   }
   
   @Override
   public Class<?> getParameterType() {
      return Double.class;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Function Parameter, function:" + function
            + ", name:" + super.getName() + ".";
   }
}
