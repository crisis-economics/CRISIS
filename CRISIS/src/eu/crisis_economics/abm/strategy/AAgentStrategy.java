/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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
package eu.crisis_economics.abm.strategy;

import eu.crisis_economics.abm.markets.nonclearing.Order;

public abstract class AAgentStrategy
		implements IAgentStrategy {
	
	@Override
	public void considerDepositMarkets(final StrategyAgent agent) {
	}
	
	@Override
	public void considerCommercialLoanMarkets(final StrategyAgent agent) {
	}
	
	@Override
	public void considerStockMarkets(final StrategyAgent agent) {
	}
	
	@Override
	public void considerInterbankMarkets(final StrategyAgent agent) {
	}
	
	@Override
	public double getDepositRate(final StrategyAgent agent) {
		return 0;
	}
	
	@Override
	public double getLendingRate(final StrategyAgent agent) {
		return 0;
	}
	
	@Override
	public double getInterLendingRate(final StrategyAgent agent) {
		return 0;
	}
	
	@Override
	public double getLoanSize(final StrategyAgent agent) {
		return 0;
	}
	
	@Override
	public void newOrder(final Order o) {
	}
	
	@Override
	public double getCommercialLoanOrderSize() {
		return 0;
	}
}
