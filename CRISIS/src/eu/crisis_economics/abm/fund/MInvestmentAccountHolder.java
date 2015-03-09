/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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
package eu.crisis_economics.abm.fund;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.crisis_economics.abm.IAgent;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;

class MInvestmentAccountHolder extends MDepositHolder implements InvestmentAccountHolder {
    
    public MInvestmentAccountHolder(final IAgent parent, final Contract cashReserve) {
       super(parent, cashReserve);
       iAgent = parent;
    }

    IAgent  iAgent;
    final boolean   withdrawNoOfShares = true;  // if true, the orderBook holds number of shares, otherwise cash value.
                                                        // This effects the value of withdrawal requests when they are settled.

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IInvestmentAccount interface implementation
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void invest(Depositor accountHolder, double cashInvestment)
        throws InsufficientFundsException {
        if(cashInvestment < 0.)
           return;                                                          // Silent
        InvestmentAccount account = investmentAccounts.get(accountHolder);
        if (account == null){
            account = new InvestmentAccount(this, accountHolder, 0.0);
            investmentAccounts.put(accountHolder, account);
        }
        double 
           oldShareCount = account.getNoOfShares();
        accountHolder.credit(cashInvestment);
        account.deposit(cashInvestment);
        numberOfEmittedShares += account.getNoOfShares() - oldShareCount;
    }
    
    @Override
    public void requestWithdrawal(Depositor accountHolder, double cashAmount)
        throws InsufficientFundsException {
        if(cashAmount <= 0.0) {
           System.err.println(
              "InvestmentAccount: withdrawal request is negative or zero (value " + cashAmount 
            + "). No action was taken.");
           return;
        }
        InvestmentAccount account = investmentAccounts.get(accountHolder);
        if(account == null) {
            throw(new IllegalArgumentException("Unrecognised account holder trying to withdraw from InvestmentAccount"));
        }
        if(account.getValue() < cashAmount) {
            throw(new InsufficientFundsException("Account holder trying to withdraw more funds than he holds in InvestmentAccount"));
        }
        if(withdrawNoOfShares) {
            addToOrderBook(account, cashAmount/getEmissionPrice());
        } else {
            addToOrderBook(account, cashAmount);
        }
    }



    /*********************************************************************************************
     * This method settles the requests to sell shares back to the stock releaser that have been
     * set by the requestWithdrawal method. This is called once every timestep, as setup in
     * init().
     * 
     * @throws InsufficientFundsException
     *********************************************************************************************/
    @Override
    public void settleOrderBook(double maximumCashCashBePaid)
        throws InsufficientFundsException {
        double              noOfShares;
        double              cashValue;
        double              totalShareCount = 0.0;
        InvestmentAccount   account;
        SettlementParty     settlement;
        maximumCashCashBePaid = Math.max(maximumCashCashBePaid, 0.);
        
        if(maximumCashCashBePaid <= 0.) {
           orderBook.clear();                               // No withdrawals can be fulfilled.
           return;
        }
        
//      recountShareDistribution();
        try {
            for(Entry<InvestmentAccount, Double> sellOrder : orderBook.entrySet()) {
                account             = sellOrder.getKey();
                settlement          = account.getDepositor();
                if(withdrawNoOfShares) {
                    noOfShares  = sellOrder.getValue();
                    cashValue   = noOfShares * getEmissionPrice();
                } else {
                    cashValue   = sellOrder.getValue();
                    noOfShares  = cashValue/getEmissionPrice();     
                }
                
                if(cashValue > account.getValue()) {                // Preempt numerical failure.
                   if(cashValue > account.getValue() + 1.)          // Intollerable.
                      throw new InsufficientFundsException();
                   System.err.printf(
                      "InvestmentAccount.settleOrderBook: client withdrawal of value %g "
                    + "cannot be honored. The present value of client investments is %g. "
                    + "The withdrawal amount has been trimmed to %g.\n",
                      cashValue, account.getValue(), account.getValue()
                      );
                   System.err.flush();
                   cashValue = account.getValue();                  // Tollerable.
                   noOfShares = cashValue / getEmissionPrice();
                }
                if(cashValue < 0.) {
                   if(cashValue < -1.e-5)                           // Intollerable.
                      throw new IllegalStateException();
                   System.err.printf(
                      "InvestmentAccount.settleOrderBook: client withdrawal of value %g "
                    + "is negative. This is assumed to be a numerical residual and has " 
                    + "been tolerated. The value of the client account is %g. "
                    + "No action was taken.\n",
                      cashValue, account.getValue()
                      );
                   System.err.flush();
                   orderBook.put(account, 0.0);                     // Tollerable.
                   continue;
                }
                cashValue = Math.min(maximumCashCashBePaid, cashValue);
                account.withdraw(cashValue);
                maximumCashCashBePaid -= cashValue;
                settlement.debit(cashValue);
                totalShareCount += noOfShares;
                orderBook.put(account, 0.0); // retain validity of orderbook
            }
        } catch(InsufficientFundsException e) {
            // Abandon
        }
        numberOfEmittedShares -= totalShareCount;
        orderBook.clear();
        normalizeClientShares();
    }

    void recountShareDistribution() {
       numberOfEmittedShares = 0.;
       for(Entry<Depositor, InvestmentAccount> record : investmentAccounts.entrySet())
          numberOfEmittedShares += record.getValue().getNoOfShares();
    }
    
    private void normalizeClientShares() {
       final double
          pricePerShare = getEmissionPrice(),
          numberOfShares = getNumberOfEmittedShares();
       if(numberOfShares <= 0.) return;
       final double
          normalizedPricePerShare = Math.sqrt(pricePerShare * numberOfShares),
          shareScaleFactor = normalizedPricePerShare / numberOfShares;
       emissionPrice = normalizedPricePerShare;
       for(InvestmentAccount account : investmentAccounts.values())
          account.setNoOfShares(account.getNoOfShares() * shareScaleFactor);
       recountShareDistribution();
    }
    
    @Override
    public double getEmissionPrice() {
        return emissionPrice;
    }

    @Override
    public double getBalance(Depositor account) {
        InvestmentAccount accountContract = investmentAccounts.get(account);
        if(accountContract == null) {
            return(0.0);
        }
        return(accountContract.getValue());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    private void addToOrderBook(InvestmentAccount account, double amount) {
        Double alreadyOrdered = orderBook.get(account);
        if(alreadyOrdered != null) {
            amount += alreadyOrdered;
        }
        orderBook.put(account, amount);
    }

    public double getOrderBookTotalShares() {
        if(withdrawNoOfShares) {
            return(getOrderBookSum());
        }
        return(getOrderBookSum()/getEmissionPrice());
    }

    
    public double getOrderBookTotalCashValue() {
        if(withdrawNoOfShares) {
            return(getOrderBookSum()*getEmissionPrice());
        }
        return(getOrderBookSum());
    }

    protected double getOrderBookSum() {
        Double total = 0.0;
        for(Double noOfShares : orderBook.values()) {
            total += noOfShares;
        }
        return(total);
    }
    
    public double getNumberOfEmittedShares() {
        return numberOfEmittedShares;
    }

    public void setNumberOfEmittedShares(double numberOfEmittedShares) {
        this.numberOfEmittedShares = numberOfEmittedShares;
    }

    public void setEmissionPrice(double emissionPrice) {
        this.emissionPrice = emissionPrice;
    }

    public void scaleOrderBook(double p) {
        for(Entry<InvestmentAccount, Double> order : orderBook.entrySet()) {
            orderBook.put(order.getKey(), order.getValue()*p);
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    // Variables
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    protected final Map<InvestmentAccount, Double> orderBook = new LinkedHashMap<InvestmentAccount, Double>();
    protected final Map<Depositor, InvestmentAccount> investmentAccounts = new LinkedHashMap<Depositor, InvestmentAccount>();

    protected double            numberOfEmittedShares   = 0.0;
    private   double            emissionPrice           = 1.0;
}
