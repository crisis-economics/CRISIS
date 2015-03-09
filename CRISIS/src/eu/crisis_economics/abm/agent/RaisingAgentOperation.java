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
  * A simple implementation of the {@link AgentOperation} interface. This 
  * implementation is designed for inheritance. When inheriting from this 
  * type, override any or all of the methods below with your own
  * implementation. Any method not overridden by your implementation will raise 
  * {@link UnsupportedOperationException}.
  * 
  * @author phillips
  */
public class RaisingAgentOperation<T> implements AgentOperation<T> {
   @Override
   public T operateOn(Bank bank) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on Bank agents.");
   }
   
   public T operateOn(ClearingBank bank) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on ClearingBank agents.");
   }
   
   public T operateOn(CommercialBank bank) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on CommercialBank agents.");
   }
   
   @Override
   public T operateOn(Firm firm) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on Firm agents.");
   }
   
   @Override
   public T operateOn(Household household) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on Household agents.");
   }
   
   @Override
   public T operateOn(Government government) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on Government agents.");
   }
   
   @Override
   public T operateOn(Fund fund) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on Fund agents.");
   }
   
   @Override
   public T operateOn(PortfolioFund fund) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on PortfolioFund agents.");
   }
   
   @Override
   public T operateOn(Intermediary intermediary) {
      throw new UnsupportedOperationException(
         "AgentOperation.operate: this implementation cannot operate on Intermediary agents.");
   }
   
   @Override
   public T operateOn(final Object object) {
      throw new UnsupportedOperationException(
         "AgentOperation.opeate: the object " + object + " is not a recognized agent type.");
   }
}
