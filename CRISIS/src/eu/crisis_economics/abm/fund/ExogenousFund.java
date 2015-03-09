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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.LiquidityException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.model.parameters.ModelParameter;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;
import eu.crisis_economics.abm.simulation.injection.factories.FundFactory;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A simple forwarding implementation of the {@link Fund} interface. This
  * implementation delegates all {@link Fund} methods to an encapsulated 
  * instance of a {@link Fund}. An exogenous cash stream is provided to 
  * the encapsulated instance.
  * 
  * @author phillips
  */
public final class ExogenousFund extends BaseFund {
   
   private Fund
      fund;
   
   private ModelParameter<Double>
      exogenousCashFlow;
   
   /**
     * Create a {@link ExogenousFund} {@link Agent}.
     * 
     * @param depositHolder
     *        The {@link DepositHolder} who will hold {@link Fund} deposits.
     * @param factory
     *        A {@link FundFactory} object, generating the forwarding {@link Fund}
     *        type to be employed by this implementation.
     * @param exogenousCashFlow
     *        A {@link TimeseriesParameter}{@code <Double>} object ({@code m}).
     *        This model parameter will be sampled exactly once per simulation cycle, at time 
     *        {@code CLEARING_MARKET_MATCHING} {@code (2)}. This parameter specifies an 
     *        exogenous flow of cash to the {@link Fund}. Concretely, if {@code m}
     *        returns {@code +100} then {@code £100} will be debited to this {@code Fund}
     *        agent a time {@code CLEARING_MARKET_MATCHING} {@code (2)}. If {@code m}
     *        returns {@code -100}, then <i>up to</i> {@code £100} will be credited to
     *        this {@link Fund} {@code Agent} at time {@code CLEARING_MARKET_MATCHING} 
     *        {@code (2)}. If {@code m} returns a negative value, and this value exceeds
     *        the liquidity of the {@link Fund} at that time, then the amount credited
     *        is silently trimmed to the maximum amount the {@link Fund} can afford.
     */
   @Inject
   public ExogenousFund(
   @Assisted
      final DepositHolder depositHolder,
   @Named("EXOGENOUS_FUND_IMPLEMENTATION_FACTORY")
      final FundFactory factory,
   @Named("EXOGENOUS_FUND_MANUAL_CASH_FLOW")
      final ModelParameter<Double> exogenousCashFlow
      ) {
      StateVerifier.checkNotNull(factory, exogenousCashFlow);
      this.fund = factory.create(depositHolder);
      this.exogenousCashFlow = exogenousCashFlow;
      
      scheduleSelf();
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "processExogenousCashFlows",
         CustomSimulationCycleOrdering.create(NamedEventOrderings.CLEARING_MARKET_MATCHING,2));
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void processExogenousCashFlows() {
      final double
         cashFlow = exogenousCashFlow.get();
      if(cashFlow > 0.)
         debit(cashFlow);
      else
         try {
            credit(Math.min(getUnallocatedCash(), -cashFlow));
         } catch (final InsufficientFundsException neverThrows) {
            System.err.println(
               "ExogenousFund.processExogenousCashFlows: failed to bill a fund agent "
             + "for " + (-cashFlow) + " units of cash. The value of unallocated cash for the "
             + "agent is " + getUnallocatedCash() + ". This behaviour is not expected in the "
             + "current implementation and is indicative of an error. No further action "
             + "will be taken to process this exogenous payment."
             );
            System.err.flush();
            return;
         }
   }
   
   @Override
   public double getEquity() {
      return fund.getEquity();
   }
   
   @Override
   public String getUniqueName() {
      return fund.getUniqueName();
   }
   
   @Override
   public MarketResponseFunction getMarketResponseFunction(
      ClearingMarketInformation marketInformation) {
      return fund.getMarketResponseFunction(marketInformation);
   }
   
   @Override
   public void addAsset(
      Contract asset) {
      fund.addAsset(asset);
   }
   
   @Override
   public void addLiability(
      Contract liability) {
      fund.addLiability(liability);
   }
   
   @Override
   public void invest(
      Depositor account,
      double cashInvestment) throws InsufficientFundsException {
      fund.invest(
         account,
         cashInvestment);
   }
   
   @Override
   public double getUnallocatedCash() {
      return fund.getUnallocatedCash();
   }
   
   @Override
   public double credit(
      double amt) throws InsufficientFundsException {
      return fund.credit(amt);
   }
   
   @Override
   public boolean removeAsset(
      Contract asset) {
      return fund.removeAsset(asset);
   }
   
   @Override
   public void decreaseCashReserves(
      double amount) throws LiquidityException {
      fund.decreaseCashReserves(amount);
   }
   
   @Override
   public boolean removeLiability(
      Contract liability) {
      return fund.removeLiability(liability);
   }
   
   @Override
   public void debit(
      double amt) {
      fund.debit(amt);
   }
   
   @Override
   public void addOrder(
      Order order) {
      fund.addOrder(order);
   }
   
   @Override
   public void requestWithdrawal(
      Depositor account,
      double cashValue) throws InsufficientFundsException {
      fund.requestWithdrawal(
         account,
         cashValue);
   }
   
   @Override
   public void cashFlowInjection(
      double amt) {
      fund.cashFlowInjection(amt);
   }
   
   @Override
   public double getTotalAssets() {
      return fund.getTotalAssets();
   }
   
   @Override
   public double getTotalLiabilities() {
      return fund.getTotalLiabilities();
   }
   
   @Override
   public double getNumberOfSharesOwnedIn(
      String stockName) {
      return fund.getNumberOfSharesOwnedIn(stockName);
   }
   
   @Override
   public boolean removeOrder(
      Order order) {
      return fund.removeOrder(order);
   }
   
   @Override
   public void increaseCashReserves(
      double amount) {
      fund.increaseCashReserves(amount);
   }
   
   @Override
   public List<Contract> getAssets() {
      return fund.getAssets();
   }
   
   @Override
   public List<Contract> getLiabilities() {
      return fund.getLiabilities();
   }
   
   @Override
   public void updateState(
      Order order) {
      fund.updateState(order);
   }
   
   @Override
   public boolean addMarket(
      Market market) {
      return fund.addMarket(market);
   }
   
   @Override
   public boolean hasShareIn(
      String stockName) {
      return fund.hasShareIn(stockName);
   }
   
   @Override
   public boolean removeMarket(
      Market market) {
      return fund.removeMarket(market);
   }
   
   @Override
   public double getEmissionPrice() {
      return fund.getEmissionPrice();
   }
   
   @Override
   public double getBalance(
      Depositor account) {
      return fund.getBalance(account);
   }
   
   @Override
   public <T extends Market> Set<T> getMarketsOfType(
      Class<T> clazz) {
      return fund.getMarketsOfType(clazz);
   }
   
   @Override
   public void settleOrderBook(
      double maximumCashCanBePaid) throws InsufficientFundsException {
      fund.settleOrderBook(maximumCashCanBePaid);
   }
   
   @Override
   public StockAccount getStockAccount(
      String stockName) {
      return fund.getStockAccount(stockName);
   }
   
   @Override
   public List<Loan> getAssetLoans() {
      return fund.getAssetLoans();
   }
   
   @Override
   public List<Loan> getLiabilitiesLoans() {
      return fund.getLiabilitiesLoans();
   }
   
   @Override
   public void setStocksToInvestIn(
      CollectionProvider<ClearingStockMarket> markets) {
      fund.setStocksToInvestIn(markets);
   }
   
   @Override
   public List<StockAccount> getAssetStockAccounts() {
      return fund.getAssetStockAccounts();
   }
   
   @Override
   public Map<String, StockAccount> getStockAccounts() {
      return fund.getStockAccounts();
   }
   
   @Override
   public List<DepositAccount> getAssetDeposits() {
      return fund.getAssetDeposits();
   }
   
   @Override
   public List<DepositAccount> getLiabilitiesDeposits() {
      return fund.getLiabilitiesDeposits();
   }
   
   @Override
   public Set<StockMarket> getStockMarkets() {
      return fund.getStockMarkets();
   }
   
   @Override
   public void registerIncomingDividendPayment(
      double dividendPayment,
      double pricePerShare,
      double numberOfSharesHeld) {
      fund.registerIncomingDividendPayment(
         dividendPayment,
         pricePerShare,
         numberOfSharesHeld);
   }
   
   @Override
   public List<? extends Order> getOrders() {
      return fund.getOrders();
   }
   
   @Override
   public void setLoansToInvestIn(
      CollectionProvider<ClearingLoanMarket> markets) {
      fund.setLoansToInvestIn(markets);
   }
   
   @Override
   public boolean isBankrupt() {
      return fund.isBankrupt();
   }
   
   @Override
   public void handleBankruptcy() {
      fund.handleBankruptcy();
   }
   
   @Override
   public double getDepositValue() {
      return fund.getDepositValue();
   }
   
   @Override
   public <T> T accept(
      AgentOperation<T> operation) {
      return fund.accept(operation);
   }
}
