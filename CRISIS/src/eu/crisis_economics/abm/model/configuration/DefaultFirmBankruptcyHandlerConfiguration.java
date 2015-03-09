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

import eu.crisis_economics.abm.firm.bankruptcy.DefaultFirmBankruptcyHandler;
import eu.crisis_economics.abm.firm.bankruptcy.FirmBankruptcyHandler;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A {@link ComponentConfiguration} for the {@link DefaultFirmBankruptcyHandler}
  * class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Repay Debt or Liquidation"
   )
public final class DefaultFirmBankruptcyHandlerConfiguration
   extends AbstractFirmBankruptcyHandlerConfiguration {
   
   private static final long serialVersionUID = -8226313089944636482L;
   
   @Override
   protected void addBindings() {
      if(!getScopeString().isEmpty())
         bind(FirmBankruptcyHandler.class)
            .annotatedWith(Names.named(getScopeString()))
            .to(DefaultFirmBankruptcyHandler.class);
      else
         bind(FirmBankruptcyHandler.class)
            .to(DefaultFirmBankruptcyHandler.class);
      super.addBindings();
   }
}
