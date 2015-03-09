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

/**
  * @author phillips
  */
public final class FollowTargetProductionPricingAlgorithm 
   extends FirmDecisionRule {
   
   private final double 
      initialValue,
      beta;
   
   /**
     * A {@link Firm} decision rule which is linked to the 
     * {@link Firm} target production {@link FirmDecisionRule}.<br><br>
     * 
     * This {@link FirmDecisionRule} decides goods prices, P, as
     * follows:<br><br>
     * (a) the {@link Firm} queries its current target production
     *     quantity, T;<br>
     * (b) the {@link Firm} queries is target production in the 
     *     last market cycle, T';<br>
     * (c) A candidate selling price, P, is computed as follows:<br><br>
     * 
     * P = (last selling price, P') * (T/T')**beta.  (*)<br><br>
     * 
     * (d) P is clamped in the range 1.1 * UPC, 100 * initial 
     *     selling price (no selling at a loss, no extortion, resp.),
     *     where UPC is the Unit Production Cost.<br><br>
     * 
     * This pricing method works in a similar way to the heuristic 
     * walking process P_next = P_last +/- (increment), except that
     * the degree of the change is linked to the target production, T.
     * The motivation for the form (*) is that:<br><br>
     * 
     *  P/P' = (T/T')**beta,<br><br>
     *  
     * Ie. when beta = 1, the ratio of changes in target production, T, 
     * is reflected exactly by changes in the selling price, P. Values 
     * beta < 1 dampen the transmission between changes in T and P.
     * 
     * @param initialValue
     *        The initial goods selling price (per unit).
     * @param beta
     *        The above exponent. This argument shold be non-negative.
     */
   @Inject
   public FollowTargetProductionPricingAlgorithm(
   @Named("INITIAL_GOODS_SELLING_PRICE_PER_UNIT")
      final double initialValue,
   @Named("FOLLOW_TARGET_PRODUCTION_PRICING_ALGORITHM_EXPONENT")
      final double beta
      ) {
       super(initGuard(initialValue));
       Preconditions.checkArgument(initialValue >= 0.);
       Preconditions.checkArgument(beta >= 0.);
       this.initialValue = initialValue;
       this.beta = beta;
    }
    
    static private double initGuard(double initialValue) {
        if(initialValue <= 0)
            throw new IllegalArgumentException(
                "GraceFactorPricingAlgorithm: initial price is non-positive.");
        return initialValue;
    }
    
    @Override
    public double computeNext(FirmState state) {
       double
          lastMarketAskPrice = super.getLastValue(),
          nextMarketAskPrice = 
             lastMarketAskPrice * Math.pow(Math.abs(
                (state.getTargetGoodsProductionNow() + 1.e-2) /
                ((state.getTargetGoodsProductionLastCycle() + 1.e-2))),
                beta
                ),
          unitCost = state.getTotalLiquidityDemandLastCycle() / 
             state.getTargetGoodsProductionLastCycle();
      if(unitCost > 0.) {
         /*
          * Assumptions: 
          *   (a) no selling at a negative margin (cap markup rates at 10%),
          *   (b) no unlimited increases in market price. This assumption
          *       is temporary, as the codebase currently has no natural 
          *       (Ie. in-built) notion of inflation.
          */
         nextMarketAskPrice = Math.min(nextMarketAskPrice, 100. * initialValue);
         nextMarketAskPrice = Math.max(nextMarketAskPrice, 1.1 * unitCost);
      }
      super.recordNewValue(nextMarketAskPrice);
      return nextMarketAskPrice;
    }

   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Follow Target Production Pricing Algorithm, initial price:"
            + initialValue + ", beta:" + beta + ".";
   }
}
