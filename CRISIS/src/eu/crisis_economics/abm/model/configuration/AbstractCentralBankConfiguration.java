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

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.bank.central.CentralBankStrategy;
import eu.crisis_economics.abm.bank.strategies.clearing.ClearingBankStrategy;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory.CentralBankStrategyFactory;

/**
  * A base class for {@link CentralBank} {@link Agent} configuration components.<br><br>
  * 
  * The current codebase offers only one {@link CentralBank} implementation. For this reason
  * this base class remains non-abstract despite its name. In future, when alternative
  * {@link CentralBank} implementations are created, this base class should become
  * <code>abstract</code> and a set of required bindings should be specified.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Bank of England",
   ImagePath = "200px-Bank_of_England.png"
   )
public class AbstractCentralBankConfiguration extends AbstractPublicConfiguration {
   
   private static final long serialVersionUID = -5369732738379234511L;
   
   private final static boolean
      DEFAULT_IS_CENTRAL_BANK_IS_ENABLED = true;
   
   private static final double
      DEFAULT_CENTRAL_BANK_INITIAL_CASH_ENDOWMENT = 1.e15;
   
   private static final boolean
      DEFAULT_DO_ENABLE_CENTRAL_BANK_TAYLOR_RULE =
         CentralBankStrategy.DEFAULT_DO_ENABLE_CENTRAL_BANK_TAYLOR_RULE,
      DEFAULT_DO_ENABLE_OUTPUT_TRACKING =
         CentralBankStrategy.DEFAULT_DO_ENABLE_OUTPUT_TRACKING;
   
   private static final double
      DEFAULT_INITIAL_INTEREST_RATE_TARGET =
         CentralBankStrategy.DEFAULT_INITIAL_INTEREST_RATE_TARGET,
      DEFAULT_INITIAL_INTEREST_RATE_SPREAD =
         CentralBankStrategy.DEFAULT_INITIAL_INTEREST_RATE_SPREAD,
      DEFAULT_OUTPUT_RESPONSE_PARAMETER =
         CentralBankStrategy.DEFAULT_OUTPUT_RESPONSE_PARAMETER;
   
   private static final int
      DEFAULT_MOVING_AVERAGE_MEMORY =
         CentralBankStrategy.DEFAULT_MOVING_AVERAGE_MEMORY;
   
   @Layout(
      Order = 1.0,
      VerboseDescription = "The Lender of Last Resort",
      FieldName = "Enable Central Bank?"
      )
   @Parameter(
      ID = "IS_CENTRAL_BANK_ENABLED"
      )
   private boolean
      isCentralBankEnabled = DEFAULT_IS_CENTRAL_BANK_IS_ENABLED;
   
   public boolean getIsCentralBankEnabled() {
      return isCentralBankEnabled;
   }
   
   public void setIsCentralBankEnabled(final boolean isCentralBankEnabled) {
      this.isCentralBankEnabled = isCentralBankEnabled;
   }
   
   public String desIsCentralBankEnabled() {
      return "Toggle for turning on the central bank. If the central bank is disabled, "
           + "banks will be unable to obtain repo loans or uncollateralized cash injection "
           + "loans from the central bank.";
   }
   
   @Layout(
      Order = 2.0,
      VerboseDescription = "Endowments & Initial Conditions",
      FieldName = "Central Bank Cash Endowment"
      )
   @Parameter(
      ID = "CENTRAL_BANK_INITIAL_CASH_ENDOWMENT"
      )
   private double
      centralBankInitialCashEndowment = DEFAULT_CENTRAL_BANK_INITIAL_CASH_ENDOWMENT;
   @Layout(
      Order = 2.1,
      FieldName = "Central Bank Initial Lending Rate"
      )
   @Parameter(
      ID = "CENTRAL_BANK_STRATEGY_INITIAL_INTEREST_RATE"
      )
   private double
      centralBankInitialInterestRate = DEFAULT_INITIAL_INTEREST_RATE_TARGET;
   @Layout(
      Order = 2.2,
      FieldName = "Central Bank Initial Interest Rate Spread"
      )
   @Parameter(
      ID = "CENTRAL_BANK_STRATEGY_INITIAL_INTEREST_RATE_SPREAD"
      )
   private double
      centralBankInitialInterestRateSpread = DEFAULT_INITIAL_INTEREST_RATE_SPREAD;
   
   public double getCentralBankInitialCashEndowment() {
      return centralBankInitialCashEndowment;
   }
   
   public void setCentralBankInitialCashEndowment(final double value) {
      this.centralBankInitialCashEndowment = value;
   }
   
   public String desCentralBankInitialCashEndowment() {
      return "The central bank initial cash endowment, if a central bank agent is present.";
   }
   
   public double getCentralBankInitialInterestRate() {
      return centralBankInitialInterestRate;
   }
   
   public void setCentralBankInitialInterestRate(
      final double centralBankInitialInterestRate) {
      this.centralBankInitialInterestRate = centralBankInitialInterestRate;
   }
   
   public double getCentralBankInitialInterestRateSpread() {
      return centralBankInitialInterestRateSpread;
   }
   
   public void setCentralBankInitialInterestRateSpread(
      final double centralBankInitialInterestRateSpread) {
      this.centralBankInitialInterestRateSpread = centralBankInitialInterestRateSpread;
   }
   
   @Layout(
      Order = 3.0,
      VerboseDescription = "Regulation",
      FieldName = "Enable Taylor Rule?"
      )
   @Parameter(
      ID = "CENTRAL_BANK_STRATEGY_DO_ENABLE_TAYLOR_RULE"
      )
   private boolean
      doEnableCentralBankTaylorRule = DEFAULT_DO_ENABLE_CENTRAL_BANK_TAYLOR_RULE;
   @Layout(
      Order = 3.1,
      FieldName = "Moving Average Memory"
      )
   @Parameter(
      ID = "CENTRAL_BANK_STRATEGY_MOVING_AVERAGE_MEMORY"
      )
   private int
      centralBankMovingAverageMemory = DEFAULT_MOVING_AVERAGE_MEMORY;
   @Layout(
      Order = 3.2,
      FieldName = "Enable Output Tracking?"
      )
   @Parameter(
      ID = "CENTRAL_BANK_STRATEGY_DO_ENABLE_OUTPUT_TRACKING"
      )
   private boolean
      centralBankDoTrackOutput = DEFAULT_DO_ENABLE_OUTPUT_TRACKING;
   @Layout(
      Order = 3.3,
      FieldName = "Output Response Parameter"
      )
   @Parameter(
      ID = "CENTRAL_BANK_STRATEGY_OUTPUT_RESPONSE_PARAMETER"
      )
   private double
      centralBankOutputResponseParameter = DEFAULT_OUTPUT_RESPONSE_PARAMETER;
   
   public boolean getDoEnableCentralBankTaylorRule() {
      return doEnableCentralBankTaylorRule;
   }
   
   public void setDoEnableCentralBankTaylorRule(
      final boolean doEnableCentralBankTaylorRule) {
      this.doEnableCentralBankTaylorRule = doEnableCentralBankTaylorRule;
   }
   
   public int getCentralBankMovingAverageMemory() {
      return centralBankMovingAverageMemory;
   }
   
   public void setCentralBankMovingAverageMemory(
      final int centralBankMovingAverageMemory) {
      this.centralBankMovingAverageMemory = centralBankMovingAverageMemory;
   }
   
   public boolean isCentralBankDoTrackOutput() {
      return centralBankDoTrackOutput;
   }
   
   public void setCentralBankDoTrackOutput(
      final boolean centralBankDoTrackOutput) {
      this.centralBankDoTrackOutput = centralBankDoTrackOutput;
   }
   
   public double getCentralBankOutputResponseParameter() {
      return centralBankOutputResponseParameter;
   }
   
   public void setCentralBankOutputResponseParameter(
      final double centralBankOutputResponseParameter) {
      this.centralBankOutputResponseParameter = centralBankOutputResponseParameter;
   }
   
   @Layout(
      Order = 4.0,
      Title = "Market Interactions",
      VerboseDescription = "Market Interactions",
      FieldName = "Market Behaviour"
      )
   @Submodel
   @Parameter(
      ID = "CENTRAL_BANK_CLEARING_MARKET_PARTICIPATION"
      )
   StockResellerMarketParticipationConfiguration centralBankClearingMarketResponses
      = new StockResellerMarketParticipationConfiguration();
   
   public StockResellerMarketParticipationConfiguration getCentralBankClearingMarketResponses() {
      return centralBankClearingMarketResponses;
   }
   
   public void setCentralBankClearingMarketResponses(
      final StockResellerMarketParticipationConfiguration clearingMarketResponses) {
      this.centralBankClearingMarketResponses = clearingMarketResponses;
   }
   
   @Layout(
      Order = 5.0,
      Title = "Loan Policies",
      VerboseDescription = "Lending Policies",
      FieldName = "Uncollateralized Cash Injection Loan Policy"
      )
   @Submodel
   @Parameter(
      ID = "CENTRAL_BANK_UNCOLLATERALIZED_CASH_INJECTION_LOAN_POLICY"
      )
   CentralBankUncollateralizedLoanPolicyConfiguration
      centralBankUncollateralizedCashInjectionLoanPolicy =
         new AlwaysDenyUncollateralizedLoansPolicyConfiguration();
   
   /**
     * Get the {@link CentralBankUncollateralizedLoanPolicyConfiguration} submodel for this component.
     */
   public final CentralBankUncollateralizedLoanPolicyConfiguration
      getCentralBankUncollateralizedCashInjectionLoanPolicy() {
      return centralBankUncollateralizedCashInjectionLoanPolicy;
   }
   
   /**
     * Set the {@link CentralBankUncollateralizedLoanPolicyConfiguration} submodel for this component.
     */
   public final void setCentralBankUncollateralizedCashInjectionLoanPolicy(
      CentralBankUncollateralizedLoanPolicyConfiguration centralBankUncollateralizedCashInjectionLoanPolicy) {
      this.centralBankUncollateralizedCashInjectionLoanPolicy =
         centralBankUncollateralizedCashInjectionLoanPolicy;
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(centralBankInitialCashEndowment >= 0.);
      Preconditions.checkArgument(centralBankInitialInterestRate >= 0.);
      Preconditions.checkArgument(centralBankInitialInterestRateSpread >= 0.);
      Preconditions.checkArgument(centralBankOutputResponseParameter >= 0.);
      Preconditions.checkArgument(centralBankMovingAverageMemory >= 0.);
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
      "CENTRAL_BANK_INITIAL_CASH_ENDOWMENT",             centralBankInitialCashEndowment,
      "IS_CENTRAL_BANK_ENABLED",                         isCentralBankEnabled,
      "CENTRAL_BANK_STRATEGY_DO_ENABLE_TAYLOR_RULE",     doEnableCentralBankTaylorRule,
      "CENTRAL_BANK_STRATEGY_INITIAL_INTEREST_RATE",     centralBankInitialInterestRate,
      "CENTRAL_BANK_STRATEGY_INITIAL_INTEREST_RATE_SPREAD",
         centralBankInitialInterestRateSpread,
      "CENTRAL_BANK_STRATEGY_MOVING_AVERAGE_MEMORY",     centralBankMovingAverageMemory,
      "CENTRAL_BANK_STRATEGY_DO_ENABLE_OUTPUT_TRACKING", centralBankDoTrackOutput,
      "CENTRAL_BANK_STRATEGY_OUTPUT_RESPONSE_PARAMETER", centralBankOutputResponseParameter
      ));
      bind(CentralBank.class).in(Singleton.class);
      install(new FactoryModuleBuilder()
         .implement(ClearingBankStrategy.class, CentralBankStrategy.class)
         .build(CentralBankStrategyFactory.class)
         );
   }
}
