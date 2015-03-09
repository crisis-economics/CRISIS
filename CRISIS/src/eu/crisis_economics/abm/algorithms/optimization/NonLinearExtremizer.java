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
package eu.crisis_economics.abm.algorithms.optimization;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.exception.NullArgumentException;

public abstract class NonLinearExtremizer {
   public final static class ImmutableExtremumResult {
      double[]
         startingCoordinate,
         extremalCoordinate,
         derivativeAtExtremum;
      double
         extremalEvalutation,
         normOfDerivativeAtExtremum;
      
      private ImmutableExtremumResult() { }; // Immutable
      
      /** Get the opimisation seed coordinate. */
      public double[] getStartingCoordinate() {
         return startingCoordinate.clone();
      }
      
      /** Get the coordinate result of the optimisation. */
      public double[] getExtremumCoordinate() {
         return extremalCoordinate.clone();
      }
      
      /** Get the derivative of the merit function at the extremum. */
      public double[] getDerivativeVectorAtExtremum() {
         return derivativeAtExtremum.clone();
      }
      
      /** Get the evaluation of the merit function at the extremum. */
      public double getMeritFunctionAtExtremum() {
         return extremalEvalutation;
      }
      
      /** Get the (Euclidean) norm of the derivative at the extremum. */
      public double getNormOfDerivativeAtExtremum() {
         return normOfDerivativeAtExtremum;
      }
      
      @Override
      public String toString() {
         String result = "";
         final int n = startingCoordinate.length;
         for(int i = 0; i< n; ++i)
            result += String.format("x[%d] = %16.10g\n", i, extremalCoordinate[i]);
         result += String.format("evaluation at extremum:         %16.10g\n", 
            getMeritFunctionAtExtremum());
         result += String.format("L2 derivative norm at extremum: %16.10g\n",
            getNormOfDerivativeAtExtremum());
         return result;
      }
   }
   
   protected final ImmutableExtremumResult createImmutableExtremumResult(
      double[] startingCoordinate,
      double[] extremalCoordinate,
      double[] derivativeAtExtremum,
      double extremalEvalutation
      ) {
      if(startingCoordinate == null ||
         extremalCoordinate == null ||
         derivativeAtExtremum == null)
         throw new NullArgumentException();
      if(Double.isNaN(extremalEvalutation))
         throw new IllegalArgumentException();
      final int n = startingCoordinate.length;
      if(n == 0 ||
         extremalCoordinate.length != n ||
         derivativeAtExtremum.length != n)
         throw new IllegalArgumentException();
      ImmutableExtremumResult result = new ImmutableExtremumResult();
      result.startingCoordinate = startingCoordinate.clone();
      result.extremalCoordinate = extremalCoordinate.clone();
      result.derivativeAtExtremum = derivativeAtExtremum.clone();
      result.extremalEvalutation = extremalEvalutation;
      result.normOfDerivativeAtExtremum = (new ArrayRealVector(derivativeAtExtremum)).getNorm();
      return result;
   }
   
   public interface OnceDifferentiableMeritFunction {
      public double evaluateAt(double[] x);
      public double[] partialDerivativesAt(double[] x);
   }
   
   private OnceDifferentiableMeritFunction meritFunction;
   
   private double solutionToleranceDemand;
   
   public NonLinearExtremizer(
         OnceDifferentiableMeritFunction meritFunction,
      double solutionToleranceDemand
      ) {
      if(meritFunction == null)
         throw new NullArgumentException();
      if(solutionToleranceDemand <= 0.)
         throw new IllegalArgumentException(
            "NonLinearExtremizer: solution tolerance (value" + solutionToleranceDemand + ")" +
            " is negative or zero.");
      this.meritFunction = meritFunction;
      this.solutionToleranceDemand = solutionToleranceDemand;
   }
   
   /** Perform the optimisation. Returns null if the merit function cannot be optimised. */
   abstract public ImmutableExtremumResult performOptimization(double[] startingCoordinate);
   
   /** Get the merit function for this optimisation. */
   public final OnceDifferentiableMeritFunction getMeritFunction() {
      return meritFunction;
   }
   
   /** Get the acceptable solution tolerance (epsilon) for this optimisation. */
   public final double getSolutionToleranceDemand() {
      return solutionToleranceDemand;
   }
}
