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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.algorithms.portfolio.returns.StockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.PortfolioWeighting;
import eu.crisis_economics.abm.events.BankBankruptcyStockInstrumentErasureEvent;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.utilities.StateVerifier;

/***********************************************************************
 * <p>
 * An AgentModule for implementing portfolio management decisions.
 * The constructor expects two function-classes: one that calculates
 * portfolio weightings, given expected returns, and one that calculates
 * expected returns.
 * </p><p>
 * You can write your own function-classes or use the provided nested
 * classes:
 * </p>
 * <dl>
 * <dt>MPortfolio.Logit</dt>        <dd>A logit portfolio weighting function</dd>
 * <dt>MPortfolio.Adaptive</dt>     <dd>An adaptive portfolio weighting function</dd>
 * <dt>MPortfolio.TrendFollower</dt><dd>A trend-following expectation function</dd>
 * <dt>MPortfolio.Fundamentalist</dt><dd>A fundamentalist expectation function</dd>
 * </dl>
 * <p>
 * So, for example:
 * </p><code>
 * new MPortfolio(new MPortfolio.logit(), new MPortfolio.TrendFollower());
 * </code><p>
 * would create a portfolio that uses the logit function with a 
 * trend following expectation.
 * </p>
 * @author daniel
 *
 **********************************************************************/
public final class SimpleStockAndLoanPortfolio implements Portfolio {
   
   private static final double
      MIN_NORMALISED_WEIGHT_STOCKS = 1.e-10,      // Minimum allowed weight
      MIN_NORMALISED_WEIGHT_LOANS = 1.e-4;
      
   private final static boolean
      VERBOSE_MODE = true;
   
   private Map<String, Double>
      stockWeights = new LinkedHashMap<String, Double>();
   private Map<String, Double>
      loanWeights = new LinkedHashMap<String, Double>(),
      loanReturns = new LinkedHashMap<String, Double>();
   private double
      cashWeight = 0.,                                     // proportion of portfolio held in cash
      targetPortfolioValue = 0.;                           // target total cash value
   PortfolioWeighting                                      // function to calculate investment
      weightFunction;                                      // weights
   StockReturnExpectationFunction
      expectationFunction;                                 // function to calculate expected returns
   
   @Inject
   public SimpleStockAndLoanPortfolio(
   @Named("SIMPLE_STOCK_AND_LOAN_PORTFOLIO_WEIGHTING")
      PortfolioWeighting w,
   @Named("SIMPLE_STOCK_AND_LOAN_STOCK_RETURN_EXPECTATION_FUNCTION")
      StockReturnExpectationFunction e
      ) {
      StateVerifier.checkNotNull(w, e);
      weightFunction      = w;
      expectationFunction = e;
      
      Simulation.events().register(this);
   }
   
   @Subscribe
   public void reactTo(final BankBankruptcyStockInstrumentErasureEvent event) {
//       int index = 0;
//       for(final Entry<String, Double> record : stockWeights.entrySet())
//          if(record.getKey().equals(event.getBankName())) {
//             weightFunction.resetWeight(index, 
//                stockWeights.get(event.getBankName()) * .02);
//             break;
//          }
//          else index++;
   }
    
    ///////////////////////////////////////////////////////////////////////////////////
    // IPortfolio interface implementation
    ///////////////////////////////////////////////////////////////////////////////////

    /****************************************************************
     * Updates the weights of each item in the portfolio
     * according to the weightFunction and expectationFunction.
     * 
     * Prerequisites: loanWeights should hold the weights of the
     * currently held illiquid loans.  
     * 
     ***************************************************************/
    @Override
    public void updatePortfolioWeights() {
        double w;
        Map<String, Double>
           minimumWeights = new HashMap<String, Double>(),   // brick-wall minimum limits on weights
           allWeights = new HashMap<String, Double>();       // stocks and loans
                
        // ---- Get expected returns for shares and loans
        weightFunction.clear();
        for(final String stock : stockWeights.keySet()) {
            weightFunction.addReturn(
               stock, expectationFunction.computeExpectedReturn(stock));
            minimumWeights.put(
               stock, MIN_NORMALISED_WEIGHT_STOCKS);
        }
        for(Entry<String, Double> loan : loanWeights.entrySet()) {
            weightFunction.addReturn(
               loan.getKey(), loanReturns.get(loan.getKey()));
            minimumWeights.put(
               loan.getKey(), MIN_NORMALISED_WEIGHT_LOANS);
        }
        weightFunction.computeWeights();
        // ---- Calculate weights
        for(final Entry<String, Double> record : weightFunction.getWeights().entrySet()) {
            w = record.getValue();
            if (w<0) throw new RuntimeException("stock/loan weight < 0");
            allWeights.put(record.getKey(), record.getValue());
        }
        // ---- add weight for cash (with reference "Cash")
        allWeights.put("Cash", 0.0);
        minimumWeights.put("Cash", cashWeight);
        
        renormaliseWithLimits(allWeights, minimumWeights);
        
        // ---- Transfer calculated weights into stockWeights and loanWeights
        for(final String stock : stockWeights.keySet())
            stockWeights.put(stock, allWeights.get(stock));
        for(final String loan : loanWeights.keySet())
            loanWeights.put(loan, allWeights.get(loan));
        
        if(VERBOSE_MODE) {
           System.out.printf(
              "------------------------------------\n"
            + "Stock and Loan portfolio: divisions:\n"
              );
           for(final Entry<String, Double> record : stockWeights.entrySet())
              System.out.printf(
                 "stock type: %30s, weight: %16.10g target investment: %16.10g\n",
                 record.getKey().toString(),
                 record.getValue(),
                 record.getValue() * getTargetPortfolioValue()
                 );
           for(final Entry<String, Double> record : loanWeights.entrySet())
              System.out.printf(
                 "loan type: %s, weight: %16.10g target investment: %16.10g\n",
                 record.getKey(),
                 record.getValue(),
                 record.getValue() * getTargetPortfolioValue()
                 );
           System.out.printf(
              "cash reserve: weight: %16.10g\n", cashWeight);
           System.out.printf(
              "------------------------------------\n");
        }
    }
    
    @Override
    public Map<String, Double> stockWeights() {
        return(stockWeights);
    }
    
    @Override
    public Map<String, Double> loanWeights() {
        return(loanWeights);
    }
    
    @Override
    public double getTargetStockInvestment(final String s) {
        return(
           stockWeights().containsKey(s) ?
           stockWeights().get(s)*getTargetPortfolioValue() : 0.
           );
    }
    
    @Override
    public double getTargetLoanInvestment(final String t) {
        return(
           loanWeights.containsKey(t) ? 
           loanWeights().get(t)*getTargetPortfolioValue() : 0.
           );
    }
    
    @Override
    public void addStock(final String stockName) {
        stockWeights.put(stockName, 0.0);
    }
    
    @Override
    public boolean hasStock(final String type) {
        return stockWeights.containsKey(type);
    }
    
    @Override
    public void addLoan(
       final String market,
       final double marginalReturn
       ) {
       loanWeights.put(market, 0.);
       loanReturns.put(market, marginalReturn);
    }
    
    @Override
    public void removeStock(final String stockName) {
        stockWeights.remove(stockName);
    }

    @Override
    public void removeLoan(final String market) {
        loanWeights.remove(market);
    }
    
    @Override
    public boolean hasLoan(final String market) {
       return loanReturns.containsKey(market);
    }

    @Override
    public void setLoanReturn(final String market, double marginalReturn) {
       if(!hasLoan(market))
          addLoan(market, marginalReturn);
       else
          loanReturns.put(market, marginalReturn);
    }
    
    @Override
    public double getCashWeight() {
        return cashWeight;
    }

    @Override
    public void setCashWeight(double cashWeight) {
        this.cashWeight = cashWeight;
    }   

    @Override
    public double getTargetPortfolioValue() {
        return targetPortfolioValue;
    }

    @Override
    public void setTargetPortfolioValue(double portfolioValue) {
        this.targetPortfolioValue = portfolioValue;
        if(VERBOSE_MODE)
           System.out.printf("Portfolio size decided: %16.10g\n", targetPortfolioValue);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Helper functions
    ///////////////////////////////////////////////////////////////////////////////////
    
    private void renormaliseWithLimits(
       Map<String, Double> weights,
       Map<String, Double> minimumWeights
       ) {
       Map<String, Boolean> belowMinimum;  // investments that need minimum limits applying
       double liquidSum;                   // sum of all non-normalised weights that are above minimum
       double illiquidSum;                 // sum of all normalised weights that are held at minimum limit
       double minLimit;
       double w;
       boolean changed;
       
       belowMinimum = new HashMap<String, Boolean>();
       for(final String reference : weights.keySet())
          belowMinimum.put(reference, false);
       liquidSum = 0.0;
       illiquidSum = 0.0;
       // ---- Calculate simple sum
       for(final double record : weights.values())
           liquidSum += record;
       
       // ---- Split sum into liquid and illiquid weights
       changed = true;
       while(changed) {
          changed = false;
          for(Entry<String, Double> record : weights.entrySet()) {
             minLimit = minimumWeights.get(record.getKey()) * liquidSum/(1.0 - illiquidSum);
             if(belowMinimum.get(record.getKey()) == false &&
                weights.get(record.getKey()) < minLimit) {
                liquidSum -= weights.get(record.getKey());
                illiquidSum += minimumWeights.get(record.getKey());
                if(illiquidSum >= 1.0) {
                    // all weights are at minimum (possibly not normalised)
                   belowMinimum.clear();
                   for(final String reference : weights.keySet())
                      belowMinimum.put(reference, true);
                    changed = false;
                    break;
                }
                belowMinimum.put(record.getKey(), true);
                changed = true;
             }
          }
       }
       // ---- renormalise (if possible)
       for(Entry<String, Double> record : weights.entrySet()) {
          if(belowMinimum.get(record.getKey())) {
             w = minimumWeights.get(record.getKey());
          } else {
             w = (1.0-illiquidSum)*weights.get(record.getKey())/liquidSum;
          }
          record.setValue(w);
       }
   }
   
   @Override
   public Set<String> getLoans() {
      return new HashSet<String>(loanWeights.keySet());
   }
   
   @Override
   public Set<String> getStocks() {
      return new HashSet<String>(stockWeights.keySet());
   }
}
