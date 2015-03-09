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
package eu.crisis_economics.abm.algorithms.matching;

/** 
  * Simple implementation of the Node interface.
  * @author phillips
  */
public class SimpleNode implements Node {
   private double 
      desiredPricePerUnit,
      desiredVolume;
   
   private Object 
      objectReference;
   
   public SimpleNode(
      final double unitPrice, 
      final double totalVolume, 
      final Object objectReference
      ) {
      this.desiredPricePerUnit = unitPrice;
      this.desiredVolume = totalVolume;
      this.objectReference = objectReference;
   }
   
   public SimpleNode(final SimpleNode other) {                     // Copy Constructor
      this.desiredPricePerUnit = other.desiredPricePerUnit;
      this.desiredVolume = other.desiredVolume;
      this.objectReference = other.objectReference;
   }
   
   @Override
   public double getPricePerUnit() { return desiredPricePerUnit; }
   
   @Override
   public double getVolume() { return desiredVolume; }
   
   @Override
   public Object getObjectReference() { return objectReference; }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return
         "Node, desired unit price: " + getPricePerUnit() 
       + ", desired volume: " + getVolume() + ".";
   }
}
