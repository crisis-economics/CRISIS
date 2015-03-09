/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
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
package eu.crisis_economics.abm.bank.strategies;

import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;

/**
  * sells all assets
  * 
  * @author bochmann
  */
public class FiresaleStrategy extends EmptyBankStrategy {
   
   public FiresaleStrategy(final StrategyBank bank) {
      super(bank);
   }
   
   @Override
   public void considerStockMarkets() {
      StockMarket market = getBank().getMarketsOfType(StockMarket.class).iterator().next();
      for(final Contract contract : getBank().getAssets()) {
         if(contract instanceof StockAccount) {
            final StockAccount
               stockAccount = (StockAccount) contract;
            double quantity = stockAccount.getQuantity();
            String name = stockAccount.getInstrumentName();
            try {
               market.addOrder(getBank(), name, quantity, 0.);
            } catch (AllocationException e) {
               return; // Abandon
            } catch (OrderException e) {
               return; // Abandon
            }
         }
      }
   }
}
