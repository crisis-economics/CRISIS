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
package eu.crisis_economics.abm.algorithms.matching;

import java.util.Collection;
import java.security.InvalidAlgorithmParameterException;

import com.google.inject.Inject;

/**
  * @author phillips
  */
public final class HomogeneousRationingAlgorithm implements RationingAlgorithm {
   
   /**
     * Create a homogeneous rationing algorithm.
     */
   @Inject
   public HomogeneousRationingAlgorithm() { }   // Stateless
   
   /**
     * Ration a pair of node collections, A and B.
     * 
     * This algorithm matches the supply of A to the demand of B
     * by applying a homogeneous multiplier (a ration factor) to 
     * either group A or group B. If:
     *    (a) The aggregate supply (S) of node group A is greater
     *        than the aggregate demand (D) of node group B, then
     *        the supply multiplier D/S is applied to every node 
     *        in A;
     *    (b) the aggregate supply (S) of node group A is less
     *        than the aggregate demand (D) of node group B, then
     *        the supply multiplier S/D is applied to every node 
     *        in B.
     * No action is taken if D = S. It is never the case A and B
     * are both modified.
     */
   @Override
   public void rationNodes(
      Collection<ComputeNode> leftNodes,   // Modified
      Collection<ComputeNode> rightNodes   // Modified
      ) throws InvalidAlgorithmParameterException
   {
      double 
         usableLeft = 0.,
         usableRight = 0.;
      for(ComputeNode node : leftNodes) 
         usableLeft += node.getUsable();
      for(ComputeNode node : rightNodes) 
         usableRight += node.getUsable();
      
      if(usableLeft == usableRight) return;
      
      final double rationFactor;
         if(usableLeft > usableRight) {
            rationFactor = 1. - usableRight / usableLeft;
            for(ComputeNode node : leftNodes)
               node.setUnusableByFraction(rationFactor);
         }
         else {
            rationFactor = 1. - usableLeft / usableRight;
            for(ComputeNode node : rightNodes) 
               node.setUnusableByFraction(rationFactor);
         }
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Global Rationing Algorithm.";
   }
}