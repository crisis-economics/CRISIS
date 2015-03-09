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
package eu.crisis_economics.abm.model.configuration;

import java.io.Serializable;

import com.google.inject.Module;

import eu.crisis_economics.abm.model.Scoped;

public interface ComponentConfiguration extends Module, Scoped, InjectorFactory, Serializable {
   /**
     * Check the validity of parameters for this {@link ComponentConfiguration}.<br><br>
     * 
     * On success, the state of this object is unchanged. On failure,
     * {@link IllegalArgumentException} is raised.<br><br>
     * 
     * Calling this method will, in addition, call {@link #checkParameterValidity()} for
     * each child {@link ComponentConfiguration} belonging to this object.
     */
   public void checkParameterValidity();
}
