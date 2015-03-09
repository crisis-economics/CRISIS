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
import java.util.List;

import kcl.waterloo.math.ArrayUtils;
import eu.crisis_economics.utilities.Pair;

/**
  * An abstract base implementation of the {@link UncollateralizedPortfolioReturnMaximizer} 
  * interface. This base class implements {@link #performOptimizationWithMinimumInvestments}
  * by expressing the caculation in terms of one call to {@link #performOptimization}.
  * 
  * @author phillips
  */
public abstract class AbstractUncollateralizedPortfolioReturnMaximizer implements
   UncollateralizedPortfolioReturnMaximizer {
   
   AbstractUncollateralizedPortfolioReturnMaximizer() { }
   
   /**
     * Implementation detail. Convert an investment (I). and return rate
     * function with market impact (r), to the return rate expected
     * for investment I (r(I)).
     */
   private static double rateForInvestment(
      final double investment,
      final Pair<Double, Double> rates
      ) {
      return rates.getSecond() + investment * rates.getFirst();
   }
   
   @Override
   public final List<Double> performOptimizationWithMinimumInvestments(
      final List<Pair<Double, Double>> assetRates,
      final List<Pair<Double, Double>> liabilityRates,
      final List<Double> minimalAssetHoldings,
      final List<Double> minimalLiabilityHoldings,
      final double cashToSpend,
      final double capitalConstraint
      ) {
      final int
         numAssetTypes = assetRates.size(),
         numLiabilityTypes = assetRates.size(),
         numInstruments = numAssetTypes + numLiabilityTypes;
      double[]
         mandatoryInvestments = new double[numInstruments];
      double
         effectiveCapitalConstraint = capitalConstraint;
      List<Pair<Double, Double>>
         effectiveAssetRates = new ArrayList<Pair<Double,Double>>(),
         effectiveLiabilityRates = new ArrayList<Pair<Double,Double>>();
      for(int i = 0; i< numAssetTypes; ++i) {
         mandatoryInvestments[i] = minimalAssetHoldings.get(i);
         effectiveAssetRates.add(
            Pair.create(
               assetRates.get(i).getFirst(),
               rateForInvestment(mandatoryInvestments[i], assetRates.get(i))
               ));
         effectiveCapitalConstraint -= mandatoryInvestments[i];
      }
      for(int j = 0; j< numLiabilityTypes; ++j) {
         mandatoryInvestments[j + numAssetTypes] = minimalLiabilityHoldings.get(j);
         effectiveLiabilityRates.add(
            Pair.create(
               liabilityRates.get(j).getFirst(),
               rateForInvestment(
                  mandatoryInvestments[j + numAssetTypes], liabilityRates.get(j))
               ));
      }
      if(effectiveCapitalConstraint <= 0.) {
         /*
          * Minimal holdings are incompatible with the maximum capital
          * constraint. Return the minimal asset/liability holdings and
          * take no further action.
          */
         return ArrayUtils.toArrayList(mandatoryInvestments);
      }
      final List<Double> result =
         performOptimization(
            effectiveAssetRates, effectiveLiabilityRates,
            cashToSpend, effectiveCapitalConstraint
            );
      for(int i = 0; i< numAssetTypes; ++i)
         result.set(i, result.get(i) + mandatoryInvestments[i]);
      for(int j = numAssetTypes; j< numInstruments; ++j)
         result.set(j, result.get(j) + mandatoryInvestments[j]);
      return result;
   }
   
   @Override
   public abstract List<Double> performOptimization(
      List<Pair<Double, Double>> assetRates,
      List<Pair<Double, Double>> liabilityRates,
      double cashToSpend,
      double capitalConstraint
      );
}
