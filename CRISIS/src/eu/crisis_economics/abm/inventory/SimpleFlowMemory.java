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

import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * Simple implementation of the FlowMemory interface.
  * @author phillips
  */
public final class SimpleFlowMemory implements FlowMemory {
   private double
      netFlow,    // Net (signed) flow since the last flush
      outflow,    // Net outflow since the last flush
      inflow;     // Net inflow since the last flush
   
   private static NamedEventOrderings
      DEFAULT_FLUSH_ORDER = NamedEventOrderings.BEFORE_ALL;
   private static ScheduleIntervals
      DEFAULT_FLUSH_INTERVAL = ScheduleIntervals.ONE_DAY;
   
   /**
     * Create a simple flow memory tracker with periodic resets (flushes).
     * @param whenToFlush
     *        When, in the simulation cycle, to reset flow memories (data
     *        collected about flows)
     * @param intervalBetweenFlushes
     *        How frequently to reset flow memories.
     */
   public SimpleFlowMemory(
      final NamedEventOrderings whenToFlush,
      final ScheduleIntervals intervalBetweenFlushes
      ) {
      StateVerifier.checkNotNull(whenToFlush, intervalBetweenFlushes);
      
      Simulation.repeatCustom(
         this, "flushFlowData", whenToFlush, intervalBetweenFlushes, intervalBetweenFlushes);
   }
   
   /**
     * Create a simple flow memory tracker with periodic resets (default
     * frequency and interval between resets).
     */
   public SimpleFlowMemory() {
      this(DEFAULT_FLUSH_ORDER, DEFAULT_FLUSH_INTERVAL);
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void flushFlowData() {
      flush();
   }
   
   @Override
   public void recordInflow(double positiveAmount) {
      if(positiveAmount <= 0.) return;                              // Silent
      inflow += positiveAmount;
      netFlow += positiveAmount;
   }

   @Override
   public void recordOutflow(double positiveAmount) {
      if(positiveAmount <= 0.) return;                              // Silent
      outflow += positiveAmount;
      netFlow -= positiveAmount;
   }

   @Override
   public void recordFlow(double amount) {
      if(amount > 0) recordInflow(amount);
      else if(amount < 0) recordOutflow(-amount);
      else return;                                                  // Silent
   }
   
   @Override
   public double netFlowAtThisTime() {
      return netFlow;
   }
   
   @Override
   public double totalInflowAtThisTime() {
      return inflow;
   }
   
   @Override
   public double totalOutflowAtThisTime() {
      return outflow;
   }

   @Override
   public void flush() {
      inflow = 0.;
      outflow = 0.;
      netFlow = 0.;
   }
}
