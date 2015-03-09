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
  * Interface for custom visitor operations on agents.
  * @author phillips
  */
public interface AgentOperation<T> {
   public abstract T operateOn(Bank bank);
   
   public abstract T operateOn(ClearingBank bank);
   
   public abstract T operateOn(CommercialBank bank);
   
   public abstract T operateOn(Firm firm);
   
   public abstract T operateOn(Household household);
   
   public abstract T operateOn(Fund fund);
   
   public abstract T operateOn(PortfolioFund fund);
   
   public abstract T operateOn(Government government);
   
   public abstract T operateOn(Intermediary government);
   
   public abstract T operateOn(final Object object);
}