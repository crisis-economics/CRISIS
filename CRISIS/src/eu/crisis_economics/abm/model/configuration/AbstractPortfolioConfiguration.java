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

import com.google.inject.Key;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;

/**
  * A base class for {@link Portfolio} object configuration components.
  * 
  * @author phillips
  */
public abstract class AbstractPortfolioConfiguration extends AbstractPrivateConfiguration {
   
   /**
     * Create a {@link AbstractPortfolioConfiguration} object.<br><br>
     * 
     * Implementations of this class should provide the following bindings:
     * 
     * <ul>
     *   <li> An implementation of {@link Portfolio}{@code .class}, annotated with the
     *        scope string, if it is present.
     * </ul>
     */
   protected AbstractPortfolioConfiguration() { }
   
   @Override
   protected void setRequiredBindings() {
      requireBinding(Key.get(Portfolio.class, Names.named(getScopeString())));
   }
   
   /**
     * When overriding this method, call {@link super#addBindings()} as a last instruction.
     */
   @Override
   protected void addBindings() {
      expose(Key.get(Portfolio.class, Names.named(getScopeString())));
   }
   
   private static final long serialVersionUID = -2396536148492610132L; 
}
