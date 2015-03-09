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
package eu.crisis_economics.abm.firm.io;

import java.util.LinkedHashSet;
import java.util.List;

/**
  * An interface to create sector names.
  * 
  * @author phillips
  */
public interface SectorNameProvider {
   /**
     * Get a {@link LinkedHashSet} of sector names. The iteration order of the
     * returned collection is fixed. Adding or removing elements from the 
     * return value will not affect this object.
     */
   public LinkedHashSet<String> asSet();
   
   /**
     * Get a {@link List} of sector names. Adding or removing elements from
     * the returned collection will not affect this object.s
     */
   public List<String> asList();
}
