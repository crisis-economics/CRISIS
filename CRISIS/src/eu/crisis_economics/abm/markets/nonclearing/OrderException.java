/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.markets.nonclearing;

public class OrderException
		extends Exception {
	
	private static final long serialVersionUID = 6815125200847788454L;
	
	public OrderException() {
		this( "unknown" );
	}
	
	/**
	 * Constructor receives some kind of message.
	 */
	public OrderException(final String err) {
		super( err ); // call super class constructor
	}
	
	// Note: I eliminated the instance variable since it is stored in Exception
	//       The message is accessible by getMessage()
}
