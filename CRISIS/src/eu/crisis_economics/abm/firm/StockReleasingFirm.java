/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
 * Copyright (C) 2015 Daniel Tang
 * Copyright (C) 2015 Christoph Aymanns
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
package eu.crisis_economics.abm.firm;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;
import eu.crisis_economics.abm.agent.AgentNameFactory;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockReleaser;
import eu.crisis_economics.abm.markets.nonclearing.StockInstrument;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.Simulation.ScheduledMethodInvokation;

/**
 * A firm that can release shares.
 * @author olaf
 *
 */
public class StockReleasingFirm extends Firm implements StockReleaser {
	
	protected final Map<StockHolder, StockAccount> stockAccounts = new LinkedHashMap<StockHolder, StockAccount>();

	public Collection<StockAccount> getStockAccounts() {
		return stockAccounts.values();
	}

	protected static final double INITIAL_EMISSION_PRICE = 1000.;//2.5e7;//1.0e9;//3e8;//2e5;//1.0; // this has to be adjusted if you want to change the initial stock price, also needs to be changed in priceStrategy
	
	/**
	 *  The bank that holds the initial shares of this firm. 
	 */
	//protected StockHolder stockHolder;

	protected double dividendPerShare;
	private double numberOfEmittedShares;

	
	private double emissionPrice;

	protected double marketValueRecordedAt;

	protected double cachedMarketValue;

	private ScheduledMethodInvokation sheduledSharePayments;
   
   /**
     * Creates a firm that puts its money into the given {@link DepositHolder}.
     * The deposit account is initialized by the amount of initialDeposit.
     * 
     * @param depositHolder
     *        the object holding this firm's deposit
     * @param initialDeposit
     *        the initial amount on the firm's account
     */
   public StockReleasingFirm(
      final DepositHolder depositHolder,
      final double initialDeposit,
      final AgentNameFactory nameGenerator
      ) {
      super(depositHolder, 0., nameGenerator); 
                                 // Is this term zero because the firm
                                 // raises cash, at initialization,
                                 // exclusively from share emissions?
      this.emissionPrice = 0;
      this.numberOfEmittedShares = 0;
      this.dividendPerShare = 0.;
      this.marketValueRecordedAt = -2.;
      
      sheduledSharePayments = 
         Simulation.once(this, "sharePayments", NamedEventOrderings.FIRM_SHARE_PAYMENTS);
      UniqueStockExchange.Instance.addStock(this, emissionPrice);
	}
	
	@SuppressWarnings("unused")
	private void sharePayments() {
	   for (final StockAccount stockAccount : stockAccounts.values()) {
          try {
              payDividends(stockAccount);
          } catch (final InsufficientFundsException e) {
              e.printStackTrace();
              System.out.print("insufficient funds ( ");
              System.out.print(getDepositValue() + " ) to pay dividend ( ");
              System.out.print(stockAccount.getReleaser().computeDividendFor( stockAccount ) 
                 + " ) for stock account ");
              System.out.print(stockAccount.toString()+" of firm ");
              System.out.print(stockAccount.getReleaser().toString()+" in time step ");
              System.out.println(Simulation.getSimState().schedule.getSteps());
              handleBankruptcy();
              return; //Don't reschedule
          }
      }
      if (getNumberOfEmittedShares() == 0 && getDividendPerShare() > 0.0) 
         System.err.println(getUniqueName() + " has no sharehoder to pay dividends to.");
      
      Simulation.enqueue(sheduledSharePayments);
	}

	/**
	 * This method triggers the immediate payment of dividends to
	 * the specified stock account. Paying dividends results in a
	 * cash outflow, which, in turn, means that agents can potentially
	 * become insolvent during attempted dividend payments.<br/><br/>
	 * 
	 * To safeguard against insolvency, this method will cap the 
	 * dividend payment at the value of unallocated cash. If the
	 * intended dividend is greater than the value of unallocated
	 * cash, then the dividend is trimmed.
	 * 
	 * @throws InsufficientFundsException
	 *         The dividend payment could not be made.
	 * @return The actual dividend payment made to the specified
	 *         stock account.
	 */
    protected double payDividends(StockAccount stockAccount)
       throws InsufficientFundsException {
       double totalDividendPayment =
          Math.min(computeDividendFor(stockAccount), cashLedger.getUnallocated());
       stockAccount.makeDividentPayment(totalDividendPayment);
       return totalDividendPayment;
    }
	
	/**
	 * Creates a firm that can release shares to a given (initial) stock holder. 

	 * @param depositHolder the object holding this firm's deposit
	 * @param initialDeposit the initial amount on the firm's account
	 * @param stockHolder the {@link StockHolder} that should initialy own the shares of this firm
	 * @param numberOfShares the number of shares to initially issue to the given {@link StockHolder}
	 */
	public StockReleasingFirm(
	   final DepositHolder depositHolder,
	   final double initialDeposit,
	   final StockHolder stockHolder,
	   final double numberOfShares,
	   final AgentNameFactory nameGenerator
	   ) {
		this(depositHolder, initialDeposit, nameGenerator);
		if(numberOfShares > 0.)
         try {
            final double initialStockPrice = initialDeposit / numberOfShares;
            releaseShares(numberOfShares, initialDeposit / numberOfShares, stockHolder);
            UniqueStockExchange.Instance.setStockPrice(this.getUniqueName(), initialStockPrice);
         } catch (InsufficientFundsException e) {
            // Cannot raise initial deposits from shares.
         }
	}
	/**
	 *  Creates a firm that can release shares to a given (initial) stock holder. This constructor also allows to fix the emission price. Christoph created this constructor
	 * @param depositHolder
	 * @param initialDeposit
	 * @param stockHolder
	 * @param numberOfShares
	 * @param emissionPrice
	 */
	public StockReleasingFirm(
	   final DepositHolder depositHolder,
	   final double initialDeposit,
	   final StockHolder stockHolder,
	   final double numberOfShares,
	   final double emissionPrice,
	   final AgentNameFactory nameGenerator
	   ) {
	   this(depositHolder, initialDeposit, nameGenerator);
	   if(numberOfShares > 0.)
         try {
            releaseShares(numberOfShares, emissionPrice, stockHolder);
         } catch (InsufficientFundsException e) {
            // Cannot raise initial deposits from shares.
         }
	   UniqueStockExchange.Instance.setStockPrice(this.getUniqueName(), emissionPrice);
	}

	@Override
	public void addStockAccount(final StockAccount stockAccount) {
		stockAccounts.put(stockAccount.getHolder(), stockAccount);
	}

	@Override
	public boolean removeStockAccount(final StockAccount stockAccount) {
		return stockAccounts.remove(stockAccount.getHolder()) != null;
	}

	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#computeDividendFor(eu.crisis_economics.abm.contracts.StockAccount)
	 */
	@Override
	public double computeDividendFor(final StockAccount stockAccount) {
//		System.out.println("Number of shares: "+stockAccount.getQuantity());
		return stockAccount.getQuantity()*this.dividendPerShare;
	}

   @Override
   public void releaseShares(
      final double numberOfShares,
      final double pricePerShare,
      final StockHolder shareHolder
      ) throws InsufficientFundsException {
      UniqueStockExchange.Instance.generateSharesFor(
         this.getUniqueName(),
         shareHolder,
         numberOfShares,
         pricePerShare
         );
      numberOfEmittedShares += numberOfShares;
      this.emissionPrice = pricePerShare;
      this.marketValueRecordedAt = -2.0;
   }
	
	// a function that can release shares without the need to pay for them.
	protected void releaseShares(
	   final double numberOfShares,
	   final StockHolder shareHolder
	   ) {
	   try {
          UniqueStockExchange.Instance.generateSharesFor(
            this.getUniqueName(), shareHolder, numberOfShares, 0.);
       } catch (final InsufficientFundsException neverThrows) {
          neverThrows.printStackTrace();
          Simulation.getSimState().finish();
       }
	   this.numberOfEmittedShares += numberOfShares;
	}

	@RecorderSource("DividendPerShare")
	public double getDividendPerShare() {
		return dividendPerShare;
	}
   
	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#setDividendPerShare(double)
	 */
	@Override
	public void setDividendPerShare(final double dividendPerShare) {
		this.dividendPerShare = Math.max(dividendPerShare, 0.0);
	}

	@RecorderSource("NumberOfEmittedShares")
	public double getNumberOfEmittedShares() {
		return numberOfEmittedShares;
	}

	public void setNumberOfEmittedShares(double numberOfEmittedShares) {
		this.numberOfEmittedShares = numberOfEmittedShares;
	}
	/* (non-Javadoc)
	 * @see eu.crisis_economics.abm.contracts.StockReleaser#getMarketValue()
	 */
	@Override
//	@RecorderSource("StockPrice")
	public double getMarketValue() {
		if (Simulation.getSimState() != null && marketValueRecordedAt < Simulation.getFloorTime()){
			marketValueRecordedAt = Simulation.getFloorTime();
			final Set<StockMarket> stockMarkets = new HashSet<StockMarket>(); 

			for (StockAccount stockAccount : stockAccounts.values()) {
				Set<StockMarket> markets = stockAccount.getHolder().getStockMarkets();
				if (markets != null){
					stockMarkets.addAll(markets);
				}     
			}

			if (stockMarkets.size() == 0){
				cachedMarketValue = emissionPrice;
				return emissionPrice;
			}

			double sum = 0.0;
			for (final StockMarket act : stockMarkets) {
				sum += getPriceOfInstrumentAtMarket( act );
			}

			//return sum / numberOfEmittedShares / stockMarkets.size();
			cachedMarketValue = sum / stockMarkets.size();
			return cachedMarketValue;
		} else {
			return cachedMarketValue;
		}
	}
	
	@RecorderSource("MarketCapitalization")
	public double getMarketCapitalization() {
		return this.getMarketValue()*this.numberOfEmittedShares;
	}
	
	private double getPriceOfInstrumentAtMarket(final StockMarket stockMarket) {
		final StockInstrument stockInstrument = stockMarket.getStockInstrument( this.getUniqueName() );
		if ( stockInstrument == null ) {
			return this.emissionPrice; //return emission price.
		} else if (stockInstrument.getLastPrice() == 0) {
			return this.emissionPrice; //return emission price.
		} else {
			return stockInstrument.getLastPrice(); //returns the last traded price			
		}
	}
	
	/**
	 * returns the number of shares in all stock accounts. It should be equivalent to numberOfEmittedShares and serves as consistency check. Do not use it in production system because it is inefficient. Use numberOfEmittedShares.
	 * @see eu.crisis_economics.abm.StockReleasingFirm#numberOfEmittedShares
	 * @return number of shares in stock accounts
	 * @exception RuntimeException if numberOfEmittedShares != number of shares in stock accounts.
	 */
	public double computeNumberOfShares() {
		double nShares = 0.0;
		for (final StockAccount stockAccount : stockAccounts.values()) {
			nShares += stockAccount.getQuantity();
		}
		if (nShares != this.numberOfEmittedShares) {
			throw new RuntimeException("Stockflow inconsistency: number of emitted shares ( "+this.numberOfEmittedShares+" ), number of shares in stock accounts ( "+nShares+" )");
		}
		return nShares;
	}

	public double getEmissionPrice() {
		return emissionPrice;
	}

	public void setEmissionPrice(double emissionPrice) {
		this.emissionPrice = emissionPrice;
	}
	
	@Override
	public void registerOutgoingDividendPayment(
	   double dividendPayment,
	   double pricePerShare,
	   double numberOfSharesHeldByInvestor) {
	   // No action. 
	}
	
	@RecorderSource("Dividend")
	public double getCurrentDividend(){
		return dividendPerShare * numberOfEmittedShares;
	}
}
