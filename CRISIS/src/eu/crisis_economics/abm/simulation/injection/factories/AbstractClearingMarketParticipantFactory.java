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
package eu.crisis_economics.abm.simulation.injection.factories;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.CompositeMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ForwardingClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A skeletal factory for {@link ClearingMarketParticipant} factories. This 
  * skeletal {@link ClearingMarketParticipantFactory} accumulates
  * {@link MarketResponseFunction} factories and subsequently creates a 
  * {@link ClearingMarketParticipant} with access to all such market responses.
  * 
  * @author phillips
  */
public abstract class AbstractClearingMarketParticipantFactory
   implements ClearingMarketParticipantFactory {
   
   private CompositeMarketResponseFunctionFactory
      responses = new CompositeMarketResponseFunctionFactory();
   
   /**
     * Create an empty {@link AbstractClearingMarketParticipantFactory} object
     * with no responses to any markets.
     */
   protected AbstractClearingMarketParticipantFactory() {
      this.responses = new CompositeMarketResponseFunctionFactory();
      
      Simulation.onceCustom(this, "setupStockMarketResponses",
         CustomSimulationCycleOrdering.create(
            Simulation.getTime() - Math.floor(Simulation.getTime())),
         ScheduleIntervals.create(0));
   }
   
   /**
     * When implementing this class, provide the following method.<br><br>
     * 
     * This method will be called exactly once, at time {@code BEFORE_ALL}, at the next 
     * soonest opportunity. Your implementation should install {@link MarketResponseFunction}
     * factories for all clearing stock markets in which the
     * {@link ClearingMarketParticipant} should be engaged.
     */
   protected abstract void setupStockMarketResponses();
   
   /**
     * When implementing this class, provide the following method.<br><br>
     * 
     * This method will be called exactly once, at time {@code BEFORE_ALL}, at the next 
     * soonest opportunity. Your implementation should install {@link MarketResponseFunction}
     * factories for all clearing loan markets in which the
     * {@link ClearingMarketParticipant} should be engaged.
     */
   protected abstract void setupLoanMarketResponses();
   
   /**
     * Get a reference to all market responses yet specified. Modifying the 
     * return value will modify the state of this object.
     */
   protected final CompositeMarketResponseFunctionFactory responses() {
      return responses;
   }
   
   @Override
   public final ClearingMarketParticipant create(Agent parent) {
      return new ForwardingClearingMarketParticipant(parent, responses);
   }
}