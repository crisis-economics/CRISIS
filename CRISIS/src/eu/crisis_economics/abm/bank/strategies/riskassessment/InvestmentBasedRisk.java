/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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

public class InvestmentBasedRisk implements BankRiskAssessment{
	private double threshold, risk;
	public InvestmentBasedRisk(){
		threshold = 5; //Hardcoded
		risk = 0.1; //Hardcoded
	}
	
	/**
	 * Riskiness of the bank increases if expected losses are too high, and decreases if too low.
	 *
	 * @param  bank the ClearingBank that owns the strategy and will collect the result.
	 * @return the risk adjustment (0% - 100%) considering the inherent riskiness of the ClearingBank.
	 */
	
	public double getAdjustment(final Bank bank){
		//risk += (bank.getExpectedLosses() > threshold) ? risk * 0.1 : - (risk * 0.1);
		return risk;
	}
}