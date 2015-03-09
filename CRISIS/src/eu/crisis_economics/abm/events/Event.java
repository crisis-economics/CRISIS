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
package eu.crisis_economics.abm.events;

import com.google.common.eventbus.EventBus;

import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A token interface for {@link Simulation} events. {@link Event} objects are used 
  * in conjunction with the {@link Simulation} {@link EventBus} to dispatch
  * notifications throughout the codebase.
  * 
  * @author phillips
  */
public interface Event { }
