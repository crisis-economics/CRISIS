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
package eu.crisis_economics.abm.algorithms.statistics;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
  * Unit tests for the class {@link FloorInterpolator}.
  * 
  * @author phillips
  */
public class TestFloorInterpolator {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + getClass().getSimpleName() + "...");
   }
   
   /**
     * Test whether an instance of {@link FloorInterpolator} interpolates
     * a simple discrete input series as expected. This unit test operates
     * as follows:<br><br>
     * 
     * {@code (a)}
     *    A short discrete subsequence of the function {@code f(T) = T**2}
     *    is generated;<br>
     * {@code (b)}
     *    An instance of {@link FloorInterpolator} is used to create a
     *   {@link UnivariateFunction}, {@code U}, from this sequence;<br>
     * {@code (c)}
     *    {@code U} is sampled repeatedly for a number of points in the domain
     *    of the input sequence. The results of this operation are compared
     *    to correct, expected outputs.
     */
   @Test
   public void testFloorInterpolatorOutput() {
      final double[]
         x = new double[] { 2., 3.,  4.,  5.,  6., },
         y = new double[] { 4., 9., 16., 25., 36., };
      final double[]
         expectedResults = {
           -Double.MAX_VALUE, 4.000000000,
            Double.MIN_VALUE, 4.000000000,
            1.000000000,      4.000000000,
            1.111111111,      4.000000000,
            1.222222222,      4.000000000,
            1.333333333,      4.000000000,
            1.444444444,      4.000000000,
            1.555555556,      4.000000000,
            1.666666667,      4.000000000,
            1.777777778,      4.000000000,
            1.888888889,      4.000000000,
            2.000000000,      4.000000000,
            2.111111111,      4.000000000,
            2.222222222,      4.000000000,
            2.333333333,      4.000000000,
            2.444444444,      4.000000000,
            2.555555556,      4.000000000,
            2.666666667,      4.000000000,
            2.777777778,      4.000000000,
            2.888888889,      4.000000000,
            3.000000000,      9.000000000,
            3.111111111,      9.000000000,
            3.222222222,      9.000000000,
            3.333333333,      9.000000000,
            3.444444444,      9.000000000,
            3.555555556,      9.000000000,
            3.666666667,      9.000000000,
            3.777777778,      9.000000000,
            3.888888889,      9.000000000,
            4.000000000,      16.00000000,
            4.111111111,      16.00000000,
            4.222222222,      16.00000000,
            4.333333333,      16.00000000,
            4.444444444,      16.00000000,
            4.555555556,      16.00000000,
            4.666666667,      16.00000000,
            4.777777778,      16.00000000,
            4.888888889,      16.00000000,
            5.000000000,      25.00000000,
            5.111111111,      25.00000000,
            5.222222222,      25.00000000,
            5.333333333,      25.00000000,
            5.444444444,      25.00000000,
            5.555555556,      25.00000000,
            5.666666667,      25.00000000,
            5.777777778,      25.00000000,
            5.888888889,      25.00000000,
            6.000000000,      36.00000000,
            6.111111111,      36.00000000,
            6.222222222,      36.00000000,
            6.333333333,      36.00000000,
            6.444444444,      36.00000000,
            6.555555556,      36.00000000,
            6.666666667,      36.00000000,
            6.777777778,      36.00000000,
            6.888888889,      36.00000000,
            7.000000000,      36.00000000,
            7.111111111,      36.00000000,
            7.222222222,      36.00000000,
            7.333333333,      36.00000000,
            7.444444444,      36.00000000,
            7.555555556,      36.00000000,
            7.666666667,      36.00000000,
            7.777777778,      36.00000000,
            7.888888889,      36.00000000,
            8.000000000,      36.00000000,
            8.111111111,      36.00000000,
            8.222222222,      36.00000000,
            8.333333333,      36.00000000,
            8.444444444,      36.00000000,
            8.555555556,      36.00000000,
            8.666666667,      36.00000000,
            8.777777778,      36.00000000,
            8.888888889,      36.00000000,
            9.000000000,      36.00000000,
            9.111111111,      36.00000000,
            9.222222222,      36.00000000,
            9.333333333,      36.00000000,
            9.444444444,      36.00000000,
            9.555555556,      36.00000000,
            9.666666667,      36.00000000,
            9.777777778,      36.00000000,
            9.888888889,      36.00000000,
            10.00000000,      36.00000000,
            10.11111111,      36.00000000,
            10.22222222,      36.00000000,
            10.33333333,      36.00000000,
            10.44444444,      36.00000000,
            10.55555556,      36.00000000,
            10.66666667,      36.00000000,
            10.77777778,      36.00000000,
            10.88888889,      36.00000000,
            11.00000000,      36.00000000,
            11.11111111,      36.00000000,
            11.22222222,      36.00000000,
            11.33333333,      36.00000000,
            11.44444444,      36.00000000,
            11.55555556,      36.00000000,
            11.66666667,      36.00000000,
            11.77777778,      36.00000000,
            11.88888889,      36.00000000,
            12.00000000,      36.00000000,
            Double.MAX_VALUE, 36.00000000 
            };
      final UnivariateInterpolator
         interpolator = new FloorInterpolator();
      final UnivariateFunction
         f = interpolator.interpolate(x, y);
      for(int i = 0; i< expectedResults.length; i += 2) {
         final double
            t = expectedResults[i],
            f_t_Observed = f.value(t),
            f_t_Expected = expectedResults[i+1];
         System.out.printf(
            "t: %16.10g observed: %16.10g expected: %16.10g\n",
            t, f_t_Observed, f_t_Expected);
         Assert.assertEquals(f_t_Observed, f_t_Expected, 1.e-12);
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
         final TestFloorInterpolator
            test = new TestFloorInterpolator();
         test.setUp();
         test.testFloorInterpolatorOutput();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
