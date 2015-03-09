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

import eu.crisis_economics.abm.inventory.Storage;

/**
  * An extension to the Storage interface, with additional methods 
  * specializing the storage to goods resource types.
  * @author phillips
  */
public interface GoodsStorage extends Storage {
   /**
     * Consume (utilize) a quantity of goods from the store. If
     * the argument x to this method is negative or zero, no action
     * is taken. If the argument is greater than y = 
     * getStoredQuantity(), x is silently trimmed to y.
     */
   public void consume(double quantity);
   
   /**
     * Consume (utilize) all goods from the store. This method
     * is equivalent to consume(getStoredQuantity()).
     */
   public void consume();
}
