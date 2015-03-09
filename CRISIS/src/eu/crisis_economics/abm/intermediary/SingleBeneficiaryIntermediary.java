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
package eu.crisis_economics.abm.intermediary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.contracts.stocks.SimpleStockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketsResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.GraduallySellSharesMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.Simulation.ScheduledMethodInvokation;
import eu.crisis_economics.abm.simulation.injection.factories.AbstractClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.utilities.StateVerifier;

/**
  * A forwarding implementation of the {@link Intermediary} interface.
  * This implementation of {@link Intermediary} conducts its business
  * on behalf of one unique beneficiary. Cash debits to this agent will
  * be immediately passed on to the beneficiary.
  * 
  * If this agent receives shares, it will seek to sell these shares on 
  * the clearing stock market at the soonest opportunity. Any funds
  * obtained as a result of share sales will be immediately passed on
  * to the beneficiary.
  * 
  * Important notes:
  * 
  * In order to participate in clearing markets, the intermediary must be 
  * added to a clearing house. Without this step, no clearing markets will
  * be available for the intermediary to participate in.
  * 
  * @author phillips
  */
public final class SingleBeneficiaryIntermediary extends Agent implements Intermediary {
   
   public static final double
      DEFAULT_ASSET_VALUE_THRESHOLD_AT_WHICH_TO_DETATCH_FROM_BENEFICIARY = 1.e-12;
   
   private final SettlementParty
      beneficiary;
   private final StockHolder
      stockHolderBehaviour;
   private final ClearingMarketParticipant
      clearingMarketParticipant;
   private final ClearingHouse
      clearingHouse;
   
   private final double
      assetValueThresholdAtWhichToDetatchFromBeneficiary;
   
   /**
     * Create a {@link SingleBeneficiaryIntermediary} entity with default
     * parameters.<br><br>
     * 
     * {@link Intermediary} entities created by this simpified constructor
     * will:
     * <ul>
     *   <li> Respond only to {@link ClearingStockMarket}{@code s};
     *   <li> Gradually sell any shares they own;
     *   <li> Sell shares at the default rate (speed).
     * </ul>
     */
   public SingleBeneficiaryIntermediary(
      final SettlementParty beneficiary,
      final ClearingHouse clearingHouse
      ) {
      this(
         beneficiary,
         new AbstractClearingMarketParticipantFactory() {
            @Override
            protected void setupStockMarketResponses() {
               for(final ClearingStockMarket market :
                  clearingHouse.getMarketsOfType(ClearingStockMarket.class))
                  responses().put(
                     market.getMarketName(),
                     (new GraduallySellSharesMarketResponseFunctionFactory(market))
                     );
               }
            @Override
            protected void setupLoanMarketResponses() { }
         },
         clearingHouse,
         DEFAULT_ASSET_VALUE_THRESHOLD_AT_WHICH_TO_DETATCH_FROM_BENEFICIARY
         );
   }
   
   /**
     * Create a {@link SingleBeneficiaryIntermediary} entity with custom parameters.
     * 
     * @param beneficiary
     *        The {@link Agent} beneficiary on whose behalf this {@link Intermediary}
     *        will operate.
     * @param marketResponsesFactory
     *        The {@link ClearingMarketsResponseFunctionFactory} object generating
     *        clearing market responses for this participant.
     * @param assetValueThresholdAtWhichToDetatchFromBeneficiary
     *        The critical asset value threshold at which this {@link Intermediary}
     *        will detach from its beneficiary. When the total value of assets 
     *        belonging to this {@link Intermediary} has dropped below this threshold,
     *        this {@link Intermediary} will discontinue market participation and 
     *        terminate its remaining assets. In this implementation, any outstanding
     *        shares will be disowned and returned to the {@link StockExchange}, and
     *        any other type of asset contract will be allowed to mature.
     */
   @Inject
   public SingleBeneficiaryIntermediary(
   @Assisted
      final SettlementParty beneficiary,
   @Named("FINANCIAL_INTERMEDIARY_CLEARING_MARKET_PARTICIPATION")
      final ClearingMarketParticipantFactory marketResponsesFactory,
      final ClearingHouse clearingHouse,
   @Named("INTERMEDIARY_ASSET_VALUE_THRESHOLD_AT_WHICH_TO_DISCONTINUE")
      final double assetValueThresholdAtWhichToDetatchFromBeneficiary
      ) {
      StateVerifier.checkNotNull(beneficiary, marketResponsesFactory, clearingHouse);
      clearingHouse.addStockMarketParticipant(this);
      this.beneficiary = beneficiary;
      this.stockHolderBehaviour = new SimpleStockHolder(this);
      this.clearingMarketParticipant = marketResponsesFactory.create(this);
      this.clearingHouse = clearingHouse;
      this.assetValueThresholdAtWhichToDetatchFromBeneficiary = 
         Math.max(assetValueThresholdAtWhichToDetatchFromBeneficiary, 0.);
      
      scheduleSelf();
   }
   
   private ScheduledMethodInvokation
      reviewMarketParticipationEvent;
   
   private void scheduleSelf() {
      this.reviewMarketParticipationEvent =
         Simulation.once(this, "reviewMarketParticipation", NamedEventOrderings.AFTER_ALL);
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void reviewMarketParticipation() {
      final double
         totalAssetsValue = getTotalAssets();
      if(totalAssetsValue <= assetValueThresholdAtWhichToDetatchFromBeneficiary) {
         final List<StockAccount> stockAccounts =
            new ArrayList<StockAccount>(getStockAccounts().values());
         for(StockAccount account : stockAccounts)
            account.disownShares();
         clearingHouse.removeParticipant(this);
      }
      else Simulation.enqueue(reviewMarketParticipationEvent);
   }
   
   /**
     * {@link SingleBeneficiaryIntermediary} entities only retain contracts and resources
     * on behalf of other agents. As a result, no cash is available for
     * credits. Every nonzero, positive call to {@link SingleBeneficiaryIntermediary.credit}
     * raises {@link InsufficientFundsException}.
     */
   @Override
   public double credit(final double positiveAmount)
      throws InsufficientFundsException {
      if(positiveAmount <= 0.) return positiveAmount;
      throw new InsufficientFundsException(
         "StockHolderIntermediary.credit: the intermediary has no funds of its own, "
       + "it exists for the purpose of managing stocks on behalf of another agent."
       + "No cash is available for this purpose."
         );
   }
   
   /**
     * {@link SingleBeneficiaryIntermediary} entities only retain contracts and resources
     * on behalf of other agents. Debit cash is immediately passed to the
     * {@link Agent} on whose behalf this entity holds assets.
     */
   @Override
   public void debit(final double positiveAmount) {
      if(positiveAmount <= 0.) return;
      beneficiary.debit(positiveAmount);
   }
   
   /**
     * {@link SingleBeneficiaryIntermediary} entities have no means of raising cash by
     * injection. This operation does nothing, and cannot fail, in this
     * implementation.
     */
   @Override
   public void cashFlowInjection(final double positiveAmount) {
      // No Source of additional cash.
   }
   
   @Override
   public void addAsset(final Contract asset) {
      stockHolderBehaviour.addAsset(asset);
      super.addAsset(asset);
   }

   @Override
   public boolean removeAsset(final Contract asset) {
      stockHolderBehaviour.removeAsset(asset);
      return(super.removeAsset(asset));
   }   
   
   public double getNumberOfSharesOwnedIn(final String stockName) {
      return stockHolderBehaviour.getNumberOfSharesOwnedIn(stockName);
   }
   
   public boolean hasShareIn(final String stockName) {
      return stockHolderBehaviour.hasShareIn(stockName);
   }
   
   public StockAccount getStockAccount(final String stockName) {
      return stockHolderBehaviour.getStockAccount(stockName);
   }
   
   public Map<String, StockAccount> getStockAccounts() {
      return stockHolderBehaviour.getStockAccounts();
   }
   
   public Set<StockMarket> getStockMarkets() {
      return stockHolderBehaviour.getStockMarkets();
   }
   
   @Override
   public MarketResponseFunction getMarketResponseFunction(
      final ClearingMarketInformation marketInformation) {
      return clearingMarketParticipant.getMarketResponseFunction(marketInformation);
   }
   
   @Override
   public void registerIncomingDividendPayment(
      double dividendPayment,
      double pricePerShare,
      double numberOfSharesHeld
      ) {
      // No action or logging.
   }
   
   /**
     * Get a reference to the beneficiary.
     */
   public SettlementParty getBeneficiary() {
      return beneficiary;
   }
   
   @Override
   public Collection<SettlementParty> getBeneficiaries() {
      return Arrays.asList(getBeneficiary());
   }
   
   @Override
   public <T> T accept(final AgentOperation<T> operation) {
      return operation.operateOn((Intermediary) this);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Intermediary, on behalf of beneficiary: " + 
         beneficiary.getUniqueName() + ".";
   }
}
