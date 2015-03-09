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
import java.util.Collections;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import ai.aitia.crisis.aspectj.Agent;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.events.WagePaymentEvent;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.MacroFirm;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.configuration.ConstantDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.configuration.ConstantTargetLeverageConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.FixedWageAskPriceDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.FixedWageBidPriceAlgorithmConfiguration;
import eu.crisis_economics.abm.model.configuration.FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.configuration.FollowTargetProductionGoodsPricingConfiguration;
import eu.crisis_economics.abm.model.configuration.LabourOnlyProductionFunctionConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.configuration.SimpleThresholdFundInvestmentDecisionRuleConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

/**
  * A workspace for model calibration. This model file provides a numerical 
  * calibration objective function.and a selection of UK economic data measurements
  * with inline discussion.
  * 
  * @author phillips
  */
public class CalibrationModel extends AbstractModel {
   
   /*                                *
    * Real-World Reference Data      *
    *                                */
   
   /**
     * UK data taken sourced from around July 2014, some data may be sourced from as early as
     * 2009. For more information see Excel Spreadsheet 'Summary of UK Targets'
     * 
     * <ul>
     *   <li> The annual UK GDP is approximately £1,650,000,000,000 per year, which 
     *        amounts to ~£4.125e11 GDP per quarter.
     *        
     *   <li> Total gross bank lending to SMEs and large businesses in 2013 is thought 
     *        to be £163 billion (http://www.bankofengland.co.uk/publications/Documents/other
     *        /monetary/trendsoctober14.pdf, p.15). On a quarterly basis this amounts to
     *        ~£40.75 billion per quarter in commercial loans.
     *        
     *   <li> The mean return rate for private non-financial Corporations (PNFC) is 12.6%
     *        per year, which is ~3.01% per quarter. This figure is, however, quite variable
     *        across stock releasing firms: rising to 36% for UK Continental Shelf (UKCS)
     *        industry i.e. oil and gas.
     *        
     *   <li> The total employed workforce in the UK is thought to be 30.61 million people.
     *        A futher 2.02 million people are employable and unemployed. The unemployment
     *        rate is therefor ~6.2% among the economically active population. The mean
     *        total weekly wage payment per employee in 2014 was thought to be £476 total gross
     *        pre-tax pay for private firms (not incuding wage arrears, July 2014).<br><br>
     *        
     *        http://www.ons.gov.uk/ons/rel/lms/labour-market-statistics/september-2014/
     *        statistical-bulletin.html#tab-6--Average-Weekly-Earnings
     *        
     *        This means that the total labour wage payment in the UK is roughly 
     *        £476 * 52.1775 * 30.61e6 = £7.6e11 per year = £1.9006e11 per quarter.
     *        
     *   <li> The UK operating surplus is ~£144bn per year (source: ONS data), so the
     *        quarterly firm profit can be estimated as £144bn/4 ~ £36e9.
     *        
     *   <li> The Sterling Overnight Index Average (SONIA) was 0.47% around 10/07/2014.
     *        We only consider overnight lending in this version of the model so this seems the
     *        most appropriate rate.
     *        
     *   <li> UK stock market capitalization was £3.5tn on CapitalIQ.... £2.1 trillion, or
     *        127% of GDP. This is a stock concept calculated from 127% of GDP flow concept.
     *        Should we rescale to be consistent with daily GDP?
     *        
     *   <li> Of the four UK commercial banks with the largest asset holdings, the mean UK
     *        commercial bank equity is £68,458,825,000.
     * </ul>
     */
   static final double
      UK_GDP_PER_QUARTER                   = 4.125e11, 
      UK_AVERAGE_FIRM_DIVIDEND_PRICE_RATIO = 3.01 / 100.,
      UK_TOTAL_QUARTERLY_WAGE_PAYMENTS     = 7.6e11 / 4.,
      UK_FIRMS_TOTAL_OPERATING_SURPLUS     = 144.0e9 / 4.,
      UK_OVERNIGHT_UNSECURED_LOAN_RATE     = 0.005,
      UK_STOCK_MARKET_CAPITALIZATION       = 3.5e12,
      UK_TOTAL_BANK_CREDIT                 = 163.0e9 / 4.,
      UK_TOTAL_BANK_DEPOSITS               = 2.6e12,
      UK_UNEMPLOYMENT_PERCENTAGE           = 6.2,
      UK_AVERAGE_BANK_LEVERAGE             = 18.0,
      UK_AVERAGE_BANK_EQUITY               = 6.8458825e10;
   
   static final int
      SIMULATION_NUMBER_OF_HOUSEHOLDS      = 1000;
   static final double
      SIMULATION_LABOUR_PER_HOUSEHOLD      = 1.0,
      SIMULATION_MUTUAL_FUND_STOCK_CAPITAL = 2.88e12;
   
   private CalibrationObjectives
      calibrationObjectives;
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public CalibrationModel() {
      this(1L);
   }
   
   public CalibrationModel(long seed) {
      super(seed);
   }
   
   @Override
   protected AbstractPublicConfiguration getAgentBindings() {
      
      ModelUtils.apply(
         agentsConfiguration, "BANK_EQUITY_TARGET", UK_AVERAGE_BANK_EQUITY, false);
      
      ModelUtils.apply(
         agentsConfiguration, "BANK_LEVERAGE_TARGET_ALGORITHM", 
         new ConstantTargetLeverageConfiguration(UK_AVERAGE_BANK_LEVERAGE), true);
      
      ModelUtils.apply(
         agentsConfiguration, "BANK_STRATEGY_STOCK_RISK_PREMIUM", 1.0, false);
      
      ModelUtils.apply(
         agentsConfiguration, "DO_BANKS_BUY_BANK_STOCKS", false, true);
      
      ModelUtils.apply(
         agentsConfiguration, "NUMBER_OF_COMMERCIAL_BANKS", 5, false);
      
      ModelUtils.apply(
         agentsConfiguration, "INITIAL_STOCK_EMISSION_VALUE_PER_BANK",
         UK_AVERAGE_BANK_EQUITY, false);
      
      // Funds
      ModelUtils.apply(
         agentsConfiguration, "NUMBER_OF_FUNDS", 10, false);
      
      ModelUtils.apply(
         agentsConfiguration, "NUMBER_OF_FUNDS_ARE_NOISE_TRADERS", 0, false);
      
      ModelUtils.apply(
         agentsConfiguration, "HOUSEHOLD_FUND_CONTRIBUTION_RULE",
         new SimpleThresholdFundInvestmentDecisionRuleConfiguration(
            SIMULATION_MUTUAL_FUND_STOCK_CAPITAL / 1000., 1.0, 0.9), true);
      
      // No IO network.
      ModelUtils.apply(
         agentsConfiguration, "FIRM_PRODUCTION_FUNCTION",
         new LabourOnlyProductionFunctionConfiguration(
            new ConstantDoubleModelParameterConfiguration(1.0)), true);
      
      /**
        * Firm liquidity target:
        * 
        * For a labour-only firm production function (yield) ~ TFP * L**a
        * where TFP is total factor productivity, L is labour input and a
        * is the labour productivity exponent (say 1.0) then:
        * 
        * (commercial loan credit) + (firm liquidity after dividends, LT) ~ w * L
        * 
        * Where w is wage per unit labour employed. So:
        * 
        * (total commercial loan credit) / Nf + LT ~ w * L.
        */
      final int
         Nf = 54;
      final double
         firmLiquidityTarget = (UK_TOTAL_QUARTERLY_WAGE_PAYMENTS - UK_TOTAL_BANK_CREDIT) / Nf;
//         expectedCommercialLoansPerFirm = UK_TOTAL_BANK_CREDIT / 54.,
//         expectedProfitPerFirm = UK_FIRMS_TOTAL_OPERATING_SURPLUS / 54.;
      
      /**
        * Selling price (p):
        * 
        * The GDP measurement for a fixed point IO economy is approximately:
        * GDP = Nf * p * (1 + q * Nf) - Nf * q * p Nf = Nf * p, assuming (a) one
        * unit of goods sold per firm to domestic consumption and (b) a homogeneous
        * IO netowrk. So GDP ~ £4.125e11 = 54. * p and p ~ GDP / Nf.
        */
      final double
         goodsPrice = UK_GDP_PER_QUARTER / 54.;
      
      /**
        * Wage per unit labour (w)
        * 
        * The labour supply, the employment percentage and total quarterly wage
        * payments are known, so the wage per unit labour w is constrained.
        */
      final double
         wagePerUnitLabour =
            UK_TOTAL_QUARTERLY_WAGE_PAYMENTS /
            (1000 * (1. - UK_UNEMPLOYMENT_PERCENTAGE * 1.e-2));
      
      /**
        * Firm liquidity targets (L):
        * 
        * The firm profit (P) is approximately:
        * P = p * (1 + Nf * q) - (existing liquidity, LT) - credit * (1 + r)
        * 
        * and for a fixed point CD-IO economy the cost of production is approximately:
        * LT + loans = w * L + Nf * q * p 
        * 
        * Where w is wage per unit labour, L is labour to employ, Nf is the number of
        * firms, p is the selling price per unit goods and q is the size of the input
        * goods order (number of units of goods) from each sector. w, L, p and Nf are
        * known.
        * 
        * Altogether the quarterly interest rate payable is:
        * 
        * r ~ [GDP - (firm operating surplus) - (total labour wages)] / (total bank credit)
        */
//      final double
//         rCommercialLoan = (UK_GDP_PER_QUARTER - UK_FIRMS_TOTAL_OPERATING_SURPLUS - 
//             UK_TOTAL_QUARTERLY_WAGE_PAYMENTS) / (UK_TOTAL_BANK_CREDIT);
      
      ModelUtils.apply(
         agentsConfiguration, "FIRM_PRODUCTION_FUNCTION",
         new LabourOnlyProductionFunctionConfiguration(
            new ConstantDoubleModelParameterConfiguration(1.0)), true);
      
      ModelUtils.apply(
         agentsConfiguration, "FIRM_PRODUCTION_GOODS_PRICING_RULE",
         new FollowTargetProductionGoodsPricingConfiguration(goodsPrice, 1./4.), true
         );
      
      ModelUtils.apply(
         agentsConfiguration, "FIRM_TARGET_PRODUCTION_RULE",
         new FollowAggregateMarketDemandTargetProductionDecisionRuleConfiguration(
            1.0, 0.03, 0.05), true);
      
      ModelUtils.apply(
         agentsConfiguration, "FIRM_LABOUR_WAGE_BID_RULE",
         new FixedWageBidPriceAlgorithmConfiguration(wagePerUnitLabour), true);
      
      final double
         firmTFPFactor = 54. / ((1000. * (1. - UK_UNEMPLOYMENT_PERCENTAGE * 1.e-2)));
      
      ModelUtils.apply(
         agentsConfiguration, "LABOUR_ONLY_PRODUCTION_FUNCTION_TFP",
         new ConstantDoubleModelParameterConfiguration(firmTFPFactor), true);
      
      final double
         initialFirmStockEmissionValue = UK_STOCK_MARKET_CAPITALIZATION / 54.;
      
      ModelUtils.apply(
         agentsConfiguration, "FIRM_INITIAL_PRICE_PER_SHARE",
         new ConstantDoubleModelParameterConfiguration(initialFirmStockEmissionValue), false);
      
      ModelUtils.apply(
         agentsConfiguration, "DEFAULT_INITIAL_NUMBER_OF_FIRM_SHARES", 1.0, false);
      
      // ~ £3 of every £4 used for production is to be sourced from commercial loans.
      ModelUtils.apply(
         agentsConfiguration, "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_STARTING_LIQUIDITY_TARGET",
         firmLiquidityTarget, false
         );
      
      ModelUtils.apply(
         agentsConfiguration, "TRANSIENT_BUFFER_LIQUIDITY_TARGET_RULE_FINAL_LIQUIDITY_TARGET",
         firmLiquidityTarget, false
         );
      
      ModelUtils.apply(
         agentsConfiguration, "INDIFFERENCE_THRESHOLD_PROPORTION_OF_MARKUP",
         1./3., false);
      
      // One unit of household labour per cycle
      ModelUtils.apply(
         agentsConfiguration, "HOUSEHOLD_LABOUR_TO_OFFER",
         new ConstantDoubleModelParameterConfiguration(1.0), true
         );
      
      ModelUtils.apply(
         agentsConfiguration, "NUMBER_OF_HOUSEHOLDS", 1000, false);
      
      ModelUtils.apply(
         agentsConfiguration, "HOUSEHOLD_LABOUR_WAGE_ASK_RULE",
         new FixedWageAskPriceDecisionRuleConfiguration(wagePerUnitLabour), true);
      
      // Disable government and interbank trading to simplify the economy:
      ModelUtils.apply(
         agentsConfiguration, "DO_ENABLE_GOVERNMENT", false, false);
      
      ModelUtils.apply(
         agentsConfiguration, "IS_INTERBANK_MARKET_ENABLED", false, false);
      
      return agentsConfiguration;
   }
   
   @Override
   protected AbstractPlumbingConfiguration getPlumbing() {
      return plumbingConfiguration;
   }
   
   private class CalibrationObjectives {
      public double getUKdividendPriceRatioError() {
         final List<StockReleasingFirm>
            firms = CalibrationModel.this.getPopulation().getAgentsOfType(StockReleasingFirm.class);
         double
            result = 0.0;
         for(final StockReleasingFirm firm : firms)
            result += firm.getDividendPerShare() / UniqueStockExchange.Instance.getStockPrice(firm);
         result /= (firms.size() == 0 ? 1. : firms.size());
         System.out.printf(
            "Dividend Price Ratio:    desired: %16.10g    observed: %16.10g\n",
            UK_AVERAGE_FIRM_DIVIDEND_PRICE_RATIO, result);
         return
            (result - UK_AVERAGE_FIRM_DIVIDEND_PRICE_RATIO) / UK_AVERAGE_FIRM_DIVIDEND_PRICE_RATIO;
      }
      
      public double getUKwageError() {
         System.out.printf(
            "Labour Wages Paid:       desired: %16.10g    observed: %16.10g\n",
            UK_TOTAL_QUARTERLY_WAGE_PAYMENTS, wagesPaidByEmployers);
         return
            (wagesPaidByEmployers - UK_TOTAL_QUARTERLY_WAGE_PAYMENTS) /
            UK_TOTAL_QUARTERLY_WAGE_PAYMENTS;
      }
      
      /**
        * XXX
        * 
        * Unfortunately due to API limitations, this method will return meaningless
        * values for non-{@link MacroFirm} {@link Firm} {@link Agent} implementations.
        */
      public double getUKfirmProfitError() {
         final List<MacroFirm>
            firms = CalibrationModel.this.getPopulation().getAgentsOfType(MacroFirm.class);
         double
            result = 0.0;
         for(final MacroFirm firm : firms)
            result += firm.getProfit();
         System.out.printf(
            "Firm Profits:            desired: %16.10g    observed: %16.10g\n",
            UK_FIRMS_TOTAL_OPERATING_SURPLUS, result);
         return
            (result - UK_FIRMS_TOTAL_OPERATING_SURPLUS) / UK_FIRMS_TOTAL_OPERATING_SURPLUS;
      }
      
      private double
         gdpMeasurement;
      
      @SuppressWarnings("unused")   // Scheduled
      private void collectGDPMeasurement() {
         this.gdpMeasurement = CalibrationModel.this.getGDP();
      }
      
      public double getUKgdpError() {
         System.out.printf(
            "GDP:                     desired: %16.10g    observed: %16.10g\n",
            UK_GDP_PER_QUARTER, gdpMeasurement);
         return
            (gdpMeasurement - UK_GDP_PER_QUARTER) / UK_GDP_PER_QUARTER;
      }
      
      public double getUKaggBankLeverageError() {
         final double
            result = CalibrationModel.this.getAggregateBankLeverage();
         System.out.printf(
            "Aggregate Bank Leverage: desired: %16.10g    observed: %16.10g\n",
            UK_AVERAGE_BANK_LEVERAGE, result);
         return
            (result - UK_AVERAGE_BANK_LEVERAGE) / UK_AVERAGE_BANK_LEVERAGE;
      }
      
      /**
        * TODO:
        * 
        * Is the intention to calibrate the mean UK commercial loan interest rate or
        * the commercial loan interest rate including interbank loan assets?
        */
      public double getUKmeanLoanInterestRateError() {
         return 0.;
      }
      
      /**
        * TODO:
        * 
        * The FTSE1000 array contains HSBC, Lloyds Group and other large UK banks.
        * These agents should be included in the stock market capitalization
        * measurement.
        */
      public double getUKstockMarketCapitalizationError() {
         final List<StockReleasingFirm>
            firms = CalibrationModel.this.getPopulation().getAgentsOfType(StockReleasingFirm.class);
         double
            result = 0.0;
         for(final StockReleasingFirm firm : firms)
            result += UniqueStockExchange.Instance.getStockPrice(firm) * 
               UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(firm);
         System.out.printf(
            "Stock Market Capital:    desired: %16.10g    observed: %16.10g\n",
            UK_STOCK_MARKET_CAPITALIZATION, result);
         return
            (result - UK_STOCK_MARKET_CAPITALIZATION) / UK_STOCK_MARKET_CAPITALIZATION;
      }
      
      public double getUKmeanCommercialBankEquity() {
         final List<CommercialBank>
            banks = CalibrationModel.this.getPopulation().getAgentsOfType(CommercialBank.class);
         double
            result = 0.0;
         for(final CommercialBank bank : banks)
            result += bank.getEquity();
         result /= (banks.size() == 0 ? 1.0 : banks.size());
         System.out.printf(
            "Commercial Bank Equity:  desired: %16.10g    observed: %16.10g\n",
            UK_AVERAGE_BANK_EQUITY, result);
         return
            (result - UK_AVERAGE_BANK_EQUITY) / UK_AVERAGE_BANK_EQUITY;
      }
      
      public double getUKtotalBankCreditError() {
         final List<CommercialBank>
            banks = CalibrationModel.this.getPopulation().getAgentsOfType(CommercialBank.class);
         double
            result = 0.0;
         for(final CommercialBank bank : banks)
            result += bank.getCommercialLoans();
         System.out.printf(
            "Commercial Loan Volume:  desired: %16.10g    observed: %16.10g\n",
            UK_TOTAL_BANK_CREDIT, result);
         return
            (result - UK_TOTAL_BANK_CREDIT) / UK_TOTAL_BANK_CREDIT;
      }
      
      public double getUKtotalBankDepositsError() {
         final List<CommercialBank>
            banks = CalibrationModel.this.getPopulation().getAgentsOfType(CommercialBank.class);
         double
            result = 0.0;
         for(final CommercialBank bank : banks)
            for(final DepositAccount depositAccount : bank.getLiabilitiesDeposits())
               result += depositAccount.getValue();
         System.out.printf(
            "Total Bank Deposits:     desired: %16.10g    observed: %16.10g\n",
            UK_TOTAL_BANK_DEPOSITS, result);
         return
            (result - UK_TOTAL_BANK_DEPOSITS) / UK_TOTAL_BANK_DEPOSITS;
      }
      
      public double getUKunemploymentPercentageError() {
         final double
            result = CalibrationModel.this.getUnemploymentPercentage();
         System.out.printf(
            "Unemployment Percentage: desired: %16.10g    observed: %16.10g\n",
            UK_UNEMPLOYMENT_PERCENTAGE, result);
         return
            (result - UK_UNEMPLOYMENT_PERCENTAGE) / UK_UNEMPLOYMENT_PERCENTAGE;
      }
      
      /**
        * TODO
        */
      public double getUKrelativeChangeInMeanLoanInterestRateError() {
         return 0.0;
      }
      
      /**
        * TODO
        */
      public double getUKrelativeChangeInStockMarketCapitalizationError() {
         return 0.0;
      }
      
      public List<Double> computeErrors() {
         final List<Double>
            errors = new ArrayList<Double>();
         
         errors.add(this.getUKdividendPriceRatioError());
         errors.add(this.getUKwageError());
         errors.add(this.getUKfirmProfitError());
         errors.add(this.getUKgdpError());
         errors.add(this.getUKaggBankLeverageError());
         errors.add(this.getUKmeanLoanInterestRateError());
         errors.add(this.getUKstockMarketCapitalizationError());
         errors.add(this.getUKtotalBankCreditError());
         errors.add(this.getUKtotalBankDepositsError());
         errors.add(this.getUKunemploymentPercentageError());
         errors.add(this.getUKmeanCommercialBankEquity());
         
         errors.add(this.getUKrelativeChangeInMeanLoanInterestRateError());
         errors.add(this.getUKrelativeChangeInStockMarketCapitalizationError());
         
         return Collections.unmodifiableList(errors);
      }
      
      /**
        * Create a {@link CalibrationObjectives} object. This object requires a 
        * running {@link Simulation} model.
        */
      CalibrationObjectives() {
         Simulation.repeat(this, "collectGDPMeasurement",
            CustomSimulationCycleOrdering.create(NamedEventOrderings.FIRM_ACCOUNTING, -1));
      }
      
      /**
        * Get the complete error measurement (a {@link Double}) for this objective function.
        */
      public double getError() {
         double
            sumOfSquares = 0.0;
         System.out.printf(
            "**********************************************************************************\n"  
          + "Computing calibration objective:\n"
            );
         final List<Double>
            errors = computeErrors();
         for(final double error : errors)
            sumOfSquares += error * error;
         final double
            result = sumOfSquares/errors.size();
         System.out.printf(
            "Result: %16.10g\n"
          + "**********************************************************************************\n",
            result
            );
         return sumOfSquares/errors.size();
      }
   }
   
   /**
     * Compute the calibration error for the running {@link Simulation}.
     */
   @SuppressWarnings("unused")   // Scheduled
   private double processCalibrationObjective(){ 
      final double
         objectiveError = calibrationObjectives.getError();
      
      this.wagesPaidByEmployers = 0.0;
      
      return objectiveError;
   }
   
   @Override
   public void start() {
      super.start();
      
      this.calibrationObjectives = new CalibrationObjectives();
      
      // Compute the calibration objective error once per simulation cycle:
      Simulation.repeat(this, "processCalibrationObjective",
         CustomSimulationCycleOrdering.create(NamedEventOrderings.AFTER_ALL, 100));
      
      // Listen for wage payment events:
      Simulation.events().register(this);
   }
   
   private double
      wagesPaidByEmployers;
   
   /**
     * A {@link WagePaymentEvent} {@link Event} subscription. This subscription tallies the
     * total {@link Labour} wage payment made each {@link Simulation} cycle.
     */
   @Subscribe
   public void listenForWagePaymentEvents(final WagePaymentEvent event) {
      wagesPaidByEmployers += event.getCashFlow();
   }
   
   /**
     * Entry Point
     */
   public static void main(final String[] argv) {
      doLoop(CalibrationModel.class, argv);
      System.out.println("CRISIS Calibration Model");
      System.exit(0);
   }
   
   private static final long serialVersionUID = -3582148240143753851L;
}
