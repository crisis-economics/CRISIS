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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.simulation.Simulation;


/**
 * This class fully encapsulates properties of a traded exchange instrument. 
 * The class holds four books - bid order, ask order, and two books representing filled and partially 
 * filled orders. It additionally holds references to updated orders. 
 * These containers are not manipulated by this class directly, instead it requires a class implementing 
 * <code>BookEngineInterface</code>, that makes all of the necessary modifications to these containers. 
 * Each instrument does its own matching and order manipulations, without interfering with any other traded instruments.
 * @author  olaf
 */
public abstract class Instrument implements Serializable{

	private static final long serialVersionUID = -6844754563075571566L;

	/**
	 * SYNCHRONOUS: no immediate matching, matching is triggered by scheduled event at time ...
	 * ASYNCHRONOUS: immediate matching if possible
	 * @author olaf
	 */
	public enum MatchingMode{ ASYNCHRONOUS, SYNCHRONOUS }
	/**
	 * The lot size minimum $\sigma$ is the smallest amount of the asset that can be traded in the market. 
	 * Sell orders must arrive with a size $\omega_x\in{\sigma+k\epsilon\for k=1,2,...}$.
	 * Buy orders must arrive with a size $\omega_x\in{-(\sigma+k\epsilon)\for k=1,2,...}$.
	 */
	public static final double LOTSIZEMINIMUM = 1.0;

	/**
	 * The lot size increment $\epsilon$ is the incremental amount of the asset that can be traded in the market. 
	 * Sell orders must arrive with a size $\omega_x\in{\sigma+k\epsilon\for k=1,2,...}$.
	 * Buy orders must arrive with a size $\omega_x\in{-(\sigma+k\epsilon)\for k=1,2,...}$.
	 */
	public static final double LOTSIZEINCREMENT = LOTSIZEMINIMUM;
	
	/**
	 * The tick size $\delta p$ is the smallest price interval between different orders that is permissible in the market.
	 * All orders must arrive with a price that is specified to the accuracy of $\delta p$.
	 */
	public static final double TICKSIZE = 0.01;

	/**
	 * Time to sync the market. 0...no sync
	 */
	public static final int SYNCTIME = 0;

	private final String tickerSymbol;
	private final List<Order> bidLimitOrders;
	private final List<Order> askLimitOrders;
	private final List<Order> filledOrders;
	private final List<Order> partiallyFilledOrders;
	private final BookEngineInterface bookEngine;
	
	private double bidVolume;
	private double askVolume;
	private double buyVolume;
	private double sellVolume;
	private double lastPrice;
	private double averagePrice;
	private double total_QuantityTimesPrice;
	private double total_Quantity;
	private double averageBuyPrice;
	private double total_BoughtQuantityTimesPrice;
	private double total_BoughtQuantity;
	private double averageSellPrice;
	private double total_SoldQuantityTimesPrice;
	private double total_SoldQuantity;
	private double bidVWAP;
	private double total_BidQuantityTimesPrice;
	private double total_BidQuantity;
	private double askVWAP;
	private double total_AskQuantityTimesPrice;
	private double total_AskQuantity;
	private double bidHigh;
	private double bidLow;
	private double askHigh;
	private double askLow;
	
	private double lastMidPrice;

	private MatchingMode matchingMode;

	private Set<InstrumentListener> listeners;

	
	/**
	 * Creates an investment instrument with a given ticker symbol and a default matchingMode. The user should not mess 
	 * around with this. Instead let the the market do it. 
	 * @param tickerSymbol
	 * @param updatedOrders
	 */
	protected Instrument(final String tickerSymbol, final BlockingQueue<Order> updatedOrders) {
		this(tickerSymbol, updatedOrders, MatchingMode.ASYNCHRONOUS,new HashSet<InstrumentListener>());
	}

	protected Instrument(final String tickerSymbol, final BlockingQueue<Order> updatedOrders,final Set<InstrumentListener> listeners) {
		this(tickerSymbol, updatedOrders, MatchingMode.ASYNCHRONOUS,new HashSet<InstrumentListener>());
		this.listeners = listeners;
	}

	/**
	 * Creates an investment instrument with a given ticker symbol and a given matchingMode. The user should not mess 
	 * around with this. Instead let the the market do it. 
	 * @param tickerSymbol
	 * @param updatedOrders
	 * @param matchingMode The matching mode defines the behavior of the instrument: Synchronous means all orders are collected and then matched. 
	 * Asynchronous means new orders are tested for matching at the time of arrival.
	 */
	protected Instrument(final String tickerSymbol, final BlockingQueue<Order> updatedOrders, final MatchingMode matchingMode, final Set<InstrumentListener> listeners) {
		
		this.tickerSymbol = tickerSymbol;
		if (matchingMode == MatchingMode.ASYNCHRONOUS) {
			this.setAsynchronous(); // we need to call this to schedule the matching
		} else {
			this.setSynchronous();
		}
		
		this.bidLimitOrders = new ArrayList<Order>();
		this.askLimitOrders = new ArrayList<Order>();
		this.filledOrders   = new ArrayList<Order>();
		this.partiallyFilledOrders = new ArrayList<Order>();
		this.listeners = listeners;
		
		//start a new book processing engine for this instrument
		bookEngine = new BookEngine(bidLimitOrders, askLimitOrders, filledOrders, partiallyFilledOrders, updatedOrders, tickerSymbol);
		
		//Make sure all variables are initialized to zero from the beginning
		bidVolume = askVolume = buyVolume = sellVolume = 0;
		averagePrice = averageSellPrice = averageBuyPrice = 0.0;
		bidVWAP = askVWAP = 0.0;
		bidHigh = bidLow = askHigh = askLow = 0.0;
		
		lastMidPrice = 0.0;
		
		//Initialise helper variables to zeroes
		total_QuantityTimesPrice = 0.0;
		total_Quantity = 0;
		total_BoughtQuantityTimesPrice = 0.0;
		total_BoughtQuantity = 0;
		total_SoldQuantityTimesPrice = 0.0;
		total_SoldQuantity = 0;
		total_AskQuantityTimesPrice = 0.0;
		total_AskQuantity = 0;
		total_BidQuantityTimesPrice = 0.0;
		total_BidQuantity = 0;
		
		// schedule reset statistics every time 
		Simulation.getSimState().schedule.scheduleRepeating( 
			new Steppable() {
						private static final long serialVersionUID = -3028084676261638035L;

						@Override
						public void step(SimState state) {
							//Make sure all variables are initialized to zero from the beginning
							bidVolume = askVolume = buyVolume = sellVolume = 0;
							averagePrice = averageSellPrice = averageBuyPrice = 0.0;
							bidVWAP = askVWAP = 0.0;
							bidHigh = bidLow = askHigh = askLow = 0.0;
							
							lastMidPrice = 0.0;
							
							//Initialise helper variables to zeroes
							total_QuantityTimesPrice = 0.0;
							total_Quantity = 0;
							total_BoughtQuantityTimesPrice = 0.0;
							total_BoughtQuantity = 0;
							total_SoldQuantityTimesPrice = 0.0;
							total_SoldQuantity = 0;
							total_AskQuantityTimesPrice = 0.0;
							total_AskQuantity = 0;
							total_BidQuantityTimesPrice = 0.0;
							total_BidQuantity = 0;
						}
			});	
	}


	/**
	 * Get the bid order book
	 * @return  a Bag of bid limit orders
	 */
	public Bag getBidLimitOrders(){
		return new Bag(bidLimitOrders);
	}
	/**
	 * Get the ask order book
	 * @return  a Bag of ask limit orders
	 */
	public Bag getAskLimitOrders(){
		return new Bag(askLimitOrders);
	}
	/**
	 * Get the book of fully filled orders. 
	 * Since orders are never removed, this is a memory leak. 
	 * Orders are no longer added
	 * @return a Bag of fully filled orders
	 */
	@Deprecated
	protected Bag getFilledOrders(){
		return new Bag(filledOrders);
	}
	/**
	 * Get the book of partially filled orders
	 * @return a Bag of partially filled orders
	 */
	protected Bag getPartiallyFilledOrders(){
		return new Bag(partiallyFilledOrders);
	}
	
	/**
	 * Match all Orders (for sync mode)
	 * @return void
	 * @throws InstrumentException 
	 */
	@Deprecated
	protected void matchOrders() throws InstrumentException{
		this.askLow=0.0;
		this.lastMidPrice = this.getCurrentMidPrice();
		bookEngine.matchOrders();
	}
	
	/**
	 * Match all Orders, matched price is best-bid price (default is best-ask price) (for sync mode)
	 * @return void
	 * @throws InstrumentException 
	 */
	@Deprecated
	protected void matchOrdersBestBid() throws InstrumentException{
		this.askLow=0.0;
		this.lastMidPrice = this.getCurrentMidPrice();
		bookEngine.matchOrdersBestBid();
	}
	
	/**
	 * Match all Orders using the interbank LOB. (for sync mode)
	 * Goes through all lenders (ask), starting from the best (lowest)
	 * For each lender, goes through all borrowers (bid) starting from the highest
	 * If the engine runs out of lenders or borrowers, and a central bank is in the LOB,
	 * lenders deposit with the CB, and borrowers borrow from the CB.
	 * @return void
	 * @throws InstrumentException 
	 */
	@Deprecated
	protected void matchOrdersInterbank() throws InstrumentException{
		this.askLow=0.0;
		this.lastMidPrice = this.getCurrentMidPrice();
		bookEngine.matchOrdersInterbank();
	}
	
	/**
	 * Match all Orders (for sync mode)
	 * @return void
	 * @throws InstrumentException 
	 */
	protected void synchronizedOrderMatch() throws InstrumentException{
		bookEngine.synchronizedOrderMatch();
	}
	/**
	 * Processes an order with specified price and quantity
	 * @param order	a valid order object
	 * @return void
	 */
	protected void processNewOrder(final Order order){
		bookEngine.processNewOrder(order);
	}
	
	/**
	 * Processes a cancellation of an already submitted order
	 * @param order	client's own order
	 * @return	an <code>Order</code> object that has been cancelled, <code>null</code> if the order has already been filled
	 */
	protected Order processCancelOrder(final Order order){
		return bookEngine.processCancelOrder(order);
	}
	
	/**
	 * Insert a valid buy order into either of order books.
	 * @param order an <code>Order</code> object previously checked for its validity.
	 */
	protected void insertBuyOrder(final Order order){
		bookEngine.insertBuyOrder(order);
	}
	
	/**
	 * Insert a valid sell buy order into either of order books.
	 * @param order an <code>Order</code> object previously checked for its validity.
	 */
	protected void insertSellOrder(final Order order){
		bookEngine.insertSellOrder(order);
	}
	/**
	 * @return  the tickerSymbol
	 */
	protected String getTickerSymbol() {
		return tickerSymbol;
	}
	
	@Override
	public String toString(){
		return this.tickerSymbol + " (symbol) "
				+ this.askLimitOrders.size() + " (askLimitOrders) "
				+ this.bidLimitOrders.size() + " (bidLimitOrders) "
				+ this.filledOrders.size() + " (filledOrders) "
				+ this.partiallyFilledOrders.size() + " (partiallyFilledOrders) "
				+ this.getAskHigh() + " (AskHigh) "
				+ this.getBestAsk() + " (BestAsk) "
				+ this.getAskLow() + " (AskLow) "
				+ this.getAskVolume() + " (AskVolume) "
				+ this.getBuyVolume() + " (BuyVolume) "
				+ this.getBidHigh() + " (BidHigh) "
				+ this.getBidLow() + " (BidLow) "
				+ this.getBestBid() + " (BestBid) "
				+ this.getBidVolume() + " (BidVolume) "
				+ this.getSellVolume() + " (SellVolume) "
				+ this.getLastPrice() + " (LastPrice) "
				;
	}
	
	/**
	 * Get this instrument's bid volume - volume of the bid side of the book
	 * @return
	 */
	protected double getBidVolume(){
		return bidVolume;
	}

	/**
	 * bid volume is only updated when a new buy order is added, or when matched by a sell order, i.e. it is a volume of the book on the bid side
	 * @param volume
	 */
	protected void updateBidVolume(final double volume) {
		bidVolume += volume;
	}

	/**
	 * Get this instrument's ask volume - volume of the ask side of the book
	 * @return
	 */
	protected double getAskVolume(){
		return askVolume;
	}

	/**
	 * ask volume is only updated when a new sell order is added, or when matched by a buy order
	 * @param volume
	 */
	protected void updateAskVolume(final double volume) {
		askVolume += volume;
	}

	protected double getBidHigh(){
		return bidHigh;
	}
	protected void updateBidHigh(final double price){
		if(price > bidHigh) {
			bidHigh = price;
		}
	}

	protected double getBidLow(){
		return bidLow;
	}
	protected void updateBidLow(final double price){
		if(bidLow == 0) {
			bidLow = price;
		} else{
			if(price < bidLow) {
				bidLow = price;
			}
		}
	}

	/**
	 * Get this Instrument's ask volume weighted average price (VWAP)
	 * @return <code>this</code> Instrument's ask volume weighted average price
	 */
	protected double getAskVWAP(){
		return askVWAP;
	}
	
	/**
	 * Update this Instrument's ask volume weighted average price (VWAP)
	 * @param quantity
	 * @param price
	 */
	protected void updateAskVWAP(final double quantity, final double price){
		total_AskQuantityTimesPrice += quantity*price;
		if(price != 0.0) {
			total_AskQuantity += quantity;
		}
		askVWAP = (new BigDecimal(total_AskQuantityTimesPrice/total_AskQuantity)).setScale(4, RoundingMode.HALF_UP).doubleValue();
	}

	public double getAskHigh(){
		return askHigh;
	}
	protected void updateAskHigh(final double price){
		if(price > askHigh) {
			askHigh = price;
		}
	}
	// aymanns: changed to public
	public double getAskLow(){
		return askLow;
	}
	protected void updateAskLow(final double price){
		if(askLow == 0) {
			askLow = price;
		} else{
			if(price < askLow) {
				askLow = price;
			}
		}
	}

	/**
	 * Get this Instrument's bid volume weighted average price (VWAP)
	 * @return <code>this</code> Instrument's bid volume weighted average price
	 */
	public double getBidVWAP(){
		return bidVWAP;
	}
	
	protected void updateBidVWAP(final double size, final double price){
		
		total_BidQuantityTimesPrice += size*price;
		if(price != 0.0) {
			total_BidQuantity += size;
		}
		bidVWAP = (new BigDecimal(total_BidQuantityTimesPrice/total_BidQuantity)).setScale(4, RoundingMode.HALF_UP).doubleValue();
	}


	/**
	 * Get this Instrument's last traded price
	 * @return last price of this instrument
	 */
	public double getLastPrice(){
		return lastPrice;
	}
	
	public double getLastMidPrice() {
		return lastMidPrice;
	}
	
	/**
	 * Set this Instrument's last traded price
	 * @param price a non-negative price 
	 * @throws InstrumentException 
	 * @exception InstrumentException if the passed argument is negative
	 */
	protected void updateLastPrice(final double price) throws InstrumentException{
		if(price <0) {
			throw new InstrumentException("Invalid price: " + price);
		}
		lastPrice = price;
	}

	/**
	 * Get this Instrument's sell volume. 
	 * @return <code>this</code> Instrument's sell volume
	 */
	protected double getSellVolume(){
		return sellVolume;	
	}
	
	/**
	 * sell volume is only updated when sell order has been matched, i.e. it is a volume of matched sell orders
	 * @param volume
	 */
	protected void updateSellVolume(final double volume){
		sellVolume += volume;
	}

	/**
	 * Get this Instrument's average traded price
	 * @return <code>this</code> Instrument's bid volume weighted average price
	 */
	//aymanns made this public
	public double getAveragePrice(){
		return averagePrice;
	}
	
	/**
	 * Uses BigDecimal to do correct rounding of doubles. Currently rounds to 4 decimal places using HALF_UP mode
	 * @param quantity
	 * @param price
	 */
	protected void updateAveragePrice(final double quantity, final double price){
		total_QuantityTimesPrice += quantity*price;
		total_Quantity += quantity;
		
		averagePrice = (new BigDecimal(total_QuantityTimesPrice/total_Quantity)).setScale(4, 
			RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Get this Instrument's average price, initiated by a sell order
	 * @return <code>this</code> Instrument's bid volume weighted average price
	 */
	protected double getAverageSellPrice(){
		return averageSellPrice;
	}
	
	protected void updateAverageSellPrice(final double quantity, final double price){
		total_SoldQuantityTimesPrice += quantity*price;
		total_SoldQuantity += quantity;
		
		averageSellPrice = (new BigDecimal(total_SoldQuantityTimesPrice/total_SoldQuantity)).setScale(4, 
				RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Get this Instrument's buy volume. 
	 * @return <code>this</code> Instrument's buy volume
	 */
	protected double getBuyVolume(){
		return buyVolume;
	}
	
	/**
	 * buy volume is only updated when buy order has been matched, i.e. it is a volume of matched buy orders
	 * @param volume
	 */
	protected void updateBuyVolume(final double volume){
		buyVolume += volume;
	}

	/**
	 * Get this Instrument's average price, initiated by a buy order
	 * @return <code>this</code> Instrument's bid volume weighted average price
	 */
	protected double getAverageBuyPrice(){
		return averageBuyPrice;
	}
	
	/**
	 * @param quantity
	 * @param price
	 */
	protected void updateAverageBuyPrice(final double quantity, final double price){
		total_BoughtQuantityTimesPrice += quantity*price;
		total_BoughtQuantity += quantity;
		
		averageBuyPrice = (new BigDecimal(total_BoughtQuantityTimesPrice/total_BoughtQuantity)).setScale(4, 
			RoundingMode.HALF_UP).doubleValue();
		
	}
	
	/**
	 * Get this Instrument's bid volume at specific price (bid-side depth),
	 * which is the total size of all active buy orders at that price.
	 * @param price 	price to get volume at
	 * @return	bid volume at the specified price
	 */
	protected double getBidVolumeAtPrice(final double price){
		
		double volume = 0;
		//Will return zero immediately if there is no order at this price or the book is empty
		for(final Order order : bidLimitOrders){
			if(order.getPrice() == price) {
				volume += order.getOpenSize();
			}
			if(order.getPrice() < price) {
				break;
			}
		}
		return volume;
	}
	
	/**
	 * Get this Instrument's ask volume at specific price (ask-side-depth),
	 * which is the total size of all active sell orders at that price.
	 * @param price 	price to get volume at
	 * @return	ask volume at the specified price
	 */
	protected double getAskVolumeAtPrice(final double price){
		double volume = 0;
		//Returns zero immediately if there is no order at this price or the book is empty
		for(final Order order : askLimitOrders){
			if(order.getPrice() == price) {
				volume += order.getOpenSize();
			}
			if(order.getPrice() > price) {
				break;
			}
		}
		return volume;
	}
	
	/**
	 * Get this Instrument's best bid price (bid price). 
	 * Is the highest stated price among active buy orders,
	 * so it is on top of the bidLimitOrders list (descending order).
	 * @return this instrument's best bid price
	 */
	protected double getBestBid(){
		return getBestPrice(bidLimitOrders);
	}
	
	/**
	 * Get this Instrument's bid relative price.
	 * The bid price is the highest price at which it is possible to sell immediately at least the lot size of the asset being traded. 
	 * The bid relative price is the difference between the bid price and the given price.
     * @param price given price
	 * @return this instrument's bid relative price
	 */
	protected double getBidRelativePrice(final double price){
		return getBestBid()-price;
	}

	/**
	 * Get this Instrument's best ask price (ask price).
	 * is the lowest stated price among active sell orders,
	 * so it is on top of the askLimitOrders list (ascending order).
	 * @return this instrument's best ask price
	 */
	public double getBestAsk(){
		return getBestPrice(askLimitOrders);
	}
	
	/**
	 * Get this Instrument's ask relative price.
	 * The ask price is the lowest price at which it is possible to buy immediately at least the lot size of the asset being traded. 
	 * The ask relative price is the difference between the given price and the ask price.
     * @param price given price
	 * @return this instrument's ask relative price
	 */
	protected double getAskRelativePrice(final double price){
		return price-getBestAsk();
	}

	/**
	 * Get this Instrument's bid-ask-spread.
	 * It is the difference between the ask price and the bid price.
	 * @return this instrument's bid-ask-spread
	 */
	protected double getBidAskSpread(){
		return getBestAsk()-getBestBid();
	}

	/**
	 * Get this Instrument's mid price.
	 * the average of the ask price and the bid price.
	 * @return this instrument's mid price
	 */
	protected double getMidPrice(){
		return (getBestAsk()+getBestBid())*.5;
	}
	
	/*
	 * A function that actually returns the mid price based on the current limit order book
	 */
	
	protected double getCurrentMidPrice() {
		return (getCurrentAskLow()+getCurrentBidHigh())*0.5;
	}
	
	/*
	 * Returns askLow of the current limitOrder book
	 */
	
	protected double getCurrentAskLow() {
		double min = 0;
		if (askLimitOrders.size() > 0) {
			min = askLimitOrders.get(0).getPrice();
			for (int i=1;i<askLimitOrders.size();i++) {
				if (askLimitOrders.get(i).getPrice() < min) {
					min = askLimitOrders.get(i).getPrice();
				}
			}
		}
		return min;
	}
	
	/*
	 * Returns bidHigh of the current limitOrder book
	 */
	
	protected double getCurrentBidHigh() {
		double max = 0;
		if (bidLimitOrders.size() > 0) {
			max = bidLimitOrders.get(0).getPrice();
			for (int i=1;i<bidLimitOrders.size();i++) {
				if (bidLimitOrders.get(i).getPrice() > max) {
					max = bidLimitOrders.get(i).getPrice();
				}
			}
		}
		return max;
	}
	
	/**
	 * Get this Instrument's relative price.
	 * The relative price of a buy order is the bid relative price.
	 * The relative price of a sell order is the ask relative price.
	 * <ul>
     * <li>an arriving order with a relative price >= 0 will not cause an immediate matching, instead becoming an active order.</li>
     * <li>an arriving order with a relative price between -spread and 0 will not cause an immediate matching, instead becoming an active order within the spread.</li>
     * <li>an arriving order with a relative price <= -spread will cause an immediate matching.</li>
     * </ul>
     * @param o given order
	 * @return this instrument's ask relative price
	 */
	protected double getRelativePrice(final Order o){
		if (o.getSide()==Order.Side.BUY) {
			return getBidRelativePrice(o.getPrice());
		} else {
			return getAskRelativePrice(o.getPrice());
		}
	}
	

	private double getBestPrice(final List<Order> book){
		if(book.size() > 0) {
			return book.get(0).getPrice();
		}
		//if the book is empty, returns zero
		return 0.0;
	}
	
	/**
	 * Returns the price at the i-th increment (depth) of available prices for buy orders starting with 0 for bid price.
	 * @param depth
	 * @return
	 */
	protected double getBidPriceAtDepth(final int depth){
		return getPriceAtDepth(depth, bidLimitOrders);
	}
	/**
	 * Returns the price at the i-th increment (depth) of available prices for sell orders starting with 0 for ask price.
	 * @param depth
	 * @return
	 */
	protected double getAskPriceAtDepth(final int depth){
		return getPriceAtDepth(depth, askLimitOrders);
	}
	
	private double getPriceAtDepth(final int depth, final List<Order> book){
		//a variable to hold a number of unique prices seen so far
		int tracker = 0;
		if(book.size()>0){
			//depth zero refers to the best price in the book
			if(depth == 0) {
				return book.get(0).getPrice();
			}
			//extract the very first price, first unique price seen so far
			double price = book.get(0).getPrice();
			tracker++;
			
			//price at depth d, is the unique price number d+1
			for(final Order order: book){
				if(order.getPrice() != price){
					tracker++;
					price = order.getPrice();
				}
				if(tracker == (depth+1)) {
					return order.getPrice();
				}
			}
			
			//return 0, if there is no price at this depth
			if(tracker < depth+1) {
				return 0.0;
			}
		}
		//return 0, is the book currently empty
		return 0.0;
	}

	/**
	 * Returns the price for a buy market order with a given volume.
	 * @param volume
	 * @return
	 */
	protected double getBidPriceAtVolume(final double volume){
		double remainingVolume = volume;
		double price = 0;
		int count = 0;
		while (count  < bidLimitOrders.size()) {
			double currentPrice  = this.getAskPriceAtDepth(count);
			double currentVolume = this.getAskVolumeAtPrice(currentPrice);
            if (currentVolume >= remainingVolume) {
            	return price += currentPrice * remainingVolume;
            }
            price += currentPrice * currentVolume;
            remainingVolume -= currentVolume;
            count++;
        }
		return price;
	}
	
	/**
	 * Returns the price for a sell market order with a given volume.
	 * @param volume
	 * @return
	 */
	protected double getAskPriceAtVolume(final double volume){
		double remainingVolume = volume;
		double price = 0;
		int count = 0;
		while (count  < askLimitOrders.size()) {
			double currentPrice  = this.getBidPriceAtDepth(count);
			double currentVolume = this.getBidVolumeAtPrice(currentPrice);
            if (currentVolume >= remainingVolume) {
            	return price += currentPrice * remainingVolume;
            }
            price += currentPrice * currentVolume;
            remainingVolume -= currentVolume;
            count++;
        }
		return price;
	}
	
	/**
	 * makes the instrument operate in synchronous matching mode. arriving orders will be inserted as active orders. matching is triggered by a scheduled event at time ...
	 */
	public void setSynchronous() {
		this.matchingMode = MatchingMode.SYNCHRONOUS;
	}

	protected boolean isSynchronous() {
		return this.matchingMode == MatchingMode.SYNCHRONOUS;
	}

	protected boolean isAsynchronous() {
		return this.matchingMode == MatchingMode.ASYNCHRONOUS;
	}

	/**
	 * makes the instrument operate in asynchronous matching mode (default). arriving orders will be checked for immediate matching.
	 */
	public void setAsynchronous() {
		this.matchingMode = MatchingMode.ASYNCHRONOUS;
	}

	/**
	 * returns the matching mode of the instrument.
	 * SYNCRONOUS: no immediate matching, matching is triggered by scheduled event at time ...
	 * ASYNCHRONOUS: immediate matching if possible
	 * @return matching mode
	 */
	public MatchingMode getMatchingMode() {
		return this.matchingMode;
	}


	/**
	 * matches orders in synchronous mode
	 */
	protected void matchOrder() {
		try {
			matchOrders();
			//synchronizedOrderMatch();
		} catch (final InstrumentException e) {
			e.printStackTrace();
		}
	}

	abstract protected void setupContract(Order o, Order curOrder, double quantity,
			double price);
	
	public void cancelAllOrders() {
		for (final Object o : getAskLimitOrders()) {
			final Order act = (Order) o;
			act.cancel();
		}
		
		for (final Object o : getBidLimitOrders()) {
			final Order act = (Order) o;
			act.cancel();
		}
		
		// These cannot be cancelled
		partiallyFilledOrders.clear();
	}

	/**
	 * returns the name of the instrument (ticker symbol)
	 * @return the {@link #tickerSymbol}
	 */
	public String getInstrumentName(){
		return this.tickerSymbol;
	}
	
	public void addListener(final InstrumentListener listener) {
	    this.listeners.add(listener);
	}
	 
	public void removeListener(final InstrumentListener listener) {
	    this.listeners.remove(listener);
	}
	
	protected void notifyListenersOfNewTrade(final Contract contract) {
	    for (final InstrumentListener instrumentListener: listeners) {
	    	instrumentListener.newContract(new InstrumentEvent(this, contract));
	    }
	}

	protected void notifyListenersOfOrderChange(final Order order) {
	    for (final InstrumentListener instrumentListener: listeners) {
	    	instrumentListener.orderUpdated(new InstrumentEvent(this, order));
	    }
	}

	public double getTotal_BidQuantity() {
		return total_BidQuantity;
	}

	public double getTotal_AskQuantity() {
		return total_AskQuantity;
	}
}
