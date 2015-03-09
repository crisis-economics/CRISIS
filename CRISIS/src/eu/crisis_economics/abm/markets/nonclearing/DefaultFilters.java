/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Olaf Bochmann
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

import java.util.Arrays;
import java.util.List;

import eu.crisis_economics.abm.markets.Party;

public class DefaultFilters {
	public static abstract class Filter {
		public abstract boolean matches(Order order);
	}
	
	private static final Filter any = new Filter() {
		@Override
		public boolean matches(final Order order) {
			return true;
		}
	};
	
	public static Filter any() {
		return any;
	}
	
	public static Filter only(final Party... parties) {
		final List<Party> partiesList = Arrays.asList( parties ); // TODO Check
		return new Filter() {
			@Override
			public boolean matches(final Order order) {
				return partiesList.contains( order.getParty() );
			}
		};
	}
}
