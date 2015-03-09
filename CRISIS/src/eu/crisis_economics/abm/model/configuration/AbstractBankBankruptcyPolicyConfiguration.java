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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.agent.ComputeRiskWeightedAssetsOperation;
import eu.crisis_economics.abm.bank.bankruptcy.BankBankruptcyPolicy;
import eu.crisis_economics.abm.bank.bankruptcy.ComputeCARBankruptcyResolutionAmountOperation;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

public abstract class AbstractBankBankruptcyPolicyConfiguration
   extends AbstractPrivateConfiguration
   implements BankBankruptcyPolicyConfiguration {
   
   private static final long serialVersionUID = -2035642523919634048L;
   
   public final double
      DEFAULT_BANK_BANKRUPTCY_RESOLUTION_CAR_TARGET = .08;
   
   @Layout(
      Title = "Asset Valuation and Equity Restoration",
      VerboseDescription =
         "This bankruptcy policy restores the bank's capital adequacy ratio to "
         + "level set by the 'Capital Adequacy Ratio Target' parameter below.  \nThe bank's "
         + "capital adequacy ratio is the bank's equity divided by the the total value of its "
         + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
         + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
         + "as the equity asset risk-weight if the asset is a firm or bank stock.\n"
         + "The risk weight parameters for each asset class in this algorithm follow the Basel "
         + "III guidelines by default, however they can also be adjusted below.",
      Order = 0
      )
   @Parameter(
      ID = "BANK_BANKRUPTCY_RESOLUTION_CAR_TARGET"
      )
   private double
      capitalAdequacyRatioTarget =
         DEFAULT_BANK_BANKRUPTCY_RESOLUTION_CAR_TARGET;
   
   @Layout(
      Order = 1,
      FieldName = "Cash Asset Risk Weight"
      )
   @Parameter(
      ID = "CASH_RISK_WEIGHT"
      )
   private double
      cashRiskWeight =
         ComputeRiskWeightedAssetsOperation.DEFAULT_CASH_RISK_WEIGHT;
   
   @Layout(
      Order = 2,
      FieldName = "Government Bond Asset Risk Weight"
      )
   @Parameter(
      ID = "GILT_RISK_WEIGHT"
      )
   private double
      giltRiskWeight =
         ComputeRiskWeightedAssetsOperation.DEFAULT_GILT_RISK_WEIGHT;
   
   @Layout(
      Order = 3,
      FieldName = "Mortgage Asset Risk Weight"
      )
   @Parameter(
      ID = "MORTGAGE_RISK_WEIGHT"
      )
   private double
      mortgageRiskWeight =
         ComputeRiskWeightedAssetsOperation.DEFAULT_MORTGAGE_RISK_WEIGHT;
   
   @Layout(
      Order = 4,
      FieldName = "Interbank Loan Asset Risk Weight"
      )
   @Parameter(
      ID = "INTERBANK_LOAN_RISK_WEIGHT"
      )
   private double
      interbankLoanRiskWeight =
         ComputeRiskWeightedAssetsOperation.DEFAULT_INTERBANK_LOAN_RISK_WEIGHT;
   
   @Layout(
      Order = 5,
      FieldName = "Repo Loan Asset Risk Weight"
      )
   @Parameter(
      ID = "REPO_LOAN_RISK_WEIGHT"
      )
   private double
      repoLoanRiskWeight =
         ComputeRiskWeightedAssetsOperation.DEFAULT_REPO_LOAN_RISK_WEIGHT;
   
   @Layout(
      Order = 6,
      FieldName = "Commercial Loan Asset Risk Weight"
      )
   @Parameter(
      ID = "COMMERCIAL_LOAN_RISK_WEIGHT"
      )
   private double
      commercialLoanRiskWeight =
         ComputeRiskWeightedAssetsOperation.DEFAULT_COMMERCIAL_LOAN_RISK_WEIGHT;
   
   @Layout(
      Order = 7,
      FieldName = "Equity Asset Risk Weight"
      )
   @Parameter(
      ID = "EQUITY_EXPOSURE_RISK_WEIGHT"
      )
   private double
      equityExposureRiskWeight =
         ComputeRiskWeightedAssetsOperation.DEFAULT_EQUITY_EXPOSURE_RISK_WEIGHT;
   
   @Layout(
      Order = 8,
      FieldName = "Other Assets Risk Weight"
      )
   @Parameter(
      ID = "DEFAULT_ASSET_RISK_WEIGHT"
      )
   private double
      defaultRiskWeight =
         ComputeRiskWeightedAssetsOperation.DEFAULT_OTHER_ASSETS_RISK_WEIGHT;
   
   public final double getCapitalAdequacyRatioTarget() {
      return capitalAdequacyRatioTarget;
   }
   
   public final void setCapitalAdequacyRatioTarget(
      final double carTarget) {
      this.capitalAdequacyRatioTarget = carTarget;
   }
   
   public final String desCapitalAdequacyRatioTarget() {
      return "The bankruptcy resolution mechanism restores the bankrupt bank's capital ratio to the"
            + " target specified by the 'Capital Adequacy Ratio Target' parameter.  This must be a "
            + "non-negative number.  The bank's "
            + "capital adequacy ratio is the bank's equity divided by the the total value of its "
            + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n";
   }
   
   public final double getCashRiskWeight() {
      return cashRiskWeight;
   }
   
   public final void setCashRiskWeight(
      double cashRiskWeight) {
      this.cashRiskWeight = cashRiskWeight;
   }
   
   public final String desCashRiskWeight() {
      return "The risk-weight applied to the Cash asset class when determining a bank's capital "
            + "adequacy ratio.  This can be adjusted here and must be a non-negative number.  For "
            + "Cash assets, Basel III recommends "
            + "a risk-weight of zero, reflecting the property that Cash is the most liquid "
            + "and least risky asset that banks can hold (inside a closed economy).\n\nThe bank's "
            + "capital adequacy ratio is the bank's equity divided by the the total value of its "
            + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n\n"
            + "The risk weight parameters for each asset class in this algorithm follow the Basel "
            + "III guidelines by default.";
   }
   
   public final double getGiltRiskWeight() {
      return giltRiskWeight;
   }
   
   public final void setGiltRiskWeight(
      double giltRiskWeight) {
      this.giltRiskWeight = giltRiskWeight;
   }

   public final String desGiltRiskWeight() {
      return "The risk-weight applied to the Government Bond asset class when determining a bank's "
            + "capital adequacy ratio.  This can be adjusted here and must be a non-negative number"
            + ".  For Government Bonds, Basel III "
            + "recommends a risk-weight of zero, reflecting the property that the Government Bond "
            + "class is the most liquid "
            + "and least risky asset that banks can hold (inside a closed economy).\n\nThe bank's "
            + "capital adequacy ratio is the bank's equity divided by the the total value of its "
            + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n\n"
            + "The risk weight parameters for each asset class in this algorithm follow the Basel "
            + "III guidelines by default.";
   }
   
   public final double getMortgageRiskWeight() {
      return mortgageRiskWeight;
   }
   
   public final void setMortgageRiskWeight(
      double mortgageRiskWeight) {
      this.mortgageRiskWeight = mortgageRiskWeight;
   }

   public final String desMortgageRiskWeight() {
      return "The risk-weight applied to the Mortgage Loan asset class when determining a bank's "
            + "capital adequacy ratio.  This can be adjusted here and must be a non-negative number"
            + ".  For Mortgage loan assets, Basel III "
            + "recommends a risk-weight of 50%, reflecting the risk that the Mortgage may be in "
            + "arrears, default or that any subsequent reposession of the property collateralizing "
            + "the asset may not realize the full value of the loan.\n\nThe bank's "
            + "capital adequacy ratio is the bank's equity divided by the the total value of its "
            + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n\n"
            + "The risk weight parameters for each asset class in this algorithm follow the Basel "
            + "III guidelines by default.";
   }

   public final double getInterbankLoanRiskWeight() {
      return interbankLoanRiskWeight;
   }
   
   public final void setInterbankLoanRiskWeight(
      double interbankLoanRiskWeight) {
      this.interbankLoanRiskWeight = interbankLoanRiskWeight;
   }
   
   public final String desInterbankLoanRiskWeight() {
      return "The risk-weight applied to the Interbank Loan asset class when determining a bank's "
            + "capital adequacy ratio.  This can be adjusted here and must be a non-negative number"
            + ".  For Interbank loan assets, Basel III "
            + "recommends a risk-weight of 20%, reflecting the risk of loss on the loan.\n\nThe "
            + "bank's capital adequacy ratio is the bank's equity divided by the the total value of"
            + " its risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n\n"
            + "The risk weight parameters for each asset class in this algorithm follow the Basel "
            + "III guidelines by default.";
   }
   
   public final double getRepoLoanRiskWeight() {
      return repoLoanRiskWeight;
   }
   
   public final void setRepoLoanRiskWeight(
      double repoLoanRiskWeight) {
      this.repoLoanRiskWeight = repoLoanRiskWeight;
   }
   
   public final String desRepoLoanRiskWeight() {
      return "The risk-weight applied to the Repurchase Agreement (Repo) Loan asset class when "
            + "determining a bank's capital adequacy ratio.  "
            + "This can be adjusted here and must be a non-negative number.  "
            + "For Repo loan assets, Basel III recommends a risk-weight of 20%, reflecting the "
            + "risk of loss on the (collateralized) loan class.\n\nThe bank's"
            + " capital adequacy ratio is the bank's equity divided by the the total value of its "
            + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n\n"
            + "The risk weight parameters for each asset class in this algorithm follow the Basel "
            + "III guidelines by default.";
   }

   public final double getCommercialLoanRiskWeight() {
      return commercialLoanRiskWeight;
   }
   
   public final void setCommercialLoanRiskWeight(
      double commercialLoanRiskWeight) {
      this.commercialLoanRiskWeight = commercialLoanRiskWeight;
   }
   
   public final String desCommercialLoanRiskWeight() {
      return "The risk-weight applied to the Commercial Loan asset class when "
            + "determining a bank's capital adequacy ratio.  "
            + "This can be adjusted here and must be a non-negative number.  "
            + "For Commercial loan assets, Basel III recommends a risk-weight of 100%, reflecting "
            + "the risk of loss on the (uncollateralized) loan class.\n\nThe bank's"
            + " capital adequacy ratio is the bank's equity divided by the the total value of its "
            + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n\n"
            + "The risk weight parameters for each asset class in this algorithm follow the Basel "
            + "III guidelines by default.";
   }
   
   public final double getEquityExposureRiskWeight() {
      return equityExposureRiskWeight;
   }
   
   public final void setEquityExposureRiskWeight(
      double equityExposureRiskWeight) {
      this.equityExposureRiskWeight = equityExposureRiskWeight;
   }
   
   public final String desEquityExposureRiskWeight() {
      return "The risk-weight applied to the Equity asset class when "
            + "determining a bank's capital adequacy ratio.  "
            + "This can be adjusted here and must be a non-negative number.  "
            + "For publicly traded Equity assets such as firm and bank stocks, Basel III recommends"
            + " a risk-weight of 300%, reflecting the risk of loss on the Equity class of investment"
            + ".  Equity is subordinate to the debt of a corporation, in that debtholders will be "
            + "repaid before the equity holders in the event of bankruptcy.  The equity class is "
            + "also generally subject to more market risk, in that the value of "
            + "equity assets is more volatile than debt.\n\nThe bank's"
            + " capital adequacy ratio is the bank's equity divided by the the total value of its "
            + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n\n"
            + "The risk weight parameters for each asset class in this algorithm follow the Basel "
            + "III guidelines by default.";
   }
   
   public final double getDefaultRiskWeight() {
      return defaultRiskWeight;
   }
   
   public final void setDefaultRiskWeight(
      double defaultRiskWeight) {
      this.defaultRiskWeight = defaultRiskWeight;
   }
   
   public final String desDefaultRiskWeight() {
      return "The risk-weight applied to assets not falling into the other classes above, when "
            + "determining a bank's capital adequacy ratio.  "
            + "This can be adjusted here and must be a non-negative number.  "
            + "For Commercial loan assets, Basel III recommends a risk-weight of 100%, reflecting "
            + "the risk of loss on the unspecified asset.\n\nThe bank's"
            + " capital adequacy ratio is the bank's equity divided by the the total value of its "
            + "risk-weighted assets, where a risk-weighted asset is the value of the asset "
            + "multiplied by the risk-weight corresponding to the asset class it belongs to, such "
            + "as the equity asset risk-weight if the asset is a firm or bank stock.\n\n"
            + "The risk weight parameters for each asset class in this algorithm follow the Basel "
            + "III guidelines by default.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(
         capitalAdequacyRatioTarget >= 0.,
         toParameterValidityErrMsg("Capital Adequacy Ratio target is negative.")
         );
      Preconditions.checkArgument(
         cashRiskWeight >= 0.,
         toParameterValidityErrMsg("Cash Risk Weight is negative.")
         );
      Preconditions.checkArgument(
         giltRiskWeight >= 0.,
         toParameterValidityErrMsg("Gilt Risk Weight is negative.")
         );
      Preconditions.checkArgument(
         mortgageRiskWeight >= 0.,
         toParameterValidityErrMsg("Mortgage Risk Weight is negative.")
         );
      Preconditions.checkArgument(
         interbankLoanRiskWeight >= 0.,
         toParameterValidityErrMsg("Interbank Loan Risk Weight is negative.")
         );
      Preconditions.checkArgument(
         repoLoanRiskWeight >= 0.,
         toParameterValidityErrMsg("Repo Loan Risk Weight is negative.")
         );
      Preconditions.checkArgument(
         commercialLoanRiskWeight >= 0.,
         toParameterValidityErrMsg("Commercial Loan Risk Weight is negative.")
         );
      Preconditions.checkArgument(
         equityExposureRiskWeight >= 0.,
         toParameterValidityErrMsg("Equity Exposure Risk Weight is negative.")
         );
      Preconditions.checkArgument(
         defaultRiskWeight >= 0.,
         toParameterValidityErrMsg("Default (other asset type) Risk Weight is negative.")
         );
   }
   
   @Override
   protected void addBindings() {
      install(
         InjectionFactoryUtils.asPrimitiveParameterModule(
            "BANK_BANKRUPTCY_RESOLUTION_CAR_TARGET",  capitalAdequacyRatioTarget,
            "CASH_RISK_WEIGHT",                       cashRiskWeight,
            "GILT_RISK_WEIGHT",                       giltRiskWeight,
            "MORTGAGE_RISK_WEIGHT",                   mortgageRiskWeight,
            "INTERBANK_LOAN_RISK_WEIGHT",             interbankLoanRiskWeight,
            "REPO_LOAN_RISK_WEIGHT",                  repoLoanRiskWeight,
            "COMMERCIAL_LOAN_RISK_WEIGHT",            commercialLoanRiskWeight,
            "EQUITY_EXPOSURE_RISK_WEIGHT",            equityExposureRiskWeight,
            "DEFAULT_ASSET_RISK_WEIGHT",              defaultRiskWeight
            ));
      bind(ComputeRiskWeightedAssetsOperation.class);
      bind(ComputeCARBankruptcyResolutionAmountOperation.class);
      bind(new TypeLiteral<AgentOperation<Double>>(){})
         .annotatedWith(Names.named("BANK_BANKRUPTCY_RESOLUTION_RWA_SUM_MEASUREMENT"))
         .to(ComputeRiskWeightedAssetsOperation.class);
      bind(BankBankruptcyPolicy.class)
         .annotatedWith(Names.named("BANK_BANKRUPTCY_RESOLUTION_POLICY"))
         .toProvider(BankBankruptcyPolicyProvider.class);
      expose(BankBankruptcyPolicy.class)
         .annotatedWith(Names.named("BANK_BANKRUPTCY_RESOLUTION_POLICY"));
   }
}
