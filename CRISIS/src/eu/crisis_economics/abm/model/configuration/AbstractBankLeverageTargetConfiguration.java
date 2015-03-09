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

import eu.crisis_economics.abm.strategy.leverage.LeverageTargetAlgorithm;

/**
  * An abstract base class for {@link LeverageTargetAlgorithm} configuration components.<br><br>
  * Implementations of this {@link ComponentConfiguration} must provide bindings for
  * the following types:
  * 
  * <ul>
  *   <li> {@link LeverageTargetAlgorithm}{@code .class} annotated with 
  *        {@link #getScopeString()}.
  * </ul>
  * 
  * @author phillips
  */
public abstract class AbstractBankLeverageTargetConfiguration
   extends AbstractPrivateConfiguration implements BankLeverageTargetConfiguration {
   
   private static final long serialVersionUID = 7943532337596710185L;
   
   /**
     * Create a {@link AbstractBankLeverageTargetConfiguration} object.<br><br>
     * 
     * Implementations of this type must expose the following bindings:
     * <ul>
     *   <li> {@link LeverageTargetAlgorithm}{@code .class} annotated with 
     *        {@link #getScopeString()}.
     * </ul>
     */
   protected AbstractBankLeverageTargetConfiguration() { }
   
   @Override
   protected final void setRequiredBindings() {
      requireBinding(Key.get(LeverageTargetAlgorithm.class, Names.named(getScopeString())));
      expose(Key.get(LeverageTargetAlgorithm.class, Names.named(getScopeString())));
   }
}
