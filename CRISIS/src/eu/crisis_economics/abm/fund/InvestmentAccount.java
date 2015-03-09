/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.fund;

import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.Depositor;

class InvestmentAccount extends DepositAccount {

	public InvestmentAccount(InvestmentAccountHolder iDepositHolder,
			Depositor iDepositor, double amount) {
		super(iDepositHolder, iDepositor, amount, 0.0);
	}
	
	@Override
	public InvestmentAccountHolder getDepositHolder() {
		return((InvestmentAccountHolder)super.getDepositHolder());
	}
	
	@Override
	public double getValue() {
		return(super.getValue()*getDepositHolder().getEmissionPrice());
	}

	@Override
	public void setValue(double val) {
		super.setValue(val/getDepositHolder().getEmissionPrice());
	}
	
	@Override
	public double getFaceValue() {
		return(getValue());
	}

	@Override
	public void setFaceValue(double val) {
		setValue(val);
	}
	
	public double getNoOfShares() {
		return(super.getValue());
	}
	
	public void setNoOfShares(final double numberOfShares) {
	    super.setValue(numberOfShares);
	}
	
	// NB: No need to override withdraw/deposit here as they will use the overriden setValue
}
