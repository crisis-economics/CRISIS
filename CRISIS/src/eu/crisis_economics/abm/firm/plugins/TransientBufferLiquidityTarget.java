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
package eu.crisis_economics.abm.firm.plugins;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.simulation.Simulation;

/**
  * An (absolute) liquidity target algorithm which encourages a liquidity
  * buffer during the simulation transient period.
  * @author phillips
  */
public final class TransientBufferLiquidityTarget
   extends FirmDecisionRule {
   
   private double
      longTermLiquidityTarget,
      startingLiquidityTarget,
      exponent,
      anticipatedTransientDuration;
   
   /**
     * Create a {@link TransientBufferLiquidityTarget} object.<br><br>
     * 
     * This {@link FirmDecisionRule} specifies the {@link Firm} liquidity
     * target as a function of time. The liquidity target, L(t), as a function
     * of time, t, is defined as follows:<br><br>
     * 
     * <center><code>
     * L(t) = L0 + (Lf - L0) * (t / t_Transient)**a if t <= t_Transient; 
     *        Lf, otherwise.
     * </code></center><br><br>
     * 
     * Where L0 is the initial {@link Firm} liquidity target, Lf is the 
     * final {@link Firm} liquidity target, and t_Transient is the time at
     * which L(t > t_Transient) = Lf should apply.
     * 
     * @param startingLiquidityTarget
     *        The starting firm liquidity target (L0).
     * @param longTermLiquidityTarget
     *        The final firm liquidity target (Lf).
     * @param exponent
     *        The value of {@code a} in the above formula.
     * @param anticipatedTransientDuration
     *        The duration over which to vary L(t), eg. the anticipated 
     *        duration of the transient.
     */
   @Inject
   public TransientBufferLiquidityTarget(
   @Named("TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_STARTING_LIQUIDITY_TARGET")
      final double startingLiquidityTarget,
   @Named("TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_FINAL_LIQUIDITY_TARGET")
      final double longTermLiquidityTarget,
   @Named("TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_EXPONENT")
      final double exponent,
   @Named("TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_TRANSIENT_TIMEFRAME")
      final double anticipatedTransientDuration
      ) {
      super(initGuard(startingLiquidityTarget));
      this.longTermLiquidityTarget = longTermLiquidityTarget;
      this.startingLiquidityTarget = startingLiquidityTarget;
      this.exponent = exponent;
      this.anticipatedTransientDuration = anticipatedTransientDuration;
   }
   
   static private double initGuard(double initialValue) {
      if(initialValue < 0.)
      throw new IllegalArgumentException(
         "TransientBufferLiquidityTarget: liquidity target is negative.");
      return initialValue;
   }
   
   @Override
   public double computeNext(FirmState state) {
      final double simulationTime = Simulation.getSimState().schedule.getTime();
      double nextLiquidityTarget;
      if(simulationTime <= anticipatedTransientDuration)
         nextLiquidityTarget =
            startingLiquidityTarget +
            (longTermLiquidityTarget - startingLiquidityTarget) * 
               Math.pow(simulationTime/anticipatedTransientDuration, exponent);
      else nextLiquidityTarget = longTermLiquidityTarget;
      super.recordNewValue(nextLiquidityTarget);
      return nextLiquidityTarget;
   }
    
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "TransientBufferLiquidityTarget algorithm, long term liqudity target: " +
         longTermLiquidityTarget + ", anticipated transient duration: " + 
         anticipatedTransientDuration + ".";
   }
}
