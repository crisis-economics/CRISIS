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
import com.google.inject.name.Names;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingGiltsBondsAndCommercialLoansMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.CompositeMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ForwardingClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.LoanSupplierMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.TargetValueStockMarketResponseFunctionFactory;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.abm.simulation.injection.factories.MarketResponseFactoryBuilder.LoanSupplierMarketResponseFunctionFactoryBuilder;
import eu.crisis_economics.abm.simulation.injection.factories.MarketResponseFactoryBuilder.TargetValueStockMarketResponseFunctionFactoryBuilder;

public final class FundMarketParticipationConfiguration
   extends AbstractPrivateConfiguration
   implements ClearingMarketParticipationConfiguration {
   
   private static final long serialVersionUID = -65193260854556783L;
   
   @Parameter(
      ID = "CREDIT_SUPPLY_FUNCTION_EXPONENT"
      )
   private double
      creditSupplyFunctionExponent =
         LoanSupplierMarketResponseFunctionFactory.DEFAULT_LOAN_SUPPLY_EXPONENT;
   @Parameter(
      ID = "CREDIT_SUPPLY_FUNCTION_MAXIMUM_INTEREST_RATE"
      )
   private double
      creditSupplyFunctionMaximumInterestRate =
         LoanSupplierMarketResponseFunctionFactory.DEFAULT_MAXIMUM_INTEREST_RATE;
   
   private final static class FundClearingMarketParticipationFactory
      implements ClearingMarketParticipantFactory {
      
      private TargetValueStockMarketResponseFunctionFactoryBuilder
         stockMarketResponseBuilder;
      
      private CompositeMarketResponseFunctionFactory
         responses;
      
      private ClearingHouse
         clearingHouse;
      
      @Inject
      private FundClearingMarketParticipationFactory(
         final LoanSupplierMarketResponseFunctionFactoryBuilder
            vanillaLoanMarketResponseBuilder,
         final TargetValueStockMarketResponseFunctionFactoryBuilder
            stockMarketResponseBuilder,
         final ClearingHouse clearingHouse
         ) {
         this.responses = new CompositeMarketResponseFunctionFactory();
         this.stockMarketResponseBuilder = stockMarketResponseBuilder;
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
            responses.put(market.getMarketName(), stockMarketResponseBuilder.create(market));
         }
      }
      
      @Override
      public ClearingMarketParticipant create(Agent parent) {
         return new ForwardingClearingMarketParticipant(parent, responses);
      }
   }
   
   public double getCreditSupplyFunctionExponent() {
      return creditSupplyFunctionExponent;
   }
   
   public void setCreditSupplyFunctionExponent(
      double creditSupplyFunctionExponent) {
      this.creditSupplyFunctionExponent = creditSupplyFunctionExponent;
   }
   
   public double getCreditSupplyFunctionMaximumInterestRate() {
      return creditSupplyFunctionMaximumInterestRate;
   }
   
   public void setCreditSupplyFunctionMaximumInterestRate(
      double creditSupplyFunctionMaximumInterestRate) {
      this.creditSupplyFunctionMaximumInterestRate = creditSupplyFunctionMaximumInterestRate;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         creditSupplyFunctionExponent >= 0.,
         toParameterValidityErrMsg("Credit Supply Function Exponent is negative."));
      Preconditions.checkArgument(
         creditSupplyFunctionMaximumInterestRate > 0.,
         toParameterValidityErrMsg(
            "Credit Supply Function Maximum Interest Rate is non-positive."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "CREDIT_SUPPLY_FUNCTION_EXPONENT",              creditSupplyFunctionExponent,
         "CREDIT_SUPPLY_FUNCTION_MAXIMUM_INTEREST_RATE", creditSupplyFunctionMaximumInterestRate
         ));
      install(new FactoryModuleBuilder()
         .implement(
            new TypeLiteral<AgentOperation<MarketResponseFunction>>(){},
            TargetValueStockMarketResponseFunctionFactory.class
            )
         .build(TargetValueStockMarketResponseFunctionFactoryBuilder.class));
      install(new FactoryModuleBuilder()
         .implement(
            new TypeLiteral<AgentOperation<MarketResponseFunction>>(){},
            LoanSupplierMarketResponseFunctionFactory.class
            )
         .build(LoanSupplierMarketResponseFunctionFactoryBuilder.class));
      bind(ClearingMarketParticipantFactory.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(FundClearingMarketParticipationFactory.class);
      expose(ClearingMarketParticipantFactory.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
