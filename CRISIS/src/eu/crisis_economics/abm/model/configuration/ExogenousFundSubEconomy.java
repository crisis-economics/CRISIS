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

import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.fund.ExogenousFund;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.fund.TrivialFund;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Parameter;
import eu.crisis_economics.abm.simulation.injection.factories.FundFactory;

/**
  * An implementation of the {@link AbstractFundSubEconomy} class.<br><br>
  * 
  * This {@link AbstractFundSubEconomy} is interchangeable with other instances.
  * This implementation specifies a subeconomy of {@link Fund}{@code s} consisting
  * of a custom number of {@link ExogenousFund} {@link Agent}{@code s}.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   Description = "A simple subeconomy providing exogenous funds with limited market interactions. "
               + "Exogenous funds may or may not hold their deposits exogenously, however each "
               + "exogenous fund is subject to an exogenous cash flow stream. Exogenous funds "
               + "have customizable unmodelled cash flows. These cash flows are bidirectional "
               + "(custom exogenous expenses or custom exogenous cash income)."
   )
public final class ExogenousFundSubEconomy extends AbstractFundSubEconomy {
   
   private static final long serialVersionUID = -2842561142408593781L;
   @Layout(
      Order = 1,
      FieldName = "Exogenous Cash Flow"
      )
   @Submodel
   @Parameter(
      ID = "EXOGENOUS_FUND_MANUAL_CASH_FLOW"
      )
   TimeseriesDoubleModelParameterConfiguration
      fundExogenousCashFlows = new FromFileTimeseriesDoubleModelParameterConfiguration();
   
   public TimeseriesDoubleModelParameterConfiguration getFundExogenousCashFlows() {
      return fundExogenousCashFlows;
   }
   
   public void setFundExogenousCashFlows(
      final TimeseriesDoubleModelParameterConfiguration fundExogenousCashFlows) {
      this.fundExogenousCashFlows = fundExogenousCashFlows;
   }
   
   @Override
   protected void addBindings() {
      install(new FactoryModuleBuilder()
         .implement(Fund.class, TrivialFund.class)
         .build(Key.get(
            FundFactory.class, Names.named("EXOGENOUS_FUND_IMPLEMENTATION_FACTORY")))
          );
      install(new FactoryModuleBuilder()
         .implement(Fund.class, ExogenousFund.class)
         .build(FundFactory.class)
         );
      expose(FundFactory.class);
   }
}
