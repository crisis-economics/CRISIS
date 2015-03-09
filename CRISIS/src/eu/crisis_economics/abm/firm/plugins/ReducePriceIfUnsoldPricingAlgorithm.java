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
package eu.crisis_economics.abm.firm.plugins;

import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * An implementation of a {@link Firm} goods selling price decision rule. This algorithm
  * is a simple adaptive heuristic price decision rule. Prices <code>p</code> are decided
  * as follows:<br><br>
  * 
  * {@code (a)}
  *   If the quantity of unsold goods {@code u} is less than {@code fg}, where {@code f}
  *   is a positive constant less than {@code 1.0} (the <i>grace</i>), and {@code g} is
  *   the total production quantity of goods available for sale in this business cycle,
  *   then increase the selling price {@code p} by a factor of {@code (1 + dm)} where {@code d}
  *   is a random uniform double in the range {@code [0, 1)]} and {@code m} is a positive
  *   constant (the <i>adaptation rate</i>);<br>
  * {@code (b)}
  *   Otherwise, multiply {@code p} by a factor of {@code (1 - dm)};<br>
  * {@code (c)}
  *   Ensure that {@code p} is in the range {@code [minimum markup rate * unit production cost,
  *   maximum markup rate * unit production cost]}, where both the minimum and maximum markup
  *   rates are customizable.
  * 
  * @author phillips
  */
public final class ReducePriceIfUnsoldPricingAlgorithm 
   extends FirmDecisionRule {
   
   private final double 
      initialValue,
      adaptationRate,
      graceAsFactionOfProduction,
      minimumMarkup,
      maximumMarkup;
   
   private final Random
      dice;
   
   /**
     * Create a {@link ReducePriceIfUnsoldPricingAlgorithm} object with custom parameters.<br><br>
     * See also {@link ReducePriceIfUnsoldPricingAlgorithm}.
     * 
     * @param initialValue
     *        The initial goods selling price (per unit).
     * @param adaptationRate
     *        The rate at which to modify prices (per simulation cycle). This argument
     *        must be non-negative.
     * @param grace
     *        The fraction of the last {@link Firm} goods production yield which need
     *        not be sold such that selling prices will still be increased.
     * @param maximumMarkup
     *        The maximum markup rate at which to set prices. This argument should
     *        be greater than {@code 1.0}, or else a {@link Firm} can never make
     *        a profit selling any goods it produces. This argument must be non-negative.
     * @param minimumMarkup
     *        The minimum markup rate at which to set prices. This argument should
     *        be greater than {@code 1.0}, or else in principle it is possible for a
     *        {@link Firm} to sell goods at a loss. This argument must be non-negative
     *        and less than {@code maximumMarkup}.
     */
   @Inject
   public ReducePriceIfUnsoldPricingAlgorithm(
   @Named("INITIAL_GOODS_SELLING_PRICE_PER_UNIT")
      final double initialValue,
   @Named("REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_ADAPTATION_RATE")
      final double adaptationRate,
   @Named("REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_GRACE")
      final double grace,
   @Named("REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MAXIMUM_MARKUP")
      final double maximumMarkup,
   @Named("REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MINIMUM_MARKUP")
      final double minimumMarkup
      ) {
      super(initGuard(initialValue));
      Preconditions.checkArgument(initialValue >= 0.);
      Preconditions.checkArgument(adaptationRate >= 0.);
      Preconditions.checkArgument(minimumMarkup >= 0.);
      Preconditions.checkArgument(maximumMarkup >= 0.);
      Preconditions.checkArgument(maximumMarkup >= minimumMarkup);
      this.initialValue = initialValue;
      this.adaptationRate = adaptationRate;
      this.graceAsFactionOfProduction = grace;
      this.minimumMarkup = minimumMarkup;
      this.maximumMarkup = maximumMarkup;
      this.dice = new Random(Simulation.getSimState().random.nextLong());
   }
   
   static private double initGuard(double initialValue) {
      if(initialValue <= 0)
         throw new IllegalArgumentException(
            "Reduce Price If Unsold Pricing Algorithm: initial price is non-positive.");
      return initialValue;
   }
   
   @Override
   public double computeNext(FirmState state) {
      double
         lastMarketAskPrice = super.getLastValue(),
         nextMarketAskPrice = 
            state.getTotalUnsoldGoodsThisCycle() >
            graceAsFactionOfProduction * state.getEffectiveGoodsProductionLastCycle() ? 
               lastMarketAskPrice * (1. - adaptationRate * dice.nextDouble()) :
               lastMarketAskPrice * (1. + adaptationRate * dice.nextDouble()),
         unitCost = state.getTotalLiquidityDemandLastCycle() / 
            state.getTargetGoodsProductionLastCycle();
      /*
       * Clamp markup rates with user parameters.
       */
      if(unitCost > 0.) {
         nextMarketAskPrice = Math.min(nextMarketAskPrice, maximumMarkup * unitCost);
         nextMarketAskPrice = Math.max(nextMarketAskPrice, minimumMarkup * unitCost);
      }
      super.recordNewValue(nextMarketAskPrice);
      return nextMarketAskPrice;
   }
   
   public double getInitialValue() {
      return initialValue;
   }
   
   public double getAdaptationRate() {
      return adaptationRate;
   }
   
   public double getGraceAsFactionOfProduction() {
      return graceAsFactionOfProduction;
   }
   
   public double getMinimumMarkup() {
      return minimumMarkup;
   }
   
   public double getMaximumMarkup() {
      return maximumMarkup;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Reduce Price If Unsold Pricing Algorithm, initial price:"
            + initialValue + ", adaptationRate:" + adaptationRate + ", "
            + "minimum markup: " + minimumMarkup + ", maximum markup: "
            + maximumMarkup + ".";
   }
}
