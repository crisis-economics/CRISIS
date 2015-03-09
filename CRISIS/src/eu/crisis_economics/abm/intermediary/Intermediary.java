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

import java.util.Collection;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.IAgent;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;

/**
  * An intermediary type that conducts its business on behalf of 
  * a set of {@link SettlementParty} beneficiaries. An example of an 
  * {@link Intermediary} is a stockholding agent that retains, and 
  * subsequently sells, stocks on behalf of beneficiaries who cannot 
  * process or sell shares themselves. Typically a {@link Intermediary} 
  * {@link SettlementParty} will retain no assets or cash of its own; 
  * the assets of the {@link Intermediary} are typically held purely on 
  * behalf of its beneficiaries.
  * 
  * @author phillips
  */
public interface Intermediary extends IAgent, StockHolder, ClearingMarketParticipant {
   /**
    * Get a reference to all {@link Agent} beneficiaries on whose behalf this
    * {@link Intermediary} conducts its business. No references are retained
    * to the collection this method returns; it is safe to modify the 
    * returned collection.
    */
   public Collection<SettlementParty> getBeneficiaries();
}
