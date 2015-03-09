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
package eu.crisis_economics.abm.household;

import java.util.ArrayList;
import java.util.List;

import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.SimpleResourceInventory;

/**
  * A simple, exogenous implementation of the Employer interface.
  * This type has a virtually unlimited (and untracked) exogenous
  * cash endowment. No cash flow injections are made, and no use is
  * made of incoming labour for production.
  * @author phillips
  */
public final class ExogenousEmployer implements Employer {
   
   private final Inventory cash;
   private final String uniqueName;
   private final List<Labour> ownedLabour;
   
   public ExogenousEmployer() {
      this.cash = new SimpleResourceInventory(Double.MAX_VALUE / 2.);
      this.uniqueName = java.util.UUID.randomUUID().toString();
      this.ownedLabour = new ArrayList<Labour>();
   }
   
   @Override
   public double credit(double amount) throws InsufficientFundsException {
      cash.pull(amount);
      return amount;
   }

   @Override
   public void debit(double amount) {
      cash.push(amount);
   }

   @Override
   public void cashFlowInjection(double amt) {
      // No Action.
   }

   @Override
   public String getUniqueName() {
      return uniqueName;
   }
   
   @Override
   public double allocateCash(double positiveAmount) {
      return cash.changeAllocatedBy(positiveAmount);
   }
   
   @Override
   public double disallocateCash(double positiveAmount) {
      return -cash.changeAllocatedBy(-positiveAmount);
   }
   
   @Override
   public double getAllocatedCash() {
      return cash.getAllocated();
   }
   
   @Override
   public double getUnallocatedCash() {
      return cash.getUnallocated();
   }
   
   @Override
   public void addLabour(Labour labour) {
      ownedLabour.add(labour);
   }
   
   @Override
   public boolean removeLabour(final Labour labour) {
      return ownedLabour.remove(labour);
   }
   
   @Override
   public double getLabourForce() {
      double result = 0.;
      for(final Labour labour : ownedLabour)
         result += labour.getQuantity();
      return result;
   }
   
   @Override
   public void disallocateLabour(double size) {
      // No Action.
   }
   
   @Override
   public void registerIncomingEmployment(
      double labourEmployed,
      double unitLabourWage
      ) {
      // No Action.
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
       return "ExogenousEmployer [cash=" + cash + ", uniqueName=" + uniqueName
          + ", ownedLabour=" + ownedLabour + "]";
   }
}
