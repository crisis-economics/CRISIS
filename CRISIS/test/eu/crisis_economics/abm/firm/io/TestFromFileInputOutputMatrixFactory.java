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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
  * A unit test structure for the {@link FromFileInputOutputMatrixFactory} class.
  * 
  * @author phillips
  */
public class TestFromFileInputOutputMatrixFactory {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "..");
   }
   
   /**
     * This unit test creates an {@link InputOutputMatrix} using the 
     * {@link FromFileInputOutputMatrixFactory} factory. The resulting
     * matrix values are tested against predicted values.<br><br>
     * 
     * This unit tests creates a small local text file with relative
     * path <code>"./from-file-input-output-table-test.dat"</code>.
     * This text file contains a small, correctly formatted input-output
     * matrix for use with the factory class {@link FromFileInputOutputMatrixFactory}.
     * <br><br>
     * 
     * After this file has been created, this test creates an instance 
     * of {@link FromFileInputOutputMatrixFactory} using the newly created
     * file as an input argument. It is then asserted that the resulting
     * matrix has the expected values.
     */
   @Test
   public void testFromFileInputOutputMatrixFactory() {
      final String[]
         expectedSectorNames = new String[] {
            "\"A\"",
            "\"B\"",
            "\"C\""
         };
      final double[][]
         matrixToSerialize = new double[][] {
            { 1., 2., 3. },
            { 2., 3., 4. },
            { 3., 4., 5. }
         };
      final String
         filename = "./from-file-input-output-table-test.dat";
      try {
         final File
            file = new File(filename);
         if(!file.exists())
            file.createNewFile();
         final FileWriter
            stream = new FileWriter(file.getAbsoluteFile());
         final BufferedWriter
            writer = new BufferedWriter(stream);
         for(int i = 0; i< expectedSectorNames.length; ++i)
            writer.write(expectedSectorNames[i] + "\n");
         for(int i = 0; i< matrixToSerialize.length; ++i) {
            for(int j = 0; j< matrixToSerialize.length; ++j)
               writer.write(String.format("%g ", matrixToSerialize[i][j]));
            writer.write('\n');
         }
         writer.close();
         stream.close();
      } catch (final IOException e) {
         Assert.fail();
      }
      
      final FromFileInputOutputMatrixFactory
         factory = new FromFileInputOutputMatrixFactory(filename);
      final InputOutputMatrix
         matrix = factory.createInputOutputMatrix();
      System.out.println(matrix);
      
      double[][]
         expectedResult = new double[][] {
            { 1./6.,  2./9.,  3./12. },
            { 2./6.,  3./9.,  4./12. },
            { 3./6.,  4./9.,  5./12. }
         };
      
      for(int i = 0; i< expectedResult.length; ++i)
         for(int j = 0; j< expectedResult.length; ++j)
            Assert.assertEquals(expectedResult[i][j], matrix.getEntry(i, j), 1.e-12);
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
         final TestFromFileInputOutputMatrixFactory
            test = new TestFromFileInputOutputMatrixFactory();
         test.setUp();
         test.testFromFileInputOutputMatrixFactory();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
