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

import com.google.inject.Singleton;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A {@link ComponentConfiguration} for the {@link InstanceIDAgentNameFactory} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Automatic Naming"
   )
public final class InstanceIDAgentNameFactoryConfiguration
   extends AbstractAgentNameFactoryConfiguration {
   
   private static final long serialVersionUID = 4102294308238741713L;
   
   @Override
   protected void addBindings() {
      bind(AgentNameFactory.class).
         annotatedWith(Names.named(getScopeString()))
         .to(InstanceIDAgentNameFactory.class)
         .in(Singleton.class);
      expose(AgentNameFactory.class)
         .annotatedWith(Names.named(getScopeString()));
   }
}
