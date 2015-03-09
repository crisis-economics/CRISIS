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
import com.google.inject.Singleton;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.bank.BadBank;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A base class for {@link BadBank} {@link Agent} configuration components.<br><br>
  * 
  * The current codebase offers only one {@link BadBank} implementation. For this reason
  * this base class remains non-abstract despite its name. In future, when alternative
  * {@link BadBank} implementations are created, this base class should become
  * <code>abstract</code> and a set of required bindings should be specified.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Default Bridge Bank",
   Description =
      "The Bad Bank is an agent that assumes bankrupt banks' non-performing " + 
      "or troubled assets and sells them off in an orderly way."
   )
public class AbstractBadBankConfiguration extends AbstractPublicConfiguration {
   
   private static final long serialVersionUID = 5361053284369027953L;
   
   private final static boolean
      DEFAULT_IS_BAD_BANK_ENABLED = true;
   
   /**
     * The initial {@link BadBank} cash endowment.<br><br>
     * 
     * <b>Know that</b> very and unrealistically high values for this parameter may have
     * subtle undesirable numerical consequences in the running model. 
     */
   public static final double
      DEFAULT_BAD_BANK_INITIAL_CASH_ENDOWMENT = 1.e15;
   
   @Layout(
      Order = 1.0,
      FieldName = "Enable Bad Bank?"
      )
   @Parameter(
      ID = "IS_BAD_BANK_ENABLED"
      )
   private boolean
      isBadBankEnabled = DEFAULT_IS_BAD_BANK_ENABLED;
   
   public boolean getIsBadBankEnabled() {
      return isBadBankEnabled;
   }
   
   public void setIsBadBankEnabled(final boolean isBadBankEnabled) {
      this.isBadBankEnabled = isBadBankEnabled;
   }
   
   public String desIsBadBankEnabled() {
      return "Toggle for turning on the bad bank.";
   }
   
   @Layout(
      Order = 1.1,
      VerboseDescription = "Endowments & Initial Conditions",
      FieldName = "Bad Bank Cash Endowment"
      )
   @Parameter(
      ID = "BAD_BANK_INITIAL_CASH_ENDOWMENT"
      )
   private double
      badBankInitialCashEndowment = DEFAULT_BAD_BANK_INITIAL_CASH_ENDOWMENT;
   
   public double getBadBankInitialCashEndowment() {
      return badBankInitialCashEndowment;
   }
   
   public void setBadBankInitialCashEndowment(final double value) {
      this.badBankInitialCashEndowment = value;
   }
   
   public String desBadBankInitialCashEndowment() {
      return
         "The bad bank initial cash endowment, if a bad bank agent is present.  In order that "
       + "the Bad Bank is able to absorb non-performing or troubled assets and still maintain "
       + "positive equity, this initial cash endowment should be several orders of "
       + "magnitude larger than the size of initial cash endowment or equity of commercial "
       + "banks.";
   }
   
   @Layout(
      Order = 3.0,
      Title = "Market Interactions",
      VerboseDescription = "Market Interactions",
      FieldName = "Market Behaviour"
      )
   @Submodel
   @Parameter(
      ID = "BAD_BANK_CLEARING_MARKET_PARTICIPATION"
      )
   StockResellerMarketParticipationConfiguration badBankClearingMarketResponses
      = new StockResellerMarketParticipationConfiguration();
   
   public StockResellerMarketParticipationConfiguration getBadBankClearingMarketResponses() {
      return badBankClearingMarketResponses;
   }
   
   public void setBadBankClearingMarketResponses(
      final StockResellerMarketParticipationConfiguration clearingMarketResponses) {
      this.badBankClearingMarketResponses = clearingMarketResponses;
   }
   
   @Override
   public void assertParameterValidity() {
      Preconditions.checkArgument(
         badBankInitialCashEndowment >= 0.,
         toParameterValidityErrMsg("Bad Bank cash endowment is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
      "BAD_BANK_INITIAL_CASH_ENDOWMENT",                 badBankInitialCashEndowment,
      "IS_BAD_BANK_ENABLED",                             isBadBankEnabled
      ));
      bind(BadBank.class).in(Singleton.class);
   }
}
