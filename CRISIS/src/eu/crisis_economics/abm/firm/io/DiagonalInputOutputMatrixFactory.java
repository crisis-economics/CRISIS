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

/**
  * An implementation of the {@link InputOutputMatrixFactory} interface. This implementation
  * generates a diagonal {@link InputOutputMatrix} with no random elements.
  * 
  * @author phillips
  */
public final class DiagonalInputOutputMatrixFactory implements InputOutputMatrixFactory {
   
   private List<String>
      sectorNames;
   
   /**
     * Create a {@link DiagonalInputOutputMatrixFactory} object with custom parameters.
     * 
     * @param sectorNames
     *        A {@link List} of sector names. The length of this collection specifies
     *        the dimension of the random square {@link InputOutputMatrix} to generate.
     */
   @Inject
   public DiagonalInputOutputMatrixFactory(
      final SectorNameProvider sectorNames
      ) {
      this.sectorNames = Preconditions.checkNotNull(sectorNames.asList());
   }
   
   @Override
   public InputOutputMatrix createInputOutputMatrix() {
      final int
         dimension = sectorNames.size();
      final double[][]
         data = new double[dimension][dimension];
      for (int i = 0; i < dimension; ++i)
         data[i][i] = 1.;
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
