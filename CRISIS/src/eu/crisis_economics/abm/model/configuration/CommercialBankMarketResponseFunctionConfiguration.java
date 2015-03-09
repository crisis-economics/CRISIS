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

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingSimpleCommercialLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingGiltsBondsAndCommercialLoansMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.BCLCommercialLoanSupplierMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.CompositeMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ForwardingClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.LoanSupplierMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.TargetValueStockMarketResponseFunctionFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.MarketResponseFactoryBuilder.BCLResponseFunctionFactoryBuilder;
import eu.crisis_economics.abm.simulation.injection.factories.MarketResponseFactoryBuilder.LoanSupplierMarketResponseFunctionFactoryBuilder;
import eu.crisis_economics.abm.simulation.injection.factories.MarketResponseFactoryBuilder.TargetValueStockMarketResponseFunctionFactoryBuilder;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   Description =
     "This component specifies parameters in the clearing market from the "
   + "perspective of commercial banks."
   + "\n\nThe banks post a credit supply function S(r) that specifies how much loan cash "
   + "the bank is willing to provide as a function of the interest rate r. The credit "
   + "supply function S(r) increases monotonically with r and reaches a maximum value at "
   + "some r = r_max (because the bank has a maximum portfolio size for commercial loans)."
   + " S(0) = 0 since no bank will lend money (and so take on a liability) for free. S(r) "
   + "is modelled as a polynomial function \nS(r) = (r / r_max)^exponent \nwhere exponent is the "
   + "'credit supply exponent' parameter and r_max is the 'Maximum Rate of Return' "
   + "parameter, both of which can be adjusted below."
   + "\n\nThe 'risk adjustment factor' parameters specify the adjustments applied to the rates of "
   + "return of debt asset classes by the banks in order to take account of the perveived "
   + "credit risk for each asset class.  The risk adjustment parameters work by dividing "
   + "market-determined rates of return to obtain the (credit) risk-adjusted expected "
   + "rates of return:\nExpected_Rate_of_Return = Market-Determined_Rate_of_Return / "
   + "Risk_Adjustment_Factor.  "
   + "\nFor example, if the 'high risk adjustment factor' is 1.14 and if the "
   + "market determined rate of return for such a high risk asset was 1.14, the (credit "
   + "risk-adjusted) expected rate of return on the high risk asset would be 1.  The higher the "
   + "credit risk, the larger the 'risk adjustment factor' parameter.  If government bonds"
   + " are perceived to bear no credit risk, the 'Government Bond Risk Adjustment Factor' "
   + "should be 1.",
   DisplayName = "Loan, Bond, Gilt & Stock Bidding"
   )
public final class CommercialBankMarketResponseFunctionConfiguration
   extends AbstractPrivateConfiguration
   implements ClearingMarketParticipationConfiguration {
   
   private static final long serialVersionUID = 6648020730826463906L;
   
   @Layout(
      Order = 0,
      FieldName = "Credit Supply Exponent"
      )
   @Parameter(
      ID = "CREDIT_SUPPLY_FUNCTION_EXPONENT"
      )
   private double
      creditSupplyFunctionExponent =
         LoanSupplierMarketResponseFunctionFactory.DEFAULT_LOAN_SUPPLY_EXPONENT;
   @Layout(
      Order = 1,
      FieldName = "Maximum Rate of Return"
      )
   @Parameter(
      ID = "CREDIT_SUPPLY_FUNCTION_MAXIMUM_INTEREST_RATE"
      )
   private double
      creditSupplyFunctionMaximumInterestRate =
         LoanSupplierMarketResponseFunctionFactory.DEFAULT_MAXIMUM_INTEREST_RATE;
   @Layout(
      Order = 2,
      FieldName = "Commercial Loan Low Risk Adjustment Factor"
      )
   @Parameter(
      ID = "LOW_RISK_COMMERCIAL_LOAN_PREMIUM"
      )
   private double
      lowRiskCommercialLoanPremium =
         BCLCommercialLoanSupplierMarketResponseFunctionFactory
            .DEFAULT_LOW_RISK_COMMERCIAL_LOAN_PREMIUM;
   @Layout(
      Order = 3,
      FieldName = "Commercial Loan Medium Risk Adjustment Factor"
      )
   @Parameter(
      ID = "MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM"
      )
   private double
      mediumRiskCommercialLoanPremium =
         BCLCommercialLoanSupplierMarketResponseFunctionFactory
            .DEFAULT_MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM;
   @Layout(
      Order = 4,
      FieldName = "Commercial Loan High Risk Adjustment Factor"
      )
   @Parameter(
      ID = "HIGH_RISK_COMMERCIAL_LOAN_PREMIUM"
      )
   private double
      highRiskCommercialLoanPremium = 
         BCLCommercialLoanSupplierMarketResponseFunctionFactory
            .DEFAULT_HIGH_RISK_COMMERCIAL_LOAN_PREMIUM;
   @Layout(
      Order = 5,
      FieldName = "Government Bond Risk Adjustment Factor"
      )
   @Parameter(
      ID = "GILT_RISK_PREMIUM"
      )
   private double
      giltRiskPremium =
         BCLCommercialLoanSupplierMarketResponseFunctionFactory
            .DEFAULT_GILT_RISK_PREMIUM;
   @Layout(
      Order = 6,
      FieldName = "Bank Bond Risk Adjustment Factor"
      )
   @Parameter(
      ID = "BOND_RISK_PREMIUM"
      )
   private double
      bondRiskPremium = 
         BCLCommercialLoanSupplierMarketResponseFunctionFactory
            .DEFAULT_BOND_RISK_PREMIUM;
   
   private static final class CommercialBankMarketParticipationComponent
      implements ClearingMarketParticipantFactory {
      
      private TargetValueStockMarketResponseFunctionFactoryBuilder
         stockMarketResponseBuilder;
      
      private CompositeMarketResponseFunctionFactory
         responses;
      
      private ClearingHouse
         clearingHouse;
      
      @Inject
      private CommercialBankMarketParticipationComponent(
      @Named("DO_ENABLE_COMMERCIAL_BANK_STOCK_MARKET_PARTICIPATION")
         final boolean doEnableStockMarketParticipation,
         final LoanSupplierMarketResponseFunctionFactoryBuilder
            vanillaLoanMarketResponseBuilder,
         final BCLResponseFunctionFactoryBuilder
            BCLMarketResponseBuilder,
         final TargetValueStockMarketResponseFunctionFactoryBuilder
            stockMarketResponseBuilder,
         final ClearingHouse clearingHouse
         ) {
         this.responses = new CompositeMarketResponseFunctionFactory();
         this.stockMarketResponseBuilder = stockMarketResponseBuilder;
         this.clearingHouse = clearingHouse;
         
         for(final ClearingSimpleCommercialLoanMarket market :
             clearingHouse.getMarketsOfType(ClearingSimpleCommercialLoanMarket.class)) {
            responses.put(
               market.getMarketName(),
               vanillaLoanMarketResponseBuilder.create(market)
               );
         }
         for(final ClearingGiltsBondsAndCommercialLoansMarket market :
             clearingHouse.getMarketsOfType(ClearingGiltsBondsAndCommercialLoansMarket.class)) {
            responses.put(
               market.getMarketName(),
               BCLMarketResponseBuilder.create(market)
               );
         }
         
         if(doEnableStockMarketParticipation)
            Simulation.once(this, "setupStockMarketResponses", NamedEventOrderings.BEFORE_ALL);
      }
      
      @SuppressWarnings("unused")   // Scheduled
      private void setupStockMarketResponses() {
         List<ClearingStockMarket>
            markets = clearingHouse.getMarketsOfType(ClearingStockMarket.class);
         for(final ClearingStockMarket market : markets) {
            responses.put(market.getMarketName(), stockMarketResponseBuilder.create(market));
         }
      }
      
      @Override
      public ClearingMarketParticipant create(Agent parent) {
         return new ForwardingClearingMarketParticipant(parent, responses);
      }
   }
   
   public double getCreditSupplyFunctionExponent() {
      return creditSupplyFunctionExponent;
   }
   
   /**
     * Set the credit supply function polynomial exponent for {@link Agent}{@code s}
     * configured to respond to clearing markets with this component. The argument
     * should be non-negative.
     */
   public void setCreditSupplyFunctionExponent(
      final double creditSupplyFunctionExponent) {
      this.creditSupplyFunctionExponent = creditSupplyFunctionExponent;
   }
   
   public String desCreditSupplyFunctionExponent() {
      return "This is a non-negative parameter that appears in the equation below."
            + "\nThe banks post a credit supply function S(r) that specifies how much loan cash "
            + "the bank is willing to provide as a function of the interest rate r. The credit "
            + "supply function S(r) increases monotonically with r and reaches a maximum value at "
            + "some r = r_max (because the bank has a maximum portfolio size for commercial loans)."
            + " S(0) = 0 since no bank will lend money (and so take on a liability) for free. S(r) "
            + "is modelled as a polynomial function \nS(r) = (r / r_max)^exponent \nwhere exponent "
            + "is the "
            + "'credit supply exponent' parameter and r_max is the 'Maximum Rate of Return' "
            + "parameter, both of which can be adjusted below.";
  }

   
   public double getCreditSupplyFunctionMaximumInterestRate() {
      return creditSupplyFunctionMaximumInterestRate;
   }
   
   /**
     * Set the maximum interest rate for {@link Agent}{@code s} configured to respond
     * to clearing loan markets with this component. The argument should be non-negative.
     */
   public void setCreditSupplyFunctionMaximumInterestRate(
      final double creditSupplyFunctionMaximumInterestRate) {
      this.creditSupplyFunctionMaximumInterestRate = creditSupplyFunctionMaximumInterestRate;
   }
   
   public String desCreditSupplyFunctionMaximumInterestRate() {
      return "This is a non-negative parameter that appears in the equation below."
            + "\nThe banks post a credit supply function S(r) that specifies how much loan cash "
            + "the bank is willing to provide as a function of the interest rate r. The credit "
            + "supply function S(r) increases monotonically with r and reaches a maximum value at "
            + "some r = r_max (because the bank has a maximum portfolio size for commercial loans)."
            + " S(0) = 0 since no bank will lend money (and so take on a liability) for free. S(r) "
            + "is modelled as a polynomial function \nS(r) = (r / r_max)^exponent \nwhere exponent "
            + "is the "
            + "'credit supply exponent' parameter and r_max is the 'Maximum Rate of Return' "
            + "parameter.";
   }
   
   public double getLowRiskCommercialLoanPremium() {
      return lowRiskCommercialLoanPremium;
   }
   
   public void setLowRiskCommercialLoanPremium(
      double lowRiskCommercialLoanPremium) {
      this.lowRiskCommercialLoanPremium = lowRiskCommercialLoanPremium;
   }
   
   public String desLowRiskCommercialLoanPremium() {
      return "This parameter is used to adjust the market determined rate of return to "
            + "obtain the expected rate of return by taking into account the credit risk of the "
            + "debt:\nExpected_Rate_of_Return = Market-Determined_Rate_of_Return / "
            + "Risk_Adjustment_Factor.  "
            + "\nFor example, if the 'low risk adjustment factor' is 1.10, if the "
            + "market determined rate of return for such a low risk asset was 1.10, the (credit) "
            + "risk-adjusted rate of return on the low risk asset would be 1.  The higher the "
            + "credit risk, the larger the 'risk adjustment factor' parameter, and the value of the"
            + " parameter must be greater than, or equal to 1.";
   }
   
   public double getMediumRiskCommercialLoanPremium() {
      return mediumRiskCommercialLoanPremium;
   }
   
   public void setMediumRiskCommercialLoanPremium(
      double mediumRiskCommercialLoanPremium) {
      this.mediumRiskCommercialLoanPremium = mediumRiskCommercialLoanPremium;
   }
   
   public String desMediumRiskCommercialLoanPremium() {
      return "This parameter is used to adjust the market determined rate of return to "
            + "obtain the expected rate of return by taking into account the credit risk of the "
            + "debt:\nExpected_Rate_of_Return = Market-Determined_Rate_of_Return / "
            + "Risk_Adjustment_Factor.  "
            + "\nFor example, if the 'medium risk adjustment factor' is 1.12, if the "
            + "market determined rate of return for such a medium risk asset was 1.12, the (credit)"
            + " risk-adjusted rate of return on the medium risk asset would be 1.  The higher the "
            + "credit risk, the larger the 'risk adjustment factor' parameter, so the value of this"
            + " the medium risk parameter should be greater than the value of the low risk "
            + "parameter.  All adjustment factors should be greater than, or equal to 1.";
   }

   
   public double getHighRiskCommercialLoanPremium() {
      return highRiskCommercialLoanPremium;
   }
   
   public void setHighRiskCommercialLoanPremium(
      double highRiskCommercialLoanPremium) {
      this.highRiskCommercialLoanPremium = highRiskCommercialLoanPremium;
   }

   public String desHighRiskCommercialLoanPremium() {
      return "This parameter is used to adjust the market determined rate of return to "
            + "obtain the expected rate of return by taking into account the credit risk of the "
            + "debt:\nExpected_Rate_of_Return = Market-Determined_Rate_of_Return / "
            + "Risk_Adjustment_Factor.  "
            + "\nFor example, if the 'high risk adjustment factor' is 1.14, if the "
            + "market determined rate of return for such a high risk asset was 1.14, the (credit)"
            + " risk-adjusted rate of return on the high risk asset would be 1.  The higher the "
            + "credit risk, the larger the 'risk adjustment factor' parameter, so the value of this"
            + " the high risk parameter should be greater than the value of the medium risk "
            + "parameter.  All adjustment factors should be greater than, or equal to 1.";
   }

   
   public double getGiltRiskPremium() {
      return giltRiskPremium;
   }
   
   public void setGiltRiskPremium(
      double giltRiskPremium) {
      this.giltRiskPremium = giltRiskPremium;
   }
   
   public String desGiltRiskPremium() {
      return "This parameter is used to adjust the market determined rate of return to "
            + "obtain the expected rate of return by taking into account the credit risk of the "
            + "debt:\nExpected_Rate_of_Return = Market-Determined_Rate_of_Return / "
            + "Risk_Adjustment_Factor.  "
            + "\nFor example, if the 'Government Bond risk adjustment factor' is 1.05, if the "
            + "market determined rate of return for the bond is 1.05, the (credit)"
            + " risk-adjusted rate of return on the bond asset would be 1.  The higher the "
            + "credit risk, the larger the 'risk adjustment factor' parameter, and the value of the"
            + " parameter must be greater than, or equal to 1.  If the government bonds are "
            + "considered to bear no credit risk, the risk adjustment factor should be set to 1.0.";
   }
   
   public double getBondRiskPremium() {
      return bondRiskPremium;
   }
   
   public void setBondRiskPremium(
      double bondRiskPremium) {
      this.bondRiskPremium = bondRiskPremium;
   }

   public String desBondRiskPremium() {
      return "This parameter is used to adjust the market determined rate of return to "
            + "obtain the expected rate of return by taking into account the credit risk of the "
            + "debt:\nExpected_Rate_of_Return = Market-Determined_Rate_of_Return / "
            + "Risk_Adjustment_Factor.  "
            + "\nFor example, if the 'Bank Bond risk adjustment factor' is 1.10, if the "
            + "market determined rate of return for the bond is 1.10, the (credit)"
            + " risk-adjusted rate of return on the bond asset would be 1.  The higher the "
            + "credit risk, the larger the 'risk adjustment factor' parameter, and the value of the"
            + " parameter must be greater than, or equal to 1.  If the bank bonds are "
            + "considered to bear more credit risk than government bonds, the bank bond risk "
            + "adjustment factor should be set to a level higher than the government bond risk "
            + "risk adjustment factor.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         creditSupplyFunctionExponent >= 0.,
         toParameterValidityErrMsg("Credit Supply Function Exponent is negative."));
      Preconditions.checkArgument(
         creditSupplyFunctionMaximumInterestRate >= 0.,
         toParameterValidityErrMsg("Credit Supply Function Maximum Interest Rate is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "CREDIT_SUPPLY_FUNCTION_EXPONENT",              creditSupplyFunctionExponent,
         "CREDIT_SUPPLY_FUNCTION_MAXIMUM_INTEREST_RATE", creditSupplyFunctionMaximumInterestRate,
         "LOW_RISK_COMMERCIAL_LOAN_PREMIUM",             lowRiskCommercialLoanPremium,
         "MEDIUM_RISK_COMMERCIAL_LOAN_PREMIUM",          mediumRiskCommercialLoanPremium,
         "HIGH_RISK_COMMERCIAL_LOAN_PREMIUM",            highRiskCommercialLoanPremium,
         "GILT_RISK_PREMIUM",                            giltRiskPremium,
         "BOND_RISK_PREMIUM",                            bondRiskPremium
         ));
      install(new FactoryModuleBuilder()
         .implement(
            new TypeLiteral<AgentOperation<MarketResponseFunction>>(){},
            BCLCommercialLoanSupplierMarketResponseFunctionFactory.class
            )
         .build(BCLResponseFunctionFactoryBuilder.class));
      install(new FactoryModuleBuilder()
         .implement(
            new TypeLiteral<AgentOperation<MarketResponseFunction>>(){},
            LoanSupplierMarketResponseFunctionFactory.class
            )
         .build(LoanSupplierMarketResponseFunctionFactoryBuilder.class));
      install(new FactoryModuleBuilder()
         .implement(
            new TypeLiteral<AgentOperation<MarketResponseFunction>>(){},
            TargetValueStockMarketResponseFunctionFactory.class
            )
         .build(TargetValueStockMarketResponseFunctionFactoryBuilder.class));
      bind(ClearingMarketParticipantFactory.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(CommercialBankMarketParticipationComponent.class);
      expose(ClearingMarketParticipantFactory.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}