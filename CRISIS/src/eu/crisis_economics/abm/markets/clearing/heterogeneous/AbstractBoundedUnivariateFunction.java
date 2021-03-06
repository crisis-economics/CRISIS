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

/**
  * @author phillips
  */
public abstract class AbstractBoundedUnivariateFunction
   implements BoundedUnivariateFunction {
   
   private BoundedDomainMixin domainBounds;
   
   protected AbstractBoundedUnivariateFunction() {
      this(new SimpleDomainBounds());
   }
   
   protected AbstractBoundedUnivariateFunction(
      final double minimumInDomain,
      final double maximumInDomain) {
      this(new SimpleDomainBounds(minimumInDomain, maximumInDomain));
   }
   
   protected AbstractBoundedUnivariateFunction(final BoundedDomainMixin domainBounds) {
      this.domainBounds = domainBounds;
   }
   
   @Override
   public double getMinimumInDomain() {
      return domainBounds.getMinimumInDomain();
   }
   
   @Override
   public double getMaximumInDomain() {
      return domainBounds.getMaximumInDomain();
   }
}
