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
package eu.crisis_economics.abm.algorithms.statistics;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
  * Unit tests for the {@link DiscreteTimeSeriesExponentialSmoother} 
  * algorithm.
  * 
  * @author phillips
  */
public class DiscreteTimeSeriesExponentialSmootherTest {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing DiscreteTimeSeriesExponentialSmootherTest..");
   }
   
   /**
     * This unit test is as follows:<br><br>
     * 
     * (a) a timeseries, with a known exponentially smoothed form,
     *     is created. The timestep interval between each record
     *     in the input timeseries is 1.0;<br>
     * (b) a {@link DiscreteTimeSeriesExponentialSmoother} algorithm
     *     is applied to this stimulus;<br>
     * (c) it is asserted that the resulting smoothed timeseries 
     *     corresponds with the expected result to within 12 significant
     *     figures.<br>
     */
   @Test
   private void testExponentialTimeseriesSmoothingWithEqualSampleSpacings() {
      double[]
         inputSeries = {
            1., 2., 3., 2., 1., 0., -1., -5., -10., 100.},
         expectedSmoothedSeries = {
            1.00000000000, 1.10000000000, 1.29000000000, 1.36100000000,
            1.32490000000, 1.19241000000, 0.973169000000, 0.375852100000,
            -0.661733110000, 9.40444020100
            };
      DiscreteTimeSeries series = new DiscreteTimeSeries();
      for(int i = 0; i< inputSeries.length; ++i)
         series.put((double) i, inputSeries[i]);
      DiscreteTimeSeriesExponentialSmoother algorithm =
         new DiscreteTimeSeriesExponentialSmoother(0.1);
      DiscreteTimeSeries result = algorithm.applyTo(series);
      for(int i = 0; i< inputSeries.length; ++i) {
         final double
            T = (double) i;
         System.out.printf(
            "input: T: %16.10g V(T): %16.10g smoothed: %16.10g expected: %16.10g\n",
            T, series.get(T), result.get(T), expectedSmoothedSeries[i]);
         
         Assert.assertEquals(
            result.get(T), 
            expectedSmoothedSeries[i],
            Math.abs(expectedSmoothedSeries[i]) * 1.e-12
            );
      }
   }
   
   /**
    * This unit test is as follows:<br><br>
    * 
    * (a) a timeseries, with a known exponentially smoothed form,
    *     is created. The timestep interval between each record
    *     in the input timeseries is 2.0;<br>
    * (b) a {@link DiscreteTimeSeriesExponentialSmoother} algorithm
    *     is applied to this stimulus;<br>
    * (c) it is asserted that the resulting smoothed timeseries 
    *     corresponds with the expected result to within 12 significant
    *     figures.<br>
    */
   @Test
   private void testExponentialTimeseriesSmoothingWithUnequalSampleSpacings() {
      double[]
         inputSeries = {1., 3., 6.},
         expectedSmoothedSeries = {1.0, 1.2, 1.68};
      DiscreteTimeSeries series = new DiscreteTimeSeries();
      for(int i = 0; i< inputSeries.length; ++i)
         series.put((double) i, inputSeries[i]);
      DiscreteTimeSeriesExponentialSmoother algorithm =
         new DiscreteTimeSeriesExponentialSmoother(0.1);
      DiscreteTimeSeries result = algorithm.applyTo(series);
      for(int i = 0; i< inputSeries.length; ++i) {
         final double
            T = (double) i;
         System.out.printf(
            "input: T: %16.10g V(T): %16.10g smoothed: %16.10g expected: %16.10g\n",
            T, series.get(T), result.get(T), expectedSmoothedSeries[i]);
         
         Assert.assertEquals(
            result.get(T), 
            expectedSmoothedSeries[i],
            Math.abs(expectedSmoothedSeries[i]) * 1.e-12
            );
      }
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("DiscreteTimeSeriesExponentialSmootherTest tests pass.");
   }
   
   /*
    * Manual entry point.
    */
   public static void main(String[] args) {
      try {
         final DiscreteTimeSeriesExponentialSmootherTest test =
            new DiscreteTimeSeriesExponentialSmootherTest();
         test.setUp();
         test.testExponentialTimeseriesSmoothingWithEqualSampleSpacings();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
      
      try {
         final DiscreteTimeSeriesExponentialSmootherTest test =
            new DiscreteTimeSeriesExponentialSmootherTest();
         test.setUp();
         test.testExponentialTimeseriesSmoothingWithUnequalSampleSpacings();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
