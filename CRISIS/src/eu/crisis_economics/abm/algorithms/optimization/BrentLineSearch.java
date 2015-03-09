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

import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import com.google.common.base.Preconditions;

import eu.crisis_economics.utilities.StateVerifier;

/**
  * Univariate line search minimization using Brent optimization.
  * @author phillips
  */
public final class BrentLineSearch {
   
   static final private double
      DEFAULT_ERROR_TARGET_RELATIVE = 1.e-10,
      DEFAULT_ERROR_TARGET_ABSOLUTE = 1.e-14;
   static final private int
      DEFAULT_MAXIMUM_EVALUATIONS = 100;
   
   /**
     * Treat a combination of:
     *   (a) a scalar-valued multivariate function,
     *   (b) a search direction (L), and
     *   (c) a starting point (P),
     * as a scalar-valued univariate function whole sole argument is 
     * the distance along the line search direction L starting at P.
     * 
     * @author phillips
     */
   static public final class LineSearchObjectiveFunction implements UnivariateFunction {
      private MultivariateFunction
         function;
      
      private double[]
         normalizedDirection,
         startingPoint,
         workspace;
      
      LineSearchObjectiveFunction(
         final MultivariateFunction function,
         final double[] startingPoint,
         final double[] vectorDirection
         ) {
         StateVerifier.checkNotNull(function, startingPoint, vectorDirection);
         Preconditions.checkArgument(startingPoint.length > 0);
         Preconditions.checkArgument(startingPoint.length == vectorDirection.length);
         this.function = function;
         this.normalizedDirection = new double[vectorDirection.length];
         { // Normalize the input vector.
            double norm = 0.;
            for(int i = 0; i< vectorDirection.length; ++i)
               norm += vectorDirection[i] * vectorDirection[i];
            norm = Math.sqrt(norm);
            for(int i = 0; i< vectorDirection.length; ++i)
               normalizedDirection[i] = vectorDirection[i] / norm;
         }
         this.startingPoint = Arrays.copyOf(startingPoint, startingPoint.length);
         this.workspace = new double[startingPoint.length];
      }
      
      @Override
      public double value(final double x) {
         for(int i = 0; i< startingPoint.length; ++i)
            workspace[i] = startingPoint[i] + x * normalizedDirection[i];
         return function.value(workspace);
      }
   }
   
   /**
     * The result of a line search operation. This class is immutable.
     * @author phillips
     */
   static final public class LineSearchResult { // Immutable
      private double[] solutionVector;
      private double evaluationAtSolution;
      
      LineSearchResult(
         final double[] solutionVector,
         final double evaluationAtSolution
         ) {
         this.solutionVector = solutionVector;
         this.evaluationAtSolution = evaluationAtSolution;
      }
      
      /**
        * @return The solution obtained from a line search operation.
        */
      public double[] getSolutionPoint() {
         return Arrays.copyOf(solutionVector, solutionVector.length);
      }
      
      /**
        * @return The evaluation of the merit function at the solution.
        */
      public double getEvaluationAtSolution() {
         return evaluationAtSolution;
      }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
      @Override
      public String toString() {
         return "LineSearchResult, solution vector: "
               + Arrays.toString(solutionVector) + ", evaluation at solution: "
               + evaluationAtSolution + ".";
      }
   }
   
   /**
     * Perform a line search minimization. This function accepts as input:
     *   (a) a starting point (a vector),
     *   (b) a direction in which to travel (a vector),
     *   (c) limits on the total distance to travel along (b).
     *   
     * With these inputs the function attempts to find the minimum of a
     * scalar-valued multivariate function along the line starting at 
     * (a) and pointing in the direction of (b).
     * 
     * @param function
     *        A scalar-valued multivariate function to minimize,
     * @param startingPoint
     *        A vector starting point from which to begin the minimization (P),
     * @param vectorDirection
     *        A vector direction along which to travel from P, (V)
     * @param maximumDistanceToTravel
     *        The maximum distance to travel in the direction of V,
     * @param maximumEvaluations
     *        The maximum number of function evaluations to identify the minimum,
     * @param relativeErrorGoal
     *        The relative error target of the minimization,
     * @param absoluteErrorGoal
     *        The absolute error target of the minimization.
     * @return
     *        A lightweight immutable struct containing the vector solution and
     *        the evaluation of the function at this point.
     */
   static public LineSearchResult doLineSearch(
      final MultivariateFunction function,
      final double[] startingPoint,
      final double[] vectorDirection,
      final double maximumDistanceToTravel,
      final int maximumEvaluations,
      final double relativeErrorGoal,
      final double absoluteErrorGoal
      ) {
      Preconditions.checkArgument(maximumEvaluations > 0);
      Preconditions.checkArgument(relativeErrorGoal > 0. || absoluteErrorGoal > 0.);
      Preconditions.checkArgument(maximumDistanceToTravel > 0.);
      final LineSearchObjectiveFunction lineSearcher =
         new LineSearchObjectiveFunction(function, startingPoint, vectorDirection);
      final UnivariateOptimizer optimizer =
         new BrentOptimizer(relativeErrorGoal, absoluteErrorGoal);
      UnivariatePointValuePair result = optimizer.optimize(
         new MaxEval(maximumEvaluations),
         new UnivariateObjectiveFunction(lineSearcher),
         GoalType.MINIMIZE,
         new SearchInterval(0, maximumDistanceToTravel, 0)
         );
      final double[] vectorSolution = new double[startingPoint.length];
      for(int i = 0; i< vectorDirection.length; ++i)
         vectorSolution[i] = lineSearcher.startingPoint[i] + 
            lineSearcher.normalizedDirection[i] * result.getPoint();
      final LineSearchResult solution = 
         new LineSearchResult(vectorSolution, result.getValue());
      return solution;
   }
   
   static public LineSearchResult doLineSearch(
      final MultivariateFunction function,
      final double[] startingPoint,
      final double[] vectorDirection,
      final double maximumDistanceToTravel
      ) {
      return doLineSearch(
         function, startingPoint, vectorDirection, maximumDistanceToTravel,
         DEFAULT_MAXIMUM_EVALUATIONS,
         DEFAULT_ERROR_TARGET_RELATIVE,
         DEFAULT_ERROR_TARGET_ABSOLUTE
         );
   }
   
   /**
     * Compute the maximum distance a line search may travel whilst
     * still remaining within the coordinate box specified by a pair
     * of (upper/lower) domain bounds.
     * 
     * @param startingPoint (P)
     *        The starting point (root) of a line search.
     * @param vectorDirection (V)
     *        The vector direction of the line search.
     * @param domainMaxima
     *        Upper bounds for each coordinate to test during the line
     *        search.
     * @param domainMinima
     *        Lower bounds for each coordinate to test during the line
     *        search.
     * @return
     *        The maximum distance along the *normalized* direction V
     *        that a line search may travel, starting at P, such that
     *        no coordinates are outside of the domain bounds.
     */
   static private double computeMaximumSearchDistanceFromDomainBounds(
      final double[] startingPoint,
      final double[] vectorDirection,
      final double[] domainMaxima,
      final double[] domainMinima
      ) {
      // Check whether the starting point is inside the boundary domain.
      for(int i = 0; i< startingPoint.length; ++i) {
         Preconditions.checkArgument(startingPoint[i] >= domainMinima[i]);
         Preconditions.checkArgument(startingPoint[i] <= domainMaxima[i]);
      }
      
      // Normalize the line search direction.
      double[] normalizedSearchDirection = new double[startingPoint.length];
      double norm = 0.;
      for(int i = 0; i< vectorDirection.length; ++i)
         norm += vectorDirection[i] * vectorDirection[i];
      norm = Math.sqrt(norm);
      if(norm == 0) return 0.;
      double maximumDistanceToTravel = Double.MAX_VALUE;
      
      // Identify the limiting step size for each coordinate direction.
      for(int i = 0; i< vectorDirection.length; ++i) {
         normalizedSearchDirection[i] = vectorDirection[i] / norm;
         double distanceLimitForThisCoordinate = 0.;
         if(normalizedSearchDirection[i] == 0) continue;
         else if(normalizedSearchDirection[i] > 0.)
            distanceLimitForThisCoordinate =
               (domainMaxima[i] - startingPoint[i]) / normalizedSearchDirection[i];
         else
            distanceLimitForThisCoordinate = 
               -(startingPoint[i] - domainMinima[i]) / normalizedSearchDirection[i];
         maximumDistanceToTravel =
            Math.min(maximumDistanceToTravel, distanceLimitForThisCoordinate);
      }
      return maximumDistanceToTravel;
   }

   static public LineSearchResult doLineSearch(
      final MultivariateFunction function,
      final double[] startingPoint,
      final double[] vectorDirection,
      final double[] domainMaxima,
      final double[] domainMinima
      ) {
      final double
         maximumDistanceToTravel = computeMaximumSearchDistanceFromDomainBounds(
            startingPoint, vectorDirection, domainMaxima, domainMinima);
      if(maximumDistanceToTravel == 0.)
         return new LineSearchResult(
            Arrays.copyOf(startingPoint, startingPoint.length), function.value(startingPoint));
      return doLineSearch(
         function, startingPoint, vectorDirection, maximumDistanceToTravel,
         DEFAULT_MAXIMUM_EVALUATIONS,
         DEFAULT_ERROR_TARGET_RELATIVE,
         DEFAULT_ERROR_TARGET_ABSOLUTE
         );
   }
}
