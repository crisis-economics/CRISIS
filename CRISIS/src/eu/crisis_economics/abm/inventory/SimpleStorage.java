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
package eu.crisis_economics.abm.inventory;

import com.google.common.base.Preconditions;

/** 
  * Simple implementation of the Storage interface.
  * 
  * @author phillips
  */
public final class SimpleStorage extends AbstractStorage {
   
   private double
      quantityStored;
   
   public SimpleStorage(double initialQuantity) {
      Preconditions.checkArgument(initialQuantity >= 0.);
      this.quantityStored = initialQuantity;
   }
   
   public SimpleStorage() { this(0.); }
   
   @Override
   public double getStoredQuantity() {
      return quantityStored;
   }
   
   @Override
   public boolean isEmpty() {
      return (quantityStored <= 0.);
   }
   
   @Override
   public double pull(double positiveAmount) {
      if(positiveAmount <= 0.) return 0.;
      final double result = Math.min(positiveAmount, quantityStored);
      quantityStored = Math.max(quantityStored - positiveAmount, 0.);
      return result;
   }
   
   @Override
   public void push(double positiveAmount) {
      if(positiveAmount <= 0.) return;
      quantityStored += positiveAmount;
   }
}