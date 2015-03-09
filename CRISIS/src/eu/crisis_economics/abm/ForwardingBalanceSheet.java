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
package eu.crisis_economics.abm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.crisis_economics.abm.contracts.Contract;

/**
  * A simple forwarding implementation of the {@link HasBalanceSheet} interface.
  * Due to the structure of existing interfaces, this type:
  *   (a) offers an implementation of @{link isBankrupt} that is equivalent
  *       to: return (getEquity() < 0.);
  *   (b) offers a trivial, empty implementation for {@link handleBankruptcy}.
  * 
  * @author phillips
  */
public final class ForwardingBalanceSheet implements HasBalanceSheet {
   
   private final HasAssets assets;
   private final List<Contract> liabilities;
   
   public ForwardingBalanceSheet() {
      this.assets = new SimpleHasAssets();
      this.liabilities = new ArrayList<Contract>();
   }
   
   @Override
   public void addAsset(Contract asset) {
      assets.addAsset(asset);
   }
   
   @Override
   public boolean removeAsset(Contract asset) {
      return assets.removeAsset(asset);
   }
   
   @Override
   public List<Contract> getAssets() {
      return assets.getAssets();
   }
   
   @Override
   public void addLiability(final Contract liability) {
      if(liability == null) return;
      liabilities.add(liability);
   }
   
   @Override
   public boolean removeLiability(final Contract liability) {
      return liabilities.remove(liability);
   }
   
   @Override
   public List<Contract> getLiabilities() {
      return Collections.unmodifiableList(new ArrayList<Contract>(liabilities));
   }
   
   @Override
   public double getEquity() {
      double equity = 0.;
      for(final Contract asset : assets.getAssets())
         equity += asset.getValue();
      for(final Contract liability : liabilities)
         equity -= liability.getValue();
      return equity;
   }
   
   /**
     * In this implementation, the {@link isBankrupt} method is equivalent to:
     * return (getEquity() < 0.).
     */
   @Override
   public boolean isBankrupt() {
      return (getEquity() < 0.);
   }
   
   /**
     * This method does nothing, and cannot fail, in the current implementation.
     */
   @Override
   public void handleBankruptcy() {
      // No action.
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      String result = assets.toString() + " ";
      result += "Liabilities (" + liabilities.size() + ")";
      if(liabilities.size() == 0) return result;
      for(final Contract contract : liabilities)
         result += ", " + contract.getClass().getName() + ", value: " + contract.getValue();
      return result + ".";
   }
}
