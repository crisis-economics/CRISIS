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
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.strategy.clearing.UncollateralizedReturnMaximizerMRFBuilder;

/**
  * An implementation of a Gilts/Bonds/Commercial Loan (BCL) clearing market
  * response function for {@link CommercialBank} {@link Agent}{@code s}. This 
  * implementation and its scope are subject to change.<br><br>
  * 
  * This algorithm will return a BCL {@link MarketResponseFunction} for 
  * a {@link CommercialBank} {@link Agent}. For any other type of market
  * participant, this algorithm returns {@code null}.
  * 
  * @author phillips
  */
public final class BCLCommercialLoanSupplierMarketResponseFunctionFactory
   extends RaisingAgentOperation<MarketResponseFunction> {
   
   public static final double
      DEFAULT_LOW_RISK_COMMERCIAL_LOAN_PREMIUM = 1.10,
      DEFAULT_MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM = 1.12,
      DEFAULT_HIGH_RISK_COMMERCIAL_LOAN_PREMIUM = 1.14,
      DEFAULT_GILT_RISK_PREMIUM = 1.00,
      DEFAULT_BOND_RISK_PREMIUM = 1.05;
   
   private final ClearingLoanMarket
      market;
   
   private final double
      lowRiskCommercialLoanPremium,
      mediumRiskCommercialLoanPremium,
      highRiskCommercialLoanPremium,
      giltRiskPremium,
      bondRiskPremium;
   
   /**
     * Create a {@link BCLCommercialLoanSupplierMarketResponseFunctionFactory} object
     * with custom parameters.
     * 
     * @param market
     *        The name of the {@link ClearingMarket} for which to generate a 
     *        {@link MarketResponseFunction}.
     * @param lowRiskCommercialLoanPremium
     *        The risk premium (return rate divisor) for low risk grade commercial
     *        loans. This argument should be strictly positive.
     * @param mediumRiskCommercialLoanPremium
     *        The risk premium (return rate divisor) for medium risk grade commercial
     *        loans. This argument should be strictly positive.
     * @param highRiskCommercialLoanPremium
     *        The risk premium (return rate divisor) for high risk grade commercial
     *        loans. This argument should be strictly positive.
     * @param giltRiskPremium
     *        The risk premium (return rate divisor) for gilts/government bond
     *        investments. This argument should be strictly positive.
     * @param bondRiskPremium
     *        The risk premium (return rate divisor) for bond liabilities. This
     *        argument should be strictly positive.
     */
   @Inject
   public BCLCommercialLoanSupplierMarketResponseFunctionFactory(
   @Assisted
      final ClearingMarket market,
   @Named("LOW_RISK_COMMERCIAL_LOAN_PREMIUM")
      final double lowRiskCommercialLoanPremium,
   @Named("MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM")
      final double mediumRiskCommercialLoanPremium,
   @Named("HIGH_RISK_COMMERCIAL_LOAN_PREMIUM")
      final double highRiskCommercialLoanPremium,
   @Named("GILT_RISK_PREMIUM")
      final double giltRiskPremium,
   @Named("BOND_RISK_PREMIUM")
      final double bondRiskPremium
      ) {
      Preconditions.checkArgument(lowRiskCommercialLoanPremium > 0.);
      Preconditions.checkArgument(mediumRiskCommercialLoanPremium > 0.);
      Preconditions.checkArgument(highRiskCommercialLoanPremium > 0.);
      Preconditions.checkArgument(giltRiskPremium > 0.);
      Preconditions.checkArgument(bondRiskPremium > 0.);
      Preconditions.checkArgument(market instanceof ClearingLoanMarket);
      Preconditions.checkNotNull(market);
      this.market = (ClearingLoanMarket) market;
      this.lowRiskCommercialLoanPremium = lowRiskCommercialLoanPremium;
      this.mediumRiskCommercialLoanPremium = mediumRiskCommercialLoanPremium;
      this.highRiskCommercialLoanPremium = highRiskCommercialLoanPremium;
      this.giltRiskPremium = giltRiskPremium;
      this.bondRiskPremium = bondRiskPremium;
   }
   
   /**
     * Create a {@link BCLCommercialLoanSupplierMarketResponseFunctionFactory} object with 
     * default parameters.
     */
   public BCLCommercialLoanSupplierMarketResponseFunctionFactory(
      final ClearingMarket market) {
      this(
         market,
         DEFAULT_LOW_RISK_COMMERCIAL_LOAN_PREMIUM,
         DEFAULT_MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM,
         DEFAULT_HIGH_RISK_COMMERCIAL_LOAN_PREMIUM,
         DEFAULT_GILT_RISK_PREMIUM,
         DEFAULT_BOND_RISK_PREMIUM
         );
   }
   
   public MarketResponseFunction operateOn(final CommercialBank bank) {
      double
         totalExistingLoanAssets = 0.;
      for(final Loan loan : bank.getAssetLoans())
         totalExistingLoanAssets += loan.getValue();
      final double
         loanAssetsIncrease =
            bank.getStrategy().getDesiredLoanInvestmentIn(market) - totalExistingLoanAssets;
      if(loanAssetsIncrease <= 1.)
         return null;                                                       // Negligible
      final UncollateralizedReturnMaximizerMRFBuilder builder =
         new UncollateralizedReturnMaximizerMRFBuilder();
      double
         cashWillingToSpendFromReserves =
            Math.min(bank.getCashReserveValue(), loanAssetsIncrease * .99);
      /*
       * Market Impact estimation
       */
      double
         marketImpactEstimate = 0.;
      for(final Object obj : Simulation.getRunningModel().getBanks()) {
         if(!(obj instanceof CommercialBank))
            continue;
         final CommercialBank other = (CommercialBank) obj;
         marketImpactEstimate +=
            other.getStrategy().getDesiredLoanInvestmentIn(market);
      }
      marketImpactEstimate = 1./Math.max(marketImpactEstimate * 10., 1.0);
      /*
       * Risk Premia
       */
      builder.addAsset("Low Risk Commercial Loan", 
         lowRiskCommercialLoanPremium, -marketImpactEstimate, 0.);
      builder.addAsset("Medium Risk Commercial Loan",
         mediumRiskCommercialLoanPremium, -marketImpactEstimate, 0.);
      builder.addAsset("High Risk Commercial Loan",
         highRiskCommercialLoanPremium, -marketImpactEstimate, 0.);
      builder.addAsset("Gilt",
         giltRiskPremium, -marketImpactEstimate, 0.);
      builder.addLiability("Bank Bond",
         bondRiskPremium, 0., 0.);
      builder.setParticipantCashToSpend(cashWillingToSpendFromReserves);
      builder.setParticipantEquity(loanAssetsIncrease);
      builder.setParticipantLeverage(1.0);
      return builder.build();
   }
   
   public double getLowRiskCommercialLoanPremium() {
      return lowRiskCommercialLoanPremium;
   }
   
   public double getMediumRiskCommercialLoanPremium() {
      return mediumRiskCommercialLoanPremium;
   }
   
   public double getHighRiskCommercialLoanPremium() {
      return highRiskCommercialLoanPremium;
   }
   
   public double getGiltRiskPremium() {
      return giltRiskPremium;
   }
   
   public double getBondRiskPremium() {
      return bondRiskPremium;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Commercial Bank BCL Market Response Function Factory, "
            + "low risk commercial loan premium: "
            + lowRiskCommercialLoanPremium
            + ", medium risk commercial loan premium: "
            + mediumRiskCommercialLoanPremium
            + ", high risk commercial loan premium: "
            + highRiskCommercialLoanPremium
            + ", gilt risk premium: " + giltRiskPremium
            + ", bond risk premium: " + bondRiskPremium + ".";
   }
}
