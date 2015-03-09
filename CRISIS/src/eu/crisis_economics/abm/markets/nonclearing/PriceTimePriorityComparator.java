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

import java.util.Comparator;

/**
 * @author olaf
 */
public class PriceTimePriorityComparator implements Comparator<Order> {

	public PriceTimePriorityComparator() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(final Order order1, final Order order2) {
		
		//For a buy order, the highest price at the top position
		if(order1.getSide() == Order.Side.BUY) {
			return compareOrder(order1, order2, -1);
		}
		//For a sell order, the lowest price at the top position
		return compareOrder(order1, order2, 1);
		
	}

	private int compareOrder(final Order order1, final Order order2, final int i) {
		//compares current order price with another order price
		int priceComp = ((Double)order1.getPrice()).compareTo(order2.getPrice());
		
		//If both prices are equal, we need to sort according to their entry time
		if(priceComp == 0){
			
			final int timeComp =((Long)order2.getEntryTime()).compareTo(order1.getEntryTime());
			
			priceComp = timeComp;
		}
		
		// last resort
		if (priceComp == 0){
			priceComp = order1.equals(order2) ? 0 : 1;
		}
		/*
		 * since the sorting order for the buy and sell order books is different
		 * we need to ensure that orders are sorted correctly.
		 * buy order book - highest buy price at the top position
		 * sell order book - lowest sell price at the top position
		 * sortingOrder will helps to do this ranking
		 * a value of -1 sorts orders in descending order of price and ascending order of time
		 * a value of 1 sorts orders in ascending order of price and ascending order of time
		 */
		
		return priceComp*i;
	}

}
