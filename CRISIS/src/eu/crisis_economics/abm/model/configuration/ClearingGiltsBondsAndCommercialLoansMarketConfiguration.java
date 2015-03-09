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

import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingGiltsBondsAndCommercialLoansMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingLoanMarketFactory;

@ConfigurationComponent(
   DisplayName = "Gilts, Bonds and Graded Loans"
   )
public final class ClearingGiltsBondsAndCommercialLoansMarketConfiguration
   extends AbstractPrivateConfiguration implements ClearingLoanMarketConfiguration {
   
   private static final long serialVersionUID = 6570806221681977311L;
   
   @Override
   protected void addBindings() {
      install(new FactoryModuleBuilder()
         .implement(
            ClearingLoanMarket.class, ClearingGiltsBondsAndCommercialLoansMarket.class)
         .build(ClearingLoanMarketFactory.class)
         );
      expose(ClearingLoanMarketFactory.class);
   }
}
