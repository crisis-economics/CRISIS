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

import com.google.common.base.Preconditions;

/**
  * Create a custom schedule point in the simulation cycle ordering.
  * @author phillips
  */
public final class CustomSimulationCycleOrdering implements SimulatedEventOrder {
   private final double
      precidence;
   private final int
      numberOfDeltas;
   
   private CustomSimulationCycleOrdering(double precidence, int numberOfDeltas) {
      Preconditions.checkArgument(precidence >= 0. && precidence <= 1.);
      // Preconditions.checkArgument(numberOfDeltas >= 0);
      this.precidence = precidence;
      this.numberOfDeltas = numberOfDeltas;
   }
   
   /**
     * Create a custom event point in the simulation cycle. 
     * 
     * Every simulation cycle is considered to be a unit interval, [0, 1], in 
     * time. The argument to this method specifies where, as compared to 
     * other events in this interval, an event should be processed.
     * 
     * @param time
     *    The point in the cycle order [0, 1] at which to process an event.
     *    This argument must be non-negative and not greater than 1.0.
     * 
     * Example: to create an object that specifies an event to be processed
     * at times (T mod 1.0 == 0.4), call: create(0.4).
     */
   public static SimulatedEventOrder create(double time) {
      CustomSimulationCycleOrdering result = new CustomSimulationCycleOrdering(time, 0);
      return result;
   }
   
   /**
     * Create a custom event point in the simulation cycle. 
     * 
     * Every simulation cycle is considered to be a unit interval, [0, 1], in 
     * time. The arguments to this method specify a point in the interval 
     * [0, 1] as follows:
     * 
     * @param namedEvent
     *    A named event from NamedSimulationCycleOrderings.
     * @param numberOfDeltas
     *    How many "infinitesimal" delays should follow namedEvent in the 
     *    resulting order. "infinitesimal" here means "as soon as possible
     *    after" namedEvent. 
     * 
     * Example: to create an event order that will be processed *as soon
     * as possible* after BEFORE_ALL:
     * 
     * X = create(final NamedSimulationCycleOrderings namedEvent, 1);
     *
     * To create an event order that will be processed *as soon
     * as possible* after X:
     * 
     * Y = create(final NamedSimulationCycleOrderings namedEvent, 2);
     * 
     * The relative order of BEFORE_ALL, X and Y is now:
     * 
     * BEFORE_ALL comes just before X comes just before Y.
     */
   public static SimulatedEventOrder create(
      final SimulatedEventOrder namedEvent,
      int numberOfDeltas
      ) {
      return new CustomSimulationCycleOrdering(namedEvent.getUnitIntervalTime(), numberOfDeltas);
   }
   
   @Override
   public double getUnitIntervalTime() {
      return precidence;
   }
   
   /** 
     * Convert an SimulatedEventOrder and a delay period (number of cycles) to 
     * an absolute simulation time.
     */
   static double toAbsoluteSimulationTime(
      final SimulatedEventOrder event,
      int cyclesToPostpone
      ) {
      return Simulation.getFloorTime() + cyclesToPostpone + event.getUnitIntervalTime();
   }
   
   @Override
   public int getPriority() {
      return numberOfDeltas;
   }
   
   @Override
   public String toString() {
      return "Custom Simulation Event Order, order: " + getUnitIntervalTime() 
           + ", priority: " + numberOfDeltas + ".";
   }
}
