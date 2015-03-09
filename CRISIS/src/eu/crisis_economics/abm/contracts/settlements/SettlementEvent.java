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

import java.util.EventObject;

import eu.crisis_economics.abm.contracts.Contract;

/**
 * @author olaf
 *
 */
public class SettlementEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6703112051056631462L;
	private Contract contract;
	private double amout;

	public SettlementEvent(final Object instrument, final Contract contract) {
		super(instrument);
		this.contract = contract;
	}

	public SettlementEvent(final Object settlement, double amout) {
		super(settlement);
		this.amout = amout;
	}

	public Contract getContract() {
		return contract;
	}

	public double getAmount() {
		return amout;
	}
}
