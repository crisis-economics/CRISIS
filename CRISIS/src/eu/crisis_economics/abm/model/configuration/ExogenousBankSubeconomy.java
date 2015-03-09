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
import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.BankStub;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.bank.strategies.clearing.BankStubClearingBankStrategy;
import eu.crisis_economics.abm.bank.strategies.clearing.ClearingBankStrategy;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingBankStrategyFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Exogenous Commercial Banks"
   )
public class ExogenousBankSubeconomy
   extends AbstractCommercialBankSubEconomy {
   
   private static final long serialVersionUID = 2894701442173295358L;
   
   private static final double
      DEFAULT_INITIAL_BANK_STOCK_EMISSION_VALUE = 1.7e9,
      DEFAULT_NUMBER_OF_SHARES_TO_EMIT_PER_BANK = 1.0,
      DEFAULT_BANK_STOCK_RISK_PREMIUM = 4.,
      DEFAULT_FUNDAMENTALIST_CHARTIST_WEIGHT = 1.;
   
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
      return "The total cash value of the bank's initial public offering (share emission)."
           + " This value should be non-negative.";
   }
   
   @Layout(
      Order = 1.0,
      Title = "Chartist && Fundamentalist Behaviour",
      FieldName = "Stock Return Adjustment Factor",
      VerboseDescription = "Fundamentalist Configuration"
      )
   @Parameter(
      ID = "BANK_STUB_STRATEGY_STOCK_RISK_PREMIUM"
      )
   private double
      bankStockRiskPremium = DEFAULT_BANK_STOCK_RISK_PREMIUM;
   
   public double getBankStockRiskPremium() {
      return bankStockRiskPremium;
   }
   
   /**
     * Set the stock investment risk premium for this model. The argument
     * should be strictly positive.
     */
   public void setBankStockRiskPremium(final double value) {
      this.bankStockRiskPremium = value;
   }
   
   public String desBankStockRiskPremium() {
      return "The parameter appears as a factor in Fundamentalist Banks' calculations of stock "
           + "return fundamentals: 'Stock Return Fundamental' = 'Dividend Per Share' / ('Stock "
           + "Price' * 'Stock Return Adjustment Factor') so that when the Stock Return Adjustment "
           + "Factor >  1 the perceived stock return fundamentals are discounted i.e. risky stock "
           + "returns appear lower to investors, and investors can be considered risk-averse.  "
           + "When Stock Return Adjustment Factor = 1 there is no discount on stock investments "
           + "and investors can be considered risk-neutral, and when Stock Return Adjustment F"
           + "actor < 1 stock returns are more attractive and investors are risk-seeking.";
      }
   
   public static final int
      DEFAULT_TREND_FOLLOWER_BANK_LAG = 2;
   
   @Layout(
      Order = 1.1,
      FieldName = "Lag (Number of Cycles)",
      VerboseDescription = "Chartist Configuration"
      )
   @Parameter(
      ID = "BANK_STUB_STRATEGY_LAG"
      )
   private int
      trendFollowerBankLag = DEFAULT_TREND_FOLLOWER_BANK_LAG;
   
   public int getTrendFollowerBankLag() {
       return trendFollowerBankLag;
   }
   
   /**
     * Set the Lag factor for trend-follower bank strategies in this model.
     * The argument should be non-negative.
     */
   public void setTrendFollowerBankLag(final int value) {
       trendFollowerBankLag = value;
   }
   
   /**
     * Verbose description for parameter {@link #getTrendFollowerBankLag}.
     */
   public String desTrendFollowerBankLag() {
      return "The number of time-steps between the two measurements of stock price that are "
           + "taken to measure the 'trend' of a stock price.  If the lag is [', the expected "
           + "return is calculated as the difference between the current price and the price "
           + "'n' time-steps previously, divided by the earlier price.  This feature captures "
           + "the trendfollower's belief that the expected future return equals the previous "
           + "return, with the trend of past price movements continuing on into the future.";
   }
   
   @Layout(
      Order = 1.3,
      FieldName = "Fundamentalist-Chartist Weight",
      VerboseDescription = "Behavior"
      )
   @Submodel
   @Parameter(
      ID = "BANK_STUB_STRATEGY_WEIGHT"
      )
   private TimeseriesDoubleModelParameterConfiguration
      fundamentalistChartistWeight = new ConstantDoubleModelParameterConfiguration(
         DEFAULT_FUNDAMENTALIST_CHARTIST_WEIGHT);
   
   public TimeseriesDoubleModelParameterConfiguration getFundamentalistChartistWeight() {
      return fundamentalistChartistWeight;
   }
   
   /**
     * Set the degree to which {@link Bank} {@link Agent}{@code s} configured by this
     * module are fundamentalists or chartists. The argument must take values 
     * in the range {@code [0, 1]}.
     */
   public void setFundamentalistChartistWeight(
      final TimeseriesDoubleModelParameterConfiguration value) {
      this.fundamentalistChartistWeight = value;
   }
   
   @Submodel
   @Layout(
      Order = 3.1,
      VerboseDescription = "Market Responses",
      FieldName = "Market Behaviour"
      )
   @Parameter(
      ID = "BANK_CLEARING_MARKET_RESPONSES"
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
   
   @Layout(
      Order = 4.0,
      Title = "Investment Portfolio",
      VerboseDescription = "Portfolio Decisions",
      FieldName = "Investment Target Smoothing"
      )
   @Submodel
   @Parameter(
      ID = "BANK_STUB_STRATEGY_PORTFOLIO_SMOOTHING_ALGORITHM"
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
   
   @Submodel
   @Layout(
      Order = 4.1,
      FieldName = "Investment Portfolio Weighting Algorithm"
      )
   @Parameter(
      ID = "BANK_STUB_STRATEGY_PORTFOLIO_WEIGHTING_ALGORITHM"
      )
   private PortfolioWeightingAlgorithmConfiguration
      bankPortfolioWeightingAlgorithm = new ExponentialAdaptivePortfolioWeightingConfiguration();
   
   public PortfolioWeightingAlgorithmConfiguration getBankPortfolioWeightingAlgorithm() {
      return bankPortfolioWeightingAlgorithm;
   }
   
   public void setBankPortfolioWeightingAlgorithm(
      final PortfolioWeightingAlgorithmConfiguration value) {
      bankPortfolioWeightingAlgorithm = value;
   }
   
   /**
     * Verbose description for parameter {@link #getInitialCashEndowmentPerBank}.
     */
  public final String desBankPortfolioWeightingAlgorithm() {
      return "The algorithm used by the Banks to calculate their investment portfolio weights "
            + "across instruments such as stocks and loans.  The options include:"
            + "\n\tExponential Adaptive Portfolio - if the average expected "
            + "return over all distinct instruments weighted by the portfolio is r', then the "
            + "weight w applied to each instrument is perturbed according to "
            + "w -> w * (1 + m * (r - r')) where r is the expected return for the instrument,"
            + " and m is a fixed constant non-negative "
            + "adaptation rate parameter. "
            + "\n\tHomogeneous Portfolio - all known instruments receive the same investment "
            + "weight. "
            + "\n\tLinear Adaptive Portfolio - if the average expected return over all distinct "
            + "instruments "
            + "weighted by the portfolio is r', then the weight w applied to each instrument"
            + "is perturbed according to w -> w + m * (r - r') where"
            + " r is the expected return for the instrument"
            + " and m is a fixed constant non-negative"
            + " adaptation rate parameter. "
            + "\n\tLogit Portfolio - if the maximum return over all distinct instruments weighted"
            + " by the portfolio is r_max, then the (un-normalised) weight applied to each"
            + " instrument is w -> exp(beta * (r - r_max)), where r"
            + " is the expected return for the instrument and beta is the intensity of choice, a "
            + "non-negative parameter."
            + "\n\tNoisy Portfolio - the natural logarithm of the portfolio weights are evolved as"
            + " autoregressive AR(1) processes "
            + "such that log_w -> log_w * rho + Z, where log_w is the log-weight of a particular"
            + "instrument, rho is the lag-1 autocorrelation "
            + "parameter and Z is a random noise term"
            + " sampled from a Normal Distribution with zero mean and variance equal to the "
            + "noise scale parameter, Z ~ N(0, noise scale).  The weights are subsequently "
            + "normalised.";
   }
   
   @Submodel
   @Layout(
      Order = 4.2,
      FieldName = "Leverage Target Algorithm"
      )
   @Parameter(
      ID = "BANK_STUB_STRATEGY_LEVERAGE_TARGET_ALGORITHM"
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
      ID = "BANK_BANKRUPTCY_RESOLUTION_POLICY",
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
   
   @Layout(
      Order = 5.0,
      Title = "Remote Control Decisions",
      VerboseDescription = "Customizable Financial Decisions",
      FieldName = "Portfolio Investment Size"
      )
   @Submodel
   @Parameter(
      ID = "BANK_STUB_STRATEGY_PORTFOLIO_SIZE_TARGETS"
      )
   private TimeseriesDoubleModelParameterConfiguration
      portfolioSizeDecision = new ConstantDoubleModelParameterConfiguration(3.5e9 * 20.);
   
   @Layout(
      Order = 5.1,
      FieldName = "Total Dividend Payment"
      )
   @Submodel
   @Parameter(
      ID = "BANK_STUB_STRATEGY_TOTAL_DIVIDEND_PAYMENTS"
      )
   private TimeseriesDoubleModelParameterConfiguration
      dividendPaymentDecision = new ConstantDoubleModelParameterConfiguration(1.2e10);
   
   public TimeseriesDoubleModelParameterConfiguration getPortfolioSizeDecision() {
      return portfolioSizeDecision;
   }
   
   public void setPortfolioSizeDecision(
      final TimeseriesDoubleModelParameterConfiguration value) {
      this.portfolioSizeDecision = value;
   }
   
   public TimeseriesDoubleModelParameterConfiguration getDividendPaymentDecision() {
      return dividendPaymentDecision;
   }
   
   public void setDividendPaymentDecision(
      final TimeseriesDoubleModelParameterConfiguration value) {
      this.dividendPaymentDecision = value;
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(
         numberOfSharesToEmitPerBank > 0.,
         toParameterValidityErrMsg("Number of shares per bank is non-positive."));
      Preconditions.checkArgument(
         initialStockEmissionValuePerBank >= 0.,
         toParameterValidityErrMsg("Initial stock emission value per bank is negative."));
      Preconditions.checkArgument(
         bankStockRiskPremium >= 0,
         toParameterValidityErrMsg("Fundamentalist Stock Risk Premium is negative."));
      Preconditions.checkArgument(
         trendFollowerBankLag >= 0,
         toParameterValidityErrMsg("Trend-Follower Lag Factor is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_SHARES_TO_EMIT_PER_BANK",               numberOfSharesToEmitPerBank,
         "INITIAL_STOCK_EMISSION_VALUE_PER_BANK",           initialStockEmissionValuePerBank,
         "BANK_STUB_STRATEGY_STOCK_RISK_PREMIUM",
            bankStockRiskPremium,
         "BANK_STUB_STRATEGY_LAG",
            trendFollowerBankLag,
         "BANK_STUB_STRATEGY_WEIGHT",
            fundamentalistChartistWeight
         ));
      install(new FactoryModuleBuilder()
         .implement(ClearingBankStrategy.class, BankStubClearingBankStrategy.class)
         .build(ClearingBankStrategyFactory.class)
         );
      bind(CommercialBank.class).to(BankStub.class);
      super.addBindings();
   }
}