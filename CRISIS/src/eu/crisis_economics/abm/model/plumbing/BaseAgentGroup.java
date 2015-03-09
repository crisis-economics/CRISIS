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
   package eu.crisis_economics.abm.model.plumbing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.crisis_economics.abm.model.Scoped;
import eu.crisis_economics.abm.model.SimpleScoped;

/**
  * A skeletal implementation of the {@link AgentGroup} interface.
  * 
  * @author phillips
  */
public class BaseAgentGroup implements AgentGroup {
   
   private static final long serialVersionUID = 4285053919072415040L;
   
   private Map<Class<?>, List<Object>>
      populance,
      marketStructures;
   
   private AgentGroup
      container;
   
   private List<AgentGroup>
      nodes;
   
   private Scoped
      scoped;
   
   /**
    * Create an {@link BaseConfigurationComponent} object with no specified
    * container {@link AgentGroup} and no specified scope.
    */
   public BaseAgentGroup() {
      this.populance = new HashMap<Class<?>, List<Object>>();
      this.marketStructures = new HashMap<Class<?>, List<Object>>();
      this.nodes = new ArrayList<AgentGroup>();
      this.scoped = new SimpleScoped();
   }
   
   /**
     * Get the collection of {@link Agent}{@code s} in this subeconomy.
     * Removing or adding elements to the returned collection will have
     * no effect on this {@link AgentGroup}.
     */
   @Override
   public Map<Class<?>, List<Object>> getAgents() {
      final Map<Class<?>, List<Object>>
         result = new HashMap<Class<?>, List<Object>>(populance);
      for(final AgentGroup component : nodes) {
         final Map<Class<?>, List<Object>>
            subModelAgents = component.getAgents();
         for(final Entry<Class<?>, List<Object>> record : subModelAgents.entrySet()) {
            if(!result.containsKey(record.getKey()))
                result.put(record.getKey(), new ArrayList<Object>());
            result.get(record.getKey()).addAll(record.getValue());
         }
      }
      return result;
   }
   
   /**
     * Get the collection of {@link Market} structures in this subeconomy.
     * Removing or adding elements to the returned collection will have
     * no effect on this {@link AgentGroup}.
     */
   @Override
   public Map<Class<?>, List<Object>> getMarkets() {
      final Map<Class<?>, List<Object>>
      result = new HashMap<Class<?>, List<Object>>(marketStructures);
      for(final AgentGroup component : nodes) {
         final Map<Class<?>, List<Object>>
            subModelAgents = component.getMarkets();
         for(final Entry<Class<?>, List<Object>> record : subModelAgents.entrySet()) {
            if(!result.containsKey(record.getKey()))
               result.put(record.getKey(), new ArrayList<Object>());
            result.get(record.getKey()).addAll(record.getValue());
         }
      }
      return result;
   }
   
   /**
     * Record the existence of an agent in this {@link AgentGroup}.
     * If the argument is {@code null}, no action is taken. If this method
     * is called twice with the same argument, duplicate elements will be
     * recorded.
     */
   protected void addAgent(final Object element) {
      if(element == null) return;
      Class<?> token = element.getClass();
      if(populance.get(token) == null)
         populance.put(token, new ArrayList<Object>());
      populance.get(token).add(element);
   }
   
   /**
     * Record the existence of a market structure in this {@link AgentGroup}.
     * If the argument is {@code null}, no action is taken. If this method
     * is called twice with the same argument, duplicate elements will be
     * recorded.
     */
   public void addMarketStructure(final Object element) {
      if(element == null) return;
      Class<?> token = element.getClass();
      if(marketStructures.get(token) == null)
         marketStructures.put(token, new ArrayList<Object>());
      marketStructures.get(token).add(element);
   }
   
   /**
     * Get the {@link Agent} population of this {@link AgentGroup}.
     */
   @Override
   public int getPopulation() {
      int size = 0;
      for(final Entry<Class<?>, List<Object>> record : populance.entrySet())
         size += record.getValue().size();
      for(final AgentGroup component : nodes)
         size += component.getPopulation();
      return size;
   }
   
   /**
     * Get the number of distinct markets in this {@link AgentGroup}.
     */
   @Override
   public int getNumberOfMarkets() {
      int size = 0;
      for(final Entry<Class<?>, List<Object>> record : marketStructures.entrySet())
         size += record.getValue().size();
      for(final AgentGroup component : nodes)
         size += component.getNumberOfMarkets();
      return size;
   }
   
   /**
     * See {@link AgentGroup.getAgentsOfType(Class<T> token)}.
     */
   @SuppressWarnings("unchecked")
   public <T> List<T> getAgentsOfType(final Class<T> token) {
      final List<T>
         result = new ArrayList<T>();
      for(final Entry<Class<?>, List<Object>> record : populance.entrySet())
         if(token.isAssignableFrom(record.getKey()))
            for(final Object obj : record.getValue())
               result.add((T) obj);
      for(final AgentGroup component : nodes)
         result.addAll(component.getAgentsOfType(token));
      return result;
   }
   
   /**
    * See {@link AgentGroup.getAgentsSatisfying(Class<?>... tokens)}.
    */
   public List<Object> getAgentsSatisfying(Class<?>... tokens) {
      final List<Object>
         result = new ArrayList<Object>();
      for(final Entry<Class<?>, List<Object>> record : populance.entrySet()) {
         boolean
            isValid = true;
         for(final Class<?> token : tokens)
            if(!token.isAssignableFrom(record.getKey())) {
               isValid = false;
               break;
            }
         if(isValid)
            result.addAll(record.getValue());
      }
      return result;
   }
   
   /**
     * See {@link AgentGroup.getMarketsOfType(Class<T> token)}.
     */
   @SuppressWarnings("unchecked")
   public <T> List<T> getMarketsOfType(final Class<T> token) {
      final List<T>
         result = new ArrayList<T>();
      for(final Entry<Class<?>, List<Object>> record : marketStructures.entrySet())
         if(token.isAssignableFrom(record.getKey()))
            for(final Object obj : record.getValue())
               result.add((T) obj);
      for(final AgentGroup component : nodes)
         result.addAll(component.getAgentsOfType(token));
      return result;
   }
   
   @Override
   public final AgentGroup getContainer() {
      return container;
   }
   
   @Override
   public final void setContainer(AgentGroup container) {
      this.container = container;
   }
   
   public String getScopeString() {
      return scoped.getScopeString();
   }
   
   public void setScope(final String scope) {
      scoped.setScope(scope);
   }
   
   public void setScope(
      final String scope,
      final Scoped parent
      ) {
      scoped.setScope(scope, parent);
   }

   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      String result =
         "Abstract Configuration Component, population: " + getPopulation() + ", "
       + "number of market structures: " + getNumberOfMarkets() + ".\n Agent Composition: \n";
      for(final Entry<Class<?>, List<Object>> record : populance.entrySet())
         for(final Object obj : record.getValue())
            result += String.format("agent: [%s] %s\n",
               obj.getClass().getSimpleName(), obj.toString());
      result += "\nMarket Structure Composition:\n";
      for(final Entry<Class<?>, List<Object>> record : marketStructures.entrySet())
         for(final Object obj : record.getValue())
            result += String.format("market structure: [%s] %s\n", obj.getClass(), obj.toString());
      return result;
   }
}