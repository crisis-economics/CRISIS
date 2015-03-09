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

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.firm.io.SectorNameProvider;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A simple configuration component for {@link Household} goods consumers.
  * This implementation signals that {@link Household} {@link Agent}{@code s} 
  * should consider every type of goods sold on the market during consumption.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Buy From All Sectors"
   )
public final class BuyFromAllSectorsHouseholdConsumptionConfiguration extends
   AbstractSectorNameProviderConfiguration
   implements HouseholdSectorsToBuyFromConfiguration {
   
   private static final long serialVersionUID = 4598778183840570606L;
   
   @Override
   protected void addBindings() {
      if(getScopeString().isEmpty())
         bind(SectorNameProvider.class)
            .to(SectorNameProvider.class);
      else
         bind(SectorNameProvider.class)
            .annotatedWith(Names.named(getScopeString()))
            .to(SectorNameProvider.class);
      super.addBindings();
   }
}
