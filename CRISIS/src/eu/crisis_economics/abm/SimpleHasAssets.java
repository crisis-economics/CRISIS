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
  * Simple implementation of the HasAssets interface.
  * @author phillips
  */
public final class SimpleHasAssets implements HasAssets {
   
   private List<Contract> assets;
   
   public SimpleHasAssets() { 
      this.assets = new ArrayList<Contract>();
   }
   
   @Override
   public void addAsset(final Contract asset) {
      if(asset == null) return;
      assets.add(asset);
   }
   
   @Override
   public boolean removeAsset(Contract asset) {
      if(asset == null) return false;
      return assets.remove(asset);
   }
   
   @Override
   public List<Contract> getAssets() {
      return Collections.unmodifiableList(new ArrayList<Contract>(assets));
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      String result = "Assets (" + assets.size() + ")";
      if(assets.size() == 0) return result + ".";
      for(final Contract contract : assets)
         result += ", " + contract.getClass().getName() + ", value: " + contract.getValue();
      return result + ".";
   }
}
