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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/*****************************************************************
 * Implementation of the logit portfolio weighting strategy. 
 * In this {@link PortfolioWeighting} strategy, if
 * the <i>maximum</i> return over all distinct instruments weighted
 * by the portfolio is {@code r_max'}, then the (un-normalised) weight {@code w} applied to each 
 * instrument is {@code w -> exp(beta * (r - r_max))}, where {@code r}
 * is the expected return for the instrument and {@code beta} is the intensity of choice parameter. 
 * @author daniel
 *
 ****************************************************************/
public final class LogitPortfolioWeighting extends AbstractPortfolioWeighting {
   
   /**
     * Create a {@link LogitPortfolioWeighting} object with custom parameters.
     * 
     * @param beta
     *        The exponential weighting to be used for the logit 
     *        distribution. This argument should be non-negative.
     */
   @Inject
   public LogitPortfolioWeighting(
   @Named("LOGIT_PORTFOLIO_WEIGHTING_BETA")
      final double beta
      ) {
      Preconditions.checkArgument(beta >= 0.);
      this.beta = beta;
   }
   
   @Override
   public void addReturn(
      final String reference, 
      final double expectedReturn
      ) {
      super.addReturn(reference, expectedReturn);
      if (expectedReturn > maxReturn)
         maxReturn = expectedReturn;
   }
   
   @Override
   public void computeWeights() {
   }
   
   @Override
   public double getWeight(final String reference) {
      return (Math.exp(beta * (super.getExpectedReturn(reference) - maxReturn)));
   }
   
   @Override
   public Map<String, Double> getWeights() {
      Map<String, Double>
         result = new HashMap<String, Double>();
      for(final Entry<String, Double> record : super.getReturns().entrySet())
         result.put(record.getKey(), getWeight(record.getKey()));
      return result;
   }
   
   public double getBeta() {
      return (beta);
   }
   
   public void setBeta(double b) {
      beta = b;
   }
   
   public void clear() {
      super.clear();
      maxReturn = 0.;
   }
   
   double maxReturn = 0.0;
   double beta = 1.0;
}