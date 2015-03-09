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

import eu.crisis_economics.abm.algorithms.series.RandomSeries;

/**
  * An implementation of the {@link Parameter} interface.<br><br>
  * 
  * This implementation delegates the {@link Parameter#get()} method to a 
  * {@link RandomSeries} object. Each call to {@link Parameter#get()} advances 
  * the {@link RandomSeries} by one step.
  * 
  * @author phillips
  */
public class RandomSeriesParameter extends AbstractParameter<Double> {
   
   private final RandomSeries
      seriesGenerator;
   
   /**
     * Create a {@link RandomSeriesParameter} object with custom parameters.<br><br>
     * 
     * See also {@link AbstractParameter} and {@link RandomSeriesParameter}.
     * 
     * @param seriesGenerator <br>
     *        An {@link RandomSeries} whose values are to be returned by
     *        {@link RandomSeriesParameter#get()}. This argument cannot be
     *        <code>null</code>.
     */
   @Inject
   public RandomSeriesParameter(
   @Named("RANDOM_SERIES_PARAMETER_IMPLEMENTATION")
      final RandomSeries seriesGenerator,
   @Named("RANDOM_SERIES_PARAMETER_NAME")
      final String parameterName
      ) {
      super(parameterName);
      this.seriesGenerator = Preconditions.checkNotNull(seriesGenerator);
   }
   
   @Override
   public Double get() {
      return seriesGenerator.next();
   }
   
   @Override
   public Class<?> getParameterType() {
      return Double.class;
   }
}
