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
  * Consumption characteristics for durable goods.
  * @author phillips
  */
interface DurableGoodsConsumptionCharacteristic {
   /**
     * Compute the result of consuming the stated quantity of goods.
     * @param amount
     *        The amount of goods to consume (utilize)
     * @return
     *        The quantity of goods remaining after consumption (utilization).
     */
   public double computeConsumption(double amount);
   
   /**
     * An implementation of the DurableGoodsConsumptionCharacteristic 
     * interface. This implementation describes a type of goods that 
     * can be reused indefinitely without loss of performance or quantity, 
     * such as an (extreme) long-life machine tool.
     * @author phillips
     */
   static final class Indestructible implements DurableGoodsConsumptionCharacteristic {
      @Override
      public double computeConsumption(double amount) {
         return amount;
      }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
      @Override
      public String toString() {
         return "Indestructible Durable Good Consumption Characteristic.";
      }
   }
   
   /**
     * An implementation of the DurableGoodsConsumptionCharacteristic 
     * interface. This implementation describes a type of goods that 
     * suffer (discrete) exponential loss each time they are used.
     * @author phillips
     */
   static final class ExponentialLoss implements DurableGoodsConsumptionCharacteristic {
      private double
         multiplier;
      
      /**
        * @param multiplier
        *    The amount of loss suffered by a quantity of these goods when
        *    consumed (utilized). For argument m, an amount of goods x
        *    will be reduced to m * x following consumption.
        */
      public ExponentialLoss(final double multiplier) {
         Preconditions.checkArgument(multiplier >= 0.);
         Preconditions.checkArgument(multiplier <= 1.);
         this.multiplier = multiplier;
      }
      
      @Override
      public double computeConsumption(double amount) {
         return multiplier * amount;
      }
      
      /**
        * Get the multiplier for this loss process.
        */
      public double getMultiplier() { return multiplier; }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
      @Override
      public String toString() {
         return "Exponential Loss Durable Good Consumption Characteristic. "
              + "Multiplier: " + multiplier + ".";
      }
   }
   
   /**
     * An implementation of the DurableGoodsConsumptionCharacteristic 
     * interface. This implementation describes a type of goods that 
     * are completely destroyed whenever they are consumed (single-use
     * goods).
     * @author phillips
     */
   static final class SingleUse implements DurableGoodsConsumptionCharacteristic {
      public SingleUse() { }
      
      @Override
      public double computeConsumption(double amount) { return 0; }
      
      /**
        * Returns a brief description of this object. The exact details of the
        * string are subject to change, and should not be regarded as fixed.
        */
      @Override
      public String toString() {
         return "Single Use Durable Good Consumption Characteristic";
      }
   }
}
