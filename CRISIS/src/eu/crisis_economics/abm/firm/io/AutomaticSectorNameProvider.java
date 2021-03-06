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
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
  * An implementation of the {@link SectorNameProvider} interface. This implementation
  * generates simple sector names suffixed with a number. Representitive sector names
  * include <code>SECTOR0, SECTOR1, SECTOR2 ...</code>.
  * 
  * @author phillips
  */
public final class AutomaticSectorNameProvider implements SectorNameProvider {
   
   public final static String
      DEFAULT_SECTOR_BASE_NAME = "SECTOR";
   
   private String
      sectorBaseName;
   private int
      numberOfSectors;
   
   /**
     * Create an {@link AutomaticSectorNameProvider} object with default parameters.
     * See also {@link #AutomaticSectorNameProvider(String, int)}.
     * 
     * @param numberOfSectors
     */
   public AutomaticSectorNameProvider(
      final int numberOfSectors
      ) {
      this(DEFAULT_SECTOR_BASE_NAME, numberOfSectors);
   }
   
   /**
     * Create an {@link AutomaticSectorNameProvider} object with custom parameters.
     * See also {@link AutomaticSectorNameProvider}.
     * 
     * @param sectorBaseName
     *        The base name to be used to generate sector names. Sector names 
     *        generated by this class will take the form <code>sectorBaseNameX</code>
     *        where {@code X} is a non-negative number.
     * @param numberOfSectors
     *        The number of sector names to generate. This argument should be
     *        non-negative.
     */
   @Inject
   public AutomaticSectorNameProvider(
   @Named("AUTOMATIC_SECTOR_NAME_FACTORY_BASE_NAME")
      final String sectorBaseName,
   @Named("AUTOMATIC_SECTOR_NAME_FACTORY_NUMBER_OF_SECTORS")
      final int numberOfSectors
      ) {
      Preconditions.checkArgument(numberOfSectors > 0);
      this.sectorBaseName = Preconditions.checkNotNull(sectorBaseName);
      this.numberOfSectors = numberOfSectors;
   }
   
   @Override
   public LinkedHashSet<String> asSet() {
      final LinkedHashSet<String>
         result = new LinkedHashSet<String>();
      for(int i = 0; i< numberOfSectors; ++i)
         result.add(createSectorName(i));
      return result;
   }
   
   @Override
   public List<String> asList() {
      final List<String>
         result = new ArrayList<String>();
      for(int i = 0; i< numberOfSectors; ++i)
         result.add(createSectorName(i));
      return result;
   }
   
   private String createSectorName(int index) {
      return sectorBaseName + index;
   }
   
   /**
     * Get the basename for sector names generated with with {@link AutomaticSectorNameProvider}.
     */
   public String getSectorBaseName() {
      return sectorBaseName;
   }
   
   /**
     * Get the size of the sector name collection generated by this factory.
     */
   public int getNumberOfSectors() {
      return numberOfSectors;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Automatic Sector Name Factory, sector base name:" + sectorBaseName
            + ", number of sectors:" + numberOfSectors + ".";
   }
}
