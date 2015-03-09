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
package eu.crisis_economics.abm.ratings;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.SimpleAbstactAgentOperation;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;

/**
  * A simple visitor measurement returning the stock price per share
  * of an {@link Agent}. If the {@link Agent} is not a {@link StockReleaser},
  * the stock price per share is zero.
  * 
  * @author phillips
  */
public final class AgentStockPricePerShareMeasurement 
   extends SimpleAbstactAgentOperation<Double> implements AgentTrackingMeasurement {
   
   public AgentStockPricePerShareMeasurement() { } // Stateless
   
   /**
     * Return the price per share of the visited {@link Agent}. If the 
     * {@link Agent} is not a {@link StockReleaser}, the stock price 
     * per share measurement is zero.
     */
   @Override
   public Double operateOn(final Agent agent) {
      return UniqueStockExchange.Instance.getStockPrice(agent.getUniqueName());
   }
}
