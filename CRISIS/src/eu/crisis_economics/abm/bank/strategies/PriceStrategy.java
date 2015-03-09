/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Fabio Caccioli
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cern.jet.random.AbstractDistribution;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.LoanInstrument;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author aymanns
 * This class basically only holds one interesting function: getPrice. This function is called by all bank classes inheriting from this class. The rest of 
 * the code in this class only served for testing purposes.
 * getPrice adjusts the price an agent offers for a given interest based on its previous price and the success of the order placed at this price.
 * Price is adjusted relatively, e.g. 5% up or down from previous price.
 */
public class PriceStrategy extends EmptyBankStrategy {
	private static final int COMMERCIAL_LOAN_MATURITY = 1;

	/**
	 * The random distribution the submitted order sizes are drawn.
	 */
	protected AbstractDistribution orderSizeDistribution = new AbstractDistribution() {
		private static final long serialVersionUID = 1L;
		@Override
		public double nextDouble() {
			return 1.0;
		}
	};
	
	/**
	 * The random distribution used to select the stock for which an order is
	 * submitted to the stock market.
	 */
	protected AbstractDistribution stockInstrumentDistribution = new AbstractDistribution() {
		private static final long serialVersionUID = 1L;
		@Override
		public int nextInt() {
			return 1;
		}
		@Override
		public double nextDouble() {
			// TODO Auto-generated method stub
			return 0;
		}
	};

	protected double lastStockOrderPrice;
	private double dPriceStock;
	
	
	protected double lastLoanOrderPrice;
	private double dPriceLoan;
	
	private int typeIndicator; // 0 = SELL, 1 = BUY, this property is for testing only and should be removed in the final version
	
	// holds last orders placed by this bank for the instruments it trades
	protected Map<String, Order> lastOrders = new HashMap<String, Order>();

	protected final static double DELTA_STOCK_PRICE = 0.05;//5.0e4;//1.0;
	protected final static double DELTA_LOAN_PRICE = 0.05;//0.01;
	
	public PriceStrategy(final StrategyBank bank) {
		super(bank);
	}
	
	// default constructor, input type should be removed in the final version as for testing purposes only
	public PriceStrategy(
	   final StrategyBank bank,
	   final AbstractDistribution orderSizeDistribution,
		final AbstractDistribution stockInstrumentDistribution,
		final int type
		) {
	   super(bank);
		this.lastLoanOrderPrice = 5.0; //check whether this makes sense
		this.dPriceLoan = DELTA_LOAN_PRICE; //check whether this makes sense
		
		this.lastStockOrderPrice = 10.0;
		this.dPriceStock = DELTA_STOCK_PRICE ; //check whether this makes sense
		
		
		this.orderSizeDistribution = orderSizeDistribution;
		this.stockInstrumentDistribution = stockInstrumentDistribution;
		
		this.typeIndicator = type;
	}
	
	@Override
	public void considerStockMarkets() {
		// first remove the current stock orders, note that the cancel
		// method removes the order both from the market and this bank. 
		
		// maybe change this to some time out criterion
		for (final Order order : getBank().getOrders()) {
			if (order instanceof StockOrder){
				((StockOrder)order).cancel();
			}
		}
		
		//if (bank.getMarkets().containsKey( StockMarket.class )) {
			for (final Market market : getBank().getMarketsOfType(StockMarket.class)) {
				final StockMarket stockMarket = (StockMarket)market;
				final List<String> stockNames = stockMarket.getInstrumentNames();
				
				// Here: BANKS EITHER SELL OR BUY
				
				// Place a sell order
				// 1. Check assets for any stocks to sell
				if (this.typeIndicator == 0) {
					if (!getBank().getStockAccounts().isEmpty()){
						// 2. Randomly pick a stock to sell
						final Random generator = new Random(Simulation.getSimState().random.nextLong());
						final Object[] ownedStockKeys = getBank().getStockAccounts().keySet().toArray();
						final int randomIndex =  generator.nextInt(ownedStockKeys.length);
						final StockAccount stockToSell = getBank().getStockAccounts().get(ownedStockKeys[randomIndex]);
						
						
						final double stockOrderSize = 10.0; // stockOrderSize should be an integer??
						final double price = this.getPrice(stockToSell.getInstrumentName(), stockOrderSize, dPriceStock,market);//15.0;
						
						if (stockToSell.getQuantity() - stockOrderSize >= 0) {
							try {
								stockMarket.addOrder(getBank(),stockToSell.getInstrumentName(),stockOrderSize,price);
								// replaces last order for given instrument name or creates new entry
								this.lastStockOrderPrice = price;
								System.out.println("Bank SELL stock order placed at: "+price + "\tof size: " + stockOrderSize);

							} catch (AllocationException e) {
								e.printStackTrace();
								Simulation.getSimState().finish();
							} catch (final OrderException e) {
								e.printStackTrace();
								Simulation.getSimState().finish();
							}
						}
						
					}
				}
				
				else {
					// This part only considers buy orders
					if (! stockNames.isEmpty()){
						
						final double size = -10.0; //* Math.abs(orderSizeDistribution.nextDouble()); // why only buy orders?

						try {
							// select a stock to sell
							final String stockName = stockNames.get( stockInstrumentDistribution.nextInt() % stockNames.size() );
							final double a_price = this.getPrice(stockName, size, dPriceStock,market);//5.0
							
							System.out.println("Bank BUY stock order placed at: "+a_price);
							stockMarket.addOrder(getBank(),stockName,size,a_price);
							// replaces last order for given instrument name or creates new entry
							this.lastStockOrderPrice = a_price;
						} catch (AllocationException e) {
							e.printStackTrace();
							Simulation.getSimState().finish();
						} catch (final OrderException e) {
							e.printStackTrace();
							Simulation.getSimState().finish();
						}
						
					}
				}
			}
		//}
	}	
	
	@Override
	public void considerCommercialLoanMarkets() {
		// first remove the current deposit orders, note that the cancel
		// method removes the order both from the market and this bank.
		
		// maybe change this to some time out criterion
		for (final Order order : getBank().getOrders()) {
			if (order instanceof CommercialLoanOrder){
				((CommercialLoanOrder)order).cancel();
			}
		}
		
		//if (bank.getMarkets().containsKey( CommercialLoanMarket.class )) {
			for (final Market market : getBank().getMarketsOfType(CommercialLoanMarket.class)) {

				final double size = 1.0;//Math.abs(orderSizeDistribution.nextDouble());
				try {
					final double price = this.getPrice(LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY), size, dPriceLoan,market);
					((CommercialLoanMarket) market).addOrder(getBank(),
							COMMERCIAL_LOAN_MATURITY,
							size,
							price);
					this.lastLoanOrderPrice = price;
					
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
	/*
	public double getStockPrice(double orderSize) {
		// check if sell (size > 0) or buy (size < 0) order
		double price = 0;
		if (orderSize > 0) {
			// sell order
			if (Math.abs(this.stockOrderTradedVolume/this.lastStockOrderVolume) < 1.0) {
				price =  this.lastStockOrderPrice - this.dPriceStock;
			}
			else {
				price =  this.lastStockOrderPrice + this.dPriceStock;
			}
		}
		else {
			// buy order
			if (Math.abs(this.stockOrderTradedVolume/this.lastStockOrderVolume) < 1.0) {
				price =  this.lastStockOrderPrice + this.dPriceStock;
			}
			else {
				price =  this.lastStockOrderPrice - this.dPriceStock;
			}
		}
		return Math.max(price, 0.1);
	}
	
	public double getLoanPrice() {

		double price = 0;
		
		if (this.loanOrderTradedVolume/this.lastLoanOrderVolume < 1.0) {
			price =  this.lastLoanOrderPrice - this.dPriceLoan;
		}
		else {
			price =  this.lastLoanOrderPrice + this.dPriceLoan;
		}
		return Math.max(price, 0.1); //price should be >0.0! negative price is rejected, price = 0 is a market order
	}
	*/
	/**
	 * This is a general function to compute the price for a given instrument
	 * TODO Think about what to do when previous order was a sell and its a buy and vice versa
	 * @return updated price
	 */
	public double getPrice(final String instrumentName,final double orderSize, final double dPrice, final Market market) {
		
		if (lastOrders.containsKey(instrumentName)) {
			final double executedSize = lastOrders.get(instrumentName).getExecutedSize();
			//System.out.println("executedSize: "+executedSize);
			final double lastOrderSize = lastOrders.get(instrumentName).getSize();
			final double lastOrderPrice = lastOrders.get(instrumentName).getPrice();
			
			double newPrice = 0;
			final double threshold = 1.0;//0.98;
			/*
			if (lastOrderPrice >1e6) {
				System.out.println("we have problem here...");
			}
			*/
			if (orderSize > 0) {
				// sell order
				if (Math.abs(executedSize/lastOrderSize) < threshold) {
					newPrice =  lastOrderPrice*(1-dPrice);//lastOrderPrice - dPrice;
				}
				else {
					final double r = Simulation.getSimState().random.nextDouble();
					if (r < 1.0) {
						newPrice =  lastOrderPrice*(1+dPrice);//lastOrderPrice + dPrice;
						
					}
					else {
						newPrice =  lastOrderPrice;
					}
				}
			}
			else {
				// buy order
				if (Math.abs(executedSize/lastOrderSize) < threshold) {
					newPrice =  lastOrderPrice*(1+dPrice);//lastOrderPrice + dPrice;
					
				}
				else {
					final double r = Simulation.getSimState().random.nextDouble();
					if (r < 1.0) {
						newPrice =  lastOrderPrice*(1-dPrice);//lastOrderPrice - dPrice;
					}
					else {
						newPrice =  lastOrderPrice;
					}
				}
			}
			return Math.max(newPrice, 0.0001);
		}
		else {
			// initial values: for test purposes only. TODO come up with some good initialization
			if (market instanceof StockMarket) {
				return 2e5; // this is the initial stock price, also needs to be adjusted in stockReleasing Firm
			}
			else {
				if (orderSize > 0) {
					System.out.println("No order for this instrument name has been submitted in the past, assume price = 15.0.");
					return .03; // this is the initial lending rate
				}
				else {
					System.out.println("No order for this instrument name has been submitted in the past, assume price = 5.0.");
					return .03; // this is the initial lending rate
				}
			}
		}
	}
	
	public double getLastLoanOrderPrice() {
		return this.lastLoanOrderPrice;
	}
	
	
	public double getLastStockOrderPrice() {
		return this.lastStockOrderPrice;
	}
	
	@Override
	public void registerNewOrder(final Order order) {
		this.lastOrders.put(order.getInstrument().getInstrumentName(), order);
	}
	
}
