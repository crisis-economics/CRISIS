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
  * generates a diagonal {@link InputOutputMatrix} with no random elements.
  * 
  * @author phillips
  */
public final class SimpleSelfBiasingInputOutputMatrixFactory implements InputOutputMatrixFactory {
   
   private List<String>
      sectorNames;
   private double
      selfBiasMultiplier;
   
   /**
     * Create a {@link SimpleSelfBiasingInputOutputMatrixFactory} object with custom
     * parameters.
     * 
     * @param sectorNames
     *        A {@link List} of sector names. The length of this collection specifies
     *        the dimension of the random square {@link InputOutputMatrix} to generate.
     * @param selfBiasMultiplier
     *        A number specifying the ratio of diagonal matrix elements to non-diagonal 
     *        matrix elements. Concretely, if the desired matrix has the form:<br><br>
     *        <center><code><table>
     *           <tr><td>a</td><td>b</td><td>b</td></tr>
     *           <tr><td>b</td><td>a</td><td>b</td></tr>
     *           <tr><td>b</td><td>b</td><td>a</td></tr>
     *        </table></code></center><br>
     *        Then <code>selfBiasMultiplier = a / b</code>. This argument should be
     *        non-negative.
     */
   @Inject
   public SimpleSelfBiasingInputOutputMatrixFactory(
      final SectorNameProvider sectorNames,
   @Named("SIMPLE_SELF_BIASING_INPUT_OUTPUT_MATRIX_FACTORY_BIAS_FACTOR")
      final double selfBiasMultiplier
      ) {
      this.sectorNames = Preconditions.checkNotNull(sectorNames.asList());
      this.selfBiasMultiplier = selfBiasMultiplier;
   }
   
   @Override
   public InputOutputMatrix createInputOutputMatrix() {
      final int
         dimension = sectorNames.size();
      final double[][]
         data = new double[dimension][dimension];
      for(int i = 0; i< dimension; ++i)
         for(int j = 0; j< dimension; ++j)
            if(i == j)
               data[i][j] = selfBiasMultiplier;
            else
               data[i][j] = 1.;
      return new InputOutputMatrix(
         MatrixUtils.createRealMatrix(data), sectorNames);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Diagonal Input-Output Matrix Factory";
   }
}
