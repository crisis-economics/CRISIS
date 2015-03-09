/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
  * A TreeMap implementation of the Repository<String> interface.
  * 
  * Inheritance:
  * Implement T createEmptyT(String TType). This
  * method is called when, and only when, a new storage space is required
  * for T with the specified key.
  * @author phillips
  */
public abstract class AbstractSetRepository<T extends Inventory>
   implements Repository<String, T> {
   private TreeMap<String, T> inventories;
   
   public AbstractSetRepository() {
      this.inventories = new TreeMap<String, T>();
   }
   
   @Override
   public void clear() {
      inventories.clear();
   }
   
   @Override
   public Object clone() {
      return inventories.clone();
   }
   
   @Override
   public boolean containsKey(Object key) {
      return inventories.containsKey(key);
   }
   
   @Override
   public boolean containsValue(Object value) {
      return inventories.containsValue(value);
   }
   
   @Override
   public Set<java.util.Map.Entry<String, T>> entrySet() {
      return inventories.entrySet();
   }
   
   @Override
   public boolean equals(Object o) {
      return inventories.equals(o);
   }
   
   @Override
   public T get(Object key) {
      return inventories.get(key);
   }
   
   @Override
   public int hashCode() {
      return inventories.hashCode();
   }
   
   @Override
   public boolean isEmpty() {
      return inventories.isEmpty();
   }

   @Override
   public Set<String> keySet() {
      return inventories.keySet();
   }
   
   @Override
   public T put(String key, T value) {
      return inventories.put(key, value);
   }
   
   @Override
   public void putAll(Map<? extends String, ? extends T> map) {
      inventories.putAll(map);
   }
   
   @Override
   public T remove(Object key) {
      return inventories.remove(key);
   }
   
   @Override
   public int size() {
      return inventories.size();
   }
   
   @Override
   public Collection<T> values() {
      return inventories.values();
   }
   
   @Override
   public final void push(final String key, double quantity) {
      if(quantity <= 0.) return;
      T inventory = inventories.get(key);
      if(inventory == null) {
         inventory = createEmptyInventory(key);
         inventories.put(key, inventory);
      }
      inventory.push(quantity);
   }
   
   @Override
   public final void pushWithDelay(final String key, double quantity) {
      if(quantity <= 0.) return;
      T inventory = inventories.get(key);
      if(inventory == null) {
         inventory = createEmptyInventory(key);
         inventories.put(key, inventory);
      }
      inventory.pushWithDelay(quantity);
   }
   
   @Override
   public final double pull(final String key, double quantity) {
      if(quantity <= 0.) return 0.;
      T inventory = inventories.get(key);
      if(inventory == null) return 0.;
      return inventory.pull(quantity);
   }
   
   /**
     * Create an empty inventory of the specified type.
     * @param inventoryType
     *        The type of inventory storage to create.
     */
   protected abstract T createEmptyInventory(String key);
   
   @Override
   public final double getStoredQuantity(final String key) {
      T inventory = inventories.get(key);
      return inventory == null ? 0. : inventory.getStoredQuantity();
   }
   
   @Override
   public double getMaximumAllocatable(String key) {
      T inventory = inventories.get(key);
      return inventory == null ? 0. : inventory.getMaximumAllocatable();
   }
   
   @Override
   public double getAllocated(String key) {
      T inventory = inventories.get(key);
      return inventory == null ? 0. : inventory.getAllocated();
   }
   
   @Override
   public double setAllocated(String key, double posiveAmount) {
      T inventory = inventories.get(key);
      if(inventory != null) return inventory.setAllocated(posiveAmount);
      return 0.;
   }
   
   @Override
   public double setUnallocated(String key, double positiveAmount) {
      T inventory = inventories.get(key);
      if(inventory != null) return inventory.setUnallocated(positiveAmount);
      return 0.;
   }
   
   @Override
   public double changeAllocatedQuantityBy(String key, double amount) {
      T inventory = inventories.get(key);
      if(inventory != null) return inventory.changeAllocatedBy(amount);
      return 0.;
   }
   
   @Override
   public double getUnallocated(String key) {
      T inventory = inventories.get(key);
      return inventory == null ? 0. : inventory.getUnallocated();
   }
   
   @Override
   public void allocateAll(String key) {
      T inventory = inventories.get(key);
      if(inventory != null) inventory.allocateAll();
   }

   @Override
   public void disallocateAll(String key) {
      T inventory = inventories.get(key);
      if(inventory != null) inventory.disallocateAll();
   }
}
