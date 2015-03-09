/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
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
package eu.crisis_economics.abm.agent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.model.parameters.ModelParameter;

/**
  * An implementation of the {@link AbstractAgentNameFactory} class. This
  * implementation uses a {@link Parameter}{@code <String>} to generate
  * names.
  * 
  * @author phillips
  */
public final class ModelParameterAgentNameFactory implements AgentNameFactory {
   private final
      ModelParameter<String> names;
   
   @Inject
   public ModelParameterAgentNameFactory(
   @Named("AGENT_NAMES_MODEL_PARAMETER")
      final ModelParameter<String> names
      ) {
      this.names = Preconditions.checkNotNull(names);
   }
   
   @Override
   public String generateNameFor(final Agent agent) {
      return names.get();
   }
}
