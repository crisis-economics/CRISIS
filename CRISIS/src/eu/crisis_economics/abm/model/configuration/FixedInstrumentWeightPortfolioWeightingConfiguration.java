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

import eu.crisis_economics.abm.algorithms.portfolio.weighting.FixedInstrumentWeightPortfolioWeighting;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.PortfolioWeighting;
import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.simulation.injection.factories.InjectionFactoryUtils;

/**
  * A model configuration component for the {@link FixedInstrumentWeightPortfolioWeighting}
  * portfolio weighting algorithm.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   Description =
      "For the fixed-instrument weight portfolio division algorithm, all instruments whose"
    + " reference names contain a pattern receive an equal share of a fixed investment weight.",
   DisplayName = "Fixed Inhomogeneous Investment"
   )
public final class FixedInstrumentWeightPortfolioWeightingConfiguration 
   extends AbstractPublicConfiguration
   implements PortfolioWeightingAlgorithmConfiguration {
   
   private static final long serialVersionUID = -7024976317965841428L;
   
   @Layout(
      Order = 0.0,
      FieldName = "Fixed Investment Weight"
      )
   private double
      fixedInvestmentWeight = 0.8;
   
   @Layout(
      Order = 0.1,
      FieldName = "Instrument Names Containing"
      )
   private String
      instrumentNameContains = "Loan";
   
   public double getFixedInvestmentWeight() {
      return fixedInvestmentWeight;
   }
   
   public void setFixedInvestmentWeight(
      final double fixedInvestmentWeight) {
      this.fixedInvestmentWeight = fixedInvestmentWeight;
   }
   
   public String getInstrumentNameContains() {
      return instrumentNameContains;
   }
   
   public void setInstrumentNameContains(
      final String instrumentNameContains) {
      this.instrumentNameContains = instrumentNameContains;
   }
   
   public FixedInstrumentWeightPortfolioWeightingConfiguration() { }
   
   /**
     * Create a {@link FixedInstrumentWeightPortfolioWeightingConfiguration} object with 
     * custom parameters.<br><br>
     * 
     * See also {@link FixedInstrumentWeightPortfolioWeightingConfiguration} and
     * {@link FixedInstrumentWeightPortfolioWeighting}.
     * 
     * @param instrumentNameSearchPattern <br>
     *        The instrument name search pattern to apply. Instruments whose names contain
     *        this search string will receive a homogeneous fraction of the fixed portfolio
     *        weight, <code>W</code>, specified below.
     * @param fixedPortfolioWeight (<code>W</code>)<br>
     *        The fixed portfolio weight to apply. This argument must be non-negative and
     *        less than {@code 1.0}.
     */
   public FixedInstrumentWeightPortfolioWeightingConfiguration(
      final String instrumentNameSearchPattern,
      final double fixedPortfolioWeight
      ) {
      this.instrumentNameContains = Preconditions.checkNotNull(instrumentNameSearchPattern);
      this.fixedInvestmentWeight = fixedPortfolioWeight;
   }
   
   @Override
   protected void addBindings() {
      install(InjectionFactoryUtils.asPrimitiveParameterModule(
         "FIXED_WEIGHT",                     fixedInvestmentWeight,
         "INSTRUMENT_NAME_TO_SEARCH_FOR",    instrumentNameContains
         ));
      bind(
         PortfolioWeighting.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(FixedInstrumentWeightPortfolioWeighting.class);
   }
}
