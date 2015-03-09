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

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.firm.io.SectorNameProvider;
import eu.crisis_economics.abm.model.parameters.IntegerCounter;

/**
  * A simple implementation of the {@link FirmSectorProvider} interface. This
  * implementation draws sector names from a {@link SectorNameProvider} object.
  * Each sector name in the {@link SectorNameProvider} is used multiple times.
  * The number of times each sector name is used is customizable.<br><br>
  * 
  * Concretely: for a {@link SectorNameProvider} with {@code 3} sectors,
  * <code>A, B, C ...</code> this provider will return sequences of the form:
  * 
  * <center><code>
  *   A, B, C ...
  * </code></center><br>
  * <center><code>
  *   A, A, B, B, C, C ...
  * </code></center><br><br>
  * 
  * The length of the repeating block is referred to as the {@code block size}.
  * 
  * @author phillips
  */
public final class MultipleFirmsPerSectorProvider implements FirmSectorProvider {
   
   private final List<String>
      sectorNames;
   private final IntegerCounter
      counter;
   private final int
      numberOfFirmsPerSector;
   
   /**
     * Create a {@link MultipleFirmsPerSectorProvider} object with custom parameters.
     * 
     * @param nameProvider
     *        A {@link SectorNameProvider} object to provide a {@link List} of 
     *        sector names in this model.
     * @param numberOfFirmsPerSector
     *        The degree of repetition when drawing sector names from the above
     *        provider. This argument must be strictly positive.
     */
   @Inject
   public MultipleFirmsPerSectorProvider(
      final SectorNameProvider nameProvider,
   @Named("NUMBER_OF_FIRMS_PER_SECTOR")
      final int numberOfFirmsPerSector
      ) {
      Preconditions.checkArgument(numberOfFirmsPerSector > 0);
      this.sectorNames = Preconditions.checkNotNull(nameProvider).asList();
      this.counter = new IntegerCounter("SectorProvider", numberOfFirmsPerSector);
      this.numberOfFirmsPerSector = numberOfFirmsPerSector;
   }
   
   /**
     * Get the number of firms per sector for this provider.
     */
   public int getNumberOfFirmsPerSector() {
      return numberOfFirmsPerSector;
   }
   
   @Override
   public String get() {
      return sectorNames.get(counter.get());
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Multiple Firms Per Sector Provider, number of firms per sector: "
            + numberOfFirmsPerSector + ".";
   }
}
