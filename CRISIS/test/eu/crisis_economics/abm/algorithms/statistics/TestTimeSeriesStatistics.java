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

import eu.crisis_economics.utilities.Pair;

/**
  * Tests for timeseries statistics.
  * @author phillips
  */
public class TestTimeSeriesStatistics {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing TimeSeriesStatistics..");
   }
   
   @Test
   /**
     * Compute the mean of a two-element timeseries.
     */
   private void testPrecomputedMeanOfTimeSeriesResult() {
      DiscreteTimeSeries input = new DiscreteTimeSeries();
      input.put(Pair.create(1., 3.));
      input.put(Pair.create(2., 4.));
      testMeanOfDiscreteTimeSeries(input, 7./2.);
   }
   
   @Test
   /**
     * Compute the mean of an extended timeseries with irregular sampling.
     */
   private void testPrecomputedMeanOfExtendedTimeSeriesResult() {
      DiscreteTimeSeries input = new DiscreteTimeSeries();
      input.put(Pair.create(1., 3.));
      input.put(Pair.create(2., 4.));
      input.put(Pair.create(4., 6.));
      testMeanOfDiscreteTimeSeries(input, 9./2.);
   }
   
   @Test
   /**
     * Compute the variance of a two-element timeseries.
     */
   private void testPrecomputedVarianceOfTimeSeriesResult() {
      DiscreteTimeSeries input = new DiscreteTimeSeries();
      input.put(Pair.create(1., 3.));
      input.put(Pair.create(2., 4.));
      testVarianceOfDiscreteTimeSeries(input, 1./12.);
   }
   
   @Test
   /**
     * Compute the variance of an extended timeseries with irregular sampling.
     */
   private void testPrecomputedVarianceOfExtendedTimeSeriesResult() {
      DiscreteTimeSeries input = new DiscreteTimeSeries();
      input.put(Pair.create(1., 3.));
      input.put(Pair.create(2., 4.));
      input.put(Pair.create(4., 6.));
      testVarianceOfDiscreteTimeSeries(input, 3./4.);
   }
   
   private void testMeanOfDiscreteTimeSeries(
      final DiscreteTimeSeries series,
      final double expectedMean
      ) {
      final DiscreteTimeSeriesStatistic statistic = new MeanOfTimeSeriesStatistic();
      final double value = statistic.measureStatistic(series);
      Assert.assertEquals(value, expectedMean, 1.e-10);
   }
   
   private void testVarianceOfDiscreteTimeSeries(
      final DiscreteTimeSeries series,
      final double expectedVariance
      ) {
      final DiscreteTimeSeriesStatistic statistic = new VarianceOfTimeSeriesStatistic();
      final double value = statistic.measureStatistic(series);
      Assert.assertEquals(value, expectedVariance, 1.e-10);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("TimeSeriesStatistics tests pass.");
   }
   
   /*
    * Manual entry point.
    */
   public static void main(String[] args) {
      TestTimeSeriesStatistics test = new TestTimeSeriesStatistics();
      test.setUp();
      test.testPrecomputedMeanOfTimeSeriesResult();
      test.testPrecomputedMeanOfExtendedTimeSeriesResult();
      test.testPrecomputedVarianceOfTimeSeriesResult();
      test.testPrecomputedVarianceOfExtendedTimeSeriesResult();
      test.tearDown();
   }
}
