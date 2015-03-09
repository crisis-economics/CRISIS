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
import eu.crisis_economics.abm.government.ComputeGiltCashDemandOperation;
import eu.crisis_economics.abm.government.Government;

/**
  * A factory object creating {@link MarketResponseFunction}{@code s} for participants
  * in loan {@link ClearingMarket}{@code s}.<br><br>
  * 
  * The {@link MarketResponseFunction} created by this factory specifies the 
  * demand for credit by a loan market participant. The participant is treated
  * as a {@link Lender}. In the case of a {@link Government} {@link Agent},
  * the credit demand is the amount of cash the {@link Government} wishes to
  * raise in gilts.
  * 
  * @author phillips
  */
public final class LoanConsumerMarketResponseFunctionFactory
   extends RaisingAgentOperation<MarketResponseFunction> {
   
   public final static double
      DEFAULT_LOAN_DEMAND_EXPONENT = 2.,
      DEFAULT_MAXIMUM_INTEREST_RATE = .5;
   
   private final ClearingMarket
      market;
   
   private final double
      exponent,
      maximumInterestRate;
   
   /**
     * Create a {@link LoanConsumerMarketResponseFunctionFactory}
     * with custom parameters for a loan {@link ClearingMarket} with the
     * specified name.
     * 
     * @param market
     *        The {@link ClearingMarket} for which a {@link MarketResponseFunction}
     *        should be generated.
     * @param exponent
     *        The polynomial exponent of the market response function.
     *        This argument should be non-negative.
     * @param maximumInterestRate
     *        The interest rate above which loan supply is minimized.
     *        This argument should be strictly positive.
     */
   @Inject
   public LoanConsumerMarketResponseFunctionFactory(
   @Assisted
      final ClearingMarket market,
   @Named("CREDIT_DEMAND_FUNCTION_EXPONENT")
      final double exponent,
   @Named("CREDIT_DEMAND_FUNCTION_MAXIMUM_INTEREST_RATE")
      final double maximumInterestRate
      ) {
      Preconditions.checkNotNull(market);
      Preconditions.checkArgument(exponent >= 0.);
      Preconditions.checkArgument(maximumInterestRate > 0.);
      this.market = market;
      this.exponent = exponent;
      this.maximumInterestRate = maximumInterestRate;
   }
   
   /**
     * Create a {@link LoanConsumerMarketResponseFunctionFactory}
     * with default parameters for a commercial loan clearing market
     * with the specified name.
     */
   public LoanConsumerMarketResponseFunctionFactory(final ClearingMarket market) {
      this(
         market,
         DEFAULT_LOAN_DEMAND_EXPONENT,
         DEFAULT_MAXIMUM_INTEREST_RATE
         );
   }
   
   public MarketResponseFunction operateOn(final Government government) {
      double
         loanDemand = ComputeGiltCashDemandOperation.on(government);
      System.out.printf("Government: desired gilt debt: %16.10g\n", loanDemand);
      return create(Math.max(loanDemand, 0.));
   }
   
   private MarketResponseFunction create(final double loanDemand) {
      if(loanDemand <= 1.)                                                 // Negligible
         return null;
      return new PartitionedResponseFunction(
         new InverseExpIOCPartitionFunction(1./maximumInterestRate),
         new PolynomialDemandInnerResponse(exponent, maximumInterestRate, loanDemand)
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
         "Loan Consumer Market Response Function Factory, market name: "
        + market.getMarketName() + ", exponent: " + exponent + ", maximum interest rate: "
        + maximumInterestRate + ".";
   }
}
