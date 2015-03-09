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

import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;

/**
  * @author phillips
  */
public final class PercentOfMarketValueLiquidityTarget
   extends FirmDecisionRule {
   
   private double
      targetProportionOfMarketValue;
   
   @Inject
   public PercentOfMarketValueLiquidityTarget(
   @Named("MARKET_VALUE_LIQUIDITY_TARGET_PERCENTILE")
      final double targetPercentOfMarketValue,
   @Named("MARKET_VALUE_LIQUIDITY_TARGET_INITIAL_VALUE_TARGET")
      final double initialValueTarget
      ) {
      super(initGuard(initialValueTarget));
      Preconditions.checkArgument(targetPercentOfMarketValue >= 0.);
      this.targetProportionOfMarketValue = targetPercentOfMarketValue * 1.e-2;
   }
   
   static private double initGuard(double initialValue) {
      if(initialValue < 0.)
      throw new IllegalArgumentException(
         "PercentOfMarketValueLiquidityTarget: liquidity target is negative.");
      return initialValue;
   }
   
   @Override
   public double computeNext(final FirmState state) {
      double
         nextLiquidityTarget =
            UniqueStockExchange.Instance.getStockPrice(state.getFirmName()) *
            UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(state.getFirmName()) *
            targetProportionOfMarketValue;
      nextLiquidityTarget = Math.max(nextLiquidityTarget, 0.);
      super.recordNewValue(nextLiquidityTarget);
      return nextLiquidityTarget;
   }
   
   /**
     * Get the target market value percentage for this liquidity algorithm.
     */
   public double getTargetMarketValuePercentage() {
      return targetProportionOfMarketValue * 1.e2;
   }
   
   /**
    * Returns a brief description of this object. The exact details of the
    * string are subject to change, and should not be regarded as fixed.
    */
   @Override
   public String toString() {
      return "Percent Of Market Value Liquidity Target, market value percent target: "
           + getTargetMarketValuePercentage() + ".";
   }
}
