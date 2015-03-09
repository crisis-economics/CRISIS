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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.base.Preconditions;

/**
  * An implementation of the {@link SectorNameProvider} interface. This 
  * implementation provides a manual specification for sector names.
  * 
  * @author phillips
  */
public final class ManualSectorNameProvider implements SectorNameProvider {
   
   private List<String>
      list;
   private LinkedHashSet<String>
      set;
   
   /**
     * Create a {@link ManualSectorNameProvider} object from a {@link Collection}
     * of manual {@link String} sector names.
     * 
     * @param sectorNames
     *        A {@link Collection} of sector names. This argument must be
     *        non-<code>null</code>. The iteration order of this collection
     *        specifies the natural ordering of the sector names.
     */
   public ManualSectorNameProvider(
      final Collection<String> sectorNames) {
      this.list = new ArrayList<String>(Preconditions.checkNotNull(sectorNames));
      this.set = new LinkedHashSet<String>(sectorNames);
   }
   
   @Override
   public LinkedHashSet<String> asSet() {
      return new LinkedHashSet<String>(set);
   }
   
   @Override
   public List<String> asList() {
      return new ArrayList<String>(list);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Manual Sector Name Provider, elements:" + list + ".";
   }
}
