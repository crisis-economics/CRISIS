/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.bank.strategies.clearing;

import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.events.BankDividendDecisionMadeEvent;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketPortfolioUpdater;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;
import eu.crisis_economics.abm.simulation.injection.EmptyCollectionProvider;
import eu.crisis_economics.utilities.StateVerifier;

public final class CustomizableClearingStrategy
   extends EmptyBankStrategy 
   implements ClearingBankStrategy {
   
   private Portfolio
      portfolio;
   private CollectionProvider<ClearingStockMarket>
      stockMarketsToParticipateIn;
   private CollectionProvider<ClearingLoanMarket>
      loanMarketsToParticipateIn;
   private TimeseriesParameter<Double>
      portfolioSizeTargets,
      totalDividendPayments;
   
   @Inject
   public CustomizableClearingStrategy(
   @Assisted
      final ClearingBank bank,
      final Portfolio portfolio,
   @Named("CUSTOMIZABLE_CLEARING_STRATEGY_PORTFOLIO_SIZE_TARGETS")
      final TimeseriesParameter<Double> portfolioSizeTargets,
   @Named("CUSTOMIZABLE_CLEARING_STRATEGY_TOTAL_DIVIDEND_PAYMENTS")
      final TimeseriesParameter<Double> totalDividendPayments
      ) {
      super(bank);
      StateVerifier.checkNotNull(portfolio, portfolioSizeTargets, totalDividendPayments);
      this.portfolio = portfolio;
      this.stockMarketsToParticipateIn = new EmptyCollectionProvider<ClearingStockMarket>();
      this.loanMarketsToParticipateIn = new EmptyCollectionProvider<ClearingLoanMarket>();
      this.portfolioSizeTargets = portfolioSizeTargets;
      this.totalDividendPayments = totalDividendPayments;
   }
   
   @Override
   public final void decidePorfolioSize() {
      
      // Add and remove defunct investment options from the portfolio.
      ClearingMarketPortfolioUpdater.updateInvestmentOptions(
         portfolio, stockMarketsToParticipateIn, loanMarketsToParticipateIn);
      
      // Update return rate estimates for loan markets.
      ClearingMarketPortfolioUpdater.updateLoanMarketReturnEstimates(
         portfolio, loanMarketsToParticipateIn, Arrays.asList("Commercial Loan", "Gilt"));
      
      portfolio.setTargetPortfolioValue(portfolioSizeTargets.get());
   }
   
   @Override
   public final void decidePortfolioDistribution() {
      portfolio.setCashWeight(0.);
      
      portfolio.updatePortfolioWeights();
   }
   
   @Override
   public final void decideDividendPayment() {
      double
         dividendToPay = 0.;
      
      if(!getBank().isBankrupt() && !getBank().liquidateAtAfterAll) {
         final double
            numberOfSharesInExistence = getBank().getNumberOfEmittedShares();
         dividendToPay = totalDividendPayments.get();
         if(dividendToPay <= 0. || numberOfSharesInExistence <= 0.)
            getBank().setDividendPerShare(0.);
         else
            getBank().setDividendPerShare(dividendToPay / numberOfSharesInExistence);
      }
      else getBank().setDividendPerShare(0.);
      
      // Port a Bank Dividend Decision Event:
      {
      final double
         riskyAssetsAfterDividend = (getBank().getTotalAssets() - getBank().getCashReserveValue()),
         equityAfterDividend = (getBank().getEquity() - dividendToPay);
      Simulation.events().post(
         new BankDividendDecisionMadeEvent(
            getBank().getUniqueName(), 
            dividendToPay,
            riskyAssetsAfterDividend / equityAfterDividend,
            riskyAssetsAfterDividend,
            equityAfterDividend,
            getBank().getLoanInvestment(),
            getBank().getStockInvestment(),
            getBank().getCashReserveValue() - dividendToPay
            ));
      }
   }
   
   @Override
   public final void considerInterbankMarkets() { 
      getBank().getInterbankStrategy().considerInterbankMarkets();
   }
   
   @Override
   public final void afterInterbankMarkets() {
      getBank().getInterbankStrategy().afterInterbankMarkets();
   }
   
   /**
     * Get the {@link ClearingBank} associated with this {@link ClearingBankStrategy}.
     */
   @Override
   public final ClearingBank getBank() {
      return (ClearingBank) super.getBank();
   }
   
   @Override
   public void setStocksToInvestIn(
      final CollectionProvider<ClearingStockMarket> markets) {
      Preconditions.checkNotNull(markets);
      stockMarketsToParticipateIn = markets;
   }
   
   @Override
   public void setLoansToInvestIn(
      final CollectionProvider<ClearingLoanMarket> markets) {
      Preconditions.checkNotNull(markets);
      loanMarketsToParticipateIn = markets;
   }
   
   @Override
   public final double getDesiredStockInvestmentIn(
      final ClearingStockMarket market) {
      return 
         portfolio.getTargetStockInvestment(market.getStockReleaserName());
   }
   
   @Override
   public final double getDesiredLoanInvestmentIn(
      final ClearingLoanMarket market) {
      return portfolio.getTargetLoanInvestment(market.getMarketName());
   }
}