/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.household;

import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author olaf
 *
 */
public abstract class StrategicHousehold extends StockTradingHousehold {

   /**
     * Creates a household that can deposit and withdraw money.
     * 
     * @param depositHolder
     * @param initialDeposit
     */
   public StrategicHousehold(
      DepositHolder depositHolder,
      double initialDeposit) {
      super(depositHolder, initialDeposit);
      Simulation.repeat(this, "considerDepositPayment",
            NamedEventOrderings.DEPOSIT_PAYMENT);
   }

	/**
	 * Subclasses should implement the households's deposit strategy by
	 * overriding this function. This method is called once in each turn to
	 * allow the household to make its move.
	 * 
	 */
	protected abstract void considerDepositPayment();
}

