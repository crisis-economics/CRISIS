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

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
  * A unit test structure for the {@link ChainedInputOutputMatrixFactory} class.
  * 
  * @author phillips
  */
public class TestChainedInputOutputMatrixFactory {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "..");
   }
   
   @Test
   /**
     * This unit test creates an {@link InputOutputMatrix} using the 
     * {@link ChainedInputOutputMatrixFactory} factory. The resulting
     * matrix values are tested against predicted values.
     */
   public void testChainedInputOutputMatrixFactory() {
      { // 1 x 1 matrix case
         final List<String>
            sectorNames = Arrays.asList("A");
         final InputOutputMatrixFactory
            factory = new ChainedInputOutputMatrixFactory(
               new ManualSectorNameProvider(sectorNames), 2);
         final InputOutputMatrix
            matrix = factory.createInputOutputMatrix();
         System.out.println(matrix);
         final double[][]
            expectedResult = new double[][] { { 1.0 } };
         for(int i = 0; i< 1; ++i)
            for(int j = 0; j< 1; ++j)
               Assert.assertEquals(matrix.getEntry(i, j), expectedResult[i][j], 1.e-12);
         }
      
      { // 3 x 3 matrix case
      final List<String>
         sectorNames = Arrays.asList("A", "B", "C");
      final InputOutputMatrixFactory
         factory = new ChainedInputOutputMatrixFactory(
            new ManualSectorNameProvider(sectorNames), 2);
      final InputOutputMatrix
         matrix = factory.createInputOutputMatrix();
      System.out.println(matrix);
      final double[][]
         expectedResult = new double[][] {
            { .5, 0., .5 },
            { 0., 1., 0. },
            { .5, 0., .5 }
            };
      for(int i = 0; i< 3; ++i)
         for(int j = 0; j< 3; ++j)
            Assert.assertEquals(matrix.getEntry(i, j), expectedResult[i][j], 1.e-12);
      }
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println(getClass().getSimpleName() + " tests pass.");
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         final TestChainedInputOutputMatrixFactory
            test = new TestChainedInputOutputMatrixFactory();
         test.setUp();
         test.testChainedInputOutputMatrixFactory();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
