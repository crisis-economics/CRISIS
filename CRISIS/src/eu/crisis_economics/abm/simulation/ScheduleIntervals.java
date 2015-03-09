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
package eu.crisis_economics.abm.simulation;

/**
  * Physical intervals between simulated events.
  * @author phillips
  */
public enum ScheduleIntervals implements ScheduleInterval {
   ONE_DAY(1),
   ONE_MONTH(30),
   TWO_MONTH(60),
   ONE_QUARTER(90),
   HALF_YEAR(180),
   ONE_YEAR(360),                                               // Rounded, for convenience.
   TWO_YEAR(720),
   FIVE_YEAR(1800);
   private int timeInterval;
   ScheduleIntervals(int timeInterval) {
      this.timeInterval = timeInterval;
   }
   @Override
   public int getValue() {
      return timeInterval;
   }
   
   /**
     * Create a {@link ScheduleInterval} object with a custom interval.
     * @param interval
     */
   public static ScheduleInterval create(final int interval) {
      return new ScheduleInterval() {
         @Override
         public int getValue() {
            return interval;
         }
      };
   }
}
