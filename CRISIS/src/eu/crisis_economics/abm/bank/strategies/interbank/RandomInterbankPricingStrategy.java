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

import java.util.Random;

import eu.crisis_economics.abm.bank.ClearingBank;

public class RandomInterbankPricingStrategy implements InterbankPricingStrategy{
	Random random;
	public RandomInterbankPricingStrategy(){
		random = new Random(1); //if we use simstate random here, we cannot compare interbank output to no-interbank, 
		//as the ordering of random values gets messed up
	}
	
	/**
	 * To be more competitive on the IBMarket, the StrategyBank adds an attractive premium to
	 * the borrowing rate. Here, it is a random number between 0% and 100% of the spread between
	 * the central bank deposit rate and lending rate.
	 *
	 * @param  bank the StrategyBank that owns the strategy and will collect the result.
	 * @return the "commercial" premium that the StrategyBank is willing to add on the of the borrowing rate
	 */
	public double getLenderPremium(ClearingBank bank){ return random.nextDouble(); }
	public double getBorrowerPremium(ClearingBank bank){ return random.nextDouble(); }

}
