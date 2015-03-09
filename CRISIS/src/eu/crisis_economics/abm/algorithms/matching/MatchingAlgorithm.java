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

import java.util.Collection;
import java.security.InvalidAlgorithmParameterException;
/**
 * @author      JKP
 * @category    Matching Algorithms
 * @see         eu.crisis_economics.algorithms.matching.package-info.java
 * @since       1.0
 * @version     1.0
 */
public interface MatchingAlgorithm {
    public Matching matchNodes(
        Collection<SimpleNode> leftGroup,
        Collection<SimpleNode> rightGroup
        ) throws InvalidAlgorithmParameterException;
}
