/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Milan Lovric
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.LiquidityException;
import eu.crisis_economics.abm.contracts.stocks.SimpleStockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A skeletal implementation of a {@link Fund} {@link Agent}.
  * 
  * @author phillips
  */
public abstract class AbstractFund extends BaseFund implements Fund {
   
   static final double INITIAL_SHARE_PRICE = 1.0;
   static final double INITIAL_CASH_WEIGHT = 0.1; // Does this need to be variable?
   
   double
      privateEquity; // held off balance sheet to allow +ve equity
   
   protected AbstractFund(
      final DepositHolder myBank,
      final ClearingMarketParticipantFactory marketParticipationFactory
      ) {
      StateVerifier.checkNotNull(myBank, marketParticipationFactory);
      
      mStockHolder        = new SimpleStockHolder(this);
      mDepositor          = new MDepositor(this, myBank, 0.);
      mLender             = new MLender(this, mDepositor.getSettlementAccount());
      mInvestmentAccount  = new MInvestmentAccountHolder(this, mDepositor.getSettlementAccount());
      iDepositHolder      = mInvestmentAccount;
      clearingMarketParticipation = marketParticipationFactory.create(this);
      mInvestmentAccount.setEmissionPrice(INITIAL_SHARE_PRICE);
      privateEquity = 0.;
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(
         this, "recalculateEmissionPrice", 
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.HOUSEHOLD_FUND_PAYMENTS, -1));
      Simulation.repeat(
         this, "preClearingProcessing", NamedEventOrderings.PRE_CLEARING_MARKET_MATCHING);
      Simulation.repeat(
         this, "postClearingProcessing", NamedEventOrderings.POST_CLEARING_MARKET_MATCHING);
   }
   
   public abstract void preClearingProcessing();
   
   /*********************************************************************************
    * This method should be called after stocks/loans are traded on the market.
    * 
    * After trading is done by the trading module, the price of shares in the fund is
    * recalculated based on the new mark-to-market value, and the new value is set
    * in the mInvestmentAccount module.
    * 
    * @throws InsufficientFundsException 
    *********************************************************************************/
   public final void postClearingProcessing() throws InsufficientFundsException {
       recalculateEmissionPrice();
       final double
          numberOfShares = mInvestmentAccount.getNumberOfEmittedShares(),
          sharePrice = getEmissionPrice();
       if(numberOfShares != 0.0) {
           if(sharePrice > 0.) {
              double
                 cashAvailable = Math.max(getBalance() - privateEquity, 0.),
                 orderBookCashValue = mInvestmentAccount.getOrderBookTotalCashValue();
              if(orderBookCashValue <= 0.)
                 mInvestmentAccount.scaleOrderBook(0.);
              else if(orderBookCashValue >= cashAvailable) {
                 // Scale orders down to fit within liquidity (leaving equity
                 // capital in cash to avoid zero balance)
                 mInvestmentAccount.scaleOrderBook(cashAvailable/orderBookCashValue);
              }
              mInvestmentAccount.settleOrderBook(cashAvailable);
           }
       }
   }
   
   /**
     * Reset all fund investment accounts, and the fund share price, to
     * their initial states. This method terminates all existing client
     * {@link InvestmentAccount}s, sets the total number of emitted
     * shares to zero, resets the fund share price, and removes all
     * entries from the fund order book.
     */
   private void resetInvestmentAccounts() {
      List<InvestmentAccount> accounts = 
         new ArrayList<InvestmentAccount>(mInvestmentAccount.investmentAccounts.values());
      for(InvestmentAccount account : accounts)
         account.terminate();
      mInvestmentAccount.investmentAccounts.clear();
      mInvestmentAccount.numberOfEmittedShares = 0.;
      mInvestmentAccount.setEmissionPrice(INITIAL_SHARE_PRICE);
      mInvestmentAccount.orderBook.clear();
   }
   
   private void recalculateEmissionPrice() {
      mInvestmentAccount.recountShareDistribution();
      final double
         numberOfShares = mInvestmentAccount.getNumberOfEmittedShares();
      if(numberOfShares != 0.0) {
         final double
            sharePrice = Math.max((getTotalAssets() - privateEquity)/numberOfShares, 0.);
         if(sharePrice <= 0.)
            resetInvestmentAccounts();
         else
            mInvestmentAccount.setEmissionPrice(sharePrice);
      }
   }
   
   ////////////////////////////////////////////////////////////////////////////////////////
   // Lender forwarding
   ////////////////////////////////////////////////////////////////////////////////////////
   
   private MLender
      mLender;
   
   @Override
   @RecorderSource("UnreservedCash")
   public final double getUnallocatedCash() {
       return(mLender.getUnallocatedCash());
   }
   
   ////////////////////////////////////////////////////////////////////////////////////////
   // StockHolder forwarding
   ////////////////////////////////////////////////////////////////////////////////////////
   
   private StockHolder
      mStockHolder;
   
   @Override
   public final double getNumberOfSharesOwnedIn(final String stockName) {
       return(mStockHolder.getNumberOfSharesOwnedIn(stockName));
   }
   
   @Override
   public final boolean hasShareIn(final String stockName) {
       return(mStockHolder.hasShareIn(stockName));
   }
   
   @Override
   public final StockAccount getStockAccount(final String stockName) {
       return(mStockHolder.getStockAccount(stockName));
   }
   
   @Override
   public final Map<String, StockAccount> getStockAccounts() {
       return(mStockHolder.getStockAccounts());
   }
   
   @Override
   public final Set<StockMarket> getStockMarkets() {
       return(mStockHolder.getStockMarkets());
   }
   
   private final ClearingMarketParticipant
      clearingMarketParticipation;
   
   @Override
   public final MarketResponseFunction getMarketResponseFunction(
      final ClearingMarketInformation marketInformation
      ) {
      return clearingMarketParticipation.getMarketResponseFunction(marketInformation);
   }
   
   @Override
   public void registerIncomingDividendPayment(
      double dividendPayment, double pricePerShare, double numberOfSharesHeld) {
      // No action.
   }
   
   ////////////////////////////////////////////////////////////////////////////////////////
   // HasAssets forwarding
   ////////////////////////////////////////////////////////////////////////////////////////
   
   @Override
   public final void addAsset(final Contract asset) {
       mStockHolder.addAsset(asset);
       super.addAsset(asset);
   }
   
   @Override
   public final boolean removeAsset(final Contract asset) {
       mStockHolder.removeAsset(asset);
       return(super.removeAsset(asset));
   }   
   
   ////////////////////////////////////////////////////////////////////////////////////////
   // IInvestmentAccount forwarding
   ////////////////////////////////////////////////////////////////////////////////////////
   
   MInvestmentAccountHolder
      mInvestmentAccount;
   
   @Override
   public final void invest(Depositor account, double cashValue)
      throws InsufficientFundsException {
      mInvestmentAccount.invest(account, cashValue);
   }
   
   @Override
   public final void requestWithdrawal(Depositor account, double cashValue)
      throws InsufficientFundsException {
      mInvestmentAccount.requestWithdrawal(account, cashValue);
   }
   
   @Override
   @RecorderSource("EmissionPrice")
   public final double getEmissionPrice() {
      return(mInvestmentAccount.getEmissionPrice());
   }
   
   @Override
   public final void settleOrderBook(final double maximumCashCanBePaid)
      throws InsufficientFundsException {
      mInvestmentAccount.settleOrderBook(maximumCashCanBePaid);
   }

   @Override
   @RecorderSource("Balance")
   public final double getBalance(Depositor account) {
      return(mInvestmentAccount.getBalance(account));
   }
   
   ////////////////////////////////////////////////////////////////////////////////////////
   // MDepositor forwarding
   ////////////////////////////////////////////////////////////////////////////////////////
   
   MDepositor
      mDepositor;
   
   public final double getBalance() {
       return(mDepositor.getBalance());
   }
   
   ////////////////////////////////////////////////////////////////////////////////////////
   // SettlementParty forwarding
   ////////////////////////////////////////////////////////////////////////////////////////
   
   @Override
   public final double credit(double amt) throws InsufficientFundsException {
       return(mDepositor.credit(amt));
   }

   @Override
   public final void debit(double amt) {
       mDepositor.debit(amt);
   }

   @Override
   public final void cashFlowInjection(double amt) {
       mDepositor.cashFlowInjection(amt);
   }
   
   ////////////////////////////////////////////////////////////////////////////////////////
   // DepositHolder interface
   ////////////////////////////////////////////////////////////////////////////////////////
   
   private MDepositHolder
      iDepositHolder;
   
   @Override
   public final void decreaseCashReserves(double amount) throws LiquidityException {
       iDepositHolder.decreaseCashReserves(amount);
   }
   
   @Override
   public final void increaseCashReserves(double amount) {
       iDepositHolder.increaseCashReserves(amount);
   }   
   
   @Override
   public final void handleBankruptcy() {
      /*
       * No action. MutualFund liabilities are shrunk to match assets when 
       * the client order book is settled, restoring positive equity.
       */
   }
   
   @Override
   public final double getDepositValue() {
      return mDepositor.getDepositValue();
   }
}
