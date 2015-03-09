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
package eu.crisis_economics.abm.inventory;

import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.SimulatedEventOrder;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * Skeletal implementation of an (auto-scheduled) "apply the effect
  * of the passage of time" event. This implementation calls the 
  * method AbstractPerishable.applyPassageOfTime periodically.
  * 
  * Inheritance: implement protected void applyPassageOfTime(
  * double timeHasElapsed). This method will be called periodically,
  * at times, and with frequency, as specified by the constructor.
  * 
  * @author phillips
  */
public abstract class AbstractPerishable {
   private double
      timeOfLastCall;
   
   public AbstractPerishable(
      final SimulatedEventOrder whenToApply,
      final ScheduleIntervals intervalBetweenApplications
      ) {
      // Pretent first call a time T = (0 + Application Time - Interval):
      this.timeOfLastCall = 
         whenToApply.getUnitIntervalTime() - intervalBetweenApplications.getValue();
      Simulation.repeatCustom(
         this, "applyPassageOfTimeImpl", whenToApply,
         intervalBetweenApplications, intervalBetweenApplications
         );
   }
   
   /**
     * Called periodically, as defined by the constructor.
     */
   abstract protected void applyPassageOfTime(double timeHasElapsed);
   
   @SuppressWarnings("unused") // Scheduled
   private void applyPassageOfTimeImpl() {
      applyPassageOfTime(getTimeHasElapsed());
      timeOfLastCall = Simulation.getTime();
   }
   
   /**
     * Get the amount of simulation time that has elapsed since the last
     * call to applyPassageOfTime().
     */
   protected final double getTimeHasElapsed() {
      return Simulation.getTime() - timeOfLastCall;
   }
}
