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
package eu.crisis_economics.abm.algorithms.portfolio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*****************************************************************
 * This is an agent-module that implements a portfolio strategy
 * that is a linear mix of any two classes that implement the IPortfolio
 * interface. i.e. the assigned weights of each instrument are given by
 * 
 * w = a*w_1 + (1-a)*w_2
 * 
 * where a is the mixing ratio and w_1 and w_2 are the weights assigned
 * by the two supplied classes.
 * 
 * When creating a new instance, just send the two portfolio bankStrategies
 * that you want to mix:
 * 
 * new LinearCombinationPortfolio(strategy1, strategy2)
 * 
 *  The mixing ratio can be controlled with setMixingRatio() and
 *  getMixingRatio() or can be sent in the constructor
 *  LinearCombinationPortfolio(strategy1, strategy2, ratio). By default
 *  the mixing ratio is set to 0.5.
 * 
 * @author daniel
 *
 ****************************************************************/
class LinearCombinationPortfolio implements Portfolio {
    
    public LinearCombinationPortfolio(Portfolio strategy1, Portfolio strategy2) {
        this(strategy1, strategy2, 0.5);
    }
    
    public LinearCombinationPortfolio(Portfolio strategy1, Portfolio strategy2, double initRatio) {
        p1 = strategy1;
        p2 = strategy2;
        ratio = initRatio;
    }

    public void setMixingRatio(double r) {
        if(r < 0.0) r = 0.0;
        if(r > 1.0) r = 1.0;
        ratio = r;
    }
    
    public double getMixingRatio() {
        return(ratio);
    }
    
    double
       ratio;
    HashMap<String, Double>
       stocks = new HashMap<String, Double>();
    HashMap<String, Double>
       loans = new HashMap<String, Double>(); 
    Portfolio
       p1,
       p2;
    
    ///////////////////////////////////////////////////////////////////////////////
    // IPortfolio implementation
    ///////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void updatePortfolioWeights() {
        if(ratio > 0.0) p1.updatePortfolioWeights();
        if(ratio < 1.0) p2.updatePortfolioWeights();
        for(final String s : stocks.keySet()) {
            stocks.put(s, ratio*p1.stockWeights().get(s) + (1.0-ratio)*p2.stockWeights().get(s));
        }
        for(final String m : loans.keySet()) {
            loans.put(m, ratio*p1.loanWeights().get(m) + (1.0-ratio)*p2.loanWeights().get(m));
        }
    }
    
    @Override
    public Map<String, Double> stockWeights() {
        return(stocks);
    }
    
    @Override
    public Map<String, Double> loanWeights() {
        return(loans);
    }
    
    @Override
    public double getTargetStockInvestment(final String s) {
        return(stockWeights().get(s)*getTargetPortfolioValue());
    }

    @Override
    public double getTargetLoanInvestment(final String type) {
        return(loanWeights().get(type)*getTargetPortfolioValue());
    }
    
    @Override
    public void addStock(final String s) {
        p1.addStock(s);
        p2.addStock(s);
        stocks.put(s, 0.);
    }
    
    @Override
    public boolean hasStock(final String stock) {
       return p1.hasStock(stock) && p2.hasStock(stock);
    }
    
    @Override
    public void addLoan(
       final String type,
       final double marginalReturn
       ) {
       p1.addLoan(type, marginalReturn);
       p2.addLoan(type, marginalReturn);
       loans.put(type, 0.);
    }
    
    @Override
    public boolean hasLoan(final String type) {
       return p1.hasLoan(type) || p2.hasLoan(type);
    }

    @Override
    public void setLoanReturn(
       final String type,
       final double marginalReturn
       ) {
       p1.setLoanReturn(type, marginalReturn);
       p2.setLoanReturn(type, marginalReturn);
    }
    
    @Override
    public void removeStock(final String s) {
        p1.removeStock(s);
        p2.removeStock(s);
        stocks.remove(s);
    }

    @Override
    public void removeLoan(final String m) {
        p1.removeLoan(m);
        p2.removeLoan(m);
        loans.remove(m);
    }
    
    @Override
    public double getCashWeight() {
        return(p1.getCashWeight());
    }

    @Override
    public void setCashWeight(double p) {
        p1.setCashWeight(p);
        p2.setCashWeight(p);
    }

    @Override
    public double getTargetPortfolioValue() {
        return(p1.getTargetPortfolioValue());
    }

    @Override
    public void setTargetPortfolioValue(double portfolioValue) {
        p1.setTargetPortfolioValue(portfolioValue);
        p2.setTargetPortfolioValue(portfolioValue);
    }

   @Override
   public Set<String> getLoans() {
      final Set<String>
         result = new HashSet<String>();
      result.addAll(p1.getLoans());
      result.addAll(p2.getLoans());
      return result;
   }

   @Override
   public Set<String> getStocks() {
      final Set<String>
         result = new HashSet<String>();
      result.addAll(p1.getStocks());
      result.addAll(p2.getStocks());
      return result;
   }
}
