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
package eu.crisis_economics.abm.algorithms.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.testng.Assert;

/**
  * A base class for {@link RandomSeries} unit tests.
  * 
  * @author phillips
  */
abstract class TestAbstractSeries {
   
   /**
     * Assert that:
     * 
     * <ul>
     *   <li> The long term observed mean for the {@link RandomSeries} in the first
     *        argument is as expected.
     *   <li> The long term standard deviation for the {@link RandomSeries} in the first
     *        argument is as expected.
     * </ul>
     * 
     * @param series (<code>S</code>) <br>
     *        The {@link RandomSeries} object to test.
     * @param numberOfSamples <br>
     *        The number of samples to draw from <code>S</code>.
     * @param expectedLongTermMean <br>
     *        The expected long term mean of the series.
     * @param expectedLongTermStd <br>
     *        The expected long term standard deviation of the series.
     * @param expectedInitialValue <br>
     *        The initial value of the series.
     */
   protected void assertLongTermMomentsAndInitialValue(
      RandomSeries series,
      final int numberOfSamples,
      final double expectedLongTermMean,
      final double expectedLongTermStd,
      final double expectedInitialValue
      ) {
      assertInitialValue(series, expectedInitialValue);
      assertLongTermMean(series, numberOfSamples, expectedLongTermMean);
      assertLongTermStd(series, numberOfSamples, expectedLongTermStd);
   }
   
   /**
     * Assert that the initial value drawn from a {@link RandomSeries} object
     * is as expected.
     * 
     * @param series <br>
     *        The {@link RandomSeries} from which to draw the initial value.
     * @param expectedInitialValue <br>
     *        The expected initial value to draw.
     */
   protected void assertInitialValue(
      RandomSeries series,
      final double expectedInitialValue
      ) {
      Assert.assertEquals(series.next(), expectedInitialValue, 1.e-14);
   }
   
   /**
     * Assert that the long term mean of values drawn from a {@link RandomSeries} object
     * is as expected.
     * 
     * @param series (<code>S</code>) <br>
     *        The {@link RandomSeries} object to test.
     * @param numberOfSamples <br>
     *        The number of samples to draw from <code>S</code>.
     * @param expectedLongTermMean <br>
     *        The expected long term mean of the series.
     */
   protected void assertLongTermMean(
      RandomSeries series,
      final int numberOfSamples,
      final double expectedLongTermMean
      ) {
      final Mean
         mean = new Mean();
      
      for(int i = 0; i< numberOfSamples; ++i)
         mean.increment(series.next());
      
      Assert.assertEquals(mean.getResult(), expectedLongTermMean, 1.e-2);
   }
   
   /**
     * Assert that the long term mean of values drawn from a {@link RandomSeries} object
     * is as expected.
     * 
     * @param series (<code>S</code>) <br>
     *        The {@link RandomSeries} object to test.
     * @param numberOfSamples <br>
     *        The number of samples to draw from <code>S</code>.
     * @param expectedLongTermStd <br>
     *        The expected long term standard deviation of the series.
     */
   protected void assertLongTermStd(
      RandomSeries series,
      final int numberOfSamples,
      final double expectedLongTermStd
      ) {
      final List<Double>
         observations = new ArrayList<Double>();
      
      for(int i = 0; i< numberOfSamples; ++i)
         observations.add(series.next());
      
      final double observedMean = 
         (new StandardDeviation()).evaluate(ArrayUtils.toPrimitive(
            observations.toArray(new Double[observations.size()])));
      
      Assert.assertEquals(observedMean, expectedLongTermStd, 1.e-1);
   }
   
   protected static interface RandomSeriesFactory {
      RandomSeries createFactory(long seed);
   }
   
   /**
     * Assert that two {@link RandomSeries} objects created with the same seed have
     * the same output series (for <code>1000</code> samples).
     * 
     * @param factory
     *        A {@link RandomSeriesFactory} object which creates an instance of a
     *        {@link RandomSeries} from a seed {@link Long}.
     */
   protected void assertReproducibility(
      final RandomSeriesFactory factory) {
      final RandomSeries
         first = factory.createFactory(123L),
         second = factory.createFactory(123L);
      for(int i = 0; i< 1000; ++i)
         Assert.assertEquals(first.next(), second.next(), 0.);
   }
}
