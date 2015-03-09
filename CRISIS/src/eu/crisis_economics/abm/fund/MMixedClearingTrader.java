/*
 * This file is part of CRISIS, an economics simulator.
 * 
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

import eu.crisis_economics.abm.IAgent;
import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;
import eu.crisis_economics.abm.strategy.leverage.ConstantTargetLeverageAlgorithm;
import eu.crisis_economics.abm.strategy.leverage.LeverageTargetAlgorithm;

class MMixedClearingTrader {

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Constructors
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public <T extends IAgent> MMixedClearingTrader(T parent, Portfolio portfolio, double fixedTargetLeverage) {
		iPortfolio 		= portfolio;
		iAgent			= parent;
		targetLeverageAlgorithm = new ConstantTargetLeverageAlgorithm(fixedTargetLeverage);
		// TODO: update VaRConstraint leverage algorithm to work with IPortfolio
//      Simulation.repeat(
 //        targetLeverageAlgorithm, "updateLeverageTarget", SimulationCycleOrdering.PRODUCTION);
	}    
    
    
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper functions
	////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * returns the target leverage used to control portfolio size.
	 * @return target leverage
	 */
	public double getTargetLeverage() {
		return targetLeverageAlgorithm.getTargetLeverage();
	}
	
	public void setTargetLeverage(final double targetLeverage){
		targetLeverageAlgorithm.setTargetLeverage(targetLeverage);
	}
	
	/**
	 * target portfolio (illiquid asset) size is (equity of the investor) times target leverage of the strategy.
	 * @return target portfolio size
	 */
    protected double getTargetPortfolioSize() {
       double equity = iAgent.getEquity();
       final double targetLeverage = targetLeverageAlgorithm.getTargetLeverage();
       return equity * targetLeverage;
    }

    protected String getLoanInstrumentTickerSymbol(int maturity) {
		return "LOAN"+new Integer(maturity).toString();
	}

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Variables
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected IAgent		  	iAgent;
	protected Portfolio	  	iPortfolio;

    protected LeverageTargetAlgorithm targetLeverageAlgorithm;// Leverage algorithm object


}
