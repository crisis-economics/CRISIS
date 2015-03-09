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

/**
  * Factory class for goods inventories.
  * 
  * The method GoodsInventoryFactory.create returns a goods inventory with
  *    (a) time decay, and 
  *    (b) goods consumption
  * characteristics as specified by the arguments.
  * @author phillips
  */
public final class GoodsInventoryFactory {
   /** 
     * Create a goods inventory with appropriate decay/consumption behaviour.
     * Arguments: 
     *   (a) goodsDecayPerUnitTime 
     *      The rate at which goods decay in time; decayPerUnitTime = 0.2 
     *      means that 20% of goods will decay (disappear) when the clock 
     *      ticks. 
     *   (b) goodsConsumptionPerUse
     *      The rate at which goods disappear when consumed (for instance 
     *      in production); consumptionFactorPerUse = 0.4 means that 40% 
     *      of goods will be removed when used in consumption. 
     */
   public static GoodsInventory createInventory(
        double initialOwnedQuantity,
        double goodsDecayPerUnitTime,
        double goodsConsumptionPerUse
        ) {
        if(goodsDecayPerUnitTime < 0. || goodsDecayPerUnitTime > 1.) 
            throw new IllegalArgumentException(
                "GoodsStorageFactory.createStorage: illegal argument:" +
                " decay factor not in range [0, 1].");
        if(goodsConsumptionPerUse < 0. || goodsConsumptionPerUse > 1.) 
            throw new IllegalArgumentException(
                "GoodsStorageFactory.createStorage: illegal argument:" +
                " consumption factor not in range [0, 1].");
        if(initialOwnedQuantity < 0.)
            throw new IllegalArgumentException(
                "GoodsStorageFactory.createStorage: illegal argument:" +
                " initial owned goods amount is negative.");
        final double 
            decayMultiplier = (1 - goodsDecayPerUnitTime),
            consumptionMultiplier = (1 - goodsConsumptionPerUse);
        GoodsInventory result;
        if(decayMultiplier == 0. && consumptionMultiplier == 0.)
           result = new NonDurableGoodsInventory();
        else {
           DurableGoodsDecayCharacteristic decay;
           DurableGoodsConsumptionCharacteristic consumption;
           
           // Decay Characteristics
           if(decayMultiplier == 0.)
              decay = new DurableGoodsDecayCharacteristic.ImmediateCompleteDecay();
           else if(decayMultiplier < 1.)
              decay = new DurableGoodsDecayCharacteristic.ExponentialLoss(goodsDecayPerUnitTime);
           else if(decayMultiplier == 1.)
              decay = new DurableGoodsDecayCharacteristic.Immortal();
           else
              throw new IllegalArgumentException(
                 "GoodsInventory.crate: decay per unit time (value " + goodsDecayPerUnitTime
               + " must be in the range [0, 1].");
           
           // Consumption Characteristics
           if(consumptionMultiplier == 0.)
              consumption = new DurableGoodsConsumptionCharacteristic.SingleUse();
           else if(consumptionMultiplier < 1.)
              consumption = new DurableGoodsConsumptionCharacteristic.ExponentialLoss(
                 consumptionMultiplier);
           else if(consumptionMultiplier == 1.)
              consumption = new DurableGoodsConsumptionCharacteristic.Indestructible();
           else
              throw new IllegalArgumentException(
                 "GoodsInventory.crate: consumption per use (value " + goodsConsumptionPerUse
               + " must be in the range [0, 1].");
           
           DurableGoodsCharacteristics characteristics =
              new SimpleDurableGoodsCharacteristics(decay, consumption);
           result = new DurableGoodsInventory(characteristics);
        }
        result.setStoredQuantity(initialOwnedQuantity);
        return result;
    }
    
    private GoodsInventoryFactory() { } // Uninstantiatable
}
