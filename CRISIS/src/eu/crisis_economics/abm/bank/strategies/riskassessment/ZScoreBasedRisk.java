/*
 * This file is part of CRISIS, an economics simulator.
 * 
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

public class ZScoreBasedRisk implements BankRiskAssessment{
	private double t1,t2,t3,t4,t5;
	public ZScoreBasedRisk(){	}
	
	/**
	 * The riskiness of the ClearingBank is computed following the Z Altman Score formula.
	 * (See Altman E. & Al. (1968) & Altman E. & Al. (2000) ) 
	 *
	 * @param  bank the ClearingBank that owns the strategy and will collect the result.
	 * @return the risk adjustment (0% - 100%) considering the inherent riskiness of the ClearingBank.
	 */
	
	public double getAdjustment(final Bank bank){
		//TODO : implement those methods in ClearingBank or in Bank class to have ZScore working.
		/*
		t1 = bank.getWorkingCapital() / bank.getTotalAssets();
		t2 = bank.getRetainedEranings() / bank.getTotalAssets();
		t3 = bank.getEBIT() / bank.getTotalAssets();
		t4 = bank.getEquity() / bank.getTotalLiabilities();
		t5 = bank.getTurnover / bank.getTotalAssets();
		*/
	
		return 1.2*t1 + 1.4*t2 + 3.3*t3 + 0.6*t4 + 0.99*t5;
	}
}