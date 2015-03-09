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
final class TargetResidualOrMaximumIterationsStoppingCondition extends
   AbstractIterativeClearingAlgorithmStoppingCondition {
   
   private double targetNetworkResidual;
   private int maximumNumberOfIterations;
   
   public TargetResidualOrMaximumIterationsStoppingCondition(
      final double targetNetworkResidual,
      final int maximumNumberOfIterations
      ) {
      Preconditions.checkArgument(targetNetworkResidual >= 0.);
      Preconditions.checkArgument(maximumNumberOfIterations > 0);
      this.targetNetworkResidual = targetNetworkResidual;
      this.maximumNumberOfIterations = maximumNumberOfIterations;
   }
   
   @Override
   public boolean isStoppingConditionSatisfied(final MixedClearingNetwork network) {
      super.incrementNumberOfIterations();
      return (network.getResidualCost() <= targetNetworkResidual) || 
             (super.getNumberOfIterationsElapsed() >= maximumNumberOfIterations);
   }
   
   /**
     * Get the target network residual.
     */
   public double getTargetNetworkResidual() {
      return targetNetworkResidual;
   }
   
   void setTargetNetworkResidual(final double value) {
      this.targetNetworkResidual = value;
   }
   
   /**
     * Get the maximum number of network iterations;
     */
   public int getMaximumNumberOfIterations() {
      return maximumNumberOfIterations;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "TargetResidualOrMaximumIterationsStoppingCondition, "
           + "target network residual: " + getTargetNetworkResidual() + ", "
           + "maximum number of network iterations: " + getMaximumNumberOfIterations() + ".";
   }
}
