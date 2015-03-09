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

import eu.crisis_economics.abm.algorithms.series.RandomSeries;

/**
  * An abstract base class for {@link RandomSeries} configuration components.
  * 
  * @author phillips
  */
public abstract class AbstractRandomSeriesConfiguration
   extends AbstractPrivateConfiguration  {
   
   /**
     * Create an {@link AbstractRandomSeriesConfiguration} object. Implementations
     * of this type should provide the following bindings:
     * 
     * <ul>
     *   <li> An implementation of {@link RandomSeries}, annotated with the scope string,
     *        if this is present.
     * </ul>
     */
   protected AbstractRandomSeriesConfiguration() { }
   
   @Override
   protected void setRequiredBindings() {
      final String
         scope = super.getScopeString();
      requireBinding(
         scope.isEmpty() ? Key.get(RandomSeries.class) :
            Key.get(RandomSeries.class, Names.named(scope)));
   }
   
   /**
     * When overriding this method, call {@link super#addBindings()} as a last instruction.
     */
   @Override
   protected void addBindings() {
      expose(
         getScopeString().isEmpty() ? Key.get(RandomSeries.class) :
            Key.get(RandomSeries.class, Names.named(getScopeString())));
   }
   
   private static final long serialVersionUID = 440893326009345281L;
}
