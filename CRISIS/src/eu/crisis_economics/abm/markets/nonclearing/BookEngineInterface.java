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

/**
 * This interface is used by the Instrument class to handle all of the processing of books 
 * required by that class. A class implementing this interface must get access to all the books 
 * in the Instrument class, as these should only be updated by the class implementing this interface.
 * bidLimitOrders, askLimitOrders, filledOrders and partiallyFilledOrders are all passed to the constructor
 * implementing this interface - there is no way to enforce this in the interface, but any implementing 
 * class must conform to this policy.
 * Additionally BookEngine gets access to updatedOrders and notification containers, as it populates these with necessary 
 * updates.
 * @author olaf
 *
 */
public interface BookEngineInterface extends Serializable {

	/**
	 * Process a newly submitted order from a client. This method should either implement matching algorithms directly, 
	 * or should delegate these responsibilities to other methods, according to the matching 
	 * technique used in the implementation.
	 * @param o
	 */
	void processNewOrder(Order o);

	/**
	 * Process a cancellation of an exising order. Prior checks need to take place, such as checking 
	 * that this order belongs to the client cancelling it. Only orders that are still in either of 
	 * the order books can be cancelled.
	 * 
	 * @param o an <code>Order</code> object that is requested to be cancelled
	 * @return <code>Order</code> object that was cancelled, or <code>null</code> if order cannot be cancelled
	 */
	Order processCancelOrder(Order o);

	/**
	 * Insert a valid buy order into one of the order books. This method should be directly used by the 
	 * <code>processNewOrder(Order o)</code> method during processing of the order. The method should also be used 
	 * when books need to be populated manually during backfills. During the insertion process, a sorting 
	 * algorithm depending on the insertion priority must be used to ensure correct order positioning during matching.
	 * @param o
	 */
	void insertBuyOrder(Order o);

	/**
	 * Insert a valid sell order into one of the order books. This method should be directly used by the 
	 * <code>processNewOrder(Order o)</code> method during processing of the order. The method should also be used 
	 * when books need to be populated manually during backfills. During the insertion process, a sorting 
	 * algorithm depending on the insertion priority must be used to ensure correct order positioning during matching.
	 * @param o
	 */
	void insertSellOrder(Order o);

	@Deprecated
	void matchOrders() throws InstrumentException;
	@Deprecated
	void matchOrdersBestBid() throws InstrumentException;
	@Deprecated
	void matchOrdersInterbank() throws InstrumentException;

	void synchronizedOrderMatch() throws InstrumentException;

	
}