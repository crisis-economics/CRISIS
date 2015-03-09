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

import ai.aitia.crisis.aspectj.Agent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A simple forwarding implementation of the {@link Parameter} interface.<br><br>
  * 
  * By adding an instance of {@link TimeseriesParameter} to a constructor
  * in your code, you indicate that you intend to take multiple future samples from
  * the {@link Parameter}. Use cases for {@link TimeseriesParameter}{@code s}
  * might include:
  * 
  * <ul>
  *   <li> The Total Factor Productivity (TFP) of a Cobb-Douglas production function.
  *   <li> The size of a {@link Household} {@link Labour} workforce in time.
  *   <li> A parameter describing the riskiness of {@link Agent} behaviour in time.
  * </ul>
  * 
  * And so on. A {@link TimeseriesParameter} samples from an instance of an existing
  * {@link Parameter} once per simulation cycle. The result of this sample is
  * stored in memory and returned by {@link TimeseriesParameter#get()} until the
  * next sample is made.<br><br>
  * 
  * @author phillips
  */
public final class TimeseriesParameter<T> implements ModelParameter<T> {
   
   private final ModelParameter<T>
      parameter;
   private T
      currentValue;
   
   /**
     * Create a custom {@link TimeseriesParameter} object.<br><br>
     * 
     * See also {@link TimeseriesParameter} and {@link Parameter}.
     * 
     * @param parameter
     *        The {@link Parameter} to which this {@link TimeseriesParameter}
     *        delegates.
     */
   @Inject
   public TimeseriesParameter(
   @Named("TIMESERIES_MODEL_PARAMETER_IMPLEMENTATION")
      final ModelParameter<T> parameter
      ) {
      this.parameter = Preconditions.checkNotNull(parameter);
      
      scheduleSelf();
      updateValue();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "updateValue",
         CustomSimulationCycleOrdering.create(NamedEventOrderings.BEFORE_ALL, -1));
   }
   
   private void updateValue() {
      currentValue = parameter.get();
   }
   
   @Override
   public T get() {
      return currentValue;
   }
   
   @Override
   public String getName() {
      return parameter.getName();
   }
   
   @Override
   public Class<?> getParameterType() {
      return parameter.getParameterType();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Timeseries Model Parameter, current value: " + currentValue + ".";
   }
}
