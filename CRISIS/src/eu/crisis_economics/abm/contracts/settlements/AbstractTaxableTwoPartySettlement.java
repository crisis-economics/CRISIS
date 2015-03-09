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

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * Skeletal implementation of a taxable two party settlement.
  * @author phillips
  */
abstract class AbstractTaxableTwoPartySettlement
   extends AbstractTwoPartySettlement {
   
   AbstractTaxableTwoPartySettlement(
      final SettlementParty firstParty,
      final SettlementParty secondParty,
      final SettlementPartyNotifier partyNotifier
      ) {
      super(firstParty, secondParty, partyNotifier);
   }
   
   @Override
   protected final double executeDirectedTransfer(
      final SettlementParty source,
      final SettlementParty target,
      final double amountToTransfer
      ) throws InsufficientFundsException {
      if(amountToTransfer == 0) return 0.;
      final double
         taxProportion = getTaxProportion();
      double amountCredited;
      try {
         amountCredited = source.credit(amountToTransfer);
      }
      catch(final InsufficientFundsException e) {
         source.cashFlowInjection(amountToTransfer);
         amountCredited = source.credit(amountToTransfer);
      }
      postCredit(amountCredited);
      final double
         targetGain = (1. - taxProportion) * amountCredited,
         taxSum = taxProportion * amountCredited;
      target.debit(targetGain);
      postDebit(targetGain);
      getGoverment().debit(taxSum);
      return amountCredited;
   }
   
   /**
     * Get the taxation level (a number in the range [0, 1]) for this settlement.
     */
   protected abstract double getTaxProportion();
   
   /**
     * Get the government object in this simulation.
     */
   protected final Government getGoverment() {
      return Simulation.getRunningModel().getGovernment();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Taxable two party settlement, tax rate: " + getTaxProportion() + ", "
           + "first party: " + getFirstParty().getUniqueName() + ", second party: "
           + getSecondParty().getUniqueName() + ".";
   }
}
