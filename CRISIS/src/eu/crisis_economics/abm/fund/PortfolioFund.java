/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.fund;

import java.util.Arrays;

import com.google.common.base.Preconditions;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingGiltsBondsAndCommercialLoansMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketPortfolioUpdater;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.LoanSupplierMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.TargetValueStockMarketResponseFunctionFactory;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;
import eu.crisis_economics.abm.simulation.injection.EmptyCollectionProvider;
import eu.crisis_economics.abm.simulation.injection.factories.AbstractClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * An implementation of the {@link AbstractFund} skeletal class.<br><br>
  * 
  * This {@link Fund} implementation uses a {@link Portfolio} object to
  * divide its investments between non-cash illiquid assets. Redeemed client
  * shares are accounted for by adjusting the portfolio cash weight 
  * immediately before the clearing markets are processed.
  * 
  * @author tang
  * @author phillips
  */
public class PortfolioFund extends AbstractFund {
   
   /**
     * Create a {@link PortfolioFund} {@link Agent}. This constructor creates a
     * {@link PortfolioFund} {@link Agent} with default {@link ClearingMarket} responses.
     * The market responses are as follows:<br><br>
     * 
     * {@code (a)} 
     *   for any {@link ClearingStockMarket} in the stated {@link ClearingHouse},
     *   the {@link Fund} will behave as a target-investment-value stock trader;<br><br>
     * {@code (b)}
     *   for any {@link ClearingLoanMarket} in the stated {@link ClearingHouse} of 
     *   type {@link ClearingGiltsBondsAndCommercialLoansMarket}, the {@link Fund}
     *   will behave as a bond investor (a {@link Lender}). The profile of the
     *   credit supply function takes default settings.<br>
     * 
     * @param depositHolder
     *        The link {@link DepositHolder} who will manage {@link Fund} 
     *        deposits
     * @param portfolio
     *        The {@link Portfolio} algorithm used to distribute
     *        {@link Fund} investments over instruments (eg. stocks, loans)
     * @param clearingHouse
     *        The {@link ClearingHouse} on through which the {@link Fund} 
     *        will participate in {@link ClearingMarket}{@code s}.
     * @param targetStockInvestmentSmoothingAlgorithm
     *        A smoothing algorithm used to smooth the transition between 
     *        target and actual stock investment values (individually for all stock
     *        instruments traded by this {@link Fund}).
     */
   public PortfolioFund(
      final DepositHolder depositHolder,
      final Portfolio portfolio,
      final ClearingHouse clearingHouse,
      final SmoothingAlgorithm targetStockInvestmentSmoothingAlgorithm
      ) {
      this(
         depositHolder,
         portfolio,
         new AbstractClearingMarketParticipantFactory() {
            @Override
            protected void setupStockMarketResponses() {
               for(final ClearingStockMarket market :
                  clearingHouse.getMarketsOfType(ClearingStockMarket.class))
                  responses().put(
                     market.getMarketName(),
                     (new TargetValueStockMarketResponseFunctionFactory(market))
                     );
               }
            @Override
            protected void setupLoanMarketResponses() {
               for(final ClearingGiltsBondsAndCommercialLoansMarket market :
                 clearingHouse.getMarketsOfType(ClearingGiltsBondsAndCommercialLoansMarket.class))
                 responses().put(
                    market.getMarketName(),
                    (new LoanSupplierMarketResponseFunctionFactory(market))
                    );
            }},
            targetStockInvestmentSmoothingAlgorithm
            );
   }
   
   /**
     * Create a {@link PortfolioFund} {@link Agent}. This constructor fully exposes
     * the {@link ClearingMarketParticipant} component of the {@link Fund}.
     * For a simplified constructor, use {@link PortfolioFund}(DepositHolder, Portfolio, 
     * ClearingHouse).
     * 
     * @param depositHolder
     *        The link {@link DepositHolder} who will manage {@link Fund} 
     *        deposits
     * @param portfolio
     *        The {@link Portfolio} algorithm used to distribute
     *        {@link Fund} investments over instruments (eg. stocks, loans)
     * @param marketParticipationFactory
     *        A factory object to create a {@link ClearingMarketParticipant}.
     *        The {@link ClearingMarketParticipant} is used in composition to
     *        define the response of this {@link PortfolioFund} to all clearing
     *        markets.
     * @param targetStockInvestmentSmoothingAlgorithm
     *        A smoothing algorithm used to smooth the transition between 
     *        target and actual stock investment values (individually for all stock
     *        instruments traded by this {@link Fund}).
     */
   public PortfolioFund(
      final DepositHolder bank,
      final Portfolio portfolio,
      final ClearingMarketParticipantFactory marketParticipationFactory,
      final SmoothingAlgorithm targetStockInvestmentSmoothingAlgorithm
      ) {
      super(bank, marketParticipationFactory);
      StateVerifier.checkNotNull(portfolio);
      
      this.portfolio = portfolio;
      this.stockInvestmentsToConsider = new EmptyCollectionProvider<ClearingStockMarket>();
      this.loanInvestmentsToConsider = new EmptyCollectionProvider<ClearingLoanMarket>();
      this.stockInvestmentSmoothingAlgorithm =
         Preconditions.checkNotNull(targetStockInvestmentSmoothingAlgorithm);
      
      portfolio.setCashWeight(INITIAL_CASH_WEIGHT);
   }
   
   /*********************************************************************************
    * This method should be called before stocks/loans are traded on the market.
    * 
    * Before trading starts, we increase the cash-reserve weight of the portfolio
    * in-line with the shares in the fund that have been redeemed in this time step
    * but have not yet been settled, so that we have cash to settle with after market clearing.
    * 
    *********************************************************************************/
   public final void preClearingProcessing() {
      double proportionOfFundRedeemed;
      double cashProportion;
      
      // Add and remove defunct investment options from the portfolio.
      ClearingMarketPortfolioUpdater.updateInvestmentOptions(
         portfolio, stockInvestmentsToConsider, loanInvestmentsToConsider);
      
      // Update return rate estimates for loan markets.
      ClearingMarketPortfolioUpdater.updateLoanMarketReturnEstimates(
         portfolio, loanInvestmentsToConsider, Arrays.asList("Bond"));
      
      portfolio.setTargetPortfolioValue(getTotalAssets());
      
      // --- Increase cash-reserve weight to account for money owed for redeemed shares
      
      if(mInvestmentAccount.withdrawNoOfShares) {
         proportionOfFundRedeemed =
            mInvestmentAccount.getOrderBookTotalShares()/
               mInvestmentAccount.getNumberOfEmittedShares();
         cashProportion = proportionOfFundRedeemed;
      } else {
         cashProportion =
            mInvestmentAccount.getOrderBookTotalCashValue()/
               portfolio.getTargetPortfolioValue();
      }
      portfolio.setCashWeight(cashProportion);
      
      portfolio.updatePortfolioWeights();
   }
   
   ////////////////////////////////////////////////////////////////////////////////////////
   // Variables
   ////////////////////////////////////////////////////////////////////////////////////////
   
   Portfolio
      portfolio;
   
   private SmoothingAlgorithm
      stockInvestmentSmoothingAlgorithm;
   
   /**
     * Get the desired investment in stocks with the specified name.
     */
   public final double getDesiredStockInvestmentIn(final String stockName) {
      if(!portfolio.hasStock(stockName))
         return 0.;
      final double
         ideal = portfolio.getTargetStockInvestment(stockName),
         actual = getStockAccount(stockName) == null ? 0. : getStockAccount(stockName).getValue();
      return stockInvestmentSmoothingAlgorithm.applySmoothing(ideal, actual);
   }
   
   /**
     * Get the desired investment in loans with the specified name.
     */
   public final double getDesiredLoanInvestmentIn(final String marketName) {
      if(portfolio.hasLoan(marketName))
         return portfolio.getTargetLoanInvestment(marketName);
      else
         return 0.;
   }
   
   private CollectionProvider<ClearingStockMarket>
      stockInvestmentsToConsider;
   private CollectionProvider<ClearingLoanMarket>
      loanInvestmentsToConsider;
   
   @Override
   public final void setStocksToInvestIn(
      final CollectionProvider<ClearingStockMarket> markets) {
      Preconditions.checkNotNull(markets);
      this.stockInvestmentsToConsider = markets;
   }
   
   @Override
   public final void setLoansToInvestIn(
      final CollectionProvider<ClearingLoanMarket> markets) {
      Preconditions.checkNotNull(markets);
      this.loanInvestmentsToConsider = markets;
   }
   
   @Override
   public <T> T accept(final AgentOperation<T> operation) {
      return operation.operateOn(this);
   }
}