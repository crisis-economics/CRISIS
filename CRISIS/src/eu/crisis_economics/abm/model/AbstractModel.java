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
package eu.crisis_economics.abm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.javatuples.Triplet;

import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Doubles;
import com.google.inject.Injector;

import kcl.waterloo.math.ArrayUtils;
import sim.engine.SimState;
import sim.util.Bag;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Recorder.RecordTime;
import eu.crisis_economics.abm.agent.ComputeCapitalAdequacyRatioOperation;
import eu.crisis_economics.abm.agent.ComputeRiskWeightedAssetsOperation;
import eu.crisis_economics.abm.bank.BadBank;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.bank.central.CentralBank;
import eu.crisis_economics.abm.bank.central.CentralBankStrategy;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.RepoLoan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.events.BankDividendDecisionMadeEvent;
import eu.crisis_economics.abm.firm.ClearingFirm;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.firm.MacroFirm;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.fund.BaseFund;
import eu.crisis_economics.abm.fund.MutualFund;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.fund.NoiseTraderFund;
import eu.crisis_economics.abm.fund.PortfolioFund;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.household.ConsumerHousehold;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.household.MacroHousehold;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketUtils;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.DepositMarket;
import eu.crisis_economics.abm.markets.nonclearing.InterbankLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import eu.crisis_economics.abm.model.plumbing.AgentGroup;
import eu.crisis_economics.abm.model.plumbing.BaseAgentGroup;
import eu.crisis_economics.abm.model.plumbing.Plumbing;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.ArrayUtil;
import eu.crisis_economics.utilities.Pair;

/**
  * An abstract base class for simulation models. Implementations of this class should
  * provide the following methods:
  * 
  * <ul>
  *   <li> {@link #getAgentBindings()}, which should provide a {@link ConfigurationComponent}
  *        specifying which {@link Agent}{@code s} and market structures to use, and
  *   <li> {@link #getPlumbing()} which should specify the {@link Plumbing} structure to be
  *        used to create and connect {@link Agent}{@code s} and market structures together.
  * </ul>
  * 
  * @author phillips
  * @author bochmann
  */
/*
 * Recorder notes:
 * // LP: lower priority, not originally included, to speed up simulation 
 *        and reduce size of log file;
 * // C:  composite data, that can be calculated later, and need not be done
 *        during the simulation;
 * // U:  unnecessary data.
 * 
 * (a) avg(loanInterestRates):
 *     is an equally weighted measure - not volume-weighted. This was replaced with 
 *     "meanLoanInterestRate" so that it remains correct when variable loan rates exist.
 * (b)
 */
@Recorder(
   value = "./MasterModel.csv", 
   recordAt = RecordTime.END_OF_ITERATION, 
   sources = {
      "aggregateBankCashReservesMinusLiabilitiesToCentralBank", // C
      "aggregateBankAssets",                                    // C
      "aggregateBankLeverage",                                  // C
      "aggEmployment",                                          // LP
      "AggregateCommercialLoans",                               // C
      "aggregateFundAssets",
      "aggregateHouseholdNetContributionsToFunds",
      "AggregateInterbankLoans",
      
      "avg(bankCapitalAdequacyRatio)",
      "sd(bankCapitalAdequacyRatio)",
      "bankCapitalAdequacyRatio",
      
      "avg(bankCashReserves)",
      "sd(bankCashReserves)",
      "bankCashReserves",
      "sum(bankCashReserves)",
      
      "avg(bankCommercialLoans)",
      "sd(bankCommercialLoans)",
      "bankCommercialLoans",
      
      "avg(bankDepositAccountHoldings)",
      "sd(bankDepositAccountHoldings)",
      "bankDepositAccountHoldings",
      
      "avg(bankDepositsFromHouseholds)",
      "sd(bankDepositsFromHouseholds)",
      "bankDepositsFromHouseholds",
      
      "avg(bankEquity)",
      "sd(bankEquity)",
      "bankEquity",
      "sum(bankEquity)",
      
      "avg(bankInterbankLoans)",
      "sd(bankInterbankLoans)",
      "bankInterbankLoans",
      "sum(bankInterbankLoans)",
      
      "avg(BankLeverage)",
      "sd(BankLeverage)",
      "BankLeverage",
      
      "bankLoans",                                              // LP
      "avg(bankLoans)",                                         // C
      "sd(bankLoans)",                                          // C
      
      "avg(bankRiskWeightedAssets)",
      "sd(bankRiskWeightedAssets)",
      "bankRiskWeightedAssets",
      
      "BankruptcyEvents",
      
      "BanksStockPortfolios",
      
      "avg(bankStockPrice)",
      "sd(bankStockPrice)",
      "min(bankStockPrice)",
      "median(bankStockPrice)",
      "max(bankStockPrice)",
      "bankStockPrice",
      
      "avg(bankTotalAssets)",
      "sd(bankTotalAssets)",
      "bankTotalAssets", 
      
      "CBdepositVolume",
      "CBequity",                                               // C
      "CBLoanVolume",
      "CBovernightDepositRate",
      "CBrefinancingRate", 
      "CommercialLoansToGDPratio",
      
      "dividendPriceRatio",                                     // C
      
      "avg(firmDividend)",
      "sd(firmDividend)",
      "firmDividend",
      "min(firmDividend)",
      "median(firmDividend)",
      "max(firmDividend)",
      
      "avg(firmEquity)",
      "sd(firmEquity)",
      "firmEquity",
      "min(firmEquity)",
      "median(firmEquity)",
      "max(firmEquity)",
      
      "sum(firmEquity)",                                        // LP
      
      "avg(firmGoodsSellingPrice)",
      "sd(firmGoodsSellingPrice)",
      "firmGoodsSellingPrice",
      "min(firmGoodsSellingPrice)",
      "median(firmGoodsSellingPrice)",
      "max(firmGoodsSellingPrice)",
/*      
      "firmLastChangeInStockPrice)",                            // C
      "avg(firmLastChangeInStockPrice)",                        // C
      "sd(firmLastChangeInStockPrice)",                         // C
      "min(firmLastChangeInStockPrice)",                        // C
      "median(firmLastChangeInStockPrice)",                     // C
      "max(firmLastChangeInStockPrice)",                        // C
*/    
      "firmLoansDividedByLoansPlusEquity",                      // C
      "avg(firmLoansDividedByLoansPlusEquity)",                 // C
      "sd(firmLoansDividedByLoansPlusEquity)",                  // C
      "min(firmLoansDividedByLoansPlusEquity)",                 // C
      "median(firmLoansDividedByLoansPlusEquity)",              // C
      "max(firmLoansDividedByLoansPlusEquity)",                 // C
      
      "firmLoansPerEquity",                                     // LP
      "avg(firmLoansPerEquity)",                                // LP
      "sd(firmLoansPerEquity)",                                 // LP
      "min(firmLoansPerEquity)",                                // LP
      "median(firmLoansPerEquity)",                             // LP
      "max(firmLoansPerEquity)",                                // LP
      
      "firmLogStockReturn",                                     // LP
      "avg(firmLogStockReturn)",                                // LP
      "sd(firmLogStockReturn)",                                 // LP
      "min(firmLogStockReturn)",                                // LP
      "median(firmLogStockReturn)",                             // LP
      "max(firmLogStockReturn)",                                // LP
      
      "min(firmProduction)",
      "median(firmProduction)",
      "max(firmProduction)",
      "avg(firmProduction)",
      "sd(firmProduction)",
      "firmProduction",
      
      "min(firmProfit)",
      "median(firmProfit)",
      "max(firmProfit)",
      "avg(firmProfit)",
      "sd(firmProfit)",
      "firmProfit",
      
      "min(firmRevenue)",
      "median(firmRevenue)",
      "max(firmRevenue)",
      "avg(firmRevenue)",
      "sd(firmRevenue)",
      "firmRevenue",
      
      "min(firmStockPrice)",
      "median(firmStockPrice)",
      "max(firmStockPrice)",
      "avg(firmStockPrice)",
      "sd(firmStockPrice)",
      "firmStockPrice",

      "FirmPercentileStockPrices",
      
      "avg(firmTotalAssets)",
      "sd(firmTotalAssets)",
      "firmTotalAssets",
      "min(firmTotalAssets)",
      "median(firmTotalAssets)",
      "max(firmTotalAssets)",
      
      "firmTotalStockReturn",                                   // LP
      "avg(firmTotalStockReturn)",                              // LP
      "sd(firmTotalStockReturn)",                               // LP
      "min(firmTotalStockReturn)",                              // LP
      "median(firmTotalStockReturn)",                           // LP
      "max(firmTotalStockReturn)",                              // LP
      
      "GDP",
      
      "avg(fundBalance)",
      "sd(fundBalance)",
      "fundBalance",
      
      "avg(fundEmissionPrice)",
      "sd(fundEmissionPrice)",
      "fundEmissionPrice",
      
      "avg(fundEquity)",
      "sd(fundEquity)",
      "fundEquity",
      
      "avg(fundPercentageValueInBonds)",
      "sd(fundPercentageValueInBonds)",
      "fundPercentageValueInBonds",
      
      "avg(fundPercentageValueInBankBonds)",
      "sd(fundPercentageValueInBankBonds)",
      "fundPercentageValueInBankBonds",
      
      "avg(fundPercentageValueInGovernmentBonds)",
      "sd(fundPercentageValueInGovernmentBonds)",
      "fundPercentageValueInGovernmentBonds",
      
      "avg(fundPercentageValueInStocks)",
      "sd(fundPercentageValueInStocks)",
      "fundPercentageValueInStocks",
      
      "avg(fundPercentageValueInBankStocks)",
      "sd(fundPercentageValueInBankStocks)",
      "fundPercentageValueInBankStocks",
      
      "avg(fundPercentageValueInFirmStocks)",
      "sd(fundPercentageValueInFirmStocks)",
      "fundPercentageValueInFirmStocks",
      
      "avg(fundTotalAssets)",
      "sd(fundTotalAssets)",
      "fundTotalAssets",
//    "min(fundTotalAssets)",
//    "median(fundTotalAssets)",
//    "max(fundTotalAssets)",
      
      "avg(fundUnreservedCash)",
      "sd(fundUnreservedCash)",
      "fundUnreservedCash",
      
      "CapitalGainsTaxRate",
      "DividendTaxRate",
      "IncomeTaxRate",
      "ValueAddedTaxRate",
      
      "GovernmentCashReserve",
      "GovernmentUnreservedCash",
      
      "PublicSectorWorkforce",
      "PercentageEmployeesWorkingInPublicSector",
      "PercentageHouseholdsWorkingInPublicSector",
      "PercentageOfPopulationOnWelfare",
      
      "GovernmentCumulativeBailoutCosts",
      "GovernmentTaxRevenue",
      "GovernmentBudget",
      
      "GovernmentEquity",
      "GovernmentTotalAssets",
      "GovernmentTotalLiabilities",
      
      "min(householdConsumptionBudget)",
      "median(householdConsumptionBudget)",
      "max(householdConsumptionBudget)",
      "avg(householdConsumptionBudget)",
      "sd(householdConsumptionBudget)",
      "householdConsumptionBudget",
//    "skew(householdConsumptionBudget)",
//    "kurtosis(householdConsumptionBudget)",
      
      "householdEquity",
      "sum(householdEquity)",                                   // LP
      
      "min(householdFundContribution)",
      "median(householdFundContribution)",
      "max(householdFundContribution)",
      "avg(householdFundContribution)",
      "sd(householdFundContribution)",
      "householdFundContribution",
      
      "min(householdSavings)",
      "median(householdSavings)",
      "max(householdSavings)",
      "avg(householdSavings)",
      "sd(householdSavings)",
      "householdSavings",
      
//    "skew(householdSavings)",
//    "kurtosis(householdSavings)",
      
      "min(householdTotalAssets)",
      "median(householdTotalAssets)",
      "max(householdTotalAssets)",
      "avg(householdTotalAssets)",
      "sd(householdTotalAssets)",
      "householdTotalAssets",
      
      "min(householdTotalCashSpentOnGoods)",
      "median(householdTotalCashSpentOnGoods)",
      "max(householdTotalCashSpentOnGoods)",
      "avg(householdTotalCashSpentOnGoods)",
      "sd(householdTotalCashSpentOnGoods)",
      "householdTotalCashSpentOnGoods",
      
//    "skew(householdTotalCashSpentOnGoods)",
//    "kurtosis(householdTotalCashSpentOnGoods)",
      
      "min(householdTotalIncome)",
      "median(householdTotalIncome)",
      "max(householdTotalIncome)",
      "avg(householdTotalIncome)",
      "sd(householdTotalIncome)",
      "householdTotalIncome",
//    "skew(householdTotalIncome)",
//    "kurtosis(householdTotalIncome)",
      
      "min(householdTotalRemuneration)",
      "median(householdTotalRemuneration)",
      "max(householdTotalRemuneration)",
      "avg(householdTotalRemuneration)",
      "sd(householdTotalRemuneration)",
      "householdTotalRemuneration",
//    "skew(householdTotalRemuneration)",
//    "kurtosis(householdTotalRemuneration)",
      
/*      "min(householdWelfareAllowance)",
      "median(householdWelfareAllowance)",
      "max(householdWelfareAllowance)",
      "avg(householdWelfareAllowance)",
      "sd(householdWelfareAllowance)",
      "householdWelfareAllowance",
*/    

//    "loanInterestRates",                                      // U
//    "avg(loanInterestRates)",                                 // U
      "meanLabourBidPrice",                                     // LP
      "meanLoanInterestRate",
      "negativeAggregateBankLiabilitiesToCentralBank",          // LP
      
//    "stockIndexArray",                                        // U
      "TaxMoneySpentForBanks",
      "TotalProduction",                                        // LP
      "UnemploymentPercentage",
      
      "MinUnsoldGoods",
      "AverageUnsoldGoods",
      "MaxUnsoldGoods",
      
//    "lastChangeInGDP",
//    "relativeChangeInGDP",
//      "objectiveFunctionValue",
//      "GDPvolatilityObjectiveFunction",
/*
      "bankUniqueName",
      "firmUniqueName",
      "fundUniqueName",
      "householdUniqueName"
*/
   })
public abstract class AbstractModel extends Simulation {
   
   private static final long serialVersionUID = -5386104326393190433L;
   
   /*             *
    * Population  *
    *             */
   
   private AgentGroup
      population = new BaseAgentGroup();
   
   /*                                       *
    * Standalone Collections for @Recorders *
    *                                       */
   
   @RecorderSource(
      value="fund", 
      collectionLengthMember="getNumberOfFundsForRecorderSampling()", 
      innerType=BaseFund.class
      )
   protected Bag funds = new Bag();
   
   int getNumberOfFundsForRecorderSampling() {
      /**
        * XXX
        * 
        * @Recorder sources are currently not compatible with empty collections.
        */
      return Math.max(1, funds.size());
   }
   
   @RecorderSource(
      value="bank",
      collectionLengthMember="getNumberOfCommercialBanksForRecorderSampling()",
      innerType=CommercialBank.class
      )
   protected Bag banks = new Bag();
   
   int getNumberOfCommercialBanksForRecorderSampling() {
      /**
        * XXX
        * 
        * @Recorder sources are currently not compatible with empty collections.
        */
      return Math.max(1, banks.size());
   }
   
   @RecorderSource(
      value="centralBank",
      collectionLength=1,
      innerType=CentralBank.class
      )
   protected Bag centralBanks = new Bag();
   
   @RecorderSource(
      value="badBank",
      collectionLength=1,
      innerType=BadBank.class
      )
   protected Bag badBanks = new Bag();
   
   @RecorderSource(
      value="household", 
      collectionLengthMember="getNumberOfHouseholdsForRecorderSampling()", 
      innerType=Household.class
      )
   protected Bag households = new Bag();
   
   int getNumberOfHouseholdsForRecorderSampling() {
      /**
        * XXX
        * 
        * @Recorder sources are currently not compatible with empty collections.
        */
      return Math.max(1, households.size());
   }
   
   @RecorderSource(
      value="government", 
      collectionLength=1, 
      innerType=Government.class
      )
   protected Bag governments = new Bag();
   
   @RecorderSource(
      value="firm",
      collectionLengthMember="getNumberOfFirmsForRecorderSampling()",
      innerType=Firm.class
      )
   protected Bag firms = new Bag();
   
   int getNumberOfFirmsForRecorderSampling() {
      /**
        * XXX
        * 
        * @Recorder sources are currently not compatible with empty collections.
        */
      return Math.max(1, firms.size());
   }
   
   /*          *
    * Ctors    *
    *          */
   
   /**
     * Create a {@link AbstractModel} model object. This constructor
     * is equivalent to {@link #AbstractModel}{@code (1L)}.
     */
   public AbstractModel() {
      this(1L);
   }
   
   /**
     * Create a {@link AbstractModel} model with a custom seed.
     */
   public AbstractModel(final long seed) {
      super(seed);
   }
   
   abstract protected AbstractPublicConfiguration getAgentBindings();
   
   abstract protected AbstractPlumbingConfiguration getPlumbing();
   
   @Override
   public void start() {
      final AbstractPublicConfiguration
         agentsConfiguration = getAgentBindings();
      
      agentsConfiguration.checkParameterValidity();
      
      final AbstractPlumbingConfiguration
         plumbingConfiguration = getPlumbing();
      
      plumbingConfiguration.checkParameterValidity();
      
      final Injector
         injector = agentsConfiguration.createInjector();
      
      final Plumbing
         plumbing = plumbingConfiguration.createInjector().getInstance(Plumbing.class);
      
      super.start();
      
      population = plumbing.populate(injector);
      
      funds = new Bag(population.getAgentsOfType(Fund.class));
      banks = new Bag(population.getAgentsOfType(CommercialBank.class));
      centralBanks = new Bag(population.getAgentsOfType(CentralBank.class));
      badBanks = new Bag(population.getAgentsOfType(BadBank.class));
      households = new Bag(population.getAgentsOfType(Household.class));
      governments = new Bag(population.getAgentsOfType(Government.class));
      firms = new Bag(population.getAgentsOfType(Firm.class));
      
      System.out.printf(
         "---------------------------------------------\n" + 
         "Financial System with Macroeconomy Simulation\n" +
         "Simulation Begins                            \n" +
         "---------------------------------------------\n"
         );
      for(NamedEventOrderings event : NamedEventOrderings.values()) {
         Simulation.repeat(this, "inspectTotalCashInSystem",
            CustomSimulationCycleOrdering.create(event, -1));
      }
      for(NamedEventOrderings event : NamedEventOrderings.values()) {
         Simulation.repeat(this, "inspectTotalCashInSystem",
            CustomSimulationCycleOrdering.create(event, 100));
      }
      
      Simulation.repeat(this, "printVerboseState", NamedEventOrderings.AFTER_ALL);
      Simulation.repeat(this, "resetMemories", NamedEventOrderings.BEFORE_ALL);
      
      events().register(this);
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void printVerboseState() {
      final AgentGroupPrinter printer = new AgentGroupPrinter();
      printer.print(population);
   }
   
   public final AgentGroup getPopulation() {
      return population;
   }
   
   private List<Pair<Double, Double>>
      cashRecords = new ArrayList<Pair<Double, Double>>(); 
   
   /**
     * A diagnostic tool for inspecting the total cash in the simulation.
     * 
     * TODO: remove
     */
   @SuppressWarnings("unused") // Scheduled
   private void inspectTotalCashInSystem() {
      double totalCashInSystem = 0.;
      System.out.printf(
         "----------------------------------\n" +
         "Cash Inspection:\n");
      for(Object obj : getBanks()) {
         final Bank bank = (Bank) obj;
         System.out.printf(
            "Bank %s cash: %16.10g\n",
            bank.getUniqueName(),
            bank.getCashReserveValue()
            );
         totalCashInSystem += bank.getCashReserveValue();
      }
      if(hasGovernment())
         totalCashInSystem += getGovernment().getCashReserveValue();
      System.out.printf(
         "Total cash in system, time %g: %16.10g\n",
         Simulation.getTime(),
         totalCashInSystem
         );
      System.out.printf(
         "----------------------------------\n");
      cashRecords.add(Pair.create(Simulation.getTime(), totalCashInSystem));
   }
   
   @SuppressWarnings("unused")
   private void resetMemories() {
      bankLeveragesAfterPendingDividends.clear();
      aggreateBankEquityAfterPendingDividend = 0.;
      aggreateBankRiskyAssetsAfterPendingDividend = 0.;
   }
   
   /*       *
    * GUIs  *
    *       */
   
   /**
     * Notes: 
     * Do we need to repeat this method here in order for the charts to be created?
     */
   @RecorderSource("meanLabourBidPrice")
   public double getMeanLabourBidPrice() {
      return population.getMarketsOfType(
         SimpleLabourMarket.class).get(0).getInstrument(1).getDemandWeightedBidWage();
   }
   
   public double getAvgWealth() {
      try {
         double ret = 0.0;
         ret = getAggregateWealth();
         return ret / getNumberOfHouseholds();
      } catch (NullPointerException e) {
         return 0;
      }
   }
   
   @RecorderSource("AggregateHouseholdWealth")
   public double getAggregateWealth() {
      double ret = 0.0;
      for (final DepositingHousehold household :
           population.getAgentsOfType(DepositingHousehold.class))
         ret += household.getCash();
      return ret;
   }
   
   /**
     * Notes: Problem - this currently returns nominal demand (the budget
     * allocated for consumption. To get actual household consumption, need to
     * use goods account / repository to calculate?
     * 
     * @return
     */
   @RecorderSource(
      value = "householdConsumptionBudget",
      collectionLengthMember = "getNumberOfHouseholdsForRecorderSampling()"
      )
   public List<Double> getHouseholdConsumption() {
      final List<Double>
         ret = Doubles.asList(new double[getNumberOfHouseholdsForRecorderSampling()]);
      int index = 0;
      for(final ConsumerHousehold household :
         population.getAgentsOfType(ConsumerHousehold.class))
         ret.set(index++, household.getConsumptionBudget());
      return ret;
   }
   
   @RecorderSource(
      value = "bankDepositsFromHouseholds",
      collectionLengthMember = "getNumberOfCommercialBanksForRecorderSampling()"
      )
   public List<Double> getBankDepositsFromHouseholds() {
      final List<Double>
         ret = Doubles.asList(new double[getNumberOfCommercialBanksForRecorderSampling()]);
      int index = 0;
      for(final CommercialBank bank :
         population.getAgentsOfType(CommercialBank.class)) {
         double sumDepositValue = 0.;
         for(final DepositAccount deposit : bank.getLiabilitiesDeposits())
            if(deposit.getDepositor() instanceof Household)
               sumDepositValue += deposit.getValue();
         ret.set(index++, sumDepositValue);
      }
      return ret;
   }
   
   /**
     * XXX
     * 
     * The current @Recorder structure is not entirely compatible with 
     * collections of heterogenous agent types (Eg. collections of agents of
     * type Firm, but with distinct implementations). 
     * 
     * Many of the existing @Recorder sources for our model files make the 
     * assumption that Firm collections are, infact, populated with MacroFirm
     * agents. These agents are cast before accessing public method in the 
     * MacroFirm implementation. This unfortunately means that alternative 
     * implementations of Firm cannot be used with the recorder structure. 
     * 
     * Due to limitations in Aitia @Recorder code, potentially the only good 
     * solutions to issue at present would be to (a) extract visitor operations 
     * for each recorder method not contracted by Firm, (b) contract every
     * operation sampled by @Recorder code, or (c) check for the implementing
     * type in the body of the following methods. The latter, although very far
     * from ideal, is the technique used for the time being.
     */
   
   /**
     * Selling price of goods manufactured by firms
     */
   @RecorderSource(
      value = "firmGoodsSellingPrice",
      collectionLengthMember = "getNumberOfFirmsForRecorderSampling()"
      )
   public List<Double> getFirmSellingPrices() { 
      final List<Double> salePrices = new ArrayList<Double>();
      for(final MacroFirm firm : population.getAgentsOfType(MacroFirm.class))
         salePrices.add(firm.getGoodsSellingPrice());
      final List<Double>
         result = new ArrayList<Double>(Collections.nCopies(getNumberOfFirms(), 0.));
      for(int i = 0; i< salePrices.size(); ++i)
         result.set(i, salePrices.get(i));
      return result;
   }
   
   @RecorderSource("aggEmployment")
   public double getAggEmployment() {
      double ret = 0.;
      for(Employee employee : population.getAgentsOfType(Employee.class))
         ret += employee.getLabourAmountEmployed();
      return ret;
   }
   
   public double getTotalWorkforceSize() {
      double ret = 0.;
      for (Household agent : getHouseholds())
         ret += ((Employee) agent).getMaximumLabourSupply();
      return ret;
   }
   
   @RecorderSource("UnemploymentPercentage")
   public double getUnemploymentPercentage() {
      return 100 * (1. - getAggEmployment() / getTotalWorkforceSize());
   }
   
   /**
     * XXX
     * 
     * Incompatible with interchangeable {@link Firm} {@link Agent}{@code s}.
     * Result will be {@code zero} for non-{@link MacroFirm} {@link Agent}{@code s}.
     */
   @RecorderSource("GDP")
   public double getGDP() {
      if (Simulation.getTime() <= 0.)
         return 0.;
      double gdp = 0.;
      for(final MacroFirm firm : population.getAgentsOfType(MacroFirm.class)) {           // TODO:
         gdp += firm.getProduction() * firm.getGoodsSellingPrice();
         Map<String, Double>
            amountOfGoodsPurchased = firm.getGoodsPurchased(),
            effectivePricesOfPurchasedGoods = firm.getPriceOfGoodsPurchased();
         for(final Entry<String, Double> record : amountOfGoodsPurchased.entrySet()) {
            /* 
             * Subtract input goods exchanged during the production process.
             */
            gdp -= record.getValue() * effectivePricesOfPurchasedGoods.get(record.getKey());
         }
      }
      return gdp;
   }
   
   @RecorderSource("CommercialLoansToGDPratio")
   public double getCommercialLoansToGDPratio() {
      if(Simulation.getTime() <= 0.) return 0.;
      return getAggregateCommercialLoans() / getGDP();
   }
   
   public Number getAvgFirmEquity() {
      double ret = 0.;
      for (final Firm firm : getFirms())
         ret += firm.getEquity();
      return ret / getFirms().size();
   }
   
   public Number getAvgFirmAssets() {
      double ret = 0.;
      for (final Firm firm : getFirms())
         ret += firm.getTotalAssets();
      return ret / getFirms().size();
   }
   
   public Number getAvgFirmLiabilities() {
      double ret = 0.;
      for (final Firm firm : getFirms())
         ret += firm.getTotalLiabilities();
      return ret / getFirms().size();
   }
   
   /**
     * XXX
     * 
     * Incompatible with interchangeable {@link Firm} {@link Agent}{@code s}.
     * Result will be {@code zero} for non-{@link MacroFirm} {@link Agent}{@code s}.
     */
   public Number getAggMaxAskedLoan() {
      double ret = 0.;
      for(final MacroFirm firm : population.getAgentsOfType(MacroFirm.class))
         ret += firm.getMaxAskedLoan();
      return ret;
   }
   
   public Number getAggEffectiveLoan() {
      double ret = 0.;
      for(final Firm firm : population.getAgentsOfType(Firm.class)) {
         final List<Loan>
            loanLiabilities = firm.getLiabilitiesLoans();
         for(final Loan loan : loanLiabilities)
            ret += loan.getValue();
      }
      return ret;
   }
   
   @RecorderSource("AverageUnsoldGoods")
   public Number getAverageUnsoldGoods() {
      double ret = 0.;
      for(final LoanStrategyFirm firm :
         population.getAgentsOfType(LoanStrategyFirm.class))
         ret += firm.getUnsold();
      if(ret == 0.) return 0.;
      else return ret / getFirms().size();
   }
   
   @RecorderSource("MaxUnsoldGoods")
   public Number getMaxUnsoldGoods() {
      double ret = 0.;
      for(final LoanStrategyFirm firm :
         population.getAgentsOfType(LoanStrategyFirm.class))
         ret = Math.max(ret, firm.getUnsold());
      return ret;
   }
   
   @RecorderSource("MinUnsoldGoods")
   public Number getMinUnsoldGoods() {
      double ret = Double.MAX_VALUE;
      for(final LoanStrategyFirm firm :
         population.getAgentsOfType(LoanStrategyFirm.class))
         ret = Math.min(ret, firm.getUnsold());
      return ret;
   }
   
   public double getAvgPrice() {
      double
         ret = 0.,
         totalProduction = 0.,
         pq = 0.;
      for(final LoanStrategyFirm firm :
         population.getAgentsOfType(LoanStrategyFirm.class)) {
         pq += firm.getProduction() * firm.getGoodsSellingPrice();
         totalProduction += firm.getProduction();
      }
      ret = pq / totalProduction;
      return ret;
   }
   
   public Number getAvgProfit() {
      double ret = 0.;
      for(final LoanStrategyFirm firm :
         population.getAgentsOfType(LoanStrategyFirm.class))
         ret += firm.getProfit();
      if(ret == 0.) return 0.;
      else return ret / getFirms().size();
   }
   
   public Number getMinProfits() {
      double ret = Double.MAX_VALUE;
      for(final LoanStrategyFirm firm :
         population.getAgentsOfType(LoanStrategyFirm.class))
         ret = Math.min(ret, firm.getProfit());
      return ret;
   }
   
   public Number getMaxProfits() {
      double ret = 0.;
      for(final LoanStrategyFirm firm :
         population.getAgentsOfType(LoanStrategyFirm.class))
         ret = Math.max(ret, firm.getProfit());
      return ret;
   }
   
   public Number getAggDividend() {
      double ret = 0.;
      for(final StockReleasingFirm firm :
         population.getAgentsOfType(StockReleasingFirm.class))
         ret += firm.getCurrentDividend();
      return ret;
   }
   
   /**
     * XXX
     * 
     * Not compatible with non-{@link MacroFirm} {@link Agent}{@code s}.
     */
   public Number getAverageMarkUp() {
      double ret = 0.;
      for(final MacroFirm firm : population.getAgentsOfType(MacroFirm.class))
         ret += firm.getMarkUp();
      if(ret == 0.) return 0.;
      else return ret / getFirms().size();
   }
   
   /**
     * XXX
     * 
     * Not compatible with non-{@link MacroFirm} {@link Agent}{@code s}.
     */
   public Number getMaxMarkUp() {
      double ret = 0.;
      for(final MacroFirm firm : population.getAgentsOfType(MacroFirm.class))
         ret = Math.max(ret, firm.getMarkUp());
      return ret;
   }
   
   public Number getMinMarkUp() {
      double ret = 1.e2;
      for(final MacroFirm firm : population.getAgentsOfType(MacroFirm.class))
         ret = Math.min(ret, firm.getMarkUp());
      return ret;
   }
   
   public Number getAggHouseholdConsumption() {
      double ret = 0.;
      for(final ConsumerHousehold household :
         population.getAgentsOfType(ConsumerHousehold.class))
         ret += household.getConsumptionBudget();
      return ret;
   }
   
   public Number getAggHouseholdDeposits() {
      double ret = 0.;
      for(final DepositingHousehold household :
         population.getAgentsOfType(DepositingHousehold.class))
         ret += household.getDepositValue();
      return ret;
   }
   
   public Number getAggBankInvestments() {
      double ret = 0.;
      for(final CommercialBank bank :
         population.getAgentsOfType(CommercialBank.class))
         ret += bank.getAssetsInLoans() + bank.getAssetsInStock();
      return ret;
   }
   
   @RecorderSource("AggregateCommercialLoans")
   public double getAggregateCommercialLoans(){
      double sum = 0.;
      for(final CommercialBank bank :
         population.getAgentsOfType(CommercialBank.class))
         sum += bank.getCommercialLoans();
      return sum;
   }
   
   public double getAggBankInvestmentsInStocks() {
      double ret = 0.;
      for(final CommercialBank bank :
         population.getAgentsOfType(CommercialBank.class))
         ret += bank.getAssetsInStock();
      return ret;
   }
   
   /**
     * Get the aggregate value of fund assets.
     */
   @RecorderSource("aggregateFundAssets")
   public Number getAggregateFundAssets() {
      double result = 0.;
      for(final Fund fund : population.getAgentsOfType(Fund.class))
         result += fund.getTotalAssets();
      return result;
   }
   
   /**
     * Compute the total, current, aggregate {@link Fund} investment by instrument. This
     * method returns a {@link List} of size 5 with the following format:<br><br>
     * 
     * record:<br>
     *    (1) the aggregate {@link Fund} cash reserve value;<br>
     *    (2) the aggregate value of {@link Fund} investments in {@link Firm} stocks;<br>
     *    (3) the aggregate value of {@link Fund} investments in {@link Bank} stocks;<br>
     *    (4) the aggregate value of {@link Fund} investments in {@link Government}
     *        {@link Bonds};<br>
     *    (5) the aggregate value of {@link Fund} investments in {@link Bank} {@link Bonds};
     *        <br><br>
     * 
     * This method includes {@link MutualFund}{@code s} as well as {@link NoiseTraderFund}
     * {@code s} when computing the overall investment size.
     */
   public List<Double> getAggreateFundAssetsByInvestmentType() {
      try {
         double[] result = new double[5];
         for(final Fund agent : getFunds()) {
            final PortfolioFund fund = (PortfolioFund) agent;
            final double
               fundAssets = fund.getTotalAssets(),
               fundCashReserve = fund.getUnallocatedCash(),
               fundInvestmentInFirmStocks = fund.getPercentageValueInFirmStocks() * fundAssets,
               fundInvestmentInBankStocks = fund.getPercentageValueInBankStocks() * fundAssets,
               fundInvestmentInGovernmentBonds =
                  fund.getPercentageValueInGovernmentBonds() * fundAssets,
               fundInvestmentInBankBonds = fund.getPercentageValueInBankBonds() * fundAssets;
            result[0] += fundCashReserve;
            result[1] += fundInvestmentInFirmStocks;
            result[2] += fundInvestmentInBankStocks;
            result[3] += fundInvestmentInGovernmentBonds;
            result[4] += fundInvestmentInBankBonds;
         }
         // Normalize to cumulative percentages
         double
            total = (result[0] + result[1] + result[2] + result[3] + result[4]);
         for(int i = 4; i>= 0; --i)
            for(int j = 0; j< i; ++j)
               result[i] += result[j];
         for(int i = 0; i< 5; ++i)
            result[i] /= total * 1.e-2;
         return ArrayUtils.toArrayList(result);
      }
      catch(final Exception failed) {
         return Arrays.asList(0., 0., 0., 0., 0.);
      }
   }
   
   /**
     * Compute the total, current, aggregate {@link Bank} investment by instrument. This
     * method returns a {@link List} of size 5 with the following format:<br><br>
     * 
     * record:<br>
     *    (1) the aggregate {@link Bank} cash reserve value;<br>
     *    (2) the aggregate value of {@link Bank} investments in {@link Firm} stocks;<br>
     *    (3) the aggregate value of {@link Bank} investments in {@link Bank} stocks;<br>
     *    (4) the aggregate value of {@link Bank} investments in {@link Government}
     *        {@link Bonds};<br>
     *    (5) the aggregate value of {@link Bank} investments in {@link Firm} {@link Loans};
     *        <br><br>
     * 
     * Each measurement is expressed as a percentage of total asset values held by all
     * {@link Bank}{@code s}.
     */
   public List<Double> getAggreateBankAssetsByInvestmentType() {
      double[] result = new double[5];
      for(final CommercialBank bank : population.getAgentsOfType(CommercialBank.class)) {
         double
            valueOfBankShares = 0.,
            valueOfFirmShares = 0.,
            valueOfGilts = 0.,
            valueOfFirmLoans = 0.;
         for(final Contract contract : bank.getAssets())
            if(contract instanceof StockAccount) {
               final StockAccount stockAccount = (StockAccount) contract;
               if(stockAccount.getReleaser() instanceof Bank)
                  valueOfBankShares += stockAccount.getValue();
               else if(stockAccount.getReleaser() instanceof Firm)
                  valueOfFirmShares += stockAccount.getValue();
            }
            else if(contract instanceof Loan) {
               final Loan loan = (Loan) contract;
               if(hasGovernment() && loan.getBorrower() == getGovernment())
                  valueOfGilts += loan.getValue();
               else if(loan.getBorrower() instanceof Firm)
                  valueOfFirmLoans += loan.getValue();
            }
         result[0] += bank.getCashReserveValue();
         result[1] += valueOfFirmShares;
         result[2] += valueOfBankShares;
         result[3] += valueOfGilts;
         result[4] += valueOfFirmLoans;
      }
      // Normalize to cumulative percentages
      double
         total = (result[0] + result[1] + result[2] + result[3] + result[4]);
      for(int i = 4; i>= 0; --i)
         for(int j = 0; j< i; ++j)
            result[i] += result[j];
      for(int i = 0; i< 5; ++i)
         result[i] /= total * 1.e-2;
      return ArrayUtils.toArrayList(result);
   }
   
   
   /**
    * XXX
    * 
    * This implementation is specific to MacroHousehold and will break if 
    * an alternative household agent is used.
    */
   @RecorderSource("aggregateHouseholdNetContributionsToFunds")
   public Number getAggregateHouseholdFundContribution() {
      double result = 0.;
      // TODO
      for(final Fund fund : population.getAgentsOfType(Fund.class))
         result += fund.getTotalLiabilities();
      return result;
   }
   
   @RecorderSource("GovernmentTaxRevenue")
   public double getTaxRevenue() {
      return hasGovernment() ? getGovernment().getTaxRevenue() : 0.;
   }
   
   @RecorderSource("GovernmentBudget")
   public double getTotalExpensesBudget() {
      return hasGovernment() ? getGovernment().getTotalExpensesBudget() : 0.;
   }
   
   @RecorderSource("GovernmentCashReserve")
   public double getGovernmentCashReserveValue() {
      return hasGovernment() ? getGovernment().getCashReserveValue() : 0.;
   }
   
   @RecorderSource("GovernmentUnreservedCash")
   public double getUnreservedCash() {
      return hasGovernment() ? getGovernment().getUnallocatedCash() : 0.;
   }
   
   @RecorderSource("GovernmentEquity")
   public double getGovernmentEquity() {
      return hasGovernment() ? getGovernment().getEquity() : 0.;
   }
   
   @RecorderSource("GovernmentTotalAssets")
   public double getTotalGovernmentAssets() {
      return hasGovernment() ? getGovernment().getTotalAssets() : 0.;
   }
   
   @RecorderSource("GovernmentTotalLiabilities")
   public double getTotalGovernmentLiabilities() {
      return hasGovernment() ? getGovernment().getTotalLiabilities() : 0.;
   }
   
   @RecorderSource("GovernmentCumulativeBailoutCosts")
   public double getGovernmentCumulativeBailoutCosts() {
      return hasGovernment() ? getGovernment().getTotalLiquiditySpentOnBailouts() : 0.;
   }
   
   @RecorderSource("PublicSectorWorkforce")
   public double getPublicSectorWorkforce() {
      return hasGovernment() ? getGovernment().getLabourForce() : 0.;
   }
   
   @RecorderSource("PublicSectorWorkforcePerTotalAvailableWorkforcePercentage")
   public double getPublicSectorWorkforcePerTotalAvailableWorkforcePercentage() {
      return getPublicSectorWorkforce() * 1.e2 / getTotalWorkforceSize();
   }
   
   @RecorderSource("PublicSectorWorkforcePerTotalEmployedPercentage")
   public double getPublicSectorWorkforcePerTotalEmployedPercentage() {
      return getPublicSectorWorkforce() * 1.e2 / getAggEmployment();
   }
   
   @RecorderSource("CapitalGainsTaxRate")
   public double getCapitalGainsTaxLevel() {
      return hasGovernment() ? getGovernment().getCapitalGainsTaxLevel() : 0.;
   }
   
   @RecorderSource("DividendTaxRate")
   public double getDividendTaxLevel() {
      return hasGovernment() ? getGovernment().getDividendTaxLevel() : 0.;
   }
   
   @RecorderSource("IncomeTaxRate")
   public double getIncomeTaxRate() {
      return hasGovernment() ? getGovernment().getLabourTaxLevel() : 0.;
   }
   
   @RecorderSource("ValueAddedTaxRate")
   public double getValueAddedTaxRate() {
      return hasGovernment() ? getGovernment().getGoodsTaxLevel() : 0.;
   }
   
   public int getNumberOfWelfareRecipients() {
      return hasGovernment() ? getGovernment().getNumberOfWelfareRecipients() : 0;
   }
   
   @RecorderSource("PercentageOfPopulationOnWelfare")
   public double getPercentageOfPopulationReceivingWelfareBenefits() {
      return getNumberOfWelfareRecipients() * 1.e2 / getNumberOfHouseholds();
   }
   
   @Override
   public void step(final SimState state) {
      super.step(state);
      this.updateGDP();
   }
   
   double
      lastGDP = 0.,
      currentGDP = 0.;
   
   private void updateGDP() {
      lastGDP = currentGDP;
      currentGDP = this.getGDP();
   }
   
   @RecorderSource("lastChangeInGDP")
   public double getLastChangeInGDP() {
      if (Simulation.getTime() <= 0.) {
         return 0.;
      } else {
         double dGDP = currentGDP - lastGDP;
         return dGDP;
      }
   }
   
   @RecorderSource("relativeChangeInGDP")
   public double relativeChangeInGDP() {
      return this.getLastChangeInGDP() / currentGDP;
   }
   
   @RecorderSource("GDPvolatilityObjectiveFunction")
   public double GDPvolatilityObjectiveFunction() {
      return (Math.abs(this.relativeChangeInGDP()) - 0.1);
   }
   
   public double getInterestRate() {
      try {
         final ClearingLoanMarket market =
            getClearingHouse().getMarketsOfType(ClearingLoanMarket.class).get(0);   // TODO
         return ClearingMarketUtils.estimateLastReturnRateForLoanMarkets(
            market, Arrays.asList("Commercial Loan"));
      } catch (final NullPointerException e) {
         return 0.;
      }
   }
   
   @RecorderSource("dividendPriceRatio") 
   /*
    * Warning - this measurement is merely the dividend-price ratio and does 
    * not include gains/losses due to change in share price (a.k.a. capital gains).
    */
   public double getStockReturn() {
      double ret = 0.;
      final List<StockReleasingFirm>
         firms = population.getAgentsOfType(StockReleasingFirm.class);
      for(final StockReleasingFirm firm : firms) {
         try {
            double
               price = UniqueStockExchange.Instance.getStockPrice(firm),
               divPerShare = ((ClearingFirm)firm).getDividendPerShare();
            ret +=  divPerShare/price;
         } catch (final NullPointerException e) {
         }
      }
      if(firms.size() == 0) return 0.;
      return ret / getFirms().size();
   }
   
   @RecorderSource("AggregateInterbankLoans")
   public double getAggregateInterbankLoans(){
      double sum = 0.;
      for(final Bank bank : population.getAgentsOfType(CommercialBank.class))
         sum += bank.getInterbankLoans();
      return sum;
   }
   
   @RecorderSource("CommercialLoansToProductionRatio")
   public double getCommercialLoansToProductionRatio(){
      return getAggregateCommercialLoans() / getTotalProduction();
   }
   
   /**
     * Get the maximum, minimum and average stock price for firms.
     * @return A tripplet of doubles, with format
     *         [min. stock price, mean stock price, max. stock price]
     */
   public final Triplet<Double, Double, Double> getFirmStockPrices() {
      double
         avgStockPrice = 0.,
         minStockPrice = Double.MAX_VALUE,
         maxStockPrice = -1.;
      int
         numStocksCounted = 0;
      for(final Firm agent : getFirms()) {
         final StockReleasingFirm firm = (StockReleasingFirm) agent;
         try {
            double pricePerShare = UniqueStockExchange.Instance.getStockPrice(firm);
            avgStockPrice += pricePerShare;
            minStockPrice = Math.min(minStockPrice, pricePerShare);
            maxStockPrice = Math.max(maxStockPrice, pricePerShare);
            ++numStocksCounted;
         } 
         catch (final NullPointerException e) { }
      }
      if(numStocksCounted > 0)
         avgStockPrice /= numStocksCounted;
      return new Triplet<Double, Double, Double>(
         minStockPrice, avgStockPrice, maxStockPrice);
   }
   
   /**
    * Get the maximum, minimum and average {@link Bank} stock price.
    * 
    * @return A tripplet of doubles, with format
    *         [min. stock price, mean stock price, max. stock price]
    */
   public final Triplet<Double, Double, Double> getBankStockPrices() {
      double
         avgStockPrice = 0.,
         minStockPrice = Double.MAX_VALUE,
         maxStockPrice = -1.;
      int
         numStocksCounted = 0;
      for(final CommercialBank bank :
         population.getAgentsOfType(CommercialBank.class)) {
         final double
            pricePerShare = UniqueStockExchange.Instance.getStockPrice(bank);
         avgStockPrice += pricePerShare;
         minStockPrice = Math.min(minStockPrice, pricePerShare);
         maxStockPrice = Math.max(maxStockPrice, pricePerShare);
         ++numStocksCounted;
      }
      if(numStocksCounted > 0)
         avgStockPrice /= numStocksCounted;
      return new Triplet<Double, Double, Double>(
         minStockPrice, avgStockPrice, maxStockPrice);
   }
   
   @RecorderSource(
      value="FirmPercentileStockPrices",
      collectionLength=10
      )
   public List<Double> getPercentileFirmStockPrices() {
      final int
         numPercentilesToPlot = 10;
      List<Double>
         result = new ArrayList<Double>(),
         allFirmStockPrices = new ArrayList<Double>();
      for(final Firm firm : population.getAgentsOfType(StockReleasingFirm.class))
         allFirmStockPrices.add(
            UniqueStockExchange.Instance.getStockPrice(firm.getUniqueName()));
      for(int i = 0; i< numPercentilesToPlot; ++i) {
         final double
            medianPercentile = (1./numPercentilesToPlot) * (i + 1);
         Median median = new Median();
         median.setData(ArrayUtil.toPrimitive(allFirmStockPrices.toArray(new Double[0])));
         result.add(median.evaluate(medianPercentile * 1.e2));
      }
      return result;
   }
   
   @RecorderSource(
         value = "firmStockPrice",
         collectionLengthMember = "getNumberOfFirmsForRecorderSampling()"
         )                                                  // TODO: is this redundant?
   public List<Double> getFirmStockPriceOvertime() {
      List<Double> allFirmStockPrices = new ArrayList<Double>();
      for(final Firm firm : population.getAgentsOfType(StockReleasingFirm.class))
         allFirmStockPrices.add(
               UniqueStockExchange.Instance.getStockPrice(firm.getUniqueName()));
      return allFirmStockPrices;
      }
   
   /**
     * This method is as {@link #getFirmStockPrices()}, with the exception that the
     * return value is an array of size 3 and not a tripplet of doubles.
     * @author Ross Richardson
     */
   @RecorderSource(value = "getFirmStockPrices", collectionLength=3)  
   public final double[] getFirmStockPricesAsArray() {
      final double[] minAvgMaxArray = new double[3];
      minAvgMaxArray[0] = this.getFirmStockPrices().getValue0();
      minAvgMaxArray[1] = this.getFirmStockPrices().getValue1();
      minAvgMaxArray[2] = this.getFirmStockPrices().getValue2();
      return minAvgMaxArray;
   }
   
   /**
     * This method is equivalent to {@link #getFirmStockPrices()}{@code .getValue1()}.
     */
   @RecorderSource("stockIndexAvg")
   public double getAverageFirmStockPrice() {
      return this.getFirmStockPrices().getValue1();
   }
   
   public Number getAvgBankEquity() {
      double ret = 0.0;
      for(final CommercialBank bank : population.getAgentsOfType(CommercialBank.class))
         ret += bank.getEquity();
      return ret / getBanks().size();
   }
   
   @RecorderSource(
         value = "bankStockPrice",
         collectionLengthMember="getNumberOfCommercialBanksForRecorderSampling()"
         )                                                  // TODO: is this redundant?
   public List<Double> getBankStockPriceOvertime() {
      List<Double> allBankStockPrices = new ArrayList<Double>();
      for(final CommercialBank bank : population.getAgentsOfType(CommercialBank.class))
         allBankStockPrices.add(
               UniqueStockExchange.Instance.getStockPrice(bank.getUniqueName()));
      return allBankStockPrices;
      }
   
   private Map<String, Double>
      bankLeveragesAfterPendingDividends = new HashMap<String, Double>();
   private double
      aggreateBankRiskyAssetsAfterPendingDividend = 0.,
      aggreateBankEquityAfterPendingDividend;
   @Subscribe
   public void listenForBankDividendDecisions(final BankDividendDecisionMadeEvent event) {
      bankLeveragesAfterPendingDividends.put(
         event.getBankName(), event.getLeverageAfterDividend());
      aggreateBankRiskyAssetsAfterPendingDividend +=
         event.getEffectiveRiskyAssetsValue();
      aggreateBankEquityAfterPendingDividend +=
         event.getEffectiveEquity();
   }
   
   public Number getAvgBankLeverage() {
      double ret = 0.;
      for(final Double value : bankLeveragesAfterPendingDividends.values())
         ret += value;
      if(bankLeveragesAfterPendingDividends.size() == 0) return 0.;
      return ret / bankLeveragesAfterPendingDividends.size();
   }
   
   @RecorderSource(
      value="BankLeverage",
      collectionLengthMember="getNumberOfBanks()"
      )
   public List<Double> getBankLeverageOverTime() {    // TODO: temporary
      return Collections.unmodifiableList(
         new ArrayList<Double>(bankLeveragesAfterPendingDividends.values()));
   }
   
   @RecorderSource("avgProduction")
   public double getAvgProduction() {
      double ret = 0.;
      final List<LoanStrategyFirm>
         firms = population.getAgentsOfType(LoanStrategyFirm.class);
      for (final LoanStrategyFirm firm : firms)
         ret += firm.getProduction();
      if(firms.isEmpty()) return 0.;
      return ret / getFirms().size();
   }
   
   @RecorderSource("TotalProduction")
   public double getTotalProduction() {
      double totalProduction = 0;
      for(LoanStrategyFirm firm : population.getAgentsOfType(LoanStrategyFirm.class))
         totalProduction += firm.getProduction();
      return totalProduction;
   }
   
   public Number getTotalEmployment() {
      double ret = 0.;
      for (final ClearingFirm firm : population.getAgentsOfType(ClearingFirm.class))
         ret += firm.getEmployment();
      return ret;
   }
   
   @RecorderSource("AggregateBankDeposits")
   public double getAggregateBankDeposits() {
      double ret = 0.;
      List<Double> depositArr = getBankDepositsOverTime();
      for (int i = 0; i < depositArr.size(); i++)
         ret += depositArr.get(i).doubleValue();
      return ret;
   }
   
   @RecorderSource(
      value = "bankLiabilitiesToCentralBank",
      collectionLengthMember="getNumberOfCommercialBanksForRecorderSampling()"
      )
   public List<Double> getBankLiabilitiesToCentralBank() {
       final List<Double>
          ret = Doubles.asList(
             new double[getNumberOfCommercialBanksForRecorderSampling()]);
       int index = 0;
       for(final CommercialBank bank :
          population.getAgentsOfType(CommercialBank.class)) {
          double valueBorrowedFromCentralBank = 0.0;
          for (int i = 0; i < ((Bank) bank).getLiabilities().size(); i++) {
             if ( ((Bank) bank).getLiabilities().get(i) instanceof Loan )
                if( ((Loan)((Bank) bank).getLiabilities().get(i)).getLender() instanceof CentralBank )
                   valueBorrowedFromCentralBank += ((Bank) bank).getLiabilities().get(i).getValue();
             if ( ((Bank) bank).getLiabilities().get(i) instanceof RepoLoan )
                if( ((RepoLoan)((Bank) bank).getLiabilities().get(i)).getLender() instanceof CentralBank )
                   valueBorrowedFromCentralBank += ((Bank) bank).getLiabilities().get(i).getValue();
          }
          ret.set(index++, valueBorrowedFromCentralBank);
       }
       return ret;
   }
   
   @RecorderSource("negativeAggregateBankLiabilitiesToCentralBank")
   public double getNegativeAggregateBankLiabilitiesToCentralBank() {
     double ret = 0;
     for(int i = 0; i < getBankLiabilitiesToCentralBank().size(); i++)
        ret -= getBankLiabilitiesToCentralBank().get(i);
     return ret;
   }
   
   @RecorderSource("aggregateBankCashReservesMinusLiabilitiesToCentralBank")
   public double getAggregateBankCashReservesMinusLiabilitiesToCentralBank() {
     return getAggregateBankCashReserves() + getNegativeAggregateBankLiabilitiesToCentralBank();
   }
   
   @RecorderSource("CBLoanVolume")
   public double getCBLoanVolume() {
      try {
         final List<CentralBank> agents =
            population.getAgentsOfType(CentralBank.class);
         if(agents.isEmpty()) return 0.;
         final CentralBank centralBank = agents.get(0);
         return ((CentralBankStrategy) centralBank.getStrategy()).getCBLoanVolumeFor(centralBank);
      } catch (final NullPointerException e) {
         return 0.;
      }
   }
   
   /**
     * Not currently working - returning a large number error....
     * 
     * @return
     */
   @RecorderSource("CBequity")
   public double getCBequity() {
      try {
         final List<CentralBank> agents =
            population.getAgentsOfType(CentralBank.class);
         if(agents.isEmpty()) return 0.;
         final CentralBank centralBank = agents.get(0);
         return centralBank.getEquity();
      } catch (final NullPointerException e) {
         return 0.;
      }
   }
   
   @RecorderSource("CBdepositVolume")
   public double getOvernightVolume() {
      try {
         final List<CentralBank> agents =
            population.getAgentsOfType(CentralBank.class);
         if(agents.isEmpty()) return 0.;
         final CentralBank centralBank = agents.get(0);
         return ((CentralBankStrategy) centralBank.getStrategy())
            .getOvernightDepositVolume(centralBank);
      } catch (final NullPointerException e) {
         return 0.;
      }
   }
   
   @RecorderSource("BankruptcyEvents")
   public double getCrisisInterventions() {
      try {
         final List<CentralBank> agents =
            population.getAgentsOfType(CentralBank.class);
         if(agents.isEmpty()) return 0.;
         final CentralBank centralBank = agents.get(0);
         return ((CentralBankStrategy) centralBank.getStrategy())
            .getCrisisInterventions().size();
      } catch (final NullPointerException e) {
         return 0.;
      }
   }
   
   @RecorderSource("CBrefinancingRate")
   public double getKeyInterestRate() {
      try {
         final List<CentralBank> agents =
            population.getAgentsOfType(CentralBank.class);
         if(agents.isEmpty()) return 0.;
         final CentralBank centralBank = agents.get(0);
         return ((CentralBankStrategy) centralBank.getStrategy())
            .getRefinancingRate();
      } catch (final NullPointerException e) {
         return 0.;
      }
   }
   
   @RecorderSource("CBovernightDepositRate")
   public double getCBovernightDepositRate() {
      try {
         final List<CentralBank> agents =
            population.getAgentsOfType(CentralBank.class);
         if(agents.isEmpty()) return 0.;
         final CentralBank centralBank = agents.get(0);
         return ((CentralBankStrategy) centralBank.getStrategy())
            .getOvernightDepositRate();
      } catch (final NullPointerException e) {
         return 0.;
      }
   }
   
   @RecorderSource("TaxMoneySpentForBanks")
   public double getTaxMoneySpentForBanks() {
      return hasGovernment() ? getGovernment().getTotalLiquiditySpentOnBailouts() : 0.;
   }
   
   // Misc. getters.
   
   public final List<Household> getHouseholds() {
      return population.getAgentsOfType(Household.class);
   }
   
   @Override
   public int getNumberOfBanks() {
      return population.getAgentsOfType(CommercialBank.class).size();
   }
   
   @Override
   public int getNumberOfFirms() {
      return population.getAgentsOfType(Firm.class).size();
   }
   
   @Override
   public int getNumberOfHouseholds() {
      return population.getAgentsOfType(Household.class).size();
   }
   
   @Override
   public DepositMarket getDepositMarket() {
      final List<DepositMarket>
         markets = population.getMarketsOfType(DepositMarket.class);
      if(markets.isEmpty()) return null;
      return markets.get(0);
   }
   
   @Override
   public CommercialLoanMarket getCommercialLoanMarket() {
      final List<CommercialLoanMarket>
         markets = population.getMarketsOfType(CommercialLoanMarket.class);
      if(markets.isEmpty()) return null;
      return markets.get(0);
   }
   
   @Override
   public InterbankLoanMarket getInterbankLoanMarket() {
      final List<InterbankLoanMarket>
         markets = population.getMarketsOfType(InterbankLoanMarket.class);
      if(markets.isEmpty()) return null;
      return markets.get(0);
   }
   
   @Override
   public StockMarket getStockMarket() {
      final List<StockMarket>
         markets = population.getMarketsOfType(StockMarket.class);
      if(markets.isEmpty()) return null;
      return markets.get(0);
   }
   
   @RecorderSource(
      value = "firmProfit",
      collectionLengthMember="getNumberOfFirmsForRecorderSampling()"
      )
   public List<Double> getProfit() {
      final List<Double>
         profit = Doubles.asList(
            new double[getNumberOfFirmsForRecorderSampling()]);
      int index = 0;
      for(final LoanStrategyFirm firm : population.getAgentsOfType(LoanStrategyFirm.class))
         profit.set(index++, firm.getProfit());
      return profit;
   }
   
   public List<Double> getAllLoanVolumes() {
       final ArrayList<Double>
          ret = new ArrayList<Double>();
       for(final Contract act : Contract.getContracts()) {
           if ( act instanceof Loan ) {
               final Loan loan = (Loan) act;
               ret.add( loan.getLoanPrincipalValue() );
           }
       }
       return ret;
   }
   
   public List<Double> getAllStockAccountVolumes() {
       final ArrayList<Double> ret = new ArrayList<Double>();
       for (final Contract act : Contract.getContracts()) {
           if ( act instanceof StockAccount ) {
               final StockAccount stockAccount = (StockAccount) act;
               ret.add( stockAccount.getQuantity() );
           }
       }
       
       return ret;
   }
   
   @RecorderSource(
      value = "bankCashReserves",
      collectionLengthMember="getNumberOfCommercialBanksForRecorderSampling()"
      )
   public List<Double> getBankCashOverTime() {
       final List<Double> ret =
          Doubles.asList(new double[getNumberOfCommercialBanksForRecorderSampling()]);
       int index = 0;
       for (final Bank bank : population.getAgentsOfType(CommercialBank.class))
          ret.set(index++, bank.getCashReserveValue());
       return ret;
   }
   
   @RecorderSource(
      value = "BanksStockPortfolios",
      collectionLengthMember="getBanksStockPortfoliosArrayLengthForRecorderSources()"
      )
   public List<Double> getBanksStockPortfolios() {
       final List<Double>
          ret = Doubles.asList(
             new double[getBanksStockPortfoliosArrayLengthForRecorderSources()]);
       int index = 0;
       for(final CommercialBank bank :
          population.getAgentsOfType(CommercialBank.class)) {
          final List<Double>
             stockInvestments = bank.getStockPortfolio();
          for(double value : stockInvestments)
             ret.set(index++, value);
       }
       return ret;
   }
   
   public int getBanksStockPortfoliosArrayLengthForRecorderSources(){
     return getNumberOfBanks() * (
        getNumberOfFirms() + population.getAgentsOfType(CommercialBank.class).size());
   }
   
   @RecorderSource("aggregateBankAssets")
   public double getAggregateBankAssets() {
     double ret = 0;
     for(final Bank bank : population.getAgentsOfType(CommercialBank.class))
        ret += bank.getTotalAssets();
     return ret;
   }
   
   public double getAggregateBankEquity() {
      double ret = 0;
      for(final Bank bank : population.getAgentsOfType(CommercialBank.class))
         ret += bank.getEquity();
      return ret;
   }

   public double getAggregateBankCashReserves() {
     double ret = 0;
     for(int i = 0; i < getBankCashOverTime().size(); i++)
        ret += getBankCashOverTime().get(i);
     return ret;
   }
   
   @RecorderSource("aggregateBankLeverage")
   public double getAggregateBankLeverage() {
     return aggreateBankRiskyAssetsAfterPendingDividend / aggreateBankEquityAfterPendingDividend;
   }

   @RecorderSource(
         value = "bankCapitalAdequacyRatio",
         collectionLengthMember="getNumberOfCommercialBanksForRecorderSampling()"
         )
   public List<Double> getBankCapitalAdequacyRatioOverTime() {
      final List<Double>
      ret = Doubles.asList(
            new double[getNumberOfCommercialBanksForRecorderSampling()]);
      int index = 0;
      for(final Bank bank : population.getAgentsOfType(CommercialBank.class)) {
         double CAR = bank.accept(new ComputeCapitalAdequacyRatioOperation(
               new ComputeRiskWeightedAssetsOperation()));
         ret.set(index++, CAR); 
      }

      return ret;
   }
   
   @RecorderSource(
         value = "bankRiskWeightedAssets",
         collectionLengthMember="getNumberOfCommercialBanksForRecorderSampling()"
         )
   public List<Double> getBankRiskWeightedAssetsOverTime() {
      final List<Double>
      ret = Doubles.asList(
            new double[getNumberOfCommercialBanksForRecorderSampling()]);
      int index = 0;
      for(final Bank bank : population.getAgentsOfType(CommercialBank.class)) {
         double RWA = bank.accept(new ComputeRiskWeightedAssetsOperation());
         ret.set(index++, RWA); 
      }

      return ret;
   }
   
   @RecorderSource(
      value = "bankDepositAccountHoldings",
      collectionLengthMember="getNumberOfCommercialBanksForRecorderSampling()"
      )
   public List<Double> getBankDepositsOverTime() {
       final List<Double>
          ret = Doubles.asList(
             new double[getNumberOfCommercialBanksForRecorderSampling()]);
       int index = 0;
       for(final Bank bank : population.getAgentsOfType(CommercialBank.class)) {
          double sumDepositValue = 0.0;
          for (final DepositAccount deposit : bank.getLiabilitiesDeposits())
             sumDepositValue += deposit.getValue();
          ret.set(index++, sumDepositValue); 
       }
       
       return ret;
   }
   
   @RecorderSource(
         value = "householdEquity",
         collectionLengthMember = "getNumberOfHouseholdsForRecorderSampling()"
         )
   public List<Double> getHouseholdEquity() {
      final List<Double>
         ret = Doubles.asList(new double[getNumberOfHouseholdsForRecorderSampling()]);
      int index = 0;
      for(final Household household :
         population.getAgentsOfType(Household.class))
         ret.set(index++, household.getEquity() );
      return ret;
   }
   
   @RecorderSource(
      value = "householdSavings",
      collectionLengthMember = "getNumberOfHouseholdsForRecorderSampling()"
      )
   public List<Double> getHouseholdSavingsOverTime() {
      final List<Double>
         ret = Doubles.asList(new double[getNumberOfHouseholdsForRecorderSampling()]);
      int index = 0;
      for(final DepositingHousehold household :
         population.getAgentsOfType(DepositingHousehold.class))
         ret.set(index++, household.getDepositValue() );
      return ret;
   }
   
   /**
     * XXX
     * 
     * Incompatible with interchangeable {@link Household} {@link Agent}{@code s}.
     * Result will be {@code zero} for non-{@link MacroHousehold} {@link Agent}{@code s}.
     */
   @RecorderSource(
      value = "householdWelfareAllowance",
      collectionLengthMember = "getNumberOfHouseholdsForRecorderSampling()"
      )
   public List<Double> getHouseholdWelfareOverTime() {
      final List<Double>
         ret = Doubles.asList(new double[getNumberOfHouseholdsForRecorderSampling()]);
      int index = 0;
      for(final MacroHousehold household :
         population.getAgentsOfType(MacroHousehold.class))
         ret.set(index++, household.getWelfareAllowance() );
      return ret;
   }

   @RecorderSource(
         value = "firmDividend",
         collectionLengthMember = "getNumberOfFirmsForRecorderSampling()"
         )                                                  // TODO: is this redundant?
      public List<Double> getFirmDividendOverTime() {
          final List<Double>
             dividends = Doubles.asList(new double[getNumberOfFirmsForRecorderSampling()]);
          int index = 0;
          for(final StockReleasingFirm firm : population.getAgentsOfType(StockReleasingFirm.class))
             dividends.set(index++, firm.getCurrentDividend());
          return dividends;
      }
   
   @RecorderSource(
         value = "firmLoansPerEquity",
         collectionLengthMember = "getNumberOfFirmsForRecorderSampling()"
         )                                                  // TODO: is this redundant?
      public List<Double> getFirmLoansPerEquityOverTime() {
          final List<Double>
             ret = Doubles.asList(new double[getNumberOfFirmsForRecorderSampling()]);
          int index = 0;
          for(final LoanStrategyFirm firm :
             population.getAgentsOfType(LoanStrategyFirm.class))
             ret.set(index++, firm.getLoansPerEquity());
          return ret;
      }

   @RecorderSource(
      value = "firmProduction",
      collectionLengthMember = "getNumberOfFirmsForRecorderSampling()"
      )                                                  // TODO: is this redundant?
   public List<Double> getFirmProductionOverTime() {
       final List<Double>
          profit = Doubles.asList(new double[getNumberOfFirmsForRecorderSampling()]);
       int index = 0;
       for(final LoanStrategyFirm firm :
          population.getAgentsOfType(LoanStrategyFirm.class))
          profit.set(index++, firm.getProduction());
       return profit;
   }
   
   @RecorderSource(
         value = "firmRevenue",
         collectionLengthMember = "getNumberOfFirmsForRecorderSampling()"
         )                                                  // TODO: is this redundant?
      public List<Double> getFirmRevenueOverTime() {
          final List<Double>
             ret = Doubles.asList(new double[getNumberOfFirmsForRecorderSampling()]);
          int index = 0;
          for(final LoanStrategyFirm firm :
             population.getAgentsOfType(LoanStrategyFirm.class))
             ret.set(index++, firm.getRevenue());
          return ret;
      }

   /*
    * Also note there is another RecorderSource for interest rates - see MarketClearingModel.java
    * for getInterestRate() method
    */
   @RecorderSource("loanInterestRates")
   public List<Double> getInterestRates(){
       List<Double> result = new ArrayList<Double>();
       for (Object bankObject : getBanks()){
           Bank bank = (Bank) bankObject;
           
           List<Loan> loans = bank.getAssetLoans();
           for (Loan loan : loans) {
               result.add(loan.getInterestRate());
           }
       }
       return result;
   }
   
   /*
    *  Also note there is another RecorderSource for interest rates - see MarketClearingModel.java 
    *  for getInterestRate() method.
    *  
    *  XXX
    *  
    *  This method will not give current mean loan interest rate when the maturity of 
    *  loans is greater than one time-step!
    */
   @RecorderSource("meanLoanInterestRate")
   public double getMeanLoanInterestRate(){
      double result = 0,
         totalVolume = 0;
      for(final Bank bank : population.getAgentsOfType(Bank.class)) {
         final List<Loan> loans = bank.getAssetLoans();
         for(final Loan loan : loans) {
            double volume = loan.getLoanPrincipalValue();
               result += loan.getInterestRate() * volume;
               totalVolume += volume;
           }
      }
      result /= totalVolume;
      return result;
   }
   
   @Override
   public List<Bank> getBanks() {
      return population.getAgentsOfType(Bank.class);
   }
   
   @Override
   public List<Bank> getRegularBanks() {
      return ArrayUtil.castList(population.getAgentsOfType(CommercialBank.class));
   }
   
   @Override
   public List<Firm> getFirms() {
      return population.getAgentsOfType(Firm.class);
   }
   
   @Override
   public List<Fund> getFunds() {
      return population.getAgentsOfType(Fund.class);
   }
   
   @Override
   public Government getGovernment() {
      final List<Government>
         result = population.getAgentsOfType(Government.class);
      return result.isEmpty() ? null : result.get(0);
   }
   
   @Override
   public CentralBank getCentralBank() {
      final List<CentralBank>
         result = population.getAgentsOfType(CentralBank.class);
      return result.isEmpty() ? null : result.get(0);
   }
   
   @Override
   public BadBank getBadBank() {
      final List<BadBank>
         result = population.getAgentsOfType(BadBank.class);
      return result.isEmpty() ? null : result.get(0);
   }
   
   @Override
   public ClearingHouse getClearingHouse() {
      final List<ClearingHouse>
         result = population.getMarketsOfType(ClearingHouse.class);
      return result.isEmpty() ? null : result.get(0);
   }
}
