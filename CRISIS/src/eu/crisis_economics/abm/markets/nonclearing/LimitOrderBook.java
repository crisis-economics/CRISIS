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

/**
 * @author  olaf
 */
public abstract class LimitOrderBook {
	
	private double bidPrice;
	private double askPrice;
	private double bidSideDepth;
	private double askSideDepth;

	public void addOrder(final LimitOrder order) {
		// TODO check for synchronous market 
		if (getRelativePrice(order)<=-getSpread())
		 {
			matching(order); // immediate matching
//		else if (getRelativePrice(order)>=0) super.addOrder(order); // TODO active order inside the book
//		else super.addOrder(order);                                 // TODO active order inside the spread
		}
	}
	
	private void matching(final LimitOrder order) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * The bid price is the highest stated price among active buy orders in the LOB.
	 * @return  the bidPrice
	 */
	public double getBidPrice() {
		return bidPrice;
	}

	/**
	 * For a given price, the bid-relative price is the difference between the bid price and the given price.
	 * It measures the distance of the given behind the bit price, i.e., how much smaller the given price is than the bit price.
	 * @param price
	 * @return bidRelativePrice
	 */
	public double getBidRelativePrice(final double price) {
		return getBidPrice()-price;
	}

	/**
	 * The ask price is the lowest stated price active among sell orders in the LOB.
	 * @return  the askPrice
	 */
	public double getAskPrice() {
		return askPrice;
	}

	/**
	 * For a given price, the ask-relative price is the difference between the given price and the ask price.
	 * It measures the distance of the given behind the ask price, i.e., how much larger the given price is than the ask price.
	 * @param price
	 * @return askRelativePrice
	 */
	public double getAskRelativePrice(final double price) {
		return price-getAskPrice();
	}

	/**
	 * For a given order, the relative price of the order is the ask-relative price for a sell order and the bid-relative price for a buy order.
	 * @param order
	 * @return relativePrice
	 */
	public double getRelativePrice(final LimitOrder order) {
		if (order.getSize()<0) {
			return getAskRelativePrice(order.getPrice()); // sell order
		}
		else {
			return getBidRelativePrice(order.getPrice());                   // buy order
		}
	}

	

	/**
	 * The bid-ask spread is the difference between ask price and bid price.
	 * @return the spread
	 */
	public double getSpread() {
		return getAskPrice()-getBidPrice();
	}

	/**
	 * The mid price is the average of the ask price and the bid price.
	 * @return the midPrice
	 */
	public double getMidPrice() {
		return (getAskPrice()+getBidPrice())/2;
	}

	/**
	 * The bid-side depth available at price {@code price} is the total size of all active buy orders at price {@code price}.
	 * @param price
	 * @return the bidSideDepth
	 */
	public double getBidSideDepth(final double price) {
		return bidSideDepth; // TODO sum of all sizes 
	}

	/**
	 * The ask-side depth available at price {@code price} is the total size of all active sell orders at price {@code price}.
	 * @return the askSideDepth
	 */
	public double getAskSideDepth(final double price) {
		return askSideDepth; // TODO sum of all sizes
	}

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
	public static final double TICKSIZE = 1.0;

	/**
	 * Time to sync the market. 0...no sync
	 */
	public static final int SYNCTIME = 0;

}
