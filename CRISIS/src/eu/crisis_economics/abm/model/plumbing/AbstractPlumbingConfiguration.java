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

import eu.crisis_economics.abm.model.configuration.AbstractPrivateConfiguration;
import eu.crisis_economics.abm.model.configuration.ComponentConfiguration;

/**
  * A {@link ComponentConfiguration} for {@link Plumbing} classes.
  * 
  * Implementations of this type must expose the following bindings:
  * <ul>
  *   <li> One public implementation of {@link Plumbing}{@code .class}.
  *        This binding will be treated as an eager singleton.
  * </ul>
  * 
  * @author phillips
  */
public abstract class AbstractPlumbingConfiguration extends AbstractPrivateConfiguration {
   private static final long serialVersionUID = 41978700951825516L;
   
   /**
     * Create a {@link AbstractPlumbingConfiguration} object.<br><br>
     * 
     * Implementations of this type must expose the following bindings:
     * <ul>
     *   <li> One public implementation of {@link Plumbing}{@code .class}.
     *        This binding will be treated as an eager singleton.
     * </ul>
     */
   protected AbstractPlumbingConfiguration() { }
   
   @Override
   protected final void setRequiredBindings() {
      requireBinding(Plumbing.class);
      expose(Plumbing.class);
   }
}
