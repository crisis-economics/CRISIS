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
import com.google.inject.Inject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.fund.MutualFund;
import eu.crisis_economics.abm.fund.NoiseTraderFund;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.FundFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.abm.simulation.injection.factories.MutualFundFactory;
import eu.crisis_economics.abm.simulation.injection.factories.NoiseTraderFundFactory;

@ConfigurationComponent(
   DisplayName = "Mutual Funds"
   )
public class MutualFundSubEconomy extends AbstractFundSubEconomy {
   
   /*                                 *
    * Model parameter default values. *
    *                                 */
   
   private static final long serialVersionUID = -1400849785617654760L;
   
   public static final int 
      DEFAULT_NUMBER_OF_FUNDS = 8,
      DEFAULT_NUMBER_OF_FUNDS_ARE_NOISE_TRADERS = 0;
   
   public static final double
      DEFAULT_NOISE_TRADER_FUND_RHO = .9,
      DEFAULT_NOISE_TRADER_FUND_NOISE_AMPLITUDE = .3;
   
   @Parameter(
      ID = "NUMBER_OF_FUNDS_ARE_NOISE_TRADERS"
      )
   private int
      numberOfFundsAreNoiseTraders = DEFAULT_NUMBER_OF_FUNDS_ARE_NOISE_TRADERS;
    
   /**
     * Get the number of {@link Fund} agents in this model.
     */
   public final int getNumberOfFundsAreNoiseTraders() {
      return numberOfFundsAreNoiseTraders;
   }
   
   /**
     * Set the total number of {@link Fund} agents in this model. The argument
     * should be strictly positive.
     */
   public final void setNumberOfFundsAreNoiseTraders(final int value) {
      this.numberOfFundsAreNoiseTraders = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getNumberOfFundsAreNoiseTraders}.
     */
   public final String desNumberOfFundsAreNoiseTraders() {
      return 
         "Of the total number of fund agents in this model, how many are noise-tranders?";
   }
   
   @Parameter(
      ID = "NOISE_TRADER_FUND_RHO"
      )
   private double
      noiseTraderFundRho = DEFAULT_NOISE_TRADER_FUND_RHO;
   @Parameter(
      ID = "NOISE_TRADER_FUND_NOISE_AMPLITUDE"
      )
   private double
      noiseTraderFundNoiseAmplitude = DEFAULT_NOISE_TRADER_FUND_NOISE_AMPLITUDE;
   
   /**
    * Get the cash value of the initial {@link Bank} share emission.
    */
   public final double getNoiseTraderFundRho() {
      return noiseTraderFundRho;
   }
  
   /**
     * Set the cash value of the initial {@link Bank} share emission. The
     * argument to this method should be non-negative.
     */
   public final void setNoiseTraderFundRho(final double value) {
      noiseTraderFundRho = value;
   }
   
   /**
     * Verbose description for parameter {@link #getNoiseTraderFundRho}.
     */
   public final String desNoiseTraderFundRho() {
      return "The noise exponent for noise-trading funds.";
   }
   
   /**
     * Get the amplitude of {@link NoiseTraderFund} stock investment noise.
     */
   public final double getNoiseTraderFundNoiseAmplitude() {
      return noiseTraderFundNoiseAmplitude;
   }
   
   /**
     * Set the amplitude of {@link NoiseTraderFund} stock investment noise. The 
     * argument should be non-negative.
     */
   public final void setNoiseTraderFundNoiseAmplitude(final double value) {
      noiseTraderFundNoiseAmplitude = value;
   }
   
   /**
     * Verbose description for parameter {@link #getNoiseTraderFundNoiseAmplitude}.
     */
   public final String desNoiseTraderFundNoiseAmplitude() {
      return "The amplitude of (mean-reverting) noise applied to stock investments by noise-"
           + "trader funds.";
   }
   
   /**
     * Verbose description for parameter {@link #getMutualFundStockRiskPremium}.
     */
   public final String desMutualFundStockRiskPremium() {
      return "The stock risk premium applied to stock investment returns by mutual funds.";
   }
   
   /*            *
    * Submodels  *
    *            */
   
   @Submodel
   @Layout(
      Order = 2.0,
      Title = "Investment Portfolio",
      VerboseDescription = "Portfolio Decisions",
      FieldName = "Portfolio"
      )
   @Parameter(
      ID = "FUNDS_PORTFOLIO"
      )
   private SimpleStockAndLoanPortfolioConfiguration
      portfolio = new SimpleStockAndLoanPortfolioConfiguration(
         new ExponentialAdaptivePortfolioWeightingConfiguration(0.1),
         new MovingMedianStockReturnExpectationFunctionConfiguration(
         new LinearCombinationStockReturnExpectationFunctionConfiguration(
            new FundamentalistStockReturnExpectationFunctionConfiguration(1.0), 
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
   
   @Submodel
   @Layout(
      Order = 3.0,
      Title = "Market Responses",
      VerboseDescription = "Market Responses",
      FieldName = "Market Behaviour"
      )
   @Parameter(
      ID = "FUNDS_CLEARING_MARKET_RESPONSES"
      )
   FundMarketParticipationConfiguration fundClearingMarketParticipation =
      new FundMarketParticipationConfiguration();
   
   public FundMarketParticipationConfiguration getFundClearingMarketParticipation() {
      return fundClearingMarketParticipation;
   }
   
   public void setFundClearingMarketParticipation(final FundMarketParticipationConfiguration value) {
      fundClearingMarketParticipation = value;
   }
   
   @Override
   protected void assertParameterValidity() {
      super.assertParameterValidity();
      Preconditions.checkArgument(numberOfFundsAreNoiseTraders >= 0,
         toParameterValidityErrMsg("the number of noise-trader funds is negative."
         ));
      Preconditions.checkArgument(numberOfFundsAreNoiseTraders <= getNumberOfFunds(),
         toParameterValidityErrMsg(
            "the number of noise-trader funds is greater than the total number of funds."
         ));
      Preconditions.checkArgument(noiseTraderFundRho >= 0.,
         "the value of the noise-trader fund noise exponent (rho) is negative."
         );
      Preconditions.checkArgument(noiseTraderFundNoiseAmplitude >= 0.,
         "the value of the noise-trader fund noise amplitude is negative."
         );
   }
   
   /**
     * A private factory for {@link Fund} {@link Agent}{@code s} created via this 
     * {@link ComponentConfiguration}. This factory will initially return as many 
     * {@link NoiseTraderFund}{@code s} as have been specified by the enclosing 
     * {@link MutualFundSubEconomy}, and subsequently it will return {@link MutualFund} 
     * {@link Agent}{@code s}. If the number of {@link Fund} {@link Agent}{@code s} 
     * requested of this factory is greater than the number of funds configured by 
     * the enclosing instance of {@link MutualFundSubEconomy}, {@link IllegalStateException}
     * is raised.
     * 
     * @author phillips
     */
   private static class FundFactoryImpl implements FundFactory {
      private int
         useCounter = 0;
      private final int
         numberOfFunds,
         numberOfFundsAreNoiseTraders;
      
      @Inject
      MutualFundFactory mutualFundFactory;
      
      @Inject
      NoiseTraderFundFactory noiseTraderFundFactory;
      
      @Inject
      FundFactoryImpl(
         @Named("NUMBER_OF_FUNDS")
         final int numberOfFunds,
         @Named("NUMBER_OF_FUNDS_ARE_NOISE_TRADERS")
         final int numberOfFundsAreNoiseTraders
         ) {
         this.numberOfFunds = numberOfFunds;
         this.numberOfFundsAreNoiseTraders = numberOfFundsAreNoiseTraders;
      }
      
      @Override
      public Fund create(final DepositHolder depositHolder) {
         if(useCounter == numberOfFunds)
            throw new IllegalStateException(
               "Funds Subeconomy: too many funds have been initialized. This subeconomy "
             + "has been configured to provide " + numberOfFunds + " fund agents, however "
             + "more than this number have now been requested."
               );
         Fund fund = 
            (useCounter < numberOfFundsAreNoiseTraders) ? 
            noiseTraderFundFactory.create(depositHolder) : mutualFundFactory.create(depositHolder);
         ++useCounter;
         return fund;
      }
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_FUNDS_ARE_NOISE_TRADERS",   numberOfFundsAreNoiseTraders,
         "NOISE_TRADER_FUND_RHO",               noiseTraderFundRho,
         "NOISE_TRADER_FUND_NOISE_AMPLITUDE",   noiseTraderFundNoiseAmplitude
         ));
      install(new FactoryModuleBuilder()
         .implement(Fund.class, MutualFund.class)
         .build(MutualFundFactory.class)
         );
      install(new FactoryModuleBuilder()
         .implement(Fund.class, NoiseTraderFund.class)
         .build(NoiseTraderFundFactory.class)
         );
      bind(FundFactory.class).to(FundFactoryImpl.class).asEagerSingleton();
      expose(FundFactory.class);
      expose(Integer.class).annotatedWith(Names.named("NUMBER_OF_FUNDS_ARE_NOISE_TRADERS"));
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Mutual Fund Subeconomy Component: " + super.toString();
   }
}
