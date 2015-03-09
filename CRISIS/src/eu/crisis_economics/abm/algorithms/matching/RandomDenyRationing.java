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

import java.security.InvalidAlgorithmParameterException;
import java.util.Collection;
import java.util.Random;
import java.lang.Math;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * Resource rationing by random denial.
  * 
  * @author phillips
  */
public final class RandomDenyRationing implements RationingAlgorithm {
   
   public final static double
      DEFAULT_INHOMOGENEITY_OF_RATIONING = .05;
   
   private final Random
      dice;
   
   private final double
      inhomogeneityOfRationing;
   
   /**
     * Create a new {@link RandomDenyRationing} algorithm with custom
     * parameters.
     * 
     * @param inhomogeneityOfRationing
     *        A number between {@code 0.0} and {@code 1.0}. This number
     *        describes the aggressiveness of inhomogeneous rationing 
     *        to be applied by this algorithm. The value {@code 0.0}
     *        describes an algorithm with no inhomogeneity. The value 
     *        {@code 1.0} describes an algorithm which can, in principle, 
     *        completely exclude some participants from the market in order
     *        to match market supply and demand.
     */
   @Inject
   public RandomDenyRationing(
   @Named("RANDOM_DENY_RATIONING_ALGORITHM_INHOMOGENEITY_FACTOR")
      final double inhomogeneityOfRationing
      ) {
      this.dice = new Random(1L);
      this.inhomogeneityOfRationing = inhomogeneityOfRationing;
   }
   
   /**
     * Create a new {@link RandomDenyRationing} algorithm with default
     * parameters.
     */
   public RandomDenyRationing() {
      this(DEFAULT_INHOMOGENEITY_OF_RATIONING);
   }
   
   @Override
   public void rationNodes(
      Collection<ComputeNode> leftNodes,                            // Modified
      Collection<ComputeNode> rightNodes                            // Modified
      ) throws InvalidAlgorithmParameterException {
      if(leftNodes == null || rightNodes == null)
         throw new InvalidAlgorithmParameterException();
      
      final double
         volumeOnLeft = getTotalNodeOffering(leftNodes),
         volumeOnRight = getTotalNodeOffering(rightNodes);
      
      if(volumeOnLeft == 0. && volumeOnRight == 0.)
         return;
      else if(volumeOnLeft > volumeOnRight)
         applyRationing(volumeOnRight, leftNodes);
      else if(volumeOnRight > volumeOnLeft)
         applyRationing(volumeOnLeft, rightNodes);
      else return;
   }
   
   /**
     * Apply a non-uniform rationing to the node collection (argument 3).
     * 
     * @param targetOffering (T)
     *        The target offering, following rationing, for the node
     *        collection. This argument can be zero. If T < 0, T is 
     *        silently set to zero.
     * @param nodes (N)
     *        The nodes to operate on. If T > M, where M is the maximum
     *        volume summed over all nodes in N, then no action is taken.
     */
   private void applyRationing(
      final double targetOffering,
      final Collection<ComputeNode> nodes
      ) {
      if(targetOffering <= 0.) {
         for(ComputeNode node : nodes)
            node.setFullyUnusable();
         return;                                                    // Target offering is zero.
      }
      double[] fs = new double[nodes.size()];
      final double
         maxOffering = getTotalNodeOffering(nodes);
      if(maxOffering == 0. || targetOffering >= maxOffering) {
         for(ComputeNode node : nodes)
            node.setFullyUsable();
         return;                                                    // All fully usable, return.
      }
      final double
         homogeneousF = targetOffering / maxOffering,
         __1minusHomogeneousF = Math.max(0., 1. - homogeneousF);
      int i = 0;
      for(; i< nodes.size(); ++i) {
         double
            f = homogeneousF + inhomogeneityOfRationing * dice.nextDouble() * __1minusHomogeneousF;
         f = Math.max(f, 0.); 
         f = Math.min(f, 1.);
         fs[i] = f;
      }
      double
         offering = 0.;
      i = 0;
      for(final ComputeNode node : nodes)
         offering += node.getVolume() * fs[i++];
      if(offering == targetOffering) {
         i = 0;
         for(final ComputeNode node : nodes)
            node.setUsableByFraction(fs[i++]);
         return;
      }
      i = 0;
      final double
         globalF = targetOffering / offering;
      i = 0;
      for(final ComputeNode node : nodes)
         node.setUsableByFraction(globalF * fs[i++]);
      return;                                                       // Success
   }
   
   /**
     * Get the total volume over a set of nodes.
     */
   private double getTotalNodeOffering(final Collection<ComputeNode> nodes) {
      double result = 0;
      for(final ComputeNode node : nodes)
         result += node.getVolume();
      return result;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Random Denial Rationing Algorithm.";
   }
}