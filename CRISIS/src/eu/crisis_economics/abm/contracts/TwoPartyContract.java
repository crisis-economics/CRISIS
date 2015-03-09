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
package eu.crisis_economics.abm.contracts;

import org.apache.commons.math3.exception.NullArgumentException;

import eu.crisis_economics.abm.contracts.settlements.SettlementParty;

/**
 * @author bochmann
 * @author phillips
 */
public abstract class TwoPartyContract extends Contract {
   private SettlementParty
      firstParty,
      secondParty;
   
   /** Contract with no expiry. */
   protected TwoPartyContract(
      SettlementParty firstParty,
      SettlementParty secondParty
      ) {
      this(firstParty, secondParty, Double.POSITIVE_INFINITY);
   }
   
   protected TwoPartyContract(
      SettlementParty firstParty,
      SettlementParty secondParty,
      final double expirationTime
      ) {
      super(expirationTime);
      this.firstParty = firstParty;
      this.secondParty = secondParty;
   }
   
   /** Get the first settlement party in this two-party contract. */
   public final SettlementParty getFirstSettlementParty() {
      return firstParty;
   }
   
   protected final void setFirstSettlmentParty(
      SettlementParty newSettlmentParty) {
      if(newSettlmentParty == null)
         throw new NullArgumentException();
      this.firstParty = newSettlmentParty;
   }
   
   /** Get the second settlement part in this two-party contract. */
   public final SettlementParty getSecondSettlementParty() {
      return secondParty;
   }
   
   protected final void setSecondSettlementParty(
      SettlementParty newSettlmentParty) {
      if(newSettlmentParty == null)
         throw new NullArgumentException();
      this.secondParty = newSettlmentParty;
   }
   
   @Override
   public String toString() {
      return "TwoPartyContract, Super: " + super.toString();
   }
}
