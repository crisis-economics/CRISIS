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
package eu.crisis_economics.abm.algorithms.matching;

/**
  * A common data structure for matching algorithms. Given a 
  * group of market participants X, a Node collection represents:
  *   (a) the objects X themselves, via references,
  *   (b) the volume (quantity) each member of X desires form the
  *       market; and
  *   (c) the price per unit each member of X is willing to pay.
  * @author phillips
  */
public interface Node {
   /**
     * Get the price per unit that the underlying participant is willing 
     * to pay.
     */
   public double getPricePerUnit();
   
   /**
     * Get the volume (total quantity) that the underlying participant
     * wants to trade.
     */
   public double getVolume();
   
   /**
     * Get an object reference to the actual market participant.
     */
   public Object getObjectReference();
}