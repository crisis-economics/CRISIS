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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.List;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.algorithms.optimization.ValueBisectorAlgorithm;

/**
  * @author phillips
  */
abstract class MarchingHeterogeneousClearingAlgorithm
   implements MixedClearingNetworkAlgorithm {
   
   private final int
      maxIterationsPerEdge;
   private final double
      accuracyGoalPerEdge;
   private MixedClearingNetworkAlgorithmStoppingCondition
      stoppingCondition;
   
   protected static class MeritFunction
      implements ValueBisectorAlgorithm.Bisectable {
      
      private MixedClearingNetworkEdge edge;
      
      MeritFunction(final MixedClearingNetworkEdge edge) {
         this.edge = edge;
      }
      
      @Override
      public double functionValue(double rate) {
         edge.setEdgeRate(rate);
         edge.flagNodesResponsesForUpdate();
         edge.updateNodeResponses();
         return (edge.getDemandResponse() + edge.getSupplyResponse());
      }
   }
   
   protected MarchingHeterogeneousClearingAlgorithm( // Immutable
      final int maxIterationsPerEdge,
      final double accuracyGoalPerEdge,
      final MixedClearingNetworkAlgorithmStoppingCondition stoppingCondition
      ) {
      Preconditions.checkArgument(maxIterationsPerEdge >= 0);
      Preconditions.checkArgument(accuracyGoalPerEdge >= 0.);
      Preconditions.checkNotNull(stoppingCondition);
      this.maxIterationsPerEdge = maxIterationsPerEdge;
      this.accuracyGoalPerEdge = accuracyGoalPerEdge;
      this.stoppingCondition = stoppingCondition;
   }
   
   /**
     * Apply this algorithm to a heterogeneous clearing network. If a 
     * set of suitable seed edge rates can be identified, the rates
     * of all edges present in the network will be modified to match 
     * these seed values before the algorithm attempts to clear the
     * network. If the algorithm cannot identify acceptable seeds for 
     * all network optimization variables, an IllegalStateException
     * is raised.
     */
   @Override
   public final double applyToNetwork(MixedClearingNetwork network) {
      applyPreMarchingStep(network);
      List<MixedClearingNetworkEdge> networkEdges = network.getEdges();
      { // Try to locate seed edge rates for this network.
         List<Double> seedEdgeRates = attemptToFindSeedRates(network);
         for(int i = 0; i< networkEdges.size(); ++i) {
            final double seedEdgeRate = seedEdgeRates.get(i);
            networkEdges.get(i).setEdgeRate(seedEdgeRate);
         }
      }
      network.updateAllVertexResponses();
      double networkResidual = network.getResidualCost();
      { // Marching and iteration
         while(true) {
            for(MixedClearingNetworkEdge edge : networkEdges)
               advanceOverEdge(edge);
            for(MixedClearingNetworkEdge edge : networkEdges)
               edge.flagNodesResponsesForUpdate();
            for(MixedClearingNetworkEdge edge : networkEdges)
               edge.updateNodeResponses();
            applyPostMarchingStep(network);
            networkResidual = network.getResidualCost();
            System.out.printf("Network residual: %16.10g\n", networkResidual);
            if(stoppingCondition.isStoppingConditionSatisfied(network)) break;
         }
      }
      return networkResidual;
   }
   
   protected void applyPreMarchingStep(MixedClearingNetwork network) { }
   
   /**
     * Try to identify seed edge rates for the primary iteration of 
     * the algorithm. Candidate seeds will be evaluated within 1, 2 
     * ... 10 significant figures of min(MaxDemandRate, MaxSupplyRate), 
     * where MaxDemandRate (MaxSupplyRate) is the maximum rate in the 
     * domain of the demand (supply) response function of the nodes
     * attached to each edge.
     */
   protected abstract List<Double> attemptToFindSeedRates(MixedClearingNetwork network);
   
   /**
     * Apply a rootfinding algorithm to one network edge 
     * in isolation. In this implementation, the rootfinding
     * algorithm used is a bracketed bisection.
     */
   protected abstract void advanceOverEdge(final MixedClearingNetworkEdge edge);
   
   protected void applyPostMarchingStep(MixedClearingNetwork network) { }
   
   /**
     * Get the maximum number of iterations used by this algorithm
     * per network edge.
     */
   protected final int getMaximumEdgeIterations() {
      return maxIterationsPerEdge;
   }
   
   /**
     * Get the algorithm clearing accuracy goal per edge.
     */
   protected final double getAccuracyGoalPerEdge() {
      return accuracyGoalPerEdge;
   }
}
