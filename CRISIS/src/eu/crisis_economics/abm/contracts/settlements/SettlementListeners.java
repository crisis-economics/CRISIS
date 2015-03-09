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

import java.util.HashSet;

/**
 * SettlementListeners maintains a static reference to the sole singleton instance 
 * and return a reference to that instance from a static instance() method. 
 * Note, it is not thread-safe.
 * @author bochmann
 *
 */
public final class SettlementListeners extends HashSet<SettlementListener> {

	private static final long serialVersionUID = 1L;
	private static SettlementListeners instance = null;
	private SettlementListeners() {
		// Exists only to defeat instantiation.
	}
	public static SettlementListeners getInstance() {
		if(instance == null) {
			instance = new SettlementListeners();
		}
		return instance;
	}
}
