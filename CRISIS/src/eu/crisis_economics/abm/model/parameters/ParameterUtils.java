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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import com.google.common.base.Preconditions;

/**
  * Static utilities for {@link Parameter} classes.
  * 
  * @author phillips
  */
public final class ParameterUtils {
   
   /**
     * Convert an object {@code X} to a {@link Parameter} returning {@code X}.
     * 
     * @param value
     *        The object {@code X}
     */
   public static <T> ModelParameter<T>
      asParameter(final T value) {
      return new ModelParameter<T>() {
         @Override
         public T get() {
            return value;
         }
         @Override
         public String getName() {
            return "Anonymous Model Parameter";
         }
         @Override
         public Class<?> getParameterType() {
            return value.getClass();
         }
      };
   }
   
   /**
     * Convert an object {@code X} into a {@link TimeseriesParameter}
     * returning {@code X}.
     * 
     * @param value
     *        The object {@code X}.
     */
   public static <T> TimeseriesParameter<T>
      asMultiSampleParameter(final T value) {
      return new TimeseriesParameter<T>(asParameter(value));
   }
   
   /**
     * Convert an instance of a {@link Parameter}{@code <T>} to an instance of
     * a {@link TimeseriesParameter}{@code <T>}. The argument must be non-<code>null</code>.
     * 
     * @param modelParameter (<code>M</code>)</br>
     *        The {@link Parameter} object to use.
     * @return
     *        A non-<code>null</code> {@link TimeseriesParameter}{@code <T>} implemented
     *        by {@code M}.
     */
   public static <T> TimeseriesParameter<T> asTimeseries(
      final ModelParameter<T> modelParameter) {
      return new TimeseriesParameter<T>(Preconditions.checkNotNull(modelParameter));
   }
   
   /**
     * Create a {@link TimeseriesParameter}{@code <T>} from an instance, {@code X},
     * of {@code T}. The resulting {@link TimeseriesParameter} will always return 
     * a reference to {@code X}.
     * 
     * @param constantValue ({@code X})
     * @return
     *        A {@link TimeseriesParameter} {@code M} whose value {@code M(t)} is {@code X}.
     */
   public static <T> TimeseriesParameter<T> asTimeseries(
      final T constantValue) {
      return new TimeseriesParameter<T>(
         new FixedValue<T>("Unnamed Paramter", constantValue));
   }

   /**
     * Create a model parameter with a double value drawn from a
     * normal distribution.
     * 
     * @param parameterName
     *        The name of the model parameter.
     * @param mean
     *        The minimum value of the paremeter.
     * @param stdDeviation
     *        The maximum value of the parameter.
     */
   public static ModelParameter<?> createParameterWithNormalDistribution(
      final String parameterName,
      final double mean,
      final double stdDeviation
      ) {
      Preconditions.checkArgument(stdDeviation >= 0.);
      return new FromRealDistributionModelParameter(
         new NormalDistribution(mean, stdDeviation),
         parameterName
         );
   }

   /**
     * Create a model parameter with a double value drawn from a
     * uniform distribution.
     * 
     * @param parameterName
     *        The name of the model parameter.
     * @param minParameterValue
     *        The minimum value of the paremeter.
     * @param maxParameterValue
     *        The maximum value of the parameter.
     */
   public static ModelParameter<?> createParameterWithUniformDistribution(
      final String parameterName,
      final double minParameterValue,
      final double maxParameterValue
      ) {
      Preconditions.checkArgument(minParameterValue <= maxParameterValue);
      return new FromRealDistributionModelParameter(
         new UniformRealDistribution(minParameterValue, maxParameterValue),
         parameterName
         );
   }
   
   private ParameterUtils() { }   // Uninstantiable
}
