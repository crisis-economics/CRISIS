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

import java.util.BitSet;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import com.google.common.base.Preconditions;


/**
  * Skeletal base class for multidimensional extremization network clearing algorithms.
  * 
  * @author phillips
  */
abstract class NumericalDerivativeClearingAlgorithm implements MixedClearingNetworkAlgorithm {
   /**
     * Update the response of all network nodes to new edge states.
     */
   protected final void updateNetworkReponses(
      final MixedClearingNetwork network,
      final double[] edgeStates
      ) {
      for(int i = 0; i< edgeStates.length; ++i) {
         final MixedClearingNetworkEdge edge = network.getEdges().get(i);
         edge.setEdgeRate(edgeStates[i]);
      }
      network.updateAllVertexResponses();
   }
   
   /**
     * Get the target costs for all edge optimizations (zero).
     */
   protected double[] calculateTarget(final MixedClearingNetwork network) {
      return new double[network.getNumberOfEdges()];
   }
   
   protected final VectorCostFunction getVectorCostFunction(
      final MixedClearingNetwork mixedClearingNetwork) {
      return new VectorCostFunction(mixedClearingNetwork);
   }
   
   protected final class VectorCostFunction implements MultivariateVectorFunction {
      private final MixedClearingNetwork network;
      
      private VectorCostFunction(final MixedClearingNetwork mixedClearingNetwork) {
         Preconditions.checkNotNull(mixedClearingNetwork);
         this.network = mixedClearingNetwork;
      }
      
      public double[] value(double[] edgeStates) {
         double[] values = new double[network.getNumberOfEdges()];
         updateNetworkReponses(network, edgeStates);
         for (int i = 0; i < values.length; ++i)
            values[i] = network.getEdges().get(i).getCost();
         return values;
      }
   }
   
   protected final ResidualCostFunction getResidualScalarCostFunction(
      final MixedClearingNetwork network) {
      return new ResidualCostFunction(network);
   }
   
   protected final class ResidualCostFunction implements MultivariateFunction {
      private final MixedClearingNetwork network;
      
      private ResidualCostFunction(final MixedClearingNetwork mixedClearingNetwork) {
         Preconditions.checkNotNull(mixedClearingNetwork);
         this.network = mixedClearingNetwork;
      }
         
      @Override
      public double value(double[] edgeStates) {
         updateNetworkReponses(network, edgeStates);
         return network.getResidualCost();
      }
   }
   
   protected final MultivariateMatrixFunction getJacobianMatrixFunction(
      final MixedClearingNetwork network) {
      return new JacobianMatrixFunction(network);
   }
   
   protected final class JacobianMatrixFunction implements MultivariateMatrixFunction {
      private final MixedClearingNetwork network;
      private BitSet[] edgeIntersections;
      
      private JacobianMatrixFunction(final MixedClearingNetwork mixedClearingNetwork) {
         Preconditions.checkNotNull(mixedClearingNetwork);
         this.network = mixedClearingNetwork;
         cacheEdgeIntersections();
      }
      
      @Override
      public double[][] value(final double[] point) {
         for(int i = 0; i< point.length; ++i)
            network.getEdges().get(i).setEdgeRate(point[i]);
         return jacobian(network);
      }
      
      private void cacheEdgeIntersections() {
         edgeIntersections = new BitSet[network.getNumberOfEdges()];
         for(int i = 0; i< network.getNumberOfEdges(); ++i) {
            final MixedClearingNetworkEdge edge = network.getEdges().get(i);
            edgeIntersections[i] = new BitSet();
            for(int j = 0; j< network.getNumberOfEdges(); ++j) {
               final MixedClearingNetworkEdge otherEdge = network.getEdges().get(j);
               edgeIntersections[i].set(j, otherEdge.touchesEdge(edge));
            }
         }
      }
      
      /**
       * Compute the Jacobian (derivative of edge costs with respect to edge rates)
       * matrix for a mixed clearing network.
       */
      private final double[][] jacobian(final MixedClearingNetwork network) {
         final int numberOfObjectives = network.getNumberOfEdges();
         double[][] result = new double[numberOfObjectives][numberOfObjectives];
         final double normalizedPert = Math.sqrt(5.*Math.ulp(1.0));
         for(int i = 0; i< numberOfObjectives; ++i) {
            for(int j = 0; j< numberOfObjectives; ++j) {
               if(!edgeIntersections[j].get(i))
                  continue;
               final MixedClearingNetworkEdge
                  edgeI = network.getEdges().get(i),
                  edgeJ = network.getEdges().get(j);
               final double
                  currentRate = edgeJ.getEdgeRate(),
                  hR = normalizedPert * currentRate,
                  upperRate = currentRate + hR,
                  lowerRate = currentRate - hR;
               edgeJ.setEdgeRate(upperRate);
               edgeJ.flagNodesResponsesForUpdate();
               edgeJ.updateNodeResponses();
               edgeI.flagNodesResponsesForUpdate();
               edgeI.updateNodeResponses();
               final double
                  edgeCostUpper = edgeI.getCost();
               edgeJ.setEdgeRate(lowerRate);
               edgeJ.flagNodesResponsesForUpdate();
               edgeJ.updateNodeResponses();
               edgeI.flagNodesResponsesForUpdate();
               edgeI.updateNodeResponses();
               final double
                  edgeCostLower = edgeI.getCost(),
                  derivative = (edgeCostUpper - edgeCostLower) / (2. * hR);
               result[j][i] = derivative;
            }
         }
         return result;
      }
   }
}
