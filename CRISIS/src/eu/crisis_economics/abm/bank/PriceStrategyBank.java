/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
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
package eu.crisis_economics.abm.bank;

import cern.jet.random.AbstractDistribution;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.BankStrategy;
import eu.crisis_economics.abm.bank.strategies.PriceStrategy;
import eu.crisis_economics.abm.simulation.injection.factories.BankStrategyFactory;

/**
  * @author aymanns
  */
public class PriceStrategyBank extends StrategyBank {
   
   public PriceStrategyBank(
      final double initialCash,
      final AbstractDistribution orderSizeDistribution,
      final AbstractDistribution stockInstrumentDistribution,
      final int type
      ) {
      super(
         initialCash, 
         new BankStrategyFactory() {
            @Override
            public BankStrategy create(StrategyBank bank) {
               return new PriceStrategy(
                 bank, orderSizeDistribution, stockInstrumentDistribution, type);
            }
         }
      );
   }
}
