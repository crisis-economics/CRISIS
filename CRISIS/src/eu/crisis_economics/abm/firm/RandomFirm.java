/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 Marotta Luca
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
package eu.crisis_economics.abm.firm;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import cern.jet.random.AbstractDistribution;
import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * A simple firm that can borrow a random amount of money from banks at random time steps
 * 
 * @author Luca Marotta
 */
public class RandomFirm extends Firm implements Borrower, Steppable {
	private static final long serialVersionUID = 1L;
	
	private static final int COMMERCIAL_LOAN_MATURITY = 1;
	
	/**
	 * The random distribution the submitted orders to loan market are drawn from.
	 */
	protected final AbstractDistribution loanSizeDistribution;
	
	/**
	 * The random distribution the steps of order submission are drawn from.
	 */
	protected final AbstractDistribution loanFrequencyDistribution;
	private final double loanChanceThreshold;
	
	/**
	 * The random distribution the submitted order prices (i. e. interest rates, in this case) are drawn.
	 */
	protected AbstractDistribution orderPriceDistribution;
	
	/**
	 * The random distribution deposits are drawn from.
	 */
	protected final AbstractDistribution depositFrequencyDistribution;
	
	/**
	 * The random distribution deposit size drawn from.
	 */
	protected final AbstractDistribution depositSizeDistribution;
	
	private final double depositChanceThreshold;
	private final double randomDepositMultiplier;
	
	private final double randomDepositOffset;

	//---------------------------------------------------------------------------------------
	
   public RandomFirm(
      final DepositHolder depositHolder,
      final AbstractDistribution distributionForAllRandomEvents,
      final AgentNameFactory nameGenerator
      ) {
      this(
         depositHolder,
         distributionForAllRandomEvents,
         distributionForAllRandomEvents,
         distributionForAllRandomEvents,
         0,
         distributionForAllRandomEvents,
         distributionForAllRandomEvents,
         nameGenerator
         );
   }
   
   public RandomFirm(
      final DepositHolder depositHolder,
      final AbstractDistribution loanSizeDistribution,
      final AbstractDistribution loanFrequencyDistribution,
      final AbstractDistribution orderPriceDistribution,
      final AbstractDistribution depositFrequencyDistribution,
      final AbstractDistribution depositSizeDistribution,
      final AgentNameFactory nameGenerator
      ) {
      this(
         depositHolder,
         loanSizeDistribution,
         loanFrequencyDistribution,
         orderPriceDistribution,
         0,
         depositFrequencyDistribution,
         depositSizeDistribution,
         nameGenerator
         );
   }
   
   public RandomFirm(
      final DepositHolder depositHolder,
      final AbstractDistribution loanSizeDistribution,
      final AbstractDistribution loanFrequencyDistribution,
      final AbstractDistribution orderPriceDistribution,
      final double initialDeposit,
      final AbstractDistribution depositFrequencyDistribution,
      final AbstractDistribution depositSizeDistribution,
      final AgentNameFactory nameGenerator
      ) {
      this(
         depositHolder,
         loanSizeDistribution,
         loanFrequencyDistribution,
         orderPriceDistribution,
         0.5,
         initialDeposit,
         depositFrequencyDistribution,
         depositSizeDistribution,
         0.5,
         100,
         0,
         nameGenerator
         );
   }
   
   public RandomFirm(
      final DepositHolder depositHolder,
      final AbstractDistribution loanSizeDistribution,
      final AbstractDistribution loanFrequencyDistribution,
      final AbstractDistribution orderPriceDistribution,
      final double randomLoanFrequency,
      final double initialDeposit,
      final AbstractDistribution depositFrequencyDistribution,
      final AbstractDistribution depositSizeDistribution,
      final double randomDepositFrequency,
      final double randomDepositMultiplier,
      final double randomDepositOffset,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder, initialDeposit, nameGenerator);
    	
    	//initialize loan variables
    	this.loanSizeDistribution = loanSizeDistribution;
    	this.loanFrequencyDistribution = loanFrequencyDistribution;
    	this.loanChanceThreshold = randomLoanFrequency;
    	this.orderPriceDistribution = orderPriceDistribution; 
    	
    	//initialize deposit variables
		this.depositFrequencyDistribution = depositFrequencyDistribution;
		this.depositSizeDistribution = depositSizeDistribution;
		
		this.depositChanceThreshold = randomDepositFrequency;
		this.randomDepositMultiplier = randomDepositMultiplier;
		this.randomDepositOffset = randomDepositOffset;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see sim.engine.Steppable#step(sim.engine.SimState)
	 */
	@Override
	public void step(final SimState state) {
		// setting up random deposits
		if ( depositFrequencyDistribution.nextDouble() < depositChanceThreshold ) {
			final double amount = depositSizeDistribution.nextDouble() * randomDepositMultiplier + randomDepositOffset;

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
		
		// setting up random loan requests
		if ( loanFrequencyDistribution.nextDouble() > loanChanceThreshold ) {
			this.considerCommercialLoanMarkets();
		}
	}
    
	public void considerCommercialLoanMarkets() {		
		// first remove the current commercial orders, note that the cancel
		// method removes the order both from the market and this firm. 
		for (final Order order : new ArrayList<Order>(orders)) {
			if (order instanceof CommercialLoanOrder){
				((CommercialLoanOrder)order).cancel();
			}
		}
		
		for (final Market market : getMarketsOfType(CommercialLoanMarket.class)) {
			try {
				((CommercialLoanMarket) market).addOrder(this,
						COMMERCIAL_LOAN_MATURITY,
						-Math.abs(loanSizeDistribution.nextDouble()),
						orderPriceDistribution.nextDouble());
			} catch (final OrderException e) {
				e.printStackTrace();
				Simulation.getSimState().finish();
			} catch (AllocationException e) {
				e.printStackTrace();
				Simulation.getSimState().finish();
			}
		}		
	}}
