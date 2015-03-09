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
package eu.crisis_economics.abm.household;

import java.util.List;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.markets.GoodsBuyer;

/**
  * A lightweight interface for domestic {@link Agent}{@code s} who consume goods.
  * This interface provides methods to control the types of goods consumed by the
  * implementing {@link Agent}.
  * 
  * @author phillips
  */
public interface DomesticGoodsConsumer extends GoodsBuyer {
   /**
     * Signal that this {@link DomesticGoodsConsumer} should consider goods of
     * the specified type.
     * 
     * @param type
     *        The type of goods to consider for consumption.
     */
   public void addGoodToConsider(final String type);
   
   /**
     * Signal that this {@link DomesticGoodsConsumer} should consider goods of
     * the specified type. This method is equivalent to a sequence of calls
     * to {@link addGoodToConsider}.
     * 
     * @param type
     *        A list of types to consider for consumption.
     */
   public void addGoodsToConsider(final List<String> types);
   
   /**
     * Signal that this {@link DomesticGoodsConsumer} should not consider goods
     * of the specified type.
     * 
     * @param type
     *        The type of goods to not consider for consumption.
     */
   public void removeGoodToConsider(final String type);
   
   /**
     * Signal that this {@link DomesticGoodsConsumer} should not consider goods
     * of the specified types. This method is equivalent to a sequence of calls to
     * {@link #removeGoodsToConsider(String)}.
     * 
     * @param type
     *        A list of types to not consider for consumption.
     */
   public void removeGoodsToConsider(final List<String> types);
   
   /**
     * Get a list of types of goods considered by this {@link DomesticGoodsConsumer}.
     * Adding or removing elements from the returned collection will not affect this
     * object.
     */
   public List<String> getGoodsTypesConsidered();
}