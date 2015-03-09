/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Peter Klimek
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
import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.LoanInstrument;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * This firm models the exogenous deposit process as mean-reverting random walk.
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
public class RandomWalkFirm extends StrategyFirm implements StrategyFirmInterface {

	private static final int COMMERCIAL_LOAN_MATURITY = 1;
	private static final double EFFICIENCY = 0.3;
	private static final double DEFAULT_PRICE = 1;
	private double expectedValueOfDepositChange = 0;
	private double expectedValueOfDeposit = 0;
	private double changeInDeposit = 0;
	private double meanReversionLevelOfDeposit;
	private double meanReversionRateOfDeposit;
	private double volatilityOfDeposit;
	private final AbstractDistribution randomShockOfDeposit;
	private double commercialLoanOrderVolume;
	private final double initialDeposit;


   /**
    * @param depositHolder
    * @param stockHolder
    * @param initialDeposit
    */
   public RandomWalkFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final int numberOfShares,
      final AgentNameFactory nameGenerator
      ) {
      this(
         depositHolder,
         initialDeposit,
         stockHolder,
         numberOfShares,
         1,
         0.1,
         0.1,
         new Normal(0.0,
            100,
            new MersenneTwister(Simulation.getSimState().random.nextInt())),
         nameGenerator
         );
   }
   
   /**
     * @param depositHolder
     * @param stockHolder
     * @param initialDeposit
     * @param meanReversionLevel
     *           S^*: the long-run equilibrium of deposits.
     * @param meanReversionRate
     *           /alpha: the speed at which deposits revert. The mean reversion
     *           rate is always greater than, or equal to, zero and higher
     *           numbers correspond to faster mean reversion.
     * @param volatility
     *           /sigma: the expected future variability of deposit over time.
     * @param randomShock
     *           /varepsilon_t
     * @param name
     *        A <i>uniqely identifying</i> name for this {@link Agent}.
     */
   public RandomWalkFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final int numberOfShares,
      final double meanReversionLevel,
      final double meanReversionRate,
      final double volatility,
      final AbstractDistribution randomShock,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            nameGenerator
            );
      this.initialDeposit = cashLedger.getStoredQuantity();
      this.meanReversionLevelOfDeposit = initialDeposit;
      this.meanReversionRateOfDeposit = meanReversionRate;
      this.volatilityOfDeposit = volatility;
      this.randomShockOfDeposit = randomShock;
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

	@Override
	protected void considerCommercialLoanMarkets() {

		for (final Order order : new ArrayList<Order>(orders)) {
			if (order instanceof CommercialLoanOrder){
				((CommercialLoanOrder)order).cancel();
			}
		}
		
		
		if (changeInDeposit > 0.0) {	// if profit then borrow
			//if (markets.containsKey( CommercialLoanMarket.class )) {
				for (final Market market :getMarketsOfType(CommercialLoanMarket.class)) {
					
					// spend half my profit on new loan
					commercialLoanOrderVolume = -.5*changeInDeposit/DEFAULT_PRICE;
					
					if (commercialLoanOrderVolume < 0.0) { 	// volume < 0 ... borrow
						try {
							((CommercialLoanMarket) market).addOrder(this,
									COMMERCIAL_LOAN_MATURITY ,
									commercialLoanOrderVolume,
									DEFAULT_PRICE);	
							System.out.println("Firm LoanOrder " + LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY) + " volume: " + commercialLoanOrderVolume);
						} catch (final OrderException e) {
							e.printStackTrace();
							Simulation.getSimState().finish();
						} catch (AllocationException e) {
							e.printStackTrace();
							Simulation.getSimState().finish();
						}

						
					}
					
				}
			//}
		}
	}
	
	@Override
	public void updateState(final Order order) {
		this.meanReversionLevelOfDeposit = this.initialDeposit + (EFFICIENCY * order.getLastExecutedVolume());
		System.out.println("Firm executed order: "+order.toString());
	}

	@Override
	public double getEmployment() {
		return 0;
	}

	@Override
	public double getProduction() {
		return this.changeInDeposit;
	}

	@Override
	protected void considerProduction() { }

	@Override
	public void cashFlowInjection(double amt) {
		// TODO Auto-generated method stub
		
	}
}
