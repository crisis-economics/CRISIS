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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A {@link FirmTargetProductionAlgorithm} which follows trends in 
  * aggregate market demand. For the following terms:<br><br>
  * 
  * (a) D = aggregate market demand for goods in the last market session,<br>
  * (b) G = the {@link Firm} production grace factor, and<br>
  * (c) M = the rate at which the {@link Firm} responds to changes in the 
  *         market,<br><br>
  * 
  * The target production, T, in terms of the previous production target, T', 
  * is defined to be:<br><br>
  * 
  * T = T' + (D - T'*(1 - G)) * M * U[0, 1),<br><br>
  * 
  * where U[0, 1) is a random double in the range [0, 1).
  * 
  * @author phillips
  */
public final class FollowAggregateMarketDemandTargetProductionAlgorithm
   extends FirmDecisionRule {
   
   private double
      graceFactor,
      adaptionRate;
   
   private SimpleGoodsMarket
      goodsMarket;
   
   /**
     * Create a {@link FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration} object.
     * @param initialTargetProduction
     *        The initial {@link Firm} target production quantity. This
     *        argument should be non-negative.
     * @param graceFactor
     *        The {@link Firm} target production grace. This parameter
     *        should be in the range [0, 1].
     * @param adaptionRate
     *        The rate at which the {@link Firm} adapts to changes in aggregate
     *        market demand. This parameter should be in the range [0, 1].
     * @param goodsMarket
     *        The market on which the {@link Firm} sells its goods.
     */
   @Inject
   public FollowAggregateMarketDemandTargetProductionAlgorithm(
   @Named("FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_INITIAL_TARGET")
      final double initialTargetProduction,
   @Named("FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_GRACE_FACTOR")
      final double graceFactor,
   @Named("FOLLOW_MARKET_DEMAND_TARGET_PRODUCTION_ALGORITHM_MARKET_ADAPTATION_RATE")
      final double adaptionRate,
      final SimpleGoodsMarket goodsMarket
      ) {
      super(initialTargetProduction);
      Preconditions.checkArgument(initialTargetProduction >= 0.,
         "Follow Aggregate Market Demand Target Production Algorithm: initial target production "
       + "quantity is negative.");
      Preconditions.checkArgument(graceFactor >= 0. && graceFactor <= 1.0,
         "Follow Aggregate Market Demand Target Production Algorithm: grace factor argument must "
       + "be in the range [0, 1] inclusive.");
      Preconditions.checkArgument(adaptionRate >= 0. && adaptionRate <= 1.0,
         "Follow Aggregate Market Demand Target Production Algorithm: market adaptation rate must "
       + "be in the range [0, 1] inclusive.");
      this.graceFactor = graceFactor;
      this.adaptionRate = adaptionRate;
      this.goodsMarket = goodsMarket;
   }
   
   @Override
   public double computeNext(FirmDecisionRule.FirmState state) {
      double
         lastMarketAggregateDemand =
             goodsMarket.getLastAggregateDemand(state.getGoodsType()) * 
             goodsMarket.getInstrument(state.getGoodsType())
                .getLastMarketShareFor(state.getFirmName()),
         lastProductionTarget = 
             super.getLastValue(),
         __factor = adaptionRate * getRandomUnitformDouble(),
         productionDifference = 
             lastMarketAggregateDemand - lastProductionTarget * (1. -  graceFactor),
         nextProductionTarget = 
             lastProductionTarget + productionDifference * __factor;
      super.recordNewValue(nextProductionTarget);
      return nextProductionTarget;
   }
   
   /**
     * Get a random double in the range [0, 1).
     */
   private double getRandomUnitformDouble() {
      return Simulation.getRunningModel().random.nextDouble();
   }
   
   /**
     * Get the grace factor.
     */
   public double getGraceFactor() {
      return graceFactor;
   }
   
   /**
     * Get the grace factor as a percentage of production.
     */
   public double getGraceFactorAsPercentage() {
      return graceFactor * 100.;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "grace factor = " + getGraceFactorAsPercentage() + ", "
            + super.toString();
   }
}
