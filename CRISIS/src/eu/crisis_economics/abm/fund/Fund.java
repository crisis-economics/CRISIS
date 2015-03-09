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
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;

/**
 * @author DT
 */
public interface Fund extends
   IAgent, StockHolder,       // Allow share ownership.
   Lender,                    // Supports buying of bonds.
   InvestmentAccountHolder,  // holds deposits from households
   Depositor,                 // Holds cash reserves at a bank
   ClearingMarketParticipant {
   
   /**
     * Specify which stock markets this {@link Fund} will consider when formulating
     * stock investments.<br><br>
     * 
     * TODO: improving this setup may involve general changes to portfolios.
     * 
     * @param stocks
     *        A collection provider for {@link ClearingStockMarket}{@code s}.
     *        Each {@link ClearingStockMarket} provided by the argument
     *        corresponds to a type of stock to consider when formulating
     *        investments.
     */
   public void setStocksToInvestIn(CollectionProvider<ClearingStockMarket> markets);
   
   /**
     * Specify which loan markets this {@link Fund} should consider when formulating 
     * loan investments.<br><br>
     * 
     * TODO: improving this setup may involve general changes to portfolios.
     * 
     * @param markets
     *        A collection provider for {@link ClearingMarket}{@code s}.
     *        Each {@link ClearingMarket} provided by the argument
     *        corresponds to a type of loan market to consider when formulating
     *        investments.
     */
   public void setLoansToInvestIn(CollectionProvider<ClearingLoanMarket> markets);
}
