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

import com.google.inject.Singleton;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;

@ConfigurationComponent(
   DisplayName = "Select Agents..."
   )
public final class MasterModelConfiguration extends AbstractPublicConfiguration {
   
   private static final long serialVersionUID = 754723257945652632L;
   
   /*            *
    * Submodels  *
    *            */
   
   @Layout(
      Order = 1.0,
      Title = "Commerical Financial Institutions",
      VerboseDescription = 
         "Commercial Banks, Bankruptcy Resolution, Bad Bank and Interbank Markets",
      FieldName = "Banking Subeconomy"
      )
   @Submodel
   @Parameter(
      ID = "COMMERCIAL_BANKS"
      )
   AbstractCommercialBankSubEconomy bankingSubEconomy = 
      new ChartistAndFundamentalistBankSubeconomy();
   
   @Layout(
      Order = 1.1,
      Title = "Central Bank",
      VerboseDescription = "The Lender of Last Resort",
      FieldName = "Central Bank"
      )
   @Submodel
   @Parameter(
      ID = "CENTRAL_BANKS"
      )
   AbstractCentralBankConfiguration
      centralBankSubeconomy = new AbstractCentralBankConfiguration();
   
   @Layout(
      Order = 1.2,
      Title = "Bridge Bank",
      VerboseDescription = "The Bridge Bank",
      FieldName = "Bad Bank"
      )
   @Submodel
   @Parameter(
      ID = "BAD_BANKS"
      )
   AbstractBadBankConfiguration
      badBankSubeconomy = new AbstractBadBankConfiguration();
   
   @Layout(
      Order = 2,
      Title = "Non-Financial Corporations",
      VerboseDescription = "Industrial Firms and Corporations",
      FieldName = "Industrial Subeconomy"
      )
   @Submodel
   @Parameter(
      ID = "FIRMS"
      )
   AbstractFirmsSubEconomy firmsSubEconomy = new MacroeconomicFirmsSubEconomy();
   
   @Layout(
      Order = 3,
      Title = "Households",
      VerboseDescription = "Domestic Savers, Employees, and Consumers",
      FieldName = "Household Subeconomy"
      )
   @Submodel
   @Parameter(
      ID = "HOUSEHOLDS"
      )
   AbstractHouseholdSubEconomy householdSubEconomy = new MacroeconomicHouseholdSubEconomy();
   
   @Layout(
      Order = 4,
      Title = "Non-Bank Financial Institutions",
      VerboseDescription = "Mutual Funds and Noise Traders",
      FieldName = "Fund Subeconomy"
      )
   @Submodel
   @Parameter(
      ID = "MUTUAL_FUNDS"
      )
   AbstractFundSubEconomy fundsSubEconomy = new MutualFundSubEconomy();
   
   @Layout(
      Order = 5,
      Title = "Government",
      VerboseDescription = "Tax Collection, Welfare, Benefits and Crisis Intervention",
      FieldName = "Government Subeconomy"
      )
   @Submodel
   @Parameter(
      ID = "GOVERNMENT"
      )
   GovernmentSubEconomy governmentSubEconomy = new GovernmentSubEconomy();
   
   @Layout(
      Order = 5.1,
      Title = "Financial Intermediation",
      VerboseDescription = "Beneficiary Intermediaries",
      FieldName = "Financial Intermediaries"
      )
   @Submodel
   @Parameter(
      ID = "FINANCIAL_INTERMEDIARIES"
      )
   AbstractIntermediaryConfiguration 
      financialIntermediaries = new AbstractIntermediaryConfiguration();
   
   @Layout(
      Order = 6,
      Title = "Agencies and Ancillaries",
      VerboseDescription = "Ancillary bodies participating indirectly in the economy",
      FieldName = "Agencies & Ancillaries Subeconomy"
      )
   @Submodel
   @Parameter(
      ID = "AGENCIES"
      )
   AgenciesSubEconomy agenciesSubEconomy = new AgenciesSubEconomy();
   
   @Layout(
      Order = 7,
      Title = "Markets",
      VerboseDescription = "Labour, Goods, Stock and Instrument Exchange Structures",
      FieldName = "Markets and Trade Structures"
      )
   @Submodel
   @Parameter(
      ID = "MARKETS"
      )
   MarketsSubEconomy marketsSubEconomy = new MarketsSubEconomy();
   
   public AbstractCommercialBankSubEconomy getBankingSubEconomy() {
      return bankingSubEconomy;
   }
   
   public void setBankingSubEconomy(
      final AbstractCommercialBankSubEconomy value) {
      this.bankingSubEconomy = value;
   }
   
   public AbstractCentralBankConfiguration getCentralBankSubeconomy() {
      return centralBankSubeconomy;
   }
   
   public void setCentralBankSubeconomy(
      final AbstractCentralBankConfiguration value) {
      this.centralBankSubeconomy = value;
   }
   
   public AbstractBadBankConfiguration getBadBankSubeconomy() {
      return badBankSubeconomy;
   }
   
   public void setBadBankSubeconomy(
      final AbstractBadBankConfiguration value) {
      this.badBankSubeconomy = value;
   }
   
   public AbstractFirmsSubEconomy getFirmsSubEconomy() {
      return firmsSubEconomy;
   }
   
   public void setFirmsSubEconomy(final AbstractFirmsSubEconomy firmsSubEconomy) {
      this.firmsSubEconomy = firmsSubEconomy;
   }
   
   public AbstractFundSubEconomy getFundsSubEconomy() {
      return fundsSubEconomy;
   }
   
   public void setFundsSubEconomy(final AbstractFundSubEconomy fundsSubEconomy) {
      this.fundsSubEconomy = fundsSubEconomy;
   }
   
   public GovernmentSubEconomy getGovernmentSubEconomy() {
      return governmentSubEconomy;
   }
   
   public void setGovernmentSubEconomy(final GovernmentSubEconomy governmentSubEconomy) {
      this.governmentSubEconomy = governmentSubEconomy;
   }
   
   public AbstractHouseholdSubEconomy getHouseholdSubEconomy() {
      return householdSubEconomy;
   }
   
   public void setHouseholdSubEconomy(final AbstractHouseholdSubEconomy householdSubEconomy) {
      this.householdSubEconomy = householdSubEconomy;
   }
   
   public MarketsSubEconomy getMarketsSubEconomy() {
      return marketsSubEconomy;
   }
   
   public void setMarketsSubEconomy(final MarketsSubEconomy marketsSubEconomy) {
      this.marketsSubEconomy = marketsSubEconomy;
   }
   
   public AgenciesSubEconomy getAgenciesSubEconomy() {
      return agenciesSubEconomy;
   }
   
   public void setAgenciesSubEconomy(final AgenciesSubEconomy agenciesSubEconomy) {
      this.agenciesSubEconomy = agenciesSubEconomy;
   }
   
   public AbstractIntermediaryConfiguration getFinancialIntermediaries() {
      return financialIntermediaries;
   }
   
   public void setFinancialIntermediaries(
      final AbstractIntermediaryConfiguration financialIntermediaries) {
      this.financialIntermediaries = financialIntermediaries;
   }
   
   @Override
   protected void addBindings() {
      bind(ClearingHouse.class).in(Singleton.class);
   }
}
