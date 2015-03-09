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

import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.matching.ForagerMatchingAlgorithm;
import eu.crisis_economics.abm.algorithms.matching.MatchingAlgorithm;
import eu.crisis_economics.abm.model.Parameter;

public class ForagerMatchingAlgorithmConfiguration
   extends AbstractPrivateConfiguration
   implements MarketMatchingAlgorithmConfiguration {
   
   private static final long serialVersionUID = 761719169208022964L;
   
   @Submodel
   @Parameter(
      ID = "FORAGER_RATIONING_ALGORITHM"
      )
   private MarketRationingAlgorithmConfiguration 
      supplyDemandRationingAlgorithm = new HomogeneousRationingAlgorithmConfiguration();
      
   public MarketRationingAlgorithmConfiguration getSupplyDemandRationingAlgorithm() {
      return supplyDemandRationingAlgorithm;
   }
   
   public void setSupplyDemandRationingAlgorithm(
      final MarketRationingAlgorithmConfiguration supplyDemandRationingAlgorithm) {
      this.supplyDemandRationingAlgorithm = supplyDemandRationingAlgorithm;
   }
   
   @Override
   protected void addBindings() {
      bind(
         MatchingAlgorithm.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(ForagerMatchingAlgorithm.class);
      expose(
         MatchingAlgorithm.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
