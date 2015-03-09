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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import com.google.common.base.Preconditions;

import eu.crisis_economics.utilities.ArrayUtil;

final class BoundedQuadraticEstimationClearingAlgorithm 
   extends NumericalDerivativeClearingAlgorithm {
   
   private final int
      maximumEvaluations;
   
   public BoundedQuadraticEstimationClearingAlgorithm( // Immutable
      final int maximumIterations,
      final int maximumEvaluations,
      final double absErrorTarget,
      final double relErrorTarget
      ) { 
      this.maximumEvaluations = maximumEvaluations;
   }
   
   @Override
   public double applyToNetwork(final MixedClearingNetwork network) {
      Preconditions.checkNotNull(network);
      final int dimension = network.getNumberOfEdges();
      
      final ResidualCostFunction aggregateCostFunction =
         super.getResidualScalarCostFunction(network);
      final RealVector
         start = new ArrayRealVector(network.getNumberOfEdges());
      start.set(1.0);                                       // Initial rate guess.
      
      final BOBYQAOptimizer optimizer = new BOBYQAOptimizer(2*dimension + 1, 1.2, 1.e-8);
      final PointValuePair result = optimizer.optimize(
         new MaxEval(maximumEvaluations),
         new ObjectiveFunction(aggregateCostFunction),
         GoalType.MINIMIZE,
         new SimpleBounds(new double[dimension], ArrayUtil.ones(dimension)),
         new InitialGuess(start.toArray())
         );
      
      final double residualCost = result.getValue();
      System.out.println("Network cleared: residual cost: " + residualCost + ".");
      
      return residualCost;
   }
   
   /**
     * @return The maximum number of network cost evaluations.
     */
   public int getMaximumEvaluations() {
      return maximumEvaluations;
   }
   
   /*
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and as such should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "Powell Quadratic Estimation Clearing Algorithm.";
   }
}
