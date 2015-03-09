/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Olaf Bochmann
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

import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.markets.Buyer;

/**
 * @author  olaf
 */
public class LoanInstrument extends Instrument {

	private static final long serialVersionUID = 66529066385344159L;
	private final int maturity;

	/**
	 * Creates a loan investment instrument with a given maturity.
	 * @param maturity
	 */
	public LoanInstrument(final int maturity, final BlockingQueue<Order> updatedOrders) {
		super(generateTicker(maturity),updatedOrders);
		this.maturity = maturity;
	}
	
	public LoanInstrument(final int maturity, final BlockingQueue<Order> updatedOrders, final MatchingMode matchingMode, final Set<InstrumentListener> listeners) {
		super(generateTicker(maturity),updatedOrders, matchingMode, listeners);
		this.maturity = maturity;
	}

	/**
	 * @return  the maturity
	 */
	public int getMaturity() {
		return maturity;
	}

	/**
	 * generates a ticker symbol for loan with a given maturity
	 * @param maturity
	 */
	public static String generateTicker(final int maturity) {
		return "LOAN"+new Integer(maturity).toString();
	}

	@Override
	protected void setupContract(
	    final Order askOrder, final Order bidOrder, final double quantity, final double price) {
	    final double amount = ((Buyer)bidOrder.getParty()).disallocateCash(quantity);
		try {
			final Loan loan = LoanFactory.createFixedRateMortgage(
			   (Borrower)askOrder.getParty(),
			   (Lender)bidOrder.getParty(),
			   amount,
			   price,
			   this.getMaturity()
			   );
			notifyListenersOfNewTrade((Contract)loan); // TODO: fix
		} catch (final LenderInsufficientFundsException e) {
			return;
		}
	}
}
