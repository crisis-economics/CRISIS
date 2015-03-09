/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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
package eu.crisis_economics.abm.markets.nonclearing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;

/**
 * Represents relationships between banks in the interbank market
 * 
 * @author Victor Spirin
 */
public class InterbankNetwork {
	private final Map<Party, Set<Party>> adj = new HashMap<Party, Set<Party>>();
	
	/**
     * Adds a new node to the graph.  If the node already exists, this
     * function is a no-op.
     *
     * @param node The node to add.
     * @return Whether or not the node was added.
     */
    private boolean addNode(Party node) {
        /* If the node already exists, don't do anything. */
        if (adj.containsKey(node))
            return false;

        /* Otherwise, add the node with an empty set of outgoing edges. */
        adj.put(node, new HashSet<Party>());
        return true;
    }
	
	// protected
    /**
     * Generates a filter from the network. This allows the network class to be used
     * with a Limit Order Book.
     *
     * @param a Bank, for which we want to generate a filter
     * @return Returns a Limit Order Book filter for the given bank. If the bank is not in the network, returns an empty filter.
     */
	protected Filter generateFilter(Party party) {
		if (!adj.containsKey(party))
			return DefaultFilters.only(party);
		return DefaultFilters.only((Party[]) adj.get(party).toArray());
	}
	
	// public interface
	/**
     * Adds a bilateral relationship between banks 'a' and 'b'.
     * If the banks are not already in the network, also adds them to the network.
     *
     * @param a Bank a
     * @param b Bank b
     */
	public void addRelationship(Party a, Party b) {
		/* Confirm both endpoints exist. */
        if (!adj.containsKey(a))
        	addNode(a);
        if (!adj.containsKey(b))
            addNode(b);

        /* Add the edge in both directions. */
        adj.get(a).add(b);
        adj.get(b).add(a);
	}
	
	/**
     * Removes a bilateral relationship between banks 'a' and 'b'.
     * If the banks are not already in the network, throws a NoSuchElementException
     *
     * @param a Bank a
     * @param b Bank b
     */
	public void removeRelationship(Party a, Party b) {
		/* Confirm both endpoints exist. */
        if (!adj.containsKey(a) || !adj.containsKey(b))
            throw new NoSuchElementException("Both banks must be in the network.");

        /* Remove the edges from both adjacency lists. */
        adj.get(a).remove(b);
        adj.get(b).remove(a);
	}
	
	/**
     * Returns true if there is a bilateral relationship between banks 'a' and 'b'.
     * If the banks are not already in the network, throws a NoSuchElementException
     *
     * @param a Bank a
     * @param b Bank b
     * @return Returns true if there is a relationship between banks 'a' and 'b'
     */
	public boolean isRelated(Party a, Party b) {
		/* Confirm both endpoints exist. */
        if (!adj.containsKey(a) || !adj.containsKey(b))
            throw new NoSuchElementException("Both banks must be in the network.");     
        
        /* Network is symmetric, so we can just check either endpoint. */
        return adj.get(a).contains(b);
	}
}
