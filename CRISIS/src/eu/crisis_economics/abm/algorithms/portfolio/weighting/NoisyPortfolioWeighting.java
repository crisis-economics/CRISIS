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

import eu.crisis_economics.abm.simulation.Simulation;

/**
  * An implementation of the {@link PortfolioWeighting} interface with 
  * random stock weights. This implementation 
  * @author phillips
  */
public class NoisyPortfolioWeighting extends AbstractPortfolioWeighting {
   
   private Map<String, Double>
      noisyWeights;
   
   private double
      rho,
      noiseScale;
   
   @Inject
   public NoisyPortfolioWeighting(
   @Named("NOISY_PORTFOLIO_WEIGHTING_RHO")
      final double rho,
   @Named("NOISY_PORTFOLIO_WEIGHTING_NOISE_SCALE")
      final double noiseScale
      ) {
      Preconditions.checkArgument(rho > 0.);
      Preconditions.checkArgument(noiseScale >= 0.);
      this.noisyWeights = new HashMap<String, Double>();
      this.rho = rho;
      this.noiseScale = noiseScale;
   }
   
   @Override
   public void computeWeights() {
      if(noisyWeights.size() != super.size())
         for(final Entry<String, Double> record : super.getReturns().entrySet())
            noisyWeights.put(record.getKey(), 1.0);
      double
         summand = 0.;
      for(Entry<String, Double> record : noisyWeights.entrySet()) {
         double
            logWeight = Math.log(noisyWeights.get(record.getKey()));
         logWeight = rho * logWeight + generateGaussianNoise();
         double result = Math.exp(logWeight);
         noisyWeights.put(record.getKey(), result);
         summand += result;
      }
      // Normalize
      for(Entry<String, Double> record : noisyWeights.entrySet())
         record.setValue(record.getValue() / summand);
   }
   
   @Override
   public double getWeight(final String reference) {
      return noisyWeights.get(reference);
   }
   
   @Override
   public Map<String, Double> getWeights() {
      return new HashMap<String, Double>(noisyWeights);
   }
   
   @Override
   public void clear() {
      super.clear();
   }
   
   /**
     * Evaluate a double from a Gaussian noise distribution.
     */
   private double generateGaussianNoise() {
      return Simulation.getSimState().random.nextGaussian() * noiseScale;
   }
   
   public double getRho() {
      return rho;
   }
   
   /**
     * Get the Gaussian noise scale for this portfolio weighting.
     * @return
     */
   public double getGaussianNoiseScale() {
      return noiseScale;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Noisy Portfolio Weighting, rho: " + rho + ", noise scale: "
            + noiseScale + ".";
   }
}