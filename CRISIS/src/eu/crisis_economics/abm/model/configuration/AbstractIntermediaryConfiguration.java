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
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.intermediary.Intermediary;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A base class for {@link Intermediary} {@link Agent} configuration components.<br><br>
  * 
  * The current codebase offers only one {@link Intermediary} implementation. For this reason
  * this base class remains non-abstract despite its name. In future, when alternative
  * {@link Intermediary} implementations are created, this base class should become
  * <code>abstract</code> and a set of required bindings should be specified.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Financial Stewards"
   )
public class AbstractIntermediaryConfiguration extends AbstractPublicConfiguration {
   
   private static final long serialVersionUID = -7150642159430572136L;
   
   private static final double
      DEFAULT_INTERMEDIARY_ASSET_VALUE_THRESHOLD_AT_WHICH_TO_DISCONTINUE = 1.e-12;
   
   @Layout(
      Order = 0.0,
      Title = "Financial Intermediaries",
      VerboseDescription = "Financial Intermediaries",
      FieldName = "Market Behaviour"
      )
   @Submodel
   @Parameter(
      ID = "FINANCIAL_INTERMEDIARY_CLEARING_MARKET_PARTICIPATION"
      )
   private StockResellerMarketParticipationConfiguration
      financialIntermediaryClearingMarketResponses
         = new StockResellerMarketParticipationConfiguration();
   
   public StockResellerMarketParticipationConfiguration
      getFinancialIntermediaryClearingMarketResponses() {
      return financialIntermediaryClearingMarketResponses;
   }
   
   public void setFinancialIntermediaryClearingMarketResponses(
      final StockResellerMarketParticipationConfiguration clearingMarketResponses) {
      this.financialIntermediaryClearingMarketResponses = clearingMarketResponses;
   }
   
   @Layout(
      Order = 0.1,
      FieldName = "Asset Value Threshold"
      )
   @Parameter(
      ID = "INTERMEDIARY_ASSET_VALUE_THRESHOLD_AT_WHICH_TO_DISCONTINUE"
      )
   private double
      intermediaryAssetValueThresholdAtWhichToDiscontinue = 
         DEFAULT_INTERMEDIARY_ASSET_VALUE_THRESHOLD_AT_WHICH_TO_DISCONTINUE;
   
   public final double getIntermediaryAssetValueThresholdAtWhichToDiscontinue() {
      return intermediaryAssetValueThresholdAtWhichToDiscontinue;
   }
   
   /**
     * Set the asset-value threshold at which intermediaries will discontinue
     * their efforts to liquidate assets and detach from beneficiaries. The
     * argument to this method should be non-negative.
     */
   public final void setIntermediaryAssetValueThresholdAtWhichToDiscontinue(
      final double value) {
      intermediaryAssetValueThresholdAtWhichToDiscontinue = value;
   }
   
   public final String desIntermediaryAssetValueThresholdAtWhichToDiscontinue() {
      return "The threshold is a non-negative parameter that specifies the proportion of loss on "
            + "claims that "
            + "non-stock holding creditors are willing to accept during a bailin resolution process"
            + " to re-capitalize an insolvent bank.  "
            + "\n\nThe bail-in resolution procedure converts some of the insolvent "
            + "bank's liabilities into new share equity.  As some of the bank's creditors may be "
            + "household agents, it is possible that some households' deposits were "
            + "converted into new share equity in the re-capitalized bank.  However, in the model, "
            + "household agents do "
            + "not hold stocks or stock accounts, nor do they participate directly in the stock "
            + "market. Therefore, in order to realise the value owed to such creditors, an "
            + "Intermediary agent acts on behalf of the Household creditors "
            + "(the beneficiaries) by taking their share of stock in the rescued bank and"
            + "selling it in the stock market, subsequently passing it back to the Households."
            + "\n\nIn this case the parameter is the threshold at which "
            + "the intermediary will give up this process and detach from the beneficiary. For "
            + "instance, in the long run, the intermediary may only be able to raise 99.999% of the"
            + " value of the beneficiary's original claim on the insolvent bank by selling new "
            + "equity in the restructured bank . A threshold "
            + "of 0.00001 would ensure that the intermediary considers its work to be complete "
            + "after passing 99.999% of the value of the original claim back to the beneficiary.";
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "INTERMEDIARY_ASSET_VALUE_THRESHOLD_AT_WHICH_TO_DISCONTINUE",
            intermediaryAssetValueThresholdAtWhichToDiscontinue
         ));
   }
}
