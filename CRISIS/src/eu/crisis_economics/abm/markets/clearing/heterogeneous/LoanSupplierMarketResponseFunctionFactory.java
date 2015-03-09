/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.RaisingAgentOperation;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.bank.strategies.clearing.ClearingBankStrategy;
import eu.crisis_economics.abm.fund.PortfolioFund;

/**
  * An implementation of the {@link MarketResponseFunction} interface for
  * {@link CommercialBank} and {@link PortfolioFund} loan suppliers. The 
  * resulting response function specifies loan supply to a named clearing market.
  * At present, if the calling {@link Agent} is not a {@link CommercialBank},
  * or a {@link PortfolioFund}, this factory returns {@code null}. 
  * 
  * @author phillips
  */
public final class LoanSupplierMarketResponseFunctionFactory
   extends RaisingAgentOperation<MarketResponseFunction> {
   
   public final static double
      DEFAULT_LOAN_SUPPLY_EXPONENT = .2,
      DEFAULT_MAXIMUM_INTEREST_RATE = 1.;
   
   private final ClearingLoanMarket
      market;
   
   private final double
      exponent,
      maximumInterestRate;
   
   /**
     * Create a {@link LoanSupplierMarketResponseFunctionFactory}
     * with custom parameters. This factory will generate a 
     * loan supplier {@link MarketResponseFunction} for a loan
     * {@link ClearingMarket}.
     * 
     * @param market
     *        The {@link ClearingMarket} for which a {@link MarketResponseFunction}
     *        should be generated. This argument must be an instance of 
     *        {@link ClearingLoanMarket}.
     * @param exponent
     *        The polynomial exponent of the market response function.
     *        This argument should be non-negative.
     * @param maximumInterestRate
     *        The interest rate above which loan supply is maximized.
     *        This argument should be strictly positive.
     */
   @Inject
   public LoanSupplierMarketResponseFunctionFactory(
   @Assisted
      final ClearingMarket market,
   @Named("CREDIT_SUPPLY_FUNCTION_EXPONENT")
      final double exponent,
   @Named("CREDIT_SUPPLY_FUNCTION_MAXIMUM_INTEREST_RATE")
      final double maximumInterestRate
      ) {
      Preconditions.checkNotNull(market);
      Preconditions.checkArgument(exponent >= 0.);
      Preconditions.checkArgument(maximumInterestRate > 0.);
      Preconditions.checkArgument(market instanceof ClearingLoanMarket);
      this.market = (ClearingLoanMarket) market;
      this.exponent = exponent;
      this.maximumInterestRate = maximumInterestRate;
   }
   
   /**
     * Create a {@link LoanSupplierMarketResponseFunctionFactory}
     * with default parameters for a commercial loan clearing market
     * with the specified name.
     */
   public LoanSupplierMarketResponseFunctionFactory(
      final ClearingMarket market) {
      this(
         market,
         DEFAULT_LOAN_SUPPLY_EXPONENT,
         DEFAULT_MAXIMUM_INTEREST_RATE
         );
   }
   
   public MarketResponseFunction operateOn(final PortfolioFund fund) {
      double loanSupply =
         fund.getDesiredLoanInvestmentIn(market.getMarketName());
      System.out.printf("Fund: desired loan investment: %16.10g\n", loanSupply);
      return create(Math.max(loanSupply, 0.));
   }
   
   public MarketResponseFunction operateOn(final CommercialBank bank) {
      final ClearingBankStrategy
         strategy = bank.getStrategy();
      final double loanSupply =
         strategy.getDesiredLoanInvestmentIn(market);
      System.out.printf("Bank: desired loan investment: %16.10g\n", loanSupply);
      return create(Math.max(loanSupply, 0.));
   }
   
   private MarketResponseFunction create(final double loanSupply) {
      if(loanSupply <= 1.)                                                 // Negligible
         return null;
      return new PartitionedResponseFunction(
         new ExpIOCPartitionFunction(1./maximumInterestRate),
         new PolynomialSupplyInnerResponse(exponent, maximumInterestRate, loanSupply)
         );
   }
   
   public ClearingMarket getMarket() {
      return market;
   }
   
   public double getExponent() {
      return exponent;
   }
   
   public double getMaximumInterestRate() {
      return maximumInterestRate;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return
         "Loan Supplier Market Response Function Factory, loan type: "
        + market + ", exponent: " + exponent + ", maximum interest rate: "
        + maximumInterestRate + ".";
   }
}
