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
import eu.crisis_economics.abm.contracts.UniquelyIdentifiable;

/**
  * A naming convention for {@link UniquelyIdentifiable} {@link Agent}{@code s}.
  * 
  * @author phillips
  */
public interface AgentNameFactory {
   /**
     * Generate a <i>uniquely identifying</i> name for the specified {@link Agent}.
     * The return value of this method should be unique among all {@link Agent}{@code s}.
     * 
     * @param agent
     *        The {@link Agent} for which to generate a uniquely identifying name.
     */
   public String generateNameFor(Agent agent);
}
