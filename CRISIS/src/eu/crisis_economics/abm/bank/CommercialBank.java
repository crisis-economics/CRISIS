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
package eu.crisis_economics.abm.bank;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;

public class CommercialBank extends ClearingBank {
   
   @Inject
   public CommercialBank(
   @Named("COMMERCIAL_BANK_INITIAL_CASH_ENDOWMENT")
      final double initialBankCash,
      final ClearingBankStrategyFactory strategyFactory,
   @Named("COMMERCIAL_BANK_CLEARING_MARKET_RESPONSES")
      final ClearingMarketParticipantFactory marketParticipationFactory,
      final ClearingHouse clearingHouse
      ) {
      super(initialBankCash, strategyFactory, marketParticipationFactory, clearingHouse);
      
      clearingHouse.addBank(this);
      clearingHouse.addLender(this);
      clearingHouse.addStockMarketParticipant(this);
   }
   
   @Override
   public <T> T accept(final AgentOperation<T> operation) {
      return operation.operateOn(this);
   }
}
