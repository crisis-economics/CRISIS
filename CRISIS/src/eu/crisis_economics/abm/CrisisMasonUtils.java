/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm;

import java.util.ArrayList;
import java.util.List;

import sim.util.Bag;

/**
 * A set of common Mason utility classes.
 * 
 * @author rlegendi
 * @since 1.0
 */
public final class CrisisMasonUtils {
	
	/**
	 * Typesafely creates a {@link java.util.List} from the specified Mason <code>Bag</code>.
	 * 
	 * <p>
	 * Usage:
	 * </p>
	 * 
	 * <pre>
	 * CrisisMasonUtils.&lt;Contract&gt; bagToList( myMasonBagWithContract )
	 * </pre>
	 * 
	 * @param bag the bag instance to convert; cannot be <code>null</code>
	 * @param <T> the list element type: all of the elements in the specified bag are casted to <code>T</code>
	 * @return a new <code>List</code> instance that contains all of the elements; it is an <code>ArrayList</code> at the
	 *         moment but that might change, do not build on this feature
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> bagToList(final Bag bag) {
		if ( null == bag ) {
			throw new IllegalArgumentException( "Parameter bag cannot be null" );
		}
		
		final ArrayList<T> ret = new ArrayList<T>();
		
		for (final Object o : bag) {
			ret.add( (T) o );
		}
		
		return ret;
	}
	
	/**
	 * Creates a Mason <code>Bag</code> from the specified list
	 * 
	 * @param list to convert; cannot be <code>null</code>
	 * @return a new <code>Bag</code> instance that contains all of the elements of the input list
	 */
	public static <T> Bag listToBag(final List<T> list) {
		if ( null == list ) {
			throw new IllegalArgumentException( "Parameter cannot be null" );
		}
		
		final Bag ret = new Bag();
		
		for (final T t : list) {
			ret.add( t );
		}
		
		return ret;
	}
	
	private static final Bag emptyBag = new Bag();
	
	public static Bag getEmptybag() {
		return emptyBag;
	}
	
	// ======================================================================================================================
	
	private CrisisMasonUtils() {
		throw new AssertionError();
	}
	
	@Override
	protected Object clone()
			throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
}
