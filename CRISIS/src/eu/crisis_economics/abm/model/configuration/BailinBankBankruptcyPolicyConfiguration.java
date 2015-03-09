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
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.bankruptcy.BailinBankruptcyResolution;
import eu.crisis_economics.abm.bank.bankruptcy.BankBankruptyHandler;
import eu.crisis_economics.abm.bank.bankruptcy.ComputeCARBankruptcyResolutionAmountOperation;
import eu.crisis_economics.abm.intermediary.SingleBeneficiaryIntermediaryFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A model GUI configuration component for a {@link Bank} bail-in 
  * bankruptcy resolution policy.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   Description = "Bail-in bank bankruptcy resolution.\n\n"
         + "A bail-in is a balance sheet restructuring of an insolvent bank. "
         + "Bank liabilities such as loans and deposits are converted into equity. To implement "
         + "this debt-to-equity conversion, we compute the size of the bail-in as the negative "
         + "equity of the failing bank, plus an overhead ensuring the minimum capital adequacy "
         + "(CAR) ratio is restored and that the bank can continue operations after the bail-in. "
         + "The resources necessary for a bail-in are subtracted from each "
         + "deposit or loan pro rata and converted into capital."
         + "\n\nThe bail-in resolution technique does not depend on external funding,"
         + "e.g. government cash. Note that bank liabilities may include deposit "
         + "accounts, so bail-in resolution policies may result in depositor losses."
         + "\n\nThe bail-in will be processed as follows: "
         + "\n(a) terminate all stock accounts in this bank, "
         + "including, potentially, shares owned by this bank in "
         + "itself; "
         + "\n(b) compute the amount required to restore the bank balance sheet to the "
         + "specified minimum capital adequacy ratio level; "
         + "\n(c) write down loan liabilities, in equal proportions, until either "
         + "(i.) no further loan debt can be written down, or (ii.) the sum in (b) "
         + "has been completely deducted from loan debt; "
         + "\n(d) if necessary, continue to write down deposit account liabilities, in " 
         + "equal proportions, until either (i.) no further deposit debt can be written "
         + "down, or (ii.) the sum in (b) has been completely deducted from deposit " 
         + "account debts; "
         + "\n(e) issue new shares to all creditors who have suffered losses. The number "
         + "of shares received by each creditor is proportional to the size of the loss "
         + "suffered during steps (c) and (d). "
         + "\n\nIf this bank no outstanding shares at the time " 
         + "this procedure is initiated, then 1.0 units of new shares, in total, will "
         + "be created for creditors who have suffered losses. " 
         + "If this bank cannot satisfy the CAR level computed "
         + "in (b) by writing down loan and deposit account liabilities, then "
         + "this bank will be scheduled for liquidation. ",
   DisplayName = "Bailin"
   )
public final class BailinBankBankruptcyPolicyConfiguration
   extends AbstractBankBankruptcyPolicyConfiguration
   implements BankBankruptcyPolicyConfiguration {
   
   private static final long serialVersionUID = -2535392472245836389L;
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(BankBankruptyHandler.class).to(BailinBankruptcyResolution.class);
      bind(new TypeLiteral<AgentOperation<Double>>(){})
         .annotatedWith(Names.named("BANK_BAILIN_RESOLUTION_EQUITY_ADJUSTMENT"))
         .to(ComputeCARBankruptcyResolutionAmountOperation.class);
      install(new FactoryModuleBuilder()
         .build(SingleBeneficiaryIntermediaryFactory.class)
         );
      expose(BankBankruptyHandler.class);
   }
}