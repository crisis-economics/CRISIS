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
package eu.crisis_economics.abm.markets.clearing.heterogeneous;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;

/**
  * A simple implementation of a composite {@link ClearingMarketsResponseFunctionFactory}.
  * 
  * @author phillips
  */
public class CompositeMarketResponseFunctionFactory implements
   ClearingMarketsResponseFunctionFactory,
   Map<String, AgentOperation<MarketResponseFunction>>
   {
   
   private Map<String, AgentOperation<MarketResponseFunction>>
      responses;
   
   /**
     * Create a {@link CompositeMarketResponseFunctionFactory} object.
     */
   public CompositeMarketResponseFunctionFactory() {
      this.responses = new HashMap<String, AgentOperation<MarketResponseFunction>>();
   }
   
   @Override
   public MarketResponseFunction create(
      final Agent participant,
      final ClearingMarketInformation market
      ) {
      if(responses.containsKey(market.getMarketName()))
         return participant.accept(responses.get(market.getMarketName()));
      return null;
   }
   
   @Override
   public void clear() {
      responses.clear();
   }
   
   @Override
   public boolean containsKey(final Object key) {
      return responses.containsKey(key);
   }
   
   @Override
   public boolean containsValue(final Object value) {
      return responses.containsValue(value);
   }
   
   @Override
   public Set<Map.Entry<String, AgentOperation<MarketResponseFunction>>> entrySet() {
      return responses.entrySet();
   }
   
   @Override
   public boolean equals(final Object o) {
      return responses.equals(o);
   }
   
   @Override
   public AgentOperation<MarketResponseFunction> get(final Object key) {
      return responses.get(key);
   }
   
   @Override
   public int hashCode() {
      return responses.hashCode();
   }
   
   @Override
   public boolean isEmpty() {
      return responses.isEmpty();
   }
   
   @Override
   public Set<String> keySet() {
      return responses.keySet();
   }
   
   @Override
   public AgentOperation<MarketResponseFunction> put(
      final String key,
      AgentOperation<MarketResponseFunction> value
      ) {
      return responses.put(key, value);
   }
   
   @Override
   public void putAll(
      final Map<? extends String,
         ? extends AgentOperation<MarketResponseFunction>> m) {
      responses.putAll(m);
   }
   
   @Override
   public AgentOperation<MarketResponseFunction> remove(final Object key) {
      return responses.remove(key);
   }
   
   @Override
   public Collection<AgentOperation<MarketResponseFunction>> values() {
      return responses.values();
   }
   
   @Override
   public int size() {
      return responses.size();
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Composite MarketResponse Function Factory with " + size() + " elements.";
   }
}
