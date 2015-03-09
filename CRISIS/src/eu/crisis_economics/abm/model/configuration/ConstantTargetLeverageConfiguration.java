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

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;
import eu.crisis_economics.abm.strategy.leverage.ConstantTargetLeverageAlgorithm;
import eu.crisis_economics.abm.strategy.leverage.LeverageTargetAlgorithm;

/**
  * A model GUI configuration component for constant (fixed value) {@link Bank}
  * target leverage algorithms.
  * 
  * @author phillips
  */
@ConfigurationComponent(
      Description =
        "Banks using a constant target leverage aspire to keep their leverage constant "
      + "over time.  The value of this constant leverage target can be set using the "
      + "parameter below.  Whenever a bank has a leverage below this target, it will try to"
      + " increase leverage (for example, by issuing more loans to firms), and whenver a "
      + "bank's leverage is above this target, it "
      + "will try to de-leverage (for example, by selling assets).\nThe definition of "
      + "leverage in this model"
      + " is Risky_Assets / Equity, so a bank's (non-risky) cash reserves are not included in"
      + " the Risky_Assets numerator."
        )
public final class ConstantTargetLeverageConfiguration
   extends AbstractBankLeverageTargetConfiguration {
   
   private static final long serialVersionUID = 4853351638818334432L;
   
   public static final double
      DEFAULT_CONSTANT_LEVERAGE_TARGET_ALGORITHM_VALUE = 20.;
   
   @Parameter(
      ID = "CONSTANT_LEVERAGE_TARGET_ALGORITHM_VALUE"
      )
   private double
      leverageTargetValue = DEFAULT_CONSTANT_LEVERAGE_TARGET_ALGORITHM_VALUE;
   
   public double getLeverageTargetValue() {
      return leverageTargetValue;
   }
   
   public void setLeverageTargetValue(final double value) {
      leverageTargetValue = value;
   }
   
   public ConstantTargetLeverageConfiguration() { }
   
   public ConstantTargetLeverageConfiguration(
      final double fixedLeverageTarget
      ) {
      this.leverageTargetValue = fixedLeverageTarget;
   }
   
   public String desLeverageTargetValue() {
      return "The fixed leverage target for leveraged banks using a constant target leverage "
            + "strategy. The value should be non-negative. The definition of leverage in this model"
            + " is Risky_Assets / Equity, so a bank's (non-risky) cash reserves are not included in"
            + " the Risky_Assets numerator.";
   }
   
   @Override
   protected void assertParameterValidity() {
      Preconditions.checkArgument(leverageTargetValue >= 0.,
         toParameterValidityErrMsg("Leverage Target Value is negative."));
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "CONSTANT_LEVERAGE_TARGET_ALGORITHM_VALUE",     leverageTargetValue
         ));
      bind(
         LeverageTargetAlgorithm.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(ConstantTargetLeverageAlgorithm.class);
   }
}