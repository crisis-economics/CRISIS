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
package eu.crisis_economics.abm.contracts.loans;

import java.io.IOException;

import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.utilities.EnumDistribution;

/**
  * @author phillips
  */
public enum MaturityDistribution {
   FIRM_ISSUE_BONDS,
   BANK_ISSUE_BONDS,
   BANK_ISSUE_HOUSEHOLD_MORTGAGES,
   BULLET_LOANS;                    // Increase flexibility if required.
   
   private EnumDistribution<ScheduleIntervals> dice;
   
   public final void setDistribution(String dataFilename)
      throws IOException {
      try {
         dice = EnumDistribution.create(ScheduleIntervals.class, dataFilename);
      } catch (IOException failedToLoadDistribution) {
         System.err.println(
            "MaturityDistribution.setDistribution [" + name() + "]: distribution could not " +
            "be loaded from the file " + dataFilename + ". Details follow. " + 
            failedToLoadDistribution.getMessage());
         throw failedToLoadDistribution;
      }
   }
   
   /**
     * Get an (immutable) schedule interval distribution for this loan type.
     * Returns null if the loan distribution has not been specified.
     */
   public final EnumDistribution<ScheduleIntervals> getDistribution() {
      return dice;
   }
   
   /**
     * Has the distribution of this loan type been specified?
     */
   public final boolean distributionHasBeenSpecified() {
      return (dice == null);
   }
}