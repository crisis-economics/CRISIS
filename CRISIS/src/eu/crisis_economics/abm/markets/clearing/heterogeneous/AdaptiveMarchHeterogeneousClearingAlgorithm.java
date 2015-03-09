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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateFunction;

import eu.crisis_economics.abm.algorithms.optimization.BrentLineSearch;
import eu.crisis_economics.abm.algorithms.optimization.ValueBisectorAlgorithm;
import eu.crisis_economics.utilities.ArrayUtil;

/**
  * A marching heterogeneous clearing algorithm with an adaptive step direction
  * and a line-search polish step.
  *  
  * Initially, this algorithm probes all edge response functions to see whether
  * it has an increasing or a decreasing cost function. The direction of the 
  * marching iteration is specified accordingly. When the algorithm has adjusted
  * edge rates on all edges in the system, a polish step is applied by 
  * minimizing the network cost residual in the compound vector direction of
  * the previous steps.
  * 
  * @author phillips
  */
final class AdaptiveMarchHeterogeneousClearingAlgorithm
   extends MarchingHeterogeneousClearingAlgorithm {
   
   private Map<MixedClearingNetworkEdge, Boolean> edgeHasDescendingCost;
   private Map<MixedClearingNetworkEdge, Double> distanceTravelledAlongEdgeLastStep;
   
   AdaptiveMarchHeterogeneousClearingAlgorithm(
      final int maxIterationsPerEdge,
      final double accuracyGoalPerEdge,
      final MixedClearingNetworkAlgorithmStoppingCondition stoppingCondition
      ) {
      super(maxIterationsPerEdge, accuracyGoalPerEdge, stoppingCondition);
      this.edgeHasDescendingCost = new HashMap<MixedClearingNetworkEdge, Boolean>();
      this.distanceTravelledAlongEdgeLastStep = new HashMap<MixedClearingNetworkEdge, Double>();
   }
   
   /**
    * Decide whether an edge has an increasing or a decreasing cost
    * as a function of the rate.
    */
   @Override
   protected void applyPreMarchingStep(final MixedClearingNetwork network) {
      for(final MixedClearingNetworkEdge edge : network.getEdges()) {
         final double testRate = edge.getMaximumRateAdmissibleByBothParties();
         final MeritFunction costFunction = new MeritFunction(edge);
         final double
            costAtMinimumRate = costFunction.functionValue(0.),
            costAtMaximumRate = costFunction.functionValue(testRate);
         edgeHasDescendingCost.put(edge, costAtMaximumRate <= costAtMinimumRate ? true : false);
      }
   }
   
   @Override
   protected List<Double> attemptToFindSeedRates(final MixedClearingNetwork network) {
      final AscentMarchSeedObjectivesSelectionAlgorithm seedSelectionAlgorithm =
         new AscentMarchSeedObjectivesSelectionAlgorithm();
      return seedSelectionAlgorithm.findSeedRates(network);
   }
   
   @Override
   protected void advanceOverEdge(
      final MixedClearingNetworkEdge edge) {
      final double existingEdgeRate = edge.getEdgeRate();
      MeritFunction meritFunction = new MeritFunction(edge);
      {
         final double
            currentEvaluation = meritFunction.functionValue(existingEdgeRate);
         if(currentEvaluation == 0 ||
            Math.abs(currentEvaluation) < super.getAccuracyGoalPerEdge()) {
               distanceTravelledAlongEdgeLastStep.put(edge, 0.);
               return;
            }
      }
      ValueBisectorAlgorithm<MeritFunction> bisector = 
         new ValueBisectorAlgorithm<MeritFunction>(super.getMaximumEdgeIterations());
      final double
         costAtCurrentRate = meritFunction.functionValue(existingEdgeRate);
      final boolean
         edgeCostIsDecreasing = edgeHasDescendingCost.get(edge),
         marchTowardMaximumRate =
            (costAtCurrentRate < 0. && !edgeCostIsDecreasing) ||
            (costAtCurrentRate > 0. && edgeCostIsDecreasing);
      double result = 0.;
      
      // Edge cases
      final double
         demandMaximumRate = edge.getMaximumRateInDemandDomain(),
         supplyMaximumRate = edge.getMaximumRateInSupplyDomain(),
         maximumAdmissibleRate = Math.min(demandMaximumRate, supplyMaximumRate),
         evaluationAtMinimum = meritFunction.functionValue(0.),
         evaluationAtMaximum = meritFunction.functionValue(maximumAdmissibleRate);
      if(edgeCostIsDecreasing && evaluationAtMaximum >= 0.)
         result = maximumAdmissibleRate;
      else if(!edgeCostIsDecreasing && evaluationAtMaximum <= 0.)
         result = maximumAdmissibleRate;
      else if(edgeCostIsDecreasing && evaluationAtMinimum <= 0.)
         result = 0.;
      else if(!edgeCostIsDecreasing && evaluationAtMinimum >= 0.)
         result = 0.;
      else if(marchTowardMaximumRate) {
         ValueBisectorAlgorithm.BisectionResult resultBracket = 
            bisector.bisectInRange(
               meritFunction, existingEdgeRate, maximumAdmissibleRate, 0.);
         result = resultBracket.getBracketLowerBound();
      }
      else {
         ValueBisectorAlgorithm.BisectionResult resultBracket = 
            bisector.bisectInRange(meritFunction, 0., existingEdgeRate, 0.);
         result = resultBracket.getBracketUpperBound();
      }
      distanceTravelledAlongEdgeLastStep.put(edge, result - existingEdgeRate);
      edge.setEdgeRate(result);
   }
   
   /**
     * Perform a line search extremization in the vector
     * direction of the last marching steps.
     */
   protected void applyPostMarchingStep(final MixedClearingNetwork network) {
      final List<MixedClearingNetworkEdge> edges = network.getEdges();
      final List<Double> startingRates = new ArrayList<Double>();
      final double[]
         lineSearchDirection = new double[edges.size()],
         domainMinima = new double[edges.size()],
         domainMaxima = new double[edges.size()];
      for(int i = 0; i< edges.size(); ++i) {
         final MixedClearingNetworkEdge edge = edges.get(i);
         startingRates.add(edge.getEdgeRate());
         final double distanceTravelledAlongThisEdge = 
            distanceTravelledAlongEdgeLastStep.get(edge);
         lineSearchDirection[i] = distanceTravelledAlongThisEdge;
         domainMaxima[i] = edge.getMaximumRateAdmissibleByBothParties();
      }
      final MultivariateFunction function = new MultivariateFunction() {
         @Override
         public double value(double[] x) {
            for(int i = 0; i< edges.size(); ++i) {
               edges.get(i).setEdgeRate(x[i]);
               edges.get(i).flagNodesResponsesForUpdate();
            }
            for(int i = 0; i< edges.size(); ++i)
               edges.get(i).updateNodeResponses();
            return network.getResidualCost();
         }
      };
      BrentLineSearch.doLineSearch(
         function,
         ArrayUtil.toPrimitive(startingRates),
         lineSearchDirection, 
         domainMaxima,
         domainMinima
         );
   }
}
