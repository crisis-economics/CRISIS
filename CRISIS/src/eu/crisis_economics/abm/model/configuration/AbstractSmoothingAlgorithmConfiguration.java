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

import com.google.inject.Key;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.algorithms.portfolio.smoothing.SmoothingAlgorithm;

/**
  * An abstract base class for {@link SmoothingAlgorithm} configuration components.<br><br>
  * Implementations of this class must provide bindings for the following:
  * 
  * <ul>
  *   <li> {@link SmoothingAlgorithm}{@code .class}. If {@link #getScopeString()} is nonempty,
  *        this binding should be annotated with {@link #getScopeString()}.
  * </ul>
  * 
  * @author phillips
  */
public abstract class AbstractSmoothingAlgorithmConfiguration
   extends AbstractPrivateConfiguration {
   
   private static final long serialVersionUID = -2589975343225003419L;
   
   /**
     * Create a {@link AbstractSmoothingAlgorithmConfiguration} object.<br><br>
     * 
     * See also {@link AbstractSmoothingAlgorithmConfiguration}.
     */
   protected AbstractSmoothingAlgorithmConfiguration() { }
   
   @Override
   protected final void setRequiredBindings() {
      if(getScopeString().isEmpty()) {
         requireBinding(SmoothingAlgorithm.class);
         expose(SmoothingAlgorithm.class);
      }
      else {
         requireBinding(Key.get(SmoothingAlgorithm.class, Names.named(getScopeString())));
         expose(Key.get(SmoothingAlgorithm.class, Names.named(getScopeString())));
      }
   }
}
