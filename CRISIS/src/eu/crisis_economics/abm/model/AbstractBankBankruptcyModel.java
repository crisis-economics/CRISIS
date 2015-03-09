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

import java.util.List;

import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.model.configuration.AbstractPublicConfiguration;
import eu.crisis_economics.abm.model.configuration.FinancialSystemWithMacroEconomyConfiguration;
import eu.crisis_economics.abm.model.configuration.MasterModelConfiguration;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import eu.crisis_economics.abm.model.plumbing.AgentGroup;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.Simulation;
import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

/**
  * A model with a manual, artifical {@link Bank} bankruptcy event. This model creates
  * a standard master model and supplements the natural evolution of this simulation by
  * manually producing a financial crisis. The method used to produce this financial
  * crisis (for example a commercial loan default episode or a stock market shock) is
  * customizable. Implementations of this class specify which bankruptcy resolution
  * technique should be used (eg. bailout, bailin or another scheme).
  * 
  * @author phillips
  */
public abstract class AbstractBankBankruptcyModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public AbstractBankBankruptcyModel() {
      this(1L);
   }
   
   /**
     * A lightweight configuration component for manual {@link Bank} bankruptcy events.
     * 
     * @author phillips
     */
   @ConfigurationComponent(
      DisplayName = "Perform a"
      )
   static abstract class AbstractBankBankruptcyOperation {
      
      private final static int
         DEFAULT_TIME_TO_APPLY = 200;
      
      @Layout(
         Order = 0.,
         FieldName = "Time To Apply"
         )
      private int
         timeToApply = DEFAULT_TIME_TO_APPLY;
      
      public int getTimeToApply() {
         return timeToApply;
      }
      
      /**
        * Set the simulation time to apply the {@link Bank} bankruptcy event.
        */
      public void setTimeToApply(final int timeToApply) {
         this.timeToApply = timeToApply;
      }
      
      public abstract void performIntervention(AgentGroup population);
      
      @ConfigurationComponent(
         DisplayName = "Commercial Loan Writedown",
         Description =
            "Induce a bank bankruptcy by writing off a fraction of all commercial loans. This "
          + "scheme will attempt to bankrupt a custom number of banks by removing a fraction of "
          + "all commercial loan assets held by the affected banks. From the perspective of the "
          + "bank, a specific fraction of all loan contract assets held will be instantaneously "
          + "written off. Firms will, at the moment of this intervention, be billed for the "
          + "sum in question. The cash raised by this process is disposed of exogenously."
         )
      public static final class BankruptcyByLoanWriteoffOperation
         extends AbstractBankBankruptcyOperation {
         
         @Layout(
            FieldName = "% Commercial Loans to Remove"
            )
         private double
            percentLoansToWriteDown = 70.;
         @Layout(
            FieldName = "Number of Banks to Harm"
            )
         private int
            numberOfBanksToHarm = 2;
         
         public double getPercentLoansToWriteDown() {
            return percentLoansToWriteDown;
         }
         
         public void setPercentLoansToWriteDown(
            final double value) {
            if(value < 0. || value > 100.)
               throw new IllegalArgumentException(
                  getClass().getSimpleName() + ": loan percent writedown must be in the range "
                + "[0, 100].");
            this.percentLoansToWriteDown = value;
         }
         
         public int getNumberOfBanksToHarm() {
            return numberOfBanksToHarm;
         }
         
         public void setNumberOfBanksToHarm(
            final int value) {
            if(value < 0)
               throw new IllegalArgumentException(
                  getClass().getSimpleName() + ": number of banks to harm must be positive.");
            this.numberOfBanksToHarm = value;
         }
         
         @Override
         public void performIntervention(AgentGroup population) {
            System.out.printf(
               "----------------------------------------------------\n"
             + "Forcibly inducing bank bankruptcy (loan writedown)..\n"
             + numberOfBanksToHarm + " Banks, " + percentLoansToWriteDown + "%% loan writedown..\n"
             + "----------------------------------------------------\n"
               );
            final List<CommercialBank>
               banks = population.getAgentsOfType(CommercialBank.class);
            for(int index = 0; index < numberOfBanksToHarm; ++index) {
               final CommercialBank
                  bankToPenalize = banks.get(index);
               final List<Loan>
                  loans = bankToPenalize.getAssetLoans();
               final double
                  initialBankEquity = bankToPenalize.getEquity(),
                  proportionOfLoansToWriteDown = percentLoansToWriteDown / 100.;
               for(Loan loan : loans) {
                  final Borrower
                     borrower = loan.getBorrower();
                  try {
                     borrower.credit(
                        loan.getNextInstallmentPayment() * proportionOfLoansToWriteDown);
                  } catch (final InsufficientFundsException neverFails) {
                     throw new IllegalStateException();
                  }
                  loan.setValue(loan.getValue() * (1. - proportionOfLoansToWriteDown));
               }
               final double
                  finalBankEquity = bankToPenalize.getEquity();
               System.out.printf(
                  "Bank equity forcibly reduced from %16.10g to %16.10g\n",
                  initialBankEquity,
                  finalBankEquity
                  );
            }
            System.out.printf(
               "------------------------------------\n"
             + "Intervention complete.\n"
             + "------------------------------------\n"
               );
         }
      }
      
      @ConfigurationComponent(
         DisplayName = "Stock Price Shock",
         Description =
            "Induce a bank bankruptcy by artificially introducing a stock market price shock. This "
          + "scheme will attempt to bankrupt all banks by instantaneously and artificially"
          + " reducing the stock price per share for all stock instruments known to the exchange."
         )
      public static final class BankruptcyByStockPriceShockOperation
         extends AbstractBankBankruptcyOperation {
         
         @Layout(
            FieldName = "% Reduction in Stock Prices"
            )
         private double
            percentStockValuesToWriteDown = 90.;
         
         public double getPercentStockValuesToWriteDown() {
            return percentStockValuesToWriteDown;
         }
         
         public void setPercentStockValuesToWriteDown(final double value) {
            if(value > 100. || value < 0.)
               throw new IllegalArgumentException(
                  getClass().getSimpleName() + ": p");
            this.percentStockValuesToWriteDown = value;
         }
         
         @Override
         public void performIntervention(AgentGroup population) {
            System.out.printf(
               "----------------------------------------------------\n"
             + "Forcibly inducing bank bankruptcy (loan writedown)..\n"
             + "Reducing all stock prices by " + percentStockValuesToWriteDown + "%%..\n"
             + "----------------------------------------------------\n"
               );
            for(final StockReleaser releaser : UniqueStockExchange.Instance.getStockReleasers()) {
               UniqueStockExchange.Instance.setStockPrice(
                  releaser.getUniqueName(),
                  UniqueStockExchange.Instance.getStockPrice(releaser.getUniqueName()) *
                     (1. - percentStockValuesToWriteDown / 100.)
                  );
            }
            System.out.printf(
               "------------------------------------\n"
             + "Intervention complete.\n"
             + "------------------------------------\n"
               );
         }
      }
   }
   
   @Layout(
      Title = "Bankruptcy Operation",
      FieldName = "Perform a",
      Order = 1
      )
   @Submodel
   public AbstractBankBankruptcyOperation
      bankruptcyOperation =
         new AbstractBankBankruptcyOperation.BankruptcyByLoanWriteoffOperation();
   
   public final AbstractBankBankruptcyOperation getBankruptcyOperation() {
      return bankruptcyOperation;
   }
   
   public final void setBankruptcyOperation(
      final AbstractBankBankruptcyOperation bankruptcyOperation) {
      this.bankruptcyOperation = bankruptcyOperation;
   }
   
   protected AbstractBankBankruptcyModel(long seed) {
      super(seed);
   }
   
   protected abstract MasterModelConfiguration configureBankruptcyPolicy(
      MasterModelConfiguration agentsConfiguration);
   
   @Override
   protected final AbstractPublicConfiguration getAgentBindings() {
      return configureBankruptcyPolicy(agentsConfiguration);
   }
   
   @Override
   protected final AbstractPlumbingConfiguration getPlumbing() {
      return plumbingConfiguration;
   }
   
   @Override
   public final void start() {
      super.start();
      
      // @Recorder sampling happens at Time mod 1 on the clock (midnight),
      // so manual loan modifications should be sampled as soon as possible
      // after this. However bankruptcies are processed at one minute to
      // midnight, meaning that the intervention should happen as soon as
      // possible before AFTER_ALL in the schedule:
      Simulation.onceCustom(
         this,
         "performIntervention",
         CustomSimulationCycleOrdering.create(
            NamedEventOrderings.INTERBANK_AFTER_MARKET_MATCHING.getUnitIntervalTime() + 1.e-10),
         ScheduleIntervals.create(bankruptcyOperation.getTimeToApply())
         );
   }
   
   @SuppressWarnings("unused")
   private final void performIntervention() {
      bankruptcyOperation.performIntervention(getPopulation());
   }
   
   private static final long serialVersionUID = -8801926682716783140L;
}
