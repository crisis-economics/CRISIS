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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.BoundedDomainMixin;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.SimpleDomainBounds;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.ArrayUtil;

/**
  * A simple implementation of the {@link Parameter}{@code <Double>}
  * interface.<br><br>
  * 
  * This implementations reads a timeseries from an external file. Calls to
  * {@link #get()} are resolved as follows:<br><br>
  * 
  * {@code (a)}
  *    Query the simulation time {@code T}: {@link Simulation#getTime()};<br>
  * {@code (b)}
  *    Evaluate the serialized timeseries at time {@code T}.<br><br>
  * 
  * This object is created with reference to a file containing the timeseries
  * data. The file should be a two-column data table with one record per line.
  * The first column should be ordered ascending and represents keypoints in
  * time {@code T}. The second colum represents the timeseries evaluation
  * {@code f(T)} at time {@code T}. The timeseries should contain at least two
  * keypoints. Columns in the file can be separated by any whitespace, colon,
  * semicolon, comma or tab. Newlines indicate the beginning of a new record.<br><br>
  * 
  * An example of a simple valid input file is as follows:<br><br>
  * 
  * <table style="width:100pt">
  *  <tr>
  *    <td>1.0</td>
  *    <td>1.0</td> 
  *  </tr>
  *  <tr>
  *    <td>2.0</td>
  *    <td>4.0</td> 
  *  </tr>
  *  <tr>
  *    <td>3.0</td>
  *    <td>9.0</td> 
  *  </tr>
  * </table>
  * 
  * @author phillips
  */
public final class FromFileTimeseriesParameter extends AbstractParameter<Double> {
   
   private UnivariateFunction
      interpolation;
   
   private BoundedDomainMixin
      bounds,
      outOfRangeEvaluation;
   
   /**
     * Create a {@link FromFileTimeseriesParameter} object using the specified
     * file.<br><br>
     * 
     * See also {@link FromFileTimeseriesParameter} and {@link Parameter}.
     * 
     * @param filename
     *        The name of the file from which to read timeseries data.
     * @param parameterName
     *        The name of the resulting {@link Parameter}. It is acceptable
     *        for this argument to be the empty string.
     * @param interpolator
     *        An instance of a {@link UnivariateInterpolator}. This interpolator
     *        is used to convert timeseries keypoints into a queryable function
     *        {@code f(T)} for all {@code T} in the domain of the interpolation
     *        keys. Possible examples, among others, include the {@link SplineInterpolator}
     *        and the {@link LinearInterpolator}.
     * 
     * @throws IOException
     *         This method raises {@link IOException} if, for any reason, the stated
     *         file cannot be processed. Reasons for such failure include: (a) the
     *         file does not exist; (b) the file is inaccessible to the read stream;
     *         (c) the file does not contain any data or contains fewer than two 
     *         records; (d) the file exists and can be accessed but does not have
     *         the required format. See {@link FromFileTimeseriesParameter}.
     */
   @Inject
   public FromFileTimeseriesParameter(
   @Named("FROM_FILE_TIMESERIES_PARAMETER_FILENAME")
      final String filename,
   @Named("FROM_FILE_TIMESERIES_PARAMETER_NAME")
      final String parameterName,
   @Named("FROM_FILE_TIMESERIES_PARAMETER_INTERPOLATOR")
      final UnivariateInterpolator interpolator
      ) throws IOException {
      super(parameterName);
      Preconditions.checkNotNull(filename);
      final BufferedReader
         reader = new BufferedReader(new FileReader(filename));
      String
         line = null;
      final List<Double>
         x = new ArrayList<Double>(),
         y = new ArrayList<Double>();
      int
         numInterpolationPoints = 0;
      try {
         while((line = reader.readLine()) != null) {
            if(line.isEmpty()) continue;
            final String[]
               entries = line.split("[\\s:;,\t]+");
            if(entries.length != 2)
               throw new IOException(
                  getClass().getSimpleName() + ": serialized timeseries are expected to "
                + "consist of exactly two data columns. The input data: " + entries + " does "
                + "not conform to this template. The data in file " + filename + " could not "
                + "be parsed."
                );
            x.add(Double.parseDouble(entries[0]));
            y.add(Double.parseDouble(entries[1]));
            ++numInterpolationPoints;
         }
      }
      catch(final NumberFormatException e) {
         throw new IOException(e.getMessage());
      }
      finally {
         reader.close();
      }
      
      if(numInterpolationPoints <= 1)
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": file " + filename + " exists and has valid format,"
          + " but contains fewer than 2 records. At least two interpolation points are required."
            );
      final UnivariateFunction function =
         interpolator.interpolate(ArrayUtil.toPrimitive(x), ArrayUtil.toPrimitive(y));
      this.interpolation = function;
      this.bounds = new SimpleDomainBounds(x.get(0), x.get(x.size()-1));
      this.outOfRangeEvaluation = new SimpleDomainBounds(y.get(0), y.get(y.size() - 1));
   }
   
   @Override
   public Double get() {
      final double
         time = Simulation.getTime();
      if(time < bounds.getMinimumInDomain())
         return outOfRangeEvaluation.getMinimumInDomain();
      else if(time > bounds.getMaximumInDomain())
         return outOfRangeEvaluation.getMaximumInDomain();
      else
         return interpolation.value(Simulation.getTime());
   }
   
   @Override
   public Class<?> getParameterType() {
      return Double.class;
   }
}
