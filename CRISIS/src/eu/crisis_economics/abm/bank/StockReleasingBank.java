/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 Milan Lovric
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
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
package eu.crisis_economics.abm.bank;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.events.BankDividendPaymentMadeEvent;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * A bank that can release shares.
 * @author olaf
 *
 */
public class StockReleasingBank extends Bank implements StockReleaser {
	protected final Map<StockHolder, StockAccount> stockAccounts = 
			new LinkedHashMap<StockHolder, StockAccount>();

	/*
	 * To be adjusted if you want to change the initial stock price, also
	 * needs to be changed in priceStrategy.
	 */
	protected static final double INITIAL_EMISSION_PRICE = 1000; // 2.5e7;//1.0e9;//3e8;//2e5;//1.0;

	protected double
      dividendPerShare;

	public StockReleasingBank(final double initialCash) {
		super(initialCash);

		final double pricePerShare = this.getCashReserveValue();
		UniqueStockExchange.Instance.addStock(this, pricePerShare);

		Simulation.once(this, "step",
		   CustomSimulationCycleOrdering.create(NamedEventOrderings.BANK_SHARE_PAYMENTS, 3));
	}

	@SuppressWarnings("unused") // Scheduled
	private void step() {
	   if(liquidateAtAfterAll) {
	      dividendPerShare = 0.;
	   }
	   else {
		// Liquidity check - note: this may not be needed for heterogeneous shareholder
		if (dividendPerShare * getNumberOfEmittedShares() > getCashReserveValue()) {
			double reducedDividendPerShare = Math.max(
					Math.min(dividendPerShare, .9 * getCashReserveValue() / getNumberOfEmittedShares()), 0);
			System.err.println(getUniqueName() +
					" intended dividend payment may result in liquidity crisis. cut dividends per " + 
					"share from " + dividendPerShare + " to " + reducedDividendPerShare);
			dividendPerShare = reducedDividendPerShare;
		}
		// equity check
		if (dividendPerShare > 0.0 && getEquity() - (dividendPerShare * getNumberOfEmittedShares()) < 0) {
			double reducedDividendPerShare = Math.max(
					Math.min(dividendPerShare, .9 * getEquity() / getNumberOfEmittedShares()), 0);
			System.err.println(getUniqueName() +
					" intended dividend payment would result in negative equity. cut dividends " + 
					"per share from " + dividendPerShare + " to " + reducedDividendPerShare);
			dividendPerShare = reducedDividendPerShare;
		}
		for (final StockAccount stockAccount : stockAccounts.values()) {
			try {
            final double
               amountPaid = payDividends(stockAccount);
            Simulation.events().post(
               new BankDividendPaymentMadeEvent(getUniqueName(), amountPaid));
			} catch (final InsufficientFundsException e) {
				e.printStackTrace();
				System.out.print("insufficient funds ( ");
				System.out.print(stockAccount.getReleaser().computeDividendFor(stockAccount) +
						" ) for stock account ");
				System.out.print(stockAccount.toString() + " of Bank ");
				System.out.print(stockAccount.getReleaser().toString() + " in time step ");
				System.out.println(Simulation.getSimState().schedule.getSteps());
				handleBankruptcy();
				return; // Don't reschedule
			}
		}
		if (getNumberOfEmittedShares() == 0 && getDividendPerShare() > 0.0)
			System.err.println(getUniqueName()
					+ " has no sharehoder to pay dividends to.");
	   }
		Simulation
		.once(this, "step",
		    CustomSimulationCycleOrdering.create(
		       NamedEventOrderings.BANK_SHARE_PAYMENTS, 3));
	}

	protected double payDividends(final StockAccount stockAccount) throws InsufficientFundsException{
		final double dividend = computeDividendFor( stockAccount );
		//System.out.println("stock releasing Bank dividend " +  dividend);
		stockAccount.makeDividentPayment(dividend);
		//System.out.println(getUniqueName() + " payed a dividend of " + dividend + " is payed to " + stockAccount.getHolder());
		return dividend;
	}

	/**
	 * This sets the amount you want the bank the pay dividends. 
	 * You can set it any time, but it takes effect next time the bank schedules the transfer 
	 * at BANK_SHARE_PAYMENTS.getStepInterval() and BANK_SHARE_PAYMENTS.ordering(). 
	 * Currently, it is at the end of each time step. 
	 * Since this bank is not using StockAccounts, totalDividend is equal dividendPerShare. 
	 * However, you should use setDividendPerShare(totalDividend/getNumberOfEmittedShares()) 
	 * to be compatible once we'll enable shares for banks.
	 * @param dividendPerShare
	 */
	@Override
	public void setDividendPerShare(final double dividendPerShare) {
		this.dividendPerShare = Math.max(dividendPerShare, 0.0);
	}

	/**
	 * This returns the current amount the bank will pay dividends. 
	 * The transfer is scheduled by the bank 
	 * at BANK_SHARE_PAYMENTS.getStepInterval() and BANK_SHARE_PAYMENTS.ordering(). 
	 * Currently, it is at the end of each time step. 
	 * @return dividendPerShare
	 */
	public double getDividendPerShare() {
		return dividendPerShare;
	}

	/**
	 * Returns the number of emitted shares.
	 */
	public double getNumberOfEmittedShares() {
		return UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(this);
	}

	@Override
	public void afterAll() {
		super.afterAll();
	}
	
	@Override
	public void addStockAccount(StockAccount stockAccount) {
		stockAccounts.put(stockAccount.getHolder(), stockAccount);

	}
	@Override
	public boolean removeStockAccount(StockAccount stockAccount) {
		return stockAccounts.remove(stockAccount.getHolder()) != null;
	}
	
	@Override
	public double computeDividendFor(StockAccount stockAccount) {
		return stockAccount.getQuantity()*this.dividendPerShare;
	}

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#releaseShares(int, double, eu.crisis_economics.abm.contracts.StockHolder)
	 */
	@Override
	public void releaseShares(
			final double numberOfShares,
			final double pricePerShare,
			final StockHolder stockHolder
			) throws InsufficientFundsException {
		UniqueStockExchange.Instance.generateSharesFor(
		   this.getUniqueName(), stockHolder, numberOfShares, pricePerShare);
	}

   public void setEmissionPrice(
      final double emissionPrice) {
      UniqueStockExchange.Instance.setStockPrice(getUniqueName(), emissionPrice);
   }
   
   public double getMarketValue() {
      if(UniqueStockExchange.Instance.hasStock(this))
         return UniqueStockExchange.Instance.getStockPrice(this);
      return 0.;
   }

	@Override
	public void registerOutgoingDividendPayment(
			double dividendPayment,
			double pricePerShare, 
			double numberOfSharesHeldByInvestor
			) { }
}