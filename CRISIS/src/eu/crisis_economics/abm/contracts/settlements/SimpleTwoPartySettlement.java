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

/**
  * Transfer funds from one party to another without intermediaries, auxilliaries or
  * deductions.
  * 
  * @author phillips
  */
class SimpleTwoPartySettlement extends AbstractTwoPartySettlement {
   
   SimpleTwoPartySettlement(
      final SettlementParty firstParty,
      final SettlementParty secondParty,
      final SettlementPartyNotifier partyNotifier
      ) {
      super(firstParty, secondParty, partyNotifier);
   }
   
   SimpleTwoPartySettlement(
      final SettlementParty firstParty,
      final SettlementParty secondParty
      ) {
      super(firstParty, secondParty);
   }
   
   /**
     * Attempt to execute a directed transfer of funds.
     */
   @Override
   protected final double executeDirectedTransfer(
      final SettlementParty source,
      final SettlementParty target,
      final double amountToTransfer
      ) throws InsufficientFundsException {
      if(amountToTransfer == 0) return 0.;
      double amountCredited;
      try {
         amountCredited = source.credit(amountToTransfer);
      }
      catch(final InsufficientFundsException e) {
         source.cashFlowInjection(amountToTransfer);
         amountCredited = source.credit(amountToTransfer);
      }
      postCredit(amountCredited);
      target.debit(amountCredited);
      postDebit(amountCredited);
      return amountCredited;
   }
}