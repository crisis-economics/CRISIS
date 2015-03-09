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
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.CompositeMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ForwardingClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.GraduallySellSharesMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.abm.simulation.injection.factories.MarketResponseFactoryBuilder.GraduallySellSharesMarketResponseFunctionFactoryBuilder;

@ConfigurationComponent(
      Description =
        "Stock Reseller Market Participation."
      + "\n\nThis is an interchangeable clearing market response configuration component for"
      + " agents who are trying to gradually sell off stocks that they own via the "
      + "clearing markets. "
      + "\n\nIf the agent currently has N shares, the 'Stock Selling Characteristic "
      + "Timescale' parameter "
      + "specifies that the agent wishes to own N * Stock_Selling_Characteristic_Timescale "
      + "number of shares when "
      + "the stock market has cleared.  This leads to a discretely sampled exponential decay"
      + " in the number of shares owned by the participant.  "
      + "The 'Critical Sale Threshold' parameter specifies the number of shares "
      + "below which the agent will stop trying to sell gradually, and will instead "
      + "offer to sell all remaining shares.",
      DisplayName = "Stock Reseller"
      )
public class StockResellerMarketParticipationConfiguration
   extends AbstractPrivateConfiguration
   implements ClearingMarketParticipationConfiguration {
      
   private static final long serialVersionUID = -6515183919906023675L;
   
   @Layout(
      FieldName = "Stock Selling Characteristic Timescale")
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
   
   private final static class StockResellerClearingMarketParticipationFactory
      implements ClearingMarketParticipantFactory {
      
      private GraduallySellSharesMarketResponseFunctionFactoryBuilder
         builder;
      
      private CompositeMarketResponseFunctionFactory
         responses;
      
      private ClearingHouse
         clearingHouse;
      
      @Inject
      private StockResellerClearingMarketParticipationFactory(
         GraduallySellSharesMarketResponseFunctionFactoryBuilder builder,
         ClearingHouse clearingHouse
         ) {
         this.builder = builder;
         this.responses = new CompositeMarketResponseFunctionFactory();
         this.clearingHouse = clearingHouse;
         
         Simulation.once(
            this, "setupStockMarketResponses", NamedEventOrderings.BEFORE_ALL);
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
   
   public double getRateAtWhichToSellOwnedShares() {
      return rateAtWhichToSellOwnedShares;
   }
   
   public void setRateAtWhichToSellOwnedShares(
      double rateAtWhichToSellOwnedShares) {
      this.rateAtWhichToSellOwnedShares = rateAtWhichToSellOwnedShares;
   }
   
   public final String desRateAtWhichToSellOwnedShares() {
      return
         "A characteristic timescale parameter in the closed interval [0, 1] which "
       + "applies to agents who are trying to gradually sell off stocks that they own via the "
       + "clearing markets. "
       + "\n\nIf such an agent currently has N shares with value V, this parameter (x) specifies"
       + " that the agent ideally wishes to own x * V shares when the stock market has been"
       + " processed. This leads to a discretely sampled exponential decay process"
       + " in the value of shares owned by the participant. The higher the value of the "
       + "this parameter parameter, in principle the more longer it takes to sell off the shares.";
   }
   
   public double getCriticalSaleThreshold() {
      return criticalSaleThreshold;
   }
   
   public final String desCriticalSaleThreshold() {
      return "This is a positive parameter that applies to "
            + " agents who are trying to gradually sell off stocks that they own via the "
            + "clearing markets. "
            + "\n\nThe 'Critical Sale Threshold' parameter specifies the number of shares "
            + "below which the agent will stop trying to sell gradually, and will instead "
            + "offer to sell all remaining shares.";
   }
   
   public void setCriticalSaleThreshold(
      double criticalSaleThreshold) {
      this.criticalSaleThreshold = criticalSaleThreshold;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(rateAtWhichToSellOwnedShares >= 0.,
         toParameterValidityErrMsg("Share Resale Rate is negative."));
      Preconditions.checkArgument(criticalSaleThreshold > 0.,
         toParameterValidityErrMsg("Critical Share Sale Threshold is non-positive."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "SHARE_SELLER_RATE_AT_WHICH_TO_SELL_SHARES",          rateAtWhichToSellOwnedShares,
         "SHARE_SELLER_THRESHOLD_AT_WHICH_TO_SELL_ALL_SHARES", criticalSaleThreshold
         ));
      install(new FactoryModuleBuilder()
         .implement(
             new TypeLiteral<AgentOperation<MarketResponseFunction>>(){},
             GraduallySellSharesMarketResponseFunctionFactory.class
             )
         .build(GraduallySellSharesMarketResponseFunctionFactoryBuilder.class)
         );
      bind(ClearingMarketParticipantFactory.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(StockResellerClearingMarketParticipationFactory.class);
      expose(ClearingMarketParticipantFactory.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}