/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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
package eu.crisis_economics.abm.bank.strategies.interbank;

import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.simulation.Simulation;

public class ReactiveInterbankPricingStrategy implements InterbankPricingStrategy{
	private double premium;
	public ReactiveInterbankPricingStrategy(){
		premium = Simulation.getSimState().random.nextDouble();		
	}
	
	/**
	 * To be more competitive on the IBMarket, the StrategyBank adds an attractive premium to
	 * the borrowing rate. Here, it adjusts the premium of the previous time-step :
	 * - increasing it if the bank could not borrow on the interbank market,
	 * - lowering it if the bank was able to borrow on the interbank market,
	 * by a relative variation.
	 *
	 * @param  bank the StrategyBank that owns the strategy and will collect the result.
	 * @return the "commercial" premium that the StrategyBank is willing to add on the of the borrowing rate
	 */
	
	public double getLenderPremium(ClearingBank bank){
		premium += (bank.hasIBLended()) ? premium * 0.1 : - (premium * 0.1);
		premium = Math.min(0,Math.max(premium,1));
		return premium;	
	}
	public double getBorrowerPremium(ClearingBank bank){
		premium += (bank.hasIBBorrowed()) ? premium * 0.1 : - (premium * 0.1);
		premium = Math.min(0,Math.max(premium,1));
		return premium;	
	}
}
