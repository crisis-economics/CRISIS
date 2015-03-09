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

import com.google.inject.Singleton;

import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.model.Parameter;

/**
  * A model configuration component for the {@link SimpleLabourMarket} class.
  * 
  * @author phillips
  */
public final class LabourMarketConfiguration extends AbstractPublicConfiguration {
   
   private static final long serialVersionUID = -8746269533142353676L;
   
   @Submodel
   @Parameter(
      ID = "SIMPLE_LABOUR_MARKET_MATCHING_ALGORITHM"
      )
   private MarketMatchingAlgorithmConfiguration
      employeeEmployerMatchingAlgorithm = new ForagerMatchingAlgorithmConfiguration();
   
   public MarketMatchingAlgorithmConfiguration
      getEmployeeEmployerMatchingAlgorithm() {
      return employeeEmployerMatchingAlgorithm;
   }
   
   public void setEmployeeEmployerMatchingAlgorithm(
      final MarketMatchingAlgorithmConfiguration employeeEmployerMatchingAlgorithm) {
      this.employeeEmployerMatchingAlgorithm = employeeEmployerMatchingAlgorithm;
   }
   
   @Override
   protected void addBindings() {
      bind(SimpleLabourMarket.class).in(Singleton.class);
   }
}
