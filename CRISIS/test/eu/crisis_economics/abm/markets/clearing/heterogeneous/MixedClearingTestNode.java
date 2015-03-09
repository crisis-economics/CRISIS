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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import eu.crisis_economics.abm.strategy.clearing.UncollateralizedReturnMaximizerMRFAdapater;
import eu.crisis_economics.utilities.Pair;

/**
 * @author phillips
 *
 */
final class MixedClearingTestNode {
   private MarketResponseFunction responseFunction;
   private String nodeID;
   
   private MixedClearingTestNode(final MarketResponseFunction responseFunction) {
      this.responseFunction = responseFunction;
      this.nodeID = UUID.randomUUID().toString();
   }
   
   // Create a polynomial increasing Inner Response Function.
   static private BoundedUnivariateFunction createPolynomialSupply(
      Random dice, double maxSupply) {
      final double
         polynomialPower = Math.max(dice.nextGaussian()/4. + 1., .1),
         normalizationFactor = dice.nextGaussian()/3. + 2;
      return new PolynomialSupplyInnerResponse(
         polynomialPower, normalizationFactor, maxSupply);
   }
   
   // Create a discontinuous increasing Inner Response Function.
   static private BoundedUnivariateFunction createDiscontinuousPolynomialSupply(
      Random dice, double maxSupply) {
      BoundedUnivariateFunction polynomialSupplyResponse = 
         createPolynomialSupply(dice, maxSupply);
      return new StepResponseFunction(polynomialSupplyResponse);
   }
   
   // Create a supply node.
   static MixedClearingTestNode createPolynomialSupplyNode(Random dice, double maxSupply) {
      BoundedUnivariateFunction polynomialSupplyResponse = 
         createPolynomialSupply(dice, maxSupply);
      MarketResponseFunction responseFunction = 
         new PartitionedResponseFunction(
            new ExpIOCPartitionFunction(), polynomialSupplyResponse);
      return new MixedClearingTestNode(responseFunction);
   }
   
   static MixedClearingTestNode createDiscontinuousSupplyNode(Random dice, double maxSupply) {
      BoundedUnivariateFunction discontinuousPolynomialSupplyResponse = 
         createDiscontinuousPolynomialSupply(dice, maxSupply);
      MarketResponseFunction responseFunction = 
         new PartitionedResponseFunction(
            new ExpIOCPartitionFunction(), discontinuousPolynomialSupplyResponse);
      return new MixedClearingTestNode(responseFunction);
   }
   
   static MixedClearingTestNode createTrivialSupplyNode() {
      BoundedUnivariateFunction trivialInnerResponse = 
         new AbstractBoundedUnivariateFunction() {
         @Override
         public double value(double coordinate) { return 0.; }
      };
      MarketResponseFunction responseFunction = 
         new PartitionedResponseFunction(
            new TrivialUnbiasedPartitionFunction(), trivialInnerResponse);
      return new MixedClearingTestNode(responseFunction);
   }
   
   // Create a polynomial decreasing Inner Response Function.
   static private BoundedUnivariateFunction createPolynomialDemand(
      Random dice, double maxDemand) {
      final double
         polynomialPower = Math.max(dice.nextGaussian()/4. + 1., .1),
         maximumRate = dice.nextGaussian()/3. + 2;
      return new PolynomialDemandInnerResponse(
         polynomialPower, maximumRate, maxDemand);
   }
   
   // Create a discontinuous increasing polynomial Inner Response Function.
   static private BoundedUnivariateFunction createDiscontinuousPolynomialDemand(
      Random dice, double maxSupply) {
      BoundedUnivariateFunction polynomialDemandResponse = 
         createPolynomialDemand(dice, maxSupply);
      return new StepResponseFunction(polynomialDemandResponse);
   }
   
   // Create a demand node.
   static MixedClearingTestNode createPolynomialDemandNode(Random dice, double maxDemand) {
      BoundedUnivariateFunction polynomialDemandResponse = 
         createPolynomialDemand(dice, maxDemand);
      MarketResponseFunction responseFunction = 
         new PartitionedResponseFunction(
            new InverseExpIOCPartitionFunction(), polynomialDemandResponse);
      return new MixedClearingTestNode(responseFunction);
   }
   
   static MixedClearingTestNode createDiscontinuousDemandNode(Random dice, double maxDemand) {
      BoundedUnivariateFunction
         stepFunction = createDiscontinuousPolynomialDemand(dice, maxDemand);
      MarketResponseFunction responseFunction = 
         new PartitionedResponseFunction(
            new InverseExpIOCPartitionFunction(), stepFunction);
      return new MixedClearingTestNode(responseFunction);
   }
   
   static MixedClearingTestNode creatPartlyNegativeDemandNode(
      final Random dice,
      final double maxValue,
      final double minValue
      ) {
      final double normalization = dice.nextDouble() * 5. + 1.;
      BoundedUnivariateFunction partlyNegativeInnerResponse = 
         new BoundedUnivariateFunction() {
         @Override
         public double value(double coordinate) { 
            return (maxValue - minValue) *
               Math.exp(-coordinate * coordinate / normalization) + minValue;
         }
         @Override
         public double getMinimumInDomain() { return 0.; }
         @Override
         public double getMaximumInDomain() { return 10.; }
      };
      MarketResponseFunction responseFunction = 
         new PartitionedResponseFunction(
            new TrivialUnbiasedPartitionFunction(), partlyNegativeInnerResponse);
      return new MixedClearingTestNode(responseFunction);
   }
   
   // Create a relay node, with both supplier and consumer behaviour.
   static MixedClearingTestNode createRelayNode(
      final Random dice,
      final double equity,
      final double leverage,
      final double existingCashToSpend,
      final Set<String> assets,
      final Set<String> liabilities,
      final Map<String, Pair<Double, Double>> assetReturns,
      final Map<String, Pair<Double, Double>> liabilityReturns
      ) {
      Map<String, Double> 
         initialAssets = new HashMap<String, Double>(),
         initialLiabilities = new HashMap<String, Double>();
      for(final String record : assets)
         initialAssets.put(record, 0.);
      for(final String record : liabilities)
         initialLiabilities.put(record, 0.);
      MarketResponseFunction responseFunction =
         UncollateralizedReturnMaximizerMRFAdapater.create(
            equity,
            leverage,
            existingCashToSpend,
            assets,
            liabilities,
            assetReturns,
            liabilityReturns,
            initialAssets,
            initialLiabilities
            );
      return new MixedClearingTestNode(responseFunction);
   }
   
   // Get the inner response function for this node.
   MarketResponseFunction getResponseFunction() {
      return responseFunction;
   }
   
   // Get the unique ID of this node.
   String getUniqueID() {
      return nodeID;
   }
}
