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

import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import eu.crisis_economics.abm.firm.bankruptcy.AssetThresholdFirmBankruptcyHandler;
import eu.crisis_economics.abm.firm.bankruptcy.CashCompensationAlgorithm;
import eu.crisis_economics.abm.firm.bankruptcy.DebtProportionStockRedistributionAlgorithm;
import eu.crisis_economics.abm.firm.bankruptcy.FirmBankruptcyHandler;
import eu.crisis_economics.abm.firm.bankruptcy.FractionOfDepositsCashCompensationAlgorithm;
import eu.crisis_economics.abm.firm.bankruptcy.StockRedistributionAlgorithm;
import eu.crisis_economics.abm.intermediary.SingleBeneficiaryIntermediaryFactory;
import eu.crisis_economics.abm.model.ConfigurationComponent;

/**
  * A {@link ComponentConfiguration} for the {@link AssetThresholdFirmBankruptcyHandler}
  * class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Stock Redistribution",
   Description =
      "A firm bankruptcy handler module (firm bankruptcy resolution policy) with stock"
    + " redistribution.\n"
    + "\n"
    + "This implementation assumes that the bankrupt firm is a MacroFirm agent. "
    + "This limitation was inherited from an earlier edition of the codebase, and it "
    + "is hoped that this restriction will be relaxed as soon as possible.\n"
    + "\n"
    + "This algorithm processes a firm bankruptcy event in three ordered steps as follows:"
    + ""
    + "<ul>\n"
    + "<li> Initially, the maximum amount of existing firm liquidity, <code>C</code>, that can be used"
    + " to pay outstanding creditor debts is calculated. Up to <code>C</code> units of "
    + "firm liquidity are paid to creditors, in proportion, according to the outstanding"
    + " debt owed to each creditor. If <code>C</code> is sufficient to settle all outstanding "
    + "creditor debts, this bankruptcy resolution process terminates. Otherwise, "
    + "<li> Should <code>C</code> be insufficient to repay the outstanding creditor debt, "
    + "then this bankruptcy resolution process resorts to a stock redistribution. If the "
    + "value of shares held by non-creditor stockholders is less than the total outstanding"
    + " firm debt, then stocks are redistributed from non-creditors to creditors (without "
    + "compensating non-creditor stockholders) until the value of the outstanding creditor "
    + "debt has been compensated in new shares. When this process is complete, this "
    + "bankruptcy handler terminates. If the value of shares owned by non-creditor stockholders "
    + "is insufficient to compensate the firms' creditors, this bankruptcy resolution "
    + "algorithm proceeds to step 3 as follows:"
    + "<li> the firm is liquidated and all of its existing debts are cancelled. " 
    + "The firm stock instrument is erased. Firm shares are created anew with a"
    + "total stock value equal to the total value of firm assets, and these new shares "
    + "are distributed to the firm creditors in proportion according to the debt owed "
    + "to each creditor."
   )
public final class AssetThresholdFirmBankruptcyHandlerConfiguration
   extends AbstractFirmBankruptcyHandlerConfiguration {
   
   private static final long serialVersionUID = -6418927078007333562L;
   
   @Override
   protected void addBindings() {
      bind(Key.get(CashCompensationAlgorithm.class, Names.named("CASH_COMPENSATION_ALGORITHM")))
         .to(FractionOfDepositsCashCompensationAlgorithm.class);
      bind(Key.get(StockRedistributionAlgorithm.class,
         Names.named("STOCK_REDISTRIBUTION_ALGORITHM")))
         .to(DebtProportionStockRedistributionAlgorithm.class);
      install(new FactoryModuleBuilder()
         .build(SingleBeneficiaryIntermediaryFactory.class));
      if(!getScopeString().isEmpty())
         bind(Key.get(FirmBankruptcyHandler.class, Names.named(getScopeString())))
            .to(AssetThresholdFirmBankruptcyHandler.class);
      else
         bind(FirmBankruptcyHandler.class)
            .to(AssetThresholdFirmBankruptcyHandler.class);
      super.addBindings();
   }
}
