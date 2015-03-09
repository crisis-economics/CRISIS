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

import eu.crisis_economics.abm.firm.bankruptcy.FirmBankruptcyHandler;

/**
  * An abstract configuration base class for {@link Firm} bankruptcy handler algorithms.
  * 
  * @author phillips
  */
public abstract class AbstractFirmBankruptcyHandlerConfiguration
   extends AbstractPrivateConfiguration
   implements FirmBankruptcyHandlerConfiguration {
   
   private static final long serialVersionUID = -3492410201634409891L;
   
   /**
     * Create a {@link AbstractFirmBankruptcyHandlerConfiguration} object.<br><br>
     * Implementations of this class should provide the following bindings:
     * 
     * <ul>
     *   <li> An implementation of {@link FirmBankruptcyHandler}{@code .class}. If
     *        it is the case that {@link #getScopeString()} is nonempty, this binding
     *        should be annotated with {@link #getScopeString()}. Otherwise this binding
     *        should carry no annotation.
     * </ul>
     */
   protected AbstractFirmBankruptcyHandlerConfiguration() { }
   
   @Override
   protected void setRequiredBindings() {
      if(!getScopeString().isEmpty())
         requireBinding(Key.get(FirmBankruptcyHandler.class, Names.named(getScopeString())));
      else
         requireBinding(Key.get(FirmBankruptcyHandler.class));
   }
   
   /**
     * When overriding this method, call {@link super#addBindings()}
     * as a last instruction.
     */
   @Override
   protected void addBindings() {
      if(!getScopeString().isEmpty())
         expose(Key.get(FirmBankruptcyHandler.class, Names.named(getScopeString())));
      else
         expose(Key.get(FirmBankruptcyHandler.class));
   }
}
