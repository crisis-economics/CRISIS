/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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

import java.util.ArrayList;
import java.util.List;

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
import eu.crisis_economics.abm.simulation.Simulation;

public class MarketMakerStrategy extends EmptyBankStrategy {

	private static final double DEFAULT_TARGET_MARKET_SHARE = .8; //fraction of total shares this agent aims at holding for each stock 
	private double targetSharesFraction=DEFAULT_TARGET_MARKET_SHARE;
	//private double orderSpread=1e2;//0.05;// if p is the last price of a stock, the agent will place a buy order at p-orderSpread and a sell order at p+orderSpread LINEAR 
	private double orderSpread=0.01;//0.01;//0.05;// if p is the last price of a stock, the agent will place a buy order at p-orderSpread and a sell order at p+orderSpread LINEAR 

	//private double orderSpread=0.05;// if p is the last price of a stock, the agent will place a buy order at p-orderSpread and a sell order at p+orderSpread PROPORTIONAL

	private Bag firms = new Bag();

	private double sharesFirm1 = 0.0;
	private double sharesFirm2 = 0.0;
	private double midPrice = 0.0;
	private double orderVolumeConst = 1000;//10;//1e-1;
	private double shareChange = 0.0;//1e-1;
	private double oldMarketValue = 0;//1e-1;


	List<Double> oldQuantity = new ArrayList<Double>();
	List<Double> newQuantity = new ArrayList<Double>();
	List<Double> oldPrice = new ArrayList<Double>();
	List<Double> newPrice = new ArrayList<Double>();


	public MarketMakerStrategy(final StrategyBank bank) {
		super(bank);
	}

	@Override
	public void considerCommercialLoanMarkets() {
	}

	@Override
	public void considerDepositMarkets() {
	}

	@Override
	public void considerStockMarkets() {
		// remove old orders
		for (final Order order : getBank().getOrders()) {
			if (order instanceof StockOrder){
				((StockOrder)order).cancel();
			}
		}

		////NEW MARKET MAKER////////////////////////////////////////////////////
		newQuantity.clear();
		newPrice.clear();
		for (int i=0; i<firms.size();i++){ 
			final StockReleasingFirm firm = (StockReleasingFirm) firms.get(i);
			double shares=0.;
			for(final Contract contract : getBank().getAssets()){
				if(contract instanceof StockAccount){
					final StockReleasingFirm firm2=(StockReleasingFirm)(((StockAccount) (contract)).getReleaser());
					if(firm == firm2) {
						shares=((StockAccount) (contract)).getQuantity();
					}
					/*					newQuantity.add((double) shares);
					if(Simulation.getFloorTime()<2){
						if ( i == 0) oldQuantity.clear();
						oldQuantity.add((double) shares);
						if ( i == 0) oldPrice.clear();
						oldPrice.add(firm.getMarketValue());

					}*/

					if (i == 0) this.sharesFirm1 = shares;
				}
			}
			newQuantity.add((double) shares);
			if(Simulation.getFloorTime()<2){
				if ( i == 0) oldQuantity.clear();
				oldQuantity.add((double) shares);
				if ( i == 0) oldPrice.clear();
				oldPrice.add(firm.getMarketValue());
			}
			for (final Market market : getBank().getMarketsOfType(StockMarket.class)) {	
				final StockInstrument instrument = ((StockMarket) market).getStockInstrument(firm.getUniqueName());
				if (instrument != null) {

					if(newQuantity.get(i)-oldQuantity.get(i)>0){
						//newPrice.add(oldPrice.get(i)*(1-orderSpread*Math.signum(newQuantity.get(i)-oldQuantity.get(i))));//percentage 
						newPrice.add(oldPrice.get(i)*(1-orderSpread*(newQuantity.get(i)-oldQuantity.get(i))/orderVolumeConst));//percentage AND PROPORTIONAL

					}
					else{
						newPrice.add(oldPrice.get(i)*(1+orderSpread/(1-orderSpread)*(-1)*(newQuantity.get(i)-oldQuantity.get(i))/orderVolumeConst));//*(((newQuantity.get(i)-oldQuantity.get(i)))));//percentage AND PROPORTIONAL
	
					}
					if(i == 0){
						shareChange=newQuantity.get(0)-oldQuantity.get(0);
						oldMarketValue = firm.getMarketValue();
					}

					try {
                       if (newQuantity.get(i) > 0) {
                       ((StockMarket) market).addOrder(
                          getBank(),
                          firm.getUniqueName(),
                          Math.min(newQuantity.get(i),orderVolumeConst),
                          newPrice.get(i)*(1+orderSpread/(1-orderSpread))
                          );
                       }
					} catch (AllocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (final OrderException e) {
						e.printStackTrace();
					}

                    try {
                       ((StockMarket) market).addOrder(
                          getBank(),
                          firm.getUniqueName(),
                          -orderVolumeConst,
                          Math.max(newPrice.get(i)*(1-orderSpread),1)
                          );
                    } catch (AllocationException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                    } catch (final OrderException e) {
                       e.printStackTrace();
                    }

					oldQuantity.set(i,newQuantity.get(i));
					oldPrice.set(i,newPrice.get(i));
				}

				if(!this.newPrice.isEmpty())
					this.midPrice = this.newPrice.get(0);
				else this.midPrice = 0;
			}
		}	//END NEW MARKET MAKER
		/*		////OLD MARKET MAKER///////////////////////////////////////////////////



		for (int i=0; i<firms.numObjs;i++){
			final StockReleasingFirm firm = (StockReleasingFirm) firms.get(i);
			final int totalShares = firm.getNumberOfEmittedShares();

			final double price = firm.getMarketValue();
			int deltaShares=0;
			int shares=0;
			for(final Contract contract : bank.getAssets()){
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
			deltaShares= (int)(targetSharesFraction*totalShares)-shares;//number of shares exceeding the target
			System.out.println("MARKET MAKER HAS NUMBER OF SHARES = "+shares);
			if (i == 0) {
				sharesFirm1 = shares;
			}
			else {
				sharesFirm2 = shares;
			}


			//if (bank.getMarkets().containsKey( StockMarket.class )) {				
				for (final Market market : bank.getMarketsOfType(StockMarket.class)) {	
					final StockInstrument instrument = ((StockMarket) market).getStockInstrument(firm.getUniqueName());
					if (instrument != null) {
						final double lastMidPrice = instrument.getLastMidPrice();
						if (lastMidPrice > 0.0) {
							//price = lastMidPrice;
						}
					}

					if(shares>0){


						try {
							((StockMarket) market).addOrder(bank,
									firm.getUniqueName(),
									shares,price*(1+orderSpread));
									//price+orderSpread);//price*(1+orderSpread));	
							System.out.println("StockOrder MarketMaker" + firm.getUniqueName() + " volume: " + shares + " price: " + (price+orderSpread));
						} catch (AllocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final OrderException e) {
							e.printStackTrace();
						}

						try {
							((StockMarket) market).addOrder(bank,
									firm.getUniqueName(),
									-shares,price*(1-orderSpread));
									//-(int)(targetSharesFraction*totalShares),price*(1-orderSpread));
									//price-orderSpread);//price*(1-orderSpread));	
							System.out.println("StockOrder MarketMaker" + firm.getUniqueName() + " volume: " + -shares + " price: " + (price-orderSpread));
						} catch (AllocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final OrderException e) {
							e.printStackTrace();
						}
					}

					if(deltaShares>0){

						try {
							((StockMarket) market).addOrder(bank,
									firm.getUniqueName(),
									-deltaShares,price*(1-orderSpread));
									//price-orderSpread);//price*(1-orderSpread));	
							System.out.println("StockOrder MarketMaker" + firm.getUniqueName() + " volume: " + -deltaShares + " price: " + (price-orderSpread));
						} catch (AllocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final OrderException e) {
							e.printStackTrace();
						}

					}
					if(deltaShares<0){

						try {
							((StockMarket) market).addOrder(bank,
									firm.getUniqueName(),
									deltaShares,price*(1+orderSpread));
									//price-orderSpread);//price*(1-orderSpread));	
							System.out.println("StockOrder MarketMaker" + firm.getUniqueName() + " volume: " + deltaShares + " price: " + (price+orderSpread));
						} catch (AllocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final OrderException e) {
							e.printStackTrace();
						}


					}

				}
			//}
		} //END OLD MARKET MAKER*/
	}

	@Override
	public void considerInterbankMarkets() {
	}

	public void addFirms(final Bag firms) {
	//public void addFirms(final List<Firm> firms) {
		this.firms = firms;
		for (int i=0;i<firms.size();++i){
			this.oldPrice.add(((StockReleasingFirm) firms.get(i)).getMarketValue());
			this.oldQuantity.add(1.0);		
		}

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
	public double getMidPrice() {
		return midPrice;
	}

	public double getTotalShares(final StrategyBank bank) {
		double totalShares=0.0;
		for(final Contract contract : bank.getAssets()){
			if(contract instanceof StockAccount)
				totalShares += 1.0*((StockAccount) contract).getQuantity();
		}
		return totalShares;
	}
	public double getTotalShareChange(final StrategyBank bank) {
		return this.shareChange;
	}

	public double getPriceChange(final StrategyBank bank) {
		return ((StockReleasingFirm) firms.get(0)).getMarketValue()-this.oldMarketValue;
	}


}
