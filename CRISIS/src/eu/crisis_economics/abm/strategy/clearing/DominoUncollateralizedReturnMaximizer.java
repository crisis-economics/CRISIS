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
package eu.crisis_economics.abm.strategy.clearing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kcl.waterloo.math.ArrayUtils;

import eu.crisis_economics.utilities.NumberUtil;
import eu.crisis_economics.utilities.Pair;

/**
  * An implementation of a polynomial time solution to the portfolio
  * profit maximization problem. This algorithm uses a custom technique
  * in a transformed state space to determine optimal investments
  * and optimal debts subject to capital constraints and pre-existing
  * cash reserves. This implementation is non-approximate and is expected
  * to return the correct, optimal result for all well-formed problems.
  * Contract KP to discuss the details of this technique.
  * 
  * @author phillips
  */
public final class DominoUncollateralizedReturnMaximizer
   extends AbstractUncollateralizedPortfolioReturnMaximizer {
   
   /**
     * Enable for verbose algorithm progress/solution information printed to stdout.
     */
   private static final boolean VERBOSE_MODE = false;
   
   final private static class Datum implements Comparable<Datum> {
      int index;
      double investment, weight;
      Pair<Double, Double> interest;
      double returnRateFor(final double investmentIn) {
         return interest.getSecond() + interest.getFirst() * investmentIn;
      }
      double returnRate() {
         return returnRateFor(investment);
      }
      double totalReturn() {
         return returnRate() * investment;
      }
      /**
        * Get the additional investment in this instrument which would
        * match the return rate on the argument, assuming that the
        * state of the argument is held constant.
        */
      double investmentMakesEqualHeldConst(final Datum other) {
         return (other.returnRate() - interest.getSecond()) / interest.getFirst() - investment;
      }
      /**
        * Get the additional investment in this instrument which would
        * match the return rate on the argument, not assuming that the
        * state of the argument is held constant.
        */
      double investmentMakesEqual(final Datum other) {
         return (other.returnRate() - returnRate()) /
                (interest.getFirst() - other.interest.getFirst() * other.weight / weight);
      }
      /**
        * Get the additional investment in this instrument which would
        * result in a zero effective return rate.
        */
      double investmentMakesNeutral() {
         return -returnRate() / interest.getFirst();
      }
      @Override
      public int compareTo(final Datum other) {
         return Double.compare(other.interest.getSecond(), interest.getSecond());
      }
   }
   
   @Override
   public List<Double>
      performOptimization(
         final List<Pair<Double, Double>> assetRates,         // der. const.
         final List<Pair<Double, Double>> liabilityRates,     // der. const.
         double cashToSpend,
         double capitalConstraint
         ) {
      capitalConstraint *= 2.0;
      cashToSpend *= 2.0;           // TODO:
      final int
         numAssetOptions = assetRates.size(),
         numLiabilityOptions = liabilityRates.size(),
         numInstruments = numAssetOptions + numLiabilityOptions;
      int
         numIterations = 0;
      final List<Datum>
         assets = new ArrayList<Datum>(),
         liabilities = new ArrayList<Datum>();
      for(int i = 0; i< numAssetOptions; ++i) {
         Datum datum = new Datum();
         datum.index = i;
         datum.interest = assetRates.get(i);
         assets.add(datum);
      }
      for(int j = 0; j< numLiabilityOptions; ++j) {
         Datum datum = new Datum();
         datum.index = j;
         datum.interest = liabilityRates.get(j);
         liabilities.add(datum);
      }
      Collections.sort(assets);
      Collections.sort(liabilities, Collections.reverseOrder());
      assets.get(0).weight = 1.;
      liabilities.get(0).weight = 1.;
      double
         totalInvestment = 0.;
      int
         assetBucket = 0,
         liabilityBucket = 0;
      boolean
         doContinue = true,
         nextLiability = false,
         nextAsset = false;
      double
         cashTolerance = cashToSpend == 0 ? 0. : Math.ulp(cashToSpend) * 10.;
      while(true) {
         if(VERBOSE_MODE) {
         // Readoff
         System.out.printf("iteration begins:\n");
            for(final Datum asset : assets) {
               System.out.printf(
                  "asset:     rate: %16.10g investment %16.10g weight: %16.10g\n",
                  asset.returnRate(), asset.investment, asset.weight);
            }
            for(final Datum liability : liabilities) {
               System.out.printf(
                  "liability: rate: %16.10g investment %16.10g weight: %16.10g\n",
                  liability.returnRate(), liability.investment, liability.weight);
            }
         }
         
         nextLiability = false;
         nextAsset = false;
         final Datum
            currentAsset = assets.get(assetBucket),
            currentLiability = liabilities.get(liabilityBucket);
         double
            criticalAssetInvestment = 0.,
            criticalLiabilityInvestment = 0.,
            cashAvailable = Math.max(cashToSpend - totalInvestment, 0.);
         if(cashAvailable < cashTolerance)
            cashAvailable = 0.;
         /*
          * Check for critical liabilities
          */
         if(cashAvailable == 0.) {
            if(liabilityBucket == numLiabilityOptions - 1)
               criticalLiabilityInvestment = Double.MAX_VALUE;
            else
               criticalLiabilityInvestment =
                  currentLiability.investmentMakesEqualHeldConst(
                     liabilities.get(liabilityBucket + 1)) / currentLiability.weight 
                        + totalInvestment;
         }
         else criticalLiabilityInvestment = Double.MAX_VALUE;
         /*
          * Check for critical assets
          */
         if(assetBucket == numAssetOptions - 1)
            criticalAssetInvestment =
               currentAsset.investmentMakesNeutral() / currentAsset.weight + totalInvestment;
         else
            criticalAssetInvestment =
               currentAsset.investmentMakesEqualHeldConst(assets.get(assetBucket + 1)) /
                  currentAsset.weight + totalInvestment;
         /*
          * Check for margins
          */
         final double
            stopPoint = Math.min(
               cashAvailable > 0 ? Double.MAX_VALUE :
               Math.max(
                  currentAsset.investmentMakesEqual(currentLiability) / currentAsset.weight, 0.)
                  + totalInvestment,
               capitalConstraint
               );
         if(stopPoint < criticalAssetInvestment &&
            stopPoint < criticalLiabilityInvestment && 
            cashAvailable == 0.)
            doContinue = false;
         else if(criticalAssetInvestment < criticalLiabilityInvestment)
            if(cashAvailable > 0.)
               nextAsset = (criticalAssetInvestment - totalInvestment < cashAvailable);
            else
               nextAsset = true;
         else
            nextLiability = true;
        double
            sumToInvest = 
               NumberUtil.min(criticalAssetInvestment, criticalLiabilityInvestment, stopPoint)
                  - totalInvestment;
        if(cashAvailable > 0.)
           sumToInvest = Math.min(cashAvailable, sumToInvest);
         
         /*
          * Increment assets
          */
         for(int i = 0; i<= assetBucket; ++i) {
            final double
               increment = sumToInvest * assets.get(i).weight;
            assets.get(i).investment += increment;
            totalInvestment += increment;
         }
         /*
          * Increment liabilities
          */
         if(cashAvailable == 0.)
            for(int j = 0; j<= liabilityBucket; ++j) {
               final double
                  increment = sumToInvest * liabilities.get(j).weight;
               liabilities.get(j).investment += increment;
            }
         
         if(VERBOSE_MODE)
            computeAndDisplayKKTFactors(
               assets, liabilities, totalInvestment, capitalConstraint);
         
         if(assets.get(assetBucket).returnRate() == 0.)
            doContinue = false;
         if(totalInvestment >= capitalConstraint)
            doContinue = false;
         
         if(!doContinue)
            break;
         /* 
          * Adjust weights
          */
         if(nextAsset) {
            assetBucket = Math.min(assetBucket + 1, numAssetOptions - 1);
            double sumGradients = 0.;
            for(int i = 0; i<= assetBucket; ++i) {
               double w = 1./assets.get(i).interest.getFirst();
               sumGradients += w;
               assets.get(i).weight = w;
            }
            sumGradients = 1./sumGradients;
            for(int i = 0; i<= assetBucket; ++i)
               assets.get(i).weight *= sumGradients;
         }
         else if(nextLiability) {
            liabilityBucket = Math.min(liabilityBucket + 1, numLiabilityOptions - 1);
            double sumGradients = 0.;
            for(int j = 0; j<= liabilityBucket; ++j) {
               double w = 1./liabilities.get(j).interest.getFirst();
               sumGradients += w;
               liabilities.get(j).weight = w;
            }
            sumGradients = 1./sumGradients;
            for(int j = 0; j<= liabilityBucket; ++j)
               liabilities.get(j).weight *= sumGradients;
         }
         
         ++numIterations;
         if(numIterations == numInstruments)
            break;
      }
      
      // Readoff
      if(VERBOSE_MODE) {
         double
            equityChange = 0.;
         System.out.printf("complete:\n");
         for(final Datum asset : assets) {
            System.out.printf(
               "asset:     rate: %16.10g investment %16.10g weight: %16.10g\n",
               asset.returnRate(), asset.investment, asset.weight);
            equityChange += asset.investment;
         }
         for(final Datum liability : liabilities) {
            System.out.printf(
               "liability: rate: %16.10g investment %16.10g weight: %16.10g\n",
               liability.returnRate(), liability.investment, liability.weight);
            equityChange -= liability.investment;
         }
         System.out.printf("equity change: %16.10g\n", equityChange);
      }
      
      double[] result = new double[numAssetOptions + numLiabilityOptions];
      double
         assetsDesired = 0.,
         liabilitiesDesired = 0.;
      for(final Datum asset : assets) {
         result[asset.index] = asset.investment / 2.;
         assetsDesired += asset.investment;
      }
      for(final Datum liability : liabilities) {
         result[liability.index + numAssetOptions] = liability.investment / 2.;
         liabilitiesDesired += liability.investment;
      }
      System.out.printf(
         "assets desired: %16.10g liabilities desired: %16.10g cc: %16.10g\n",
         assetsDesired,
         liabilitiesDesired,
         capitalConstraint
         );
      return ArrayUtils.toArrayList(result);
   }
   
   private static void computeAndDisplayKKTFactors(
      final List<Datum> assets,
      final List<Datum> liabilities,
      final double totalInvestment,
      final double maximumCapital
      ) {
      System.out.printf("KKT conditions:\n");
      final int
         numAssets = assets.size(),
         numLiabilities = liabilities.size(),
         numInstruments = numAssets + numLiabilities;
      double[]
         lambdaA = new double[numAssets],
         lambdaL = new double[numLiabilities];
      double
         lambdaCC = 0.,
         vEq = 0.;
      for(int j = 0; j< numLiabilities; ++j) {
         final double
            returnRate = liabilities.get(j).returnRate(),
            investment = liabilities.get(j).investment;
         if(investment > 0.) {
            vEq = returnRate;
            lambdaL[j] = 0.;
         } else
            lambdaL[j] = returnRate - vEq;
      }
      for(int i = 0; i< numAssets; ++i) {
         final double
            returnRate = assets.get(i).returnRate(),
            investment = assets.get(i).investment;
         if(investment > 0.) {
            lambdaCC = returnRate - vEq;
            lambdaA[i] = 0.;
         }
         else
            lambdaA[i] = -returnRate + lambdaCC + vEq;
      }
      double[]
         stationaryConstraints = new double[numInstruments];
      double
         marginConstraint = 0.,
         capitalConstraint = 0.,
         equityConstraint = 0.;
      boolean[]
         primarySignConstraints = new boolean[numInstruments],
         dualSignConstraints = new boolean[numInstruments + 1];
      boolean
         captialCSConstraint = false;
      boolean[]
         primaryCSConstaints = new boolean[numInstruments];
      for(int i = 0; i< numAssets; ++i) {
         final Datum asset = assets.get(i);
         final double
            evaluation = -asset.returnRate() + lambdaCC + vEq - lambdaA[i];
         stationaryConstraints[i] = evaluation;
         marginConstraint += asset.totalReturn();
         capitalConstraint += asset.investment;
         equityConstraint += asset.investment;
         primarySignConstraints[i] = (asset.investment >= 0.);
         dualSignConstraints[i] = (lambdaA[i] >= 0.);
         primaryCSConstaints[i] = (lambdaA[i] * asset.investment == 0.);
      }
      for(int j = 0; j< numLiabilities; ++j) {
         final Datum liability = liabilities.get(j);
         final double
            evaluation = liability.returnRate() - vEq - lambdaL[j];
         stationaryConstraints[j + numAssets] = evaluation;
         marginConstraint -= liability.totalReturn();
         equityConstraint -= liability.investment;
         primarySignConstraints[j + numAssets] = (liability.investment >= 0.);
         dualSignConstraints[j + numAssets] = (lambdaL[j] >= 0.);
         primaryCSConstaints[j + numAssets] = (lambdaL[j] * liability.investment == 0.);
      }
      captialCSConstraint = lambdaCC * (capitalConstraint - maximumCapital) == 0.;
      /*
       * Readoff
       */
      for(int i = 0; i< numAssets; ++i) {
         System.out.printf(
            "asset     %2d: r:      %16.10g I:%16.10g %s: dF: %16.10g %s\n",
            i, assets.get(i).returnRate(), 
            assets.get(i).investment,
            assets.get(i).investment >= 0. ? "[valid]" : "[* invalid]",
            stationaryConstraints[i],
            Math.abs(stationaryConstraints[i]) <= 1.e-6 ? "[valid]" : "[* invalid]"
            );
      }
      for(int j = 0; j< numLiabilities; ++j) {
         System.out.printf(
            "liability %2d: r:      %16.10g I:%16.10g %s: dF: %16.10g %s\n",
            j, liabilities.get(j).returnRate(),
            liabilities.get(j).investment,
            liabilities.get(j).investment >= 0. ? "[valid]" : "[* invalid]",
            stationaryConstraints[j + numAssets],
            Math.abs(stationaryConstraints[j + numAssets]) <= 1.e-6 ? 
                "[valid]" : "[* invalid]"
            );
      }
      System.out.printf(
         "margin:               %16.10g %s\n" +
         "total assets:         %16.10g %s\n" +
         "equity change:        %16.10g %s\n" +
         "lambda (CC):          %16.10g %s\n" +
         "nu (equity):          %16.10g [exists]\n",
         marginConstraint,
         marginConstraint >= 0 ? "[valid]" : "[invalid] *",
         totalInvestment,
         totalInvestment >= 0 ? "[valid]" : "[invalid] *",
         equityConstraint,
         Math.abs(equityConstraint) <= 1.e-5 ? "[valid]" : "[invalid] *",
         lambdaCC,
         lambdaCC >= 0 ? "[valid]" : "[invalid] *",
         vEq
         );
      for(int i = 0; i< numAssets; ++i) {
         System.out.printf(
            "asset     %2d: lambda: %16.10g %s sign: %s cs. constraint: %s\n",
            i, lambdaA[i],
            lambdaA[i] >= 0. ? "[valid]" : "[* invalid]",
            dualSignConstraints[i] ? "[valid]" : "[* invalid]",
            primaryCSConstaints[i] ? "[valid]" : "[* invalid]"
            );
      }
      for(int j = 0; j< numLiabilities; ++j) {
         System.out.printf(
            "liability %2d: lambda: %16.10g %s sign: %s cs. constraint: %s\n",
            j, lambdaL[j],
            lambdaL[j] >= 0. ? "[valid]" : "[* invalid]",
            dualSignConstraints[j + numAssets] ? "[valid]" : "[* invalid]",
            primaryCSConstaints[numAssets + j] ? "[valid]" : "[* invalid]"
            );
      }
      System.out.printf(
         "capital constraint: %s\n", Boolean.toString(captialCSConstraint));
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Domino Uncollateralized Portfolio Return Maximizer.";
   }
}
