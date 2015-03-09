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
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.MixedNetworkMarket.SubnetworkClearingMode;
import eu.crisis_economics.utilities.Pair;

/**
  * @author phillips
  */
public final class MixedNetworkMarketTest {
   
   private static class TestResourceExchangeDelegate
      implements ResourceExchangeDelegate {
      
      private double
         netIdealSignedResourceTrade,
         netIdealUnsignedResourceTrade;
      
      private final ClearingInstrument
         resource;
      
      TestResourceExchangeDelegate(final ClearingInstrument resource) {
         this.resource = resource;
         this.netIdealSignedResourceTrade = 0;
         this.netIdealUnsignedResourceTrade = 0;
      }
      
      @Override
      public void commit(MixedClearingNetworkResult result) {
         System.out.printf(
            "[%s] rate: %16.10g, demand: %16.10g supply: %16.10g, excess: %+16.10g\n",
            resource.toString(),
            result.getClearingRate(), 
            result.getDemandVolume(), 
            result.getSupplyVolume(),
            result.getDemandVolume() - result.getSupplyVolume()
            );
         netIdealSignedResourceTrade += result.getDemandVolume() - result.getSupplyVolume();
         netIdealUnsignedResourceTrade += result.getDemandVolume() + result.getSupplyVolume();
      }
      
      public double getNetIdealSignedResourceTrade() {
         return netIdealSignedResourceTrade;
      }
      
      public double getNetIdealUnsignedResourceTrade() {
         return netIdealUnsignedResourceTrade;
      }
   }
   
   @BeforeMethod
   public void setUp() {
      System.out.println("Testing simplified MCN launchers...");
   }
   
   @Test
   public void runAllTestsInSequence() {
      try {
         testMixedNetworkMarket(10, 10, 10, .5);
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
   
   private void testMixedNetworkMarket(
      final int numConsumers,
      final int numSuppliers,
      final int numResources,
      final double probabilityIsHeterogeneousResource
      ) {
      System.out.println("Testing mixed network market.");
      
      Random dice = new Random(11);
      
      /*
       * Network specification.
       * 
       * Each resource in the network corresponds to one of:
       * (a) a pure heterogeneous bond resource, connecting 
       *     consumers and suppliers via a complete bipartite
       *     subnetwork, or
       * (b) a pure homogeneous stock resource, connecting all
       *     suppliers to one distinguished supply node ('the 
       *     exchange').
       */
      Map<
         ClearingInstrument, 
         Pair<ResourceExchangeDelegate, SubnetworkClearingMode>
         > subnetworks = new HashMap<
            ClearingInstrument, Pair<ResourceExchangeDelegate, SubnetworkClearingMode>>();
      
      int
         numHomogeneousResources = 0,
         numHeterogeneousResources = 0;
      List<String> homogeneousResourceNames = new ArrayList<String>();
      
      final List<TestResourceExchangeDelegate> results =
         new ArrayList<TestResourceExchangeDelegate>();
      
      for(int i = 0; i< numResources; ++i) {
         final ClearingInstrument resource = 
            new ClearingInstrument("TestResource" + Integer.toString(i), "");
         final SubnetworkClearingMode subnetworkType;
         if(dice.nextDouble() > probabilityIsHeterogeneousResource) {
            subnetworkType = SubnetworkClearingMode.HOMOGENEOUS;
            homogeneousResourceNames.add(resource.getName());
            numHomogeneousResources++;
         } else {
            subnetworkType = SubnetworkClearingMode.HETEROGENEOUS;
            numHeterogeneousResources++;
         }
         final TestResourceExchangeDelegate delegate =
            new TestResourceExchangeDelegate(resource);
         results.add(delegate);
         final Pair<ResourceExchangeDelegate, SubnetworkClearingMode> subnetwork = 
            Pair.create((ResourceExchangeDelegate)(delegate), subnetworkType);
         subnetworks.put(resource, subnetwork);
      }
      
      System.out.println(
         "Number of homogeneous resources: " + numHomogeneousResources + ", " +
         "Number of heterogeneous resources: " + numHeterogeneousResources + "."
         );
      
      MixedNetworkMarket market = new MixedNetworkMarket(subnetworks);
      
      /*
       * Test parameters
       */
      final int
         numRegularConsumers = numConsumers,
         numRegularSuppliers = numSuppliers;
      
      long startTime = System.currentTimeMillis();
      
      List<MixedClearingTestNode>
         demandNodes = new ArrayList<MixedClearingTestNode>(),
         supplyNodes = new ArrayList<MixedClearingTestNode>(),
         stockDistributionNodes = new ArrayList<MixedClearingTestNode>();
      
      // Regular Consumers
      for(int i = 0; i< numRegularConsumers; ++i)
         demandNodes.add(MixedClearingTestNode.createDiscontinuousDemandNode(
            dice, (double)numRegularSuppliers));
      
      // Regular Suppliers
      for(int j = 0; j< numRegularSuppliers; ++j)
         supplyNodes.add(MixedClearingTestNode.createDiscontinuousSupplyNode(
            dice, (double)numRegularConsumers));
      
      // Stock Distribution Nodes
      for(int k = 0; k< numHomogeneousResources; ++k) {
         stockDistributionNodes.add(MixedClearingTestNode.createTrivialSupplyNode());
      }
      
      for(int i = 0; i< numRegularConsumers; ++i) {
         MixedClearingTestNode demandNode = demandNodes.get(i);
         market.addBuyOrder(
            demandNode,
            demandNode.getUniqueID(),
            demandNode.getResponseFunction()
            );
      }
      for(int j = 0; j< numRegularSuppliers; ++j) {
         MixedClearingTestNode supplyNode = supplyNodes.get(j);
         market.addSellOrder(
            supplyNode,
            supplyNode.getUniqueID(),
            supplyNode.getResponseFunction()
            );
      }
      for(int k = 0; k< homogeneousResourceNames.size(); ++k) {
         MixedClearingTestNode supplyNode = stockDistributionNodes.get(k);
         market.addSellOrder(
            supplyNode,
            supplyNode.getUniqueID(),
            supplyNode.getResponseFunction()
            );
      }
      
      market.matchAllOrders(30, 15,
         new TargetResidualOrMaximumIterationsStoppingCondition(1.e-8, 15));
      
      for(TestResourceExchangeDelegate delegate : results) {
         Assert.assertTrue(
           Math.abs(delegate.getNetIdealSignedResourceTrade())/
              (numConsumers * numSuppliers) < 1.e-3);
         Assert.assertTrue(delegate.getNetIdealUnsignedResourceTrade() > 1.e-1);
      }
      
      long timeTaken = System.currentTimeMillis() - startTime;
      System.out.println("Time taken (ms): " + timeTaken);
      
      System.out.println("Mixed network market tests pass.");
   }
   
   @AfterMethod
   public void tearDown() {
      System.out.println("Simplified MCN launchers tests complete.");
   }
   
   /**
     * Manual entry point.
     */
   public static void main(String[] args) {
      MixedNetworkMarketTest test = new MixedNetworkMarketTest();
      test.setUp();
      test.runAllTestsInSequence();
      test.tearDown();
   }
}
