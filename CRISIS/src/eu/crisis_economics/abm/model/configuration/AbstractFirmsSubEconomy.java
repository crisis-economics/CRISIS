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

import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.FirmFactory;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.FundFactory;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * An abstract base class for {@link Firm} subeconomy configuration modules.<br><br>
  * 
  * This base class provides mandatory (Ie. required) bindings for the implementing
  * class. These mandatory bindings ensure that implementations of this class
  * are interchangeable.
  * 
  * @author phillips
  */
public abstract class AbstractFirmsSubEconomy extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = -8761459974294400240L;
   
   public static final double
      DEFAULT_INITIAL_FIRM_CASH_ENDOWMENT = 0.,
      DEFAULT_INITIAL_FIRM_PRICE_PER_SHARE = 8.e8,
      DEFAULT_INITIAL_NUMBER_OF_FIRM_SHARES = 1.0;
   
   public static final int
      DEFAULT_NUMBER_OF_FIRMS = 52;
   
   public static final boolean
      DEFAULT_DO_HOLD_FIRM_DEPOSITS_EXOGENOUSLY = false;
   
   @Layout(
      Order = 0,
      FieldName = "Do hold firm deposits exogenously?"
      )
   @Parameter(
      ID = "DO_HOLD_FIRM_DEPOSITS_EXOGENOUSLY"
      )
   private boolean
      doHoldFirmDepositsExogenously = DEFAULT_DO_HOLD_FIRM_DEPOSITS_EXOGENOUSLY;
   
   public boolean getDoHoldFirmDepositsExogenously() {
      return doHoldFirmDepositsExogenously;
   }
   
   public void setDoHoldFirmDepositsExogenously(
      final boolean doHoldFirmDepositsExogenously
      ) {
      this.doHoldFirmDepositsExogenously = doHoldFirmDepositsExogenously;
   }
   
   @Layout(
      Order = 1.0,
      Title = "Names && Sectors",
      VerboseDescription = "Sectors and uniquely identifying firm names",
      FieldName = "Naming Convention"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_NAME_GENERATOR"
      )
   private AbstractAgentNameFactoryConfiguration
      namingConvention = new InstanceIDAgentNameFactoryConfiguration();
   
   public AbstractAgentNameFactoryConfiguration getNamingConvention() {
      return namingConvention;
   }
   
   public void setNamingConvention(
      final AbstractAgentNameFactoryConfiguration namingConvention) {
      this.namingConvention = namingConvention;
   }
   
   @Layout(
      Order = 1.1,
      FieldName = "Sectors"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_PRODUCTION_GOODS_TYPE"
      )
   private FirmSectorProviderConfiguration
      sectors = new MultipleFirmsPerSectorProviderConfiguration(1);
   
   public final FirmSectorProviderConfiguration getSectors() {
      return sectors;
   }
   
   public final void setSectors(
      final FirmSectorProviderConfiguration sectors) {
      this.sectors = sectors;
   }
   
   /**
     * Verbose description for parameter {@link #getSectors}.
     */
   public final String desSectors() {   // TODO
      return "The number of firms per sector, along with the number of sectors, determines the "
            + "total amount of firms in the Input-Output macroeconomy.  Firms in the same sector "
            + "produce identical goods, although the amount produced by each firm depends on its "
            + "productivity and will vary across firms.";
   }
   
   @Layout(
      Order = 1.2,
      FieldName = "Number of Firms"
      )
   @Parameter(
      ID = "NUMBER_OF_FIRMS"
      )
   private int
      numberOfFirms = DEFAULT_NUMBER_OF_FIRMS;
   
   public int getNumberOfFirms() {
      return numberOfFirms;
   }
   
   public void setNumberOfFirms(
      int numberOfFirms) {
      this.numberOfFirms = numberOfFirms;
   }
   
   public String desNumberOfFirms() {
      return "The number of distinct firm agents to create.";
   }
   
   @Layout(
      Order = 2,
      Title = "Endowments",
      VerboseDescription = "Cash",
      FieldName = "Initial Cash Endowment Per Firm"
      )
   @Parameter(
      ID = "FIRM_INITIAL_CASH_ENDOWMENT"
      )
   private double
      initialCashEndowmentPerFirm = DEFAULT_INITIAL_FIRM_CASH_ENDOWMENT;
   
   @Layout(
      Order = 2.1,
      VerboseDescription = "Share Emission",
      FieldName = "Initial Price Per Share Per Firm"
      )
   @Submodel
   @Parameter(
      ID = "FIRM_INITIAL_PRICE_PER_SHARE"
      )
   private SampledDoubleModelParameterConfiguration
      initialFirmPricePerShare = new ConstantDoubleModelParameterConfiguration(
         DEFAULT_INITIAL_FIRM_PRICE_PER_SHARE);
   
   @Layout(
      Order = 2.2,
      FieldName = "Shares Emitted Per Firm"
      )
   @Parameter(
      ID = "FIRM_INITIAL_NUMBER_OF_SHARES_EMITTED"
      )
   private double
      initialNumberOfSharesPerFirm = DEFAULT_INITIAL_NUMBER_OF_FIRM_SHARES;
   
   /**
     * Get the amount of cash with which each {@link Firm} is initialized.
     * This initial cash endowment does not include any cash obtained
     * from an initial share emission.
     */
   public final double getInitialCashEndowmentPerFirm() {
      return initialCashEndowmentPerFirm;
   }
   
   /**
     * Set the amount of cash with which each {@link Firm} is initialized.
     * This initial cash endowment does not include any cash obtained
     * from an initial share emission. The argument to this method
     * should be non-negative.
     */
   public final void setInitialCashEndowmentPerFirm(final double value) {
      initialCashEndowmentPerFirm = value;
   }
   
   /**
     * Verbose description for parameter {@link #getInitialCashEndowmentPerFirm}.
     */
   public final String desInitialCashEndowmentPerFirm() {
      return "The initial amount of cash with which each Bank is endowed at initialization. "
           + "This sum does not include any additional cash that the Bank may obtain from "
           + "an initial share emission.";
   }
   
   /**
     * Get the cash value of the initial {@link Firm} share emission.
     */
   public final SampledDoubleModelParameterConfiguration getInitialFirmPricePerShare() {
      return initialFirmPricePerShare;
   }
   
   /**
     * Set the cash value of the initial {@link Firm} share emission. The
     * argument to this method should be non-negative.
     */
   public final void setInitialFirmPricePerShare(
      final SampledDoubleModelParameterConfiguration value) {
      initialFirmPricePerShare = value;
   }
   
   /**
     * Verbose description for parameter {@link #getInitialStockEmissionPerFirm}.
     */
   public final String desInitialFirmPricePerShare() {
      return "The cash value of each initial firms share emission.";
   }
   
   /**
     * Get the initial number of shares per {@link Firm}.
     */
   public final double getInitialNumberOfSharesPerFirm() {
      return initialNumberOfSharesPerFirm;
   }
   
   /**
     * Set the initial number of shares per {@link Firm}. The argument should
     * be strictly positive.
     */
   public final void setInitialNumberOfSharesPerFirm(final double value) {
      initialNumberOfSharesPerFirm = value;
   }
   
   /**
     * Verbose description for parameter {@link #getInitialNumberOfSharesPerFirm}.
     */
   public final String desInitialNumberOfSharesPerFirm() {
      return "The initial number of shares to release per firm.";
   }
   
   /**
     * Create a {@link AbstractFirmsSubEconomy} object.<br><br>
     * 
     * Implementations of this type must expose the following bindings:
     * <ul>
     *   <li> {@link FundFactory}{@code .class}, for the construction of {@link Fund}
     *        entities;
     *   <li> A {@link Double} annotated with {@code FIRM_INITIAL_CASH_ENDOWMENT}, which
     *        specifies the initial cash sum given to each newly created {@link Firm};
     *   <li> A {@link Double} annotated with {@code FIRM_INITIAL_NUMBER_OF_SHARES_EMITTED},
     *        which specifies the initial number of shares emitted by each {@link Firm};
     *   <li> A {@link Boolean} annotated with {@code DO_HOLD_FIRM_DEPOSITS_EXOGENOUSLY},
     *        which specifies the total value of the initial share emission;
     *   <li> A {@link AgentNameFactory} annotated with {@code FIRM_NAME_GENERATOR}.
     * </ul>
     */
   protected AbstractFirmsSubEconomy() { }
   
   /**
     * When overriding this method, call {@link super#assertParameterValidity()}
     * as a first instruction.
     */
   @Override
   protected void assertParameterValidity() {
      /*
       * Firm initialization parameters:
       */
      Preconditions.checkArgument(initialCashEndowmentPerFirm >= 0.,
         toParameterValidityErrMsg("Initial Firm Cash Endowment is negative."));
   }
   
   /**
     * When overriding this method, call {@link super#addBindings()} as a 
     * last instruction.
     */
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "NUMBER_OF_FIRMS", numberOfFirms));
      expose(Integer.class).annotatedWith(Names.named("NUMBER_OF_FIRMS"));
   }
   
   @Override
   protected final void setRequiredBindings() {
      requireBinding(FirmFactory.class);
      
      requireBinding(Key.get(String.class, Names.named("FIRM_SECTOR_NAME")));
      
      requireBinding(Key.get(Double.class, Names.named("FIRM_INITIAL_CASH_ENDOWMENT")));
      requireBinding(Key.get(Double.class, Names.named("FIRM_INITIAL_NUMBER_OF_SHARES_EMITTED")));
      requireBinding(Key.get(Double.class, Names.named("FIRM_INITIAL_PRICE_PER_SHARE")));
      
      requireBinding(Key.get(AgentNameFactory.class, Names.named("FIRM_NAME_GENERATOR")));
      
      requireBinding(Key.get(Boolean.class, Names.named("DO_HOLD_FIRM_DEPOSITS_EXOGENOUSLY")));
      
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FIRM_INITIAL_CASH_ENDOWMENT",            initialCashEndowmentPerFirm,
         "FIRM_INITIAL_NUMBER_OF_SHARES_EMITTED",  initialNumberOfSharesPerFirm,
         "DO_HOLD_FIRM_DEPOSITS_EXOGENOUSLY",      doHoldFirmDepositsExogenously
         ));
      
      expose(Double.class).annotatedWith(Names.named("FIRM_INITIAL_CASH_ENDOWMENT"));
      expose(Double.class).annotatedWith(Names.named("FIRM_INITIAL_NUMBER_OF_SHARES_EMITTED"));
      expose(Double.class).annotatedWith(Names.named("FIRM_INITIAL_PRICE_PER_SHARE"));
      expose(Boolean.class).annotatedWith(Names.named("DO_HOLD_FIRM_DEPOSITS_EXOGENOUSLY"));
   }
}
