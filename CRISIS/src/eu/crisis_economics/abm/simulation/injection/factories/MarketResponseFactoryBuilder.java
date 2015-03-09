/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.simulation.injection.factories;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;

public interface MarketResponseFactoryBuilder {
   public interface BCLResponseFunctionFactoryBuilder
      extends MarketResponseFactoryBuilder {
   }
   
   public interface LoanSupplierMarketResponseFunctionFactoryBuilder
      extends MarketResponseFactoryBuilder {
   }
   
   public interface LoanConsumerMarketResponseFunctionFactoryBuilder
      extends MarketResponseFactoryBuilder {
   }
   
   public interface TargetValueStockMarketResponseFunctionFactoryBuilder
      extends MarketResponseFactoryBuilder {
   }
   
   public interface GraduallySellSharesMarketResponseFunctionFactoryBuilder
      extends MarketResponseFactoryBuilder {
   }
   
   public AgentOperation<MarketResponseFunction> create(ClearingMarket marketInfo);
}
