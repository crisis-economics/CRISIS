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

import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.fund.TrivialFund;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.simulation.injection.factories.FundFactory;

/**
  * An implementation of the {@link AbstractFundSubEconomy} class.<br><br>
  * 
  * This {@link AbstractFundSubEconomy} is interchangeable with other instances.
  * This implementation specifies a subeconomy of {@link Fund}{@code s} consisting
  * of a custom number of {@link TrivialFund} {@link Agent}{@code s}.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   Description = "A simple subeconomy providing trivial funds with no market interactions. "
               + "This module specifies a group of minimally functional fund agents which "
               + "very little business other than to recieve client investments and process "
               + "investment account withdrawals. Fund agents in this module will make no "
               + "investment decisions, and should any fund receive stocks or other illiquid "
               + "assets, no action will be taken to resell or modify these assets by the funds "
               + "themselves. In particular, shares owned by funds will not be resold or offered "
               + "to the markets."
   )
public final class TrivialFundSubEconomy extends AbstractFundSubEconomy {
   
   private static final long serialVersionUID = 1085201560234923800L;
   
   @Override
   protected void addBindings() {
      install(new FactoryModuleBuilder()
         .implement(Fund.class, TrivialFund.class)
         .build(FundFactory.class)
         );
      expose(FundFactory.class);
   }
   
}
