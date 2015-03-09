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
package eu.crisis_economics.abm.agent;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.FixedValueContract;
import eu.crisis_economics.abm.contracts.loans.InterbankLoan;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.RepoLoan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.household.Household;

/**
  * Returns risk-weighted assets (RWA). By default, risk weights for this
  * algorithm follow Basel III guidelines.
  */
public final class ComputeRiskWeightedAssetsOperation
   extends SimpleAbstactAgentOperation<Double> {
   
   /**
     * By default:<br>
     * if an asset is cash or government bond, risk weight is 0<br>
     * if an asset is mortgage loan, risk weight is 0.5<br>
     * if an asset is interbank loan or repo loan, risk weight is 0.2<br>
     * if an asset is commercial loan or other type of loan, risk weight is 1.0<br>
     * if an asset is equity exposure (all our stocks are publicly traded), risk weight is 3.0<br>
     * if any other type of asset, the risk weight is 1.0.
     */
   public final static double
      DEFAULT_CASH_RISK_WEIGHT = 0.,
      DEFAULT_GILT_RISK_WEIGHT = 0.,
      DEFAULT_MORTGAGE_RISK_WEIGHT = .5,
      DEFAULT_INTERBANK_LOAN_RISK_WEIGHT = .2,
      DEFAULT_REPO_LOAN_RISK_WEIGHT = .2,
      DEFAULT_COMMERCIAL_LOAN_RISK_WEIGHT = 1.,
      DEFAULT_EQUITY_EXPOSURE_RISK_WEIGHT = 3.,
      DEFAULT_OTHER_ASSETS_RISK_WEIGHT = 1.;
   
   private double
      cashRiskWeight = DEFAULT_CASH_RISK_WEIGHT,
      giltRiskWeight = DEFAULT_GILT_RISK_WEIGHT,
      mortgageRiskWeight = DEFAULT_MORTGAGE_RISK_WEIGHT,
      interbankLoanRiskWeight = DEFAULT_INTERBANK_LOAN_RISK_WEIGHT,
      repoLoanRiskWeight = DEFAULT_REPO_LOAN_RISK_WEIGHT,
      commercialLoanRiskWeight = DEFAULT_COMMERCIAL_LOAN_RISK_WEIGHT,
      equityExposureRiskWeight = DEFAULT_EQUITY_EXPOSURE_RISK_WEIGHT,
      otherAssetsRiskWeight = DEFAULT_OTHER_ASSETS_RISK_WEIGHT;
   
   /**
     * Create a {@link ComputeRiskWeightedAssetsOperation} object with 
     * custom parameters.<br><br>
     * Although this algorithm will operate (in an undefined manner) with negative
     * arguments, each argument to this constructor should be non-negative.
     */
   @Inject
   public ComputeRiskWeightedAssetsOperation(   // Immutable
   @Named("CASH_RISK_WEIGHT")
      final double cashRiskWeight,
   @Named("GILT_RISK_WEIGHT")
      final double giltRiskWeight,
   @Named("MORTGAGE_RISK_WEIGHT")
      final double mortgageRiskWeight,
   @Named("INTERBANK_LOAN_RISK_WEIGHT")
      final double interbankLoanRiskWeight,
   @Named("REPO_LOAN_RISK_WEIGHT")
      final double repoLoanRiskWeight,
   @Named("COMMERCIAL_LOAN_RISK_WEIGHT")
      final double commercialLoanRiskWeight,
   @Named("EQUITY_EXPOSURE_RISK_WEIGHT")
      final double equityExposureRiskWeight,
   @Named("DEFAULT_ASSET_RISK_WEIGHT")
      final double otherAssetsRiskWeight
      ) {
      this.cashRiskWeight = cashRiskWeight;
      this.giltRiskWeight = giltRiskWeight;
      this.mortgageRiskWeight = mortgageRiskWeight;
      this.interbankLoanRiskWeight = interbankLoanRiskWeight;
      this.repoLoanRiskWeight = repoLoanRiskWeight;
      this.commercialLoanRiskWeight = commercialLoanRiskWeight;
      this.equityExposureRiskWeight = equityExposureRiskWeight;
      this.otherAssetsRiskWeight = otherAssetsRiskWeight;
   }
   
   /**
     * Create a {@link ComputeRiskWeightedAssetsOperation} object with 
     * default parameters.
     */
   public ComputeRiskWeightedAssetsOperation() {
      this(
         DEFAULT_CASH_RISK_WEIGHT,
         DEFAULT_GILT_RISK_WEIGHT,
         DEFAULT_MORTGAGE_RISK_WEIGHT,
         DEFAULT_INTERBANK_LOAN_RISK_WEIGHT,
         DEFAULT_REPO_LOAN_RISK_WEIGHT,
         DEFAULT_COMMERCIAL_LOAN_RISK_WEIGHT,
         DEFAULT_EQUITY_EXPOSURE_RISK_WEIGHT,
         DEFAULT_OTHER_ASSETS_RISK_WEIGHT
         );
   }
   
   @Override
   public Double operateOn(final Agent agent) {
      double
         riskWeight = 0.,
         riskWeightedAssets = 0.;
      
      for(final Contract contract : agent.getAssets()){
         if(contract instanceof FixedValueContract) 
            riskWeight = cashRiskWeight;              // cash
         else if (contract instanceof Loan) {
            if (((Loan) contract).getBorrower() instanceof Household)          // TODO: type
               riskWeight = mortgageRiskWeight;       // residential mortgage
            else if (((Loan) contract).getBorrower() instanceof Firm) 
               riskWeight = commercialLoanRiskWeight; // commercial loan (corporate exposure)
            else if (((Loan) contract).getBorrower() instanceof Government)
               riskWeight = giltRiskWeight;           // government bonds
            else if (contract instanceof RepoLoan)
               riskWeight = repoLoanRiskWeight;       // repo loan 
            else if (contract instanceof InterbankLoan)
               riskWeight = interbankLoanRiskWeight;  // interbank loan
            else
               riskWeight = otherAssetsRiskWeight;        // default option for other types of loans
         } 
         else if (contract instanceof StockAccount)
            riskWeight = equityExposureRiskWeight;    // publicly-traded equity exposure 
         else
            riskWeight = otherAssetsRiskWeight;           // default option: other types of assets
         
         riskWeightedAssets += contract.getValue() * riskWeight;
      }
      return riskWeightedAssets;
   }
   
   public double getCashRiskWeight() {
      return cashRiskWeight;
   }
   
   public double getGiltRiskWeight() {
      return giltRiskWeight;
   }
   
   public double getMortgageRiskWeight() {
      return mortgageRiskWeight;
   }
   
   public double getInterbankLoanRiskWeight() {
      return interbankLoanRiskWeight;
   }
   
   public double getRepoLoanRiskWeight() {
      return repoLoanRiskWeight;
   }
   
   public double getCommercialLoanRiskWeight() {
      return commercialLoanRiskWeight;
   }
   
   public double getEquityExposureRiskWeight() {
      return equityExposureRiskWeight;
   }
   
   public double getDefaultRiskWeight() {
      return otherAssetsRiskWeight;
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Compute Risk Weighted Assets Algorithm, cash risk weight: "
            + cashRiskWeight + ", gilt risk weight: " + giltRiskWeight
            + ", mortgage risk weight: " + mortgageRiskWeight
            + ", interbank loan risk weight: " + interbankLoanRiskWeight
            + ", repo loan risk weight: " + repoLoanRiskWeight
            + ", commercial loan risk weight: " + commercialLoanRiskWeight
            + ", equity exposure risk weight: " + equityExposureRiskWeight
            + ", default (other) risk weight: " + otherAssetsRiskWeight + ".";
   }
}
