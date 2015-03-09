/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Milan Lovric
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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
package eu.crisis_economics.abm.firm;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import ec.util.MersenneTwisterFast;
import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.BoundedUnivariateFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.InverseExpIOCPartitionFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.PartitionedResponseFunction;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A stub {@link Firm} implementation with partial exogenous behaviour.<br><br>
  * 
  * The {@link Firm} pays random dividends using a stochastic process. In particular,
  * the dividend {@code D} to pay in the next simulation cycle is modelled as a 
  * mean-reverting random walk defined as follows:<br><br>
  *   
  *   <code>
  *   D_{t+1} = D_t + alpha * (D* - D_t) + sigma * epsilon_t
  *   </code><br><br>
  *   
  * where
  * <ul>
  *    <li> {@code alpha} is the mean reversion rate;
  *    <li> {@code D*} is the mean reversion level (the long-run equilibrium);
  *    <li> {@code sigma} is the standard deviation (the volatility) of noise, and
  *    <li> {@code epsilon_t} is a random shock.
  * </ul>
  * 
  * The {@link Firm} production function (goods production) is mocked so as to enable the 
  * payment of random dividends and loans. This {@link Firm} produces no goods, but is 
  * debitted with {@code (a)} {@link Loan} installment repayment cash, as required, and 
  * {@code (b)} sufficient cash to pay dividends. This cash is sourced exogenously.<br><br>
  * 
  * This {@link Firm} responds to {@link ClearingLoanMarket}{@code s} by posting 
  * a polynomial credit demand function. The exponent of the credit demand function
  * is customizable. The maximum credit demand per simulation cycle is taken to be 
  * {@code D*}. The markup rate (maximum borrowing interest rate) for this {@link Firm}
  * is taken to be {@code 1.0}.<br><br>
  * 
  * This {@link Firm} stub does not employ a workforce. This {@link Firm} does not 
  * offer goods for sale to other {@link Agent}{@code s} in the model, therefor 
  * its selling price is {@code 0}. Similarly, the unsold quantity of goods is {@code zero}.
  * 
  * @author lovric
  * @author phillips
  */
public final class FirmStub extends ClearingFirm {
   
   // Default parameters for the stochastic dividend process
   public static final double
      DEFAULT_INITIAL_DIVIDEND_PER_SHARE = 0.05,           // starting value (dividend per share)
      DEFAULT_DIVIDEND_NOISE_DEV = 2.e6,                   // deviation of the noise component
      DEFAULT_MEAN_REVERSION_LEVEL = 1.e8,                 // mean reversion level (long-run 
                                                           //  equilibrium) for the mean-reverting 
                                                           //  process.
      DEFAULT_MEAN_REVERSION_RATE = 0.01;                  // mean reversion rate for the mean-
                                                           //  reverting process
   
   // Parameters of the stochastic dividend process (can be firm-specific)
   private double
      meanReversionLevel,
      meanReversionRate,
      dividendNoiseDev;
   
   // A random generator to be used for the noise component of the dividend process
   private MersenneTwisterFast
      randomShock; 
   
   private CreditDemandFunction
      creditDemandFunction;
   
   /**
     * Create a {@link FirmStub} {@link Agent} with custom parameters. 
     * 
     * @param depositHolder
     *        The {@link DepositHolder} who manages the {@link Firm} deposit account.
     * @param initialDeposit
     *        The initial cash endowment.
     * @param stockHolder
     *        The {@link StockHolder} who, initially, owns shares in this {@link Firm}.
     * @param numberOfShares
     *        The number of shares to emit. This argument should be non-negative.
     * @param emissionPrice
     *        The initial price per share (PPS) for shares in this {@link Firm}. This
     *        argument should be non-negative.
     * @param initialDividendPerShare
     *        The initial dividend per share (DPS) payment for this {@link Firm}. This
     *        argument should be non-negative.
     * @param meanReversionLevel
     *        The mean reversion level (the long-run equilibrium) for the dividend per 
     *        share (DPS) stochastic payment process. This argument should be non-negative.
     * @param meanReversionRate
     *        The mean reversion rate (rate of change) for the dividend per share
     *        (DPS) stochastic process. This argument should be non-negative.
     * @param creditDemandFunction
     *        The commercial loan {@link CreditDemandFunction} to be used by this
     *        {@link Firm}.
     * @param name
     *        A unique {@link String} name for this {@link Agent}.
     */
   @Inject
   public FirmStub(
   @Assisted
      final DepositHolder depositHolder,
   @Named("FIRM_INITIAL_CASH_ENDOWMENT")
      final double initialDeposit,
   @Assisted
      final StockHolder stockHolder,
   @Assisted
      final double numberOfShares,
   @Named("FIRM_INITIAL_PRICE_PER_SHARE")
      final double emissionPrice,
   @Named("FIRM_STUB_CREDIT_DEMAND_FUNCTION")
      final CreditDemandFunction creditDemandFunction,
   @Named("FIRM_STUB_INITIAL_DIVIDEND_PER_SHARE")
      final double initialDividendPerShare,
   @Named("FIRM_STUB_MEAN_REVERSION_LEVEL")
      final double meanReversionLevel,
   @Named("FIRM_STUB_MEAN_REVERSION_RATE")
      final double meanReversionRate,
   @Named("FIRM_STUB_NOISE_DEV")
      final double dividendNoiseDev,
   @Named("FIRM_NAME_GENERATOR")
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            emissionPrice,
            nameGenerator
            );
      Preconditions.checkArgument(numberOfShares >= 0.);
      Preconditions.checkArgument(emissionPrice >= 0.);
      Preconditions.checkArgument(initialDividendPerShare >= 0.);
      Preconditions.checkArgument(meanReversionLevel >= 0.);
      Preconditions.checkArgument(meanReversionRate >= 0.);
      this.setDividendPerShare(initialDividendPerShare);
      this.meanReversionLevel = meanReversionLevel;
      this.meanReversionRate = meanReversionRate;
      this.dividendNoiseDev = dividendNoiseDev;
      this.creditDemandFunction = Preconditions.checkNotNull(creditDemandFunction);
      this.randomShock =  new MersenneTwisterFast(Simulation.getSimState().random.nextLong());
      
      scheduleSelf();
   }
   
   /**
     * Create a {@link FirmStub} {@link Agent} with default parameters. See
     * {@link FirmStub#FirmStub(DepositHolder, double, StockHolder, double, double, 
     * double, double, double)}.
     */
   public FirmStub(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final StockHolder stockHolder,
      final double numberOfShares,
      final double emissionPrice,
      final CreditDemandFunction creditDemandFunction,
      final AgentNameFactory nameGenerator
      ) {
      this(
         depositHolder, initialDeposit, stockHolder,
         numberOfShares, emissionPrice, creditDemandFunction,
         DEFAULT_INITIAL_DIVIDEND_PER_SHARE,
         DEFAULT_MEAN_REVERSION_LEVEL,
         DEFAULT_MEAN_REVERSION_RATE,
         DEFAULT_DIVIDEND_NOISE_DEV,
         nameGenerator
         );
   }
   
   private void scheduleSelf() {
      Simulation.repeat(this, "setNextDividend", NamedEventOrderings.FIRM_ACCOUNTING);
   }
   
   /**
    * Production output is mocked so as to enable the payment of random dividends and loans.
    * 
    * @return production output
    */
   private double productionFunction() {
       double
          totalLoanPaymentsDue = 0;
       for(final Contract contract : getLiabilities())
          if(contract instanceof Loan)
             totalLoanPaymentsDue += ((Loan) contract).getNextInstallmentPayment();
       return totalLoanPaymentsDue + getCurrentDividend();
   }
   
   @Override
   protected void considerCommercialLoanMarkets() { }
   
   @Override
   protected void considerDepositPayment() { }
   
   private double
      mostRecentRevenue,
      mostRecentTotalProduction;
   
   @Override
   protected void considerProduction() {
      mostRecentRevenue = productionFunction();
      mostRecentTotalProduction = mostRecentRevenue;
      debit(mostRecentRevenue);
   }
    
   /**
     * Calculate, and set, next-period random dividend (per share) using a stochastic
     * process.<br><br>
     * 
     * The stochastic process is a mean reverting random walk. The dividend {@code D}
     * to pay is determined as follows:<br><br>
     *   
     *   <code>
     *   D_{t+1} = D_t + alpha * (D* - D_t) + sigma * epsilon_t
     *   </code><br><br>
     *   
     * where
     * <ul>
     *    <li> {@code alpha} is the mean reversion rate;
     *    <li> {@code D*} is the mean reversion level (the long-run equilibrium);
     *    <li> {@code sigma} is the standard deviation (the volatility) of noise, and
     *    <li> {@code epsilon_t} is a random shock.
     * </ul>
     */
   @SuppressWarnings("unused")
   private void setNextDividend() {
      final double 
         existingTotalDividend = getDividendPerShare() * getNumberOfEmittedShares();
      
      /*
      // Logarithmic version (ensures positive dividend value; requires smaller noise deviation)
      double logDividend = Math.log(dividend);
      double logMeanReversionLevel = Math.log(meanReversionLevel);
      double logDividendNext =
         logDividend + meanReversionRate * (logMeanReversionLevel - logDividend) + 
         DEFAULT_DIVIDEND_NOISE_DEV * randomShock.nextGaussian();
      double dividendNext = Math.exp(logDividendNext);
      */
      
      // Non-logarithmic version
      double nextDividend = 
         existingTotalDividend + meanReversionRate * (meanReversionLevel - existingTotalDividend) 
       + dividendNoiseDev * randomShock.nextGaussian();
      
      this.setDividendPerShare(Math.max(0., nextDividend) / getNumberOfEmittedShares());
   }
   
   @Override
   public MarketResponseFunction getMarketResponseFunction(
      final ClearingMarketInformation caller
      ) {
      if(ClearingLoanMarket.class.isAssignableFrom(caller.getMarketType()))
         return new PartitionedResponseFunction(
            new InverseExpIOCPartitionFunction(1.),
            new BoundedUnivariateFunction() {
               @Override
               public double value(double interestRate) {
                  return creditDemandFunction.getCreditDemandResponse(
                     meanReversionLevel, 1.0, interestRate);
               }
               @Override
               public double getMaximumInDomain() {
                   return 1.;
               }
               @Override
               public double getMinimumInDomain() {
                  return 0;
               }
            });
      return null;
   }
   
   @Override
   public double getRevenue() {
      return mostRecentRevenue;
   }
   
   @Override
   public double getProfit() {
      return getCurrentDividend();
   }
   
   @Override
   public double getEmployment() {
      return 0.;
   }
   
   @Override
   public double getProduction() {
      return mostRecentTotalProduction;
   }
   
   @Override
   public double getGoodsSellingPrice() {
      return 0.;
   }
   
   @Override
   public double getUnsold() {
      return 0;
   }
   
   @Override
   public void handleBankruptcy() {
      System.err.println(
         "FirmStub: a firm stub is bankrupt. The firm stub implementation receives funds from "
       + "exogenous sources. As a result, firm stub bankruptcy is not expected in the current "
       + "implementation. This event is indicative of a simulation error. The simulation will "
       + "proceed."
         );
      System.err.flush();
   }
}