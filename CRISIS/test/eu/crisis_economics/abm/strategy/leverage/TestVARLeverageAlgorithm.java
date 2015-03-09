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
package eu.crisis_economics.abm.strategy.leverage;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

public class TestVARLeverageAlgorithm {
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing " + this.getClass().getCanonicalName() + "..");
   }
   
   /** The following python script reproduces the results of this test.
    * 
    * import numpy
    * from numpy import *
    * # Parameters
    * numFirms = 10
    * d = 0.002
    *
    * # Moving stock returns and covariance matrix
    * lastCov = zeros( (numFirms, numFirms) )
    * lastMV = zeros( (numFirms, 1) )
    *
    * movingReturns = zeros( (numFirms, 1) )
    * movingCov = zeros( (numFirms, numFirms) )
    *
    * # Compute the (adaptive) covariance matrix
    * # nextMV - vector of current market values.
    * def computeCovMatrix(nextMV):
    *   global movingReturns
    *   global movingCov
    *   logReturns = log(nextMV / lastMV)
    *   print logReturns
    *   movingReturns = d * logReturns + (1. - d) * movingReturns
    *   excess = (logReturns - movingReturns)
    *   movingCov = d * excess * excess.transpose() + (1. - d) * lastCov
    *
    * # Compute sigma
    * def computeStdDeviation(stockWeights):
    * sigma = stockWeights.transpose() * movingCov * stockWeights
    * loanWeight = 1. - sum(stockWeights)
    * return math.sqrt(sigma * (1. + loanWeight * loanWeight))
    *
    * # Test
    * lastMV = matrix([ 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 3.0 ]).transpose()
    * nextMV = matrix([ 3.1, 3.0, 2.9, 2.8, 2.7, 2.6, 2.5, 2.4, 2.3, 2.2 ]).transpose()
    *
    * wS = matrix(
    * [ 0.09263169, 0.08565675, 0.04081051, 0.03642706, 0.20540501, 
    *   0.10388996, 0.19336965,   0.112198, 0.03217309, 0.09743826]).transpose()
    *
    * computeCovMatrix(nextMV)
    * sigma = computeStdDeviation(wS)
    */
   @Test
   public void assertVARConstraintNumericalResults() {
      List<Double> lastMarketValues = Arrays.asList(
         new Double[] { 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 3.0 });
      List<Double> newMarketValues = Arrays.asList(
         new Double[] { 3.1, 3.0, 2.9, 2.8, 2.7, 2.6, 2.5, 2.4, 2.3, 2.2 });
      double[] firmStockWeights = 
         new double[] {
            0.09263169027660921, 0.08565675449244271, 0.0408105094539671, 
            0.03642706388527581, 0.20540500687189617, 0.10388995808479616,
            0.19336965468276385, 0.11219800472108199, 0.03217309470071595, 
            0.0974382628304512 };
      DenseDoubleMatrix1D meanLogStockReturns = new DenseDoubleMatrix1D(10);
      DenseDoubleMatrix2D meanCovMatrix = new DenseDoubleMatrix2D(10, 10);
      
      final double stdDeviationGained =
         ValueAtRiskLeverageTargetAlgorithm.CalculatePortfolioStandardDeviation(
         10, newMarketValues, lastMarketValues, firmStockWeights, 
         0.002, meanLogStockReturns, meanCovMatrix);
      
      // Verify the (adaptive) stock covariance matrix.
      {
      double[][] expectedMovingCovMatrix = 
         {{  3.02153360e-04,  2.40623445e-04,  1.79835617e-04,  1.19592707e-04,  5.97076787e-05,
             0.00000000e+00, -5.97076787e-05, -1.19592707e-04, -1.79835617e-04, -2.40623445e-04 },
          {  2.40623445e-04,  1.91623360e-04,  1.43214246e-04,  9.52390838e-05,  4.75489246e-05,
             0.00000000e+00, -4.75489246e-05, -9.52390838e-05, -1.43214246e-04, -1.91623360e-04 },
          {  1.79835617e-04,  1.43214246e-04,  1.07034551e-04,  7.11791796e-05,  3.55368123e-05, 
             0.00000000e+00, -3.55368123e-05, -7.11791796e-05, -1.07034551e-04, -1.43214246e-04 },
          {  1.19592707e-04,  9.52390838e-05,  7.11791796e-05,  4.73349548e-05,  2.36323798e-05,
             0.00000000e+00, -2.36323798e-05, -4.73349548e-05, -7.11791796e-05, -9.52390838e-05 },
          {  5.97076787e-05,  4.75489246e-05,  3.55368123e-05,  2.36323798e-05,  1.17986671e-05,
             0.00000000e+00, -1.17986671e-05, -2.36323798e-05, -3.55368123e-05, -4.75489246e-05 },
          {  0.00000000e+00,  0.00000000e+00,  0.00000000e+00,  0.00000000e+00,  0.00000000e+00,
             0.00000000e+00,  0.00000000e+00,  0.00000000e+00,  0.00000000e+00,  0.00000000e+00 },
          { -5.97076787e-05, -4.75489246e-05, -3.55368123e-05, -2.36323798e-05, -1.17986671e-05,
             0.00000000e+00,  1.17986671e-05,  2.36323798e-05,  3.55368123e-05,  4.75489246e-05 },
          { -1.19592707e-04, -9.52390838e-05, -7.11791796e-05, -4.73349548e-05, -2.36323798e-05,
             0.00000000e+00,  2.36323798e-05,  4.73349548e-05,  7.11791796e-05,  9.52390838e-05 },
          { -1.79835617e-04, -1.43214246e-04, -1.07034551e-04, -7.11791796e-05, -3.55368123e-05, 
             0.00000000e+00,  3.55368123e-05,  7.11791796e-05,  1.07034551e-04,  1.43214246e-04 },
          { -2.40623445e-04, -1.91623360e-04, -1.43214246e-04, -9.52390838e-05, -4.75489246e-05, 
             0.00000000e+00,  4.75489246e-05,  9.52390838e-05,  1.43214246e-04,  1.91623360e-04 }};
      
      for(int i = 0; i< 10; ++i)
         for(int j = 0; j< 10; ++j) {
            final double
               expected = expectedMovingCovMatrix[i][j],
               gained = meanCovMatrix.get(i, j);
            if(expected != 0.)
               Assert.assertEquals(Math.abs(gained/expected - 1.), 0, 1.e-8);
            else
               Assert.assertEquals(gained, 0., 0.);
         }
      }
      
      final double stdDeviationExpected = 0.00105648058459;
      
      // Verify the portfolio variance.
      Assert.assertEquals(stdDeviationGained, stdDeviationExpected, 1.e-5);
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println(this.getClass().getCanonicalName() + " Tests Pass.");
   }
   
   /*
    * Manual entry point.
    */
   static public void main(final String[] args) {
      try {
         TestVARLeverageAlgorithm test = new TestVARLeverageAlgorithm();
         test.setUp();
         test.assertVARConstraintNumericalResults();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
