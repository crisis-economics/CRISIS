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
package eu.crisis_economics.abm.model.parameters;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.RealDistribution;

import com.google.inject.Inject;

import eu.crisis_economics.utilities.StateVerifier;

/**
  * A simple implementation of the {@link Parameter}{@code <Double>} interface.
  * This implementation accepts a {@link RealDistribution}, {@code p}, as input
  * and uses {@code p} to resolve queries to {@link #get}.<br><br>
  * 
  * For this implementation, the {@link #get} method is equivalent to {@code p.sample()}.
  * 
  * @author phillips
  */
public final class FromRealDistributionModelParameter extends AbstractParameter<Double> {
   
   private RealDistribution
      distribution;
   
   /**
     * Create a custom {@link FromRealDistributionModelParameter} object.
     * See {@link FromRealDistributionModelParameter}.
     * 
     * @param distribution
     *        The underlying {@link UnivariateFunction} to be used to resolve
     *        calls to {@link FromRealDistributionModelParameter#get}.
     * @param parameterName
     *        The {@link String} name of this model parameter.
     */
   @Inject
   public FromRealDistributionModelParameter(
      final RealDistribution distribution,
      final String parameterName
      ) {
      super(parameterName);
      StateVerifier.checkNotNull(distribution);
      this.distribution = distribution;
   }
   
   @Override
   public Double get() {
      return distribution.sample();
   }
   
   /**
     * Get the underlying {@link RealDistribution} for this object.
     */
   public RealDistribution getDistribution() {
      return distribution;
   }
   
   @Override
   public Class<?> getParameterType() {
      return Double.class;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "From Distribution Model Parameter, distribution:" + distribution
            + ", parameter name:" + super.getName() + ".";
   }
}
