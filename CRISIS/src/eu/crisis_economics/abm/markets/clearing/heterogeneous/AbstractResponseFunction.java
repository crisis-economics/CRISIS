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

import org.apache.commons.math3.exception.NullArgumentException;

public abstract class AbstractResponseFunction
   implements MarketResponseFunction {
   
   private SimpleDomainBounds domainBounds;
   
   protected AbstractResponseFunction() {
      this.domainBounds = new SimpleDomainBounds();
   }
   
   protected AbstractResponseFunction(
      final double minimumInDomain,
      final double maximumInDomain) {
      this(new SimpleDomainBounds(minimumInDomain, maximumInDomain));
   }
   
   protected AbstractResponseFunction(final SimpleDomainBounds domainBounds) {
      if(domainBounds == null)
         throw new NullArgumentException();
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
