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

import eu.crisis_economics.abm.model.ConfigurationComponent;
import eu.crisis_economics.abm.model.Layout;
import eu.crisis_economics.abm.model.plumbing.AbstractPlumbingConfiguration;
import eu.crisis_economics.abm.model.plumbing.FinancialSystemWithMacroeconomy;
import eu.crisis_economics.abm.model.plumbing.Plumbing;

/**
  * A simple implementation of the {@link AbstractPlumbingConfiguration}
  * class. This implementation binds the {@link Plumbing} interface to the
  * {@link FinancialSystemWithMacroEconomy} class.
  * 
  * @author phillips
  */
@ConfigurationComponent(
   DisplayName = "Enable All"
   )
public final class FinancialSystemWithMacroEconomyConfiguration
   extends AbstractPlumbingConfiguration {
   
   private static final long serialVersionUID = 4356980942674877149L;
   
   public final static boolean
      DO_ENABLE_STOCK_TRADING = true;
   
   @Layout(
      FieldName = "Enable Stock Trading?",
      Image = "CRISIS-full.png",
      Order = 0
      )
   private boolean
      doEnableStockTrading = DO_ENABLE_STOCK_TRADING;
   
   public boolean isDoEnableStockTrading() {
      return doEnableStockTrading;
   }
   
   public void setDoEnableStockTrading(
      final boolean doEnableStockTrading) {
      this.doEnableStockTrading = doEnableStockTrading;
   }

   @Override
   protected void addBindings() {
      bind(Plumbing.class).to(FinancialSystemWithMacroeconomy.class).asEagerSingleton();
   }
}
