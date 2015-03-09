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
package eu.crisis_economics.abm.intermediary;

import eu.crisis_economics.abm.contracts.settlements.SettlementParty;

/**
  * A lightweight interface for a {@link SingleBeneficiaryIntermediary} factory
  * object. This interface is compatible with the dependency injector. The 
  * dependency injector does not require this class to be implemented.
  * 
  * @author phillips
  */
public interface SingleBeneficiaryIntermediaryFactory {
   /**
     * Create a {@link SingleBeneficiaryIntermediary} for the specified 
     * beneficiary.
     * 
     * @param beneficiary
     *        The {@link SettlementParty} on behalf of which the resulting
     *        {@link SingleBeneficiaryIntermediary} should conduct its
     *        business.
     */
   SingleBeneficiaryIntermediary createFor(SettlementParty beneficiary);
}
