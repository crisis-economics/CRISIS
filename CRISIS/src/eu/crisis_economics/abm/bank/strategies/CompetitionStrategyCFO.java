/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 James Porter
 * Copyright (C) 2015 Fabio Caccioli
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
package eu.crisis_economics.abm.bank.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.util.Bag;
import eu.crisis_economics.abm.bank.StockTradingBank;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositAccount;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.LoanStrategyFirm;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.firm.StrategyFirm;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.LoanInstrument;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author fabio
 * @author christoph
 */
public class CompetitionStrategyCFO
extends EmptyBankStrategy {

   /*
	protected SpecsCompetitionStrategyNewInitialization model;

	public void setModel(SpecsCompetitionStrategyNewInitialization model) {
		this.model = model;
	}
	*/
	private Bag banks = new Bag();

	private double equity;

	private double dividendsToBePaid;

	private double cashReserveRequirement = 0.01;


	private static final double DEFAULT_TARGET_LEVERAGE = 5;

	private static final double EQUITY_PAYOUT = 1.0;

	/**
	 * target leverage of the bank
	 */
	private double targetLeverage = DEFAULT_TARGET_LEVERAGE;

	/**
	 * allocation is computed according to instrument fitness 
	 * (return per dollar invested) exponential function with parameter riskParameter. 
	 */
	private double riskParameter = 1.0;//1.0;//10.0;//1.0;

	/**
	 * contains the size of the orders the bank wants to place in the stock market
	 */
	private final List<Double> stockOrderVolume = new ArrayList<Double>();
	private final List<Double> stockOrderPrice = new ArrayList<Double>();
	/**
	 * contains the name of the stocks corresponding to the order-size in stockOrderVolume 
	 */
	private final List<String> stockOrderInstrument = new ArrayList<String>();

	private double commercialLoanOrder = 0.0;

	private Bag firms = new Bag();

	private final int COMMERCIAL_LOAN_MATURITY = 7;

	private double newInvestment;

	// not sure about these ======

	static private double DEFAULT_EXPECTED_RETURN = 3;

	private final Map<String,Double> lastStockOrderPriceList = new HashMap<String,Double>();

	// ===========================

	/*
	 * switches for markets
	 */

	private boolean StockMarketOn = true;

	private boolean InterbankMarketOn = true;

	private boolean mixedStrategiesOn = false;

	// not sure about this
	private double CurrentLeverage = 0.0;

	// Parameters for trend following strategy ====================

	/**
	 * To allocate its investment at time t+1, the bank computes the difference between the price of
	 *  a stock at time t and its price at time t-lag.
	 */
	private int lag=10;

	/**
	 * contains stock prices between current time and (current time - lag)
	 */
	private  List<List<Double>> pastPrice = new ArrayList<List<Double>>();
	/**
	 * contains interest rates for loans issued between current time and (current time - lag)
	 */
	private  List<Double> pastInterestRate = new ArrayList<Double>();

	/**
	 * intensity of choice parameter
	 *   trendParameter > 0 --> trend follower strategy 
	 *   trendParameter < 0 -->  contrarian strategy 
	 * */
	private double trendParameter=2.2;//1.5;// previously 3.0

	// =============================================================

	// Parameters for mixed bankStrategies =============================

	private double utilityTrendFollower = 3.5;
	private double utilityFundamentalist = 3.5;

	private enum LastStrategy{FUNDAMENTALIST, TRENDFOLLOWER} 

	private LastStrategy lastStrategy;

	private double IntensityOfChoice = 0.5;//1.0;

	private int countTrendFollower = 0;
	private int countFundamentalist = 0;

	// =============================================================

	// Fields for accounting =======================================

	/**
	 * holds cashflow/profit generated through lending to a particular firm in a given time step - does NOT include principal payments
	 */
	private Map<String,Double> lendingCashflow = new HashMap<String,Double>();
	/**
	 * holds CASHFLOWS due to trading in a particular stock, note not profits, that is a bit more difficult to estimate
	 */
	private Map<String,Double> stockTradingCashflow = new HashMap<String,Double>();
	/**
	 * holds CASHFLOWS due to trading in a particular stock, note not profits, that is a bit more difficult to estimate
	 */
	private Map<String,Double> dividendCashflow = new HashMap<String,Double>();

	/**
	 * holds CASHFLOWS due to inter bank lending
	 */
	private Map<String,Double> interbankCashflow = new HashMap<String,Double>();

	/**
	 * holds fraction of CASH investment into stocks -> estimated since stock price not yet known
	 */
	private double stockSpending = 0.0;

	/**
	 * holds fraction CASH investment into commerical loans
	 */
	private double loanSpending = 0.0;

	/**
	 * holds fraction of CASH investment into interbank loans
	 */
	private double interBankSpending = 0.0;

	/**
	 * holds profit due to stock trading, assuming ability to liquidate entire portfolio in given time step
	 */
	private double stockProfit = 0.0;

	private double bankDividend = 0.0;

	private double averageLoanReturn = 0.0;
	private Map<String,Double> averageStockReturns = new HashMap<String,Double>();
	private final double tau = 70;//50;	

	private double altEquity = 0.0;

	// this is for order tactics =======================================================

	// holds last orders placed by this bank for the instruments it trades
	protected Map<String, Order> lastOrders = new HashMap<String, Order>();

	private double lendingRate;
	protected final static double DELTA_STOCK_PRICE =0.01;//0.01;//0.003;//1e3;//0.01;//0.05;//1.0e5;// 0.05;//5.0e4;//1.0;
	protected final static double DELTA_LOAN_PRICE = 0.003;//0.05;///0.01;//1e-3;//0.01;//1e-4;//0.01;

	protected double lastStockOrderPrice;

	private double initialStockPrice = 1e11;
	private double initialInterestRate = 0.1;

	private double totalLoanDemand = 0;
	private double totalLoanSupply = 0;
	
	private Map<String,Double> totalStockReturns = new HashMap<String,Double>();
   
   private double returnDifference = 0;
   private double lastLeverage = 0;
   private double tempTargetLeverage = 2;
   
   public CompetitionStrategyCFO(final StrategyBank bank) {
      super(bank);
   }

	/**
	 * This function takes care of how much should be invested, which investment strategy should be used (fundamentalist or trend following)
	 * and how much dividend the bank has to pay.
	 */
	public void allocateInvestment(StrategyBank bank) {

		this.equity = bank.getEquity();

		/*
		 * 2. Compute dividends using fixed equity target -> gives effective equity (equity - dividends)
		 */
		this.dividendsToBePaid = Math.max(EQUITY_PAYOUT*(this.equity - bank.getInitialEquity()),0.0);
		double RoE = this.dividendsToBePaid/bank.getInitialEquity(); // TODO check whether it would be better to use last equity here, currently assuming constant equity for bank
		if (lastStrategy == LastStrategy.FUNDAMENTALIST) {
			//this.utilityFundamentalist = this.utilityFundamentalist*discountRate  + RoE;//tax;
			this.utilityFundamentalist = (this.countFundamentalist*this.utilityFundamentalist + RoE)/(this.countFundamentalist+1);
			this.countFundamentalist += 1;
		}
		else {
			//this.utilityTrendFollower = this.utilityTrendFollower*discountRate + RoE;//tax;
			this.utilityTrendFollower = (this.countTrendFollower*this.utilityTrendFollower + RoE)/(this.countTrendFollower+1);
			this.countTrendFollower += 1;
		}


		double cashReserve = this.cashReserveRequirement*bank.getDepositAmount();
		double deltaC = this.dividendsToBePaid - (bank.getCashReserveValue() - cashReserve);
		/*
		if (deltaC  < 0) {
			try {
				bank.credit(this.dividendsToBePaid);
				this.dividendsToBePaid = 0.0;
				deltaC = this.dividendsToBePaid - (bank.getCashReserveValue() - cashReserve);
			} catch (InsufficientFundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 */
		altEquity = bank.getEquity();
		/*
		 * deltaC is the amount of cash the bank has to raise to pay its dividends or the amount of cash available to investment. 
		 * Leverage consideration produces a value deltaQ (< 0 if we want to deleverage and > 0 if we want to leverage up)
		 * We  now compute also the size of investment deltaQ needed for the bank to attain its target leverage.
		 * The actual size of investment will be given by the following rule:
		 * 
		 * delta Q > 0 (leverage too low), deltaC > 0 (cash too low): -deltaC
		 * delta Q > 0 (leverage too low), deltaC < 0 (cash too high): min(-deltaC,deltaQ)
		 * delta Q < 0 (leverage too high), deltaC > 0 (cash too low): -max(-deltaQ,deltaC)
		 * delta Q < 0 (leverage too low), deltaC < 0 (cash too high): deltaQ
		 * 
		 */

		/*
		 * 3. Attain leverage using effective equity
		 */

		double deltaQ = attainTargetLeverage(bank,this.targetLeverage,this.equity - this.dividendsToBePaid);
		this.newInvestment = 0.0;
		if (deltaQ > 0 && deltaC > 0) {
			this.newInvestment = - deltaC;//proposal
			//this.newInvestment =  deltaC;

		}
		else if (deltaQ > 0 && deltaC < 0) {
			this.newInvestment = Math.min(-deltaC, deltaQ);
		}
		else if (deltaQ < 0 && deltaC > 0) {
			this.newInvestment = -Math.max(deltaC, -deltaQ);//proposal
			//this.newInvestment = Math.max(deltaC, -deltaQ);

		}
		else if (deltaQ < 0 && deltaC < 0) {
			this.newInvestment = deltaQ;
		}

		//this.newInvestment = deltaQ;

		/*
		 * 4. Decide strategy - not implemented for now - only use fundamentalist strategy
		 */

		//allocateInvestmentFundamentalist(bank, this.newInvestment);


		if (this.utilityFundamentalist == 0.0) {
			allocateInvestmentFundamentalist(bank, this.newInvestment);
			this.lastStrategy = LastStrategy.FUNDAMENTALIST;
			bank.setIsFundamentalist(1);
			System.out.println("DIDNT ENTER LOOP");
		}
		else {
			// find max
			double max = Math.max(this.utilityFundamentalist, this.utilityTrendFollower);
			double pFu = Math.exp(this.IntensityOfChoice*(this.utilityFundamentalist-max));
			double pTf = Math.exp(this.IntensityOfChoice*(this.utilityTrendFollower-max));

//			System.out.println("pFu diff: "+pFu);
//			System.out.println("pTf diff: "+pTf);

			double Z = pFu + pTf;
			pFu = pFu/Z;
			pTf = pTf/Z;

//			System.out.println("PFU:"+pFu+"utility: "+this.utilityFundamentalist);
//			System.out.println("PTF:"+pTf+"utility: "+this.utilityTrendFollower);

			if (this.mixedStrategiesOn == true) {
				double randomNumber = Simulation.getSimState().random.nextDouble();

				if (randomNumber < pFu) {
				//if (randomNumber < 0.1) {
					allocateInvestmentFundamentalist(bank, this.newInvestment);
					this.lastStrategy = LastStrategy.FUNDAMENTALIST;
//					System.out.println("USES FUNDAMENTALIST STRATEGY");
					bank.setIsFundamentalist(1);
				}
				else {
					allocateInvestmentTrendFollower(bank, this.newInvestment);
					this.lastStrategy = LastStrategy.TRENDFOLLOWER;
//					System.out.println("USES TRENDFOLLOWING STRATEGY");
					bank.setIsFundamentalist(0);
				}
			}
			// if not using mixed bankStrategies, use fundamentalist strategy
			else {
				//double randomNumber = Simulation.getSimState().random.nextDouble();
				allocateInvestmentFundamentalist(bank, this.newInvestment);
				this.lastStrategy = LastStrategy.FUNDAMENTALIST;
//				System.out.println("USES FUNDAMENTALIST STRATEGY");
				bank.setIsFundamentalist(1);
			}
		}


	}

	/**
	 * determines the amount of cash the bank needs to invest/raise in order to reach the target leverage
	 * <p> lambda = (A - C) / ( A - L ) </p>
	 * <p> Q = (A - L) targetLeverage  - A + cash </p>
	 * @param targetLeverage
	 * @return total Q<0 sell, Q>0 buy
	 */
	public double  attainTargetLeverage(final StrategyBank bank, final double targetLeverage, double effectiveEquity){
		double totalAssets=0;
		final double cash=bank.getCashReserveValue();

		for(final Contract contract : bank.getAssets()){
			totalAssets+=contract.getValue();	
		}			
		this.CurrentLeverage = this.getTotalIlliquidAssets(bank)/bank.getEquity();
		//final double q = effectiveEquity * targetLeverage  - totalAssets + cash;
		
		//double r = bank.getQuotedInterestRate();
		//double c = Math.pow((1+r),COMMERCIAL_LOAN_MATURITY)*r/(Math.pow((1+r),COMMERCIAL_LOAN_MATURITY)-1);
		//double g = (1-c/r)*(1+r)*(Math.pow((1+r),(COMMERCIAL_LOAN_MATURITY-1))-1)/r + c/r*(COMMERCIAL_LOAN_MATURITY-1);
		double leverage= targetLeverage;
		if (Simulation.getFloorTime() > 1) {
		//leverage = computeNewLeverage(bank);//targetLeverage;
		}
		
		//double leverage = g*this.totalLoanDemand/firms.size()/effectiveEquity*(firms.size()+1);
		final double q = effectiveEquity * leverage  - totalAssets + cash;
		
		
		return q;
	}
	
	public double computeNewLeverage(final StrategyBank bank) {
		double alpha = 1;
		int count=0;
		double r=0;
		double expectedReturnLoan = 0;
		for(final Contract contract : bank.getAssets()){
			if(contract instanceof Loan){
				count++;
				r=r+((Loan) contract).getInterestRate();
			}
		}
		if (count == 0) {
			expectedReturnLoan = DEFAULT_EXPECTED_RETURN ;
		} else {
			expectedReturnLoan=r*1.0/count;
		}
		
		double averageStockReturn = 0;
		for (int i=0;i<firms.size();++i) {
			double price =  ((StockReleasingFirm) firms.get(i)).getMarketValue();
			if (price > 0) {
				averageStockReturn += ((StockReleasingFirm) firms.get(i)).getDividendPerShare()/price;	
			}
			else {
				averageStockReturn += DEFAULT_EXPECTED_RETURN;
			}
		}
		averageStockReturn = averageStockReturn/firms.size();
		if (Simulation.getFloorTime() > 40) {
//			System.out.println("now");
		}
		double diff = Math.abs(averageStockReturn-expectedReturnLoan);
		//double leverageChange = this.CurrentLeverage - this.lastLeverage;
		//this.lastLeverage = this.CurrentLeverage;
		double leverageChange = this.tempTargetLeverage - this.lastLeverage;
		this.lastLeverage = this.tempTargetLeverage;
		/*
		if (diff < this.returnDifference) {
			if (leverageChange > 0) {
				newLeverage = this.CurrentLeverage + alpha*diff;
			}
			else {
				newLeverage = this.CurrentLeverage - alpha*diff;
			}
		}
		else {
			if (leverageChange < 0) {
				newLeverage = this.CurrentLeverage + alpha*diff;
			}
			else {
				newLeverage = this.CurrentLeverage - alpha*diff;
			}
		}
		*/
		if (diff < this.returnDifference) {
			if (leverageChange > 0) {
				this.tempTargetLeverage = this.tempTargetLeverage + alpha*diff;
			}
			else {
				this.tempTargetLeverage = this.tempTargetLeverage - alpha*diff;
			}
		}
		else {
			if (leverageChange < 0) {
				this.tempTargetLeverage = this.tempTargetLeverage + alpha*diff;
			}
			else {
				this.tempTargetLeverage = this.tempTargetLeverage - alpha*diff;
			}
		}
		
		this.returnDifference = diff;
		//return newLeverage;
		return this.tempTargetLeverage;
	}

	/**
	 * This function re-allocates the regularBanks investment portfolio using a trend following strategy. It considers any investment due to leverage adjustment Q and the returns of the
	 * investment opportunities. Investment opportunities with higher price increase/decrease in the last lag time-steps will take a larger/smaller part of the investment portfolio, exponential allocation is used.
	 * 
	 * @param bank
	 * @param Q - investment due to leverage adjustment
	 * @return amount of cash needed to make investment
	 */
	public double allocateInvestmentTrendFollower(final StrategyBank bank, final double Q) {
		double lastReturnLoan=0;
		final List<Double> expectedStockReturn = new ArrayList<Double>();
		final List<Double> stockPrice = new ArrayList<Double>();

		//clear previous orders
		stockOrderVolume.clear();
		stockOrderInstrument.clear();
		stockOrderPrice.clear();
		this.loanSpending = 0.0;
		this.stockSpending = 0.0;

		//this keeps track of the returns for the different stocks the bank is invested in
		bank.stockReturns.clear();



		//compute the bank's position on commercial loans 
		double currentLoanSize=0.0;// current amount invested in commercial loans
		for(final Contract contract : bank.getAssets()){
			if(contract instanceof Loan /*&& (int)(contract.getCreationTime())==(int)(Simulation.getSimState().schedule.getTime()-1)*/){
				currentLoanSize+=contract.getValue();
			}
		}

		for(int i=0;i<firms.size();++i){
			for(int j=0;j<lag-2;++j)
				pastPrice.get(i).set(j,(pastPrice.get(i).get(j+1)));
			pastPrice.get(i).set(lag-1, ((StockReleasingFirm) firms.get(i)).getMarketValue()) ;
		}

		for(int j=0;j<lag-2;++j)
			pastInterestRate.set(j,(pastInterestRate.get(j+1)));
		pastInterestRate.set(lag-1,lastReturnLoan);


		/*
		 * In the current SimulationCycleOrdering dividends are paid last from the cash the bank has accumulated. Hence the regularBanks runs the risk of investing cash
		 * it needs later on to pay dividends. Hence the bank retrieves the dividends it will have to pay from the firm and puts it "aside". Currently firms
		 * deposit their investment in its deposit account (this is obviously unrealistic). During production firms consume their investment, this is 
		 * called depreciation. This also goes out of the regularBanks cash after the bank makes its portfolio allocation. Hence it has to be put aside here just 
		 * like the dividends.
		 * 
		 */
		// This is the cash constraint on the investment the bank wants to make. The factor of 0.9 is for "security".
		
      double depreciation = 0.;
      for(final DepositAccount contract : bank.getLiabilitiesDeposits()) {
         if(contract.getDepositor() instanceof LoanStrategyFirm) {
            for(final Loan loan : 
               ((LoanStrategyFirm) contract.getDepositor()).getLiabilitiesLoans()) {
               depreciation += loan.getValue() - loan.getLoanValueAfterNextInstallment();
            }
         }
      }
      final double cash = Math.max(bank.getCashReserveValue()*0.9 - depreciation, 0.);

		//compute  returns for all stock-releasing firms. 
		//Stock return is defined as: rStock = d/P with d the dividend per share and P the price of a share

		bank.averageStockReturn = 0.0;

		for(int i=0;i<firms.size();i++) {
			double marketValue=((StockReleasingFirm) firms.get(i)).getMarketValue();

			/*
			 * Rather than estimating the stock return using the market price the bank uses the price it offered in the last time step.
			 * This is a better reflection of the actual price on the market as sometimes during long periods without trading order prices increase
			 * drastically until the next trade is made. At the high price the order size does not correspond to the desired amoun anymore.
			 */
			if (lastStockOrderPriceList.containsKey(((StockReleasingFirm) firms.get(i)).getUniqueName())) {
				marketValue = lastStockOrderPriceList.get(((StockReleasingFirm) firms.get(i)).getUniqueName());
			}

			//stockOrderInstrument.add(((StockReleasingFirm) firms.get(i)).getUniqueName());
			double dividends = 0;
			if (marketValue>0) {
				dividends = ((StockReleasingFirm) firms.get(i)).getDividendPerShare()/marketValue;
			}
			//System.out.println("DIVIDENDS: "+dividends+"MARKETVALUE: "+marketValue);
			stockPrice.add(marketValue);

			expectedStockReturn.add(dividends);

			bank.stockReturns.add(dividends);
			bank.averageStockReturn += dividends/firms.size();
		}



		//bank.assetsInLoans = currentLoanSize;
		//bank.assetsInStock = this.getTotalIlliquidAssets(bank) - bank.assetsInLoans;

		//allocation: weight of stock (same for commercial loans that are considered as a single investment opportunity) i is proportional to exp(-riskParameter*(r_max-r_i)) with r_i return of stock i and r_max maximum return
		//TODO: change to a Markowitz optimal portfolio allocation
		double max;
		if(pastInterestRate.get(0)==0) max=1.0;
		//else 		 max=(pastInterestRate.get(lag-1)-pastInterestRate.get(0))/pastInterestRate.get(0);
		else 		 max=(pastInterestRate.get(lag-1));

		for (int i=0;i<firms.size();++i) {
			if(((pastPrice.get(i)).get(lag-1) - (pastPrice.get(i)).get(0))/(pastPrice.get(i)).get(0) > max) {
				max=(pastPrice.get(i).get(lag-1)-pastPrice.get(i).get(0))/(pastPrice.get(i)).get(0);
			}
		}

		double norm2 = 0.0;
		//double norm=firms.size()*Math.exp(-riskParameter*(max-expectedReturnLoan));
		//double ret=(pastInterestRate.get(lag-1)-pastInterestRate.get(0))/pastInterestRate.get(0);
		double ret=(pastInterestRate.get(lag-1));		
		double norm=Math.exp(-trendParameter*(max-ret));
		for(int i=0;i<expectedStockReturn.size();++i){
			ret=((pastPrice.get(i).get(lag-1)-pastPrice.get(i).get(0)))/pastPrice.get(i).get(0)*(0.5 + Simulation.getSimState().random.nextDouble());
			norm+=Math.exp(-trendParameter*(max-ret));
			norm2 += Math.exp(-trendParameter*(max-ret));
		}
		final double Lx = this.getExpiringLoans(bank);
		double q = 0.0;
		//check that we don't try to get rid of more loans that the one are currently expiring, q stores remaining deleveraging need 

		//this.commercialLoanOrder=firms.size()*Math.exp(-riskParameter*(max-expectedReturnLoan))/norm*(this.getTotalIlliquidAssets(bank)+Q)-currentLoanSize+Lx;
		//bank.assetsInLoans = firms.size()*Math.exp(-riskParameter*(max-expectedReturnLoan))/norm*(this.getTotalIlliquidAssets(bank)+Q);
		//ret=(pastInterestRate.get(lag-1)-pastInterestRate.get(0))/pastInterestRate.get(0);
		ret=(pastInterestRate.get(lag-1));
		this.commercialLoanOrder=Math.exp(-trendParameter*(max-ret))/norm*(this.getTotalIlliquidAssets(bank)+Q)-currentLoanSize+Lx;
		bank.assetsInLoans = Math.exp(-trendParameter*(max-ret))/norm*(this.getTotalIlliquidAssets(bank)+Q);


		if (-this.commercialLoanOrder > Lx) {
			if (norm2 != 0.0) {
				q = (this.commercialLoanOrder + Lx)/norm2*norm;
			}
			this.commercialLoanOrder=0.0;
		}
		else if (this.commercialLoanOrder < 0) {
			this.commercialLoanOrder = 0.0;
		}

		// check whether investment decision satisfies cash constraint

		double cashCheck=this.commercialLoanOrder;
		final List<Double> stockOrderVolumeBag = new ArrayList<Double>();
		//double z=this.commercialLoanOrder;//z is a normalization factor

		double temp = 0.0;

		for(int i=0;i<firms.size();++i){
			ret=((pastPrice.get(i).get(lag-1)-pastPrice.get(i).get(0)))/pastPrice.get(i).get(0);
			final double stockOrderVolume  = Math.exp(-trendParameter*(max-ret))/norm*(this.getTotalIlliquidAssets(bank)+Q+q)-this.getStockAmount(bank, (StockReleasingFirm) firms.get(i));
			temp += Math.exp(-trendParameter*(max-ret))/norm*(this.getTotalIlliquidAssets(bank)+Q+q);
			stockOrderVolumeBag.add(stockOrderVolume);
			cashCheck += Math.max(0, stockOrderVolume); // use negative sign here because of sign flip in logic, see below negative sign reappears
		}

		bank.assetsInStock = temp;


		// adjust investment if cash constraint is exceeded
		if (cashCheck > cash) {
			this.commercialLoanOrder = this.commercialLoanOrder/cashCheck*cash;
		}

		// to test if works with stock market off, comment out if not testing.
		/*
		if (Q > cash) {Q = cash;}
		this.commercialLoanOrder=Q+Lx;
		if (this.commercialLoanOrder < 0) {
			this.commercialLoanOrder = 0.0;
		}
		 */

		if (this.StockMarketOn == false) {
			double qCopy = Q;
			if (Q > cash) {qCopy = cash;}
			this.commercialLoanOrder=qCopy+Lx;
			if (this.commercialLoanOrder < 0) {
				this.commercialLoanOrder = 0.0;
			}
		}

		double spend = this.commercialLoanOrder;
		this.loanSpending = this.commercialLoanOrder;

		for(int i=0;i<expectedStockReturn.size();++i){
			double A = 1.0;
			if (cashCheck > cash) {
				A = cash/cashCheck;
			}
			// retrieve price for this offer on stock market, use to compute actual size of stock order, there should only be one stock market, still 
			// need to do iteration because markets are returned as Set which does not provide get function
			double p = 0.0;
			for (final Market market : bank.getMarketsOfType(StockMarket.class)) {
				p = getPrice(((StockReleasingFirm) firms.get(i)).getUniqueName(),-stockOrderVolumeBag.get(i),  DELTA_STOCK_PRICE,market);
			}
			//System.out.println("Predicted stock price: "+p+" for stock: "+((StockReleasingFirm) firms.get(i)).getUniqueName());

			spend += Math.max(0,-stockOrderVolumeBag.get(i)*A);
			double stockOrderSize= stockOrderVolumeBag.get(i)*A/p;

			final double numberOfStockOwned = ((StockTradingBank) bank ).getNumberOfSharesOwnedIn(((StockReleasingFirm) firms.get(i)).getUniqueName());
			if (stockOrderSize<0 && -stockOrderSize > numberOfStockOwned) {
				stockOrderSize=-numberOfStockOwned;
			}
			if(stockOrderSize!=0.0){
				this.stockOrderVolume.add(-stockOrderSize);
				stockOrderInstrument.add(((StockReleasingFirm) firms.get(i)).getUniqueName());
				stockOrderPrice.add(p);
			}
			// Uncomment if testing without stock market, also need to comment out body of "considerStockMarket" function
			if (this.StockMarketOn == false) {
				this.stockOrderVolume.add(0.0);
			}


		}
		this.stockSpending = spend - this.loanSpending;
		return spend;

	}

	/**
	 * This function re-allocates the regularBanks investment portfolio based on a fundamentalist investment strategy. It considers any investment due to leverage adjustment Q and the returns of the
	 * investment opportunities. Higher return opportunities will take a larger part of the investment portfolio, exponential allocation is used.
	 * 
	 * @param bank
	 * @param Q - investment due to leverage adjustment
	 * @return amount of cash needed to make investment
	 */
	public double allocateInvestmentFundamentalist(final StrategyBank bank, final double Q) {
		double expectedReturnLoan=0;

		final List<Double> expectedStockReturn = new ArrayList<Double>();
		final List<Double> stockPrice = new ArrayList<Double>();

		//clear previous orders
		stockOrderVolume.clear();
		stockOrderInstrument.clear();
		stockOrderPrice.clear();
		this.loanSpending = 0.0;
		this.stockSpending = 0.0;

		//this keeps track of the returns for the different stocks the bank is invested in
		bank.stockReturns.clear();

		//=========================================================================================
		// this is actually part of the trend following strategy but still needs to be updated here
		double lastReturnLoan = 0.0;

		for(int i=0;i<firms.size();++i){
			for(int j=0;j<lag-2;++j)
				pastPrice.get(i).set(j,(pastPrice.get(i).get(j+1)));
			pastPrice.get(i).set(lag-1, ((StockReleasingFirm) firms.get(i)).getMarketValue()) ;
		}

		for(int j=0;j<lag-2;++j)
			pastInterestRate.set(j,(pastInterestRate.get(j+1)));
		pastInterestRate.set(lag-1,lastReturnLoan);

		//==========================================================================================

		/*
		 * In the current SimulationCycleOrdering dividends are paid last from the cash the bank has accumulated. Hence the regularBanks runs the risk of investing cash
		 * it needs later on to pay dividends. Hence the bank retrieves the dividends it will have to pay from the firm and puts it "aside". Currently firms
		 * deposit their investment in its deposit account (this is obviously unrealistic). During production firms consume their investment, this is 
		 * called depreciation. This also goes out of the regularBanks cash after the bank makes its portfolio allocation. Hence it has to be put aside here just 
		 * like the dividends.
		 * 
		 */
		double dividendsToBePaid = 0.0;
		double depreciation = 0.0;
		// This is the cash constraint on the investment the bank wants to make. The factor of 0.9 is for "security".
		final double cash = Math.max(bank.getCashReserveValue()*0.9 - dividendsToBePaid-depreciation,0);

		//compute  returns for all stock-releasing firms. 
		//Stock return is defined as: rStock = d/P with d the dividend per share and P the price of a share

		bank.averageStockReturn = 0.0;

		for(int i=0;i<firms.size();i++) {
			double marketValue=((StockReleasingFirm) firms.get(i)).getMarketValue();


			/*
			 * Rather than estimating the stock return using the market price the bank uses the price it offered in the last time step.
			 * This is a better reflection of the actual price on the market as sometimes during long periods without trading order prices increase
			 * drastically until the next trade is made. At the high price the order size does not correspond to the desired amoun anymore.
			 */
			if (lastStockOrderPriceList.containsKey(((StockReleasingFirm) firms.get(i)).getUniqueName())) {
				marketValue = lastStockOrderPriceList.get(((StockReleasingFirm) firms.get(i)).getUniqueName());
			} 

			//stockOrderInstrument.add(((StockReleasingFirm) firms.get(i)).getUniqueName());
			double dividends = 0;
			if (marketValue>0) {
				dividends = ((StockReleasingFirm) firms.get(i)).getDividendPerShare()/marketValue;
			}
//			System.out.println("DIVIDENDS: "+dividends+" MARKETVALUE: "+marketValue+" PROFIT: "+profit+" NUMBEROFSHARES: "+numberOfShares);
			stockPrice.add(marketValue);
			
			
			// comment out for cumulative stock return ===================================
			expectedStockReturn.add(dividends);
			// ===================================

			//bank.stockReturns.add(dividends);
			bank.averageStockReturn += dividends/firms.size();
			
			// cumulative stock returns ===================================
			if (totalStockReturns.containsKey(((StockReleasingFirm) firms.get(i)).getUniqueName()) == true) {
				double old = totalStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName());
				totalStockReturns.put(((StockReleasingFirm) firms.get(i)).getUniqueName(), old + dividends);
			}
			else {
				totalStockReturns.put(((StockReleasingFirm) firms.get(i)).getUniqueName(), dividends);
			}
			//expectedStockReturn.add(totalStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName()));
			//System.out.println("CUMULATIVE STOCK RETURN: "+totalStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName()));
			bank.stockReturns.add(totalStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName()));
			//=============================================================

			// TODO see whether we can get this (using average stock returns) to work... postpone for now (28.1.13)
			if (this.averageStockReturns.containsKey(((StockReleasingFirm) firms.get(i)).getUniqueName()) ==true) {
				double av = this.averageStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName())*(1-1/this.tau)+dividends/this.tau;
				this.averageStockReturns.put(((StockReleasingFirm) firms.get(i)).getUniqueName(), av);
				//expectedStockReturn.add(av);
			}
			else {
				this.averageStockReturns.put(((StockReleasingFirm) firms.get(i)).getUniqueName(), dividends);
				//expectedStockReturn.add(dividends);
			}
		}

		//compute return for all commercial loans previously issued by the bank
		/*
			double currentLoanSize=0.0;// current amount invested in commercial loans
			for(final Contract contract : bank.getAssets()){
				double r=0;
				int count=0;
				if(contract instanceof Loan){
					count++;
					r=r+((Loan) contract).getInterestRate();
					currentLoanSize+=contract.getValue();
				}
				if (count == 0) {
					expectedReturnLoan = DEFAULT_EXPECTED_RETURN ;
				} else {
					expectedReturnLoan=r*1.0/count;
				}
			}
		 */
		double currentLoanSize=0.0;// current amount invested in commercial loans
		int count=0;
		double r=0;
		for(final Contract contract : bank.getAssets()){
			if(contract instanceof Loan){
				count++;
				r=r+((Loan) contract).getInterestRate();
				currentLoanSize+=contract.getValue();
			}
		}
		if (count == 0) {
			expectedReturnLoan = DEFAULT_EXPECTED_RETURN ;
		} else {
			expectedReturnLoan=r*1.0/count;
		}// TODO substract risk discount from expectedReturnLoan
		this.averageLoanReturn = this.averageLoanReturn*(1-1/this.tau)+expectedReturnLoan/this.tau;
		
		// average returns ========================================================
		double maxAv = this.averageLoanReturn;
		for (int i=0;i<averageStockReturns.size();++i) {
			if (averageStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName()) > maxAv) {
				maxAv = averageStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName());
			}
		}
		Math.exp(-riskParameter*(maxAv-averageLoanReturn));
		for (int i=0;i<averageStockReturns.size();++i) {
				Math.exp(-riskParameter*(maxAv-averageStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName())));
		}

		
		//  =======================================================================
		
		
		
		//expectedReturnLoan = this.totalLoanReturn;
		bank.setLoanReturn(expectedReturnLoan);
		//System.out.println("CUMULATIVE LOAN RETURN: "+expectedReturnLoan);
		//=============================================================
		
		//expectedReturnLoan = this.averageLoanReturn;

		//bank.assetsInLoans = currentLoanSize;
		//bank.assetsInStock = this.getTotalIlliquidAssets(bank) - bank.assetsInLoans;

		//allocation: weight of stock (same for commercial loans that are considered as a single investment opportunity) i is proportional to exp(-riskParameter*(r_max-r_i)) with r_i return of stock i and r_max maximum return
		//TODO: change to a Markowitz optimal portfolio allocation
		double max=expectedReturnLoan;
		double av  = expectedReturnLoan;
		for (int i=0;i<expectedStockReturn.size();++i) {
			av += expectedStockReturn.get(i);
			if(expectedStockReturn.get(i)>max) {
				max=expectedStockReturn.get(i);
			}
		}
		av = av/(expectedStockReturn.size() +1);
		double norm2 = 0.0;
		//double norm=firms.size()*Math.exp(-riskParameter*(max-expectedReturnLoan));
		double norm=Math.exp(-riskParameter*(max-expectedReturnLoan)); //TODO add exp(0)=1 for interest rate 0 on rsk free assets
		for(int i=0;i<expectedStockReturn.size();++i){
			norm+=Math.exp(-riskParameter*(max-expectedStockReturn.get(i)));
			norm2 += Math.exp(-riskParameter*(max-expectedStockReturn.get(i)));
		}
		final double Lx = this.getExpiringLoans(bank);
		double q = 0.0;
		//check that we don't try to get rid of more loans that the one are currently expiring, q stores remaining deleveraging need 

		//this.commercialLoanOrder=firms.size()*Math.exp(-riskParameter*(max-expectedReturnLoan))/norm*(this.getTotalIlliquidAssets(bank)+Q)-currentLoanSize+Lx;
		//bank.assetsInLoans = firms.size()*Math.exp(-riskParameter*(max-expectedReturnLoan))/norm*(this.getTotalIlliquidAssets(bank)+Q);

		//double p1 = Math.exp(-riskParameter*(max-expectedReturnLoan))/norm;
		//double Aill = this.getTotalIlliquidAssets(bank);
		
		// recompute weights ===================
		double alpha = 22.0;//0.1;
		//double loanWeight = bank.getLoanInvestment()/this.getTotalIlliquidAssets(bank) + alpha*(expectedReturnLoan - av);//*(1+0.1*Math.tan(expectedReturnLoan - av));//
		List<Double> stockWeights = new ArrayList<Double>();
		for (int i=0;i<expectedStockReturn.size();++i) {
			double w = this.getStockAmount(bank, (StockReleasingFirm) firms.get(i))/this.getTotalIlliquidAssets(bank)+ alpha*(expectedStockReturn.get(i)-av);//*(1+0.1*Math.tan(expectedStockReturn.get(i) - av));// 
			stockWeights.add(w);
		}
			
		// =====================================
		
		this.commercialLoanOrder=Math.exp(-riskParameter*(max-expectedReturnLoan))/norm*(this.getTotalIlliquidAssets(bank)+Q)-currentLoanSize+Lx;
		//this.commercialLoanOrder=Math.exp(-riskParameter*(maxAv-averageLoanReturn))/normAv*(this.getTotalIlliquidAssets(bank)+Q)-currentLoanSize+Lx;
		//this.commercialLoanOrder=loanWeight/sumWeights*(this.getTotalIlliquidAssets(bank)+Q)-currentLoanSize+Lx;
		bank.assetsInLoans = Math.exp(-riskParameter*(max-expectedReturnLoan))/norm*(this.getTotalIlliquidAssets(bank)+Q);


		if (-this.commercialLoanOrder > Lx) {
			if (norm2 != 0.0) {
				q = (this.commercialLoanOrder + Lx)/norm2*norm;
			}
			this.commercialLoanOrder=0.0;
		}
		else if (this.commercialLoanOrder < 0) {
			this.commercialLoanOrder = 0.0;
		}

		// check whether investment decision satisfies cash constraint

		double cashCheck=this.commercialLoanOrder;
		final List<Double> stockOrderVolumeBag = new ArrayList<Double>();
		//double z=this.commercialLoanOrder;//z is a normalization factor

		double temp = 0.0;

		for(int i=0;i<expectedStockReturn.size();++i){
			final double stockOrderVolume  = Math.exp(-riskParameter*(max-expectedStockReturn.get(i)))/norm*(this.getTotalIlliquidAssets(bank)+Q+q)-this.getStockAmount(bank, (StockReleasingFirm) firms.get(i));
			//final double stockOrderVolume  = Math.exp(-riskParameter*(maxAv-averageStockReturns.get(((StockReleasingFirm) firms.get(i)).getUniqueName())))/normAv*(this.getTotalIlliquidAssets(bank)+Q+q)-this.getStockAmount(bank, (StockReleasingFirm) firms.get(i));
			//final double stockOrderVolume  = stockWeights.get(i)/sumWeights*(this.getTotalIlliquidAssets(bank)+Q+q)-this.getStockAmount(bank, (StockReleasingFirm) firms.get(i));
			temp += Math.exp(-riskParameter*(max-expectedStockReturn.get(i)))/norm*(this.getTotalIlliquidAssets(bank)+Q+q);
			stockOrderVolumeBag.add(stockOrderVolume);
			cashCheck += Math.max(0, stockOrderVolume); // use negative sign here because of sign flip in logic, see below negative sign reappears
		}

		bank.assetsInStock = temp;


		// adjust investment if cash constraint is exceeded
		if (cashCheck > cash) {
			this.commercialLoanOrder = this.commercialLoanOrder/cashCheck*cash;
		}
		
		// to test if works with stock market off, comment out if not testing.
		if (this.StockMarketOn == false) {
			double qCopy = Q;
			if (Q > cash) {qCopy = cash;}
			this.commercialLoanOrder=qCopy+Lx;
			if (this.commercialLoanOrder < 0) {
				this.commercialLoanOrder = 0.0;
			}
		}
	
		double spend = this.commercialLoanOrder;
		this.loanSpending = this.commercialLoanOrder;

		for(int i=0;i<expectedStockReturn.size();++i){
			double A = 1.0;
			if (cashCheck > cash) {
				A = cash/cashCheck;
			}
			// retrieve price for this offer on stock market, use to compute actual size of stock order, there should only be one stock market, still 
			// need to do iteration because markets are returned as Set which does not provide get function
			double p = 0.0;
			for (final Market market : bank.getMarketsOfType(StockMarket.class)) {
				p = getPrice(((StockReleasingFirm) firms.get(i)).getUniqueName(),-stockOrderVolumeBag.get(i),  DELTA_STOCK_PRICE,market);
			}

			spend += Math.max(0,-stockOrderVolumeBag.get(i)*A);
			double stockOrderSize= stockOrderVolumeBag.get(i)*A/p;

			final double numberOfStockOwned = ((StockTradingBank) bank ).getNumberOfSharesOwnedIn(((StockReleasingFirm) firms.get(i)).getUniqueName());
			if (stockOrderSize<0 && -stockOrderSize > numberOfStockOwned) {
				stockOrderSize=-numberOfStockOwned;
			}
			if(stockOrderSize!=0.0){
				//if(stockOrderSize>3.0 || stockOrderSize<-3.0){

				this.stockOrderVolume.add(-stockOrderSize);
				stockOrderInstrument.add(((StockReleasingFirm) firms.get(i)).getUniqueName());
				stockOrderPrice.add(p);
			}
			// Uncomment if testing without stock market, also need to comment out body of "considerStockMarket" function
			if (this.StockMarketOn == false) {
				this.stockOrderVolume.add(0.0);
			}

		}
		this.stockSpending = spend - this.loanSpending;
		
		bank.setLoanSupply(this.commercialLoanOrder);
		
		return spend;

	}
	/**
	 * This function places loan orders. It is called first in the bank's schedule order. It therefore also calls the investment allocation function.
	 */
	@Override
	public void considerCommercialLoanMarkets() {
		allocateInvestment(getBank());
		// first remove the current commercial orders, note that the cancel
		// method removes the order both from the market and this bank. 
		for (final Order order : getBank().getOrders()) {
			if (order instanceof CommercialLoanOrder){
				((CommercialLoanOrder)order).cancel();
			}
		}

		for (final Order order : getBank().getOrders()) {
			if (order instanceof StockOrder){
				((StockOrder)order).cancel();
			}
		}
		/*
		 * Here orders are placed on the market. Price is retrieved from getPrice function in PriceStrategy.
		 */

		if (commercialLoanOrder > 0) {	// send order
			//if (bank.getMarkets().containsKey( CommercialLoanMarket.class )) {
			for (final Market market : getBank().getMarketsOfType(CommercialLoanMarket.class)) {
				try {
					double previousRate = this.lendingRate;
					if (lastOrders.get(LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY)) != null) {
						final double executedSize = lastOrders.get(LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY)).getExecutedSize();
						final double lastOrderSize = lastOrders.get(LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY)).getSize();
						getBank().setLoanSuccess(Math.abs(executedSize/lastOrderSize));
					}
					else {
					   getBank().setLoanSuccess(0);
					}
					this.lendingRate = getLoanPrice(LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY),commercialLoanOrder, DELTA_LOAN_PRICE,market);
					getBank().setQuotedInterestRate(this.lendingRate);
					if (previousRate > this.lendingRate) {
					   getBank().setSupplyMatched(false);
					}
					else {
					   getBank().setSupplyMatched(true);
					}
					//this.lendingRate = 0.2245;
					final Order lastOrder = ((CommercialLoanMarket) market).addOrder(getBank(),
							COMMERCIAL_LOAN_MATURITY ,
							commercialLoanOrder,
							lendingRate);
					this.lastOrders.put(LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY), lastOrder);
//					System.out.println("LoanOrder " + LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY) + " volume: " + commercialLoanOrder + " price: " + lendingRate);
				} catch (final OrderException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				} catch (AllocationException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				}
			}
			//}
		}
	}

	/**
	 * This function places stock orders it is called second in the bank's schedule order.
	 */
	@Override
   public void considerStockMarkets() {
		
		if (this.StockMarketOn == true) {
			// comment out this function body if you want to test without stock market.
			
			// first remove the current commercial orders, note that the cancel
			// method removes the order both from the market and this bank. 
			for (final Order order : getBank().getOrders()) {
				if (order instanceof StockOrder){
					((StockOrder)order).cancel();
				}
			}

			getBank().averageStockOrderPrice = 0.0;

			for(int i=0;i<stockOrderVolume.size();i++) {
				if (stockOrderVolume.get(i) > 0.1 || stockOrderVolume.get(i) < -0.1 ) {
					//if (stockOrderVolume.get(i) > 3.0 || stockOrderVolume.get(i) < -3.0 ) {

					//if (bank.getMarkets().containsKey( StockMarket.class )) {
					for (final Market market : getBank().getMarketsOfType(StockMarket.class)) {
						try {
							// keep track of some statistics for the regularBanks
							this.lastStockOrderPrice = stockOrderPrice.get(i);//getPrice(stockOrderInstrument.get(i),stockOrderVolume.get(i),  DELTA_STOCK_PRICE,market);
							//double checkPrice = getPrice(stockOrderInstrument.get(i),stockOrderVolume.get(i),  DELTA_STOCK_PRICE,market);

							this.lastStockOrderPriceList.put(stockOrderInstrument.get(i), this.lastStockOrderPrice);

							getBank().averageStockOrderPrice += this.lastStockOrderPrice/stockOrderVolume.size();

							final Order lastOrder = ((StockMarket) market).addOrder(getBank(),
									stockOrderInstrument.get(i),
									stockOrderVolume.get(i),
									lastStockOrderPrice);	
							this.lastOrders.put(stockOrderInstrument.get(i), lastOrder);
//							System.out.println("StockOrder Bank" + stockOrderInstrument.get(i) + " volume: " + stockOrderVolume.get(i) + " price: " + lastStockOrderPrice);
						} catch (AllocationException e) {
							e.printStackTrace();
							Simulation.getSimState().finish();
						} catch (final OrderException e) {
							e.printStackTrace();
							Simulation.getSimState().finish();
						}
					}
					//}
				}
			}
		}
		// record value for loan demand and supply
		this.totalLoanDemand = this.computeTotalLoanDemand();// market.getInstrument(LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY)).getTotal_BidQuantity(); // buy - supply
        this.totalLoanSupply = this.computeTotalLoanSupply();//market.getInstrument(LoanInstrument.generateTicker(COMMERCIAL_LOAN_MATURITY)).getTotal_AskQuantity(); // sell - demand
		
	}

	/**
	 * This function usually places orders on the interbank market. It is called last in the bank's schedule order. We do not implement an interbank in this strategy,
	 * instead we use this function to do some accounting and pay the bank's dividends determined in the allocateInvestment function.
	 */

	@Override
	public void considerInterbankMarkets() {

		// Compute profit from stock trading
		this.stockProfit = 0.0;
		int count = 0;
		for (Object firm : this.firms.objs) {
			if (firm instanceof StockReleasingFirm) {
				double lastPrice = this.pastPrice.get(count).get(lag-1);
				double newPrice = ((StockReleasingFirm) firm).getMarketValue();
				if (getBank().hasShareIn(((StockReleasingFirm) firm).getUniqueName()) == true) {
					double numberOfSharesOwned = getBank().getStockAccount(((StockReleasingFirm) firm).getUniqueName()).getQuantity();
					this.stockProfit += numberOfSharesOwned*(newPrice - lastPrice);
				}
			}
		}



		if (getBank().getCashReserveValue() < this.dividendsToBePaid) {
			//this.dividendsToBePaid = 0.5*bank.getCashReserveValue();
			this.dividendsToBePaid = 0.1*getBank().getCashReserveValue();
		}
		//bank.credit(EQUITY_PAYOUT*(bank.getEquity()-bank.getInitialEquity()));
		this.bankDividend = this.dividendsToBePaid;
		getBank().setDividendPerShare(this.dividendsToBePaid / getBank().getNumberOfEmittedShares());

		//this.CurrentLeverage = this.getTotalIlliquidAssets(bank)/bank.getEquity();

	}


	public List<Double> getStockOrderVolume() {
		return stockOrderVolume;
	}

	// TODO setFirms would be better - or something like this.firms.addAll(firms)
	public void addFirms(final Bag firms) {
		this.firms = firms;
	}

	public double getRiskParameter() {
		return riskParameter;
	}

	public void setRiskParameter(final double riskParameter) {
		this.riskParameter = riskParameter;
	}

	public List<String> getStockOrderInstrument() {
		return stockOrderInstrument;
	}

	public Double getCommertialLoanOrderVolume() {
		return commercialLoanOrder;
	}


	public double getTargetLeverage() {
		return targetLeverage;
	}


	public void setTargetLeverage(final double targetLeverage) {
		this.targetLeverage = targetLeverage;
	}


	public double getNewInvestment() {
		return newInvestment;
	}

	public double getTotalIlliquidAssets(final StrategyBank bank) {
		final double cash=bank.getCashReserveValue();
		double totalAssets=0.0;
		for(final Contract contract : bank.getAssets()){
			totalAssets+=contract.getValue();	

		}
		return totalAssets-cash;
	}


	public double getExpiringLoans(final StrategyBank bank){
		double expiringLoans=0.0;
		for(final Contract contract : bank.getAssets()){
			if (contract.getExpirationTime() == Simulation.getSimState().schedule.getTime()) {
				expiringLoans = expiringLoans + contract.getValue() - ((Loan) contract).getLoanValueAfterNextInstallment();
			}
		}			
		return expiringLoans;
	}

	public double getStockAmount(final StrategyBank bank, final StockReleasingFirm firm){
		double q=0.0;
		if (((StockTradingBank) bank).hasShareIn(firm.getUniqueName())) {
			q = ((StockTradingBank) bank).getStockAccount(firm.getUniqueName()).getValue();
		}
		return q;
	}

	public double getDepositAmount(final StrategyBank bank) {
		double depositAmount = 0;
		for (final Contract contract : bank.getLiabilitiesDeposits()) {
			depositAmount += ((DepositAccount) contract).getValue();
		}
		return depositAmount;
	}




	@Override
	public double getDepositRate() {
		return 0;
	}


	@Override
	public double getLendingRate() {
		return lendingRate;
	}

	public boolean isInterbankMarketOn() {
		return InterbankMarketOn;
	}

	public void setInterbankMarketOn(boolean interbankMarketOn) {
		InterbankMarketOn = interbankMarketOn;
	}

	public boolean isStockMarketOn() {
		return StockMarketOn;
	}

	public void setStockMarketOn(boolean stockMarketOn) {
		StockMarketOn = stockMarketOn;
	}

	public double getCurrentLeverage() {
		return CurrentLeverage;
	}

	public void setPastPrice() {
		// TODO Auto-generated method stub
		for(	int i=0;i<this.firms.size();++i){
			List<Double> tempList = new ArrayList<Double>();
			for(int j=0;j<this.lag;++j)
				tempList.add((((StockReleasingFirm) firms.get(i)).getMarketValue()));
			pastPrice.add(tempList);
		}
	}

	public void setPastInterestRate() {
		// TODO Auto-generated method stub
		for(int j=0;j<this.lag;++j)
			this.pastInterestRate.add(1.0);
	}

	public Map<String,Double> getLendingCashflow() {
		return lendingCashflow;
	}

	public void setLendingCashflow(String firmID,double cashflow) {
		if (this.lendingCashflow.containsKey(firmID)) {
			double update = this.lendingCashflow.get(firmID)+cashflow;
			this.lendingCashflow.put(firmID, update);
		}
		else{
			this.lendingCashflow.put(firmID, cashflow);
		}

	}

	public Map<String,Double> getStockTradingCashflow() {
		return stockTradingCashflow;
	}

	public void setStockTradingCashflow(String firmID,double cashflow) {
		if (this.stockTradingCashflow.containsKey(firmID)) {
			double update = this.stockTradingCashflow.get(firmID)+cashflow;
			this.stockTradingCashflow.put(firmID, update);
		}
		else{
			this.stockTradingCashflow.put(firmID, cashflow);
		}
	}

	public Map<String, Double> getDividendCashflow() {
		return dividendCashflow;
	}

	public void setDividendCashflow(String firmID,double cashflow) {
		if (this.dividendCashflow.containsKey(firmID)) {
			double update = this.dividendCashflow.get(firmID)+cashflow;
			this.dividendCashflow.put(firmID, update);
		}
		else{
			this.dividendCashflow.put(firmID, cashflow);
		}
	}

	public double totalLendingCashFlow() {
		double sum = 0.0;
		for (double value : this.lendingCashflow.values()) {
			sum += value;	
		}
		return sum;
	}

	public double totalStockTradingCashFlow() {
		double sum = 0.0;
		for (double value : this.stockTradingCashflow.values()) {
			sum += value;	
		}
		return sum;
	}

	public double totalDividendCashFlow() {
		double sum = 0.0;
		for (double value : this.dividendCashflow.values()) {
			sum += value;	
		}
		return sum;
	}

	public double getValueStockPortfolio(StrategyBank bank) {
		double value=0.0;
		for(final Contract contract : bank.getAssets()){
			if (contract instanceof StockAccount) {
				value += ((StockAccount) contract).getValue();
			}
		}
		return value;
	}

	public double getValueCommercialLoanPortfolio(StrategyBank bank) {
		double value=0.0;
		for(final Contract contract : bank.getAssets()){
			if (contract instanceof Loan) {
				if (((Loan) contract).getBorrower() instanceof Firm) {
					value += ((Loan) contract).getValue();
				}
			}
		}
		return value;
	}

	public double getValueInterbankLoanPortfolio(StrategyBank bank) {
		double value=0.0;
		for(final Contract contract : bank.getAssets()){
			if (contract instanceof Loan) {
				// if (((Loan) contract).getBorrower() instanceof MixedStrategyBank) {
				//	value += ((Loan) contract).getValue();
				// }
			}
		}
		return value;
	}

	public double getStockSpending() {
		return stockSpending;
	}

	public double getLoanSpending() {
		return loanSpending;
	}

	public Map<String,Double> getInterbankCashflow() {
		return interbankCashflow;
	}

	public void setInterbankCashflow(String firmID, double cashflow) {
		if (this.interbankCashflow.containsKey(firmID)) {
			double update = this.interbankCashflow.get(firmID)+cashflow;
			this.interbankCashflow.put(firmID, update);
		}
		else{
			this.interbankCashflow.put(firmID, cashflow);
		}
	}

	public double totalInterbankCashflow() {
		double sum = 0.0;
		for (double value : this.interbankCashflow.values()) {
			sum += value;	
		}
		return sum;
	}

	public double getInterbankSpending() {
		return interBankSpending;
	}

	public double getStockProfit() {
		return stockProfit;
	}

	public double getBankDividendPayment() {
		return bankDividend;
	}

	public boolean isMixedStrategiesOn() {
		return mixedStrategiesOn;
	}

	public void setMixedStrategiesOn(boolean mixedStrategiesOn) {
		this.mixedStrategiesOn = mixedStrategiesOn;
	}

	/**
	 * This is a general function to compute the price for a given instrument
	 * TODO Think about what to do when previous order was a sell and its a buy and vice versa
	 * @return updated price
	 */
	public double getPrice(final String instrumentName,final double orderSize, final double dPrice, final Market market) {
		
		if (lastOrders.containsKey(instrumentName)) {
			final double executedSize = lastOrders.get(instrumentName).getExecutedSize();
			//System.out.println("executedSize: "+executedSize);
			final double lastOrderSize = lastOrders.get(instrumentName).getSize();
			final double lastOrderPrice = lastOrders.get(instrumentName).getPrice();

			double newPrice = 0;
			final double threshold = 1.0;//0.98;
			/*
			if (lastOrderPrice >1e6) {
				System.out.println("we have problem here...");
			}
			 */
			if (orderSize > 0) {
				// sell order
				if (Math.abs(executedSize/lastOrderSize) < threshold) {
					newPrice =  lastOrderPrice*(1-dPrice);//lastOrderPrice - dPrice;lastOrderPrice - dPrice;//
					//newPrice = lastOrderPrice - dPrice;//

				}
				else {
					final double r = Simulation.getSimState().random.nextDouble();
					if (r < 1.0) {
						newPrice =  lastOrderPrice*(1+dPrice/(1-dPrice));//lastOrderPrice + dPrice;lastOrderPrice + dPrice;//
						//newPrice = lastOrderPrice + dPrice;//


					}
					else {
						newPrice =  lastOrderPrice;
					}
				}
			}
			else {
				// buy order
				if (Math.abs(executedSize/lastOrderSize) < threshold) {
					newPrice =  lastOrderPrice*(1+dPrice/(1-dPrice));//lastOrderPrice + dPrice;lastOrderPrice + dPrice;//
					//newPrice = lastOrderPrice + dPrice;//


				}
				else {
					final double r = Simulation.getSimState().random.nextDouble();
					if (r < 1.0) {
						newPrice =  lastOrderPrice*(1-dPrice);//lastOrderPrice - dPrice;lastOrderPrice - dPrice;//
						//newPrice = lastOrderPrice - dPrice;//

					}
					else {
						newPrice =  lastOrderPrice;
					}
				}
			}
			return Math.max(newPrice, 1e-12);
		}
		else {
			// initial values: for test purposes only. TODO come up with some good initialization
			if (market instanceof StockMarket) {
				return this.initialStockPrice;//1.0e9;//3e8;//2e5; // this is the initial stock price, also needs to be adjusted in stockReleasing Firm
			}
			else {
				if (orderSize > 0) {
//					System.out.println("No order for this instrument name has been submitted in the past, assume price = 15.0.");
					return this.initialInterestRate;//.03; // this is the initial lending rate
				}
				else {
//					System.out.println("No order for this instrument name has been submitted in the past, assume price = 5.0.");
					return this.initialInterestRate;//.03; // this is the initial lending rate
				}
			}
		}
	}
	
	public double getLoanPrice(final String instrumentName,final double orderSize, final double dPrice, final Market market) {
		
		if (lastOrders.containsKey(instrumentName)) {
			final double executedSize = lastOrders.get(instrumentName).getExecutedSize();
			//System.out.println("executedSize: "+executedSize);
			final double lastOrderSize = lastOrders.get(instrumentName).getSize();
			final double lastOrderPrice = lastOrders.get(instrumentName).getPrice();

			double newPrice = 0;
			final double threshold = 1.0;//0.98;
			/*
			if (lastOrderPrice >1e6) {
				System.out.println("we have problem here...");
			}
			 */
			if (orderSize > 0) {
				// sell order
				if (Math.abs(executedSize/lastOrderSize) < threshold) {
					newPrice =  lastOrderPrice*(1-dPrice);//lastOrderPrice - dPrice;lastOrderPrice - dPrice;//
					//newPrice = lastOrderPrice - dPrice;//

				}
				else {
					final double r = Simulation.getSimState().random.nextDouble();
					if (r < 1.0) {
						//if (Simulation.getFloorTime() > 100) {
						//	System.out.println("now");
						if (this.totalLoanDemand > this.totalLoanSupply) {
						newPrice =  lastOrderPrice*(1+dPrice/(1-dPrice));}//lastOrderPrice + dPrice;lastOrderPrice + dPrice;//
						//newPrice = lastOrderPrice + dPrice;// 
						else {
							newPrice = lastOrderPrice;
						}
						//}
						//else {
						//	newPrice =  lastOrderPrice*(1+dPrice/(1-dPrice));
						//}

					}
					else {
						newPrice =  lastOrderPrice;
					}
				}
			}
			else {
				// buy order
				if (Math.abs(executedSize/lastOrderSize) < threshold) {
					newPrice =  lastOrderPrice*(1+dPrice/(1-dPrice));//lastOrderPrice + dPrice;lastOrderPrice + dPrice;//
					//newPrice = lastOrderPrice + dPrice;//


				}
				else {
					final double r = Simulation.getSimState().random.nextDouble();
					if (r < 1.0) {
						newPrice =  lastOrderPrice*(1-dPrice);//lastOrderPrice - dPrice;lastOrderPrice - dPrice;//
						//newPrice = lastOrderPrice - dPrice;//

					}
					else {
						newPrice =  lastOrderPrice;
					}
				}
			}
			return Math.max(newPrice, 1e-12);
		}
		else {
			// initial values: for test purposes only. TODO come up with some good initialization
			if (market instanceof StockMarket) {
				return this.initialStockPrice;//1.0e9;//3e8;//2e5; // this is the initial stock price, also needs to be adjusted in stockReleasing Firm
			}
			else {
				if (orderSize > 0) {
//					System.out.println("No order for this instrument name has been submitted in the past, assume price = 15.0.");
					return this.initialInterestRate;//.03; // this is the initial lending rate
				}
				else {
//					System.out.println("No order for this instrument name has been submitted in the past, assume price = 5.0.");
					return this.initialInterestRate;//.03; // this is the initial lending rate
				}
			}
		}
	}

	public double getAltEquity() {
		return altEquity;
	}

	public double getInitialStockPrice() {
		return initialStockPrice;
	}

	public void setInitialStockPrice(double initialStockPrice) {
		this.initialStockPrice = initialStockPrice;
	}

	public double getInitialInterestRate() {
		return initialInterestRate;
	}

	public void setInitialInterestRate(double initialInterestRate) {
		this.initialInterestRate = initialInterestRate;
	}
	/*
	public double computeTotalLoanDemand() {
		double demand = 0;
		for (Object firm : model.getFirms().objs) {
			if (firm != null) {
			demand += ((LoanStrategyFirm) firm).getAskedLoan();
			}
		}
		return demand;
	}
	
	public double computeTotalLoanSupply() {
		double supply = 0;
		for (Object bank : model.getBanks().objs) {
			if (bank != null) {
			supply += ((StrategyBank) bank).getLoanSupply();
			}
		}
		return supply;
	}
	*/
	public double computeTotalLoanDemand() {
		double demand = 0;
		for (Object firm : firms.objs) {
			if (firm != null) {
			demand += ((StrategyFirm) firm).getAskedLoan();
			}
		}
		return demand;
	}
	
	public double computeTotalLoanSupply() {
		double supply = 0;
		for (Object bank : banks.objs) {
			if (bank != null) {
			supply += ((StrategyBank) bank).getLoanSupply();
			}
		}
		return supply;
	}

	public Bag getBanks() {
		return banks;
	}

	public void setBanks(Bag banks) {
		this.banks = banks;
	}



}

