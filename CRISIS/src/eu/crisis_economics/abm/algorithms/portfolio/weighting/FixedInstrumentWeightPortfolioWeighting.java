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
package eu.crisis_economics.abm.algorithms.portfolio.weighting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * A simple implementation of the {@link PortfolioWeighting} interface. This implementation
  * specifies a fixed weight for all instruments whose reference names contain a customizable
  * substring. For instance, all instruments whose reference names contain "Loan" may receive
  * a fixed portfolio weight.
  * 
  * @author phillips
  */
public final class FixedInstrumentWeightPortfolioWeighting
   extends AbstractPortfolioWeighting {
   
   final double
      fixedWeight;
   final String
      instrumentNameToSearchFor;
   
   /**
     * Create a {@link FixedInstrumentWeightPortfolioWeighting} object with custom
     * parameters.
     */
   @Inject
   public FixedInstrumentWeightPortfolioWeighting(
   @Named("FIXED_WEIGHT")
      final double fixedWeight,
   @Named("INSTRUMENT_NAME_TO_SEARCH_FOR")
      final String instrumentNameToSearchFor
      ) {
      this.fixedWeight = fixedWeight;
      this.instrumentNameToSearchFor = Preconditions.checkNotNull(instrumentNameToSearchFor);
      if(instrumentNameToSearchFor.isEmpty())
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": instrument name search pattern is empty.");
      if(fixedWeight < 0. || fixedWeight > 1.0)
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": fixed weight must be in the range [0, 1].");
   }
   
   private int
      numberOfInstrumentsMatchingQueryPattern;
   
   public void computeWeights() {
      numberOfInstrumentsMatchingQueryPattern = 0;
      for(final String record : super.getReturns().keySet())
         if(record.contains(instrumentNameToSearchFor))
            ++numberOfInstrumentsMatchingQueryPattern;
   }
   
   @Override
   public double getWeight(final String reference) {
      if(!super.getReturns().keySet().contains(reference))
         return 0.;
      else if(reference.contains(instrumentNameToSearchFor))
         return fixedWeight / numberOfInstrumentsMatchingQueryPattern;
      else if(numberOfInstrumentsMatchingQueryPattern != super.size())
         return (1. - fixedWeight) / (super.size() - numberOfInstrumentsMatchingQueryPattern);
      else return 0.;
   }
   
   @Override
   public Map<String, Double> getWeights() {
      Map<String, Double>
         result = new HashMap<String, Double>();
      for(final Entry<String, Double> record : super.getReturns().entrySet())
         result.put(record.getKey(), getWeight(record.getKey()));
      return result;
   }
}