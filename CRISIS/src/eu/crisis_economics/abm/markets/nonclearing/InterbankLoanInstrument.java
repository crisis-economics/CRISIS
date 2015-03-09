/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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
package eu.crisis_economics.abm.markets.nonclearing;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InterbankBorrower;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.markets.Buyer;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.Order.Side;

/**
 * This instrument makes sure that risk-adjusted rates are used on the limit-order book,
 * but the actual loan is generated using nominal rates.
 * 
 * @author Victor
 *
 */
public class InterbankLoanInstrument extends LoanInstrument {
	
	private static final long serialVersionUID = 3448119885321084608L;

	public InterbankLoanInstrument(int maturity, BlockingQueue<Order> updatedOrders) {
		super(maturity, updatedOrders);
	}

	
	public InterbankLoanInstrument(int maturity, BlockingQueue<Order> updatedOrders, MatchingMode matchingMode,
			Set<InstrumentListener> listeners) {
		super(maturity, updatedOrders, matchingMode, listeners);
	}
	
	/**
	 * Calculate risk-adjusted rates and submit them to the limit order book engine,
	 * while storing the nominal rates for setting up contract	 * 
	 * @param order	a valid order object
	 * @return void
	 */
	@Override
	protected void processNewOrder(final Order order){
		if (order.getSide().equals(Side.BUY)) {
			//risk-adjust borrowing rate if we are not a central bank
			Party party = order.getParty();
			//for central bank, getRiskAdjustedPrice just returns order.getPrice()
			double riskAdjustedPrice = getRiskAdjustedPrice(party, order.getPrice());
			order.setPrice(riskAdjustedPrice);
		}
		try {
		System.out.println("INTERBANK process new order: SIDE: " + order.getSide() + 
				", party: " + order.getParty().getUniqueName() + 
				", price: " + order.getPrice() + 
				", size: " + order.getSize());
		}
		catch(NumberFormatException e) {
		   System.out.println();
		}
		super.processNewOrder(order);
	}
	
	/**
	 * Match all Orders (for sync mode)
	 * @return void
	 * @throws InstrumentException 
	 */
	@Deprecated
	protected void matchOrders() throws InstrumentException{
		super.matchOrdersInterbank();
	}
	
	/**
	 * Calculate nominal rates and set up the loan
	 * @param order	a valid order object
	 * @return void
	 */
	@Override
	protected void setupContract(final Order bidOrder, final Order askOrder, final double quantity,
			final double price) {
		
		//System.out.println("Setting up contract between LENDER: " + askOrder.getParty().getUniquePartyOrderIdentitifer() + " and BORROWER: " + bidOrder.getParty().getUniquePartyOrderIdentitifer() + " for QUANTITY: " + quantity + " at nominalPrice: " + price);
		
		Party borrowingParty = bidOrder.getParty();
		Party lendingParty = askOrder.getParty();
		double nominalPrice;
		if (lendingParty instanceof CentralBank)
			nominalPrice = price; //if central bank is lending, we already have correct price
		else
			nominalPrice = getNominalPrice(borrowingParty, price); //otherwise, calculate nominal price from risk-adjusted
		//super.setupContract(bidOrder, askOrder, quantity, nominalPrice);
		
		try {
			((Buyer)lendingParty).disallocateCash(quantity);
			final Contract contract;
			if (lendingParty instanceof CentralBank) //Loan from CB
			{
				//TODO: this may need to be a collateralized loan
				contract = (Contract)LoanFactory.createInterbankLoan(
						(Bank)borrowingParty, (Bank)lendingParty, quantity, nominalPrice, this.getMaturity(),
						NamedEventOrderings.INTERBANK_CB_LOAN_PAYMENTS, ScheduleIntervals.ONE_DAY);
				//loan = new InterbankCBLoan((Borrower)bidOrder.getParty(), (Lender)askOrder.getParty(), quantity, nominalPrice, this.getMaturity());
			} else if (borrowingParty instanceof CentralBank) //deposit with CB
			{				
				contract = (Contract)LoanFactory.createInterbankLoan(
						(Bank)borrowingParty, (Bank)lendingParty, quantity, nominalPrice, this.getMaturity(),
						NamedEventOrderings.INTERBANK_CB_DEPOSIT_PAYMENTS, ScheduleIntervals.ONE_DAY);
				//loan = new InterbankCBDeposit((Borrower)bidOrder.getParty(), (Lender)askOrder.getParty(), quantity, nominalPrice, this.getMaturity());
			} else //normal interbank loan between two commercial banks
			{
				contract = (Contract)LoanFactory.createInterbankLoan(
						(Bank)borrowingParty, (Bank)lendingParty, quantity, nominalPrice, this.getMaturity(),
						NamedEventOrderings.INTERBANK_LOAN_PAYMENTS, ScheduleIntervals.ONE_DAY);
				//loan = new InterbankLoan((Borrower)bidOrder.getParty(), (Lender)askOrder.getParty(), quantity, nominalPrice, this.getMaturity());
			}
			super.notifyListenersOfNewTrade(contract);
		} catch (final LenderInsufficientFundsException e) {
			e.printStackTrace();
		}
		
		System.out.println("Setting up contract between LENDER: " + askOrder.getParty().getUniqueName() + " and BORROWER: " + bidOrder.getParty().getUniqueName() + " for QUANTITY: " + quantity + " at nominalPrice: " + nominalPrice + "(riskAdjusted price was: " + price + ")");
	}
	
	/**
	 * Request risk-adjusted rate from the bank
	 * @param party a borrowing bank
	 * @param nominalPrice nominal borrowing rate
	 * @return risk adjusted rate
	 */
	protected double getRiskAdjustedPrice(Party party, double nominalPrice) {
		if (party instanceof CentralBank)
			//for central bank, there is no risk so nominalprice = riskadjustedprice
			return nominalPrice;
		else	
			return ((InterbankBorrower)party).getRiskAdjustedBorrowingRate(nominalPrice);
	}
	
	/**
	 * Request nominal rate from the bank
	 * @param party a borrowing bank
	 * @param riskAdjustedPrice risk-adjusted borrowing rate
	 * @return nominal rate
	 */
	protected double getNominalPrice(Party party, double riskAdjustedPrice) {
		if (party instanceof CentralBank)
			//for central bank, there is no risk so nominalprice = riskadjustedprice
			return riskAdjustedPrice;
		else
			return ((InterbankBorrower)party).getNominalBorrowingRate(riskAdjustedPrice);
	}

}
