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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.security.InvalidAlgorithmParameterException;

import org.testng.Assert;

/**
 * @author      JKP
 * @category    Rationing Algorithms
 * @see         eu.crisis_economics.algorithms.matching.package-info.java
 * @since       1.0
 * @version     1.0
 */
public final class WorstPropositionRationing implements RationingAlgorithm {
    
    private enum GoverningSide { BUYERS, SELLERS }
    
    public WorstPropositionRationing() { }
    
    @Override
    public void rationNodes(
        Collection<ComputeNode> leftNodes,   // Modified
        Collection<ComputeNode> rightNodes   // Modified
        ) throws InvalidAlgorithmParameterException
    {
        double 
            allocatableLeft = 0.,
            allocatableRight = 0.;
        for (ComputeNode node : leftNodes) 
            allocatableLeft += node.getUsable();
        for (ComputeNode node : rightNodes)
            allocatableRight += node.getUsable();
        
        // No rationing required.
        if(allocatableLeft == allocatableRight) return;
        
        final GoverningSide governingSide = 
            (allocatableRight > allocatableLeft) ? 
                GoverningSide.BUYERS : GoverningSide.SELLERS;
        
        switch(governingSide) {
        case BUYERS:
            {
               final double globalRationFactor = 
                  1. - allocatableLeft/allocatableRight;
               for(ComputeNode node : rightNodes) {
                  node.setUnusableByFraction(globalRationFactor);
              }
            }
            break;
        case SELLERS:
           {
              class PriceOffering implements Comparable<PriceOffering> {
                 double priceOffering;
                 ComputeNode parentNode;
                 public PriceOffering(
                     double price,
                     ComputeNode parentNode) {
                     this.priceOffering = price;
                     this.parentNode = parentNode;
                 }
                 @Override
                 public int compareTo(PriceOffering other) {
                      return Double.compare(
                         priceOffering, other.priceOffering);
                 }
              }
              List<PriceOffering> priceOfferings = new ArrayList<PriceOffering>();
              for(ComputeNode node : leftNodes) {
                  PriceOffering newPriceOffering = new PriceOffering(
                      node.getPricePerUnit(), node);
                  priceOfferings.add(newPriceOffering);
              }
              Collections.sort(priceOfferings);
                 
              double allocationRemaining = 
                  Math.min(allocatableLeft, allocatableRight);
              for(int i = 0; i< priceOfferings.size(); ++i) {
                  PriceOffering offerNow = priceOfferings.get(i);
                  if(allocationRemaining <= 0) {
                      offerNow.parentNode.setFullyUnusable();
                      continue;
                  }
                  double requestedAmont =
                      offerNow.parentNode.getUsable();
                  if(requestedAmont <= allocationRemaining) {
                      allocationRemaining -= requestedAmont;
                      continue;
                  }
                  double amountNotObtainable =
                      (requestedAmont - allocationRemaining);
                  offerNow.parentNode.incrementUnusable(amountNotObtainable);
                     allocationRemaining = 0;
                     continue;
              }
           }
           break;
        default:
            throw new IllegalStateException();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Testing WorstPropositionRationing type..");
        
        {
            List<ComputeNode> 
                leftNodes = new ArrayList<ComputeNode>(),
                rightNodes = new ArrayList<ComputeNode>();
            
            leftNodes.add(new ComputeNode(new SimpleNode(1.0, 10., null)));
            leftNodes.add(new ComputeNode(new SimpleNode(1.1, 10., null)));
            leftNodes.add(new ComputeNode(new SimpleNode(1.2, 10., null)));
            leftNodes.add(new ComputeNode(new SimpleNode(1.3, 10., null)));
            leftNodes.add(new ComputeNode(new SimpleNode(1.4, 10., null)));
            leftNodes.add(new ComputeNode(new SimpleNode(1.5, 10., null)));
            
            rightNodes.add(new ComputeNode(new SimpleNode(2.1, 15 + 10, null)));
            rightNodes.add(new ComputeNode(new SimpleNode(2.0, 15 + 5, null)));
            rightNodes.add(new ComputeNode(new SimpleNode(1.9, 15 + 1, null)));
            rightNodes.add(new ComputeNode(new SimpleNode(1.8, 15 - 5, null)));
            
            WorstPropositionRationing rationAlgorithm = 
               new WorstPropositionRationing();
            try {
                rationAlgorithm.rationNodes(leftNodes, rightNodes);
            } catch (InvalidAlgorithmParameterException e) {
                Assert.fail();
            }
            
            Double[] expectedAllocatable = new Double[] { 25., 20., 15., 0. };
            
            System.out.println("left group:");
            for(int i = 0; i< leftNodes.size(); ++i)
                System.out.println(leftNodes.get(i).toString());
            System.out.println("right group:");
            for(int i = 0; i< rightNodes.size(); ++i) {
                ComputeNode node = rightNodes.get(i);
                System.out.println(node.toString());
                Assert.assertEquals(
                    expectedAllocatable[i], node.getUsable());
            }
        }
        {
            List<ComputeNode> 
                leftNodes = new ArrayList<ComputeNode>(),
                rightNodes = new ArrayList<ComputeNode>();
            
            rightNodes.add(new ComputeNode(new SimpleNode(1.0, 10., null)));
            rightNodes.add(new ComputeNode(new SimpleNode(1.1, 10., null)));
            rightNodes.add(new ComputeNode(new SimpleNode(1.2, 10., null)));
            rightNodes.add(new ComputeNode(new SimpleNode(1.3, 10., null)));
            rightNodes.add(new ComputeNode(new SimpleNode(1.4, 10., null)));
            rightNodes.add(new ComputeNode(new SimpleNode(1.5, 10., null)));
            
            leftNodes.add(new ComputeNode(new SimpleNode(2.1, 15 + 10, null)));
            leftNodes.add(new ComputeNode(new SimpleNode(2.0, 15 + 5, null)));
            leftNodes.add(new ComputeNode(new SimpleNode(1.9, 15 + 1, null)));
            leftNodes.add(new ComputeNode(new SimpleNode(1.8, 15 - 5, null)));
            
            WorstPropositionRationing rationAlgorithm = 
               new WorstPropositionRationing();
            try {
                rationAlgorithm.rationNodes(leftNodes, rightNodes);
            } catch (InvalidAlgorithmParameterException e) {
                Assert.fail();
            }
            
            Double[] expectedAllocatable = new Double[] { 25., 20., 15., 0. };
            
            System.out.println("right group:");
            for(int i = 0; i< rightNodes.size(); ++i)
                System.out.println(rightNodes.get(i).toString());
            System.out.println("left group:");
            for(int i = 0; i< leftNodes.size(); ++i) {
                ComputeNode node = leftNodes.get(i);
                System.out.println(node.toString());
                Assert.assertEquals(
                    expectedAllocatable[i], node.getUsable(), 1.e-8);
            }
        }
        
        System.out.println("WorstPropositionRationing tests pass.");
    }
}