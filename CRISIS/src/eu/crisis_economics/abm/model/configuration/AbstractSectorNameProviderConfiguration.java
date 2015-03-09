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

import java.util.Collections;

import com.google.inject.Key;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.io.SectorNameProvider;

/**
  * An abstract base class for sector and goods type specifications. Implementations
  * of this class provide {@link Collections} of sector names and goods type names.
  * 
  * @author phillips
  */
public class AbstractSectorNameProviderConfiguration extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = 681534210810826217L;
   
   /**
     * Create an {@link AbstractSectorNameProviderConfiguration}. Implementations of 
     * this class must expose the following bindings:
     * 
     * <ul>
     *   <li> One implementation of {@link SectorNameProvider}{@code .class}. The
     *        collection specifies all sector names for the running model. This
     *        provider should be bound in singleton scope. If this class has a
     *        nontrivial scope {@link #getScopeString()}, then the binding should
     *        be annotated with the scope string.
     * </ul>
     */
   protected AbstractSectorNameProviderConfiguration() { }
   
   @Override
   protected void setRequiredBindings() {
      if(getScopeString().isEmpty())
         requireBinding(SectorNameProvider.class);
      else
         requireBinding(Key.get(
            SectorNameProvider.class, Names.named(getScopeString())));
   }
   
   @Override
   protected void addBindings() {
      if(getScopeString().isEmpty())
         expose(SectorNameProvider.class);
      else
         expose(Key.get(
            SectorNameProvider.class, Names.named(getScopeString())));
   }
}
