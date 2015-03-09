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

import eu.crisis_economics.utilities.StateVerifier;

/**
  * An abstract base class for a {@link Parameter} objects whose values are
  * parsed from a text file. Implementations of this class should provide the
  * following methods:
  * 
  * <ul>
  *   <li> {@link #parse(String)}. This method should accept as input one 
  *        {@link String} and return a parsed instance of {@code T}.
  * </ul>
  * 
  * @author phillips
  *
  * @param <T>
  *        The return type of the {@link Parameter}.
  */
public abstract class AbstractFromFileParameter<T>
   extends AbstractParameter<T> {
   
   private int
      evaluationCounter;
   
   private List<T>
      values;
   
   /**
     * Create a {@link AbstractFromFileParameter} object using the specified
     * file.
     * 
     * @param filename <br>
     *        The name of the file from which to read sample data.
     * @param parameterName <br>
     *        The name of the resulting {@link Parameter}. It is acceptable
     *        for this argument to be the empty string.
     * 
     * @throws IOException
     *         This method raises {@link IOException} if, for any reason, the stated
     *         file cannot be processed. Reasons for such failure include: (a) the
     *         file does not exist; (b) the file is inaccessible to the read stream;
     *         (c) the file does not contain any data; (d) the file exists and can 
     *         be accessed but does not have the required format. 
     *         See {@link AbstractFromFileParameter}.
     */
   protected AbstractFromFileParameter(
      final String filename,
      final String parameterName
      ) throws IOException {
      super(parameterName);
      StateVerifier.checkNotNull(filename, parameterName);
      final BufferedReader
         reader = new BufferedReader(new FileReader(filename));
      String
         line = null;
      values = new ArrayList<T>();
      try {
         while((line = reader.readLine()) != null) {
            if(line.isEmpty()) continue;
            values.add(parse(line));
         }
      }
      catch(final NumberFormatException e) {
         throw new IOException(e.getMessage());
      }
      finally {
         reader.close();
      }
      if(values.isEmpty())
         throw new IllegalStateException(
            getClass().getSimpleName() + ": the file named " + filename + " contains no data.");
   }
   
   abstract T parse(final String line);
   
   @Override
   public final T get() {
      if(evaluationCounter >= values.size())
         throw new IllegalStateException(
            getClass().getSimpleName() + ": the number of evaluations, " + evaluationCounter
          + ", has exceeded the length of the source datafile. No further samples are known."
           );
      return values.get(evaluationCounter++);
   }
   
   /**
     * Get the number of records parsed from the file.
     */
   public final int getNumberOfRecords() {
      return values.size(); 
   }
   
   @Override
   public abstract Class<?> getParameterType();
}
