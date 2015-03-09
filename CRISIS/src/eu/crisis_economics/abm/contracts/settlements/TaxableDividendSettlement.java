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

import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * Taxable dividend settlement with dividend payment notification.
  * @author phillips
  */
final class TaxableDividendSettlement extends AbstractTaxableTwoPartySettlement {
   TaxableDividendSettlement(
      final StockReleaser firstParty,
      final StockHolder secondParty
      ) {
      super(firstParty, secondParty, new DividendSettlementPartyNotifier(firstParty, secondParty));
      Preconditions.checkNotNull(
         Simulation.getRunningModel().getGovernment(),
         "TaxableDividendSettlement: taxable dividend settlements require a government agent.");
   }
   
   /**
     * Get the dividend transaction tax, if any, as specified by government.
     */
   @Override
   protected double getTaxProportion() {
      return super.getGoverment().getDividendTaxLevel();
   }
}
