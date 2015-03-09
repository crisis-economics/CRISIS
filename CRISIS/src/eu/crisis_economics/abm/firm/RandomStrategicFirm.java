/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Fabio Caccioli
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
 * A firm that randomly borrows at the commercial loan market.
 * @author olaf
 *
 */
public class RandomStrategicFirm extends StrategyFirm implements StrategyFirmInterface {
	
	private static final int COMMERCIAL_LOAN_MATURITY = 1;

	/**
	 * The random distribution the submitted order sizes are drawn.
	 */
	protected AbstractDistribution orderSizeDistribution;
	
	/**
	 * The random distribution the submitted order prices are drawn.
	 */
	protected AbstractDistribution orderPriceDistribution;

	private double lendingRate;

	/**
	 * Creates a firm that acts randomly on the commercial loan market, sampling from a given distribution for loan size and interest.
	 * @param depositHolder
	 * @param stockHolder
	 * @param initialDeposit
	 * @param orderSizeDistribution
	 * @param orderPriceDistribution
	 * @param name
	 *        A <i>uniquely identifying</i> name for this {@link Agent}.
	 */
   public RandomStrategicFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final int numberOfShares,
      final AbstractDistribution orderSizeDistribution,
      final AbstractDistribution orderPriceDistribution,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            nameGenerator
            );
      this.orderSizeDistribution = orderSizeDistribution;
      this.orderPriceDistribution = orderPriceDistribution;
   }

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.firm.StrategyFirmInterface#considerCommercialLoanMarkets()
	 */
	@Override
	public void considerCommercialLoanMarkets() {		
		// first remove the current commercial orders, note that the cancel
		// method removes the order both from the market and this bank. 
		for (final Order order : new ArrayList<Order>(orders)) {
			if (order instanceof CommercialLoanOrder){
				((CommercialLoanOrder)order).cancel();
			}
		}
		
		for (final Market market : getMarketsOfType(CommercialLoanMarket.class)) {
			try {
				this.lendingRate = orderPriceDistribution.nextDouble();
				((CommercialLoanMarket) market).addOrder(this,
						COMMERCIAL_LOAN_MATURITY,
						-Math.abs(orderSizeDistribution.nextDouble()),
						this.lendingRate);
			} catch (final OrderException e) {
				e.printStackTrace();
				Simulation.getSimState().finish();
			} catch (AllocationException e) {
				e.printStackTrace();
				Simulation.getSimState().finish();
			}
		}		
	}

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.firm.StrategyFirmInterface#getEmployment()
	 */
	@Override
	public double getEmployment() {return 0.0;} //TODO implement employment market

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.firm.StrategyFirmInterface#getProduction()
	 */
	@Override
	public double getProduction() {return 0.0;} //TODO implement market for goods

	@Override
	protected void considerDepositPayment() {
	}

	@Override
	protected void considerProduction() {
	}
}
