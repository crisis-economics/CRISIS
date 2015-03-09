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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
public final class MixedClearingNetworkTest {
   private static class TestContractCreationDelegate
      implements ResourceExchangeDelegate {
      
      private int
         demandNodeIndex,
         supplyNodeIndex;
      
      private String resourceName;
      
      TestContractCreationDelegate(
         final int demandNodeIndex,
         final int supplyNodeIndex
         ) {
         this(demandNodeIndex, supplyNodeIndex, "resource");
      }
      
      TestContractCreationDelegate(
         final int demandNodeIndex,
         final int supplyNodeIndex,
         final String resourceName
         ) {
         this.demandNodeIndex = demandNodeIndex;
         this.supplyNodeIndex = supplyNodeIndex;
         this.resourceName = "[" + resourceName + "]";
      }
      
      @Override
      public void commit(MixedClearingNetworkResult result) {
         System.out.printf(
            "%s New contract, parties: %5d %5d, rate: %16.10g, " + 
            "demand: %16.10g supply: %16.10g, excess: %+16.10g\n", 
            resourceName,
            demandNodeIndex, supplyNodeIndex, 
            result.getClearingRate(), 
            result.getDemandVolume(), 
            result.getSupplyVolume(),
            result.getDemandVolume() - result.getSupplyVolume()
            );
      }
   }
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing Mixed Clearing Networks...");
   }
   
   private void runAllTestsInSequence() {
      try {
         setUp();
         testPureHeterogeneousNetwork();
         testMixedNetworkClearing();
         multipleTestPureHomogeneousClearing();
         testPureDiscontinuousHeterogeneousNetwork();
         testPureHomogeneousNetworkWithTrivialSupplyObjective(50);
   //    testPureHeterogeneousNetworkWithObjectiveConverters(2, 2);
         testLCQPRelayNetwork();
         tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
   
   /**
     * An MCN unit test with the following properties:
     *   (a) a custom number of suppliers (N),
     *   (b) a custom number of consumers (M).
     * The network in this unit test is a completely heterogeneous
     * structure with N*M differentiated resources. The network
     * is cleared with an ascending march clearing algorithm. For
     * a network with N=100, M=100, it is expected that the residual
     * following clearing is at most 10^-10.
     */
   @Test
   public void testPureHeterogeneousNetwork() {
      /*
       * Test parameters
       */
      final int
         numSupplyNodes = 100,
         numDemandNodes = 100,
         maxCleraingIterationsPerEdge = 15,
         maxClearingIterations = 7;
      final double
         edgeClearingAccuracyGoal = 1.e-10,
         networkClearingAccuracyGoal = 1.e-10;
      
      System.out.println("Testing pure heterogeneous clearing..");
      
      long startTime = System.currentTimeMillis();
      
      MixedClearingNetworkAlgorithm clearingAlgorithm =
         new DescentMarchHeterogeneousClearingAlgorithm(
            maxCleraingIterationsPerEdge,
            edgeClearingAccuracyGoal,
            new TargetResidualOrMaximumIterationsStoppingCondition(
               networkClearingAccuracyGoal, maxClearingIterations)
            );
      
      List<MixedClearingTestNode>
         demandNodes = new ArrayList<MixedClearingTestNode>(),
         supplyNodes = new ArrayList<MixedClearingTestNode>();
      
      Random dice = new Random(6L);
      
      for(int i = 0; i< numDemandNodes; ++i)
         demandNodes.add(MixedClearingTestNode.createPolynomialDemandNode(dice, 1.));
      for(int j = 0; j< numSupplyNodes; ++j)
         supplyNodes.add(MixedClearingTestNode.createPolynomialSupplyNode(dice, 1.));
      
      MixedClearingNetwork.Builder builder = 
         new MixedClearingNetwork.Builder();
      for(int i = 0; i< numDemandNodes; ++i) {
         MixedClearingTestNode demandNode = demandNodes.get(i);
         builder.addNetworkNode(
            demandNode,
            demandNode.getResponseFunction(),
            demandNode.getUniqueID()
            );
      }
      for(int j = 0; j< numSupplyNodes; ++j) {
         MixedClearingTestNode supplyNode = supplyNodes.get(j);
         builder.addNetworkNode(
            supplyNode,
            supplyNode.getResponseFunction(),
            supplyNode.getUniqueID()
            );
      }
      
      // Complete graph connections
      
      for(int i = 0; i< numDemandNodes; ++i) {
         for(int j = 0; j< numSupplyNodes; ++j) {
            ResourceExchangeDelegate delegate = 
               new TestContractCreationDelegate(i, j);
            builder.addEdge(
               demandNodes.get(i).getUniqueID(),
               supplyNodes.get(j).getUniqueID(),
               delegate,
               new ClearingInstrument("Mock Market", "Bond")
               );
         }
      }
      
      MixedClearingNetwork network = builder.build();
      
      final double residual = network.applyClearingAlgorithm(clearingAlgorithm);
      Assert.assertTrue(Math.abs(residual) < 1.e-10);
      network.createContracts();
      
      System.out.println("Complete. Network residual: " + residual);
      
      long timeTaken = System.currentTimeMillis() - startTime;
      System.out.println("Time taken (ms): " + timeTaken);
      
      System.out.println("Pure heterogeneous clearing tests pass.");
   }
   
   /**
    * An MCN unit test with the following properties:
    *   (a) a custom number of suppliers (N),
    *   (b) a custom number of consumers (M),
    *   (c) several subnetworks composed of pure 
    *       homogeneous resources.
    * The network in this unit test consists of one pure
    * heterogeneous resource and a custom number of 
    * differentiated pure homogeneous resources forming a 
    * mixed network.
    * 
    * The network is cleared with an ascending march clearing 
    * algorithm. For a network with N=100, M=100, and 30 
    * homogeneous resources, it is expected that the residual
    * following clearing is at most 10^-10.
    */
   @Test
   public void testMixedNetworkClearing() {
      /*
       * Test parameters
       */
      final int
         numSupplyNodes = 20,
         numDemandNodes = 20,
         maxCleraingIterationsPerEdge = 20,
         maxClearingIterations = 20,
         numberOfHyperedges = 30;
      final double
         hyperEdgeProbability = .5,
         edgeClearingAccuracyGoal = 0.,
         networkClearingAccuracyGoal = 1.e-12;
      
      Random dice = new Random();
      
      /*
      // Key/Value pairs ({i, j}, v) in the attached map indicate the 
      // following: 
      // (a) v == -1: the edge {i, j} is a singleton not belonging to
      //     a hyperedge.
      // (b) v >= 0: the edge {i, j} is a hyperedge component belonging
      //     to the hyperedge with index v.
      */
      Map<Pair<Integer, Integer>, Integer> hyperedges = 
         new HashMap<Pair<Integer, Integer>, Integer>();
      {
      int counter = 0;
      for(int m = 0; m < numDemandNodes; ++m) {
         for(int n = 0; n < numSupplyNodes; ++n) {
            Pair<Integer, Integer> edgeIndices = new Pair<Integer, Integer>(m, n);
            if(counter < numberOfHyperedges) {
               hyperedges.put(edgeIndices, counter);
               ++counter;
               continue;
            }
            if(dice.nextDouble() >= hyperEdgeProbability)
               hyperedges.put(edgeIndices, -1);
            else {
               int hyperEdgeIndex = dice.nextInt(numberOfHyperedges);
               hyperedges.put(edgeIndices, hyperEdgeIndex);
            }
         }
      }
      }
      
      System.out.println("Testing mixed clearing network..");
      
      long startTime = System.currentTimeMillis();
      
      MixedClearingNetworkAlgorithm clearingAlgorithm =
         new AscentMarchHeterogeneousClearingAlgorithm(
            maxCleraingIterationsPerEdge,
            edgeClearingAccuracyGoal,
            new TargetResidualOrMaximumIterationsStoppingCondition(
               networkClearingAccuracyGoal, maxClearingIterations)
            );
      List<MixedClearingTestNode>
         demandNodes = new ArrayList<MixedClearingTestNode>(),
         supplyNodes = new ArrayList<MixedClearingTestNode>();
      
      for(int i = 0; i< numDemandNodes; ++i)
         demandNodes.add(MixedClearingTestNode.createPolynomialDemandNode(dice, 1.));
      for(int j = 0; j< numSupplyNodes; ++j)
         supplyNodes.add(MixedClearingTestNode.createPolynomialSupplyNode(dice, 1.));
      
      MixedClearingNetwork.Builder builder = 
         new MixedClearingNetwork.Builder();
      for(int i = 0; i< numDemandNodes; ++i) {
         MixedClearingTestNode demandNode = demandNodes.get(i);
         builder.addNetworkNode(
            demandNode,
            demandNode.getResponseFunction(),
            demandNode.getUniqueID()
            );
      }
      for(int j = 0; j< numSupplyNodes; ++j) {
         MixedClearingTestNode supplyNode = supplyNodes.get(j);
         builder.addNetworkNode(
            supplyNode,
            supplyNode.getResponseFunction(),
            supplyNode.getUniqueID()
            );
      }
      for(int k = 0; k< numberOfHyperedges; ++k)
         builder.addHyperEdge(Integer.toString(k));
      
      // Complete graph connections
      
      for(Entry<Pair<Integer, Integer>, Integer> record : hyperedges.entrySet()) {
         final int
            i = record.getKey().getFirst(),
            j = record.getKey().getSecond();
         ResourceExchangeDelegate delegate = 
            new TestContractCreationDelegate(i, j);
         final int hyperedgeIndex = record.getValue();
         if(hyperedgeIndex == -1)
            builder.addEdge(
               demandNodes.get(i).getUniqueID(),
               supplyNodes.get(j).getUniqueID(),
               delegate,
               new ClearingInstrument("Mock Market", "Bond")
               );
         else {
            builder.addToHyperEdge(
               demandNodes.get(i).getUniqueID(),
               supplyNodes.get(j).getUniqueID(),
               delegate,
               Integer.toString(hyperedgeIndex),
               new ClearingInstrument("Mock Market", "Test Resource")
               );
         }
      }
      
      MixedClearingNetwork network = builder.build();
      
      final double residual = network.applyClearingAlgorithm(clearingAlgorithm);
      Assert.assertTrue(Math.abs(residual) < 1.e-10);
      network.createContracts();
      
      System.out.println("Complete. Network residual: " + residual);
      
      long timeTaken = System.currentTimeMillis() - startTime;
      System.out.println("Time taken (ms): " + timeTaken);
      
      System.out.println("Mixed clearing tests pass.");
   }
   
   /**
     * An MCN unit test with the following properties:
     *   (a) a custom number of suppliers (N),
     *   (b) a custom number of consumers (M),
     *   (c) one pure homogeneous resource shared
     *       among all consumers and suppliers.
     * The value of M is set by the argument in the method
     * signature.
     * 
     * The network is cleared with an ascending march clearing 
     * algorithm. It is expected that the network residual should
     * be no greater than 10**-10 for a wide range of 
     * configurations.
     */
   @Test
   public void multipleTestPureHomogeneousClearing() {
      for(int i = 0; i< 100; ++i)
         testPureHomogeneousNetworkClearing(i);
   }
   
   private void testPureHomogeneousNetworkClearing(int seed) {
      /*
       * Test parameters
       */
      final int
         numSatelliteNodes = 100,
         numCentralNodes = (seed % 2 + 1),
         maxCleraingIterationsPerEdge = 25,
         maxClearingIterations = 20;
      final double
         edgeClearingAccuracyGoal = 1.e-10,
         networkClearingAccuracyGoal = 1.e-10;
      
      Random dice = new Random(seed);
      
      System.out.println("Testing mixed clearing network..");
      
      long startTime = System.currentTimeMillis();
      
      MixedClearingNetworkAlgorithm clearingAlgorithm =
         new AscentMarchHeterogeneousClearingAlgorithm(
            maxCleraingIterationsPerEdge,
            edgeClearingAccuracyGoal,
            new TargetResidualOrMaximumIterationsStoppingCondition(
               networkClearingAccuracyGoal, maxClearingIterations)
            );
      List<MixedClearingTestNode>
         demandNodes = new ArrayList<MixedClearingTestNode>(),
         supplyNodes = new ArrayList<MixedClearingTestNode>();
      
      for(int i = 0; i< numSatelliteNodes; ++i)
         demandNodes.add(MixedClearingTestNode.createPolynomialDemandNode(dice, 1./numSatelliteNodes));
      for(int j = 0; j< numCentralNodes; ++j)
         supplyNodes.add(MixedClearingTestNode.createPolynomialSupplyNode(dice, 5./numCentralNodes));
      
      MixedClearingNetwork.Builder builder = 
         new MixedClearingNetwork.Builder();
      for(MixedClearingTestNode demandNode : demandNodes) {
         builder.addNetworkNode(
            demandNode,
            demandNode.getResponseFunction(),
            demandNode.getUniqueID()
            );
      }
      for(MixedClearingTestNode supplyNode : supplyNodes) {
         builder.addNetworkNode(
            supplyNode,
            supplyNode.getResponseFunction(),
            supplyNode.getUniqueID()
            );
      }
      
      final ClearingInstrument resource = 
         new ClearingInstrument("Mock Market", "Shares");
      builder.addHyperEdge(resource.getUUID());
      
      for(int i = 0; i< demandNodes.size(); ++i) {
         for(int j = 0; j< supplyNodes.size(); ++j) {
            MixedClearingTestNode
               demandNode = demandNodes.get(i),
               supplyNode = supplyNodes.get(j);
            ResourceExchangeDelegate delegate = 
               new TestContractCreationDelegate(i, 0);
            builder.addToHyperEdge(
               demandNode.getUniqueID(),
               supplyNode.getUniqueID(),
               delegate,
               resource.getUUID(),
               resource
               );
         }
      }
      
      MixedClearingNetwork network = builder.build();
      
      final double residual = network.applyClearingAlgorithm(clearingAlgorithm);
      Assert.assertTrue(Math.abs(residual) < 1.e-10);
      network.createContracts();
      
      System.out.println("Complete. Network residual: " + residual);
      
      long timeTaken = System.currentTimeMillis() - startTime;
      System.out.println("Time taken (ms): " + timeTaken);
      
      System.out.println("Pure homogeneous clearing tests pass.");
   }
   
   /**
    * An MCN unit test with the following properties:
    *   (a) a custom number of suppliers (N),
    *   (b) a custom number of consumers (M),
    *   (c) one pure heterogeneous resources traded 
    *       among all consumers and suppliers,
    *   (d) supply and demand market responses which are
    *       discontinuous in multiple places.
    * The network is cleared with an ascending march clearing 
    * algorithm. It is expected that the network residual should
    * be no greater than 10**-7 for a wide range of 
    * configurations.
    */
   @Test
   public void testPureDiscontinuousHeterogeneousNetwork() {
      /*
       * Test parameters
       */
      final int
         numSupplyNodes = 100,
         numDemandNodes = 100,
         maxCleraingIterationsPerEdge = 15,
         maxClearingIterations = 7;
      final double
         edgeClearingAccuracyGoal = 1.e-10,
         networkClearingAccuracyGoal = 1.e-10;
      
      System.out.println("Testing pure heterogeneous discontinuous clearing..");
      
      long startTime = System.currentTimeMillis();
      
      MixedClearingNetworkAlgorithm clearingAlgorithm =
         new AscentMarchHeterogeneousClearingAlgorithm(
            maxCleraingIterationsPerEdge,
            edgeClearingAccuracyGoal,
            new TargetResidualOrMaximumIterationsStoppingCondition(
               networkClearingAccuracyGoal, maxClearingIterations)
            );
      List<MixedClearingTestNode>
         demandNodes = new ArrayList<MixedClearingTestNode>(),
         supplyNodes = new ArrayList<MixedClearingTestNode>();
      
      Random dice = new Random(6L);
      
      for(int i = 0; i< numDemandNodes; ++i)
         demandNodes.add(MixedClearingTestNode.createDiscontinuousDemandNode(dice, 1.));
      for(int j = 0; j< numSupplyNodes; ++j)
         supplyNodes.add(MixedClearingTestNode.createDiscontinuousSupplyNode(dice, 1.));
      
      MixedClearingNetwork.Builder builder = 
         new MixedClearingNetwork.Builder();
      for(int i = 0; i< numDemandNodes; ++i) {
         MixedClearingTestNode demandNode = demandNodes.get(i);
         builder.addNetworkNode(
            demandNode,
            demandNode.getResponseFunction(),
            demandNode.getUniqueID()
            );
      }
      for(int j = 0; j< numSupplyNodes; ++j) {
         MixedClearingTestNode supplyNode = supplyNodes.get(j);
         builder.addNetworkNode(
            supplyNode,
            supplyNode.getResponseFunction(),
            supplyNode.getUniqueID()
            );
      }
      
      // Complete graph connections
      
      for(int i = 0; i< numDemandNodes; ++i) {
         for(int j = 0; j< numSupplyNodes; ++j) {
            ResourceExchangeDelegate delegate = 
               new TestContractCreationDelegate(i, j);
            builder.addEdge(
               demandNodes.get(i).getUniqueID(),
               supplyNodes.get(j).getUniqueID(),
               delegate,
               new ClearingInstrument("Mock Market", "Bond")
               );
         }
      }
      
      MixedClearingNetwork network = builder.build();
      
      final double residual = network.applyClearingAlgorithm(clearingAlgorithm);
      Assert.assertTrue(Math.abs(residual) < 1.e-7);
      network.createContracts();
      
      System.out.println("Complete. Network residual: " + residual);
      
      long timeTaken = System.currentTimeMillis() - startTime;
      System.out.println("Time taken (ms): " + timeTaken);
      
      System.out.println("Pure discontinuous heterogeneous clearing tests pass.");
   }
   
// @Test
   public void testPureHeterogeneousNetworkWithObjectiveConverters(
      int numConsumers, int numSuppliers) {
      /*
       * Test parameters
       */
      final int
         numSupplyNodes = numConsumers,
         numDemandNodes = numSuppliers,
         maxClearingIterationsPerEdge = 15,
         maxClearingIterations = 7;
      final double
         edgeClearingAccuracyGoal = 1.e-10,
         networkClearingAccuracyGoal = 1.e-10;
      
      System.out.println("Testing pure heterogeneous clearing with objective converters..");
      
      long startTime = System.currentTimeMillis();
      
      MixedClearingNetworkAlgorithm clearingAlgorithm =
         new AscentMarchHeterogeneousClearingAlgorithm(
            maxClearingIterationsPerEdge,
            edgeClearingAccuracyGoal,
            new TargetResidualOrMaximumIterationsStoppingCondition(
               networkClearingAccuracyGoal, maxClearingIterations)
            );
      List<MixedClearingTestNode>
         demandNodes = new ArrayList<MixedClearingTestNode>(),
         supplyNodes = new ArrayList<MixedClearingTestNode>();
      
      Random dice = new Random(6L);
      
      for(int i = 0; i< numDemandNodes; ++i)
         demandNodes.add(MixedClearingTestNode.createDiscontinuousDemandNode(dice, 1.));
      for(int j = 0; j< numSupplyNodes; ++j)
         supplyNodes.add(MixedClearingTestNode.createDiscontinuousSupplyNode(dice, 1.));
      
      MixedClearingNetwork.Builder builder = 
         new MixedClearingNetwork.Builder();
      for(int i = 0; i< numDemandNodes; ++i) {
         MixedClearingTestNode demandNode = demandNodes.get(i);
         builder.addNetworkNode(
            demandNode,
            demandNode.getResponseFunction(),
            demandNode.getUniqueID()
            );
      }
      for(int j = 0; j< numSupplyNodes; ++j) {
         MixedClearingTestNode supplyNode = supplyNodes.get(j);
         builder.addNetworkNode(
            supplyNode,
            supplyNode.getResponseFunction(),
            supplyNode.getUniqueID()
            );
      }
      
      for(int i = 0; i< numDemandNodes; ++i) {
         for(int j = 0; j< numSupplyNodes; ++j) {
            ResourceExchangeDelegate delegate = 
               new TestContractCreationDelegate(i, j);
            builder.addEdge(
               demandNodes.get(i).getUniqueID(),
               supplyNodes.get(j).getUniqueID(),
               delegate,
               new ClearingInstrument("Mock Market", "Bond")
               );
         }
      }
      
      MixedClearingNetwork network = builder.build();
      
      final double residual = network.applyClearingAlgorithm(clearingAlgorithm);
      network.createContracts();
      
      System.out.println("Complete. Network residual: " + residual);
      
      long timeTaken = System.currentTimeMillis() - startTime;
      System.out.println("Time taken (ms): " + timeTaken);
      
      System.out.println("Pure heterogeneous clearing with objective converters tests pass.");
   }
   
   /**
     * An MCN unit test with the following properties:
     *   (a) one unique resource supplier, posting a 
     *       trivial market response: 0.
     *   (b) a custom number of consumers (M),
     *   (c) one pure homogeneous resource shared by all
     *       network edges.
     * The value of M is set by the argument in the method
     * signature. This test is an emulation of the procedure
     * used to the process the stock market via the clearing 
     * markets.
     * 
     * The network is cleared with an ascending march clearing 
     * algorithm. It is expected that the network residual should
     * be no greater than 10**-11 for a wide range of 
     * configurations.
     */
   @Test
   public void testPureHomogeneousNetworkWithTrivialSupplyObjective() {
      try {
         testPureHomogeneousNetworkWithTrivialSupplyObjective(50);
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
   
   private void testPureHomogeneousNetworkWithTrivialSupplyObjective(int numConsumers) {
      /*
       * Test parameters
       */
      final int
         numSupplyNodes = 1,
         numDemandNodes = numConsumers,
         maxClearingIterationsPerEdge = 25,
         maxClearingIterations = 10;
      final double
         edgeClearingAccuracyGoal = 1.e-10,
         networkClearingAccuracyGoal = 1.e-10;
      
      System.out.println("Testing pure homogeneous clearing with trivial supply.");
      
      long startTime = System.currentTimeMillis();
      
      MixedClearingNetworkAlgorithm clearingAlgorithm =
         new AscentMarchHeterogeneousClearingAlgorithm(
            maxClearingIterationsPerEdge,
            edgeClearingAccuracyGoal,
            new TargetResidualOrMaximumIterationsStoppingCondition(
               networkClearingAccuracyGoal, maxClearingIterations)
            );
      List<MixedClearingTestNode>
         demandNodes = new ArrayList<MixedClearingTestNode>(),
         supplyNodes = new ArrayList<MixedClearingTestNode>();
      
      Random dice = new Random(6L);
      
      for(int i = 0; i< numDemandNodes; ++i)
         demandNodes.add(MixedClearingTestNode.creatPartlyNegativeDemandNode(
            dice, .9 * dice.nextDouble() + .1, -dice.nextDouble()));
      for(int j = 0; j< numSupplyNodes; ++j)
         supplyNodes.add(MixedClearingTestNode.createTrivialSupplyNode());
      
      MixedClearingNetwork.Builder builder = 
         new MixedClearingNetwork.Builder();
      for(int i = 0; i< numDemandNodes; ++i) {
         MixedClearingTestNode demandNode = demandNodes.get(i);
         builder.addNetworkNode(
            demandNode,
            demandNode.getResponseFunction(),
            demandNode.getUniqueID()
            );
      }
      for(int j = 0; j< numSupplyNodes; ++j) {
         MixedClearingTestNode supplyNode = supplyNodes.get(j);
         builder.addNetworkNode(
            supplyNode,
            supplyNode.getResponseFunction(),
            supplyNode.getUniqueID()
            );
      }
      
      final ClearingInstrument resource = new ClearingInstrument("Mock Market", "Shares");
      builder.addHyperEdge(resource.getUUID());
      
      for(int i = 0; i< demandNodes.size(); ++i) {
         for(int j = 0; j< supplyNodes.size(); ++j) {
            MixedClearingTestNode
               demandNode = demandNodes.get(i),
               supplyNode = supplyNodes.get(j);
            ResourceExchangeDelegate delegate = 
               new TestContractCreationDelegate(i, 0);
            builder.addToHyperEdge(
               demandNode.getUniqueID(),
               supplyNode.getUniqueID(),
               delegate,
               resource.getUUID(),
               resource
               );
         }
      }
      
      MixedClearingNetwork network = builder.build();
      
      final double residual = network.applyClearingAlgorithm(clearingAlgorithm);
      Assert.assertTrue(Math.abs(residual) < 1.e-11);
      network.createContracts();
      
      System.out.println("Complete. Network residual: " + residual);
      
      long timeTaken = System.currentTimeMillis() - startTime;
      System.out.println("Time taken (ms): " + timeTaken);
      
      System.out.println("Pure heterogeneous clearing with trivial supply tests pass.");
   }
   
   /**
     * A test with:
     *  (a) consumers (firms), who accept cash as commercial loans,
     *  (b) intermediaries (banks), who accept cash as bonds and
     *      relay this as commercial loans with increased interest
     *      rates, and
     *  (c) suppliers (funds) who release cash as bonds.
     * This method is solved using a Powell Quadratic clearing
     * algorithm. It is expected that the newtwork residual should
     * be less than 10^-15 for 3 consumers, 4 intermediaries and
     * 4 suppliers.
     */
   @Test
   public void testLCQPRelayNetwork() {
      /*
       * Test parameters
       */
      final int
         numConsumerNodes = 3,
         numRelayNodes = 4,
         numSupplierNodes = 4;
      final double
         edgeClearingAccuracyGoal = 1.e-9,
         networkClearingAccuracyGoal = 1.e-9;
      
      System.out.println("Testing pure heterogeneous clearing with resource relay.");
      
      long startTime = System.currentTimeMillis();
      
//      MixedClearingNetworkAlgorithm clearingAlgorithm = 
//         new AscentMarchHeterogeneousClearingAlgorithm(
//             maxClearingIterationsPerEdge,
//             maxClearingIterations,
//             edgeClearingAccuracyGoal,
//             networkClearingAccuracyGoal
//             );
//      MixedClearingNetworkAlgorithm clearingAlgorithm = 
//         new LevenbergMarquardtClearingAlgorithm(20, 3000, 1.e-3, 1.e-3);
        MixedClearingNetworkAlgorithm clearingAlgorithm =
           new BoundedQuadraticEstimationClearingAlgorithm(
              20, 
              10000,
              networkClearingAccuracyGoal,
              edgeClearingAccuracyGoal
              );
//      MixedClearingNetworkAlgorithm clearingAlgorithm =
//         new NelderMeadClearingAlgorithm(20, 3000, 1.e-7, 1.e-7);
      
      List<MixedClearingTestNode>
         consumerNodes = new ArrayList<MixedClearingTestNode>(),
         relayNodes = new ArrayList<MixedClearingTestNode>(),
         supplierNodes = new ArrayList<MixedClearingTestNode>();
      
      Random dice = new Random(6L);
      
      // Add Commerical Loan consumer nodes.
      for(int i = 0; i< numConsumerNodes; ++i) {
         MixedClearingTestNode consumerNode =  MixedClearingTestNode.createPolynomialDemandNode(
               dice, 1.0); //.9 * dice.nextDouble() + .1)
         consumerNodes.add(consumerNode);
      }
      
      // Create Bond/Commerical Loan relay nodes.
      for(int j = 0; j< numRelayNodes; ++j) {
         final Set<String>
            assetTypes = new HashSet<String>(),
            liabilityTypes = new HashSet<String>();
         assetTypes.add("Commercial Loan");
         liabilityTypes.add("Bank Bond");
         Map<String, Pair<Double, Double>>
            assetReturns = new HashMap<String, Pair<Double,Double>>(),
            liabilityReturns = new HashMap<String, Pair<Double,Double>>();
         assetReturns.put("Commercial Loan", Pair.create(-1.e-5, 0.1));
         liabilityReturns.put("Bank Bond", Pair.create(+1.e-6, 0.05));
         relayNodes.add(
            MixedClearingTestNode.createRelayNode(
               dice,
               1.0,
               1.0,
               0.,
               assetTypes,
               liabilityTypes,
               assetReturns,
               liabilityReturns
               )
            );
      }
      
      // Create Bond supplier nodes.
      for(int k = 0; k< numSupplierNodes; ++k) {
         MixedClearingTestNode supplierNode = 
            MixedClearingTestNode.createPolynomialSupplyNode(
               dice, 1.0); //.9 * dice.nextDouble() + .1)
         supplierNodes.add(supplierNode);
      }
      
      MixedClearingNetwork.Builder builder = 
         new MixedClearingNetwork.Builder();
      
      // Add Commercial Loan consumer nodes to the network.
      for(int i = 0; i< numConsumerNodes; ++i) {
         final MixedClearingTestNode consumerNode = consumerNodes.get(i);
         builder.addNetworkNode(
            consumerNode,
            consumerNode.getResponseFunction(),
            consumerNode.getUniqueID()
            );
      }
      
      // Add Commercial Loan/Bond relay nodes to the network.
      for(int i = 0; i< numRelayNodes; ++i) {
         final MixedClearingTestNode relayNode = relayNodes.get(i);
         builder.addNetworkNode(
            relayNode,
            relayNode.getResponseFunction(),
            relayNode.getUniqueID()
            );
      }
      
      // Add Bond supplier nodes to the network.
      for(int i = 0; i< numSupplierNodes; ++i) {
         final MixedClearingTestNode supplyNode = supplierNodes.get(i);
         builder.addNetworkNode(
            supplyNode,
            supplyNode.getResponseFunction(),
            supplyNode.getUniqueID()
            );
      }
      
      // Add Heterogeneous Commerical Loan trade routes
      for(int i = 0; i< consumerNodes.size(); ++i) {
         for(int j = 0; j< relayNodes.size(); ++j) {
            MixedClearingTestNode
               consumerNode = consumerNodes.get(i),
               relayNode = relayNodes.get(j);
            ResourceExchangeDelegate delegate = 
               new TestContractCreationDelegate(i, j, "Commercial Loan");
            ClearingInstrument resource =
               new ClearingInstrument("Mock Market", "Commercial Loan");
            builder.addEdge(
               consumerNode.getUniqueID(),
               relayNode.getUniqueID(),
               delegate,
               resource
               );
         }
      }
      
      // Add heterogeneous Bond trade routes
      for(int j = 0; j< relayNodes.size(); ++j) {
         for(int k = 0; k< supplierNodes.size(); ++k) {
            MixedClearingTestNode
               relayNode = relayNodes.get(j),
               supplierNode = supplierNodes.get(k);
            ResourceExchangeDelegate delegate = 
               new TestContractCreationDelegate(j, k, "Bank Bond");
            ClearingInstrument resource =
               new ClearingInstrument("Mock Market", "Bank Bond");
            builder.addEdge(
               relayNode.getUniqueID(),
               supplierNode.getUniqueID(),
               delegate,
               resource
               );
         }
      }
      
      MixedClearingNetwork network = builder.build();
      
      final double residual = network.applyClearingAlgorithm(clearingAlgorithm);
      network.createContracts();
      
      System.out.println("Complete. Network residual: " + residual);
      
      long timeTaken = System.currentTimeMillis() - startTime;
      System.out.println("Time taken (ms): " + timeTaken);
      
      System.out.println(
         "Pure heterogeneous clearing with resource relay tests pass.");
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("Mixed Clearing Networks tests pass.");
   }
   
   /**
     * Manual entry point.
     */
   public static void main(String[] args) {
      MixedClearingNetworkTest test = new MixedClearingNetworkTest();
      test.runAllTestsInSequence();
   }
}
