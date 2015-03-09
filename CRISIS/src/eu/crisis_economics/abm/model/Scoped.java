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
package eu.crisis_economics.abm.model;

import java.io.Serializable;

public interface Scoped extends Serializable {
   /**
     * Get the scope {@link String} for this object. This method cannot
     * be named {@code getScope()}. Otherwise, the model GUIs will
     * interpret this method as part of a mutator pair for a field named
     * {@code scope}.
     */
   public String getScopeString();
   
   /**
     * Set the scope {@link String} for this object. If the argument
     * is {@code null}, an empty {@link String} is assumed.
     */
   public void setScope(String scope);
   
   /**
     * Set the scope {@link String} for this object. If the argument
     * is {@code null}, an empty {@link String} is assumed. If the second
     * argument is not {@code null}, then {@code parent.getScope() + "_"}
     * is prefixed to the first argument.
     */
   public void setScope(String scope, Scoped parent);
}
