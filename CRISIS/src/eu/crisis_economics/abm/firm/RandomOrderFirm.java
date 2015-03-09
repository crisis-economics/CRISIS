/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
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

import cern.jet.random.AbstractDistribution;
import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * This firm submits every time step market orders to the commercial loan market with maturity 10.
 * @author olaf
 *
 */
public class RandomOrderFirm extends RandomWalkFirm {

	private final AbstractDistribution randomSizeOfLoan;
	final private double meanSizeOfLoan = 0.0;
	final private double varianceSizeOfLoan = 0.25;
	final private int COMMERCIAL_LOAN_MATURITY = 10;
	private double loanSize;

   /**
     * Creates a firm that models the exogenous lending process, where the loan
     * size is drawn from the log-normal distribution using default parameter.
     * 
     * @param depositHolder
     * @param stockHolder
     * @param initialDeposit
     */
   public RandomOrderFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final int numberOfShares,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            nameGenerator
            );
      this.randomSizeOfLoan = new Normal(
         meanSizeOfLoan, varianceSizeOfLoan,
         new MersenneTwister(Simulation.getSimState().random.nextInt())
         );
   }
   
   /**
     * Creates a firm that models the exogenous lending process, where the loan
     * size is drawn from a given distribution.
     * 
     * @param depositHolder
     * @param stockHolder
     * @param initialDeposit
     * @param meanReversionLevel
     * @param meanReversionRate
     * @param volatility
     * @param randomShock
     * @param orderSizeDistribution
     */
   public RandomOrderFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final int numberOfShares,
      final double meanReversionLevel,
      final double meanReversionRate,
      final double volatility,
      final AbstractDistribution randomShock,
      final AbstractDistribution orderSizeDistribution,
      final AgentNameFactory nameGenerator
      ) {
      super(
         depositHolder,
         initialDeposit,
         stockHolder,
         numberOfShares,
         meanReversionLevel,
         meanReversionRate,
         volatility,
         randomShock,
         nameGenerator
         );
      this.randomSizeOfLoan = orderSizeDistribution;
   }

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.firm.StrategyFirm#considerCommercialLoanMarkets()
	 */
	@Override
	protected void considerCommercialLoanMarkets() {
		// first remove the current commercial orders, note that the cancel
		// method removes the order both from the market and this bank. 
		for (final Order order : new ArrayList<Order>(orders)) {
			if (order instanceof CommercialLoanOrder){
				((CommercialLoanOrder)order).cancel();
			}
		}
		
		this.loanSize = Math.log(Math.abs(randomSizeOfLoan.nextDouble())+1)*100; // log-normal

		
		for (final Market market : getMarketsOfType(CommercialLoanMarket.class)) {
			try {
				((CommercialLoanMarket) market).addOrder(this,
						COMMERCIAL_LOAN_MATURITY,
						-loanSize,	//buy order (borrower)
						0);				//market order
			} catch (final OrderException e) {
				e.printStackTrace();
				Simulation.getSimState().finish();
			} catch (AllocationException e) {
				e.printStackTrace();
				Simulation.getSimState().finish();
			}
		}		

	}

	public double getLoanSize() {
		return loanSize;
	}

}
