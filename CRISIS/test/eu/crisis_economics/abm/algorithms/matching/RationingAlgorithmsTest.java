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

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.algorithms.matching.ComputeNode;
import eu.crisis_economics.abm.algorithms.matching.Node;
import eu.crisis_economics.abm.algorithms.matching.RandomDenyRationing;
import eu.crisis_economics.abm.algorithms.matching.SimpleNode;
import eu.crisis_economics.abm.simulation.EmptySimulation;

public class RationingAlgorithmsTest {
   private SimState state;
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing RationingAlgorithmsTest..");
      state = new EmptySimulation(0L);
      state.start();
   }
   
   private static interface CreateAlgorithm {
      public RationingAlgorithm create();
   }
   
   private static class CreateRandomDenyRationingAlgorithm implements CreateAlgorithm {
      @Override
      public RationingAlgorithm create() {
         return new RandomDenyRationing();
      }
   }
   
   private static class CreateGlobalRationingAlgorithm implements CreateAlgorithm {
      @Override
      public RationingAlgorithm create() {
         return new HomogeneousRationingAlgorithm();
      }
   }
   
   @Test
   public void testRandomDenyRationingAlgorithm() {
      testAlgorithm(new CreateRandomDenyRationingAlgorithm());
   }
   
   @Test
   public void testGlobalRationingAlgorithm() {
      testAlgorithm(new CreateGlobalRationingAlgorithm());
   }
   
   private void testAlgorithm(final CreateAlgorithm builder) {
      Random dice = new Random(1L);
      final int numTests = 1000;
      
      // Nontrivial demand/supply; typically neither node collection is empty:
      for(int i = 0; i< numTests; ++i) {
         Collection<ComputeNode>
            leftNodes = generateRandomNodeCollection(dice),
            rightNodes = generateRandomNodeCollection(dice);
         RationingAlgorithm rationingAlgorithm = builder.create();
         try {
            rationingAlgorithm.rationNodes(leftNodes, rightNodes);
         } catch (final InvalidAlgorithmParameterException e) {
            Assert.fail();
         }
         assessResult(leftNodes, rightNodes);
      }
      
      // Nontrivial demand/supply: left node group is empty:
      for(int i = 0; i< numTests; ++i) {
         Collection<ComputeNode>
            leftNodes = generateEmptyNodeCollection(),
            rightNodes = generateRandomNodeCollection(dice);
         RationingAlgorithm rationingAlgorithm = builder.create();
         try {
            rationingAlgorithm.rationNodes(leftNodes, rightNodes);
         } catch (final InvalidAlgorithmParameterException e) {
            Assert.fail();
         }
         assessResult(leftNodes, rightNodes);
      }
      
      // Nontrivial demand/supply: right node group is empty:
      for(int i = 0; i< numTests; ++i) {
         Collection<ComputeNode>
            leftNodes = generateRandomNodeCollection(dice),
            rightNodes = generateEmptyNodeCollection();
         RationingAlgorithm rationingAlgorithm = builder.create();
         try {
            rationingAlgorithm.rationNodes(leftNodes, rightNodes);
         } catch (final InvalidAlgorithmParameterException e) {
            Assert.fail();
         }
         assessResult(leftNodes, rightNodes);
      }
      
      // Nontrivial demand/supply: both node groups are empty:
      for(int i = 0; i< numTests; ++i) {
         Collection<ComputeNode>
            leftNodes = generateEmptyNodeCollection(),
            rightNodes = generateEmptyNodeCollection();
         RationingAlgorithm rationingAlgorithm = builder.create();
         try {
            rationingAlgorithm.rationNodes(leftNodes, rightNodes);
         } catch (final InvalidAlgorithmParameterException e) {
            Assert.fail();
         }
         assessResult(leftNodes, rightNodes);
      }
      
      // Nontrivial demand/supply: left node group nonempty, has trivial supply:
      for(int i = 0; i< numTests; ++i) {
         Collection<ComputeNode>
            leftNodes = generateNonEmptyNodeCollectionWithTrivialSupply(dice),
            rightNodes = generateRandomNodeCollection(dice);
         RationingAlgorithm rationingAlgorithm = builder.create();
         try {
            rationingAlgorithm.rationNodes(leftNodes, rightNodes);
         } catch (final InvalidAlgorithmParameterException e) {
            Assert.fail();
         }
         assessResult(leftNodes, rightNodes);
      }
      
      // Nontrivial demand/supply: right node group nonempty, has trivial supply:
      for(int i = 0; i< numTests; ++i) {
         Collection<ComputeNode>
            leftNodes = generateRandomNodeCollection(dice),
            rightNodes = generateNonEmptyNodeCollectionWithTrivialSupply(dice);
         RationingAlgorithm rationingAlgorithm = builder.create();
         try {
            rationingAlgorithm.rationNodes(leftNodes, rightNodes);
         } catch (final InvalidAlgorithmParameterException e) {
            Assert.fail();
         }
         assessResult(leftNodes, rightNodes);
      }
      
      // Nontrivial demand/supply: both node group nonempty, with trivial supply:
      for(int i = 0; i< numTests; ++i) {
         Collection<ComputeNode>
            leftNodes = generateNonEmptyNodeCollectionWithTrivialSupply(dice),
            rightNodes = generateNonEmptyNodeCollectionWithTrivialSupply(dice);
         RationingAlgorithm rationingAlgorithm = builder.create();
         try {
            rationingAlgorithm.rationNodes(leftNodes, rightNodes);
         } catch (final InvalidAlgorithmParameterException e) {
            Assert.fail();
         }
         assessResult(leftNodes, rightNodes);
      }
   }
   
   /**
     * Generate a random node collection.
     * Constraints:
     *   (a) price per unit is positive, or zero, in all cases,
     *       and is computed individually for each node;
     *   (b) desired volumes are positive, and zero with finite
     *       probability;
     *   (c) the collection may be empty, with finite probability.
     */
   private Collection<ComputeNode> generateRandomNodeCollection(Random dice) {
      final int
         numElements = dice.nextInt(100);
      List<ComputeNode> result = new ArrayList<ComputeNode>();
      for(int i = 0; i< numElements; ++i) {
         final double
            price = Math.pow(dice.nextGaussian(), 2.),
            volumeDesired = dice.nextDouble() < .02 ? 0. : Math.pow(dice.nextGaussian(), 2.);
         Node node = new SimpleNode(price, volumeDesired, null);
         result.add(new ComputeNode(node));
      }
      return result;
   }
   
   /**
     * Generate an empty node collection.
     */
   private Collection<ComputeNode> generateEmptyNodeCollection() {
      return new ArrayList<ComputeNode>();
   }
   
   /**
     * Generate a nonempty node collection with trivial supply.
     */
   private Collection<ComputeNode>
      generateNonEmptyNodeCollectionWithTrivialSupply(Random dice) {
      final int
         numElements = dice.nextInt(100) + 1;
      List<ComputeNode> result = new ArrayList<ComputeNode>();
      for(int i = 0; i< numElements; ++i) {
         final double
            price = Math.pow(dice.nextGaussian(), 2.),
            volumeDesired = 0.;
         Node node = new SimpleNode(price, volumeDesired, null);
         result.add(new ComputeNode(node));
      }
      return result;
   }
   
   /**
     * Assess the result of rationing session.
     */
   private void assessResult(
      final Collection<ComputeNode> leftNodes,
      final Collection<ComputeNode> rightNodes
      ) {
      final double
         usableLeft = getTotalNodeOffering(leftNodes),
         usableRight = getTotalNodeOffering(rightNodes);
      Assert.assertEquals(usableLeft, usableRight, 1.e-8);
   }
   
   /**
     * Get the total usable volume over a set of nodes.
     */
   private double getTotalNodeOffering(
      final Collection<ComputeNode> nodes) {
      double result = 0;
      for(final ComputeNode node : nodes)
         result += node.getUsable();
      return result;
   }
   
   @BeforeMethod
   public void tearDown() {
      System.out.println("RationingAlgorithmsTest tests pass.");
      state.finish();
      state = null;
   }
   
   /*
    * Manual entry point.
    */
   static public void main(String[] args) {
      try {
         RationingAlgorithmsTest test = new RationingAlgorithmsTest();
         test.setUp();
         test.testRandomDenyRationingAlgorithm();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
      
      try {
         RationingAlgorithmsTest test = new RationingAlgorithmsTest();
         test.setUp();
         test.testGlobalRationingAlgorithm();
         test.tearDown();
      }
      catch(Exception e) {
         Assert.fail();
      }
   }
}
