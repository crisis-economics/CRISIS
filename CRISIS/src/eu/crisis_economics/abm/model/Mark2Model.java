/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Bob De Caux
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
package eu.crisis_economics.abm.model;

import java.util.List;

import sim.engine.SimState;
import eu.crisis_economics.abm.bank.BadBank;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.DepositMarket;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;

public interface Mark2Model {

	public abstract void start();

	public abstract void step(SimState state);

	public abstract int getNumberOfBanks();

	public abstract int getNumberOfFirms();

	public abstract int getNumberOfHouseholds();

	public abstract DepositMarket getDepositMarket();

	public abstract CommercialLoanMarket getCommercialLoanMarket();

	public abstract InterbankLoanMarket getInterbankLoanMarket();

	public abstract StockMarket getStockMarket();

	public abstract List<Bank> getBanks();
	
	/**
	  * Get a list of {@link Bank} {@link Agent}{@code s} that are public deposit 
	  * holders. The resulting list will not contain reference to a {@link CentralBank},
	  * a {@link BadBank} or any shadowbank {@link Agent}.
	  */
	public abstract List<Bank> getRegularBanks();

	public abstract List<Firm> getFirms();
	
	public abstract List<Fund> getFunds();
	
	/**
	  * Get the government agent, if any, that exists in this model. At present
	  * multiple governments are not supported. If no government object is present 
	  * in the model, this method returns null. 
	  */
	public abstract Government getGovernment();
	
	/**
	  * Does the simulation have a government object?
	  */
	public abstract boolean hasGovernment();
	
    /**
      * Get the clearing house object, if any, that exists in this model. At
      * present multiple clearing houses are not supported. if no clearing house 
      * is present in the model, this method returns null. 
      */
    public ClearingHouse getClearingHouse();
    
    /**
      * Does the simulation have a clearing house object?
      */
    public boolean hasClearingHouse();

	public abstract List<Household> getHouseholds();
	
	public abstract CentralBank getCentralBank();
	
	public abstract BadBank getBadBank();

}