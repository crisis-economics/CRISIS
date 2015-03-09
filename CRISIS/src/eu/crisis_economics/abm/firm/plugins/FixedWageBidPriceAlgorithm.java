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
  * A trivial (fixed) labour wage selection algorithm for {@link Firms}.
  * @author phillips
  */
public final class FixedWageBidPriceAlgorithm extends FirmDecisionRule {
   
   /**
     * Create a {@link FixedWageBidPriceAlgorithm} object.<br><br>
     * 
     * This {@link FirmDecisionRule} returns the same value every 
     * time it is asked to make a labour wage decision. The return
     * value is the argument to this constructor.
     * 
     * @param fixedLabourWage
     *        The fixed labour wage to return. This value should
     *        be non-negative.
     */
   @Inject
   public FixedWageBidPriceAlgorithm(
   @Named("FIXED_WAGE_BID_PRICE_ALGORITHM")
      final double fixedLabourWage) {
      super(initGuard(fixedLabourWage));
   }
   
   static double initGuard(double initialValue) {
      Preconditions.checkArgument(initialValue >= 0.);
      return initialValue;
   }
   
   @Override
   public double computeNext(FirmState currentState) {
      return this.getLabourWage();
   }
   
   /**
     * Get the (fixed) labour wage.
     */
   public double getLabourWage() {
      return super.getLastValue();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Trivial Labour Wage Algorithm, wage offering: " + this.getLabourWage();
   }
}