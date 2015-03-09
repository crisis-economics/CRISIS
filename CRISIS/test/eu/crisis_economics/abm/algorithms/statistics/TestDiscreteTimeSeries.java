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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.utilities.Pair;

/**
  * Test structure for DiscreteTimeSeries
  * @author phillips
  */
public class TestDiscreteTimeSeries {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing TestDiscreteTimeSeries..");
   }
   
   @Test
   /**
     * Test basic features of the TestDiscreteTimeSeries type, including
     * its internal sorting order.
     */
   public void testDirecteTimeSeriesBasicOperations() {
      DiscreteTimeSeries series = new DiscreteTimeSeries();
      final int numberOfSamples = 10;
      Random dice = new Random(117);
      List<Pair<Double, Double>> expected = new ArrayList<Pair<Double,Double>>();
      for(int i = 0; i< numberOfSamples; ++i) {
         Pair<Double, Double> randomEntry = Pair.create((double)i, dice.nextGaussian());
         series.put(randomEntry);
         expected.add(randomEntry);
      }
      Assert.assertEquals(series.size(), expected.size());
      final List<Pair<Double, Double>> gained = series.asList();
      for(int i = 0; i< numberOfSamples; ++i) {
         double
            local = expected.get(i).getFirst(),
            external = gained.get(i).getFirst();
         Assert.assertEquals(local, external, 0.);
         local = expected.get(i).getSecond();
         external = gained.get(i).getSecond();
         Assert.assertEquals(local, external, 0.); 
      }
      @SuppressWarnings("unused")
      DiscreteTimeSeries test = series.headMap(1.);
      test = series.tailMap(9., true);
      test = series.headMap(1., true);
      test = series.subMap(1., true, 9., false);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("TestDiscreteTimeSeries tests pass.");
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      TestDiscreteTimeSeries test = new TestDiscreteTimeSeries();
      test.setUp();
      test.testDirecteTimeSeriesBasicOperations();
      test.tearDown();
   }
}
