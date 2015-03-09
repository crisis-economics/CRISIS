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

import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/** 
  * A skeletal implementation of the Storage interface.
  * This implementation automatically manages enqueued
  * quantities.
  * 
  * In this implementation:
  *   (a) enqueued quantities appear in the store
  *       at BEFORE_ALL in the simulation cycle, 
  *   (b) enqueued quantities are processed whenever
  *       ONE_DAY passes.
  * 
  * Inheritance: implement the following methods:
  * public abstract double getStoredQuantity();
  * public abstract boolean isEmpty();
  * public abstract double pull(double positiveAmount);
  * public abstract void push(double positiveAmount);
  * public abstract double setStoredQuantity(double amount);
  * 
  * @author phillips
  */
public abstract class AbstractStorage implements Storage {
   
   private double
      enqueued;
   
   public AbstractStorage(double initialQuantity) {
      Preconditions.checkArgument(initialQuantity >= 0.);
      this.enqueued = 0.;
      
      scheduleSelf();
   }
   
   public AbstractStorage() { this(0.); }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "update",
         CustomSimulationCycleOrdering.create(NamedEventOrderings.BEFORE_ALL, 1));
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void update() {
      push(enqueued);
      enqueued = 0.;
   }
   
   @Override
   public abstract double getStoredQuantity();
   
   @Override
   public abstract boolean isEmpty();
   
   @Override
   public abstract double pull(double positiveAmount);
   
   @Override
   public abstract void push(double positiveAmount);
   
   @Override
   public final void pushWithDelay(double positiveAmount) {
      if(positiveAmount <= 0.) return;
      enqueued += positiveAmount;
   }
   
   @Override
   public final double setStoredQuantity(double amount) {
      if(amount < 0.) amount = 0.;
      final double change = amount - getStoredQuantity();
      if(change > 0.)
         push(change);
      else if(change < 0.)
         pull(-change);
      return getStoredQuantity();
   }
   
   @Override
   public final void flush() {
      setStoredQuantity(0.);
      enqueued = 0.;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Storage, stored amount: " + getStoredQuantity() + ", enqueued: "
            + enqueued + ".";
   }
}