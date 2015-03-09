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

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * An abstract base class for {@link CommercialBank} subeconomy configuration modules.<br><br>
  * 
  * This base class provides mandatory (Ie. required) bindings for the implementing
  * class. These mandatory bindings ensure that implementations of this class
  * are interchangeable.
  * 
  * @author phillips
  */
public abstract class AbstractCommercialBankSubEconomy extends AbstractPublicConfiguration {
   
   private static final long serialVersionUID = -8283485012935845522L;
   
   private static final boolean
      DEFAULT_DO_ENABLE_COMMERCIAL_BANK_STOCK_MARKET_PARTICIPATION = true,
      DEFAULT_IS_INTERBANK_MARKET_ENABLED = false,
      DEFAULT_BANKS_MAY_BUY_STOCKS_IN_OTHER_BANKS = true;
   
   private static final int 
      DEFAULT_NUMBER_OF_BANKS = 4;
   
   @Layout(
      Order = 0.0,
      FieldName = "Number of Banks"
      )
   @Parameter(
      ID = "NUMBER_OF_COMMERCIAL_BANKS"
      )
   private int 
      numberOfBanks = DEFAULT_NUMBER_OF_BANKS;
   
   public final int getNumberOfBanks() {
      return numberOfBanks;
   }
   
   /**
     * Set the number of {@link Bank} {@link Agent}{@code s} in this model. The argument
     * must not be less than {@code 1}.
     */
   public final void setNumberOfBanks(final int value) {
      this.numberOfBanks = value;
   }
   
   /**
     * Verbose description for parameter {@link #getNumberOfBanks}.
     */
   public final String desNumberOfBanks() {
      return "The number of banks to create.";
   }
   
   @Layout(
      Order = 3.0,
      Title = "Market Responses",
      FieldName = "Allow banks to trade in the Stock Market?"
      )
   @Parameter(
      ID = "DO_ENABLE_COMMERCIAL_BANK_STOCK_MARKET_PARTICIPATION"
      )
   private boolean
      doEnableStockMarketParticipation =
         DEFAULT_DO_ENABLE_COMMERCIAL_BANK_STOCK_MARKET_PARTICIPATION;
   
   @Layout(
      Order = 3.01,
      FieldName = "Allow Banks To Buy Bank Stocks?"
      )
   @Parameter(
      ID = "DO_BANKS_BUY_BANK_STOCKS"
      )
   private boolean
      doBanksBuyBankStocks = DEFAULT_BANKS_MAY_BUY_STOCKS_IN_OTHER_BANKS;
   
   public boolean getDoBanksBuyBankStocks() {
      return doBanksBuyBankStocks;
   }
   
   public void setDoBanksBuyBankStocks(
      final boolean value) {
      this.doBanksBuyBankStocks = value;
   }
   
   public final boolean getDoEnableStockMarketParticipation() {
      return doEnableStockMarketParticipation;
   }
   
   public final void setDoEnableStockMarketParticipation(
      final boolean doEnableStockMarketParticipation) {
      this.doEnableStockMarketParticipation = doEnableStockMarketParticipation;
   }
   
   public final String desDoEnableStockMarketParticipation() {
      return "Toggle to allow commercial banks to trade in the stock market.  There are two types "
           + "of commercial bank stock trading strategies - a fundamentalist trading strategy "
           + "based on expected dividend-price ratios of stocks, and a trend-following trading "
           + "strategy based on expected trends in stock prices.";
   }
   
   @Layout(
      Order = 7.0,
      Title = "Interbank Market",
      VerboseDescription = "Bank to Bank Lending",
      FieldName = "Enable Interbank Interaction?"
      )
   @Parameter(
      ID = "IS_INTERBANK_MARKET_ENABLED"
      )
   private boolean
      isInterbankMarketEnabled = DEFAULT_IS_INTERBANK_MARKET_ENABLED;
   
   public boolean getIsInterbankMarketEnabled() {
      return isInterbankMarketEnabled;
   }
   
   public void setIsInterbankMarketEnabled(final boolean isInterbankMarketEnabled) {
      this.isInterbankMarketEnabled = isInterbankMarketEnabled;
   }
   
   public String desIsInterbankMarketEnabled() {
      return "Toggle for turning on Interbank Market.  If Interbank Market is disabled, banks "
           + "in need of liquidity must borrow from the Central Bank at a penalty rate.";
   }
   
   /**
     * Create an {@link AbstractCommercialBankSubEconomy} object.<br><br>
     * 
     * Implementations of this type must expose the following bindings:
     * <ul>
     *   <li> An implementation of {@link ClearingBankStrategyFactory}{@code .class},
     *        for the construction of {@link CommercialBank} entities.
     * </ul>
     */
   protected AbstractCommercialBankSubEconomy() { }
   
   /**
     * When overriding this method, call {@link super#assertParameterValidity()}
     * as a first instruction.
     */
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         numberOfBanks >= 0,
         toParameterValidityErrMsg("Number of Banks is negative."));
   }
   
   /**
     * When overriding this method, call {@link super#addBindings()} as a 
     * first instruction.
     */
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_COMMERCIAL_BANKS",                      numberOfBanks,
         "IS_INTERBANK_MARKET_ENABLED",                     isInterbankMarketEnabled,
         "DO_BANKS_BUY_BANK_STOCKS",                        doBanksBuyBankStocks,
         "DO_ENABLE_COMMERCIAL_BANK_STOCK_MARKET_PARTICIPATION",
            doEnableStockMarketParticipation
         ));
      bind(InterbankLoanMarket.class).in(Singleton.class);
   }
   
   @Override
   protected final void setRequiredBindings() {
      requireBinding(Key.get(Integer.class, Names.named("NUMBER_OF_COMMERCIAL_BANKS")));
      requireBinding(Key.get(Boolean.class, Names.named("IS_INTERBANK_MARKET_ENABLED")));
      requireBinding(Key.get(Boolean.class, Names.named("DO_BANKS_BUY_BANK_STOCKS")));
      requireBinding(Key.get(Boolean.class,
         Names.named("DO_ENABLE_COMMERCIAL_BANK_STOCK_MARKET_PARTICIPATION")));
      requireBinding(ClearingBankStrategyFactory.class);
      requireBinding(InterbankLoanMarket.class);
   }
}
