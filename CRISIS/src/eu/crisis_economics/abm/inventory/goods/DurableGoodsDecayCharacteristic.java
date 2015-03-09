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

import com.google.common.base.Preconditions;

/**
  * Time-decay characteristics for durable goods.
  * @author phillips
  */
interface DurableGoodsDecayCharacteristic {
   /**
     * Compute the result of a time decay process applied to a quantity 
     * of goods.
     * @param amount
     *        The quantity of goods to subject to decay.
     * @param
     *        The duration over which the goods suffer decay.
     * @return
     *        The quantity of goods remaining after the decay process finishes.
     */
   public double computeTimeDecay(double amount, double timeHasElapsed);
   
   /**
     * An implementation of the DurableGoodsDecayCharacteristic 
     * interface. This implementation describes a type of goods that 
     * does not suffer any decay.
     * @author phillips
     */
   static final class Immortal implements DurableGoodsDecayCharacteristic {
      @Override
      public double computeTimeDecay(double amount, double timeHasElapsed) {
         return amount;
      }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
      @Override
      public String toString() {
         return "Immortal Durable Good Decay Characteristic.";
      }
   }
   
   /**
     * An implementation of the DurableGoodsDecayCharacteristic 
     * interface. This implementation describes a type of goods that 
     * suffers (discrete) exponential decay per unit of time elapsed.
     * @author phillips
     */
   static final class ExponentialLoss implements DurableGoodsDecayCharacteristic {
      private double
         multiplier;
      
      /**
        * @param multiplier
        *    The amount of decay (loss) suffered by one unit of goods
        *    after one unit of time passes.
        */
      public ExponentialLoss(final double loss) {
         Preconditions.checkArgument(loss >= 0.);
         Preconditions.checkArgument(loss <= 1.);
         this.multiplier = 1. - loss;
      }
      
      @Override
      public double computeTimeDecay(double amount, double timeHasElapsed) {
         return Math.pow(multiplier, timeHasElapsed) * amount;
      }
      
      /**
        * Get the multiplier for this loss process (per unit time).
        */
      public double getMultiplier() { return multiplier; }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
      @Override
      public String toString() {
         return "Exponential Loss Durable Good Decay Characteristic, "
              + "Multiplier: " + multiplier + ".";
      }
   }
   
   /**
     * An implementation of the DurableGoodsDecayCharacteristic 
     * interface. This implementation describes a type of goods that
     * decay completely (to zero) whenever the passage of time is processed.
     * @author phillips
     */
   static final class ImmediateCompleteDecay implements DurableGoodsDecayCharacteristic {
      public ImmediateCompleteDecay() { }
      
      @Override
      public double computeTimeDecay(double amount, double timeHasElapsed) {
         if(timeHasElapsed > 0.) return 0.;
         else return amount;
      }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
     @Override
     public String toString() {
        return "Decay To Zero Durable Good Decay Characteristic";
     }
   }
}
