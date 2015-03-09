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
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.strategies.clearing.ClearingBankStrategy;
import eu.crisis_economics.abm.bank.strategies.clearing.PortfolioClearingStrategy;
import eu.crisis_economics.abm.bank.strategies.clearing.SmoothingClearingStrategy;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   Description =
      "A financial subeconomy with multiple differentiated commercial banks, an optional"
    + " lender of last resort, an optional bad bank, financial intermediaries, customizable"
    + " bank bankruptcy resolution policies, optional interbank trading activity, and"
    + " customizable bank market behaviours. This subeconomy consists of fundamentalist and "
    + "chartist commercial banks and commercial banks whose behaviour is a mixture of "
    + "fundamentalist and chartist states.\n\n"
    + 
      "<b>Fundamentalist Banks</b>\n use the Dividend-Price ratios of Stocks when calculating "
    + "the expected returns of Stock investments in the following way:\n\n"
    + "<center><code>"
    + "Expected_Stock_Return = Dividend_per_share / (Stock_price * Stock_return_adjustment_factor)"
    + "</code></center>\n"
    + "where the dividend per share received just before the current time-step and the stock "
    + "price at the previous time-step are used.  The Stock Return Adjustment Factor is a "
    + "parameter set in the panel below.  Returns on debt (loans, bonds, mortgages) are "
    + "calculated using their yield measures from the end of the previous time-step.\n\n"
    + 
      "<b>Trend-Follower Banks</b>\n use the change in the price of Stocks over time when "
    + "calculating the expected returns of Stock investments in the following way:\n\n"
    + "<center><code>"
    + "Expected_Stock_Return = (Share_Price(t) - Share_Price(t-Lag)) / Share_Price(t-Lag)"
    + "</code></center>\n"
    + "where the time t is the most recently available share price (i.e. derived at the "
    + "end of the "
    + "previous time-step) and the 'Lag' is a parameter that can be set below, which "
    + "specifies the number of time-steps between Stock price observations.  Returns on "
    + "debt (loans, bonds, mortgages) are calculated using their yield measures from the "
    + "end of the previous time-step.\n\n"
    + "Commercial Banks issue loans to Firms by creating deposits on the "
    + "liability side of their balance sheet and loans on the asset side of their balance "
    + "sheet, thereby increasing credit in the economy and leveraging up.  They can also "
    + "hold deposits from households and funds, issue mortgages to households, borrow from "
    + "Funds, and trade in Firm (and Bank) stocks on the Stock Market.  When the Central "
    + "Bank is enabled, Banks can also borrow or lend on the interbank market."
    + "\n\nAt the start of the simulation, in order to raise capital to finance their "
    + "operations, Banks issue stocks that are initially held by Funds, and are "
    + "subsequently traded on the Stock Market."
    + "\n\nIt is possible for Banks to go bankrupt and the various bankruptcy resolution "
    + "methods can be adjusted in the Bankruptcy section below.",
    DisplayName = "Chartists & Fundamentalists"
   )
public class ChartistAndFundamentalistBankSubeconomy
   extends AbstractCommercialBankSubEconomy {
   
   private static final long serialVersionUID = 234488057262211944L;
   
   private static final double
      DEFAULT_INITIAL_BANK_STOCK_EMISSION_VALUE = 1.7e9,
      DEFAULT_NUMBER_OF_SHARES_TO_EMIT_PER_BANK = 1.0,
      DEFAULT_INITIAL_BANK_CASH_ENDOWMENT = 0.,
      DEFAULT_BANK_EQUITY_TARGET = 3.5e9;
   
   @Layout(
      Order = 0.1,
      Title = "Endowments && Public Offerings",
      FieldName = "Bank Cash Endowment",
      VerboseDescription = "Endowments"
      )
   @Parameter(
      ID = "COMMERCIAL_BANK_INITIAL_CASH_ENDOWMENT"
      )
   private double
      initialCashEndowmentPerBank = DEFAULT_INITIAL_BANK_CASH_ENDOWMENT;
   
   /**
     * Get the amount of cash with which each {@link Bank} is initialized.
     * This initial cash endowment does not include any cash obtained
     * from an initial public offering (share emission).
     */
   public final double getInitialCashEndowmentPerBank() {
      return initialCashEndowmentPerBank;
   }
   
   /**
     * Set the amount of cash with which each {@link Bank} is initialized.
     * This initial cash endowment does not include any cash obtained
     * from an initial public offering (share emission). The argument to this method
     * should be non-negative.
     */
   public final void setInitialCashEndowmentPerBank(final double value) {
      initialCashEndowmentPerBank = value;
   }
   
   /**
     * Verbose description for parameter {@link #getInitialCashEndowmentPerBank}.
     */
   public final String desInitialCashEndowmentPerBank() {
      return "The initial amount of cash with which each Bank is endowed at initialization. "
           + "This sum does not include any additional cash that the Bank may obtain from "
           + "an initial public offering (share emission).  The argument should be non-negative.";
   }
   
   @Layout(
      Order = 0.2,
      FieldName = "Bank Equity Target"
      )
   @Parameter(
      ID = "BANK_EQUITY_TARGET"
      )
   private double
      bankEquityTarget = DEFAULT_BANK_EQUITY_TARGET;
   
   public final double getBankEquityTarget() {
      return bankEquityTarget;
   }
   
   /**
     * Set the {@link Bank} equity target. The argument should be non-negative.
     */
   public final void setBankEquityTarget(final double value) {
      bankEquityTarget = value;
   }
   
   /**
     * Verbose description for parameter {@link #getBankEquityTarget}.
     */
   public final String desBankEquityTarget() {
      return "The target level of equity that banks aim for. Banks will consider their target "
            + "equity when deciding what amount of cash to release to shareholders as dividends.";
   }
   
   @Layout(
      Order = 0.4,
      VerboseDescription = "Shares & Public Offerings",
      FieldName = "Shares Issued Per Bank"
      )
   @Parameter(
      ID = "NUMBER_OF_SHARES_TO_EMIT_PER_BANK"
      )
   private double
      numberOfSharesToEmitPerBank = DEFAULT_NUMBER_OF_SHARES_TO_EMIT_PER_BANK;   
   /**
     * Get the number of shares issued at each {@link Bank}'s initial public offering, to
     * {@link Fund}{@code s}.  Note that the model admits fractions of a share.
     */
   public final double getNumberOfSharesToEmitPerBank() {
      return numberOfSharesToEmitPerBank;
   }
   
   /**
     * Set the number of shares issued at each {@link Bank}'s initial public offering, to
     * {@link Fund}{@code s}. The argument should be strictly positive.
     */
   public final void setNumberOfSharesToEmitPerBank(final double value) {
      numberOfSharesToEmitPerBank = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getNumberOfSharesToEmitToFundsPerBank}.
     */
   public final String desNumberOfSharesToEmitPerBank() {
      return "The number of shares issued at initial public offering (at initialization) per bank. "
      + "This should be strictly positive, but can be a fraction of a share.";
   }
   
   @Layout(
      Order = 0.5,
      FieldName = "Initial Public Offering Value Per Bank"
      )
   @Parameter(
      ID = "INITIAL_STOCK_EMISSION_VALUE_PER_BANK"
      )
   private double
      initialStockEmissionValuePerBank = DEFAULT_INITIAL_BANK_STOCK_EMISSION_VALUE;
   /**
     * Get the cash value of the {@link Bank}'s initial public offering (share emission).
     */
   public final double getInitialStockEmissionValuePerBank() {
      return initialStockEmissionValuePerBank;
   }
   
   /**
     * Set the cash value of the {@link Bank}'s initial public offering (share emission). The
     * argument to this method should be non-negative.
     */
   public final void setInitialStockEmissionValuePerBank(final double value) {
      initialStockEmissionValuePerBank = value;
   }
   
   /**
     * Verbose description for parameter {@link #getInitialCashEndowmentPerBank}.
     */
   public final String desInitialStockEmissionValuePerBank() {
      return "The total cash value of the bank's initial public offering (share emission).  "
           + "This value should be non-negative.";
   }
   
   @Submodel
   @Layout(
      Order = 3.1,
      VerboseDescription = "Market Responses",
      FieldName = "Market Behaviour"
      )
   @Parameter(
      ID = "COMMERCIAL_BANK_CLEARING_MARKET_RESPONSES"
      )
   private CommercialBankMarketResponseFunctionConfiguration
      bankClearingMarketResponses = new CommercialBankMarketResponseFunctionConfiguration();
   
   public CommercialBankMarketResponseFunctionConfiguration getBankClearingMarketResponses() {
      return bankClearingMarketResponses;
   }
   
   public void setBankClearingMarketResponses(
      final CommercialBankMarketResponseFunctionConfiguration clearingMarketResponses) {
      this.bankClearingMarketResponses = clearingMarketResponses;
   }
   
   @Submodel
   @Layout(
      Order = 4.0,
      Title = "Investment Portfolio",
      VerboseDescription = "Portfolio Decisions",
      FieldName = "Portfolio"
      )
   @Parameter(
      ID = "BANK_PORTFOLIO"
      )
   private SimpleStockAndLoanPortfolioConfiguration
      portfolio = new SimpleStockAndLoanPortfolioConfiguration(
         new ExponentialAdaptivePortfolioWeightingConfiguration(0.1),
         new MovingMedianStockReturnExpectationFunctionConfiguration(
         new LinearCombinationStockReturnExpectationFunctionConfiguration(
            new FundamentalistStockReturnExpectationFunctionConfiguration(4.0), 
            new TrendFollowerStockReturnExpectationFunctionConfiguration(2),
            new ConstantDoubleModelParameterConfiguration(1.0)
            )
         )
      );
   
   public SimpleStockAndLoanPortfolioConfiguration getPortfolio() {
      return portfolio;
   }
   
   public void setPortfolio(
      final SimpleStockAndLoanPortfolioConfiguration portfolio) {
      this.portfolio = portfolio;
   }
   
   @Layout(
      Order = 4.1,
      FieldName = "Investment Target Smoothing"
      )
   @Submodel
   @Parameter(
      ID = "SMOOTHING_CLEARING_BANK_STRATEGY_SMOOTHING_ALGORITHM"
      )
   private AbstractSmoothingAlgorithmConfiguration
      investmentSmoothing = new GaussianWeightSmoothingAlgorithmConfiguration();
   
   public AbstractSmoothingAlgorithmConfiguration getInvestmentSmoothing() {
      return investmentSmoothing;
   }
   
   public void setInvestmentSmoothing(
      final AbstractSmoothingAlgorithmConfiguration value) {
      this.investmentSmoothing = value;
   }
   
   @Layout(
      Order = 4.3,
      FieldName = "Leverage Target Algorithm"
      )
   @Submodel
   @Parameter(
      ID = "PORTFOLIO_CLEARING_STRATEGY_LEVERAGE_TARGET_ALGORITHM"
      )
   private BankLeverageTargetConfiguration
      leverageAlgorithm = new ConstantTargetLeverageConfiguration();
   
   public BankLeverageTargetConfiguration getLeverageAlgorithm() {
      return leverageAlgorithm;
   }
   
   public void setLeverageAlgorithm(final BankLeverageTargetConfiguration value) {
      leverageAlgorithm = value;
   }
   
   public final String desLeverageAlgorithm(){
      return
         "The algorithm used by banks in this component to calculate the value of leverage to "
       + "aim for. There are two options: \nFixed Leverage Target - where the value of "
       + "leverage that banks aim for is a constant number, \nValue at Risk Leverage Target - where"
       + " the value of leverage that banks aim for depends on their perception of risk, which "
       + "depends on the volatility of asset prices.\nThe definition of leverage in this model"
       + " is Risky_Assets / Equity, so a bank's (non-risky) cash reserves are not included in "
       + "the Risky_Assets numerator.";
   }
   
   @Layout(
      Order = 4,
      Title = "Commercial Bank Bankruptcy Resolution Policy",
      VerboseDescription = "The Bankruptcy Resolution Policy applies whenever a Commercial Bank "
            + "agent becomes insolvent (i.e. their equity is negative).",
      FieldName = "Bankruptcy Resolution Policy"
      )
   @Submodel
   @Parameter(
      ID = "BANK_BANKRUPTCY_POLICY_CONFIGURATION",
      Scoped = false
      )
   private BankBankruptcyPolicyConfiguration
      bankBankruptcyResolutionPolicy = new BailinBankBankruptcyPolicyConfiguration();
   
   public BankBankruptcyPolicyConfiguration getBankBankruptcyResolutionPolicy() {
      return bankBankruptcyResolutionPolicy;
   }
   
   public void setBankBankruptcyResolutionPolicy(
      final BankBankruptcyPolicyConfiguration value) {
      bankBankruptcyResolutionPolicy = value;
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(
         initialCashEndowmentPerBank >= 0.,
         toParameterValidityErrMsg("Initial Cash Endowment Per Bank is negative."));
      Preconditions.checkArgument(
         bankEquityTarget >= 0.,
         toParameterValidityErrMsg("Bank Equity Target is negative."));
      Preconditions.checkArgument(
         numberOfSharesToEmitPerBank > 0.,
         toParameterValidityErrMsg("Number of shares per bank is non-positive."));
      Preconditions.checkArgument(
         initialStockEmissionValuePerBank >= 0.,
         toParameterValidityErrMsg("Initial stock emission value per bank is negative."));
   }
   
   private static class ClearingBankStrategyFactoryImpl implements ClearingBankStrategyFactory {
      @Inject @Named("STRATEGY_FACTORY_MASK")
      ClearingBankStrategyFactory factory;
      @Inject @Named("SMOOTHING_CLEARING_BANK_STRATEGY_SMOOTHING_ALGORITHM")
      SmoothingAlgorithm smoothing;
      
      @Override
      public ClearingBankStrategy create(final ClearingBank bank) {
         return new SmoothingClearingStrategy(factory.create(bank), smoothing);
      }
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_SHARES_TO_EMIT_PER_BANK",         numberOfSharesToEmitPerBank,
         "INITIAL_STOCK_EMISSION_VALUE_PER_BANK",     initialStockEmissionValuePerBank,
         "COMMERCIAL_BANK_INITIAL_CASH_ENDOWMENT",    initialCashEndowmentPerBank,
         "PORTFOLIO_CLEARING_STRATEGY_EQUITY_TARGET", bankEquityTarget
         ));
      install(new FactoryModuleBuilder()
         .implement(ClearingBankStrategy.class, PortfolioClearingStrategy.class)
         .build(Key.get(ClearingBankStrategyFactory.class, Names.named("STRATEGY_FACTORY_MASK")))
         );
      bind(ClearingBankStrategyFactory.class).to(ClearingBankStrategyFactoryImpl.class);
      super.addBindings();
   }
}