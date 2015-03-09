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
package eu.crisis_economics.abm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.crisis_economics.abm.contracts.Contract;

/**
  * Simple implementation of the {@link HasLiabilities} interface.
  * 
  * @author phillips
  */
public final class SimpleHasLiabilities implements HasLiabilities {
   
   private List<Contract> liabilities;
   
   public SimpleHasLiabilities() { 
      this.liabilities = new ArrayList<Contract>();
   }
   
   @Override
   public void addLiability(final Contract liability) {
      if(liability == null) return;
      liabilities.add(liability);
   }
   
   @Override
   public boolean removeLiability(Contract liability) {
      if(liability == null) return false;
      return liabilities.remove(liability);
   }
   
   @Override
   public List<Contract> getLiabilities() {
      return Collections.unmodifiableList(new ArrayList<Contract>(liabilities));
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      String result = "Liabilities (" + liabilities.size() + ")";
      if(liabilities.size() == 0) return result + ".";
      for(final Contract contract : liabilities)
         result += ", " + contract.getClass().getName() + ", value: " + contract.getValue();
      return result + ".";
   }
   
   @Override
   public boolean isBankrupt() {
      return false;     // TODO
   }
   
   @Override
   public void handleBankruptcy() {
      // No action
   }
}
