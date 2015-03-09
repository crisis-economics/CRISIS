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

import com.google.common.base.Preconditions;

/**
  * A stopping condition for MCN clearing algrithms. This stopping 
  * condition will indicate termination if and only if:
  *   (a) the network residual is less than, or equal to, a fixed 
  *       value, or
  *   (b) the number of clearing iterations (calls to isStoppingConditionSatisfied)
  *       has reached a fixed value.
  * @author phillips
  */
final class OrderOrIterationsStoppingCondition implements
   MixedClearingNetworkAlgorithmStoppingCondition {
   
   private TargetResidualOrMaximumIterationsStoppingCondition stoppingCondition;
   private double targetOrderOfMagnitudeReductionInResidual;
   
   public OrderOrIterationsStoppingCondition(
      final double targetOrderOfMagnitudeReductionInResidual,
      final int maximumNumberOfIterations,
      final MixedClearingNetwork network
      ) {
      Preconditions.checkArgument(targetOrderOfMagnitudeReductionInResidual > 0.);
      Preconditions.checkArgument(maximumNumberOfIterations > 0);
      Preconditions.checkNotNull(network);
      this.targetOrderOfMagnitudeReductionInResidual = targetOrderOfMagnitudeReductionInResidual;
      this.stoppingCondition = new TargetResidualOrMaximumIterationsStoppingCondition(
         0., maximumNumberOfIterations);
   }
   
   @Override
   public boolean isStoppingConditionSatisfied(final MixedClearingNetwork network) {
      if(stoppingCondition.getNumberOfIterationsElapsed() == 0)
         stoppingCondition.setTargetNetworkResidual(
            network.getResidualCost()/Math.pow(10., targetOrderOfMagnitudeReductionInResidual));
      return stoppingCondition.isStoppingConditionSatisfied(network);
   }
   
   /**
     * Get the target network residual.
     */
   public double getTargetNetworkResidual() {
      return stoppingCondition.getTargetNetworkResidual();
   }
   
   /**
     * Get the maximum number of network iterations;
     */
   public int getMaximumNumberOfIterations() {
      return stoppingCondition.getMaximumNumberOfIterations();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "OrderOfMagnitudeResidualReductionOrMaximumIterationsStoppingCondition, "
           + "target network residual: " + getTargetNetworkResidual() + ", "
           + "maximum number of network iterations: " + getMaximumNumberOfIterations() + ".";
   }
}
