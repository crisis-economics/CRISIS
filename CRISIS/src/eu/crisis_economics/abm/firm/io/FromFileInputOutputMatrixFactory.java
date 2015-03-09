/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.firm.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.math3.linear.MatrixUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * An implementation of the {@link InputOutputMatrixFactory} interface. This implementation
  * parses an {@link InputOutputMatrix} from a text file whose filename is specified in 
  * the constructor.<br><br>
  * 
  * The input text file should consist of a block of quoted strings followed by a 
  * block of matrix elements. The number of strings should correspond to the dimension
  * of the matrix, and the list of matrix elements should correspond to a list of
  * elements appearing in a matrix with square dimension. An example of a valid 
  * input file is as follows:<br><br>
  * 
  * <code>
  * <table>
  *   <tr><td>"Agriculture"   </td><td style="width:70pt"></td><td style="width:70pt"></td></tr>
  *   <tr><td>"Mining"        </td><td></td><td></td></tr>
  *   <tr><td>"Legal Services"</td><td></td><td></td></tr>
  *   <tr><td>0.1</td><td>0.2</td><td>0.3</td></tr>
  *   <tr><td>0.5</td><td>0.6</td><td>0.7</td></tr>
  *   <tr><td>0.8</td><td>0.9</td><td>1.0</td></tr>
  * </table>
  * </code>
  * 
  * @author phillips
  */
public final class FromFileInputOutputMatrixFactory implements InputOutputMatrixFactory {
   
   private final String
      filename;
   
   /**
     * Create a {@link FromFileInputOutputMatrixFactory} object with custom
     * parameters.
     * 
     * @param filename
     *        The {@link String} filename from which to parse an {@link InputOutputMatrix}.
     *        This argument must be non-<code>null</code> and must target a valid, accessible
     *        text file specifying a complete {@link InputOutputMatrix}. For the expected 
     *        format of this file, see {@link FromFileInputOutputMatrixFactory}. This 
     *        argument should not be the empty {@link String}.
     */
   @Inject
   public FromFileInputOutputMatrixFactory(
   @Named("FROM_FILE_INPUT_OUTPUT_MATRIX_FACTORY_FILENAME")
      final String filename
      ) {
      Preconditions.checkArgument(!filename.isEmpty());
      this.filename = Preconditions.checkNotNull(filename);
   }
   
   /**
     * Try to parse the {@link String} filename specified in the constructor
     * in order to create an {@link InputOutputMatrix}.
     */
   @Override
   public InputOutputMatrix createInputOutputMatrix() {
      try {
         return tryParse(filename);
      } catch (final IOException e) {
         throw new IllegalStateException(e.getMessage());
      }
   }
   
   /** Try to parse IO Table data from a file. Throws IOException on 
     *  parser failures and/or filestream IO errors. The data file should 
     *  contain a text representations of the IO Matrix, one row per line, 
     *  composed of matrix entries separated by commas (,) spaces, 
     *  tabs (\t) or colon (:). IO Tables are necessarily square matrices
     *  containing non-negative values.
     *  
     *  Example usage: 
     *    IOMatrix matrix = IOTableParser.tryParse(filename);
     *    double matrixEntry = matrix.get(i, j)          // Matrix entries
     *    int matrixDimension = matrix.matrixDimension() // Matrix size (width)
     * */
   public InputOutputMatrix tryParse(String filename) throws IOException {
       final List<Double>
          elements = new ArrayList<Double>();
       final List<String>
          sectorNames = new ArrayList<String>();
       double[][]
          matrixElements = null;
       final BufferedReader
          fileReader = new BufferedReader(new FileReader(filename));
       String
          line = null;
       boolean
          foundDataSection = false;
       try {
          line = null;
          if(!foundDataSection) {
             while((line = fileReader.readLine()) != null) {
                if(line.isEmpty()) continue;
                line = line.trim();
                if(line.startsWith("\"")) {
                   // This element must also end with "
                   if(!line.endsWith("\""))
                      throw new IOException(
                         "FromFileInputOutputMatrix: input line " + line + " has an illegal format."
                         );
                   sectorNames.add(line);
                }
                else {
                   foundDataSection = true;
                   break;
                }
             }
          }
          if(foundDataSection) {
             if(line == null)
                line = fileReader.readLine();
             while(line != null) {
                final StringTokenizer
                   tokenizer = new StringTokenizer(line);
                while(tokenizer.hasMoreTokens()) {
                   final String
                      nextToParse = tokenizer.nextToken(" ,:\t");
                   if(line.isEmpty()) continue;
                   elements.add(Double.parseDouble(nextToParse));
                   continue;
               }
               line = fileReader.readLine();
             }
          }
          
          // Reformat the input as double[][]
          final int
             dimension = (int) Math.sqrt(elements.size());
          if(dimension * dimension != elements.size())
             throw new IOException(
                "FromFileInputOutputMatrix: input data has length " + elements.size() + ", "
              + "which is not square. This data does not represent an input-output matrix."
                 );
          matrixElements = new double[dimension][dimension];
          int counter = 0;
          for(int i = 0; i< dimension; ++i)
             for(int j = 0; j< dimension; ++j)
                matrixElements[i][j] = elements.get(counter++);
       }
       catch(final Exception e) {
          throw new IOException(
             "FromFileInputOutputMatrix: input-output datafile does not have the correct format. " 
           + "Details of the underlying problem follow: " + e.getMessage());
       }
       finally {
          fileReader.close();
       }
       
       final InputOutputMatrix
          result = new InputOutputMatrix(
             MatrixUtils.createRealMatrix(matrixElements),
             sectorNames
             );
       return result;
   }
}
