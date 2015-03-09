/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Fabio Caccioli
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
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockInstrument;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;

public class MarketMakerSpringStrategy extends EmptyBankStrategy {
	
	private static final double DEFAULT_TARGET_MARKET_SHARE = .8; //fraction of total shares this agent aims at holding for each stock 
	private double targetSharesFraction=DEFAULT_TARGET_MARKET_SHARE;
	private double orderSpread=0.05;// if p is the last price of a stock, the agent will place a buy order at p-orderSpread and a sell order at p+orderSpread 
	private Bag firms = new Bag();
	
	private double sharesFirm1 = 0.0;
	private double sharesFirm2 = 0.0;
	double sellPrice = 1e12;
	double buyPrice = 1.0;
	double springConst = 1e5;
	double lastMidPrice = 1.0;
	
	public MarketMakerSpringStrategy(final StrategyBank bank) {
	   super(bank);
	}
	
	@Override
	public void considerStockMarkets() {
		// remove old orders
		for (final Order order : getBank().getOrders()) {
			if (order instanceof StockOrder){
				((StockOrder)order).cancel();
			}
		}

		getBank().getCashReserveValue();
		for (int i=0; i<firms.numObjs;i++){
			final StockReleasingFirm firm = (StockReleasingFirm) firms.get(i);
			final double totalShares = firm.getNumberOfEmittedShares();
			
			firm.getMarketValue();
			double deltaShares=0;
			double shares=0;
			for(final Contract contract : getBank().getAssets()){
				int check=0;
				if(contract instanceof StockAccount){
					final StockReleasingFirm firm2=(StockReleasingFirm)(((StockAccount) (contract)).getReleaser());
					if(firm == firm2) {
						check=1;
					}
				}
				if(check==1) {
					shares=((StockAccount) (contract)).getQuantity();
				}

			}
			deltaShares= targetSharesFraction*totalShares-shares;//number of shares exceeding the target
			System.out.println("MARKET MAKER HAS NUMBER OF SHARES = "+shares);
			if (i == 0) {
				sharesFirm1 = shares;
			}
			else {
				sharesFirm2 = shares;
			}
			

			
			//if (bank.getMarkets().containsKey( StockMarket.class )) {				
				for (final Market market : getBank().getMarketsOfType(StockMarket.class)) {	
					final StockInstrument instrument = ((StockMarket) market).getStockInstrument(firm.getUniqueName());
					if (instrument != null) {
						lastMidPrice = instrument.getLastMidPrice();
						if (lastMidPrice > 0.0) {
							//price = lastMidPrice;
						}
					}
					
					if(deltaShares>0){
						//buyPrice = lastMidPrice*(1-orderSpread-springConst*deltaShares/totalShares);
						//sellPrice =  lastMidPrice*(1+orderSpread+springConst*deltaShares/totalShares);						
						//buyPrice = firm.getMarketValue()*(1-orderSpread-springConst*deltaShares/totalShares);
						//sellPrice =  firm.getMarketValue()*(1+orderSpread+springConst*deltaShares/totalShares);
						//buyPrice = Math.max(0.1,firm.getMarketValue()*(1-orderSpread-springConst*deltaShares/totalShares));
						//sellPrice =  firm.getMarketValue()*(1+orderSpread+springConst*deltaShares/totalShares);						
						buyPrice = Math.max(0.1,(firm.getMarketValue()+springConst*deltaShares/totalShares)*(1-orderSpread));
						sellPrice =  (firm.getMarketValue()+springConst*deltaShares/totalShares)*(1+orderSpread);						

					}
					
					else{
//						buyPrice = firm.getMarketValue()*(1-orderSpread-springConst*deltaShares/totalShares);													
//						sellPrice =  firm.getMarketValue()*(1+orderSpread);
						//buyPrice = Math.max(0.1,firm.getMarketValue()*(1-orderSpread+springConst*deltaShares/totalShares));													
						//sellPrice =  firm.getMarketValue()*(1+orderSpread-springConst*deltaShares/totalShares);
						 double midPrice = Math.max(0.1,firm.getMarketValue()-(springConst*(-deltaShares)/totalShares));
						buyPrice = Math.max(0.1,midPrice*(1-orderSpread));
						sellPrice =  midPrice*(1+orderSpread);						

					
					}
					
					if(shares>0){
						/*final StockInstrument instrument = ((StockMarket) market).getStockInstrument(firm.getUniqueName());
						if (instrument != null) {
							lastMidPrice = instrument.getLastMidPrice();
							if (lastMidPrice > 0.0) {
								//price = lastMidPrice;
							}
						}*/
						

						try {
							((StockMarket) market).addOrder(getBank(),
									firm.getUniqueName(),
									0.01*shares,
									sellPrice);
						} catch (AllocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final OrderException e) {
							e.printStackTrace();
						}

						try {
							((StockMarket) market).addOrder(getBank(),
									firm.getUniqueName(),
									-0.01*shares,buyPrice);
						} catch (AllocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final OrderException e) {
							e.printStackTrace();
						}
					}
					
					if(deltaShares>0){
						
						try {
							((StockMarket) market).addOrder(getBank(),
									firm.getUniqueName(),
									-deltaShares,buyPrice);
									//price-orderSpread);//price*(1-orderSpread));	
//							System.out.println("StockOrder MarketMaker" + firm.getUniqueName() + " volume: " + -deltaShares + " price: " + (price-orderSpread));
						} catch (AllocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final OrderException e) {
							e.printStackTrace();
						}
						
					}
					if(deltaShares<0){
						
						try {
							((StockMarket) market).addOrder(getBank(),
									firm.getUniqueName(),
									-deltaShares,sellPrice);
									//price-orderSpread);//price*(1-orderSpread));	
//							System.out.println("StockOrder MarketMaker" + firm.getUniqueName() + " volume: " + deltaShares + " price: " + (price-orderSpread));
						} catch (AllocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final OrderException e) {
							e.printStackTrace();
						}
						
					}	
					}
					
				}
			//}
		}
	
	public void addFirms(final Bag firms) {
		this.firms = firms;
	}

	public void setOrderSpread(final double orderSpread) {
		this.orderSpread = orderSpread;
	}

	public double getOrderSpread() {
		return this.orderSpread;
	}

	public void setTargetSharesFraction(final double targetSharesFraction) {
		this.targetSharesFraction = targetSharesFraction;
	}

	public double getTargetSharesFraction() {
		return this.targetSharesFraction;
	}
	
	public double getSharesFirm2() {
		return sharesFirm2;
	}
	
	public double getSharesFirm1() {
		return sharesFirm1;
	}
	
	public double getTotalShares(final StrategyBank bank) {
		double totalShares=0.0;
		for(final Contract contract : bank.getAssets()){
			if(contract instanceof StockAccount)
				totalShares += 1.0*((StockAccount) contract).getQuantity();
		}
		return totalShares;
	}
	
}

