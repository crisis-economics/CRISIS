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
package eu.crisis_economics.abm.strategy.clearing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingInstrument;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.utilities.Pair;
import eu.crisis_economics.utilities.StateVerifier;

public class UncollateralizedReturnMaximizerMRFAdapater implements MarketResponseFunction {
   
   private double
      equity,
      lambda,
      existingCashToSpend;
   private Set<String>
      assets,
      liabilities;
   private Map<String, Pair<Double, Double>>
      assetRisks,
      liabilityRisks;
   private Map<String, Double>
      initialAssetHoldings,
      initialLiabilityHoldings;
   
   private UncollateralizedReturnMaximizerMRFAdapater() { } // Immutable
   
   /**
     * Create a clearing market response function using
     * UncollateralizedReturnMaximizer.computeOptimalInvestments().<br><br>
     * 
     * Arguments are as follows:<br>
     *  (1) the equity of the market participant,<br>
     *  (2) the target leverage of the participant,<br>
     *  (3) the participant's existing assets of each type,<br>
     *  (4) the participant's existing liability holdings of each type.
     *  <br><br>
     *  
     * Internally, the returned object groups market trade opportunities
     * according to the resources indicated by (3) and (4). If the market
     * trades a resource not indicated by arguments (3) and (4), the
     * behaviour of this method is undefined.<br><br>
     * 
     * The format of the argument assertRisks [liabilityRisks] is 
     * as follows:<br>
     * Map<<br>
     *   String: name of asset [liability],<br>
     *   Pair<<br>
     *      Double: Risk premium for this asset [liability] (the return
     *              rate divisor),<br>
     *      Double: Market impact for this asset [liability]; derivative of
     *              return rate wrt. investment<br>
     *      >><br><br>
     * 
     * This {@link MarketResponseFunction} adapter treats cash in a special
     * way. If the asset list contains an entry with key "Cash", then this
     * asset is added to the underlying LCQP maximization operation even if
     * cash resources are not specified in calls to {@link #getValue}.
     */
   static public MarketResponseFunction create(                 // Possible collateral extension.
      final double equity,
      final double lambda,
      final double existingCashToSpend,
      final Set<String> assets,
      final Set<String> liabilities,
      final Map<String, Pair<Double, Double>> assetRisks,
      final Map<String, Pair<Double, Double>> liabilityRisks,
      final Map<String, Double> initialAssetHoldings,
      final Map<String, Double> initialLiabilityHoldings
      ) {
      StateVerifier.checkNotNull(assets, liabilities, assetRisks, liabilityRisks);
      UncollateralizedReturnMaximizerMRFAdapater result =
         new UncollateralizedReturnMaximizerMRFAdapater();
      result.equity = equity;
      result.lambda = lambda;
      result.assets = assets;
      result.liabilities = liabilities;
      result.assetRisks = assetRisks;
      result.liabilityRisks = liabilityRisks;
      result.initialAssetHoldings = initialAssetHoldings;
      result.initialLiabilityHoldings = initialLiabilityHoldings;
      result.existingCashToSpend = existingCashToSpend;
      return result;
   }
   
   /**
     * This constructor behaves as {@link #create(double, double, Set, Set, Map, Map, Map, Map)},
     * with the assumption that initial asset and liability holdings are all zero.
     */
   static public MarketResponseFunction create(
      final double equity,
      final double lambda,
      final double existingCashToSpend,
      final Set<String> assets,
      final Set<String> liabilities,
      final Map<String, Pair<Double, Double>> assetRisks,
      final Map<String, Pair<Double, Double>> liabilityRisks
      ) {
      Map<String, Double>
         initialAssetHoldings = new HashMap<String, Double>(),
         initialLiabilityHoldings = new HashMap<String, Double>();
      for(final String record : assets)
         initialAssetHoldings.put(record, 0.);
      for(final String record : liabilities)
         initialLiabilityHoldings.put(record, 0.);
      return create(
         equity, lambda, existingCashToSpend,
         assets, liabilities, assetRisks,
         liabilityRisks, initialAssetHoldings,
         initialLiabilityHoldings);
   }
   
   @Override
   public double getMinimumInDomain() {
      return 0;
   }

   @Override
   public double getMaximumInDomain() {
      return 2.0;                                               // Maximum 200% return rate.
   }
   
   /**
     * Enable for verbose algorithm progress/solution information printed to stdout.
     */
   static final private boolean VERBOSE_MODE = false;
   
   final double MINIMAL_INTEREST_RATE = 1.e-6;
   
   @Override
   public double[] getValue(final int[] queries, final TradeOpportunity[] opportunities) {
      // Map<
      //   Resource Name, 
      //   Pair<Aggregated Index, Number Of Trade Opportunities>
      //   >
      final Map<ClearingInstrument, Pair<Integer, Integer>>
         mappingFromAggregatedAssets =
            new HashMap<ClearingInstrument, Pair<Integer, Integer>>(),
         mappingFromAggregatedLiabilities =
            new HashMap<ClearingInstrument, Pair<Integer, Integer>>();
      final List<Double>
         initialAggregatedAssetHoldings = new ArrayList<Double>(),
         initialAggregatedLiabilityHoldings = new ArrayList<Double>();
      int
         numberOfAssets = 0,
         numberOfLiabilities = 0;
      final List<Pair<Double, Double>>
         assetRatesWithRisk = new ArrayList<Pair<Double, Double>>(),
         liabilityRatesWithRisk = new ArrayList<Pair<Double, Double>>();
      
      // Check whether the participant has existing assets and liabilities
      for(int i = 0; i< opportunities.length; ++i) {
         final TradeOpportunity opportunity = opportunities[i];
         final String
            instrumentName = opportunity.getInstrument().getName();
         final boolean
            isAsset = assets.contains(instrumentName),
            isLiability = liabilities.contains(instrumentName);
         if(!isAsset && !isLiability)
            throw new IllegalArgumentException();
         if(isAsset && isLiability)
            throw new IllegalArgumentException();
         
         // Aggregate assets and liabilities by class
         final ClearingInstrument
            key = opportunity.getInstrument();
         if(isAsset) {                                    // Register an asset option
            if(!mappingFromAggregatedAssets.containsKey(key)) {
               mappingFromAggregatedAssets.put(key, Pair.create(numberOfAssets, 1));
               initialAggregatedAssetHoldings.add(
                  initialAssetHoldings.get(key.getName()));
               final double
                  riskPremium = assetRisks.get(key.getName()).getFirst(),
                  riskIncreaseRate = assetRisks.get(key.getName()).getSecond();
               assetRatesWithRisk.add(
                  Pair.create(
                     riskIncreaseRate,
                     Math.max(opportunity.getRate()/riskPremium, MINIMAL_INTEREST_RATE)
                     ));
               ++numberOfAssets;
            } else {
               Pair<Integer, Integer>
                  record = mappingFromAggregatedAssets.get(key),
                  revised = Pair.create(record.getFirst(), record.getSecond() + 1);
               mappingFromAggregatedAssets.put(key, revised);
            }
         } else {                                         // Register a liability option
            if(!mappingFromAggregatedLiabilities.containsKey(key)) {
               mappingFromAggregatedLiabilities.put(key, Pair.create(numberOfLiabilities, 1));
               initialAggregatedLiabilityHoldings.add(
                  initialLiabilityHoldings.get(key.getName()));
               final double
                  riskPremium = liabilityRisks.get(key.getName()).getFirst(),
                  riskIncreaseRate = liabilityRisks.get(key.getName()).getSecond();
               liabilityRatesWithRisk.add(
                  Pair.create(
                     riskIncreaseRate,
                     Math.max(opportunity.getRate()/riskPremium, MINIMAL_INTEREST_RATE)
                     ));
               ++numberOfLiabilities;
            } else {
               Pair<Integer, Integer>
                  record = mappingFromAggregatedLiabilities.get(key),
                  revised = Pair.create(record.getFirst(), record.getSecond() + 1);
               mappingFromAggregatedLiabilities.put(key, revised);
            }
         }
      }
      
      // Compute optimal investments
      UncollateralizedPortfolioReturnMaximizer optimizer = 
         new DominoUncollateralizedReturnMaximizer();
            
      final List<Double> optimum =
         optimizer.performOptimization(
            assetRatesWithRisk,
            liabilityRatesWithRisk,
            existingCashToSpend,
            equity * lambda
            );
      
      if(VERBOSE_MODE) {
         for(int i = 0; i< assetRatesWithRisk.size(); ++i) {
            System.out.printf(
               "LCQP: assert w/risk: const.: %16.10g dec.: %16.10g\n",
               assetRatesWithRisk.get(i).getSecond(),
               assetRatesWithRisk.get(i).getFirst()
               );
         }
         for(int j = 0; j< liabilityRatesWithRisk.size(); ++j) {
            System.out.printf(
               "LCQP: liability w/risk: const.: %16.10g dec.: %16.10g\n",
               liabilityRatesWithRisk.get(j).getSecond(),
               liabilityRatesWithRisk.get(j).getFirst()
               );
         }
         System.out.printf(
            "LCQP: equity: %16.10g lambda: %16.10g: max asset: %16.10g\n",
            equity, lambda, equity * lambda);
      }
      
      double totalThroughput = 0.;
      
      // Split homogeneous trades equally between all prospective partners
      final Map<ClearingInstrument, Double>
         demandForTrade = new HashMap<ClearingInstrument, Double>();
      for(final Entry<ClearingInstrument, Pair<Integer, Integer>> record : 
         mappingFromAggregatedAssets.entrySet()) {
         demandForTrade.put(
            record.getKey(),
            -optimum.get(record.getValue().getFirst()) / record.getValue().getSecond()
            );
         
         if(VERBOSE_MODE) {
            System.out.printf(
               "LCQP: resource class: %20s total trade demand: %16.10g\n",
               record.getKey(),
               -optimum.get(record.getValue().getFirst())
               );
         }
            
         totalThroughput -= optimum.get(record.getValue().getFirst());
      }
      for(final Entry<ClearingInstrument, Pair<Integer, Integer>> record : 
         mappingFromAggregatedLiabilities.entrySet()) {
         demandForTrade.put(
            record.getKey(),
            optimum.get(record.getValue().getFirst() + numberOfAssets)
               / record.getValue().getSecond()
               );
         
         if(VERBOSE_MODE) {
            System.out.printf(
               "LCQP: resource class: %20s total trade demand: %16.10g *\n",
               record.getKey(),
               optimum.get(record.getValue().getFirst() + numberOfAssets)
               );
         }
      }
      
      double[] result = new double[queries.length];
      for(int i = 0; i< queries.length; ++i) {
         final int index = queries[i];
         final ClearingInstrument
            key = opportunities[index].getInstrument();
         result[i] = demandForTrade.get(key);
      }
      
      if(Boolean.valueOf(VERBOSE_MODE)) {
         System.out.printf("LCQP: bank maximum demand: %16.10g\n", equity * lambda);
         System.out.printf("LCQP: throughput: %16.10g (%16.10g of maximum)\n\n",
            totalThroughput, 100.*totalThroughput/(equity * lambda));
      }
      
      return result;
   }
}
