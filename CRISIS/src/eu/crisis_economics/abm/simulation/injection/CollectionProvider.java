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
package eu.crisis_economics.abm.simulation.injection;

import java.util.Collection;
import java.util.List;

/**
  * An interface for a {@link List<T>} provider class. Implementations of this
  * class produce a (non-null, but potentially empty) {@link List} of objects
  * when queried.
  * 
  * @author phillips
  */
public interface CollectionProvider<T> extends Iterable<T> {
   /**
     * Produce a list of objects. Adding or removing elements from
     * the return value will not affect this object.
     */
   public Collection<T> get();
}
