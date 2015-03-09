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
import java.util.Date;
import java.util.Iterator;

import sim.util.Bag;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * a simple market order
 * @author  olaf
 */
// TODO Consider implementing hashCode()/equals()
public abstract class Order {
	private final Party party;
	private final Instrument instrument;
	private double size;
	private double executedSize;
	private double openSize;
	
	private double price;
	private int timeout;
	private Bag target;
	
	/**
	 * This reference should be kept final since matching may take place during
	 * initialization in the constructor. If the filter is set only afterwards,
	 * it might cause confusion.
	 */
	private final Filter filter;
	
	/**
	 * @author   olaf
	 */
	public enum Side{
	BUY,
	SELL}
	
	/**
	 * @author   olaf
	 */
	public enum Type{
	LIMIT,
	MARKET}
	
	/**
	 * @author   olaf
	 */
	public enum Status{
	NOT_PROCESSED,
	CANCELLED,
	PARTIALLY_FILLED,
	FILLED,
	REJECTED}

	private final Side side;
	private final Type type;
	private Status status;
//	private final UUID orderID;
	private final long entryTime;
	
	private final Bag trades;
	/**
	 * A class to hold an order fill for this order only
	 * @author  olaf
	 */
	private class OrderTrade implements Serializable{
		
		private static final long serialVersionUID = 1L;
		double volume;
		double tradePrice;
		long tradeTime;
		
		protected OrderTrade(final double volume, final double price, final long tradeTime){
			this.volume = volume;
			this.tradePrice = price;
			this.tradeTime = tradeTime;
		}
		
		/**
		 * @return
		 */
		public double getVolume(){
			return volume;
		}
		/**
		 * @return
		 */
		public double getTradePrice(){
			return tradePrice;
		}
//		/**
//		 * @return
//		 */
//		public long getTradeTime(){
//			return tradeTime;
//		}
		
		@Override
		public String toString(){
			return volume + " @$" + tradePrice + " : " + new Date(tradeTime);
		}
	}

	/**
	 * Creates an order object for the instrument {@code instrument}.
	 * <ul>
	 * <li>If {@code size} > 0, the order is a commitment to sell abs({@code size}) units at the price no less then {@code price}. </li>
	 * <li>If {@code size} < 0, the order is a commitment to buy abs({@code size}) units at the price no more then {@code price}. </li>
	 * <li>If {@code price} > 0, the order is a limit order. </li>
	 * <li>If {@code price} = 0, the order is a market order. </li>
	 * </ul>
	 * @param party the market party placing the order (Seller or Buyer)
	 * @param instrument investment instrument
	 * @param size the quantity to order
	 * @param price limit price
	 * @throws OrderException unregistered market party
	 */
	public Order (final Party party, final Instrument instrument, final double size, final double price) throws OrderException {
		this(party, instrument, size, price, DefaultFilters.any());
	}
	
	public Order (final Party party, final Instrument instrument, final double size, final double price, final Filter filter) throws OrderException {

		if ( null == filter ) {
			throw new IllegalArgumentException( "filter == null" );
		}
		
		this.party  = party;
		this.instrument = instrument;
		this.filter = filter;

		// TODO These should be IllegalArgumentExceptions, no?
		if (size>0) {
			if (!isRegisteredSeller(party)) {
				throw new OrderException("unregistered seller");
			}
			side=Side.SELL;
			this.size   = size;
		}
		else if (size<0) {
			if (!isRegisteredBuyer(party)) {
				throw new OrderException("unregistered buyer");
			}
			side=Side.BUY;
			this.size   = -size;
		} else {
			throw new OrderException("nothing to sell/buy");
		}
		this.openSize = this.size;
		this.executedSize = 0.0;

		if (price>0) {
			type=Type.LIMIT;
			this.price  = price;
		}
		else if (price<0) {
			throw new OrderException("price<0");
		} else {
			type=Type.MARKET;
			this.price  = price;
		}

//		this.orderID = UUID.randomUUID();
		this.status = Status.NOT_PROCESSED;
		this.entryTime = Simulation.getCycleIndex();//System.nanoTime();	// TODO needs to be simulation time, but it breaks tests
		this.trades = new Bag();
	
		this.setTimeout(DEFAULTTIMEOUT);
		this.setTarget(new Bag());
		this.party.addOrder(this);
		this.instrument.processNewOrder(this);
	}
	
//	/**
//	 * Creates an order object for the market {@code market}.
//	 * If {@code size} > 0, the order is a commitment to sell abs({@code size}) units at the price no less then {@code price}. 
//	 * If {@code size} < 0, the order is a commitment to buy abs({@code size}) units at the price no more then {@code price}. 
//	 * <p>
//	 * The order will be canceled after {@code maturity} time steps if no market matching, cyclic market clearing 
//	 * or owner cancel has happened before. 
//	 * <p>
//	 * Note: if {@code maturity}=0, no maturity event will be scheduled.
//	 * <p>
//	 * {@code target} sets a set of market parties for which the order is targeted for
//	 * <p>
//	 * Note: An empty set is a global order.
//	 * @param party
//	 * @param market
//	 * @param size
//	 * @param price
//	 * @param maturity
//	 * @param target
//	 * @throws OrderException 
//	 */
//	public Order (Party party, Market market, double size, double price, int maturity, Bag target) throws OrderException {
//		if (size>0)
//			if (!market.isRegisteredSeller(party)) throw new OrderException("unregistered seller");
//		else if (size<0) 
//			if (!market.isRegisteredBuyer(party)) throw new OrderException("unregistered buyer");
//		else throw new OrderException("nothing to sell/buy");
//		this.party  = party;
//		this.market = market;
//		this.size   = size;
//		this.price  = price;
//		this.setMaturity(maturity);
//		this.setTarget(target);
//		this.party.addOrder(this);
//		this.market.addOrder(this);
//	}
	
	/**
	 * Order may be removed by the seller or buyer party, by the order (default or specified maturity), or by the market (cyclic market clearing).
	 */
	public void cancel() {
		
		disallocatePartyAsset();
		this.openSize = 0.0;
		party.removeOrder(this);
		
		instrument.processCancelOrder(this);
	}

	/**
	 * This method should be overwritten by orders for the various markets in order to release blocked assets. 
	 * It is usually called when order is canceled. Disallocation after matching is done in the instrument.
	 */
	abstract protected void disallocatePartyAsset();

	/**
	 * updates order's state once an order is submitted, it's total volume stays constant, only openSize and executedSize can change through this method
	 * @param volume
	 * @param price
	 */
	protected void execute(final double volume, final double price){
		final OrderTrade trade = new OrderTrade(volume, price, System.currentTimeMillis()); // TODO use simulation time
		this.trades.add(trade);
		this.openSize -= volume;
		this.executedSize += volume;
		this.party.updateState(this);
		this.instrument.notifyListenersOfOrderChange(this);
	}

	/**
	 * @return  the market party
	 */
	public Party getParty() {
		return party;
	}

	/**
	 * @return  the market
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	/**
	 * @return  size
	 */
	public double getSize() {
		return size;
	}

	/**
	 * @return  the price
	 */
	public double getPrice() {
		return price;
	}
	
	/**
	 * @return  the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status  the status to set
	 */
	protected void setStatus(final Status status) {
		this.status = status;
	}

//	/**
//	 * @return  the orderID
//	 */
//	public UUID getOrderID() {
//		return orderID;
//	}

	/**
	 * @return  the entryTime
	 */
	public long getEntryTime() {
		return entryTime;
	}

	/**
	 * @return  the side
	 */
	public Side getSide() {
		return side;
	}

	/**
	 * @return  the executedSize
	 */
	public double getExecutedSize() {
		return executedSize;
	}

	/**
	 * @return  the openSize
	 */
	public double getOpenSize() {
		return openSize;
	}

	/**
	 * @return  the type
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Returns the average price executed on this order. If no trades has been done, it returns 0
	 */
	public double getAverageExecutedPrice(){
		double avgPrice = 0.0;
		double volume = 0.0;
		
		if(almostEqual(trades.size(),0,EPSILON)) {
			return 0.0;
		}
		
		for(int i = 0; i < trades.size(); i++) {
			final OrderTrade trade = (OrderTrade) trades.get(i);
			avgPrice += (trade.getTradePrice()*trade.getVolume());
			volume += trade.getVolume();
		}

		return avgPrice/volume;
		
	}
	
	public double getLastExecutedPrice(){
		return ((OrderTrade)trades.get(trades.size()-1)).getTradePrice();
	}
	
	public double getLastExecutedVolume(){
		return ((OrderTrade)trades.get(trades.size()-1)).getVolume();
	}
	
	@Override
	public String toString(){
		return this.instrument.getTickerSymbol() + " (instrument) " 
				+ this.size + " (size) " 
				+ this.openSize + " (openSize) " 
				+ this.executedSize + " (executedSize) " 
				+ this.side + " (side) " 
				+ this.type + " (type) " 
				+ this.status + " (status) "
				+ this.price + " (price) "
				; 
		
	}
	
	public int getNumberOfTrades(){
		return trades.size();
	}
	
	public void printTrades(){
		for (@SuppressWarnings("unchecked")
		final
		Iterator<OrderTrade> iterTrades = trades.iterator(); iterTrades.hasNext();) {
			System.out.println(iterTrades.next().toString());
		}
	}

	protected boolean isFilled(){
		return almostEqual(executedSize,size,EPSILON);
	}
	
	/**
	 * Floating-point numbers may end up being slightly imprecise due to rounding. This usually can be ignored. 
	 * However, it also means that numbers expected to be equal often differ slightly, and a simple equality test 
	 * fails. The solution is to check whether the difference of the numbers is very small. The error margin that the 
	 * difference is compared to is given by the parameter epsilon.  
	 * @param a number for equality test
	 * @param b number for equality test
	 * @param epsilon error margin
	 */
	// TODO This should be a common static utility function
	public static boolean almostEqual(final double a, final double b, final double epsilon)
	{
	    final double absA = Math.abs(a);
	    final double absB = Math.abs(b);
	    final double diff = Math.abs(a - b);

	    if (a == b) { // shortcut, handles infinities
	        return true;
	    } else if (a * b == 0) { // a or b or both are zero
	        // relative error is not meaningful here
	        return diff < (epsilon * epsilon);
	    } else { // use relative error
	        return diff / (absA + absB) < epsilon;
	    }
	}
	
	/**
	 * default error margin for floating-point number equality test
	 */
	public static final double EPSILON = 0.00001;
	
	protected boolean isClosed(){
		return almostEqual(openSize,0,EPSILON);
	}

	/**
	 * @return  trades
	 */
	protected Bag getTrades() {
		return new Bag(trades);
	}

	/**
	 * The order will be canceled after returned number of time steps if no market matching, cyclic market clearing  or owner cancel has happened before.  <p> Note: if  {@code  return} =0, no maturity event is scheduled.
	 * @return  the maturity
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * The order will be canceled after {@code  maturity}  time steps if no market matching, cyclic market clearing  or owner cancel has happened before.  <p> Note: if  {@code  maturity} =0, no maturity event will be scheduled.
	 */
	private void setTimeout(final int timeout) {
		this.timeout = timeout;
		// TODO schedule maturity
	}


	/**
	 * returns the order target, a set of market parties for which the order is addressed. Note <p> Note: An empty set is a global order.
	 * @return  the target
	 */
	public Bag getTarget() {
		return target;
	}


	/**
	 * sets a set of market parties for which the order is targeted for <p> Note: An empty set is a global order.
	 * @param target  the target to set
	 */
	private void setTarget(final Bag target) {
		this.target = target;
	}

	/**
	 * default maturity of the order in time steps. (0 - no maturity)
	 */
	public static final int DEFAULTTIMEOUT = 0;
	
	/**
	 * Returns true if the party implements the appropriate contractual (sell) interface for the target market type.
	 * This is known in the market type: e.g. for LoanMarket party must have implemented Lender.
	 * 
	 * @param party
	 */
	abstract public boolean isRegisteredSeller(Party party);

	/**
	 * Returns true if the party implements the appropriate contractual (buy) interface for the target market type.
	 * This is known in the market type: e.g. for LoanMarket party must have implemented Borrower.
	 * 
	 * @param party
	 */
	abstract public boolean isRegisteredBuyer(Party party);

	public void setPrice(final double price) {
		this.price = price;
	}
	
	// Do NOT modify!
	@SuppressWarnings("unused")
	private final Filter setFilter(final Filter filter) {
		throw new AssertionError("Filter cannot be reset: matching performed in the constructor.");
	}

	public Filter getFilter() {
		return filter;
	}

	public boolean accepts(final Order other) {
		return filter.matches( other );
	}
	
}
