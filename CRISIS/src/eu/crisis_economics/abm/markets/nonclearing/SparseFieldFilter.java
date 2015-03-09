/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.markets.nonclearing;

import sim.field.SparseField;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;

/**
 * @author Tamás Máhr
 *
 */
public class SparseFieldFilter extends Filter {

	protected SparseField field;
	
	protected Party toCompare;
	
	/**
	 * @param field
	 */
	public SparseFieldFilter(Party toCompare, SparseField field) {
		super();
		this.toCompare = toCompare;
		this.field = field;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public boolean matches(Order order) {
		return field.getObjectsAtLocationOfObject(toCompare).contains(order.getParty());
	}

}
