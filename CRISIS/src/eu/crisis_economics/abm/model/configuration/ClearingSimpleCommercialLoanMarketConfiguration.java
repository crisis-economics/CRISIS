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

import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingSimpleCommercialLoanMarket;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingLoanMarketFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Commercial Loans Only"
   )
public final class ClearingSimpleCommercialLoanMarketConfiguration
   extends AbstractPrivateConfiguration implements ClearingLoanMarketConfiguration {
   
   private static final long serialVersionUID = -2541103635345392952L;
   
   @Parameter(
      ID = "CLEARING_SIMPLE_COMMERCIAL_LOAN_MARKET_USE_HETEROGENEOUS_LOANS"
      )
   private boolean
      isHeterogeneousMarket = true; 
   
   public boolean isHeterogeneousMarket() {
      return isHeterogeneousMarket;
   }
   
   public void setHeterogeneousMarket(
      final boolean isHeterogeneousMarket) {
      this.isHeterogeneousMarket = isHeterogeneousMarket;
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "CLEARING_SIMPLE_COMMERCIAL_LOAN_MARKET_USE_HETEROGENEOUS_LOANS",
         isHeterogeneousMarket));
      install(new FactoryModuleBuilder()
         .implement(ClearingLoanMarket.class, ClearingSimpleCommercialLoanMarket.class)
         .build(ClearingLoanMarketFactory.class)
         );
      expose(ClearingLoanMarketFactory.class);
   }
}
