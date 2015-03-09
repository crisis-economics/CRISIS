/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Olaf Bochmann
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

import java.util.HashSet;
import java.util.Set;

import sim.field.network.Network;

/**
 * @author olaf
 */
public class Settlements {
	
	private Set<SettlementListener> listeners = new HashSet<SettlementListener>();

	/**
      * holds contract relationships.
      */
    private Network contractNetwork;
	
	/**
	 * simple constructor
	 */
	public Settlements() {
		this.contractNetwork = new Network(directed());
	}

	public Settlement create(final SettlementParty partyA, final SettlementParty partyB){
		final Settlement settlement = SettlementFactory.createDirectSettlement(partyA,partyB);
		settlement.setListeners(listeners);
		contractNetwork.addEdge(partyA, partyB, settlement);
		return settlement;
	}

	boolean directed() { return true; }
}
