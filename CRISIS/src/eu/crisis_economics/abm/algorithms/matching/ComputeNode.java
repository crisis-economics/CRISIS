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

import com.google.common.base.Preconditions;

/**
  * Extend a node to include 'usable' and 'unusable' subvolumes. This
  * type is detail and should retain package private status.
  * @author phillips
  */
final class ComputeNode implements Node {
   private double unusable;
   private final Node node;
   
   /**
     * Add 'usable' and 'unusable' subvolumes to an existing node
     * object. The argument is never modified.
     */
   public ComputeNode(final Node node) { // Mutable, Detail
      Preconditions.checkNotNull(node);
      this.node = node;
      unusable = 0.;
   }
   
   /**
     * The node subvolume that is unusable.
     */
   public double getUnusable() {
      return unusable;
   }
   
   /**
     * The node subvolume that is usable.
     */
   public double getUsable() {
      return Math.max(this.getVolume() - unusable, 0.);
   }
   
   /**
     * This method is equivalent to setUnusable(getUnusable() + volume).
     */
   public void incrementUnusable(double volume) {
      setUnusable(unusable + volume);
   }
   
   /**
     * Set the size of the unusable subvolume. The size of the unusable
     * subvolume will be adjusted to V = x, where x is the argument. If
     * V < 0, V is silently set to to zero. If V is greater than the volume
     * of the node, then V is silently trimmed to this value.
     */
   public void setUnusable(double volume) {
      unusable = Math.max(Math.min(volume, getVolume()), 0.);
   }
   
   /**
     * This method is equivalent to setUnusable(volume() - amount).
     */
   public void setUsable(double amount) {
      setUnusable(getVolume() - amount);
   }
   
   /**
    * Flag the node as completely unusable.
     */
   public void setFullyUnusable() {
      unusable = getVolume();
   }
   
   /**
     * Flag the node as completely usable (no unusable subvolume).
     */
   public void setFullyUsable() {
      unusable = 0.;
   }
   
   /**
     * For argument x, sets the unusable subvolume, V, to V = x * total volume.
     * 
     * @param fraction
     *        (F) The fraction of the total volume to set unusable. If F < 0,
     *        F is silently set to zero. If F > 1, F is silently trimmed to 1.
     */
   public void setUnusableByFraction(double fraction) {
      unusable = Math.min(Math.max(fraction, 0.), 1.) * getVolume();
   }
   
   /**
     * For arguments x, this method is equivalent to setUnusableByFraction(1 - x).
     */
   public void setUsableByFraction(double fraction) {
      setUnusableByFraction(1. - fraction);
   }
   
   @Override
   public double getPricePerUnit() {
      return node.getPricePerUnit();
   }
   
   @Override
   public double getVolume() {
      return node.getVolume();
   }
   
   @Override
   public Object getObjectReference() {
      return node.getObjectReference();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Computation Node, usable volume: " + getUsable() + ", "
            + " unusable volume: " + getUnusable() + ".";
   }
}
