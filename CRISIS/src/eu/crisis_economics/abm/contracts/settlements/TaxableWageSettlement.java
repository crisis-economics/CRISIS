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
package eu.crisis_economics.abm.contracts.settlements;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * Taxable wage settlement with no party notifications.
  * @author phillips
  */
final class TaxableWageSettlement extends AbstractTaxableTwoPartySettlement {
   TaxableWageSettlement(
      final Employer firstParty,
      final Employee secondParty
      ) {
      super(firstParty, secondParty, new SimpleSettlementPartyNotifier());
      Preconditions.checkNotNull(
         Simulation.getRunningModel().getGovernment(),
         "TaxableWageSettlement: taxable wage settlements require a government agent.");
   }
   
   /**
     * Get the tax per unit cash spend on labour wages, if any, as specified by government.
     */
   @Override
   protected double getTaxProportion() {
      return super.getGoverment().getLabourTaxLevel();
   }
}
