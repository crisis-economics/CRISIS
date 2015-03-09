/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Peter Klimek
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 James Porter
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
 * Copyright (C) 2015 Aurélien Vermeir
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
package eu.crisis_economics.abm.bank;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.bank.strategies.BankStrategy;
import eu.crisis_economics.abm.bank.strategies.interbank.InterbankMarketStrategy;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.InterbankBorrower;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.BankStrategyFactory;

/**
 * A {@link Bank} implementation that receives an {@link BankStrategy} object (via
 * a factory) in the constructor. The {@link BankStrategy} object defines methods
 * that implements strategic behavior of the {@link Bank} regarding four markets: 
 * deposit, commercial loan, interbank loan, and stock markets. This class provides 
 * delegate methods which call the corresponding {@link BankStrategy} methods.
 * 
 * @author Tamás Máhr
 */
public class StrategyBank extends StockTradingBank implements InterbankBorrower {

    private BankStrategy strategy;
    private InterbankMarketStrategy interbankStrategy;

    public double averageStockReturn;
	public double assetsInStock;
	public double assetsInLoans;

	public double averageStockOrderPrice;
	
	private double loanSupply;
	
	private boolean loanSupplyMatched;
	
	private double quotedInterestRate;
	
	private double loanSuccess;
	
	private int isFundamentalist;

	public List<Double> stockReturns = new ArrayList<Double>();
	private double loanReturn;

   public StrategyBank(
      final double initialCash,
      final BankStrategyFactory strategyFactory
      ) {
      super(initialCash);
      Preconditions.checkNotNull(strategyFactory);
      
      this.strategy = strategyFactory.create(this);
      this.interbankStrategy = new InterbankMarketStrategy(this);
      
      // Scheduled events.
      Simulation.repeat(this, "considerCommercialLoanMarkets",
         NamedEventOrderings.COMMERCIAL_MARKET_BIDDING);
      Simulation.repeat(this, "considerDepositMarkets",
         NamedEventOrderings.DEPOSIT_MARKET_BIDDING);
      Simulation.repeat(this, "considerInterbankMarkets",
         NamedEventOrderings.INTERBANK_MARKET_BIDDING);
      Simulation.repeat(this, "afterInterbankMarkets",
         NamedEventOrderings.INTERBANK_AFTER_MARKET_MATCHING);
      Simulation.repeat(this, "considerStockMarkets",
         NamedEventOrderings.STOCK_MARKET_BIDDING);
   }

	public void setStrategy(final BankStrategy strategy) {
		if(strategy == null)
			throw new IllegalArgumentException("StrategyBank.setStrategy: argument is null.");
		this.strategy = strategy;
	}

	public BankStrategy getStrategy() {
		return strategy;
	}
	
	public InterbankMarketStrategy getInterbankStrategy() {
		return interbankStrategy;
	}
	
	// TODO Remove final declarations

	/**
	 * Subclasses should implement the bank's deposit-market strategy by
	 * overriding this function. This method is called once in each turn to
	 * allow the bank to make its move. The default implementation does nothing.
	 */
	public final void considerDepositMarkets() {
		strategy.considerDepositMarkets();
	}

	/**
	 * Subclasses should implement the bank's commercial loan strategy here.
	 * This method is called once in each turn to allow the bank to make its
	 * move. The default implementation does nothing.
	 */
	public final void considerCommercialLoanMarkets() {
		strategy.considerCommercialLoanMarkets();
	}

	/**
	 * Subclasses should implement the bank's stock-market strategy here. This
	 * method is called once in each turn to allow the bank to make its move.
	 * The default implementation does nothing.
	 */
	public final void considerStockMarkets() {
		strategy.considerStockMarkets();
	}

	/**
	 * Subclasses should implement the bank's interbank-loan-market strategy
	 * here. This method is called once in each turn to allow the bank to make
	 * its move. The default implementation does nothing.
	 */
	public final void considerInterbankMarkets() {
		strategy.considerInterbankMarkets();
	}
	
    public final void afterInterbankMarkets() {
       strategy.afterInterbankMarkets();
    }

	@Override
	public void addOrder(final Order order) {
		super.addOrder(order);
		strategy.registerNewOrder(order);
	}

	public double getDepositRate() {
		return strategy.getDepositRate();
	}

	public double getLendingRate() {
		return strategy.getLendingRate();
	}

	public double getInterLendingRate() {
		return strategy.getInterLendingRate();
	}

	public double getAssetsInLoans() {
		return assetsInLoans;
	}

	public double getAssetsInStock() {
		return assetsInStock;
	}

	public double getAverageStockReturn() {
		return averageStockReturn;
	}

	public double getAverageStockOrderPrice() {
		return averageStockOrderPrice;
	}

	public double getDepositAmount() {
		double depositAmount = 0;
		for (final Contract contract : this.getLiabilitiesDeposits()) {
			depositAmount += ((DepositAccount) contract).getValue();
		}
		return depositAmount;
	}
	
	public double getStockInvestment() {
		double stockInvestment = 0;
		for (StockAccount stock : getStockAccounts().values())
			stockInvestment += stock.getValue();
		return stockInvestment;
	}
	
   public double getStockInvestmentIn(final String stockName) {
      final StockAccount
         account = getStockAccount(stockName);
      if(account == null)
         return 0.;
      else
         return account.getValue();
   }
	
	public double getLoanInvestment() {
		double loanInvestment = 0;
		for (Contract contract : assets) {
			if (contract instanceof Loan) {
				loanInvestment += contract.getValue();
			}
		}
		return loanInvestment;
	}
	
	public double getLoanSupply() {
		return loanSupply;
	}
	
	public void setLoanSupply(double loanSupply) {
		this.loanSupply = loanSupply;
	}

	public boolean isSupplyMatched() {
		return loanSupplyMatched;
	}

	public void setSupplyMatched(boolean supplyMatched) {
		this.loanSupplyMatched = supplyMatched;
	}

	public double getQuotedInterestRate() {
		return quotedInterestRate;
	}

	public void setQuotedInterestRate(double quotedInterestRate) {
		this.quotedInterestRate = quotedInterestRate;
	}

	public double getLoanSuccess() {
		return loanSuccess;
	}

	public void setLoanSuccess(double loanSuccess) {
		this.loanSuccess = loanSuccess;
	}

	

	public double getLoanReturn() {
		return loanReturn;
	}

	public void setLoanReturn(double loanReturn) {
		this.loanReturn = loanReturn;
	}

	public int getIsFundamentalist() {
		return isFundamentalist;
	}

	public void setIsFundamentalist(int isFundamentalist) {
		this.isFundamentalist = isFundamentalist;
	}
	
	public boolean hasIBLended() { return Simulation.getSimState().random.nextDouble() > 0.5 ? true : false; }
	public boolean hasIBBorrowed() { return Simulation.getSimState().random.nextDouble() > 0.5 ? true : false; }
	
	@Override
	public double getRiskAdjustedBorrowingRate(double nominalRate) {
		return interbankStrategy.getRiskAdjustedInterbankBorrowingRate(nominalRate);
	}

	@Override
	public double getNominalBorrowingRate(double riskAdjustedRate) {
		return interbankStrategy.getNominalInterbankBorrowingRate(riskAdjustedRate);
	}
   
   /**
     * Enable or disable interbank market activity for this bank.
     */
   @Inject
   public void setIsInterbankActivityEnabled(
   @Named("IS_INTERBANK_MARKET_ENABLED") boolean value
      ) {
      interbankStrategy.setInterbankMarketEnabled(value);
   }
}
