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
package eu.crisis_economics.abm.agent;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A simple {@link AgentOperation<Void>} which checks for negative asset
  * contract values. If such an asset is identified, {@link IllegalStateException}
  * is raised.
  * 
  * @author phillips
  */
public final class CheckForNegativeAssetsOperation extends SimpleAbstactAgentOperation<Void> {
   
   /**
     * Create a stateless {@link CheckForNegativeAssetsOperation} object.
     */
   public CheckForNegativeAssetsOperation() { }   // Stateless
   
   /**
     * Check the {@link Agent} in the argument for negative contract values,
     * at event {@code (E + 1)} for each named event {@code E} in the
     * {@link SimulationEventOrder}. If, at any future time, the {@link Agent}
     * is found to have a negative asset contract value, {@link IllegalArgumentException}
     * is raised.
     */
   @Override
   public Void operateOn(Agent agent) {
      for(final NamedEventOrderings event : NamedEventOrderings.values()) {
         Simulation.repeat(this, "execute",
            CustomSimulationCycleOrdering.create(event, 2), (Agent) agent);
      }
      return null;
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void execute(final Agent agent) {
      boolean foundBadContract = false;
      for(final Contract contract : agent.getAssets()) {
         if(contract.getValue() < -1. || contract.getFaceValue() < -1.) {
            System.out.println(
               "Contract " + contract.getClass().getName() + " has negative value. " +
               "value: " + contract.getValue() + ", creation time: " +
               contract.getCreationTime() + ". toString: " + contract + "."
               );
            foundBadContract = true;
         }
      }
      for(final Contract contract : agent.getLiabilities()) {
         if(contract.getValue() < -1. || contract.getFaceValue() < -1.) {
            foundBadContract = true;
         }
      }
      if(foundBadContract)
         throw new IllegalStateException(
           "A contract with a negative value was identified. This behaviour is indicative of "
         + "an error and is not expected in the current implementation. Write to KP with"
         + "the details of the above error message and the current stack trace.");
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Check For Negative Assets Agent Operation.";
   }
}
