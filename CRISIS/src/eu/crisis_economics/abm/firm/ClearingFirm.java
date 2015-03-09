/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
 * Copyright (C) 2015 Ariel Y. Hoffman
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
package eu.crisis_economics.abm.firm;

import com.google.inject.Inject;

import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;

/**
  * @author bochmann
  * @author phillips
  */
public abstract class ClearingFirm extends LoanStrategyFirm implements ClearingMarketParticipant {
   
   private
      ClearingHouse clearingHouse;
   
   /**
     * Create a {@link ClearingFirm} object.
     */
   public ClearingFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final double numberOfShares,
      final double emissionPrice,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            emissionPrice,
            nameGenerator
            );
   }
   
   public final ClearingHouse getClearingHouse() {
      return clearingHouse;
   }
   
   @Inject
   public final void setClearingHouse(
      ClearingHouse clearingHouse) {
      if(this.clearingHouse!=null)
         this.clearingHouse.removeParticipant(this);
      this.clearingHouse = clearingHouse;
      if(this.clearingHouse != null) 
         if (!clearingHouse.hasParticipant(this)) { 
            clearingHouse.addFirm(this);
            clearingHouse.addBorrower(this);
         }
   }
}