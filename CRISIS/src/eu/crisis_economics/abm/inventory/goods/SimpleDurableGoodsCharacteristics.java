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
package eu.crisis_economics.abm.inventory.goods;

import eu.crisis_economics.utilities.StateVerifier;

/** 
  * Simple implementation of the DurableGoodsCharacteristics interface.
  * @author phillips
  */
public final class SimpleDurableGoodsCharacteristics implements DurableGoodsCharacteristics {
   
   private final DurableGoodsDecayCharacteristic
      decayBehavior;
   private final DurableGoodsConsumptionCharacteristic
      consumptionBehavior;
   
   public SimpleDurableGoodsCharacteristics(
      final DurableGoodsDecayCharacteristic decayBehavior,
      final DurableGoodsConsumptionCharacteristic consumptionBehavior
      ) {
      StateVerifier.checkNotNull(decayBehavior, consumptionBehavior);
      this.decayBehavior = decayBehavior;
      this.consumptionBehavior = consumptionBehavior;
   }
   
   @Override
   public double computeTimeDecay(double amount, double timeHasElapsed) {
      return decayBehavior.computeTimeDecay(amount, timeHasElapsed);
   }
   
   @Override
   public double computeConsumption(double amount) {
      return consumptionBehavior.computeConsumption(amount);
   }
   
   /**
     * Get the decay behaviour characteristic.
     */
   public DurableGoodsDecayCharacteristic getDecayBehavior() {
      return decayBehavior;
   }
   
   /**
     * Get the consumption behaviour characteristic.
     */
   public DurableGoodsConsumptionCharacteristic getConsumptionBehavior() {
      return consumptionBehavior;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "SimpleDurableGoodsCharacteristics, Decay Behavior: "
            + decayBehavior + ", Consumption Behavior: " + consumptionBehavior
            + ".";
   }
}
