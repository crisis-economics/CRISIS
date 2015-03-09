/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.bank.strategies;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.LoanInstrument;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;

public class MacroBankStrategy extends CompetitionStrategyCFO {

   private double desiredLoanSupply;
	private double desiredStockMarketCapital;
	private double desiredCash;
	private double dividends;
    private double availableCash;
	
	private double initialPeriodCash;
	private double initialPeriodEquity;
	private StockMarket stockMarket;
	private CommercialLoanMarket commercialLoanMarket;
	private Bag firms;
	
   public MacroBankStrategy(StrategyBank bank) {
      super(bank);
   }
	
	private void allocateAssets(StrategyBank bank) {
		//we need to allocate our assets between cash, loans and stock market.
		
		for (final Order order : bank.getOrders()) {
			if (order instanceof CommercialLoanOrder){
				((CommercialLoanOrder)order).cancel();
			}
		}

		for (final Order order : bank.getOrders()) {
			if (order instanceof StockOrder){
				((StockOrder)order).cancel();
			}
		}
		
		
		
		
		
		MersenneTwisterFast random = Simulation.getSimState().random;
		
		double assets = bank.getTotalAssets();
		
		double a = 0.05+random.nextDouble()*0.1;
		double b = 0.4+random.nextDouble()*0.1;
		double c = 1 - a - b;
		desiredCash = a*assets;
		availableCash =  bank.getCashReserveValue() - desiredCash - bank.getAllocatedCash();
		availableCash = Math.max(availableCash, 0);
		desiredLoanSupply = b*assets;
		desiredStockMarketCapital = c*assets;
		
		initialPeriodCash = bank.getCashReserveValue();
		initialPeriodEquity = bank.getEquity();
	}
	
	
	@Override
	public void considerCommercialLoanMarkets() {
		
		//get situation to understand how much to pay in dividends at the end of the period
		
		allocateAssets(getBank());
		double loans = getBank().getAssetsInLoans();
		
		if (loans < desiredLoanSupply){
			double lendingRate=0.03;
			double commercialLoanOrder = Math.min(desiredLoanSupply-loans, availableCash);
			
			Order lastOrder;
			try {
				lastOrder = commercialLoanMarket.addOrder(getBank(),
						7 ,
						commercialLoanOrder,
						lendingRate);
				this.lastOrders.put(LoanInstrument.generateTicker(7), lastOrder);
			} catch (AllocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OrderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	
		
	}
	


	@Override
	public void considerStockMarkets() {
		
		
				
		for (final Order order : getBank().getOrders()) {
			if (order instanceof StockOrder){
				((StockOrder)order).cancel();
			}
		}
		
		//we want to have equal share of each firm		
		double unitStock = desiredStockMarketCapital/firms.size();
		
		Object[] instruments = (stockMarket.getInstrumentNames()).toArray();  
		
		for (final Object instrument : instruments) {
				
			StockMarket market = stockMarket;
			StockAccount stockAccount = getBank().getStockAccount((market.getInstrument("Stock_"+(String)instrument)).getInstrumentName());		
			
			double stockValue = 0;
			if (stockAccount != null){
				stockValue = stockAccount.getValue();
			}
			
			
			if (stockValue > unitStock & stockValue != 0) {
				// if stock account is different from -1 the bank has something to sell
				//sell shares
				double sellingPrice = stockAccount.getValue()/stockAccount.getQuantity()*0.99;
				double volume = Math.round((stockValue - unitStock)/sellingPrice);
				
				try {
					stockMarket.addOrder(getBank(),
							stockAccount.getInstrumentName(),
							volume,
							sellingPrice);
					System.out.println("stock market stock order" + stockAccount.getInstrumentName() + "volume: " + volume);
				} catch (AllocationException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				} catch (OrderException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				}
				
				
				
				
			}
			
			if  (stockValue < unitStock) {
				// if the stock account is not available then the stock value is 0
				//buy
				double buyingPrice = (market.getInstrument("Stock_"+(String)instrument)).getLastPrice()*1.01;
				if (buyingPrice == 0){
					buyingPrice = 1;}
					
				double volume = Math.round((unitStock-stockValue)/buyingPrice);
				availableCash =  getBank().getCashReserveValue() - desiredCash - getBank().getAllocatedCash();
				availableCash = Math.max(availableCash, 0);
				volume = Math.min(volume, availableCash/buyingPrice);
				if (volume > 0) {
					try {
						stockMarket.addOrder(getBank(),
								(market.getInstrument("Stock_"+(String)instrument)).getInstrumentName(),
								-volume,
								buyingPrice);
						System.out.println("stock market buy order" + (market.getInstrument("Stock_"+(String)instrument)).getInstrumentName() + "volume: " + volume);
					} catch (AllocationException e) {
						e.printStackTrace();
						Simulation.getSimState().finish();
					} catch (OrderException e) {
						e.printStackTrace();
						Simulation.getSimState().finish();
					}
	
			      }
			}
		}
	}
	
	@Override
	public void considerInterbankMarkets() {
		System.out.println("pay dividends");
		// here we will pay dividends (we don't know how the scheduling is handled for regularBanks and bankStrategies, it is hidden somewhere
		
		
		
		
		// we want to pay dividends only from cash flows
		this.dividends = 0.0;
		
		
		double deltaCash = getBank().getCashReserveValue() - initialPeriodCash;
		double deltaE = getBank().getEquity() - initialPeriodEquity;
		double deltaEi= getBank().getEquity() - getBank().getInitialEquity();
		
		if (deltaCash>0 & deltaE >0 & deltaEi>0){
			
			this.dividends = Math.min(deltaCash,deltaE);
			this.dividends = Math.min(this.dividends, getBank().getCashReserveValue()-desiredCash-getBank().getAllocatedCash());
		
		}
		
				
				//dividendo va pagato solo con unallocated cash
		
		getBank().setDividendPerShare(this.dividends / getBank().getNumberOfEmittedShares());
		//do nothing
		
	}

	public void setStockMarket(StockMarket stockMarket) {
		this.stockMarket = stockMarket;
		
	}


	public void setCommercialLoanMarket(
			CommercialLoanMarket commercialLoanMarket) {
		this.commercialLoanMarket = commercialLoanMarket;
		
	}
	
	@Override
	public void addFirms(Bag firms){
		this.firms = firms;
	}
	

}
