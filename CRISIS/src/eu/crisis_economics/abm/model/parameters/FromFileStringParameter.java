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

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A simple implementation of the {@link Parameter}{@code <String>}
  * interface.<br><br>
  * 
  * This implementations reads a collection of sample data from a file.
  * The file contains all possible values for this {@link Parameter}
  * in sequence. The file should be a single-column data table with
  * exactly one {@link String} record per line.<br><br>
  * 
  * The method {@link #get()} for this method can be called at most {@code N}
  * times for a datafile containing {@code N} records. The {@code N+1-th} call
  * to {@link #get()} raises {@link IllegalStateException}.
  * 
  * An example of a simple valid input file is as follows:<br><br>
  * 
  * <table style="width:100pt">
  *  <tr>
  *    <td><code>HSBC</code></td>
  *  </tr>
  *  <tr>
  *    <td><code>SANTANDER</code></td> 
  *  </tr>
  *  <tr>
  *    <td><code>LLOYDS</code></td> 
  *  </tr>
  * </table><br>
  * 
 * The above input file results in a {@link AbstractFromFileParameter}
 * with {@code 3} possible values. The values <code>HSBC</code>, <code>SANTANDER</code>,
 * <code>LLOYDS</code> will be returned, in that order, for each call to
 * {@link #get()}. The fourth call to {@link #get()} raises {@link IllegalStateException}.
 * 
 * @author phillips
 */
public final class FromFileStringParameter
   extends AbstractFromFileParameter<String> {
   
   /**
     * Create a {@link FromFileStringParameter} object using the specified file.<br><br>
     * 
     * See also {@link AbstractFromFileParameter} and {@link FromFileStringParameter}.
     */
   @Inject
   public FromFileStringParameter(
   @Named("FROM_FILE_SAMPLED_MODEL_PARAMETER_FILENAME")
      final String filename,
   @Named("FROM_FILE_SAMPLED_MODEL_PARAMETER_NAME")
      final String parameterName
      ) throws IOException {
      super(filename, parameterName);
   }
   
   /**
     * Trim the input {@link String} and return it as a parsed element.
     */
   @Override
   String parse(final String line) {
      return line.trim();
   }
   
   @Override
   public Class<?> getParameterType() {
      return String.class;
   }
}
