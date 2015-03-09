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
import eu.crisis_economics.abm.firm.plugins.FollowTargetProductionPricingAlgorithm;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

@ConfigurationComponent(
   DisplayName = "Follow Target Production"
   )
public final class FollowTargetProductionGoodsPricingConfiguration
   extends AbstractPrivateConfiguration
   implements FirmGoodsPricingDecisionRuleConfiguration {
   
   private static final long serialVersionUID = -1517452997127261414L;
   
   public static final double
      DEFAULT_INITIAL_GOODS_SELLING_PRICE_PER_UNIT = 8.25e7,
      DEFAULT_FOLLOW_TARGET_PRODUCTION_PRICING_ALGORITHM_EXPONENT = 1./3.;
   
   @Parameter(
      ID = "INITIAL_GOODS_SELLING_PRICE_PER_UNIT"
      )
   private double
      goodsInitialSalePrice =
         DEFAULT_INITIAL_GOODS_SELLING_PRICE_PER_UNIT;
   @Parameter(
      ID = "FOLLOW_TARGET_PRODUCTION_PRICING_ALGORITHM_EXPONENT"
      )
   private double
      targetProductionTrailingExponent =
         DEFAULT_FOLLOW_TARGET_PRODUCTION_PRICING_ALGORITHM_EXPONENT;
   
   public double getGoodsInitialSalePrice() {
      return goodsInitialSalePrice;
   }
   
   public void setGoodsInitialSalePrice(
      final double goodsInitialSalePrice) {
      this.goodsInitialSalePrice = goodsInitialSalePrice;
   }
   
   public double getTargetProductionTrailingExponent() {
      return targetProductionTrailingExponent;
   }
   
   public void setTargetProductionTrailingExponent(
      final double targetProductionTrailingExponent) {
      this.targetProductionTrailingExponent = targetProductionTrailingExponent;
   }
   
   public FollowTargetProductionGoodsPricingConfiguration() { }
   
   public FollowTargetProductionGoodsPricingConfiguration(
      final double goodsInitialSalePrice,
      final double exponent) {
      this.goodsInitialSalePrice = goodsInitialSalePrice;
      this.targetProductionTrailingExponent = exponent;
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(goodsInitialSalePrice >= 0,
         toParameterValidityErrMsg("Initial goods selling price is negative."));
      Preconditions.checkArgument(targetProductionTrailingExponent >= 0,
         toParameterValidityErrMsg("Trailing Exponent is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "INITIAL_GOODS_SELLING_PRICE_PER_UNIT",                  goodsInitialSalePrice,
         "FOLLOW_TARGET_PRODUCTION_PRICING_ALGORITHM_EXPONENT",   targetProductionTrailingExponent
         ));
      bind(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(FollowTargetProductionPricingAlgorithm.class);
      expose(
         FirmDecisionRule.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
