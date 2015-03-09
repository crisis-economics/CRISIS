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

import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.crisis_economics.abm.firm.FirmFactory;
import eu.crisis_economics.abm.firm.FirmStub;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   Description = 
      "A stub Firm implementation with partial exogenous behaviour.\n\n" + 
      "" + 
      "This Firm pays random dividends using a stochastic process. The dividend <code>D</code>" + 
      "to pay in the next simulation cycle is modelled as a mean-reverting random walk defined " +
      "as follows:\n\n" + 
      "  " + 
      "  <center><code>" + 
      "  D_{t+1</code> = D_t + alpha  (D - D_t) + sigma * epsilon_t" + 
      "  </code></center>" + 
      "  " + 
      "where" + 
      "<ul>" + 
      "   <li> <code>alpha</code> is the mean reversion rate;" + 
      "   <li> <code>D</code> is the mean reversion level (the long-run equilibrium);" + 
      "   <li> <code>sigma</code> is the standard deviation (the volatility) of noise, and" + 
      "   <li> <code>epsilon_t</code> is a random shock." + 
      "</ul>" + 
      "" + 
      "The production function for this Firms is mocked so as to enable the payment of random " +
      "dividends and loans. This Firm produces no goods, but rather is debitted with (a) Loan" +
      " installment repayment cash, as required, and (b) sufficient cash to pay dividends. This " + 
      "cash is sourced exogenously.\n\n" + 
      "" + 
      "This Firm responds to commercial loan clearing markets by posting a polynomial credit " + 
      "demand function. The exponent of the credit demand function is customizable. The maximum " + 
      "credit demand per simulation cycle is taken to be <code>D</code>. The markup rate " +
      "(maximum borrowing interest rate) for this Firm is taken to be <code>1.0</code>."
      )
public final class FirmStubSubEconomyConfiguration extends AbstractFirmsSubEconomy {
   
   private static final long serialVersionUID = -1560515999319878744L;
   
   @Parameter(
      ID = "FIRM_STUB_INITIAL_DIVIDEND_PER_SHARE"
      )
   private double
      initialDividendPerShare = FirmStub.DEFAULT_INITIAL_DIVIDEND_PER_SHARE;
   @Parameter(
      ID = "FIRM_STUB_MEAN_REVERSION_LEVEL"
      )
   private double
      meanReversionLevel = FirmStub.DEFAULT_MEAN_REVERSION_LEVEL;
   @Parameter(
      ID = "FIRM_STUB_MEAN_REVERSION_RATE"
      )
   private double
      meanReversionRate = FirmStub.DEFAULT_MEAN_REVERSION_RATE;
   @Parameter(
      ID = "FIRM_STUB_NOISE_DEV"
      )
   private double
      dividendNoiseDev = FirmStub.DEFAULT_DIVIDEND_NOISE_DEV;
   
   public double getInitialDividendPerShare() {
      return initialDividendPerShare;
   }
   
   public void setInitialDividendPerShare(
      final double initialDividendPerShare) {
      this.initialDividendPerShare = initialDividendPerShare;
   }
   
   public double getMeanReversionLevel() {
      return meanReversionLevel;
   }
   
   public void setMeanReversionLevel(
      final double meanReversionLevel) {
      this.meanReversionLevel = meanReversionLevel;
   }
   
   public double getMeanReversionRate() {
      return meanReversionRate;
   }
   
   public void setMeanReversionRate(
      final double meanReversionRate) {
      this.meanReversionRate = meanReversionRate;
   }
   
   public double getDividendNoiseDev() {
      return dividendNoiseDev;
   }
   
   public void setDividendNoiseDev(
      final double dividendNoiseDev) {
      this.dividendNoiseDev = dividendNoiseDev;
   }
   
   @Submodel
   @Parameter(
      ID = "FIRM_STUB_CREDIT_DEMAND_FUNCTION"
      )
   private CreditDemandFunctionConfiguration
      creditDemandFunction = new CustomRiskToleranceCreditDemandFunctionConfiguration();
   
   public CreditDemandFunctionConfiguration getCreditDemandFunction() {
      return creditDemandFunction;
   }
   
   public void setCreditDemandFunction(
      final CreditDemandFunctionConfiguration creditDemandFunction) {
      this.creditDemandFunction = creditDemandFunction;
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
      "FIRM_STUB_INITIAL_DIVIDEND_PER_SHARE",      initialDividendPerShare,
      "FIRM_STUB_MEAN_REVERSION_LEVEL",            meanReversionLevel,
      "FIRM_STUB_MEAN_REVERSION_RATE",             meanReversionRate,
      "FIRM_STUB_NOISE_DEV",                       dividendNoiseDev
      ));
      install(new FactoryModuleBuilder()
         .implement(LoanStrategyFirm.class, FirmStub.class)
         .build(FirmFactory.class)
         );
      expose(FirmFactory.class);
      super.addBindings();
   }
}
