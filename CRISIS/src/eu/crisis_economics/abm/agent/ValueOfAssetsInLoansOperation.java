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

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.loans.Loan;

/**
  * A simple {@link AgentOperation}{@code <Double>} computing
  * the total existing loan asset value for an {@link Agent}.
  * 
  * @author phillips
  */
public final class ValueOfAssetsInLoansOperation
   extends SimpleAbstactAgentOperation<Double> {
   @Override
   public Double operateOn(final Agent agent) {
      double result = 0.;
      for(final Contract contract : agent.getAssets())
         if(contract instanceof Loan)
            result += contract.getValue();
      return result;
   }
   
   static public double on(final Agent agent) {
      return agent.accept(new ValueOfAssetsInLoansOperation());
   }
}
