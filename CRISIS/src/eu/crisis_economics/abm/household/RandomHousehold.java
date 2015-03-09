/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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

import sim.engine.SimState;
import sim.engine.Steppable;
import cern.jet.random.AbstractDistribution;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;

/**
 * A random household implementation.
 * 
 * <p>
 * Creates a random amount that will be either deposited (if it is positive) or withdrawn (if it is negative) from associated
 * deposit account. The generated value can be adjusted by setting an <i>activity chance threshold</i>, a <i>multiplier</i>
 * and an <i>offset</i>. The household then does the following in each simulation step if it was scheduled:
 * </p>
 * 
 * <pre>
 * if ( activityChanceDistribution.nextDouble() < activityThreshold )
 *     amount = depositSizeDistribution.nextDouble() * multiplier + offset
 *     if ( amount < 0 )
 *         credit( amount )
 *     else
 *         debit( amount)
 * </pre>
 * 
 * <p>
 * The <i>multiplier</i> and <i>offset</i> values are required to provide flexibility for the given distribution. For
 * instance, setting <i>multiplier</i> to <i>100</i> and <i>offset</i> to <i>-50</i> results in an amount of
 * <code>[-50, 50]</code>.
 * </p>
 * 
 * <p>
 * We support all distributions included in the <a href="http://acs.lbl.gov/software/colt/">Colt library v1.2.0</a>. As an
 * overview, please refer to the <a
 * href="http://acs.lbl.gov/software/colt/api/cern/jet/random/package-tree.html">corresponding section</a> of the <a
 * href="http://acs.lbl.gov/software/colt/api/index.html">project documentation</a> to get an overview of the usable
 * distributions.
 * </p>
 * 
 * @author rlegendi
 * @since 1.0
 */
public class RandomHousehold
		extends DepositingHousehold
		implements Steppable {
	
	/** A default constant for interoperability. */
	private static final long serialVersionUID = 1L;
	
	/** The activity chance distribution. */
	protected final AbstractDistribution activityChanceDistribution;
	
	/** The deposit size distribution. */
	protected final AbstractDistribution depositSizeDistribution;
	
	/** The activity chance threshold. */
	private final double activityChanceThreshold;
	
	/** The random deposit multiplier. */
	private final double randomDepositMultiplier;
	
	/** The random deposit offset. */
	private final double randomDepositOffset;
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Instantiates a new random household.
	 * 
	 * @param depositHolder the deposit holder
	 * @param distributionForAllRandomEvents the distribution that will be used globally for all random events
	 */
	public RandomHousehold(final DepositHolder depositHolder, final AbstractDistribution distributionForAllRandomEvents) {
		this( depositHolder, 0.0, distributionForAllRandomEvents, distributionForAllRandomEvents );
	}
	
	/**
	 * Instantiates a new random household with a default deposit of <i>0</i>.
	 * 
	 * @param depositHolder the deposit holder
	 * @param distributionForAllRandomEvents the distribution that will be used globally for all random events
	 * @param activityChanceThreshold the activity chance threshold
	 * @param randomDepositMultiplier the random deposit multiplier
	 * @param randomDepositOffset the random deposit offset
	 */
	public RandomHousehold(final DepositHolder depositHolder, final AbstractDistribution distributionForAllRandomEvents,
			final double activityChanceThreshold, final double randomDepositMultiplier, final double randomDepositOffset) {
		this( depositHolder, 0.0, distributionForAllRandomEvents, distributionForAllRandomEvents, activityChanceThreshold,
				randomDepositMultiplier, randomDepositOffset );
	}
	
	/**
	 * Instantiates a new random household with a default deposit of <i>0</i>.
	 * 
	 * @param depositHolder the deposit holder
	 * @param activityChanceDistribution the activity chance distribution
	 * @param depositSizeDistribution the deposit size distribution
	 */
	public RandomHousehold(final DepositHolder depositHolder, final AbstractDistribution activityChanceDistribution,
			final AbstractDistribution depositSizeDistribution) {
		this( depositHolder, 0.0, activityChanceDistribution, depositSizeDistribution );
	}
	
	/**
	 * Instantiates a new random household which creates a deposit or a withdrawal with <i>50%</i> chance within the interval
	 * <code>[0, 100]</code>.
	 * 
	 * @param depositHolder the deposit holder
	 * @param initialDeposit the initial deposit
	 * @param activityChanceDistribution the deposit frequency distribution
	 * @param depositSizeDistribution the deposit size distribution
	 */
	public RandomHousehold(final DepositHolder depositHolder, final double initialDeposit,
			final AbstractDistribution activityChanceDistribution, final AbstractDistribution depositSizeDistribution) {
		this( depositHolder, initialDeposit, activityChanceDistribution, depositSizeDistribution, 0.5, 100.0, 0.0 );
	}
	
	/**
	 * Instantiates a new random household.
	 * 
	 * @param depositHolder the deposit holder
	 * @param initialDeposit the initial deposit
	 * @param activityChanceDistribution the deposit frequency distribution
	 * @param depositSizeDistribution the deposit size distribution
	 * @param randomDepositFrequency the random deposit frequency
	 * @param randomDepositMultiplier the random deposit multiplier
	 * @param randomDepositOffset the random deposit offset
	 */
	public RandomHousehold(final DepositHolder depositHolder, final double initialDeposit,
			final AbstractDistribution activityChanceDistribution, final AbstractDistribution depositSizeDistribution,
			final double randomDepositFrequency, final double randomDepositMultiplier, final double randomDepositOffset) {
		super( depositHolder, initialDeposit );
		
		this.activityChanceDistribution = activityChanceDistribution;
		this.depositSizeDistribution = depositSizeDistribution;
		
		this.activityChanceThreshold = randomDepositFrequency;
		this.randomDepositMultiplier = randomDepositMultiplier;
		this.randomDepositOffset = randomDepositOffset;
	}
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Determine random amount to deposit or withdraw.
	 * 
	 * @return see the class's description for the detailed description of the formulae
	 */
	private double determineRandomAmountToDepositOrWithdraw() {
		return depositSizeDistribution.nextDouble() * randomDepositMultiplier + randomDepositOffset;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.engine.Steppable#step(sim.engine.SimState)
	 */
	@Override
	public void step(final SimState state) {
		if ( activityChanceDistribution.nextDouble() < activityChanceThreshold ) {
			final double amount = determineRandomAmountToDepositOrWithdraw();
			
			if ( amount < 0 ) {
				try {
					credit( - amount );
				} catch (final InsufficientFundsException e) {
					// If there's not enough money, spend all of it
				    cashLedger.pull(this.getDepositValue());
				}
			} else {
				debit( amount );
			}
		}
	}
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.crisis_economics.abm.household.Household#toString()
	 */
	@Override
	public String toString() {
		return "RandomHousehold [activityChanceDistribution=" + activityChanceDistribution + ", depositSizeDistribution=" +
				depositSizeDistribution + ", depositChanceThreshold=" + activityChanceThreshold +
				", randomDepositMultiplier=" + randomDepositMultiplier + ", randomDepositOffset=" + randomDepositOffset +
				", toString()=" + super.toString() + "]";
	}
}
