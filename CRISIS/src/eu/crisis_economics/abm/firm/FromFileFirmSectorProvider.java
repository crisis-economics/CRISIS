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
package eu.crisis_economics.abm.firm;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.model.parameters.FromFileStringParameter;

/**
  * An implementation of the {@link FirmSectorProvider} interface. This implementation
  * reads sectors from a text file and returns the sector designated to each {@link Firm}
  * in sequence.<br><br>
  * 
  * This implementation assumes a text file with one sector name (a string) per line.
  * 
  * @author phillips
  */
public final class FromFileFirmSectorProvider implements FirmSectorProvider {
   
   private FromFileStringParameter
      sectors;
   
   /**
     * Create a {@link FromFileFirmSectorProvider} object from a text file.
     * 
     * @param filename
     *        The text file from which to read sectors. This argument must not
     *        be <code>null</code>.
     */
   @Inject
   public FromFileFirmSectorProvider(
   @Named("FROM_FILE_FIRM_SECTOR_NAME_PROVIDER_SOURCE_FILENAME")
      final String filename
      ) {
      try {
         this.sectors = new FromFileStringParameter(
            Preconditions.checkNotNull(filename), "Firm Sectors");
      } catch (final IOException e) {
         throw new IllegalArgumentException(
            "From File Firm Sector Provider: the filename " + filename + " could not be parsed.");
      }
   }
   
   @Override
   public String get() {
      return sectors.get();
   }
}
