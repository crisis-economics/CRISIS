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
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.plugins.FirmDecisionRule;
import eu.crisis_economics.abm.firm.plugins.ReducePriceIfUnsoldPricingAlgorithm;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A configuration component for the {@link ReducePriceIfUnsoldPricingAlgorithm} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Reduce Price On Unsold"
   )
public final class ReducePriceIfUnsoldPricingAlgorithmConfiguration
   extends AbstractPrivateConfiguration
   implements FirmGoodsPricingDecisionRuleConfiguration {
   
   private static final long serialVersionUID = -3404039222468789954L;
   
   public static final double
      DEFAULT_INITIAL_GOODS_SELLING_PRICE_PER_UNIT = 8.25e7,
      DEFAULT_REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_ADAPTATION_RATE = .04,
      DEFAULT_REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_GRACE = 1.e-5,
      DEFAULT_REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MINIMUM_MARKUP = 1.01,
      DEFAULT_REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MAXIMUM_MARKUP = 1000.;
   
   @Layout(
      Title = "Initialiation && Rates of Change",
      FieldName = "Initial Selling Price",
      VerboseDescription = "Rule Behavior",
      Order = 1.1
      )
   @Parameter(
      ID = "INITIAL_GOODS_SELLING_PRICE_PER_UNIT"
      )
   private double
      goodsInitialSalePrice =
         DEFAULT_INITIAL_GOODS_SELLING_PRICE_PER_UNIT;
   @Layout(
      FieldName = "Market Adaptation Rate",
      Order = 1.2
      )
   @Parameter(
      ID = "REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_ADAPTATION_RATE"
      )
   private double
      adaptationRate =
         DEFAULT_REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_ADAPTATION_RATE;
   @Layout(
      FieldName = "Sales Grace",
      Order = 1.3
      )
   @Parameter(
      ID = "REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_GRACE"
      )
   private double
      grace = DEFAULT_REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_GRACE;
   @Layout(
      Title = "Markup Rates",
      FieldName = "Minimum Markup Rate",
      Order = 2.1,
      VerboseDescription = "Mainimum and maximum markup rates when selecting prices"
      )
   @Parameter(
      ID = "REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MINIMUM_MARKUP"
      )
   private double
      minimumMarkup = DEFAULT_REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MINIMUM_MARKUP;
   @Layout(
      FieldName = "Maximum Markup Rate",
      Order = 2.2,
      VerboseDescription = "Mainimum and maximum markup rates when selecting prices"
      )
   @Parameter(
      ID = "REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MAXIMUM_MARKUP"
      )
   private double
      maximumMarkup = DEFAULT_REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MAXIMUM_MARKUP;
   
   public double getGoodsInitialSalePrice() {
      return goodsInitialSalePrice;
   }
   
   public void setGoodsInitialSalePrice(
      final double goodsInitialSalePrice) {
      this.goodsInitialSalePrice = goodsInitialSalePrice;
   }
   
   public double getAdaptationRate() {
      return adaptationRate;
   }
   
   public void setAdaptationRate(
      final double adaptationRate) {
      this.adaptationRate = adaptationRate;
   }
   
   public double getGrace() {
      return grace;
   }
   
   public void setGrace(
      final double grace) {
      this.grace = grace;
   }
   
   public double getMinimumMarkup() {
      return minimumMarkup;
   }
   
   public void setMinimumMarkup(
      final double minimumMarkup) {
      this.minimumMarkup = minimumMarkup;
   }
   
   public double getMaximumMarkup() {
      return maximumMarkup;
   }
   
   public void setMaximumMarkup(
      final double maximumMarkup) {
      this.maximumMarkup = maximumMarkup;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(goodsInitialSalePrice >= 0,
         toParameterValidityErrMsg("Initial goods selling price is negative."));
      Preconditions.checkArgument(adaptationRate >= 0,
         toParameterValidityErrMsg("Adaptation Rate is negative."));
      Preconditions.checkArgument(minimumMarkup >= 0,
         toParameterValidityErrMsg("Minimum markup rate is negative."));
      Preconditions.checkArgument(maximumMarkup >= 0,
         toParameterValidityErrMsg("Maximum markup rate is negative."));
      Preconditions.checkArgument(maximumMarkup >= minimumMarkup,
         toParameterValidityErrMsg("Maximum markup rate is less than minimum markup rate"));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "INITIAL_GOODS_SELLING_PRICE_PER_UNIT",                     goodsInitialSalePrice,
         "REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_ADAPTATION_RATE", adaptationRate,
         "REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_GRACE",           grace,
         "REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MAXIMUM_MARKUP",  maximumMarkup,
         "REDUCE_PRICE_IF_UNSOLD_PRICING_ALGORITHM_MINIMUM_MARKUP",  minimumMarkup
         ));
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(ReducePriceIfUnsoldPricingAlgorithm.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
