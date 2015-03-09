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
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.ClearingBank;
import eu.crisis_economics.abm.bank.CommercialBank;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.fund.Fund;
import eu.crisis_economics.abm.fund.PortfolioFund;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.intermediary.Intermediary;

/**
  * A simple implementation of the AgentOperation<T> interface. This
  * implementation:
  *  (a) raises UnsupportedOperationException if the visited agent is of
  *      an unknown type, or otherwise
  *  (b) treats all visited agents equally, by delegating to
  *      acceptOperation(Agent).
  * @author phillips
  * @param <T>
  *        The return type of the the operation.
  */
public abstract class SimpleAbstactAgentOperation<T> implements AgentOperation<T> {

   @Override
   public final T operateOn(Bank bank) {
      return operateOn((Agent) bank);
   }
   
   @Override
   public final T operateOn(ClearingBank bank) {
      return operateOn((Agent) bank);
   }
   
   @Override
   public final T operateOn(CommercialBank bank) {
      return operateOn((Agent) bank);
   }
   
   @Override
   public final T operateOn(Firm firm) {
      return operateOn((Agent) firm);
   }

   @Override
   public final T operateOn(Household household) {
      return operateOn((Agent) household);
   }
   
   @Override
   public final T operateOn(Fund fund) {
      return operateOn((Agent) fund);
   }
   
   @Override
   public final T operateOn(PortfolioFund fund) {
      return operateOn((Agent) fund);
   }
   
   @Override
   public T operateOn(Government government) {
      return operateOn((Agent) government);
   }
   
   @Override
   public T operateOn(Intermediary intermediary) {
      return operateOn((Agent) intermediary);
   }
   
   /**
     * Common visitor logic for known agent types.
     */
   public abstract T operateOn(Agent agent);
   
   @Override
   public T operateOn(final Object object) {
      throw new UnsupportedOperationException(
         "AgentOperation.opeate: the object " + object + " is not a recognized agent type.");
   }
   
}
