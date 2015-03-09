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

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * An implementation of the {@link AbstractUnivariateInterpolatorConfiguration}
  * {@link ComponentConfiguration}.<br><br>
  * 
  * This module binds instances of {@link UnivariateInterpolator} to the 
  * {@link SplineInterpolator} implementation.
  */
@ConfigurationComponent(
   DisplayName = "Spline"
   )
public final class SplineUnivariateInterpolatorConfiguration
   extends AbstractUnivariateInterpolatorConfiguration {
   
   private static final long serialVersionUID = 6691568632724865798L;
   
   @Override
   protected void addBindings() {
      bind(UnivariateInterpolator.class)
         .annotatedWith(Names.named(getScopeString()))
         .to(SplineInterpolator.class);
   }
}
