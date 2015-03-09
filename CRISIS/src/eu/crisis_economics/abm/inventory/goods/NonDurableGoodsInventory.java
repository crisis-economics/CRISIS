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

/**
  * A concrete implementation of AbstractGoodsInventory. This 
  * implementation treats inventory as follows:
  *    (a) consumption of x units of stock results in
  *        the complete loss of x units of stock (with the 
  *        exception that, if x < 0, then no action is taken),
  *    (b) all stock disappears completely whenever the
  *        passage of time is processed.
  * @author phillips
  */
public final class NonDurableGoodsInventory extends AbstractGoodsInventory {
   
   public NonDurableGoodsInventory(double initialStock) {
      super(new SimpleResourceInventory(initialStock));
   }
   
   public NonDurableGoodsInventory() { this(0.); }
   
   @Override
   public void consume(double quantity) {
      if(quantity <= 0.) return;
      final double stored = getStoredQuantity();
      quantity = Math.min(stored, quantity);
      setStoredQuantity(stored - quantity);
   }
   
   @Override
   public void consume() {
      consume(getStoredQuantity());
   }
   
   @Override
   protected void applyPassageOfTime(double timeHasElapsed) {
      setStoredQuantity(0.);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Non-Durable Goods Inventory, stored: " + getStoredQuantity() + ", allocated: " +
             getAllocated() + ".";
   }
}
