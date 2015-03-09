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

import eu.crisis_economics.abm.bank.bankruptcy.BankBankruptyHandler;
import eu.crisis_economics.abm.bank.bankruptcy.LiquidateBankruptcyResolution;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A model GUI configuration component for a {@link Bank} liquidation 
  * bankruptcy resolution policy.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   Description =
     "Liquidate Bank Bankruptcy Policy.\n\n"
   + "This policy requires the Central Bank and Bad Bank to be enabled.\n\n"
   + "When a bank is liquidated, it is closed and lost from the simulation.  The bank's "
   + "assets are "
   + "sold over time to pay its liabilities to depositors or other creditors. This "
   + "is modelled by transferring the institution's assets to the bad bank agent (see "
   + "component "
   + "in the 'Configure Banking Sub Economy' menu for more information and relevant "
   + "parameters) and receiving immediately the funds equaling the face value of the sold"
   + " assets.  These funds are then used to pay off the institution's debtors as far "
   + "as possible. Secured debtors (e.g. through repurchase agreements) take "
   + "priority, the remaining funds are distributed pro rata among unsecured "
   + "debtors. The negative equity of the bank leads to a capital loss of the "
   + "bank's debtors and depositors. Households, firms and funds, who held their deposits "
   + "at the liquidated bank, open up a new deposit account at a randomly chosen bank.",
   DisplayName = "Liquidate"
   )
public final class LiquidateBankBankruptcyPolicyConfiguration
   extends AbstractBankBankruptcyPolicyConfiguration
   implements BankBankruptcyPolicyConfiguration {
   
   private static final long serialVersionUID = 7029659381132248071L;
   
   @Override
   protected void addBindings() {
      super.addBindings();
      bind(BankBankruptyHandler.class).to(LiquidateBankruptcyResolution.class);
      expose(BankBankruptyHandler.class);
   }
}