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
package eu.crisis_economics.abm.strategy.leverage;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;

/**
  * A target leverage algorithm with a fixed leverage target.
  * 
  * @author phillips
  */
public final class ConstantTargetLeverageAlgorithm
   extends AbstractLeverageTargetAlgorithm {
   
   /**
     * Create a {@link ConstantTargetLeverageAlgorithm} object.
     * 
     * @param initialLeverageTarget
     *        The initial leverage target. This argument should be
     *        non-negative.
     */
   @Inject
   public ConstantTargetLeverageAlgorithm(
   @Named("CONSTANT_LEVERAGE_TARGET_ALGORITHM_VALUE")
      double initialLeverageTarget
      ) {
      super(initialLeverageTarget);
   }
   
   @Override
   public void computeTargetLeverage(Portfolio bankStrategy) {
      // No action.
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Constant Target Leverage Algorithm. Leverage Target: " 
           + super.getTargetLeverage() + ".";
   }
}
