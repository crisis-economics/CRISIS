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
import java.util.Random;

import org.apache.commons.math3.linear.MatrixUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * An implementation of the {@link InputOutputMatrixFactory} interface. This implementation
  * generates an {@link InputOutputMatrix} using a random number generator whose seed is
  * customizable.
  * 
  * @author phillips
  */
public final class RandomInputOutputMatrixFactory implements InputOutputMatrixFactory {
   
   private List<String>
      sectorNames;
   private long
      seed;
   
   /**
     * Create a {@link RandomInputOutputMatrixFactory} object with custom parameters.
     * 
     * @param sectorNames
     *        A {@link List} of sector names. The length of this collection specifies
     *        the dimension of the random square {@link InputOutputMatrix} to generate.
     * @param seed
     *        A random number seed used to populate a random {@link InputOutputMatrix}.
     *        A new random number generator is created, using this seed, every time
     *        a new {@link InputOutputMatrix} is created using this factory.
     */
   @Inject
   public RandomInputOutputMatrixFactory(
      final SectorNameProvider sectorNames,
   @Named("RANDOM_INPUT_OUTPUT_MATRIX_FACTORY_SEED")
      final long seed
      ) {
      this.sectorNames = Preconditions.checkNotNull(sectorNames.asList());
      this.seed = seed;
   }
   
   @Override
   public InputOutputMatrix createInputOutputMatrix() {
      final Random dice = new Random(seed);
      final int dimension = sectorNames.size();
      final double[][] data = new double[dimension][dimension];
      for (int i = 0; i < dimension; ++i)
         for (int j = 0; j < dimension; ++j)
            data[i][j] = dice.nextDouble();
      return new InputOutputMatrix(
         MatrixUtils.createRealMatrix(data), sectorNames);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Random Input-Output Matrix Factory";
   }
}
