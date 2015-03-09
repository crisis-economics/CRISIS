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
package eu.crisis_economics.abm.bank.central;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import sim.util.Bag;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.bank.strategies.clearing.ClearingBankStrategy;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.loans.RepoLoan;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.simulation.injection.CollectionProvider;

public final class CentralBankStrategy 
   extends EmptyBankStrategy implements ClearingBankStrategy {
   
   public final static boolean
      DEFAULT_DO_ENABLE_CENTRAL_BANK_TAYLOR_RULE = true,
      DEFAULT_DO_ENABLE_OUTPUT_TRACKING = true;
   
   public final static double
      DEFAULT_INITIAL_INTEREST_RATE_TARGET = 0.0002,
      DEFAULT_INITIAL_INTEREST_RATE_SPREAD = 0.00005,
      DEFAULT_OUTPUT_RESPONSE_PARAMETER = .5;
   
   public final static int
      DEFAULT_MOVING_AVERAGE_MEMORY = 7;
   
   private boolean
      taylorRuleEnabled;
   
   private double
      interestRateTarget,
      interestRateSpread,
      interestRate;
   
   private final int
      movingAverageMemory;
   
   private final boolean
      doTrackOutput;
   private final double
      outputResponseParameter;
   
   private List<Double>
      outputSeries = new ArrayList<Double>();
   
   private List<LoanStrategyFirm>
      firms = new ArrayList<LoanStrategyFirm>();
   
   private Bag
      interventions = new Bag();
   
   private List<String>
      collateralRegister = new ArrayList<String>();
   
   /**
     * Create a {@link CentralBankStrategy} strategy algorithm with custom
     * parameters.
     *
     * @param centralBank
     *        The {@link CentralBank} to which this strategy applies. It is 
     *        expected that this object should be an instance of {@link CentralBank}.
     * @param doEnableTaylorRule
     *        Whether or not to enable a Taylor rule
     * @param initialInterestRate
     *        The initial {@link CentralBank} interest rate
     * @param initialInterestRateSpread
     *        (Half of) the initial spread between the refinancing rate and the
     *        overnight deposit rate. 
     * @param movingAverageMemory
     *        The length of the moving average timewindow to be used
     * @param doEnableOutputTracking
     * @param outputResponseParameter
     */
   @Inject
   public CentralBankStrategy(
   @Assisted
      final ClearingBank centralBank,
   @Named("CENTRAL_BANK_STRATEGY_DO_ENABLE_TAYLOR_RULE")
      final boolean doEnableTaylorRule,
   @Named("CENTRAL_BANK_STRATEGY_INITIAL_INTEREST_RATE")
      final double initialInterestRate,
   @Named("CENTRAL_BANK_STRATEGY_INITIAL_INTEREST_RATE_SPREAD")
      final double initialInterestRateSpread,
   @Named("CENTRAL_BANK_STRATEGY_MOVING_AVERAGE_MEMORY")
      final int movingAverageMemory,
   @Named("CENTRAL_BANK_STRATEGY_DO_ENABLE_OUTPUT_TRACKING")
      final boolean doEnableOutputTracking,
   @Named("CENTRAL_BANK_STRATEGY_OUTPUT_RESPONSE_PARAMETER")
      final double outputResponseParameter
      ) {
      super(centralBank);
      this.taylorRuleEnabled = DEFAULT_DO_ENABLE_CENTRAL_BANK_TAYLOR_RULE;
      this.interestRateTarget = DEFAULT_INITIAL_INTEREST_RATE_TARGET;
      this.interestRateSpread = DEFAULT_INITIAL_INTEREST_RATE_SPREAD;
      this.movingAverageMemory = DEFAULT_MOVING_AVERAGE_MEMORY;
      this.doTrackOutput = DEFAULT_DO_ENABLE_OUTPUT_TRACKING;
      this.outputResponseParameter = DEFAULT_OUTPUT_RESPONSE_PARAMETER;
      this.interestRate = interestRateTarget;
   }
   
   /**
     * Create a {@link CentralBankStrategy} strategy algorithm with default
     * parameters.
     */
   public CentralBankStrategy(final CentralBank centralBank) {
      this(
         centralBank,
         DEFAULT_DO_ENABLE_CENTRAL_BANK_TAYLOR_RULE,
         DEFAULT_INITIAL_INTEREST_RATE_TARGET,
         DEFAULT_INITIAL_INTEREST_RATE_SPREAD,
         DEFAULT_MOVING_AVERAGE_MEMORY,
         DEFAULT_DO_ENABLE_OUTPUT_TRACKING,
         DEFAULT_OUTPUT_RESPONSE_PARAMETER
         );
   }
   
   @Override
   public void considerCommercialLoanMarkets() {
      this.setCollateralRegister(new ArrayList<String>());
      this.setCrisisInterventions(new Bag());
   }
   
   @Override
   public void considerInterbankMarkets() {
      getBank().getInterbankStrategy().considerInterbankMarkets();
      
      // update interest rate
      double y = 0;
      for (int i = 0; i < firms.size(); i++) {
         y += ((LoanStrategyFirm) firms.get(i)).getProduction();
      }
      this.outputSeries.add(y);
      int T = this.outputSeries.size();
      int window = 20;
      
      if (taylorRuleEnabled) {
         if (T >= window + 20) {
            final double alpha = 2. / (this.movingAverageMemory + 1.);
            double qCurrent = this.outputSeries.get(T - window);
            double qAvg = 0;
            this.interestRate = this.interestRateTarget;
            for (int i = T - window; i < T; i++) {
               qAvg += this.outputSeries.get(i);
               qCurrent = alpha * this.outputSeries.get(i) + (1. - alpha) * qCurrent;
            }
            qAvg /= window;
            
            if(doTrackOutput) {
               interestRate += this.outputResponseParameter
                     * Math.log(Math.max(qCurrent, 1.e-10)
                           / Math.max(qAvg, 1.e-10));
            }
            if ((this.interestRate - this.interestRateSpread) < 0.001) {
               interestRate = 0.001 + this.interestRateSpread;
            }
         }
         interestRate = Math.max(interestRate, 0.);
      } else
         interestRate = interestRateTarget;
   }
   
   public void addFirms(final List<LoanStrategyFirm> agents) {
      firms.addAll(agents);
   }
   
   public boolean getTrackOutput() {
      return this.doTrackOutput;
   }
   
   public double getOutputResponseParameter() {
      return this.outputResponseParameter;
   }
   
   public void setCollateralRegister(List<String> collReg) {
      this.collateralRegister = collReg;
   }
   
   public List<String> getCollateralRegister() {
      return this.collateralRegister;
   }
   
   public double getInflationTarget() {
      return interestRateTarget;
   }
   
   public void setInflationTarget(double interestRateTarget) {
      this.interestRateTarget = interestRateTarget;
   }
   
   public double getRefinancingRate() {
      return this.interestRate + this.interestRateSpread;
   }
   
   public double getOvernightDepositRate() {
      return this.interestRate - this.interestRateSpread;
   }
   
   public double getOvernightDepositVolume(final StrategyBank bank) {
      double totalDeposits = 0.0;
      for (final Contract contract : bank.getLiabilities()) {
         if (contract instanceof Loan) {
            totalDeposits += contract.getValue();
         }
      }
      return totalDeposits;
   }
   
   public double getCBLoanVolumeFor(final StrategyBank bank) {
      double totalAssets = 0.0;
      for (final Contract contract : bank.getAssets()) {
         if (contract instanceof RepoLoan) {
            totalAssets += contract.getValue();
         }
      }
      return totalAssets;
   }
   
   public Bag getCrisisInterventions() {
      return interventions;
   }
   
   public void setCrisisInterventions(Bag iv) {
      this.interventions = iv;
   }
   
   public double getInterestRateSpread() {
      return interestRateSpread;
   }
   
   public void setInterestRateSpread(final double interestRateSpread) {
      this.interestRateSpread = interestRateSpread;
   }
   
   public int getMovingAverageMemory() {
      return movingAverageMemory;
   }
   
   @Override
   public void decidePorfolioSize() {
      // No action.
   }
   
   @Override
   public void decidePortfolioDistribution() {
      // No action.
   }
   
   @Override
   public void decideDividendPayment() {
      // No action.
   }
   
   @Override
   public double getDesiredStockInvestmentIn(
      final ClearingStockMarket market) {
      return 0;
   }
   
   @Override
   public double getDesiredLoanInvestmentIn(
      final ClearingLoanMarket market) {
      return 0.;
   }
   
   @Override
   public void setStocksToInvestIn(
      final CollectionProvider<ClearingStockMarket> markets) {
      // No action.
   }

   @Override
   public void setLoansToInvestIn(
      final CollectionProvider<ClearingLoanMarket> markets) {
      // No action.
   }
}
