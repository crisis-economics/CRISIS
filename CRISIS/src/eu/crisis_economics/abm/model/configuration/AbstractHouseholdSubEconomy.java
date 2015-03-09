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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.io.SectorNameProvider;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.fund.FundInvestmentDecisionRule;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.model.parameters.TimeseriesParameter;
import eu.crisis_economics.abm.simulation.injection.factories.HouseholdFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * An abstract base class for {@link Household} subeconomy configuration
  * modules.<br><br>
  * 
  * This base class provides mandatory (Ie. required) bindings for the implementing
  * class. These mandatory bindings ensure that implementations of this class
  * are interchangeable.
  * 
  * @author phillips
  */
public abstract class AbstractHouseholdSubEconomy extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = 4452693429176282222L;
   
   public static final int 
      DEFAULT_NUMBER_OF_HOUSEHOLDS = 1000;
   
   public static final double 
      DEFAULT_HOUSEHOLD_CASH_ENDOWMENT = 6.75e7 * 3.,
      DEFAULT_PROPORTION_OF_HOUSEHOLD_INITIAL_CASH_IN_FUNDS = .2,
      DEFAULT_AMOUNT_OF_LABOUR_PER_HOUSEHOLD = 2.5;
   
   public static final boolean
      DEFAULT_DO_HOLD_HOUSEHOLD_DEPOSITS_EXOGENOUSLY = false;
   
   @Layout(
      Order = 0,
      FieldName = "Number of Households"
      )
   @Parameter(
      ID = "NUMBER_OF_HOUSEHOLDS"
      )
   private int
      numberOfHouseholds = DEFAULT_NUMBER_OF_HOUSEHOLDS;
   
   public final int getNumberOfHouseholds() {
      return numberOfHouseholds;
   }
   
   /**
     * Get the number of {@link Household}{@code s} in this model. The argument
     * should be strictly positive.
     */
   public final void setNumberOfHouseholds(final int value) {
      this.numberOfHouseholds = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getNumberOfHouseholds}.
     */
   public final String desNumberOfHouseholds() {
      return 
         "The number of households in the model. Households supply their labour to earn wages, " 
       + "consume goods produced by firms, and save their residual income as bank deposits "
       + "and fund investments.";
   }
   
   @Layout(
      Order = 1,
      Title = "Labour && Wage",
      VerboseDescription = "Labour to Offer",
      FieldName = "Labour Per Household"
      )
   @Submodel
   @Parameter(
      ID = "HOUSEHOLD_LABOUR_TO_OFFER"
      )
   private TimeseriesDoubleModelParameterConfiguration
      labourToOfferPerHousehold =
         new ConstantDoubleModelParameterConfiguration(DEFAULT_AMOUNT_OF_LABOUR_PER_HOUSEHOLD);
   
   public final TimeseriesDoubleModelParameterConfiguration
      getLabourToOfferPerHousehold() {
      return labourToOfferPerHousehold;
   }
   
   public final void setLabourToOfferPerHousehold(
      final TimeseriesDoubleModelParameterConfiguration value) {
      labourToOfferPerHousehold = value;
   }
   
   /**
     * Verbose description for the parameter {@link #getLabourToOfferPerHousehold}.
     */
   public final String desLabourToOfferPerHousehold() {
      return "The amound of labour (number of units) each household can supply"
           + " in this model.";
   }
   
   @Layout(
      Order = 2,
      FieldName = "Cash Endowment"
      )
   @Submodel
   @Parameter(
      ID = "HOUSEHOLD_INITIAL_CASH_ENDOWMENT"
      )
   private SampledDoubleModelParameterConfiguration
      initialCashEndowmentPerHousehold =
         new ConstantDoubleModelParameterConfiguration(DEFAULT_HOUSEHOLD_CASH_ENDOWMENT);
   
   public final SampledDoubleModelParameterConfiguration
      getInitialCashEndowmentPerHousehold() {
      return initialCashEndowmentPerHousehold;
   }
   
   /**
     * Set the initial {@link Household} cash endowment. The argument should
     * be non-negative.
     */
   public final void setInitialCashEndowmentPerHousehold(
      final SampledDoubleModelParameterConfiguration value) {
      initialCashEndowmentPerHousehold = value;
   }
   
   @Layout(
      Order = 2.4,
      FieldName = "Sectors To Buy From"
      )
   @Submodel
   @Parameter(
      ID = "HOUSEHOLD_GOODS_TYPES_TO_CONSUME"
      )
   private HouseholdSectorsToBuyFromConfiguration
      sectorsToBuyFrom = new BuyFromAllSectorsHouseholdConsumptionConfiguration();
   
   public HouseholdSectorsToBuyFromConfiguration getSectorsToBuyFrom() {
      return sectorsToBuyFrom;
   }
   
   public void setSectorsToBuyFrom(
      final HouseholdSectorsToBuyFromConfiguration sectorsToBuyFrom) {
      this.sectorsToBuyFrom = sectorsToBuyFrom;
   }

   @Layout(
      Order = 3,
      Title = "Fund Interaction",
      VerboseDescription = "Fund investment decisions",
      FieldName = "Initial Proportion of Cash In Fund Investments"
      )
   @Parameter(
      ID = "PROPORTION_OF_INITIAL_HOUSEHOLDS_CASH_IN_FUND_INVESTMENTS"
      )
   private double
      proportionOfInitialHouseholdCashInFundInvestments =
         DEFAULT_PROPORTION_OF_HOUSEHOLD_INITIAL_CASH_IN_FUNDS;
   
   /**
     * Verbose description for the parameter {@link #getInitialCashEndowmentPerHousehold}.
     */
   public final String desInitialCashEndowmentPerHousehold() {
      return 
         "The initial household cash endowment (the amount of cash given to each household at "
       + "initialization).";
   }
   
   public final double getProportionOfInitialHouseholdCashInFundInvestments() {
      return proportionOfInitialHouseholdCashInFundInvestments;
   }
   
   /**
     * Set the initial proportion of {@link Household} cash in {@link Fund}
     * investments. The argument should be non-negative and not greater than
     * {@code 1.0}.
     */
   public final void setProportionOfInitialHouseholdCashInFundInvestments(
      final double value) {
      proportionOfInitialHouseholdCashInFundInvestments = value;
   }
   
   @Layout(
      Order = 5,
      FieldName = "Fund Contribution / Withdrawal Decision Rule"
      )
   @Submodel
   @Parameter(
      ID = "HOUSEHOLD_FUND_CONTRIBUTION_RULE",
      Scoped = false
      )
   private HouseholdFundContributionRuleConfiguration
      fundContributionDecisionRule = new SimpleThresholdFundInvestmentDecisionRuleConfiguration();
   
   public final HouseholdFundContributionRuleConfiguration
      getFundContributionDecisionRule() {
      return fundContributionDecisionRule;
   }
   
   public final void setFundContributionDecisionRule(
      final HouseholdFundContributionRuleConfiguration value) {
      this.fundContributionDecisionRule = value;
   }
   
   @Layout(
      Order = 6,
      Title = "Cash Deposits",
      FieldName = "Hold Household Deposits Exogenously?"
      )
   @Parameter(
      ID = "DO_HOLD_HOUSEHOLD_DEPOSITS_EXOGENOUSLY"
      )
   private boolean
      doHoldHouseholdDepositsExogenously = DEFAULT_DO_HOLD_HOUSEHOLD_DEPOSITS_EXOGENOUSLY;
   
   public boolean isDoHoldHouseholdDepositsExogenously() {
      return doHoldHouseholdDepositsExogenously;
   }
   
   public void setDoHoldHouseholdDepositsExogenously(
      final boolean doHoldHouseholdDepositsExogenously) {
      this.doHoldHouseholdDepositsExogenously = doHoldHouseholdDepositsExogenously;
   }
   
   /**
     * Verbose description for the parameter {@link #getInitialHouseholdCash}.
     */
   public final String desProportionOfInitialHouseholdCashInFundInvestments() {
      return 
         "The initial proportion (0.0-1.0 inclusive) of household cash in fund investments.";
   }
   
   /**
     * Create a {@link AbstractHouseholdSubEconomy} object.<br><br>
     * 
     * Implementations of this type must expose the following bindings:
     * <ul>
     *   <li> {@link HouseholdFactory}{@code .class}, for the construction of
     *        {@link Household} {@link Agent}{@code s};
     *   <li> An {@link Integer} annotated with {@code NUMBER_OF_HOUSEHOLDS}, which 
     *        specifies the expected number of distinct {@link Household} {@link Agent}{@code s}
     *        in the populated model;
     *   <li> An {@link Double} annotated with {@code HOUSEHOLD_CASH_ENDOWMENT}, which 
     *        specifies the {@link Household} cash endowment at initialization;
     *   <li> An {@link Double} annotated with
     *        {@code PROPORTION_OF_HOUSEHOLD_INITIAL_CASH_IN_FUNDS}, which 
     *        specifies the proportion of initial {@link Household} cash held in 
     *        {@link Fund}{@code s} at the start of the simulation.
     *   <li> An {@link Boolean} annotated with
     *        {@code DO_HOLD_HOUSEHOLD_DEPOSITS_EXOGENOUSLY}, which specifies 
     *        whether or not the {@link Household} should hold its deposits in 
     *        an exogenous location.
     *   <li> An implementation of {@link FundInvestmentDecisionRule}{@code .class}.
     *   <li> An implementation of {@link SectorNameProvider}{@code .class}
     *        annotated with <code>HOUSEHOLD_GOODS_TYPES_TO_CONSUME</code>.
     * </ul>
     */
   protected AbstractHouseholdSubEconomy() { }
   
   /**
     * When overriding this method, call {@link super#assertParameterValidity()}
     * as a first instruction.
     */
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(numberOfHouseholds > 0,
         toParameterValidityErrMsg("Number of Households is non-positive."));
      Preconditions.checkArgument(proportionOfInitialHouseholdCashInFundInvestments >= 0,
         toParameterValidityErrMsg(
            "Proportion of Initial Household Cash In Fund Investments is negative."));
   }
   
   @Override
   protected final void setRequiredBindings() {
      requireBinding(HouseholdFactory.class);
      
      requireBinding(Key.get(Integer.class, Names.named("NUMBER_OF_HOUSEHOLDS")));
      
      requireBinding(Key.get(Double.class,
         Names.named("PROPORTION_OF_INITIAL_HOUSEHOLDS_CASH_IN_FUND_INVESTMENTS")));
      requireBinding(
         Key.get(Double.class, Names.named("HOUSEHOLD_INITIAL_CASH_ENDOWMENT")));
      requireBinding(
         Key.get(new TypeLiteral<TimeseriesParameter<Double>>(){},
         Names.named("HOUSEHOLD_LABOUR_TO_OFFER")));
      requireBinding(FundInvestmentDecisionRule.class);
      
      requireBinding(
         Key.get(SectorNameProvider.class, Names.named("HOUSEHOLD_GOODS_TYPES_TO_CONSUME")));
      
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_HOUSEHOLDS",                            numberOfHouseholds,
         "PROPORTION_OF_INITIAL_HOUSEHOLDS_CASH_IN_FUND_INVESTMENTS",
            proportionOfInitialHouseholdCashInFundInvestments,
         "DO_HOLD_HOUSEHOLD_DEPOSITS_EXOGENOUSLY",
            doHoldHouseholdDepositsExogenously
         ));
      
      expose(Integer.class).annotatedWith(
         Names.named("NUMBER_OF_HOUSEHOLDS"));
      expose(Double.class).annotatedWith(
         Names.named("PROPORTION_OF_INITIAL_HOUSEHOLDS_CASH_IN_FUND_INVESTMENTS"));
      expose(Boolean.class).annotatedWith(
         Names.named("DO_HOLD_HOUSEHOLD_DEPOSITS_EXOGENOUSLY"));
   }
   
   /**
     * When overriding this method, call {@link super#assertParameterValidity()}
     * as a last instruction.
     */
   @Override
   protected void addBindings() {
      expose(FundInvestmentDecisionRule.class);
      expose(SectorNameProvider.class)
         .annotatedWith(Names.named("HOUSEHOLD_GOODS_TYPES_TO_CONSUME"));
   }
}
