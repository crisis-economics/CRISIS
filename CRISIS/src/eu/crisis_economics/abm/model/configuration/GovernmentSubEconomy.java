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
import com.google.inject.Singleton;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.Submodel;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Default Government"
   )
public final class GovernmentSubEconomy extends AbstractPublicConfiguration {
   
   private static final long serialVersionUID = 5335230956258289208L;
   
   public static final boolean
      DEFAULT_DO_ENABLE_GOVERNMENT = true,
      DEFAULT_DO_ENABLE_GOVERNMENT_LABOUR_MARKET_PARTICIPATION = true,
      DEFAULT_DO_ENABLE_GOVERNMENT_GOODS_MARKET_PARTICIPATION = true,
      DEFAULT_DO_ENABLE_GOVERNMENT_GILTS_MARKET_PARTICIPATION = true;
   
   public static final double
      DEFAULT_GOVERNMENT_INITIAL_STARTING_CASH = 1.e15,
      DEFAULT_GOVERNMENT_TOTAL_GILT_SUPPLY = 5. * 4. * 1.7e9,
      DEFAULT_GOVERNMENT_WAGE_PER_UNIT_LABOUR_ASK_PRICE = 6.75e7,
      DEFAULT_GOVERNMENT_THRESHOLD_AT_WHICH_TO_DISOWN_SHARES = 1.e-14;
   
   @Parameter(
      ID = "DO_ENABLE_GOVERNMENT"
      )
   private boolean
      doEnableGovernment = DEFAULT_DO_ENABLE_GOVERNMENT;
   @Parameter(
      ID = "DO_ENABLE_GOVERNMENT_LABOUR_MARKET_PARTICIPATION"
      )
   private boolean
      doEnableGovernmentLabourMarketParticipation =
         DEFAULT_DO_ENABLE_GOVERNMENT_LABOUR_MARKET_PARTICIPATION;
   @Parameter(
      ID = "DO_ENABLE_GOVERNMENT_GOODS_MARKET_PARTICIPATION"
      )
   private boolean
      doEnableGovernmentGoodsMarketParticipation =
         DEFAULT_DO_ENABLE_GOVERNMENT_GOODS_MARKET_PARTICIPATION;
   @Parameter(
      ID = "DO_ENABLE_GOVERNMENT_GILTS_MARKET_PARTICIPATION"
      )
   private boolean
      doEnableGovernmentGiltsMarketParticipation =
         DEFAULT_DO_ENABLE_GOVERNMENT_GILTS_MARKET_PARTICIPATION;
   
   @Parameter(
      ID = "GOVERNMENT_INITIAL_STARTING_CASH"
      )
   private double
      governmentInitialStartingCash = DEFAULT_GOVERNMENT_INITIAL_STARTING_CASH;
   @Parameter(
      ID = "GOVERNMENT_TOTAL_GILT_SUPPLY"
      )
   private double
      governmentTotalGiltSupply = DEFAULT_GOVERNMENT_TOTAL_GILT_SUPPLY;
   @Parameter(
      ID = "GOVERNMENT_WAGE_PER_UNIT_LABOUR_ASK_PRICE"
      )
   private double
      governmentWagePerUnitLabourAskPrice = DEFAULT_GOVERNMENT_WAGE_PER_UNIT_LABOUR_ASK_PRICE;
   @Parameter(
      ID = "GOVERNMENT_THRESHOLD_AT_WHICH_TO_DISOWN_SHARES"
      )
   private double
      governmentQuantityThresholdAtWhichToDisownShares =
         DEFAULT_GOVERNMENT_THRESHOLD_AT_WHICH_TO_DISOWN_SHARES;
   
   public final boolean getDoEnableGovernment() {
      return doEnableGovernment;
   }
   
   public final void setDoEnableGovernment(final boolean value) {
      this.doEnableGovernment = value;
   }
   
   /**
     * Returns true when and only when the {@link Government} {@link Agent} employs
     * a labour force.
     */
   public final boolean getDoEnableGovernmentLabourMarketParticipation() {
      return doEnableGovernmentLabourMarketParticipation;
   }
   
   /**
     * Set whether or not the {@link Government} {@link Agent} employs a labour
     * force.
     */
   public final void setDoEnableGovernmentLabourMarketParticipation(final boolean value) {
      this.doEnableGovernmentLabourMarketParticipation = value;
   }
   
   /**
     * Verbose description for the parameter 
     * {@link #getDoEnableGovernmentLabourMarketParticipation}.
     */
   public final String desDoEnableGovernmentLabourMarketParticipation() {
      return 
         "Toggle whether or not the Government seeks to employ a workforce.";
   }
   
   /**
     * Returns true when and only when the {@link Government} {@link Agent} buys 
     * and consumes goods.
     */
   public final boolean getDoEnableGovernmentGoodsMarketParticipation() {
      return doEnableGovernmentGoodsMarketParticipation;
   }
   
   /**
     * Set whether or not the {@link Government} {@link Agent} buys and consumes
     * goods.
     */
   public final void setDoEnableGovernmentGoodsMarketParticipation(final boolean value) {
      this.doEnableGovernmentGoodsMarketParticipation = value;
   }
   
   /**
     * Verbose description for the parameter 
     * {@link #getDoEnableGovernmentGoodsMarketParticipation}.
     */
   public final String desDoEnableGovernmentGoodsMarketParticipation() {
      return 
         "Toggle whether or not the Government seeks to buy and consume goods.";
   }
   
   /**
     * Returns true when and only when the {@link Government} {@link Agent} offers
     * gilts (Government bonds).
     */
   public final boolean getDoEnableGovernmentGiltsMarketParticipation() {
      return doEnableGovernmentGiltsMarketParticipation;
   }
   
   /**
     * Set whether or not the {@link Government} {@link Agent} offers gilts (Government
     * bonds).
     */
   public final void setDoEnableGovernmentGiltsMarketParticipation(final boolean value) {
      this.doEnableGovernmentGiltsMarketParticipation = value;
   }
   
   /**
     * Verbose description for the parameter 
     * {@link #getDoEnableGovernmentGiltsMarketParticipation}.
     */
   public final String desDoEnableGovernmentGiltsMarketParticipation() {
      return 
         "Toggle whether or not the Government offers gilts (Government bonds).";
   }
   
   public final double getGovernmentInitialStartingCash() {
      return governmentInitialStartingCash;
   }
   
   /**
     * Set the {@link Government} initial starting cash for this model. The argument
     * should be non-negative.
     */
   public final void setGovernmentInitialStartingCash(final double value) {
      this.governmentInitialStartingCash = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getGovernmentInitialStartingCash}.
     */
   public final String desGovernmentInitialStartingCash() {
      return 
         "The amount of cash endowed to the Goverment agent at initialization.";
   }
   
   /**
     * Get the {@link Government} gilt supply (maximum Government bond debt) for 
     * this model.
     */
   public final double getGovernmentTotalGiltSupply() {
      return governmentTotalGiltSupply;
   }
   
   /**
     * Set the {@link Government} gilt supply (maximum Government bond debt) for 
     * this model. The argument must be non-negative.
     */
   public final void setGovernmentTotalGiltSupply(final double value) {
      this.governmentTotalGiltSupply = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getGovernmentTotalGiltSupply}.
     */
   public final String desGovernmentTotalGiltSupply() {
      return 
         "The maximum sum the Government is prepared to owe to investors as Gilt "
       + "(Government bond) debt.";
   }
   
   /**
     * Get the {@link Government} wage per unit labour ask price for this model.
     */
   public final double getGovernmentWagePerUnitLabourAskPrice() {
      return governmentWagePerUnitLabourAskPrice;
   }
   
   /**
     * Set the {@link Government} wage per unit labour ask price for this model. The
     * argument should be non-negative.
     */
   public final void setGovernmentWagePerUnitLabourAskPrice(final double value) {
      this.governmentWagePerUnitLabourAskPrice = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getGovernmentWagePerUnitLabourAskPrice}.
     */
   public final String desGovernmentWagePerUnitLabourAskPrice() {
      return 
         "The Government ask price (wage) per unit labour to employ.";
   }
   
   public double getGovernmentQuantityThresholdAtWhichToDisownShares() {
      return governmentQuantityThresholdAtWhichToDisownShares;
   }
   
   public void setGovernmentQuantityThresholdAtWhichToDisownShares(final double value) {
      governmentQuantityThresholdAtWhichToDisownShares = value;
   }
   
   @Submodel
   @Parameter(
      ID = "GOVERNMENT_CLEARING_MARKET_PARTICIPATION"
      )
   private GovernmentClearingMarketResponseFunctionConfiguration
      clearingMarketResponses = new GovernmentClearingMarketResponseFunctionConfiguration();
   
   public GovernmentClearingMarketResponseFunctionConfiguration getClearingMarketResponses() {
      return clearingMarketResponses;
   }
   
   public void setClearingMarketResponses(
      final GovernmentClearingMarketResponseFunctionConfiguration clearingMarketResponses) {
      this.clearingMarketResponses = clearingMarketResponses;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(governmentInitialStartingCash >= 0.,
         toParameterValidityErrMsg("Initial Starting Cash is negative."));
      Preconditions.checkArgument(governmentQuantityThresholdAtWhichToDisownShares >= 0.,
         toParameterValidityErrMsg("Disown Shares Quantity Threshold is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "DO_ENABLE_GOVERNMENT",                             doEnableGovernment,
         "GOVERNMENT_INITIAL_STARTING_CASH",                 governmentInitialStartingCash,
         "GOVERNMENT_TOTAL_GILT_SUPPLY",                     governmentTotalGiltSupply,
         "GOVERNMENT_WAGE_PER_UNIT_LABOUR_ASK_PRICE",        governmentWagePerUnitLabourAskPrice,
         "GOVERNMENT_THRESHOLD_AT_WHICH_TO_DISOWN_SHARES", 
            governmentQuantityThresholdAtWhichToDisownShares,
         "DO_ENABLE_GOVERNMENT_LABOUR_MARKET_PARTICIPATION",
            doEnableGovernmentLabourMarketParticipation,
         "DO_ENABLE_GOVERNMENT_GOODS_MARKET_PARTICIPATION",
            doEnableGovernmentGoodsMarketParticipation,
         "DO_ENABLE_GOVERNMENT_GILTS_MARKET_PARTICIPATION",
            doEnableGovernmentGiltsMarketParticipation
         ));
      bind(Government.class).in(Singleton.class);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Government Subeconomy Configuration Component: " + super.toString();
   }
}
