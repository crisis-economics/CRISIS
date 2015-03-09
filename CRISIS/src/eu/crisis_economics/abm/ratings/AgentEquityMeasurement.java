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
package eu.crisis_economics.abm.ratings;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;

/**
  * Simple visitor measurement returning the equity of the visited agent.
  * @author phillips
  */
public final class AgentEquityMeasurement 
   extends SimpleAbstactAgentOperation<Double> implements AgentTrackingMeasurement {
   
   public AgentEquityMeasurement() { } // Stateless
   
   /**
     * Return the equity of the visited agent.
     */
   @Override
   public Double operateOn(final Agent agent) {
      return agent.getEquity();
   }
}
