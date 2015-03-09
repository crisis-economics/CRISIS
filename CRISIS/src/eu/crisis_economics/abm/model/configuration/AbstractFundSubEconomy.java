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

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.fund.ExogenousDepositHolder;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.FundFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * An abstract base class for {@link Fund} subeconomy configuration modules.<br><br>
  * 
  * This base class provides mandatory (Ie. required) bindings for the implementing
  * class. These mandatory bindings ensure that implementations of this class
  * are interchangeable.
  * 
  * @author phillips
  */
public abstract class AbstractFundSubEconomy extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = 2037104037805884119L;
   
   public static final int DEFAULT_NUMBER_OF_FUNDS = 8;
   
   public static final boolean DEFAULT_DO_ENABLE_FUNDS = true;
   
   public static final boolean
      DEFAULT_DO_HOLD_FUND_DEPOSITS_EXOGENOUSLY = false;
   
   @Parameter(
      ID = "NUMBER_OF_FUNDS"
      )
   private int
      numberOfFunds = DEFAULT_NUMBER_OF_FUNDS;
   
   @Parameter(
      ID = "DO_HOLD_FUND_DEPOSITS_EXOGENOUSLY"
      )
   private boolean
      doHoldFundDepositsExogenously = DEFAULT_DO_HOLD_FUND_DEPOSITS_EXOGENOUSLY;
   
   public boolean getDoHoldFundDepositsExogenously() {
      return doHoldFundDepositsExogenously;
   }
   
   public void setDoHoldFundDepositsExogenously(final boolean value) {
      this.doHoldFundDepositsExogenously = value;
   }
   
   public String desDoHoldFundDepositsExogenously() {
      return
         "Toggle whether or not fund agents should store their deposits in an exogenous location. "
       + "Enabling this toggle indicates that funds should not deposit their cash reserves with "
       + "commercial banks. Fund cash deposits will instead be stored, and processed, by an "
       + "exogenous deposit holder.";
   }
   
   public final int getNumberOfFunds() {
      return numberOfFunds;
   }
   
   /**
     * Set the total number of {@link Fund} agents in this model. The argument
     * should be strictly positive.
     */
   public final void setNumberOfFunds(final int value) {
      this.numberOfFunds = value;
   }
   
   public final String desNumberOfFunds() {
      return 
         "The number of distinct funds in the model. Funds offer an investment opportunity "
       + "to household agents, and add a supplementary direction for household cash flow over " 
       + "and above domestic bank deposits. Funds invest household contributions in (a) "
       + "the stock market, and (b) bonds, and thereafter distribute their profits/losses to" 
       + "households, depending on the relative size of the household contribution.";
   }
   
   @Layout(
      FieldName = "Do enable Funds?"
      )
   @Parameter(
      ID = "DO_ENABLE_FUNDS"
      )
   private boolean
      doEnableFunds = DEFAULT_DO_ENABLE_FUNDS;
   
   public boolean isDoEnableFunds() {
      return doEnableFunds;
   }
   
   public void setDoEnableFunds(
      final boolean doEnableFunds) {
      this.doEnableFunds = doEnableFunds;
   }
   
   public String desDoEnableFunds() {
      return "Toggle whether or not to enable funds. If disabled, this parameter signals that "
           + "no fund agents should be created.";
   }
   
   @Submodel
   @Layout(
      Order = 2.9
      )
   @Parameter(
      ID = "FUNDS_STOCK_INVESTMENT_SMOOTHING_ALGORITHM"
      )
   private AbstractSmoothingAlgorithmConfiguration
      stockInvestmentSmoothing = new NoSuddenIncreaseSmoothingAlgorithmConfiguration(.9);
   
   public AbstractSmoothingAlgorithmConfiguration getStockInvestmentSmoothing() {
      return stockInvestmentSmoothing;
   }
   
   public void setStockInvestmentSmoothing(
      final AbstractSmoothingAlgorithmConfiguration stockInvestmentSmoothing) {
      this.stockInvestmentSmoothing = stockInvestmentSmoothing;
   }
   
   /**
     * Create a {@link AbstractFundSubEconomy} object.<br><br>
     * 
     * Implementations of this type must expose the following bindings:
     * <ul>
     *   <li> {@link FundFactory}{@code .class}, for the construction of {@link Fund}
     *        entities;
     *   <li> An {@link Integer} annotated with {@code NUMBER_OF_FUNDS}, which specifies
     *        the expected number of distinct {@link Fund} {@link Agent}{@code s} in 
     *        the populated model;
     *   <li> A {@link Boolean} annotated with {@code DO_HOLD_FUND_DEPOSITS_EXOGENOUSLY},
     *        which specifies whether or not {@link Fund} entities should hold their 
     *        cash deposits with {@link Bank} {@link Agent}{@code s} or in an exogenous,
     *        unmodelled location.
     *   <li> A {@link Boolean} annotated with {@code DO_ENABLE_FUNDS}. This parameter
     *        specifies whether or not {@link Fund} {@link Agent}{@code s} should be
     *        created.
     * </ul>
     */
   protected AbstractFundSubEconomy() { }
   
   /**
     * When overriding this method, call {@link super#assertParameterValidity()}
     * as a first instruction.
     */
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(numberOfFunds > 0,
         toParameterValidityErrMsg("the number of fund agents is negative or zero"
         ));
   }
   
   @Override
   protected final void setRequiredBindings() {
      requireBinding(FundFactory.class);
      requireBinding(ExogenousDepositHolder.class);
      requireBinding(Key.get(Integer.class, Names.named("NUMBER_OF_FUNDS")));
      requireBinding(Key.get(Boolean.class, Names.named("DO_HOLD_FUND_DEPOSITS_EXOGENOUSLY")));
      
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_FUNDS",                     numberOfFunds,
         "DO_HOLD_FUND_DEPOSITS_EXOGENOUSLY",   doHoldFundDepositsExogenously,
         "DO_ENABLE_FUNDS",                     doEnableFunds
         ));
      expose(Integer.class).annotatedWith(Names.named("NUMBER_OF_FUNDS"));
      expose(Boolean.class).annotatedWith(Names.named("DO_HOLD_FUND_DEPOSITS_EXOGENOUSLY"));
      expose(Boolean.class).annotatedWith(Names.named("DO_ENABLE_FUNDS"));
      bind(ExogenousDepositHolder.class).asEagerSingleton();
      expose(ExogenousDepositHolder.class);
   }
}
