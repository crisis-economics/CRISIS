/*
 * This file is part of CRISIS, an economics simulator.
 * 
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

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;

public class FirmStub extends Agent implements StockReleaser, Depositor {

    FirmStub(Bank bank, double initialDeposit) {
        mDepositor     = new MDepositor(this, bank, initialDeposit);
        mStockReleaser = new MStockReleaser(this, mDepositor);
    }
    
    public MStockReleaser   mStockReleaser;
    public MDepositor       mDepositor;
    double marketValue;
    
    public void setMarketValue(double v) {
        marketValue = v;
    }

    @Override
    public double getMarketValue() {
        return(marketValue);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // StockReleaser forwarding
    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addStockAccount(StockAccount stockAccount) {
        mStockReleaser.addStockAccount(stockAccount);       
    }

    @Override
    public boolean removeStockAccount(StockAccount stockAccount) {
        return(mStockReleaser.removeStockAccount(stockAccount));
    }

    @Override
    public double computeDividendFor(StockAccount stockAccount) {
        return(mStockReleaser.computeDividendFor(stockAccount));
    }

    @Override
    public void releaseShares(double numberOfShares, double pricePerShare,
            StockHolder shareHolder) {
        mStockReleaser.releaseShares(numberOfShares, pricePerShare, shareHolder);
    }

    @Override
    public void setDividendPerShare(double dividendPerShare) {
        mStockReleaser.setDividendPerShare(dividendPerShare);
    }

//  @Override
//  public double getMarketValue() {
//      return(mStockReleaser.getMarketValue());
//  }

    @Override
    public double getDividendPerShare() {
        return(mStockReleaser.getDividendPerShare());
    }

//  @Override
//  public String getStockTicker() {
//      return(mInvestmentAccount.getStockTicker());
//  }

    @Override
    public void registerOutgoingDividendPayment(double dividendPayment,
            double pricePerShare, double numberOfSharesHeldByInvestor) {
        mStockReleaser.registerOutgoingDividendPayment(dividendPayment, pricePerShare, numberOfSharesHeldByInvestor);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // SettlementParty forwarding
    ////////////////////////////////////////////////////////////////////////////////////////
        

    @Override
    public double credit(double amt) throws InsufficientFundsException {
        return(mDepositor.credit(amt));
    }

    @Override
    public void debit(double amt) {
        mDepositor.debit(amt);
    }

    @Override
    public void cashFlowInjection(double amt) {
        mDepositor.cashFlowInjection(amt);
    }
    
    @Override
    public double getDepositValue() {
       return mDepositor.getBalance();
    }
    
    @Override
    public <T> T accept(final AgentOperation<T> operation) {
       return operation.operateOn(this);
    }
}
