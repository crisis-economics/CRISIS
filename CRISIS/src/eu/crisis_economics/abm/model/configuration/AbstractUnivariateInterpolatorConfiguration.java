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

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import com.google.inject.Key;
import com.google.inject.name.Names;

/**
  * An abstract base class for {@link UnivariateInterpolator} configuration components.<br><br>
  * Implementations of this {@link ComponentConfiguration} must provide bindings for
  * the following types:
  * 
  * <ul>
  *   <li> {@link UnivariateInterpolator}{@code .class} annotated with 
  *        {@link #getScopeString()}.
  * </ul>
  * 
  * @author phillips
  */
public abstract class AbstractUnivariateInterpolatorConfiguration
   extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = -3429600096780768313L;
   
   /**
     * Create a {@link AbstractUnivariateInterpolatorConfiguration} object.<br><br>
     * 
     * Implementations of this type must expose the following bindings:
     * <ul>
     *   <li> {@link UnivariateInterpolator}{@code .class} annotated with 
     *        {@link #getScopeString()}.
     * </ul>
     */
   protected AbstractUnivariateInterpolatorConfiguration() { }
   
   @Override
   protected final void setRequiredBindings() {
      requireBinding(Key.get(UnivariateInterpolator.class, Names.named(getScopeString())));
      expose(Key.get(UnivariateInterpolator.class, Names.named(getScopeString())));
   }
}
