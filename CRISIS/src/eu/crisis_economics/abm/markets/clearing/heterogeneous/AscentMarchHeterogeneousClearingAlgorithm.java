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

import eu.crisis_economics.abm.algorithms.optimization.ValueBisectorAlgorithm;

/**
  * @author phillips
  */
final class AscentMarchHeterogeneousClearingAlgorithm
   extends MarchingHeterogeneousClearingAlgorithm {
   
   AscentMarchHeterogeneousClearingAlgorithm( // Immutable
      final int maxIterationsPerEdge,
      final double accuracyGoalPerEdge,
      final MixedClearingNetworkAlgorithmStoppingCondition stoppingCondition
      ) {
      super(maxIterationsPerEdge, accuracyGoalPerEdge, stoppingCondition);
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
            Math.abs(currentEvaluation) < super.getAccuracyGoalPerEdge())
            return;
      }
      ValueBisectorAlgorithm<MeritFunction> bisector = 
         new ValueBisectorAlgorithm<MeritFunction>(super.getMaximumEdgeIterations());
      // Edge cases
      double
         demandMaximumRate = edge.getMaximumRateInDemandDomain(),
         supplyMaximumRate = edge.getMaximumRateInSupplyDomain(),
         maximumAdmissibleRate = Math.min(demandMaximumRate, supplyMaximumRate),
         evaluationAtMinimum = meritFunction.functionValue(0.),
         evaluationAtMaximum = meritFunction.functionValue(maximumAdmissibleRate);
      final boolean
         isIncreasing = (evaluationAtMaximum >= evaluationAtMinimum);
      double result;
      if(isIncreasing && evaluationAtMaximum <= 0.)
         result = maximumAdmissibleRate;
      else if(!isIncreasing && evaluationAtMaximum >= 0.)
         result = maximumAdmissibleRate;
      else if(isIncreasing && evaluationAtMinimum >= 0.)
         result = 0.;
      else if(!isIncreasing && evaluationAtMinimum <= 0.)
         result = 0.;
      else {
         /*
          * If neither party knows what the maximum resource rate is,
          * attempt to find a suitable rate by increasing the existing
          * edge rate in multiples of 10. If this process does not 
          * terminate in ten iterations (Ie. 10 orders of magnitude in
          * the existing edge rate), abandon the attempt, restore
          * the system to the existing edge rate, and return.
          */
         if(maximumAdmissibleRate == Double.MAX_VALUE) {
            maximumAdmissibleRate = Math.max(existingEdgeRate * 10., 1.0);
            final double
               lowerEvaluation = meritFunction.functionValue(existingEdgeRate);
            int attempts = 0;
            while(meritFunction.functionValue(maximumAdmissibleRate) * lowerEvaluation > 0.) {
               maximumAdmissibleRate *= 10.;
               if(attempts++ > 20) {
                  meritFunction.functionValue(existingEdgeRate);
                  return;
               }
            }
         }
         
         ValueBisectorAlgorithm.BisectionResult resultBracket = 
            bisector.bisectInRange(
               meritFunction, existingEdgeRate, maximumAdmissibleRate, 0.);
         result = resultBracket.getBracketLowerBound();
      }
      edge.setEdgeRate(result);
   }
}
