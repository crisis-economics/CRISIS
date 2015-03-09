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
package eu.crisis_economics.abm.model.configuration;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingGiltsBondsAndCommercialLoansMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.CompositeMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ForwardingClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.GraduallySellSharesMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.LoanConsumerMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.MarketResponseFactoryBuilder.LoanConsumerMarketResponseFunctionFactoryBuilder;
import eu.crisis_economics.abm.simulation.injection.factories.MarketResponseFactoryBuilder.GraduallySellSharesMarketResponseFunctionFactoryBuilder;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

public final class
   GovernmentClearingMarketResponseFunctionConfiguration
   extends AbstractPrivateConfiguration
   implements ClearingMarketParticipationConfiguration {
   
   private static final long serialVersionUID = 738203880005369002L;
   
   @Parameter(
      ID = "CREDIT_DEMAND_FUNCTION_EXPONENT"
      )
   private double
      creditDemandFunctionExponent =
         LoanConsumerMarketResponseFunctionFactory.DEFAULT_LOAN_DEMAND_EXPONENT;
   @Parameter(
      ID = "CREDIT_DEMAND_FUNCTION_MAXIMUM_INTEREST_RATE"
      )
   private double
      creditDemandFunctionMaximumInterestRate = .2;
   @Parameter(
      ID = "SHARE_SELLER_RATE_AT_WHICH_TO_SELL_SHARES"
      )
   private double
      rateAtWhichToSellOwnedShares =
         GraduallySellSharesMarketResponseFunctionFactory
            .DEFAULT_SHARE_SELLER_RATE_AT_WHICH_TO_SELL_SHARES;
   @Parameter(
      ID = "SHARE_SELLER_THRESHOLD_AT_WHICH_TO_SELL_ALL_SHARES"
      )
   private double
      criticalSaleThreshold =
         GraduallySellSharesMarketResponseFunctionFactory
            .DEFAULT_SHARE_SELLER_THRESHOLD_AT_WHICH_TO_SELL_ALL_SHARES;
   
   private static final class GovernmentMarketParticipationComponent
      implements ClearingMarketParticipantFactory {
      
      private GraduallySellSharesMarketResponseFunctionFactoryBuilder
         builder;
      
      private CompositeMarketResponseFunctionFactory
         responses;
      
      private ClearingHouse
         clearingHouse;
      
      @Inject
      private GovernmentMarketParticipationComponent(
      @Named("DO_ENABLE_GOVERNMENT_GILTS_MARKET_PARTICIPATION")
         final boolean doEnableGiltsMarketParticipation,
         final LoanConsumerMarketResponseFunctionFactoryBuilder
            vanillaLoanMarketResponseBuilder,
         final GraduallySellSharesMarketResponseFunctionFactoryBuilder
            stockMarketResponseBuilder,
         final ClearingHouse clearingHouse
         ) {
         this.responses = new CompositeMarketResponseFunctionFactory();
         this.builder = stockMarketResponseBuilder;
         this.clearingHouse = clearingHouse;
         
         for(final ClearingGiltsBondsAndCommercialLoansMarket market :
            clearingHouse.getMarketsOfType(ClearingGiltsBondsAndCommercialLoansMarket.class)) {
           responses.put(
              market.getMarketName(),
              vanillaLoanMarketResponseBuilder.create(market)
              );
         }
         
         Simulation.once(this, "setupStockMarketResponses", NamedEventOrderings.BEFORE_ALL);
      }
      
      @SuppressWarnings("unused")   // Scheduled
      private void setupStockMarketResponses() {
         List<ClearingStockMarket>
            markets = clearingHouse.getMarketsOfType(ClearingStockMarket.class);
         for(final ClearingStockMarket market : markets) {
            responses.put(market.getMarketName(), builder.create(market));
         }
      }
      
      @Override
      public ClearingMarketParticipant create(Agent parent) {
         return new ForwardingClearingMarketParticipant(parent, responses);
      }
   }
   
   public double getCreditDemandFunctionExponent() {
      return creditDemandFunctionExponent;
   }
   
   /**
     * Set the credit demand function polynomial exponent for {@link Agent}{@code s}
     * configured to respond to clearing markets with this component. The argument
     * should be non-negative.
     */
   public void setCreditDemandFunctionExponent(
      final double creditDemandFunctionExponent) {
      this.creditDemandFunctionExponent = creditDemandFunctionExponent;
   }
   
   public double getCreditDemandFunctionMaximumInterestRate() {
      return creditDemandFunctionMaximumInterestRate;
   }
   
   /**
     * Set the maximum interest rate for {@link Agent}{@code s} configured to respond
     * to clearing loan markets with this component. The argument should be non-negative.
     */
   public void setDemandSupplyFunctionMaximumInterestRate(
      final double creditDemandFunctionMaximumInterestRate) {
      this.creditDemandFunctionMaximumInterestRate = creditDemandFunctionMaximumInterestRate;
   }
   
   public double getRateAtWhichToSellOwnedShares() {
      return rateAtWhichToSellOwnedShares;
   }

   public void setRateAtWhichToSellOwnedShares(
      double rateAtWhichToSellOwnedShares) {
      this.rateAtWhichToSellOwnedShares = rateAtWhichToSellOwnedShares;
   }
   
   public double getCriticalSaleThreshold() {
      return criticalSaleThreshold;
   }
   
   public void setCriticalSaleThreshold(final double value) {
      criticalSaleThreshold = value;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         creditDemandFunctionExponent >= 0.,
         toParameterValidityErrMsg("Credit Demand Function Exponent is negative."));
      Preconditions.checkArgument(
         creditDemandFunctionMaximumInterestRate > 0.,
         toParameterValidityErrMsg(
            "Credit Demand Function Maximum Interest Rate is non-positive."));
      Preconditions.checkArgument(
         rateAtWhichToSellOwnedShares >= 0.,
         toParameterValidityErrMsg("Share Resale Rate is negative."));
      Preconditions.checkArgument(criticalSaleThreshold >= 0.,
         toParameterValidityErrMsg("Critical Sale Threshold is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "CREDIT_DEMAND_FUNCTION_EXPONENT",              creditDemandFunctionExponent,
         "CREDIT_DEMAND_FUNCTION_MAXIMUM_INTEREST_RATE", creditDemandFunctionMaximumInterestRate,
         "SHARE_SELLER_RATE_AT_WHICH_TO_SELL_SHARES",    rateAtWhichToSellOwnedShares,
         "SHARE_SELLER_THRESHOLD_AT_WHICH_TO_SELL_ALL_SHARES", criticalSaleThreshold
         ));
      install(new FactoryModuleBuilder()
         .implement(
            new TypeLiteral<AgentOperation<MarketResponseFunction>>(){},
            LoanConsumerMarketResponseFunctionFactory.class
            )
         .build(LoanConsumerMarketResponseFunctionFactoryBuilder.class));
      install(new FactoryModuleBuilder()
         .implement(
            new TypeLiteral<AgentOperation<MarketResponseFunction>>(){},
            GraduallySellSharesMarketResponseFunctionFactory.class
            )
         .build(GraduallySellSharesMarketResponseFunctionFactoryBuilder.class));
      bind(ClearingMarketParticipantFactory.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(GovernmentMarketParticipationComponent.class);
      expose(ClearingMarketParticipantFactory.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
