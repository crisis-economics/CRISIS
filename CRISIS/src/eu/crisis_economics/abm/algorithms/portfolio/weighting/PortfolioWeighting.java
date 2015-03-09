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
package eu.crisis_economics.abm.algorithms.portfolio.weighting;

import java.util.Map;

/*******************************************************************
 * <p>
 *  Interface for algorithms that calculate how to partition a portfolio
 *  up among a set of instruments. Implement this if you want to write your
 *  own portfolio weighting function.
 *  </p><p>
 *  Given a set of expected returns on investments, the function
 *  should calculate a set of weights (not necessarily
 *  normalised) for each of those investments.
 *  </p><p>
 *  You receive an ordered set of expected returns to investments through
 *  addReturn() and should supply a similarly ordered set of weights
 *  through getWeight(). clear() should reset the expected returns
 *  and do whatever else needs doing, this method is guaranteed to be
 *  called before any others. 
 *  </p>
 * @author daniel
 *
 ******************************************************************/
public interface PortfolioWeighting {
   public void addReturn(final String reference, double expectedReturn);
   public void computeWeights();
   public double getWeight(String reference);
   public Map<String, Double> getWeights();
   public int size();
   public void clear();
}
