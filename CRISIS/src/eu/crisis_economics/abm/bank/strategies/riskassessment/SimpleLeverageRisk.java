/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Aurélien Vermeir
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
package eu.crisis_economics.abm.bank.strategies.riskassessment;

import eu.crisis_economics.abm.bank.Bank;

public class SimpleLeverageRisk implements BankRiskAssessment{
	private double threshold, risk;
	public SimpleLeverageRisk(){
		threshold = 0; //Hardcoded
		risk = 0.1; //Hardcoded
	}
	
	/**
	 * Riskiness of the bank increases if leverage is too high, decreases if too low.
	 * 
	 * @param  bank the ClearingBank that owns the strategy and will collect the result.
	 * @return the risk adjustment (in percent) considering the inherent riskiness of the ClearingBank.
	 */
	
	public double getAdjustment(final Bank bank){
		if (threshold == 0) threshold = 1.2 * bank.getLeverage();
		risk += (bank.getLeverage() > threshold) ? risk * 0.1 : - (risk * 0.1);
		//risk = 0.1; //there is something wrong with the above calculation? Sometimes risk adjusted rate is below 0
		return risk;
	}
}
