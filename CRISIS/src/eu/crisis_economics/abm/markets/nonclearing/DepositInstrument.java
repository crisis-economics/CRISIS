/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.markets.nonclearing;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;

/**
 * @author Tamás Máhr
 */
public class DepositInstrument extends Instrument {

	private static final long serialVersionUID = -6098381600876404436L;

	public static enum AccountType {
		DEBIT
	}
	
	public DepositInstrument(final BlockingQueue<Order> updateOrders, final AccountType type) {
		super(generateTicker(type), updateOrders);
		
	}

	public DepositInstrument(final BlockingQueue<Order> updateOrders, final AccountType type, final MatchingMode matchingMode, final Set<InstrumentListener> listeners) {
		super(generateTicker(type), updateOrders, matchingMode, listeners);		
	}

	
	
	public static String generateTicker(final AccountType type){
		return "DEPOSIT_" + type.name();
	}
	
	@Override
	protected void setupContract(final Order buyOrder, final Order sellOrder, final double quantity,
			final double price) {
		final DepositAccount depositAccount = new DepositAccount((DepositHolder) buyOrder.getParty(), (Depositor)sellOrder.getParty(), quantity, price);
		notifyListenersOfNewTrade(depositAccount);
	}
}
