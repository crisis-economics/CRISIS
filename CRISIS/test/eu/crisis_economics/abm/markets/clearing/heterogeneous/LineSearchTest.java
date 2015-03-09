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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.Random;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.junit.Assert;
import org.junit.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import eu.crisis_economics.abm.algorithms.optimization.BrentLineSearch;
import eu.crisis_economics.abm.algorithms.optimization.BrentLineSearch.LineSearchResult;

public class LineSearchTest {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing Brent line search minimizer..");
   }
   
   @Test
   /**
     * Test the Brent line search algorithm by minimizing a one-dimensional
     * cosine function. The starting point of the minimization is the origin,
     * and the maximum distance to travel in the positive x direction is 2*Pi.
     * The expected solution is therefor the local minimum at Pi.
     */
   public void testOneDimensionalCosMinimization() {
      final MultivariateFunction meritFunction = new MultivariateFunction() {
         @Override
         public double value(final double[] x) {
            return Math.cos(x[0]);
         }
      };
      final double[] lineDirection = new double[] { 1.0 };
      double distanceToTravel = Math.PI * 2.;
      final LineSearchResult solution = BrentLineSearch.doLineSearch(
         meritFunction, new double[] { 0.0 }, lineDirection, distanceToTravel);
      Assert.assertTrue(Math.abs(solution.getEvaluationAtSolution() + 1.) < 1.e-10);
      Assert.assertEquals(solution.getSolutionPoint()[0], Math.PI, 1.e-10);
   }
   
   @Test
   /**
     * Test the Brent line search algorithm by minimizing a multidimensional
     * quadratic scalar function. This test generates many random quadratic 
     * fields with random global minima. In each case, the starting point
     * of the minimization is random and the line search direction points 
     * toward the global minimum.
     */
   public void batchTestMultidimensionalQuadraticLineSearch() {
      final Random dice = new Random();
      for(int i = 0; i< 100; ++i) {     // 100 random quadratic minimization tests
         final int numDimensions = dice.nextInt(20) + 2;
         final double[]
            globalMinimum = new double[numDimensions],
            startingPoint = new double[numDimensions];
         for(int j = 0; j< globalMinimum.length; ++j) {
            globalMinimum[j] = dice.nextDouble() * 10. - 5.;
            startingPoint[j] = dice.nextDouble() * 10. - 5.;
         }
         testMultiDimensionalQuadraticMinimization(
            numDimensions,
            startingPoint,
            globalMinimum
            );
      }
   }
   
   private void testMultiDimensionalQuadraticMinimization(
      final int numberOfDimensions,
      final double[] startingPoint,
      final double[] locationOfGlobalMinimum
      ) {
      final MultivariateFunction meritFunction = new MultivariateFunction() {
         @Override
         public double value(final double[] x) {
            double result = 0.;
            for(int i = 0; i< numberOfDimensions; ++i)
               result += (x[i] - locationOfGlobalMinimum[i]) * (x[i] - locationOfGlobalMinimum[i]);
            return result;
         }
      };
      final double[] lineDirection = new double[startingPoint.length];
      double distanceToTravel = 0.;
      for(int i = 0; i< lineDirection.length; ++i) {
         lineDirection[i] = locationOfGlobalMinimum[i] - startingPoint[i];
         distanceToTravel += lineDirection[i] * lineDirection[i];
      }
      distanceToTravel = Math.sqrt(distanceToTravel);
      final LineSearchResult solution = BrentLineSearch.doLineSearch(
         meritFunction, startingPoint, lineDirection, distanceToTravel + 1.);
      Assert.assertTrue(Math.abs(solution.getEvaluationAtSolution()) < 1.e-10);
      for(int i = 0; i< startingPoint.length; ++i)
         Assert.assertEquals(solution.getSolutionPoint()[i], locationOfGlobalMinimum[i], 1.e-10);
   }
   
   @Test
   /**
     * Test the Brent line search algorithm by minimizing a negative
     * multidimensional quadratic scalar function, with maximum at the
     * origin. This test generated many random boundary boxes (limits
     * inside of which to perform line searches) and, in each case, 
     * extremizes the quadratic function along a random direction starting
     * at the origin. It is expected that the solution lies on the domain
     * bounds in each case.
     */
   public void batchTestBoundedDecreasingMultidimensionalSearch() {
      final Random dice = new Random();
      for(int i = 0; i< 100; ++i) {     // 100 random tests
         final int numDimensions = dice.nextInt(20) + 2;
         final double[]
            lowerDomainBounds = new double[numDimensions],
            upperDomainBounds = new double[numDimensions];
         for(int j = 0; j< numDimensions; ++j) {
            lowerDomainBounds[j] = -dice.nextDouble();
            upperDomainBounds[j] = +dice.nextDouble();
         }
         testOptimizationWithDomainBounds(
            numDimensions,
            lowerDomainBounds,
            upperDomainBounds
            );
      }
   }
   
   private void testOptimizationWithDomainBounds(
      final int numberOfDimensions,
      final double[] lowerDomainBounds,
      final double[] upperDomainBounds
      ) {
      final MultivariateFunction meritFunction = new MultivariateFunction() {
         @Override
         public double value(final double[] x) {
            double result = 0.;
            for(int i = 0; i< numberOfDimensions; ++i)
               result += -x[i] * x[i];
            return result;
         }
      };
      Random dice = new Random();
      double[]
         startingPoint = new double[numberOfDimensions],
         vectorDirection = new double[numberOfDimensions];
      for(int i = 0; i< numberOfDimensions; ++i)
         vectorDirection[i] = dice.nextDouble() - .5;
      LineSearchResult solution = BrentLineSearch.doLineSearch(
         meritFunction,
         startingPoint,
         vectorDirection,
         upperDomainBounds,
         lowerDomainBounds
         );
      boolean
         solutionIsOnDomainBoundary = false;
      for(int i = 0; i< numberOfDimensions; ++i) {
         final double
            solutionPoint = solution.getSolutionPoint()[i];
         System.out.printf(
            "Coordinate %4d: distance from lower bound: %16.10g, from upper bound: %16.10g\n",
            i, solutionPoint - lowerDomainBounds[i], upperDomainBounds[i] - solutionPoint);
         if(Math.abs(solutionPoint - upperDomainBounds[i]) < 1.e-8 ||
            Math.abs(solutionPoint - lowerDomainBounds[i]) < 1.e-8)
            solutionIsOnDomainBoundary = true;
      }
      Assert.assertTrue(solutionIsOnDomainBoundary);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("Brent line search minimizer tests pass.");
   }
   
   /**
     * Manual entry point.
     */
   static public void main(String[] args) {
      final LineSearchTest test = new LineSearchTest();
      test.setUp();
      test.testOneDimensionalCosMinimization();
      test.batchTestMultidimensionalQuadraticLineSearch();
      test.batchTestBoundedDecreasingMultidimensionalSearch();
      test.tearDown();
   }
}
