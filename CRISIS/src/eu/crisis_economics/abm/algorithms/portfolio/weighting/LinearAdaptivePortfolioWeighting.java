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
package eu.crisis_economics.abm.algorithms.portfolio.weighting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * An implementation of an adaptive portfolio weighting strategy. This
  * {@link PortfolioWeighting} strategy uses a linear adaptive process
  * in order to match the returns on distinct instruments. Concretely: if
  * the <i>average</i> expected return is {@code r'} over all distinct instruments weighted
  * by the portfolio, then the weight {@code w} applied to each instrument
  * is perturbed according to {@code w -> w + m * (r - r')} where {@code r}
  * is the expected return for the instrument, {@code w} is the weight
  * applied to the instrument, and {@code m} is a fixed constant positive
  * market adaptation rate parameter specified by the constructor.
  * 
  * @author daniel
  */
public final class LinearAdaptivePortfolioWeighting extends AdapativePortfolioWeighting {
    
   public final static double
      DEFAULT_LINEAR_ADAPTIVE_PORTFOLIO_INITIAL_RETURN_ESTIMATE = .1,
      DEFAULT_LINEAR_ADAPTIVE_PORTFOLIO_ADAPTATION_RATE = .02;
   
   /**
     * Create a {@link LinearAdaptivePortfolioWeighting} portfolio object
     * with default parameters.
     */
   public LinearAdaptivePortfolioWeighting() {
      this(
         DEFAULT_LINEAR_ADAPTIVE_PORTFOLIO_INITIAL_RETURN_ESTIMATE,
         DEFAULT_LINEAR_ADAPTIVE_PORTFOLIO_INITIAL_RETURN_ESTIMATE
         );
   }
   
   /**
     * Create a {@link LinearAdaptivePortfolioWeighting} portfolio object
     * with custom parameters.
     */
   @Inject
   public LinearAdaptivePortfolioWeighting(
   @Named("LINEAR_ADAPTIVE_PORTFOLIO_INITIAL_RETURN_ESTIMATE")
      final double initReturn,
   @Named("LINEAR_ADAPTIVE_PORTFOLIO_ADAPTATION_RATE")
      final double adaptRate
      ) {
      super(initReturn, adaptRate);
   }
   
   @Override
   public void computeWeights() {
      super.computeWeights();
      Map<String, Double>
         lastWeights = new HashMap<String, Double>(getWeights());
      double
         meanExpectedReturn = super.getSumOfExpectedReturns() / super.size();
      // Perturb
      for(final Entry<String, Double> record : super.getReturns().entrySet()) {
         double
            diff = record.getValue() - meanExpectedReturn,
            w = lastWeights.get(record.getKey()) + super.getAdaptationRate() * diff;
         w = Math.max(w, 0.);
         setWeight(record.getKey(), w);
      }
      normalizeWeights();
   }
}