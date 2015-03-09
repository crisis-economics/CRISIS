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
package eu.crisis_economics.abm.firm;

import ai.aitia.crisis.aspectj.Agent;

import com.google.inject.Provider;

/**
  * A typedef interface for objects that provide sector names for 
  * {@link Firm}{@code s}. Implementations of this interface provide
  * on-demand sector names for new {@link Firm} {@link Agent}{@code s}.
  * 
  * @author phillips
  */
public interface FirmSectorProvider extends Provider<String> {
}
