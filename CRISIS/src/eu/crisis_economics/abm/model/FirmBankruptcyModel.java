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
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
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
  * A model with manual, artifical {@link Firm} bankruptcy events. This model creates
  * a standard master model and supplements the natural evolution of this simulation by
  * manually producing a series of {@link Firm} bankruptcies.
  * 
  * @author phillips
  */
public class FirmBankruptcyModel extends AbstractModel {
   
   /*                             *
    * Bindings and Configuration  *
    *                             */
   private MasterModelConfiguration
      agentsConfiguration = new MasterModelConfiguration(); 
   
   private AbstractPlumbingConfiguration
      plumbingConfiguration = new FinancialSystemWithMacroEconomyConfiguration();
   
   public FirmBankruptcyModel() {
      this(1L);
   }
   
   public FirmBankruptcyModel(long seed) {
      super(seed);
   }
   
   /**
     * A lightweight configuration component for manual {@link Firm} bankruptcy events.
     * 
     * @author phillips
     */
   @ConfigurationComponent(
      DisplayName = "Perform a"
      )
   static abstract class AbstractFirmBankruptcyOperation {
      
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
         DisplayName = "Cash Extraction",
         Description =
            "Induce firm bankruptcies by artifically withdrawing their cash. This "
          + "model will attempt to bankrupt a customizable number of firms through removing "
          + "(potentially large) fractions of all cash assets held by affected firms. This cash "
          + "extraction is in turn likely to yield commercial loan default, and will be "
          + "executed whether or not the cash is allocated for other purposes."
         )
      public static final class BankruptcyByCashExtractionOperation
         extends AbstractFirmBankruptcyOperation {
         
         @Layout(
            FieldName = "% Firm Cash To Remove"
            )
         private double
            percentFirmCashToRemove = 50.;
         @Layout(
            FieldName = "Number of Firms to Harm"
            )
         private double
            percentOfFirmsToHarm = 40.;
         
         public double getPercentFirmCashToRemove() {
            return percentFirmCashToRemove;
         }
         
         public void setPercentFirmCashToRemove(
            final double value) {
            if(value < 0. || value > 100.)
               throw new IllegalArgumentException(
                  getClass().getSimpleName() + ": cash drain percentage must be in the range "
                + "[0, 100].");
            this.percentFirmCashToRemove = value;
         }
         
         public double getNumberOfFirmsToHarm() {
            return percentOfFirmsToHarm;
         }
         
         public void setNumberOfFirmsToHarm(
            final double value) {
            if(value < 0. || value > 100.)
               throw new IllegalArgumentException(
                  getClass().getSimpleName() + ": percent of firms to harm must be in the range "
                + "[0, 100].");
            this.percentOfFirmsToHarm = value;
         }
         
         @Override
         public void performIntervention(AgentGroup population) {
            System.out.printf(
               "----------------------------------------------------\n"
             + "Forcibly inducing firm bankruptcies (cash extraction)..\n"
             + percentOfFirmsToHarm + " Firms, " + percentFirmCashToRemove + "%% cash extraction..\n"
             + "----------------------------------------------------\n"
               );
            final List<LoanStrategyFirm>
               firms = population.getAgentsOfType(LoanStrategyFirm.class);
            final int
               numFirmsToHarm = (int) Math.floor(percentOfFirmsToHarm * (firms.size() - 1) / 100.);
            for(int index = 0; index < numFirmsToHarm; ++index) {
               final LoanStrategyFirm
                  firmToPenalize = firms.get(index);
               final double
                  firmCash = firmToPenalize.getDepositValue(),
                  cashAmountToExtract = firmCash * percentFirmCashToRemove / 100.;
               try {
                  firmToPenalize.credit(cashAmountToExtract);
               } catch (final InsufficientFundsException neverThrows) {
                  System.err.println(neverThrows);
                  System.err.flush();
                  System.out.println(
                     getClass().getSimpleName() + ": failed to extract " + cashAmountToExtract
                   + " units of cash from " + firmToPenalize.getUniqueName() + ". The deposit "
                   + " account value of the firm is " + firmToPenalize.getDepositValue() + ". "
                   + "This behaviour is not expected in the current implementation and is "
                   + "indicative of an error."
                   );
                  Simulation.getRunningModel().finish();
               }
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
   public AbstractFirmBankruptcyOperation
      bankruptcyOperation =
         new AbstractFirmBankruptcyOperation.BankruptcyByCashExtractionOperation();
   
   public final AbstractFirmBankruptcyOperation getBankruptcyOperation() {
      return bankruptcyOperation;
   }
   
   public final void setBankruptcyOperation(
      final AbstractFirmBankruptcyOperation bankruptcyOperation) {
      this.bankruptcyOperation = bankruptcyOperation;
   }
   
   @Override
   protected final AbstractPublicConfiguration getAgentBindings() {
      return agentsConfiguration;
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
   
   private static final long serialVersionUID = 6793178406346933284L;
}
