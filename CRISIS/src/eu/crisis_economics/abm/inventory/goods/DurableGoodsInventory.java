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

import eu.crisis_economics.abm.inventory.SimpleResourceInventory;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A concrete implementation of AbstractGoodsInventory. This 
  * implementation accepts a DurableGoodsCharacteristics object
  * and operates as follows:
  *   (a) consume(x) and consume() compute the effect of the 
  *       consumption via the DurableGoodsCharacteristics field
  *       and then set the stored quantity of durable goods 
  *       accordingly,
  *   (b) time decay is automatically scheduled. The effect of
  *       the passage of time on enclosed goods is delegaged to
  *       the DurableGoodsCharacteristics field.
  * @author phillips
  */
public final class DurableGoodsInventory extends AbstractGoodsInventory {
   private DurableGoodsCharacteristics characteristics;
   
   public DurableGoodsInventory(final DurableGoodsCharacteristics characteristics) {
      this(characteristics, 0.);
   }
   
   public DurableGoodsInventory(
      final DurableGoodsCharacteristics characteristics,
      double initialStock
      ) {
      super(new SimpleResourceInventory(0.));
      StateVerifier.checkNotNull(characteristics);
      this.characteristics = characteristics;
   }
   
   @Override
   public void consume(double quantity) {
      if(quantity <= 0.) return;
      final double
         stored = getStoredQuantity();
      quantity = Math.min(stored, quantity);
      final double
         unused = stored - quantity;
      setStoredQuantity(unused + characteristics.computeConsumption(quantity));
   }
   
   @Override
   public void consume() {
      consume(getStoredQuantity());
   }
   
   @Override
   protected void applyPassageOfTime(double timeHasElapsed) {
      if(timeHasElapsed <= 0.) return;
      setStoredQuantity(characteristics.computeTimeDecay(getStoredQuantity(), timeHasElapsed));
   }
}
