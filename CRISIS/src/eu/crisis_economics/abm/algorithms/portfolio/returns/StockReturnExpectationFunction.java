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
package eu.crisis_economics.abm.algorithms.portfolio.returns;

/** 
  * Interface for functions that calculate the expected merginal 
  * return of stock investments. Implement this interface if you
  * want to write your own expectation function.
  * 
  * @author daniel
  */
public interface StockReturnExpectationFunction {
   
   /**
     * Compute the expected marginal return per unit of cash invested
     * in stocks with the specified name. The procedure used to compute
     * the expected marginal return depends on the implementation.
     * @param stockName
     *        The name of the stock to be analyzed.
     * @return
     *        The expected marginal return per unit of cash 
     *        invested in stocks.
     */
   public double computeExpectedReturn(String stockReleaserName);
}
