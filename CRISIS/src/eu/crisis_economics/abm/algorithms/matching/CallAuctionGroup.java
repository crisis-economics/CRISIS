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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.lang.IllegalArgumentException;

import eu.crisis_economics.abm.algorithms.matching.SimpleNode;

/**
 * @author      JKP
 * @category    Matching Algorithms (Detail)
 * @see         eu.crisis_economics.algorithms.matching.package-info.java
 * @since       1.0
 * @version     1.0
 */
public class CallAuctionGroup implements Iterable<ComputeNode> {
    private Collection<ComputeNode> m_nodes;
    /**
     * @throws IllegalArgumentException if
     *         (a) nodes is empty
     *         (b) all (node : nodes( have zero volume
     */
    public CallAuctionGroup( // Mutable, Iterable
        Collection<SimpleNode> nodes)
        throws IllegalArgumentException {
        if (nodes.size() == 0)
            throw new IllegalArgumentException("CallAuctionGroup(): zero nodes");
        m_nodes = new ArrayList<ComputeNode>();
        for(Node node : nodes) { m_nodes.add(new ComputeNode(node)); }
        if(this.totalVolume() == 0.)
            throw new IllegalArgumentException(
                "CallAuctionGroup(): total volume is zero");
    }
    
    static public class Division { // Immutable, Privately Constructed
        public final double 
            lowerThan, greaterThan, equalTo, threshold;
        
        private Division() {
            this.lowerThan   = 0.;
            this.greaterThan = 0.;
            this.equalTo     = 0.;
            this.threshold   = Double.NEGATIVE_INFINITY;
        }
        
        private Division(
            double lowerThan, double greaterThan, double equalTo, double divisionThreshold) {
            this.lowerThan   = lowerThan;
            this.greaterThan = greaterThan;
            this.equalTo     = equalTo;
            this.threshold   = divisionThreshold;
        }
        
        public double greaterThanEqualTo() { return this.equalTo + this.greaterThan; }
        
        public double lowerThanEqualTo() { return this.equalTo + this.lowerThan; }
    }
    
    public int size() { return m_nodes.size(); }
    
    public Collection<ComputeNode> nodes() { return m_nodes; }
    
    // group nodes by price threshold
    public CallAuctionGroup.Division splitByPrice(double priceThreshold) {
        double lowerThan = 0., greaterThan = 0., equalTo = 0.;
        for (ComputeNode node : this) {
            final double nodePrice = node.getPricePerUnit();
            if (nodePrice == priceThreshold)     { equalTo += node.getUsable();     } 
            else if (nodePrice > priceThreshold) { greaterThan += node.getUsable(); } 
            else                                 { lowerThan += node.getUsable();   }
        }
        return new Division(lowerThan, greaterThan, equalTo, priceThreshold);
    }
    
    // sum of node volumes
    public double totalVolume() {
        double result = 0.;
        for(Node node : this) { result += node.getVolume(); }
        return result;
    }
    
    @Override
    public Iterator<ComputeNode> iterator() { return m_nodes.iterator(); }
}