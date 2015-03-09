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

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.Repository;

/**
  * A simple inventory valuation algorithm, assuming a value of
  * £1 per unit of all resources owned.
  * @author phillips
  */
public final class UnitPriceRepositoryValuation<K, T extends Inventory>
   extends AbstractRepositoryValuation<K, T> {
   
   private final UnitPriceInventoryValuation delegate;
   
   protected UnitPriceRepositoryValuation(
      final Repository<K, T> repository,
      final HasAssets holder
      ) {
      super(repository, holder);
      delegate = new UnitPriceInventoryValuation();
   }
   
   @Override
   public void setValue(double newValue) {
      throw new UnsupportedOperationException();
   }
   
   @Override
   protected InventoryValuation getInventoryValuation(K key) {
      return delegate;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "£1 Per Unit Inventory Valuation";
   }
}
