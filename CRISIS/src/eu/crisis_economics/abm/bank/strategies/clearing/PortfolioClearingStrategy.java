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
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.portfolio.Portfolio;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.loans.RepoLoan;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.events.BankDividendDecisionMadeEvent;
import eu.crisis_economics.abm.events.ShareTransactionEvent;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketPortfolioUpdater;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;
import eu.crisis_economics.abm.simulation.injection.EmptyCollectionProvider;
import eu.crisis_economics.abm.strategy.leverage.LeverageTargetAlgorithm;
import eu.crisis_economics.utilities.StateVerifier;

public class PortfolioClearingStrategy
   extends EmptyBankStrategy 
   implements ClearingBankStrategy {
   
   /**
     * Enable {@code VERBOSE_MODE = true} for verbose console analysis.
     */
   private static final boolean
      VERBOSE_MODE = true;
   
   protected Portfolio
      portfolio;
   protected LeverageTargetAlgorithm
      leverageTargetAlgorithm;
   protected CollectionProvider<ClearingStockMarket>
      stockMarketsToParticipateIn;
   protected CollectionProvider<ClearingLoanMarket>
      loanMarketsToParticipateIn;
   protected double
      equityTarget;
   private double
      dividendToPay,
      equityTargetThisCycle;
   
   @Inject
   protected PortfolioClearingStrategy(
   @Assisted
      final ClearingBank bank,
   @Named("BANK_PORTFOLIO")
      final Portfolio portfolio,
   @Named("PORTFOLIO_CLEARING_STRATEGY_LEVERAGE_TARGET_ALGORITHM")
      final LeverageTargetAlgorithm leverageTargetAlgorithm,
   @Named("PORTFOLIO_CLEARING_STRATEGY_EQUITY_TARGET")
      final double equityTarget
      ) {
      super(bank);
      
      StateVerifier.checkNotNull(portfolio, leverageTargetAlgorithm);
      Preconditions.checkArgument(equityTarget >= 0.);
      this.portfolio = portfolio;
      this.leverageTargetAlgorithm = leverageTargetAlgorithm;
      this.equityTarget = equityTarget;
      this.stockMarketsToParticipateIn = new EmptyCollectionProvider<ClearingStockMarket>();
      this.loanMarketsToParticipateIn = new EmptyCollectionProvider<ClearingLoanMarket>();
   }
   
   /**
     * The fields {@link #shareCashFlows} and the method {@code #respondToShareTransaction}
     * merely record the cash flows assicated with share transactions occurring over the 
     * markets. These fields and methods do not affect the decisions made by this strategy.
     */
   double
      shareCashFlow = 0.;
   protected Map<String, Double>
      shareCashFlows = new HashMap<String, Double>();
   
   @Subscribe
   public void respondToShareTransaction(
      final ShareTransactionEvent event) {
      if(event.getStockHolder().getUniqueName() == getBank().getUniqueName()) {
         shareCashFlow += event.getCashFlow();
         if(!shareCashFlows.containsKey(event.getStockName()))
            shareCashFlows.put(event.getStockName(), event.getCashFlow());
         else
            shareCashFlows.put(event.getStockName(), 
               shareCashFlows.get(event.getStockName()) + event.getCashFlow());
      }
   }
   
   @Override
   public final void decidePorfolioSize() {
      
      shareCashFlow = 0.;
      shareCashFlows.clear();
      
      // Add and remove defunct investment options from the portfolio.
      ClearingMarketPortfolioUpdater.updateInvestmentOptions(
         portfolio, stockMarketsToParticipateIn, loanMarketsToParticipateIn);
      
      // Update return rate estimates for loan markets.
      ClearingMarketPortfolioUpdater.updateLoanMarketReturnEstimates(
         portfolio, loanMarketsToParticipateIn, Arrays.asList("Commercial Loan", "Gilt"));
      
      /*
       * The Bank has just attempted to repay its existing bond liabilities, has
       * recieved dividends from Firm shares, commercial loan instalments, and
       * government bond income. As a result (assuming the that bank is still OK)
       * the bank either has outstanding repo loan liabilities or central bank
       * CILs to repay, but no bond liabilities.
       */
      leverageTargetAlgorithm.computeTargetLeverage(portfolio);
      
      // Check whether the bank is fit to participate in the markets:
      if(getBank().isBankrupt() || getBank().liquidateAtAfterAll) {
         portfolio.setTargetPortfolioValue(0.);
         return;
      }
      
      final double
         equityBase =
            getBank().getEquity() < equityTarget ? 
            Math.max(getBank().getEquity(), 0.) : equityTarget;
      
      equityTargetThisCycle = equityBase;
      
      if(VERBOSE_MODE)
         System.out.printf(
            "Bank: equity at time of portfolio decision: %16.10g target: %16.10g [%g%%]\n",
            getBank().getEquity(),
            equityTargetThisCycle,
            1.e2 * getBank().getEquity() / equityTargetThisCycle
            );
      
      portfolio.setTargetPortfolioValue(
         equityBase * leverageTargetAlgorithm.getTargetLeverage());
   }
   
   @Override
   public final void decidePortfolioDistribution() {
      portfolio.setCashWeight(0.);
      
      portfolio.updatePortfolioWeights();
   }
   
   /**
     * In this implementation, the {@link Bank} will pay no dividends if it is bankrupt
     * or pending a resolution procedure. Otherwise, of the {@link Bank} is not bankrupt
     * of pending a resolution procedure, the total dividend payment is determined by 
     * the equity target of the bank and its current liquidity constraints.
     */
   @Override
   public final void decideDividendPayment() {
      
      //
      // Decide the diviend payment per share.
      // 
      // At this stage the bank may have repo loan liabilities (collateralized
      // central bank loans) about to be repaid in the next simulation cycle. In 
      // the current implementation, these repo loans were created in this cycle.
      // The repo loan face value (Ie. total due repayment) is to be subtracted
      // from the bank dividend payment:
      // 
      if(!getBank().isBankrupt() && !getBank().liquidateAtAfterAll) {
         final double
            numberOfSharesInExistence = getBank().getNumberOfEmittedShares(),
            pendingRepoRepayment = getBank().getRepoLoanAmountToPay(),
            idealDividend =
               (equityTargetThisCycle < equityTarget ? .9 : 1.) *
               (getBank().getEquity() - equityTargetThisCycle);
         getBank().cashFlowInjection(idealDividend / .9 + pendingRepoRepayment);
         dividendToPay = Math.max(
            Math.min(
               idealDividend,
               Math.min(
                  .9 * getBank().getCashReserveValue(),
                  getBank().getCashReserveValue() - pendingRepoRepayment)
               ),
               0.);
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
      
      if(VERBOSE_MODE)
         System.out.println(print());
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
   
   /**
     * Create a highly verbose analysis string for this object. This method does
     * not affect the behaviour of this class.
     */
   private String print() {
      String result = "";
      result += 
         "Market performance for " + getBank().getUniqueName();
      final double
         desiredPortfolioSize = portfolio.getTargetPortfolioValue();
      double
         actualPortfolioSize = 0.;
      for(final ClearingStockMarket market : stockMarketsToParticipateIn) {
         final String
            stockInvestorName = market.getStockReleaserName();
         final double
            intendedStockInvestment = portfolio.getTargetStockInvestment(stockInvestorName),
            actualStockInvestment =
               getBank().getStockAccount(stockInvestorName) == null ? 0. : 
               getBank().getStockAccount(stockInvestorName).getValue();
         result += String.format(
            "[%s] intended stock: %16.10g actual: %16.10g "
          + "[%g%%] pps: %16.10g cis: %16.10g shares owned: %16.10g\n",
            stockInvestorName,
            intendedStockInvestment,
            actualStockInvestment,
            1.e2 * actualStockInvestment / intendedStockInvestment,
            UniqueStockExchange.Instance.getStockPrice(stockInvestorName),
            UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(stockInvestorName),
            actualStockInvestment / UniqueStockExchange.Instance.getStockPrice(stockInvestorName)
            );
         actualPortfolioSize += actualStockInvestment;
      }
      for(final ClearingLoanMarket market : loanMarketsToParticipateIn) {
         final double
            intendedLoanInvestment = portfolio.getTargetLoanInvestment(market.getMarketName()),
            actualLoanInvestment = getBank().getLoanInvestment();
         result += String.format(
            "[%s] intended loan: %16.10g actual: %16.10g [%g%%]\n",
            market.getMarketName(),
            intendedLoanInvestment,
            actualLoanInvestment,
            1.e2 * actualLoanInvestment / intendedLoanInvestment
            );
         actualPortfolioSize += actualLoanInvestment;
      }
      double
         bankRepoLiability = 0.,
         bankDepositsLiability = 0.;
      for(final Contract contract : getBank().getLiabilities())
         if(contract instanceof RepoLoan)
            bankRepoLiability += contract.getValue();
         else if(contract instanceof DepositAccount)
            bankDepositsLiability += contract.getValue();
      
      result += String.format(
         "intended portfolio size: %16.10g actual: %16.10g [%g%%]\n" +
         "equity: %16.10g target: %16.10g\n" +
         "cash reserve less pending dividend: %16.10g\n" +
         "dividend to pay: %16.10g\n" + 
         "leverage after dividend: %16.10g\n" +
         "leverage target: %16.10g\n" +
         "assets: %16.10g\n" +
         " in stock: %16.10g\n" +
         " in loan: %16.10g\n" +
         "liabilities: %16.10g\n" +
         " in deposits: %16.10g\n" +
         " in repo: %16.10g\n" +
         " other: %16.10g\n",
         desiredPortfolioSize,
         actualPortfolioSize,
         1.e2 * actualPortfolioSize / desiredPortfolioSize,
         getBank().getEquity(),
         equityTarget,
         getBank().getCashReserveValue() - dividendToPay,
         dividendToPay,
         actualPortfolioSize / (getBank().getEquity() - dividendToPay),
         leverageTargetAlgorithm.getTargetLeverage(),
         getBank().getTotalAssets(),
         getBank().getLoanInvestment(),
         getBank().getStockInvestment(),
         getBank().getTotalLiabilities(),
         bankDepositsLiability,
         bankRepoLiability,
         getBank().getTotalLiabilities() - bankDepositsLiability - bankRepoLiability
         );
      return result;
   }
}