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
package eu.crisis_economics.abm.agent;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.Agent;

/**
  * Compute the Capital Adequacy Ratio (CAR) for an {@link Agent}. This algorithm
  * allows you to specify a custom Risk-Weighted Assets (RWA) measurement.<br><br>
  * 
  * The CAR measurement can be positive or negative, depending on the sign of the
  * {@link Agent} equity.<br><br>
  * 
  * If the {@link Agent} risk-weighted assets (RWA) = 0, the CAR measurement is
  * as follows:<br>
  * RWA = 0 && equity > 0: CAR = +Infinity<br>
  * RWA = 0 && equity < 0: CAR = -Infinity<br>
  * RWA = 0 && equity = 0: CAR = NaN.
  */
public final class ComputeCapitalAdequacyRatioOperation
   extends SimpleAbstactAgentOperation<Double> {
   
   private final AgentOperation<Double>
      riskWeightedAssetsComputation;
   
   /**
     * Create a {@link ComputeCapitalAdequacyRatioOperation} {@link AgentOperation}
     * using a custom risk-weighted assets (RWA) measurement algorithm.
     */
   public ComputeCapitalAdequacyRatioOperation(
      final AgentOperation<Double> riskWeightedAssetsComputation
      ) {
      Preconditions.checkNotNull(riskWeightedAssetsComputation);
      this.riskWeightedAssetsComputation = riskWeightedAssetsComputation;
   }
   
   /**
     * Compute, and return, the Capital Adequacy Ratio.
     */
   @Override
   public Double operateOn(final Agent agent) {
      final double
         capital = agent.getEquity(),
         riskWeightedAssets = agent.accept(riskWeightedAssetsComputation);
      return capital / riskWeightedAssets;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Compute Capital Adequacy Ratio Algorithm.";
   }
}
