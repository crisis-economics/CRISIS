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
package eu.crisis_economics.abm.algorithms.portfolio;

import java.util.Map;
import java.util.Set;

/**
  * Interface for a dynamic stock and loan portfolio. A stock
  * and loan portfolio is an object that divides a cash sum
  * between desired stock and loan investments.
  * 
  * An implementation of this interface should calculate the 
  * proportion of the portfolio to invest in each instrument.
  * 
  * @author daniel
  * @author phillips
  */
public interface Portfolio {
   /**
     * Update weights to present market prices
     */
   public void updatePortfolioWeights();
   
   /**
     * Get current stock weights
     */
   public Map<String, Double> stockWeights();
   
   /**
     * Get current loan weights
     */
   public Map<String, Double> loanWeights();
   
   /**
     * Get the target investment in a given stock
     */
   public double getTargetStockInvestment(String uuid);
   
   /**
     * Get the target investment in a given loan type
     */
   public double getTargetLoanInvestment(String uuid);
   
   /**
     * Add a stock to the portfolio
     */
   public void addStock(String uuid);
   
   /**
     * Is a stock considered by the portfolio?
     */
   public boolean hasStock(String uuid);
   
   /**
     * Add a loan market to the portfolio.
     */
   public void addLoan(String uuid, double initialReturnRate);
   
   /**
     * Is a loan market considered by the portfolio?
     */
   public boolean hasLoan(String uuid);
   
   /**
     * Get a list of loans considered by this portfolio.
     */
   public Set<String> getLoans();
   
   /**
     * Get a list of stocks considered by this portfolio.
     */
   public Set<String> getStocks();
   
   public void setLoanReturn(String uuid, double marginalReturn);
   
   /**
     * Remove a stock from the portfolio.
     */
   public void removeStock(String s);
   
   /**
     * Remove a loan type from the portfolio.
     */
   public void removeLoan(String type);
   
   /**
     * Get the current cash weight.
     */
   public double getCashWeight();
   
   /**
     * Set proportion of portfolio to be held in cash
     */
   public void setCashWeight(double p);
   
   /**
     * Get the target portfolio value.
     */
   public double getTargetPortfolioValue();
   
   /**
     * Set the target portfolio value.
     */
   public void setTargetPortfolioValue(double v);
}

