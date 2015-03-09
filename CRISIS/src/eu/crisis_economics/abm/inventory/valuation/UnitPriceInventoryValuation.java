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
package eu.crisis_economics.abm.inventory.valuation;

import eu.crisis_economics.abm.inventory.Storage;

/**
  * A simple storage valuation algorithm. 
  * This valuation technique estimates the value of a storage space
  * as being equal to the quantity of resources it contains (Ie. £1
  * per unit of resource owned).
  * @author phillips
  */
public final class UnitPriceInventoryValuation implements InventoryValuation {
   public UnitPriceInventoryValuation() { } // Stateless
   
   @Override
   public double getValueOf(final Storage storage) {
      return storage.getStoredQuantity();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Unit Price Storage Valuation.";
   }
}
