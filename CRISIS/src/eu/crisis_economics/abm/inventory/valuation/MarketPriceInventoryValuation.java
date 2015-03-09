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
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A simple storage valuation algorithm based on market value.
  * This valuation technique estimates the value of a storage space
  * as being equal to the quantity of resources it contains times
  * the unit market price of the resource. The price per unit 
  * calculation is determined by a simple delegate method.
  * @author phillips
  */
public final class MarketPriceInventoryValuation implements InventoryValuation {
   
   public interface MarketPricePerUnitDelegate {
      double getPricePerUnit();
   }
   
   private final MarketPricePerUnitDelegate callback;
   
   public MarketPriceInventoryValuation(MarketPricePerUnitDelegate callback) {
      StateVerifier.checkNotNull(callback);
      this.callback = callback;
   }
   
   @Override
   public double getValueOf(final Storage storage) {
      return callback.getPricePerUnit() * storage.getStoredQuantity();
   }
   
   /**
     * Get the market value per unit of the stored resource.
     */
   public double getValuePerUnit() {
      return callback.getPricePerUnit();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Market Price Per Unit Storage Valuation, value per unit: "
           + getValuePerUnit() + ".";
   }
}
