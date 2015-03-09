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

import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.weighting.HomogeneousPortfolioWeighting;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.PortfolioWeighting;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A model configuration component for the {@link HomogeneousPortfolioWeighting}
  * portfolio weighting algorithm.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   Description =
      "For the Homogeneous Portfolio Weighting algorithm, all known instruments receive the "
    + "same investment weight.",
   DisplayName = "Homogeneous Investment"
   )
public final class HomogeneousPortfolioWeightingConfiguration 
   extends AbstractPublicConfiguration
   implements PortfolioWeightingAlgorithmConfiguration {
   
   private static final long serialVersionUID = -8235895514526324953L;
   
   @Override
   protected void addBindings() {
      bind(
         PortfolioWeighting.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(HomogeneousPortfolioWeighting.class);
   }
}
