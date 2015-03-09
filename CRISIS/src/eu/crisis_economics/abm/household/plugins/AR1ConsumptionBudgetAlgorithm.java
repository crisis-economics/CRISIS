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
package eu.crisis_economics.abm.household.plugins;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

//import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;

/**
  * A simple forwarding implementation of an AR1-type consumption budget decision rule.
  * This consumption budget algorithm initially delegates the consumption budget size
  * decision to another {@link HouseholdDecisionRule}. The result of this delegate decision
  * is smoothed in time via a discrete exponential process. The memory (adaptation rate)
  * of this smoothing process is customizable.<br><br>
  * 
  * Concretely, at each timestep {@code T} this decision rule asks a delegate consumption
  * budget {@code D} to select a budget size {@code B(t)}. This decision rule returns the
  * value {@code B(t) * m  + B(t - 1) * (1 - m)} where {@code m} is a fixed and customizable
  * memory parameter.
  * 
  * @author phillips
  */
public final class AR1ConsumptionBudgetAlgorithm
   extends HouseholdDecisionRule {
   
   private final double
      adaptationRate;
   
//   private SimpleGoodsMarket
//      goodsMarket;
   
   private HouseholdDecisionRule
      implementation;
   
   @Inject
   public AR1ConsumptionBudgetAlgorithm(
   @Named("AR1_CONSUMPTION_BUDGET_ALGORITHM_IMPLEMENTATION")
      final HouseholdDecisionRule implementation,
//      final SimpleGoodsMarket goodsMarket,
   @Named("AR1_CONSUMPTION_BUDGET_ALGORITHM_ADAPTATION_RATE")
      final double adaptationRate
      ) {
      super(0.);
      this.adaptationRate = adaptationRate;
//      this.goodsMarket = Preconditions.checkNotNull(goodsMarket);
      this.implementation = Preconditions.checkNotNull(implementation);
   }
   
   @Override
   public double computeNext(final HouseholdState state) {
      implementation.computeNext(state);
      double
         maximumBudget = state.getUnreservedDeposits(),
         intendedConsumption = implementation.getLastValue(),
         smoothedConsumption =
            adaptationRate * intendedConsumption + 
            (1. - adaptationRate) * super.getLastValue();
      super.recordNewValue(smoothedConsumption);
      return Math.min(
         smoothedConsumption,
         maximumBudget
         );
   }
   
   /**
     * Get the budget size adaptation rate for this decision rule.
     */
   public double getAdaptationRate() {
      return adaptationRate;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return getClass().getSimpleName() + ", memory rate: " + getAdaptationRate() + ".";
   }
}
