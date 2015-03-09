/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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

import java.util.List;

import eu.crisis_economics.abm.contracts.Contract;

/**
  * Classes implementing this interface should handle a list of assets.
  * 
  * @author Tamás Máhr
  */
public interface HasAssets {
   
   /**
    * Adds a new asset to the object's list of assets.
    * 
    * @param asset
    *           the new asset to add
    */
   public void addAsset(Contract asset);
   
   /**
    * Removes the provided asset from the object's list of assets. If the asset
    * is not an asset of the object, this method does nothing.
    * 
    * @param asset
    *           the asset to remove
    * @return <code>true</code> if the list of assets was modified during the
    *         call (i.e. the list contained the given asset), and
    *         <code>false</code> otherwise
    */
   public boolean removeAsset(Contract asset);
   
   /**
    * Returns an (<i>unmodifiable</i>) view of the assets owned.
    * 
    * Note that the return value is an unmodifiable <i>container</i>, not an
    * unmodifiable collection of <i>unmodifiable contacts</i>. It is permitted
    * to modify contracts in the return value, but it is not permitted to remove
    * entries.
    * 
    * This method copies the underlying contract references before constructing
    * an unmodifiable view of the balance sheet. Thus, if you terminate a
    * contract by modifying it, ConcurrentModificationException will not be
    * raised.
    * 
    * @return list of owned contracts; an <i>empty list</i> otherwise
    */
   public List<Contract> getAssets();
}
