/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.security.InvalidAlgorithmParameterException;

import org.testng.Assert;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.matching.Matching;
import eu.crisis_economics.abm.algorithms.matching.Matching.OneToOneMatch;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A simple node matching algorithm.
  * 
  * This algorithm interprets the right node group as a set
  * of buyers and the left node group as a set of sellers. 
  * 
  * Initially, a rationing algorithm, as specified by the
  * constructor, is applied to the nodes. Rationing algorithms
  * match supply and demand between the two groups.
  * 
  * The buyer node group is then shuffled. Trades are now 
  * executed between sellers and buyers, starting a the top
  * of both columns and working downward as follows:
  * 
  *    shuffled buyers               sellers
  * 1. quantity Q1, price P1         quantity Q1', price P1'
  * 2. quantity Q2, price P2         quantity Q2', price P2'
  *    ...                           ...
  * 
  * Whenever a potential trade (min(Q, Q'), P, P') is
  * identified, the algorithm makes the following assumptions:
  *    (a) the buyer price P takes priority. If the buyer
  *        price P <= P', then P is selected;
  *    (b) if the seller price P' < P, then a negotiation 
  *        between the buyer and the seller is assumed. The
  *        resulting execution price is taken to be (P'+P)/2.
  * 
  * The name of this algorithm (Forager) is based on its 
  * similarity to hunter-gatherers, who randomly encounter
  * natural resources, and accept, out of necessity, whatever
  * resources they find.
  * 
  * @author phillips
  */
public final class ForagerMatchingAlgorithm implements MatchingAlgorithm {
   
   private final RationingAlgorithm
      rationing;
   
   @Inject
   public ForagerMatchingAlgorithm(   // Immutable
   @Named("FORAGER_RATIONING_ALGORITHM")
      final RationingAlgorithm rationingAlgorithm
      ) {
      StateVerifier.checkNotNull(rationingAlgorithm);
      rationing = rationingAlgorithm;
   }
   
   @Override
   public Matching matchNodes(
      Collection<SimpleNode> left,
      Collection<SimpleNode> right
      ) throws InvalidAlgorithmParameterException
   {
       Matching.Builder builder = new Matching.Builder();
       
       if(left.isEmpty() || right.isEmpty())
          return builder.build();
       
       final List<ComputeNode> 
          leftWorkspace = new ArrayList<ComputeNode>(),
          rightWorkspace = new ArrayList<ComputeNode>();
       for(Node node : left)
          leftWorkspace.add(new ComputeNode(node));
       for(Node node : right)
          rightWorkspace.add(new ComputeNode(node));
       
       rationing.rationNodes(leftWorkspace, rightWorkspace);                    // Rationing
       
       final List<Integer> rightSidePermuation = new ArrayList<Integer>();
       for(int i = 0; i< right.size(); ++i)
          rightSidePermuation.add(i);
       Random rnd = new Random(Simulation.getSimState().random.nextLong());
       java.util.Collections.shuffle(rightSidePermuation, rnd);
       
       {
       int leftCursor = 0;
       for(int i = 0; i< right.size(); ++i) {
          int rightCursor = rightSidePermuation.get(i);
          final ComputeNode rightNode = rightWorkspace.get(rightCursor);
          double
             sought = rightNode.getUsable();
          if(sought == 0) continue;
          int j = leftCursor;
          for(; j< left.size(); ++j) {
             final ComputeNode leftNode = leftWorkspace.get(j);
             final double 
                offered = leftNode.getUsable();
             if(offered == 0) continue;
                double executionPrice = 
                   (rightNode.getPricePerUnit() <= leftNode.getPricePerUnit()) ?
                   rightNode.getPricePerUnit() :
                   .5 * (rightNode.getPricePerUnit() + leftNode.getPricePerUnit());
                if(offered <= rightNode.getUsable()) {
                   builder.addMatch(new OneToOneMatch(
                      leftNode, rightNode, offered, executionPrice));
                   rightNode.incrementUnusable(offered);
                   leftNode.setFullyUnusable();
                }
                else {
                   builder.addMatch(new OneToOneMatch(
                      leftNode, rightNode, sought, executionPrice));
                   leftNode.incrementUnusable(sought);
                   rightNode.setFullyUnusable();
                   break;
                }
                sought = rightNode.getUsable();
                if(sought == 0) break;
            }
            leftCursor = j;
        }
        }
        
        return builder.build();
    }
    
    // TODO: migrate to /test/
    static public void main(String[] args) {
        System.out.println("testing ForagerMatchingAlgorithm type..");
        
        class TestParameters {
            final double
                totalSupply,
                totalDemand;
            final int
                numSellers,
                numBuyers;
            TestParameters(
                double totalDemand,
                double totalSupply,
                int numSellers,
                int numBuyers) {
                this.totalSupply = totalSupply;
                this.totalDemand = totalDemand;
                this.numSellers = numSellers;
                this.numBuyers = numBuyers;
            }
        }
        
        TestParameters[] allTestConfigs = new TestParameters[]
            { 
                new TestParameters(50., 150., 1000, 5), 
                new TestParameters(150., 50,  1000, 5),
                new TestParameters(50., 150,  5, 1000),
                new TestParameters(150., 50,  5, 1000)
            };
        
        for(TestParameters parameters : allTestConfigs) {
            RationingAlgorithm globalRationing = new HomogeneousRationingAlgorithm();
            ForagerMatchingAlgorithm matchingAlgorithm = 
                 new ForagerMatchingAlgorithm(globalRationing);
            
            List<SimpleNode> 
                sellers = new ArrayList<SimpleNode>(),
                buyers = new ArrayList<SimpleNode>();
            
            double 
                totalSupply = parameters.totalSupply,
                totalDemand = parameters.totalDemand;
            int 
                numSellers = parameters.numSellers,
                numBuyers = parameters.numBuyers;
            
            java.util.Random dice = new java.util.Random(1L);
            
            { // Generate supply
                double supplyRemaining = totalSupply;
                for(int i = 0; i< numSellers - 1; ++i) {
                    double supply = totalSupply / numSellers + 
                        (2.e-2 * dice.nextDouble() - 1.e-2) * (totalSupply / numSellers);
                    sellers.add(new SimpleNode(1., supply, new Integer(i)));
                    supplyRemaining -= supply;
                }
                sellers.add(new SimpleNode(1., supplyRemaining, new Integer(numSellers - 1)));
            }
            { // Generate supply
                double demandRemaining = totalDemand;
                for(int i = 0; i< numBuyers - 1; ++i) {
                    double demand = totalDemand / numBuyers + 
                        (2.e-2 * dice.nextDouble() - 1.e-2) * (totalDemand / numBuyers);
                    buyers.add(new SimpleNode(.5 + dice.nextDouble(), demand, new Integer(i)));
                    demandRemaining -= demand;
                }
                buyers.add(new SimpleNode(1., demandRemaining, new Integer(numBuyers - 1)));
            }
            
            Matching match = null;
            try {
                match = matchingAlgorithm.matchNodes(sellers, buyers);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            {
            double[]
                supplyGiven = new double[numSellers],
                demandTaken = new double[numBuyers];
            for(OneToOneMatch trade : match) {
                int 
                    sellerIndex = (Integer)trade.leftNode.getObjectReference(),
                    buyerIndex = (Integer)trade.rightNode.getObjectReference();
                supplyGiven[sellerIndex] += trade.matchAmount;
                demandTaken[buyerIndex] += trade.matchAmount;
            }
            if(totalDemand > totalSupply) {
                double ration = totalSupply / totalDemand;
                for(int i = 0; i< numSellers; ++i)
                    Assert.assertEquals(supplyGiven[i], sellers.get(i).getVolume(), 1.e-12);
                for(int i = 0; i< numBuyers; ++i)
                    Assert.assertEquals(demandTaken[i], buyers.get(i).getVolume() * ration, 1.e-12);
            }
            else {
                double ration = totalDemand / totalSupply;
                for(int i = 0; i< numSellers; ++i)
                    Assert.assertEquals(supplyGiven[i], sellers.get(i).getVolume() * ration, 1.e-12);
                for(int i = 0; i< numBuyers; ++i)
                    Assert.assertEquals(demandTaken[i], buyers.get(i).getVolume(), 1.e-12);
            }
            }
        }
        
        System.out.println("ForagerMatchingAlgorithm tests pass");
        return;
    }
}
