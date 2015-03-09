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

import java.util.Map.Entry;

import eu.crisis_economics.abm.HasAssets;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.Repository;
import eu.crisis_economics.utilities.StateVerifier;

/** 
  * A skeletal implementation of the contract class, with specialization
  * to repositories. This class allows the value of a repository to
  * be placed on the balance sheet of the repository owner:
  * 
  * AbstractRepositoryValuation<K, T extends Inventory> where K is the 
  * inventory key type and T is the inventory type.
  * 
  * Inheritance: implement
  *    (a) StorageValuation getInventoryValuation(InventoryKey key).
  *        This method returns creates a StorageValuation object to determine
  *        the value of Inventories with the specified key; and
  *    (b) setValue, if this operation is permitted.
  * 
  * In the current implementation, repositories have no notion of 
  * interest or return rates. The value of getInterest() is zero for
  * this type, and setInterestRate() is an unsupported operation.
  * 
  * @author phillips
  */
public abstract class AbstractRepositoryValuation<K, T extends Inventory> extends Contract {
   
   private final Repository<K, T>
      repository;
   
   protected AbstractRepositoryValuation(
      final Repository<K, T> repository,
      HasAssets holder
      ) {
      super(Double.MAX_VALUE);
      StateVerifier.checkNotNull(repository, holder);
      this.repository = repository;
      holder.addAsset(this);
   }
   
   @Override
   public final double getValue() {
      double totalValue = 0.;
      for(final Entry<K, T> record : repository.entrySet()) {
         final InventoryValuation evaluator = getInventoryValuation(record.getKey());
         totalValue += evaluator.getValueOf(record.getValue());
      }
      return totalValue;
   }
   
   @Override
   public abstract void setValue(double newValue);
   
   @Override
   public final double getFaceValue() {
      return getValue();
   }
   
   @Override
   public final void setFaceValue(double value) {
      setValue(value);
   }
   
   @Override
   public final double getInterestRate() { return 0.; }
   
   @Override
   public final void setInterestRate(double interestRate) {
      throw new UnsupportedOperationException();
   }
   
   /**
     * Get an inventory valuation object for the specified key.
    * @return 
     */
   protected abstract InventoryValuation getInventoryValuation(K key);
   
   /**
     * Get a reference to the underlying repository.
     */
   protected final Repository<K, T> getRepository() { return repository; }
}
