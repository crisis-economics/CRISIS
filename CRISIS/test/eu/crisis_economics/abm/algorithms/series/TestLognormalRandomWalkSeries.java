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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
  * A unit test for the {@link LognormalMeanRevertingRandomWalkSeries} class.
  * 
  * @author phillips
  */
public class TestLognormalRandomWalkSeries extends TestAbstractSeries {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing.. " + LognormalMeanRevertingRandomWalkSeries.class.getSimpleName());
   }
   
   /**
     * Test basic features of the {@link LognormalMeanRevertingRandomWalkSeries} type, including:
     * 
     * <ul>
     *   <li> The long term mean of the series.
     *   <li> The long term standard deviation of the series.
     *   <li> Saturation (upper/lower bounds) of the series.
     *   <li> The reproducibility of the timeseries given a fixed random seed.
     * </ul>
     */
   @Test
   public void testLognormalRandomWalkSeries() {
      { // Reproducibility
         super.assertReproducibility(
            new RandomSeriesFactory() {
               @Override
               public RandomSeries createFactory(long seed) {
                  return new LognormalMeanRevertingRandomWalkSeries(
                     10.0, 1.0, 8.0, 12.0, seed);
               }
            }
         );
      }
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println(
         LognormalMeanRevertingRandomWalkSeries.class.getSimpleName() + " tests pass.");
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         final TestLognormalRandomWalkSeries
            test = new TestLognormalRandomWalkSeries();
         test.setUp();
         test.testLognormalRandomWalkSeries();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
