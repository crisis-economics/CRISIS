/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
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
package eu.crisis_economics.abm.markets.clearing;

import eu.crisis_economics.abm.contracts.CashAllocating;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.GoodHolder;
import eu.crisis_economics.abm.firm.MacroFirm;
import eu.crisis_economics.abm.household.MacroHousehold;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;

public class SimpleLabourMarketOrder  {
	private Party party;
	private SimpleLabourInstrument instrument;
	private double size;
	private Side side;
	private double price;
	private double openSize;
	
	public SimpleLabourMarketOrder(
	    Party party,
	    SimpleLabourInstrument instrument,
	    double size,
	    double price) throws OrderException {
		
		this.party = party;
		this.instrument = instrument;
		this.price = price;
		
		if (size > 0) {
			this.side=Side.SELL;  //sell
			this.size = size;
		}
		else {
		    this.side = Side.BUY; 
		    this.size = -size;//buy
	    } 
		
		this.openSize = this.size;
	}

	public enum Side { BUY, SELL }
	
	protected void disallocatePartyAsset() {
		if(side == Side.BUY) {
         ((CashAllocating)this.getParty()).disallocateCash(getOpenSize()*getPrice());
		} else {
			((Employee)this.getParty()).disallocateLabour(getOpenSize());
		}		
	}
	
	public Side getSide() {
		return side;
	}

	public double getOpenSize() {
		return openSize;
	}

	public void updateOpenSize(double volume) {
		this.openSize-=volume;
	}
	
	public void decrementOpenSize(double volume) {
	    openSize -= volume;
	}
	
	public double getSize() {
		return size;
	}
	
	public double getPrice() {
		return price;
	}

	public Party getParty() {
		return party;
	}

	public boolean isRegisteredSeller(Party party) {
		if ( null == party ) {
			throw new IllegalArgumentException( "party == null" );
		}
		
		if (party instanceof GoodHolder) return true; 
		else return false;
	}

	
	public boolean isRegisteredBuyer(Party party) {
		if ( null == party ) {
			throw new IllegalArgumentException( "party == null" );
		}
		
		if (party instanceof GoodHolder) return true; 
		else return false;
	}

	public void cancel() {
		disallocatePartyAsset();
		this.openSize = 0.0;
		if (party instanceof MacroFirm){
			((MacroFirm)party).removeOrder(this);
		}
		if (party instanceof MacroHousehold){
			((MacroHousehold) party).removeOrder(this);
		}
		
		instrument.discontinueOrderTracking(this);
	}

	


}
