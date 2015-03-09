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

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;

import com.google.common.base.Preconditions;

/**
  * An implementation of the Levenberg-Marquardt SSQ fitting algorithm
  * for mixed network clearing.
  * 
  * @author phillips
  */
final class LevenbergMarquardtClearingAlgorithm 
   extends NumericalDerivativeClearingAlgorithm {
   
   private final int
      maximumIterations,
      maximumEvaluations;
   private final double
      absErrorTarget,
      relErrorTarget;
   
   public LevenbergMarquardtClearingAlgorithm( // Immutable
      final int maximumIterations,
      final int maximumEvaluations,
      final double absErrorTarget,
      final double relErrorTarget
      ) { 
      this.maximumIterations = maximumIterations;
      this.maximumEvaluations = maximumEvaluations;
      this.absErrorTarget = absErrorTarget;
      this.relErrorTarget = relErrorTarget;
   }
   
   @Override
   public double applyToNetwork(final MixedClearingNetwork network) {
      Preconditions.checkNotNull(network);
      
      final VectorCostFunction function = super.getVectorCostFunction(network);
      final MultivariateJacobianFunction model = 
         LeastSquaresFactory.model(function, super.getJacobianMatrixFunction(network));
      final RealVector
         observed = new ArrayRealVector(super.calculateTarget(network)),
         start = new ArrayRealVector(network.getNumberOfEdges());
      for(int i = 0; i< network.getNumberOfEdges(); ++i)
         start.setEntry(i, network.getEdges().get(i).getMaximumRateAdmissibleByBothParties());
      start.set(1.0);
      
      final ConvergenceChecker<LeastSquaresProblem.Evaluation> evaluationChecker =
         LeastSquaresFactory.evaluationChecker(
            new SimpleVectorValueChecker(relErrorTarget, absErrorTarget));
      final LeastSquaresProblem problem =
         LeastSquaresFactory.create(
            model, observed, start, evaluationChecker, maximumEvaluations, maximumIterations);
      
      final LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
      final Optimum result = optimizer.optimize(problem);
      
      final double residualCost = result.getRMS();
      System.out.println("Network cleared: residual cost: " + residualCost + ".");
      
      return residualCost;
   }
   
   /**
     * @return The maximum number of Levenberg iterations.
     */
   public int getMaximumIterations() {
      return maximumIterations;
   }

   /**
    * @return The maximum number of network cost evaluations.
    */
   public int getMaximumEvaluations() {
      return maximumEvaluations;
   }

   /**
    * @return The absolute residual error threshold target.
    */
   public double getAbsErrorTarget() {
      return absErrorTarget;
   }

   /**
    * @return The relative residual error threshold target.
    */
   public double getRelErrorTarget() {
      return relErrorTarget;
   }
   /*
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and as such should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "Levenberg Marquardt Clearing Algorithm, maximum iterations:"
            + maximumIterations + ", maximum network cost evaluations:" + maximumEvaluations
            + ", absolute error target:" + absErrorTarget + ", relative error target:"
            + relErrorTarget + ".";
   }
}
