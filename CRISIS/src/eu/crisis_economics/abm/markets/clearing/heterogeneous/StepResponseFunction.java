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

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.analysis.function.StepFunction;

/**
  * A response function combining:
  *   (a) an existing response function R, with 
  *   (b) a discontinuous step function S.
  * The value of this combined response function is R(p)*S(p)
  * at any point p. The step function S is specified as a 
  * sequence of step coordinates x(i). At each x(i), the value
  * of S(p) decreases in equal fractions of the unit interval.
  * This class is used by heterogeneous clearing unit tests,
  * and should retain default privacy.
  */
final class StepResponseFunction
   extends AbstractBoundedUnivariateFunction {
   
   private StepFunction stepFunction;
   private BoundedUnivariateFunction existingResponseFunction;
   
   public StepResponseFunction(
      final BoundedUnivariateFunction existingResponseFunction,
      final double[] stepCoordinates
      ) {
      super(new SimpleDomainBounds(0., existingResponseFunction.getMaximumInDomain()));
      initCommon(existingResponseFunction, stepCoordinates);
   }
   
   /**
     * Generate a step function with discontinuities at 
     * random points in the unit interval.
     */
   public StepResponseFunction(
      final BoundedUnivariateFunction existingResponseFunction) {
      super(new SimpleDomainBounds(0., existingResponseFunction.getMaximumInDomain()));
      Random random = new Random();
      int numSteps = random.nextInt(10) + 2;
      double[] stepCoordinates = new double[numSteps];
      for(int i = 0; i< numSteps; ++i)
         stepCoordinates[i] = random.nextDouble() / existingResponseFunction.getMaximumInDomain();
      Arrays.sort(stepCoordinates);
      initCommon(existingResponseFunction, stepCoordinates);
   }
   
   private void initCommon(
      final BoundedUnivariateFunction existingResponseFunction,
      final double[] stepCoordinates
      ) {
      double[] stepYCoordinates = new double[stepCoordinates.length];
      for(int i = 0; i< stepYCoordinates.length; ++i)
         stepYCoordinates[i] = 1.-i/(double)stepYCoordinates.length;
      this.stepFunction = new StepFunction(stepCoordinates, stepYCoordinates);
      this.existingResponseFunction = existingResponseFunction;
   }
   
   @Override
   public double value(double rate) {
      if(rate >= super.getMaximumInDomain()) return 0.;
      else if(rate < 0.)
         throw new IllegalArgumentException(
            "StepResponseFunction.value: argument " + rate + " is out of bounds. ");
      return existingResponseFunction.value(rate) * stepFunction.value(rate);
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return
         "Heterogeneous clearing stepped response function.";
   }
}
