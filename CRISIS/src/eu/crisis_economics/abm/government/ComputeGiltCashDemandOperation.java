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
package eu.crisis_economics.abm.government;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.agent.RaisingAgentOperation;

/**
  * A lightweight {@link AgentOperation}{@code <Double>} returning the gilt
  * (bond credit) demand for {@link Government} {@link Agent}{@code s}. If
  * the visited {@link Agent} is not a {@link Government} {@link Agent}, this
  * operation raises {@link UnsupportedOperationException}.
  * 
  * @author phillips
  */
public final class ComputeGiltCashDemandOperation extends RaisingAgentOperation<Double> {
   @Override
   public Double operateOn(final Government government) {
      return government.getMaximumGiltCashDemand();
   }
   
   /**
     * Apply {@link ComputeGiltCashDemandOperation} to an {@link Agent} as a static method.
     */
   public static double on(Agent agent) {
      return agent.accept(new ComputeGiltCashDemandOperation());
   }
}
