/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.bank.strategies;

import java.util.List;

import cern.jet.random.AbstractDistribution;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.DepositMarket;
import eu.crisis_economics.abm.markets.nonclearing.DepositOrder;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;
import eu.crisis_economics.abm.markets.nonclearing.DepositInstrument.AccountType;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author Tamás Máhr
 */
public class RandomBankStrategy
		extends EmptyBankStrategy {

	private static final double DEPOSIT_ORDER_SIZE = -100000000000000.0;

	private static final int COMMERCIAL_LOAN_MATURITY = 1;

	private static final int INTERBANK_LOAN_MATURITY = 1;

	/**
	 * The random distribution the submitted order sizes are drawn.
	 */
	protected AbstractDistribution orderSizeDistribution;
	
	/**
	 * The random distribution the submitted order prices are drawn.
	 */
	protected AbstractDistribution orderPriceDistribution;
	
	/**
	 * The random distribution used to select the stock for which an order is
	 * submitted to the stock market.
	 */
	protected AbstractDistribution stockInstrumentDistribution;

	// TODO These contains only the LAST values! If they submit to multiple markets, it is broken
	private double depositRate;

	private double lendingRate;

	private double interLendingRate;

	private double loanSize;

	public RandomBankStrategy(
	   final StrategyBank bank,
	   final AbstractDistribution orderSizeDistribution,
	   final AbstractDistribution orderPriceDistribution,
		final AbstractDistribution stockInstrumentDistribution
		) {
	   super(bank);
		this.orderSizeDistribution = orderSizeDistribution;
		this.orderPriceDistribution = orderPriceDistribution;
		this.stockInstrumentDistribution = stockInstrumentDistribution;
	}

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.bank.StrategyBankInterface#considerCommercialLoanMarkets()
	 */
	@Override
	public void considerCommercialLoanMarkets() {		
		// first remove the current commercial orders, note that the cancel
		// method removes the order both from the market and this bank. 
		for (final Order order : getBank().getOrders()) {
			if (order instanceof CommercialLoanOrder){
				((CommercialLoanOrder)order).cancel();
			}
		}
		
		this.loanSize = Math.log(Math.abs(orderSizeDistribution.nextDouble())+1)*100; // log-normal
		
		//if (bank.getMarkets().containsKey( CommercialLoanMarket.class )) {
			for (final Market market : getBank().getMarketsOfType(CommercialLoanMarket.class)) {
				try {
					this.lendingRate = orderPriceDistribution.nextDouble();
					((CommercialLoanMarket) market).addOrder(getBank(),
							COMMERCIAL_LOAN_MATURITY,
							this.loanSize,		//sell order
							this.lendingRate);
				} catch (final OrderException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				} catch (AllocationException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				}
			}
		//}
	}
	
	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.bank.StrategyBankInterface#considerDepositMarkets()
	 */
	@Override
	public void considerDepositMarkets() {
		// first remove the current deposit orders, note that the cancel
		// method removes the order both from the market and this bank. 
		for (final Order order : getBank().getOrders()) {
			if (order instanceof DepositOrder) {
				final DepositOrder depositOrder = (DepositOrder) order;
				depositOrder.cancel();
			}
		}
		
		//if (bank.getMarkets().containsKey( DepositMarket.class )) {
			for (final Market market : getBank().getMarketsOfType(DepositMarket.class)) {
				try {
					this.depositRate = orderPriceDistribution.nextDouble();
					((DepositMarket)market).addOrder(getBank(), AccountType.DEBIT, DEPOSIT_ORDER_SIZE, this.depositRate);
				} catch (final OrderException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				}
			}
		//}
	}
	
	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.bank.StrategyBankInterface#considerStockMarkets()
	 */
	@Override
	public void considerStockMarkets() {
		// first remove the current stock orders, note that the cancel
		// method removes the order both from the market and this bank. 
		for (final Order order : getBank().getOrders()) {
			if (order instanceof StockOrder){
				((StockOrder)order).cancel();
			}
		}
		
		//if (bank.getMarkets().containsKey( StockMarket.class )) {
			for (final Market market : getBank().getMarketsOfType(StockMarket.class)) {
				final StockMarket stockMarket = (StockMarket)market;
				final List<String> stockNames = stockMarket.getInstrumentNames();
				if (! stockNames.isEmpty()){
					try {
						stockMarket.addOrder(getBank(),
								stockNames.get( stockInstrumentDistribution.nextInt() % stockNames.size() ),
								-1 * Math.abs(orderSizeDistribution.nextDouble()),
								orderPriceDistribution.nextDouble());
					} catch (AllocationException e) {
						e.printStackTrace();
						Simulation.getSimState().finish();
					} catch (final OrderException e) {
						e.printStackTrace();
						Simulation.getSimState().finish();
					}
				}
			}
		//}
	}	
	
	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.bank.StrategyBankInterface#considerInterbankMarkets()
	 */
	@Override
	public void considerInterbankMarkets() {
		// first remove the current interbank orders, note that the cancel
		// method removes the order both from the market and this bank. 
		for (final Order order : getBank().getOrders()) {
			if (order instanceof InterbankLoanOrder) {
				final InterbankLoanOrder interbankLoanOrder = (InterbankLoanOrder) order;
				interbankLoanOrder.cancel();
			}
		}
		
		//if (bank.getMarkets().containsKey( InterbankLoanMarket.class )) {
			for (final Market market : getBank().getMarketsOfType(InterbankLoanMarket.class)) {
				try {
					this.interLendingRate = orderPriceDistribution.nextDouble();
					((InterbankLoanMarket)market).addOrder(getBank(), INTERBANK_LOAN_MATURITY, -1 * orderSizeDistribution.nextDouble(), this.interLendingRate);
				} catch (final OrderException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				} catch (AllocationException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				}
			}
		//}
	}

	
	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.bank.StrategyBankInterface#getDepositRate()
	 */
	@Override
	public double getDepositRate() {return this.depositRate;} //TODO consider using market value

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.bank.StrategyBankInterface#getLendingRate()
	 */
	@Override
	public double getLendingRate() {return this.lendingRate;} //TODO consider using market value

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.bank.StrategyBankInterface#getInterLendingRate()
	 */
	@Override
	public double getInterLendingRate() {return this.interLendingRate;} //TODO consider using market value

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.bank.StrategyBankInterface#getLoanSize()
	 */
	@Override
	public double getLoanSize() {
		return loanSize;
	}
	
}
