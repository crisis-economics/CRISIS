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
package eu.crisis_economics.abm.model.plumbing;

import com.google.inject.Injector;

/**
  * A plumbing algorithm used to generate an {@link AgentGroup} with all mutual
  * {@link Agent} and market relationships established.
  * 
  * @author phillips
  */
public interface Plumbing {
   /**
     * Populate this {@link AgentGroup} with {@link Agent} and market
     * structures. This object will be flushed and populated anew each
     * time this method is called.
     * 
     * @param injector
     *        The {@link Injector} to be used to construct {@link Agent} and
     *        market structures for this {@link AgentGroup}.
     * 
     * @return a populated and configured {@link AgentGroup}.
     */
   public BaseAgentGroup populate(final Injector injector);
}
