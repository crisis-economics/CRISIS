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

import com.google.inject.assistedinject.FactoryModuleBuilder;

import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.household.ExogenousHousehold;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.HouseholdFactory;

@ConfigurationComponent(
   Description = ""  // TODO
   )
public final class ExogenousHouseholdsSubEconomy extends AbstractHouseholdSubEconomy {
   
   private static final long serialVersionUID = -4591933825332297247L;
   @Layout(
      Order = 3,
      Title = "Remote Control Decision Rules",
      FieldName = "Deposits D(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_HOUSEHOLD_DEPOSITS_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      depositsTimeseries = new ConstantDoubleModelParameterConfiguration(1.e7);
   
   public TimeseriesDoubleModelParameterConfiguration getDepositsTimeseries() {
      return depositsTimeseries;
   }
   
   public void setDepositsTimeseries(
      final TimeseriesDoubleModelParameterConfiguration depositsTimeseries) {
      this.depositsTimeseries = depositsTimeseries;
   }
   
   @Layout(
      Order = 5,
      FieldName = "Labour Supply L(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_HOUSEHOLD_LABOUR_SUPPLY_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      labourSupplyTimeseries = new ConstantDoubleModelParameterConfiguration(2.5);
   
   public TimeseriesDoubleModelParameterConfiguration getLabourSupplyTimeseries() {
      return labourSupplyTimeseries;
   }
   
   public void setLabourSupplyTimeseries(
      final TimeseriesDoubleModelParameterConfiguration labourSupplyTimeseries) {
      this.labourSupplyTimeseries = labourSupplyTimeseries;
   }
   
   @Layout(
      Order = 6,
      FieldName = "Wage Ask Price W(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_HOUSEHOLD_WAGE_ASK_PRICE_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      wageAskPriceTimeseries = new ConstantDoubleModelParameterConfiguration(6.75e7);
   
   public TimeseriesDoubleModelParameterConfiguration getWageAskPriceTimeseries() {
      return wageAskPriceTimeseries;
   }
   
   public void setWageAskPriceTimeseries(
      final TimeseriesDoubleModelParameterConfiguration wageAskPriceTimeseries) {
      this.wageAskPriceTimeseries = wageAskPriceTimeseries;
   }
   
   @Layout(
      Order = 7,
      Title = "Goods Consumption",
      FieldName = "Consumption Budget B(t)"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_HOUSEHOLD_CONSUMPTION_BUDGET_TIMESERIES"
      )
   private TimeseriesDoubleModelParameterConfiguration
      consumptionBudgetTimeseries =
         new RandomSeriesParameterConfiguration(
            new MeanRevertingRandomWalkSeriesConfiguration(
               .2, 4.e8, 1.e6, 4.e8, 0., Double.MAX_VALUE, 1L));
   
   public TimeseriesDoubleModelParameterConfiguration getConsumptionBudgetTimeseries() {
      return consumptionBudgetTimeseries;
   }
   
   public void setConsumptionBudgetTimeseries(
      final TimeseriesDoubleModelParameterConfiguration consumptionBudgetTimeseries) {
      this.consumptionBudgetTimeseries = consumptionBudgetTimeseries;
   }
   
   @Layout(
      Order = 8,
      FieldName = "Consumption Function"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_HOUSEHOLD_CONSUMPTION_FUNCTION"
      )
   private AbstractHouseholdConsumptionFunctionConfiguration
      consumptionFunction = new CESConsumptionFunctionConfiguration();
   
   public AbstractHouseholdConsumptionFunctionConfiguration getConsumptionFunction() {
      return consumptionFunction;
   }
   
   public void setConsumptionFunction(
      final AbstractHouseholdConsumptionFunctionConfiguration value) {
      this.consumptionFunction = value;
   }
   
   @Override
   protected void addBindings() {
      install(new FactoryModuleBuilder()
         .implement(DepositingHousehold.class, ExogenousHousehold.class)
         .build(HouseholdFactory.class)
         );
      expose(HouseholdFactory.class);
      super.addBindings();
   }
}
