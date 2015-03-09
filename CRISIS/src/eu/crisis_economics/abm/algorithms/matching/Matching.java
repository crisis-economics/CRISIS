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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.lang.Math;
/**
 * @author      JKP
 * @category    Matching Algorithms
 * @see         eu.crisis_economics.algorithms.matching.package-info.java
 * @since       1.0
 * @version     1.0
 */
public final class Matching implements Iterable<Matching.OneToOneMatch> {
    private List<Matching.OneToOneMatch>
        m_matches;
    
    private List<UnmatchedNode>
        m_unmatchedLeftNodes, 
        m_unmatchedRightNodes;
    
    private Matching( // Immutable, Builder
        Matching.Builder builder) 
    {
        this.m_matches             = Collections.unmodifiableList(builder.m_matches);
        this.m_unmatchedLeftNodes  = Collections.unmodifiableList(builder.m_unmatchedLeftNodes);
        this.m_unmatchedRightNodes = Collections.unmodifiableList(builder.m_unmatchedRightNodes);
    }
    
    static public class Builder
    {
        private List<Matching.OneToOneMatch> 
            m_matches;
        
        private List<UnmatchedNode>
            m_unmatchedLeftNodes,
            m_unmatchedRightNodes;
        
        public Builder() { // Mutable
            m_matches             = new ArrayList<Matching.OneToOneMatch>();
            m_unmatchedLeftNodes  = new ArrayList<Matching.UnmatchedNode>();
            m_unmatchedRightNodes = new ArrayList<Matching.UnmatchedNode>();
        }
        
        public void addMatch(Matching.OneToOneMatch match) { m_matches.add(match); }
        
        public void addUnmatchedLeftNode(Node node) { 
            m_unmatchedLeftNodes.add(new UnmatchedNode(node, node.getVolume())); 
        }
        
        public void addUnmatchedLeftNode(Node node, double volume) { 
            m_unmatchedLeftNodes.add(new UnmatchedNode(node, volume)); 
        }
        
        public void addUnmatchedRightNode(Node node) { 
            m_unmatchedRightNodes.add(new UnmatchedNode(node, node.getVolume())); 
        }
        
        public void addUnmatchedRightNode(Node node, double volume) { 
            m_unmatchedRightNodes.add(new UnmatchedNode(node, volume)); 
        }
        
        public Matching build() { return new Matching(this); }
    }
    
    static public class OneToOneMatch
    {
        public final double 
            matchAmount,
            matchCost;
        
        public final Node 
            leftNode, 
            rightNode;
        
        public OneToOneMatch( // Immutable
            final Node fromLeft,
            final Node toRight,
            double matchAmount,
            double matchCost
            ) throws IllegalArgumentException
        {
            if(fromLeft == null || toRight == null) {
                throw new IllegalArgumentException(
                    " Matching.OneToOneMatch(): node is null");
            }
            if(matchAmount < 0.) {
               if(matchAmount > -1.e-10)
                  matchAmount = 0.; // trade residue
               else
                  throw new IllegalArgumentException(
                     " Matching.OneToOneMatch: match amount is not positive");
            }
            if( matchAmount > Math.max(Math.min(fromLeft.getVolume(), toRight.getVolume()), 0.) ) {
                throw new IllegalArgumentException(
                    " Matching.OneToOneMatch: match amount is too large for nodes");
            }
            this.matchAmount = matchAmount;
            this.matchCost = matchCost;
            this.leftNode = fromLeft;
            this.rightNode = toRight;
        }
    }
    
    public static class UnmatchedNode
    {
        public final double unmatchedAmount;
        
        public final Node node;
        
        public UnmatchedNode( // Immutable
            final Node node,
            double unmatchedAmount
            ) throws IllegalArgumentException {
            if(node == null) {
                throw new IllegalArgumentException(
                    " Matching.UnmatchedNode: node is null");
            }
            if(unmatchedAmount < 0.) {
                throw new IllegalArgumentException(
                    " Matching.UnmatchedNode: unmatched amount is not postive");
            }
            if( unmatchedAmount > node.getVolume() ) {
                throw new IllegalArgumentException(
                    " Matching.UnmatchedNode: unmatched amount is too large for node");
            }
            this.unmatchedAmount = unmatchedAmount;
            this.node = node;
        }
    }
    
    @Override
    public Iterator<Matching.OneToOneMatch> iterator() { return m_matches.iterator(); }
    
    public Iterator<Matching.UnmatchedNode> unmatchedLeftNodes() { 
        return m_unmatchedLeftNodes.iterator();
    }
    
    public Iterator<Matching.UnmatchedNode> unmatchedRightNodes() { 
        return m_unmatchedRightNodes.iterator();
    } 
}
