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
package eu.crisis_economics.abm.simulation.injection.factories;

import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;

/**
  * A lightweight {@link ClearingLoanMarket} factory interface. This interface is 
  * used by the dependency injector. It is not necessary to implement this interface.
  * 
  * @author phillips
  */
public interface ClearingLoanMarketFactory {
   /**
     * Create a {@link ClearingLoanMarket}.
     * 
     * @param marketName
     *        The {@link String} name of the market to create.
     * @param clearingHouse
     *        The {@link ClearingHouse} to which the {@link ClearingMarket} should
     *        belong.
     */
   public ClearingLoanMarket create(String marketName, ClearingHouse clearingHouse);
}
