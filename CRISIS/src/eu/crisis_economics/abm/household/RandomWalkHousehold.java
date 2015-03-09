/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
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
package eu.crisis_economics.abm.household;

import cern.jet.random.AbstractDistribution;
import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * This household models the exogenous deposit process as mean-reverting random walk.
 * Many financial or economic processes can be modeled as mean-reverting random walks. 
 * Mean-reverting walks differ from simple diffusion by the addition of a central expectation, 
 * usually growing with time, and a restoring force that pulls subsequent values toward that 
 * expectation. The random term is logarithmically distributed, and the initial value of the 
 * mean can be above or below the series start. The strength of the restoring force is given 
 * in terms of the time to return to the mean.
 * The change in deposits is the sum of a mean reversion term and a random term:
 * <p> 
 * S_{t+1} - S_t = /alpha ( S^* - S_t) + /sigma /varepsilon_t 
 * <p>
 * where S_t is the current state of the household in terms of deposit. 
 * <p>
 * S_{t+1} is the next increment of the mean reverting random walk - the new deposit. The 
 * expected values are calculated by omitting the random term.
 * <p>
 * This function modifies the following variables:
 * <ul>
 * <li>this.expectedValueOfDepositChange
 * <li>this.expectedValueOfDeposit
 * <li>this.changeInDeposit
 * <li>deposit value of the associated account
 * </ul>
 * 
 * @author olaf
 *
 */
public class RandomWalkHousehold extends StrategicHousehold {

	private double expectedValueOfDepositChange = 0;
	private double expectedValueOfDeposit = 0;
	private double changeInDeposit = 0;
	private double meanReversionLevelOfDeposit = 1;
	private double meanReversionRateOfDeposit = 0.01;
	private double volatilityOfDeposit = 0.01;
	private final AbstractDistribution randomShockOfDeposit;


	/**
	 * Creates a household that models the exogenous deposit process as mean-reverting random walk using default parameter.
	 * @param depositHolder
	 * @param initialDeposit
	 */
	public RandomWalkHousehold(final DepositHolder depositHolder,
			final double initialDeposit) {
		super(depositHolder, initialDeposit);
		this.randomShockOfDeposit=new Normal(0.0,1.0, new MersenneTwister(Simulation.getSimState().random.nextInt())); //TODO consider using Simulation.getSimState().random
		this.meanReversionLevelOfDeposit=initialDeposit;
		this.volatilityOfDeposit=this.volatilityOfDeposit*initialDeposit;
	}

	/**
	 * Creates a household that models the exogenous deposit process as mean-reverting random walk using the given parameter below.
	 * @param depositHolder
	 * @param initialDeposit
     * @param meanReversionLevel S^*: the long-run equilibrium of deposits.
     * @param meanReversionRate /alpha: the speed at which deposits revert. The mean reversion 
     *                          rate is always greater than, or equal to, zero and higher numbers 
     *                          correspond to faster mean reversion.
     * @param volatility /sigma: the expected future variability of deposit over time.
     * @param randomShock /varepsilon_t
	 */
	public RandomWalkHousehold(final DepositHolder depositHolder, final double initialDeposit, final double meanReversionLevel, final double meanReversionRate, final double volatility, final AbstractDistribution randomShock) {
		super(depositHolder, initialDeposit);
		this.meanReversionLevelOfDeposit=meanReversionLevel;
		this.meanReversionRateOfDeposit=meanReversionRate;
		this.volatilityOfDeposit=volatility;
		this.randomShockOfDeposit=randomShock;
	} 

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.household.StrategicHousehold#considerDepositPayment()
	 */
	@Override
	protected void considerDepositPayment() {
		final double deposit = this.getDepositValue();
        final double newDeposit = deposit + meanReversionRateOfDeposit * (meanReversionLevelOfDeposit - deposit) + volatilityOfDeposit * randomShockOfDeposit.nextDouble();
        this.expectedValueOfDepositChange = meanReversionRateOfDeposit * (meanReversionLevelOfDeposit - deposit);
        this.expectedValueOfDeposit = deposit + meanReversionRateOfDeposit * (meanReversionLevelOfDeposit - deposit);
        this.changeInDeposit = newDeposit - deposit;

		if ( this.changeInDeposit < 0 ) {
			try {
				credit( - this.changeInDeposit );
			} catch (final InsufficientFundsException e) {
				// If there's not enough money, spend all of it
			    cashLedger.pull(this.getDepositValue());
			}
		} else {
			debit( this.changeInDeposit );
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.crisis_economics.abm.household.Household#toString()
	 */
	@Override
	public String toString() {
		return "RandomWalkHousehold [expectedValueOfDepositChange=" + expectedValueOfDepositChange + ", expectedValueOfDeposit=" +
				expectedValueOfDeposit + ", changeInDeposit=" + changeInDeposit + ", toString()=" + super.toString() + "]";
	}

	public double getMeanReversionLevelOfDeposit() {
		return meanReversionLevelOfDeposit;
	}

	public void setMeanReversionLevelOfDeposit(final double meanReversionLevel) {
		this.meanReversionLevelOfDeposit = meanReversionLevel;
	}

	public double getMeanReversionRateOfDeposit() {
		return meanReversionRateOfDeposit;
	}

	public void setMeanReversionRateOfDeposit(final double meanReversionRate) {
		this.meanReversionRateOfDeposit = meanReversionRate;
	}

	public double getVolatilityOfDeposit() {
		return volatilityOfDeposit;
	}

	public void setVolatilityOfDeposit(final double volatility) {
		this.volatilityOfDeposit = volatility;
	}

	public double getExpectedValueOfDepositChange() {
		return expectedValueOfDepositChange;
	}

	public double getExpectedValueOfDeposit() {
		return expectedValueOfDeposit;
	}

	public double getChangeInDeposit() {
		return changeInDeposit;
	}
}
