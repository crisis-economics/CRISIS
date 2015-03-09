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

package eu.crisis_economics.abm.bank.bankruptcy;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;

/**
  * A measurement algorithm which accepts, as input, (a) a CAR (Capital
  * Adequacy Ratio) target, (b) a RWA sum (Risk Weighted Assets) measurement
  * algorithm and (c) an {@link Agent}, and returns the equity change that
  * would be required of the {@link Agent} in order to satisfy the stated
  * CAR Target.
  * 
  * @author phillips
  */
public final class ComputeCARBankruptcyResolutionAmountOperation
   extends SimpleAbstactAgentOperation<Double> {
   
   private double
      CARTarget;
   private AgentOperation<Double>
      riskWeightedAssetsMeasurement;
   
   /**
     * Create a {@link ComputeCARBankruptcyResolutionAmountOperation} 
     * {@link Agent} measurement object.<br><br>
     * 
     * This algorithm computes the equity change that would be required of
     * an {@link Agent} in order to satisfy the specified CAR target after
     * such a change to its equity.
     * 
     * @param CARTarget
     *        The Capital Adequacy Ration (CAR) target to apply. This 
     *        argument should be non-negative.
     * @param riskWeightedAssetsMeasurement
     *        The measurement used to compute the {@link Agent} RWA sum.
     */
   @Inject
   public ComputeCARBankruptcyResolutionAmountOperation(
   @Named("BANK_BANKRUPTCY_RESOLUTION_CAR_TARGET")
      final double CARTarget,
   @Named("BANK_BANKRUPTCY_RESOLUTION_RWA_SUM_MEASUREMENT")
      final AgentOperation<Double> riskWeightedAssetsMeasurement
      ) {
      Preconditions.checkArgument(CARTarget >= 0.);
      Preconditions.checkNotNull(riskWeightedAssetsMeasurement);
      this.CARTarget = CARTarget;
      this.riskWeightedAssetsMeasurement = riskWeightedAssetsMeasurement;
   }
   
   @Override
   public Double operateOn(Agent agent) {
      double
         capital = agent.getEquity(),
         rwaMeasurement = agent.accept(riskWeightedAssetsMeasurement),
         targetCapital = CARTarget * rwaMeasurement;
      
      if(rwaMeasurement == 0)
         targetCapital = 1;                  // for RWA=0 we just need to push above zero, e.g. 1
      
      return (targetCapital - capital);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Compute CAR Bankruptcy Resolution Amount Operation, CAR Target:"
            + CARTarget + ", RWA measurement: "
            + riskWeightedAssetsMeasurement + ".";
   }
}
