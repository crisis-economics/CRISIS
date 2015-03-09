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

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.bankruptcy.BailoutBankruptcyResolution;
import eu.crisis_economics.abm.bank.bankruptcy.BankBankruptyHandler;
import eu.crisis_economics.abm.bank.bankruptcy.ComputeCARBankruptcyResolutionAmountOperation;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A model GUI configuration component for a {@link Bank} bail-out 
  * bankruptcy resolution policy.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   Description =
     "Bail-out bank bankruptcy resolution.\n\n"
   + "A bail-out will lead to a continuation of business for the failing bank.  The "
   + "Government agent collects taxes from other agents (e.g. firms, households, funds, "
   + "other banks) which are used, when required, to re-capitalize troubled banks.  The "
   + "amount of money needed for this process equals the negative equity of the institution"
   + " under consideration, plus an overhead ensuring the minimum capital adequacy (CAR) "
   + "ratio is restored and that the bank can continue operations after the bail-out. If "
   + "there are not enough funds available for a bail-out, the troubled bank will be "
   + "liquidated."
   + "\n\nThe bailout will be processed as follows: "
   + "\n(a) terminate all stock accounts in this bank, "
   + "including, potentially, shares owned by this bank in "
   + "itself; "
   + "\n(b) compute the amount required to restore the bank balance sheet to the "
   + "specified minimum capital adequacy ratio level; "
   + "\n(c) request this sum from the Government agent, in exchange for shares; "
   + "\n(d) if the Government agent is unable, or declines, to pay the sum "
   + "bailout sum, then this resolution policy fails. ",
      DisplayName = "Bailout"
      )
public final class BailoutBankBankruptcyPolicyConfiguration
   extends AbstractBankBankruptcyPolicyConfiguration
   implements BankBankruptcyPolicyConfiguration {
   
   private static final long serialVersionUID = 8795906365591535423L;
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(BankBankruptyHandler.class).to(BailoutBankruptcyResolution.class);
      bind(new TypeLiteral<AgentOperation<Double>>(){})
         .annotatedWith(Names.named("BANK_BAILOUT_RESOLUTION_EQUITY_ADJUSTMENT"))
         .to(ComputeCARBankruptcyResolutionAmountOperation.class);
      expose(BankBankruptyHandler.class);
   }
}