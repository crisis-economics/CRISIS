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

import java.util.List;
import java.util.Map;

import eu.crisis_economics.abm.model.Scoped;

public interface AgentGroup extends Scoped {
   /**
     * Get the collection of {@link Agent}{@code s} in this subeconomy.
     * Removing or adding elements to the returned collection will have
     * no effect on this {@link AgentGroup}.
     */
   public Map<Class<?>, List<Object>> getAgents();
   
   /**
     * Get a list of {@link Agent}{@code s} in this subeconomy with the 
     * specified type. Removing or adding elements to the returned collection
     * will have no effect on this {@link AgentGroup}.
     */
   public <T> List<T> getAgentsOfType(Class<T> token);
   
   /**
     * Get a list of {@link Agent}{@code s} in this subeconomy which satisfy
     * all of the stated types. Removing or adding elements to the returned
     * collection will have no effect on this {@link AgentGroup}. It is safe
     * to cast every element in the returned {@link List} to any type in 
     * the argument list.
     */
   public List<Object> getAgentsSatisfying(Class<?>... tokens);
   
   /**
     * Get the collection of {@link Market} structures in this subeconomy.
     * Removing or adding elements to the returned collection will have
     * no effect on this {@link AgentGroup}.
     */
   public Map<Class<?>, List<Object>> getMarkets();
   
   /**
     * Get a list of {@link Markets}{@code s} in this subeconomy with the 
     * specified type. Removing or adding elements to the returned collection
     * will have no effect on this {@link AgentGroup}.
     */
   public <T> List<T> getMarketsOfType(Class<T> token);
   
   /**
     * Get the {@link Agent} population of this {@link AgentGroup}.
     */
   public int getPopulation();
   
   /**
     * Get the number of distinct markets in this {@link AgentGroup}.
     */
   public int getNumberOfMarkets();
   
   /**
     * Get the broader {@link AgentGroup} to which this subeconomy
     * belongs. It is acceptable for the return value of this method to
     * be {@code null}. If this method returns {@code null}, then this 
     * {@link AgentGroup} is a highest level economy component with
     * no parent.
     */
   AgentGroup getContainer();
   
   /**
     * Set the broader {@link AgentGroup} to which this subeconomy
     * belongs.
     * 
     * @param component
     *        The containing {@link AgentGroup}. It is acceptable for
     *        the argument to be {@code null}.
     */
   void setContainer(AgentGroup component);
}
