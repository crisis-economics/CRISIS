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
  * Interface for simulated resources (Eg. goods) which perish, decay,
  * or react to the passage of time.
  * @author phillips
  */
public interface Perishable {
   /**
     * Process the effect of the passage of time. When this method is
     * called, the state of the object will be modified according to
     * the simulation time period since the last call. The first call
     * will be taken as t = 0.;
     */
   public void applyPassageOfTime();
}
