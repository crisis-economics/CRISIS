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

import org.apache.commons.math3.distribution.RealDistribution;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.model.configuration.AbstractPrimitiveParameterConfiguration
   .AbstractDoubleModelParameterConfiguration;
import eu.crisis_economics.abm.model.parameters.FromRealDistributionModelParameter;
import eu.crisis_economics.abm.model.parameters.ModelParameter;

/**
  * An abstract base class for {@link Double} {@link Parameter} configuration
  * components. Implementations of this class must provide a binding for the 
  * {@link RealDistribution} class.
  * 
  * @author phillips
  */
public abstract class AbstractDistributionModelParameterConfiguration
   extends AbstractDoubleModelParameterConfiguration {
   
   private static final long serialVersionUID = 8339262293448382231L;
   
   @Override
   protected final void setRequiredBindings() {
      super.setRequiredBindings();
      requireBinding(RealDistribution.class);
   }
   
   /**
     * When overriding this method, call {@code super.}{@link #addBindings()} as 
     * a final instruction.
     */
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(String.class)
         .toInstance(getScopeString());
      bind(
         getScopeString().isEmpty() ? 
            Key.get(new TypeLiteral<ModelParameter<Double>>(){}) :
            Key.get(new TypeLiteral<ModelParameter<Double>>(){}, Names.named(getScopeString())))
         .to(FromRealDistributionModelParameter.class);
      expose(
         getScopeString().isEmpty() ? 
            Key.get(new TypeLiteral<ModelParameter<Double>>(){}) :
            Key.get(new TypeLiteral<ModelParameter<Double>>(){}, Names.named(getScopeString()))
         );
   }
}
