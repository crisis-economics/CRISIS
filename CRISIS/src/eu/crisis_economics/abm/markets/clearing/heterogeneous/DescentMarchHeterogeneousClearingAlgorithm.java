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
final class DescentMarchHeterogeneousClearingAlgorithm
   extends MarchingHeterogeneousClearingAlgorithm {
   
   DescentMarchHeterogeneousClearingAlgorithm( // Immutable
      final int maxIterationsPerEdge,
      final double accuracyGoalPerEdge,
      final MixedClearingNetworkAlgorithmStoppingCondition stoppingCondition
      ) {
      super(maxIterationsPerEdge, accuracyGoalPerEdge, stoppingCondition);
   }
   
   @Override
   protected List<Double> attemptToFindSeedRates(final MixedClearingNetwork network) {
      final SeedObjectivesSelectionAlgorithm seedObjectivesSelectionAlgorithm =
         new DescentMarchSeedObjectivesSelectionAlgorithm();
      return seedObjectivesSelectionAlgorithm.findSeedRates(network);
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
      ValueBisectorAlgorithm.BisectionResult resultBracket = 
         bisector.bisectInRange(meritFunction, 0., existingEdgeRate, 0.);
      double result = resultBracket.getBracketUpperBound();
      edge.setEdgeRate(result);
   }
}
