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

import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * An implementation of the {@link InputOutputMatrixFactory} interface. This implementation
  * generates a chained (circuitry) {@link InputOutputMatrix} with no random elements.<br><br>
  * Concretely, this factory generates {@link InputOutputMatrix} objects with (pre-normalized)
  * format:
  * 
  * <center><code>
  * <table>
  *   <tr> <td>0</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>1</td> </tr>
  *   <tr> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>1</td> <td>0</td> </tr>
  *   <tr> <td>0</td> <td>0</td> <td>0</td> <td>1</td> <td>0</td> <td>0</td> </tr>
  *   <tr> <td>0</td> <td>0</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> </tr>
  *   <tr> <td>0</td> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>1</td> </tr>
  *   <tr> <td>1</td> <td>0</td> <td>0</td> <td>0</td> <td>1</td> <td>0</td> </tr>
  * </table>
  * </code></center><br>
  * 
  * The length of the horizontal repeating pattern is called the <i>chain length</i>.
  * In the above example, the chain length is {@code 3}. Chain length {@code 1}
  * corresponds to a homogenous matrix filled with {@code 1s}.<br><br>
  * 
  * @author phillips
  */
public final class ChainedInputOutputMatrixFactory implements InputOutputMatrixFactory {
   
   private List<String>
      sectorNames;
   private final int
      chainLength;
   
   /**
     * Create a {@link ChainedInputOutputMatrixFactory} object with custom parameters.
     * 
     * @param sectorNames
     *        A {@link List} of sector names. The length of this collection specifies
     *        the dimension of the random square {@link InputOutputMatrix} to generate.
     * @param the length of the input-output chain to generate. 
     *        See {@link ChainedInputOutputMatrixFactory}. This argument must be nonzero
     *        and positive.
     */
   @Inject
   public ChainedInputOutputMatrixFactory(
      final SectorNameProvider sectorNames,
   @Named("CHAINED_INPUT_OUTPUT_MATRIX_FACTORY_CHAIN_LENGTH")
      final int chainLength
      ) {
      Preconditions.checkArgument(chainLength > 0);
      this.sectorNames = Preconditions.checkNotNull(sectorNames.asList());
      this.chainLength = chainLength;
   }
   
   @Override
   public InputOutputMatrix createInputOutputMatrix() {
      final int
         dimension = sectorNames.size();
      final double[][]
         data = new double[dimension][dimension];
      for(int i = 0; i < dimension; ++i)
         for(int j = 0; j< dimension; ++j)
            if(( - i + j - 1) % chainLength == 0)
               data[i][j] = 1.;
      return new InputOutputMatrix(
         MatrixUtils.createRealMatrix(data), sectorNames);
   }
   
   /**
     * Get the chain length for this {@link InputOutputMatrixFactory}.
     */
   public int getChainLength() {
      return chainLength;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Chained Input-Output Matrix Factory. Chain length: " + getChainLength();
   }
}
