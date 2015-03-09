/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
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

import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * A Firm that can borrow on the commercial loan market. 
 * @author olaf
 */
public abstract class StrategyFirm extends StockReleasingFirm implements Borrower {
	
	protected double askedLoan;
	public double getAskedLoan() {
		return askedLoan;
	}

   public StrategyFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder, initialDeposit, nameGenerator);
      
      scheduleEvents();
   }
   
   public StrategyFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final double emissionPrice,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder, initialDeposit, nameGenerator);
      
      scheduleEvents();
   }
   
   /**
     * Creates a firm that can borrow on the loan market.
     * 
     * @param depositHolder
     * @param stockHolder
     * @param initialDeposit
     */
   public StrategyFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final double numberOfShares,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder, initialDeposit, stockHolder, numberOfShares, nameGenerator);
      
      scheduleEvents();
   }

   /**
     * Creates a firm that can borrow on the loan market and for which we can fix
     * an emission price - christoph created this constructor
     * 
     * @param depositHolder
     * @param initialDeposit
     * @param stockHolder
     * @param numberOfShares
     * @param emissionPrice
     */
   public StrategyFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final double numberOfShares,
      final double emissionPrice,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            emissionPrice,
            nameGenerator);
      
      scheduleEvents();
   }

    public void scheduleEvents(){
        Simulation.repeat(this, "considerProduction",
           NamedEventOrderings.FIRM_DECIDE_PRODUCTION);
        Simulation.repeat(this, "considerCommercialLoanMarkets", 
           NamedEventOrderings.AFTER_COMMERCIAL_MARKET_BIDDING);
        Simulation.repeat(this, "considerDepositPayment", 
           NamedEventOrderings.DEPOSIT_PAYMENT);
    }
	
	/**
	 * Subclasses should implement the firms's production strategy by
	 * overriding this function. This method is called once in each turn to
	 * allow the firm to make its move.
	 * 
	 */
	abstract protected void considerProduction();

	/**
	 * Subclasses should implement the firms's loan-market strategy by
	 * overriding this function. This method is called once in each turn to
	 * allow the firm to make its move. 
	 * 
	 */
	protected abstract void considerCommercialLoanMarkets();

	/**
	 * Subclasses should implement the firms's deposit strategy by
	 * overriding this function. This method is called once in each turn to
	 * allow the firm to make its move.
	 * 
	 */
	protected abstract void considerDepositPayment();

   @Override
   public void registerOutgoingDividendPayment(
      double dividendPayment,
      double pricePerShare, 
      double numberOfSharesHeldByInvestor
      ) { }
}
