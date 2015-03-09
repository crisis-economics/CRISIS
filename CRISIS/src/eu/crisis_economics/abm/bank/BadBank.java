/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
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

package eu.crisis_economics.abm.bank;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.bank.strategies.clearing.ClearingBankStrategy;
import eu.crisis_economics.abm.bank.strategies.clearing.TrivialClearingStrategy;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;

/**
  * A {@link ClearingBank} with a "sell all" policy. This {@link Bank} will gradually
  * sell any shares it owns in other {@link StockReleaser}{@code s}. This {@link Bank}
  * will attempt to resell other assets, such as loan contracts, if possible. If it is
  * not possible to resell an asset, this {@link Bank} will wait for the asset to 
  * mature.
  * 
  * @author phillips
  */
public final class BadBank extends ClearingBank {
   @Inject
   public BadBank(
   @Named("BAD_BANK_INITIAL_CASH_ENDOWMENT")
      final double initialCash,
   @Named("BAD_BANK_CLEARING_MARKET_PARTICIPATION")
      final ClearingMarketParticipantFactory marketParticipationFactory,
      final ClearingHouse clearingHouse
      ) {
      super(
         initialCash,
         new ClearingBankStrategyFactory() {
            @Override
            public ClearingBankStrategy create(final ClearingBank bank) {
               return new TrivialClearingStrategy(bank);
               }
            },
         marketParticipationFactory,
         clearingHouse
         );
      
      clearingHouse.addStockMarketParticipant(this);
      UniqueStockExchange.Instance.removeStock(this);       // TODO: Troubled Bank hierarchy.
   }
}
