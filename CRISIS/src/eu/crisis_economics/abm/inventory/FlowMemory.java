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

/**
  * Interface for a flow utility class with memory about signed flows (Eg.
  * flow of funds, or flow of goods).
  * @author phillips
  */
public interface FlowMemory {
   /**
     * Record an inflow. If the argument to this method is negative or
     * zero, no action is taken.
     */
   public void recordInflow(double positiveAmount);
   
   /**
     * Record an outflow. If the argument to this method is negative or
     * zero, no action is taken.
     */
   public void recordOutflow(double positiveAmount);
   
   /**
     * Record a signed flow. Depending on the argument, x, to this method:
     *   (a) if x > 0 then this method is equivalent to recordInflow(x),
     *   (b) if x < 0 then this method is equivalent to recordOutflow(x),
     *   (c) otherwise no action is taken.
     */
   public void recordFlow(double amount);
   
   /**
     * This method is equivalent to FlowMemoryStorage.totalInflowAtThisTime - 
     * FlowMemoryStorage.totalOutflowAtThisTime.
     */
   public double netFlowAtThisTime();
   
   /**
     * Get the net inflow of stored quantity in this timestep.
     */
   public double totalInflowAtThisTime();
   
   /**
     * Get the net outflow of stored quantity in this timestep.
     */
   public double totalOutflowAtThisTime();
   
   /**
     * Reset the object to its initial state.
     */
   public void flush();
}
