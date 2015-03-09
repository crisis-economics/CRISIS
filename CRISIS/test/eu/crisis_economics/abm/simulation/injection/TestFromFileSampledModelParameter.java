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
package eu.crisis_economics.abm.simulation.injection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.model.parameters.AbstractFromFileParameter;
import eu.crisis_economics.abm.model.parameters.FromFileDoubleParameter;
import eu.crisis_economics.abm.model.parameters.FromFileTimeseriesParameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * Lightweight unit tests for the class {@link AbstractFromFileParameter}.
  * 
  * @author phillips
  */
public class TestFromFileSampledModelParameter {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "...");
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void sample(
      ModelParameter<Double> parameter,
      final double[] expectedX,
      final double[] expectedY
      ) {
      final double
         time = Simulation.getTime();
      final int
         cycle = (int) Simulation.getCycleIndex();
      final double
         yRequired = (cycle >= expectedX[expectedX.length - 1]) ? 
            expectedY[expectedY.length - 1] : 
               (cycle < expectedX[0] ? expectedY[0] : expectedY[(int)(cycle - expectedX[0])]),
         yObserved = parameter.get();
      System.out.printf(
         "T: %16.10g expected: %16.10g: obtained: %16.10g\n",
         Simulation.getTime(), yRequired, yObserved
         );
      Assert.assertEquals(yRequired, yObserved, 1.e-10);
   }
   
   /**
     * Test whether an instance of {@link FromFileTimeseriesParameter} correctly
     * reads, and correctly reports, a timeseries from a datafile. This unit test 
     * operates as follows:<br><br>
     * 
     * {@code (a)}
     *    A temporary file {@code F}, named {@code "./paramter-test.dat"} is created 
     *    in the local directory;<br>
     * {@code (b)}
     *    {@code F} is populated with a short discrete subsequence from the expression
     *    {@code f(T) = T**2};<br>
     * {@code (c)}
     *    {@code F} is parsed by an instance of {@link FromFileTimeseriesParameter};<br>
     * {@code (d)}
     *    An {@link EmptySimulation} is run for {@code 10} cycles. At each cycle,
     *    it is asserted that the {@link ModelParameter} yields the same expression
     *    {@code f(T)} as is indicated by {@code F}.<br><br>
     *    
     * For convenience {@code F} is not deleted at the end of the above session.
     */
   @Test
   public void testReadModelParameterSamplesFromFile() {
      final double[]
         expectedSamples = new double[] { 2., 3., 5., 7., 11. };
      final String
         fileName = "./sampled-parameter-test.dat";
      try {
         final File
            file = new File(fileName);
         if(!file.exists())
            file.createNewFile();
         final FileWriter
            stream = new FileWriter(file.getAbsoluteFile());
         final BufferedWriter
            writer = new BufferedWriter(stream);
         for(int i = 0; i< expectedSamples.length; ++i)
            writer.write(expectedSamples[i] + "\n");
         writer.close();
         stream.close();
      } catch (final IOException e) {
         Assert.fail();
      }
      ModelParameter<Double>
         parameter = null;
      try {
         parameter = new FromFileDoubleParameter(fileName, "[name]");
      } catch (final IOException e) {
         Assert.fail();
      }
      for(int i = 0; i< expectedSamples.length; ++i) {
         final double
            observed = parameter.get(),
            expected = expectedSamples[i];
         System.out.printf("observed: %16.10g expected: %16.10g\n",
            observed, expected);
         Assert.assertEquals(observed, expected, 0.);
      }
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println(getClass().getSimpleName() + " tests pass.");
   }
   
   /*
    * Manual entry point.
    */
   static public void main(final String[] args) {
      try {
         final TestFromFileSampledModelParameter
            test = new TestFromFileSampledModelParameter();
         test.setUp();
         test.testReadModelParameterSamplesFromFile();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
