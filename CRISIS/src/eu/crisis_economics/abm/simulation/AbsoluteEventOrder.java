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

import org.apache.commons.math3.exception.NullArgumentException;

/**
  * An ordered simulation event, including
  *  (a) event priority at time t,
  *  (b) an execution interval between successive firings of the event,
  *  (c) a delay prior to the first firing of the event.
  */
abstract class AbsoluteEventOrder {
   private SimulatedEventOrder
      cycleOrder;
   private static final int 
      DEFAULT_REPEAT_EXECUTION_INTERVAL = ScheduleIntervals.ONE_DAY.getValue(),
      DEFAULT_EXECUTION_DELAY = ScheduleIntervals.ONE_DAY.getValue();
   private int
      delayPeriod;
   
   /** Create a repeating or non-repeating event with priority. */
   public static AbsoluteEventOrder createEvent(
      SimulatedEventOrder eventOrder,
      boolean isRepeating) {
      if(isRepeating)
         return new RepeatingOrderedEvent(eventOrder);
      else
         return new NonRepeatingOrderedEvent(eventOrder);
   }
   
   /** Create a repeating or non-repeating event with priority and an execution delay. */
   public static AbsoluteEventOrder createEvent(
      SimulatedEventOrder eventOrder,
      boolean isRepeating,
      ScheduleInterval executionDelay
      ) {
      if(isRepeating)
         return new RepeatingOrderedEvent(
            eventOrder, executionDelay.getValue());
      else
         return new NonRepeatingOrderedEvent(eventOrder, executionDelay.getValue());
   }
   
   /** Create a repeating event with an execution interval and an execution delay. */
   public static AbsoluteEventOrder createEvent(
      SimulatedEventOrder eventOrder,
      ScheduleInterval executionInterval,
      ScheduleInterval executionDelay) {
      return new RepeatingOrderedEvent(
         eventOrder, executionDelay.getValue(), executionInterval.getValue());
   }
   
   // Repeating, ordered events.
   private static final class RepeatingOrderedEvent extends AbsoluteEventOrder {
      private int executionInterval;
      private RepeatingOrderedEvent(
         final SimulatedEventOrder eventOrder,
         int executionDelay,
         int executionInterval
         ) {
         super(eventOrder, executionDelay);
         this.executionInterval = executionInterval;
      }
      private RepeatingOrderedEvent(
         SimulatedEventOrder eventOrder,
         int executionDelay) {
         this(eventOrder, executionDelay, DEFAULT_REPEAT_EXECUTION_INTERVAL);
      }
      private RepeatingOrderedEvent(
         SimulatedEventOrder eventOrder) {
         super(eventOrder);
         this.executionInterval = DEFAULT_REPEAT_EXECUTION_INTERVAL;
      }
      @Override
      public boolean isRepeating() { return true; }
      @Override
      public int getSuccessiveExecutionInterval() { 
         return executionInterval;
      }
   }
   
   // Non-Repeating, Ordered Future Events.
   private static final class NonRepeatingOrderedEvent extends AbsoluteEventOrder {
      protected NonRepeatingOrderedEvent(SimulatedEventOrder eventOrder) {
         super(eventOrder);
      }
      protected NonRepeatingOrderedEvent(
         SimulatedEventOrder eventOrder,
         int executionDelay) {
         super(eventOrder, executionDelay);
      }
      @Override
      public boolean isRepeating() { return false; }
      @Override
      public int getSuccessiveExecutionInterval() { return 0; }
   }
   
   private AbsoluteEventOrder(SimulatedEventOrder eventOrder) {
      if(eventOrder == null) 
         throw new NullArgumentException();
      this.cycleOrder = eventOrder;
      this.delayPeriod = DEFAULT_EXECUTION_DELAY;
   }
   
   private AbsoluteEventOrder(    // Privately Constructed
      final SimulatedEventOrder cycleOrder,
      int executionDelay
      ) {
      if(cycleOrder == null) 
         throw new NullArgumentException();
      if(executionDelay < 0)
         throw new IllegalArgumentException(
            "OrderedEvent: execution delay (value " + executionDelay + ")" +
            "is non-positive.");
      this.cycleOrder = cycleOrder;
      this.delayPeriod = executionDelay;
   }
   
   /** Get the order (named priority) of this event. */
   public SimulatedEventOrder getCycleOrder() {
      return cycleOrder;
   }
   
   /** Get the interval between successive executions. */
   public abstract int getSuccessiveExecutionInterval();
   
   /** Get the waiting period until the event is active. */
   public int getDelayPeriod() {
      return delayPeriod;
   }
   
   /** Is this event repeating? */
   public abstract boolean isRepeating();
   
   @Override
   public String toString() {
      return "Ordered Event, order: " + getCycleOrder() + 
         ", successive execution interval: " + getSuccessiveExecutionInterval() + ".";
   }
}
