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

import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.FirmSectorProvider;

public class AbstractFirmSectorProviderConfiguration extends AbstractPrivateConfiguration {
    private static final long serialVersionUID = 7925335620887183206L;
    
   /**
      * Create an {@link AbstractFirmSectorProviderConfiguration}
      * object. Implementations of this type should provide the following
      * bindings:
      * 
      * <ul>
      *   <li> An implementation of a {@link FirmSectorProvider}, bound in 
      *        singleton scope.
      * </ul>
      */
   protected AbstractFirmSectorProviderConfiguration() { }
   
   @Override
   protected void setRequiredBindings() {
      requireBinding(FirmSectorProvider.class);
   }
   
   /**
     * When overriding this method, call {@link super#addBindings()} as a last
     * instruction.
     */
   @Override
   protected void addBindings() {
      bind(String.class)
         .annotatedWith(Names.named("FIRM_SECTOR_NAME"))
         .toProvider(FirmSectorProvider.class);
      expose(String.class)
         .annotatedWith(Names.named("FIRM_SECTOR_NAME"));
   }
}
