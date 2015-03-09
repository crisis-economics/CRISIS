/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Milan Lovric
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.crisis_economics.abm.IAgent;
import eu.crisis_economics.abm.algorithms.portfolio.returns.FundamentalistStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.NoSmoothingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.HomogeneousPortfolioWeighting;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.LinearAdaptivePortfolioWeighting;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.FixedValueContract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Loan;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingLoanMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MockClearingLoanMarket;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.AbstractCollectionProvider;

public class FundTest {
    public Simulation sim;
    public Bank myBank;
    public MutualFund myFund;
    public HouseholdStub[] myHouseholds;
    public FirmStub[] myFirms;
    double startingFundDeposits;
    private ClearingHouse
       clearingHouse;
    static final int N_HOUSEHOLDS = 1000;
    static final int ITERATIONS = 500;
    static final int N_FIRMS = 10;
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public class MHouseholdStrategy {
        InvestmentAccountHolder iInvestmentAccount;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @BeforeMethod
    public void setUp() {
        System.out.println("Testing mutual funds..");
        
        sim = new EmptySimulation(1);
        clearingHouse = new ClearingHouse();
        startingFundDeposits = 0.0;
        myBank = new Bank(1000000.0);
        myFund = new MutualFund(
           myBank,
           new LinearAdaptivePortfolioWeighting(),
           new FundamentalistStockReturnExpectationFunction(),
           clearingHouse,
           new NoSmoothingAlgorithm()
           );
        myFund.debit(startingFundDeposits);
        
        int i;
        myHouseholds = new HouseholdStub[N_HOUSEHOLDS];
        for(i=0; i<N_HOUSEHOLDS; ++i) {
            myHouseholds[i] = new HouseholdStub(myFund,myBank,200);
        }

        myFirms = new FirmStub[N_FIRMS];
        for(i=0; i<N_FIRMS; ++i) {
            myFirms[i] = new FirmStub(myBank, 100);
            UniqueStockExchange.Instance.addStock(myFirms[i], 1.0);
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void runAllFundsTests() throws InsufficientFundsException {
        testInvestmentAccount(myHouseholds[0]);
        testInvestmentAccount(myHouseholds[1]);
        myFund.mInvestmentAccount.setEmissionPrice(4.0);
        testInvestmentAccount(myHouseholds[0]);
        testInvestmentAccount(myHouseholds[1]);
        myFund.mInvestmentAccount.setEmissionPrice(1.0);
     // testSharePriceCalculation();
        testHouseholdFundMarketInteraction();
        testPrivateEquityConstancyAndConservationOfCash();
        testLongTermNetWithdrawal();
        testInvestmentFollowedByCompleteWithdrawal();
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////   
    public void testHouseholdFundMarketInteraction() throws InsufficientFundsException {
        double totalInvestment;
        double[] householdDesiredInvestment = new double[N_HOUSEHOLDS];
        double totalLoanValue;
        int j;
        Random rand = new Random();
        rand.setSeed(12345);
        final MockClearingLoanMarket 
           firstVirtualLoanMarket = new MockClearingLoanMarket(
              myFund, myBank, "Bonds", "3% Coupon Bond", 0.03, clearingHouse),
           secondVirtualLoanMarket = new MockClearingLoanMarket(
              myFund, myBank, "Bonds", "5% Coupon Bond", 0.05, clearingHouse);
        clearingHouse.addMarket(firstVirtualLoanMarket);
        clearingHouse.addMarket(secondVirtualLoanMarket);
        final List<ClearingStockMarket>
           stocks = new ArrayList<ClearingStockMarket>();
        
        System.out.println("Testing Household/MutualFund/Market interaction");
        for(j=0; j<N_FIRMS; ++j) {
            myFund.portfolio.addStock(myFirms[j].getUniqueName());
            myFirms[j].setMarketValue(10.0*rand.nextDouble());
            myFirms[j].setDividendPerShare(rand.nextDouble()*myFirms[j].getMarketValue());
            final ClearingStockMarket
               stockMarket = new ClearingStockMarket(myFirms[j], clearingHouse);
            stocks.add(stockMarket);
            clearingHouse.addMarket(stockMarket);
        }
        
        myFund.setLoansToInvestIn(new AbstractCollectionProvider<ClearingLoanMarket>() {
           @Override
           public Collection<ClearingLoanMarket> get() {
              return clearingHouse.getMarketsOfType(ClearingLoanMarket.class);
           }
        });
        myFund.setStocksToInvestIn(new AbstractCollectionProvider<ClearingStockMarket>() {
           @Override
           public Collection<ClearingStockMarket> get() {
              return clearingHouse.getMarketsOfType(ClearingStockMarket.class);
           }
        });
        
        for(int i=0; i<ITERATIONS; ++i) {
            totalInvestment = 0.0;
            for(j=0; j<N_HOUSEHOLDS; ++j) {
                householdDesiredInvestment[j] = myHouseholds[j].investmentRandomAmount();
                totalInvestment += householdDesiredInvestment[j];
            }
            firstVirtualLoanMarket.expireLoans();
            secondVirtualLoanMarket.expireLoans();
            myFund.preClearingProcessing();
            firstVirtualLoanMarket.process();
            secondVirtualLoanMarket.process();
            for(final ClearingStockMarket market : stocks)
               market.process();
            myFund.postClearingProcessing();
            totalLoanValue = 0.0;
            for(Loan loan : myFund.getAssetLoans()) {
                totalLoanValue += loan.getValue();
            }
//          cashWeight = myFund.getBalance()/(myFund.marketCap()+myFund.equityCapital());
            if(totalLoanValue < totalInvestment) {
//              Assert.assertEquals(cashWeight, 
//                      Math.min(MutualFund.INITIAL_CASH_WEIGHT,
//                              (totalInvestment-totalLoanValue+myFund.equityCapital())/(totalInvestment+myFund.equityCapital())),
//                      1e-4);
                for(j=0; j<N_HOUSEHOLDS; ++j) {
                    Assert.assertEquals(myFund.getBalance(myHouseholds[j]), householdDesiredInvestment[j], 1e-8);
                    Assert.assertEquals(myHouseholds[j].getEquity(), 200.0, 1e-8);
                }
            }
            Assert.assertEquals(myBank.getEquity(), 1000000.0, 1e-6);
        }
        
        double oldShareCount = myFund.mInvestmentAccount.getNumberOfEmittedShares();
        myFund.mInvestmentAccount.recountShareDistribution();
        double shareCountDrift = myFund.mInvestmentAccount.getNumberOfEmittedShares() - oldShareCount;
        System.out.println("Share count drift = "+shareCountDrift);
        
        myFund.portfolio.stockWeights().clear();
        System.out.println("Done testing Household/MutualFund/Market interaction");
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////   
    public void testInvestmentAccount(HouseholdStub myHousehold) throws InsufficientFundsException {
        double  balance;
        double  initialFundBalance;
        
        System.out.println("Testing InvestmentAccount interface on household's fund");
        
        myFund.invest(myHousehold,100.0); // set up InvestmentAccount contract if not already there
        
        // ---- Zero investment
        initialFundBalance = myFund.getBalance();
        myFund.invest(myHousehold, 0.0);
        balance = myFund.getBalance();
        Assert.assertEquals(balance, initialFundBalance);
        Assert.assertEquals(myFund.getBalance(myHousehold), 100.0, 1.e-10);
        
        // --- positive investment
        myFund.invest(myHousehold, 100.0);
        balance = myFund.getBalance();
        Assert.assertEquals(balance, initialFundBalance + 100.0, 1.e-10);
        Assert.assertEquals(myFund.getBalance(myHousehold), 200.0, 1.e-10);

        // --- negative investment
        myFund.invest(myHousehold, -100.0);
        balance = myFund.getBalance();
        Assert.assertEquals(balance, initialFundBalance + 100.0, 1.e-10);
        Assert.assertEquals(myFund.getBalance(myHousehold), 200.0, 1.e-10);
        
        myFund.requestWithdrawal(myHousehold, 50);
        myFund.settleOrderBook(myFund.getBalance() - startingFundDeposits);
        balance = myFund.getBalance();
        Assert.assertEquals(balance, initialFundBalance + 50, 1.e-10);
        Assert.assertEquals(myFund.getBalance(myHousehold), 150.0, 1.e-10);

        // --- 2 orders from same household in a timestep
        myFund.requestWithdrawal(myHousehold, 25);
        myFund.requestWithdrawal(myHousehold, 25);
        myFund.settleOrderBook(myFund.getBalance() - startingFundDeposits);
        balance = myFund.getBalance();
        Assert.assertEquals(balance, initialFundBalance);
        Assert.assertEquals(myFund.getBalance(myHousehold),100.0, 1.e-10);

        // --- check for proper deletion of orders after settlement
        myFund.settleOrderBook(myFund.getBalance() - startingFundDeposits);
        balance = myFund.getBalance();
        Assert.assertEquals(balance, initialFundBalance);
        Assert.assertEquals(myFund.getBalance(myHousehold),100.0, 1.e-10);

        // --- negative withdrawal
        myFund.requestWithdrawal(myHousehold, -100.0);
        balance = myFund.getBalance();
        myFund.settleOrderBook(myFund.getBalance() - startingFundDeposits);
        Assert.assertEquals(balance, initialFundBalance);
        Assert.assertEquals(myFund.getBalance(myHousehold), 100.0, 1.e-10);

        myFund.requestWithdrawal(myHousehold,100);
        myFund.settleOrderBook(myFund.getBalance() - startingFundDeposits);

        System.out.println("Done testing InvestmentAccount");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////   
    public void testSharePriceCalculation() throws InsufficientFundsException {
        System.out.println("Testing calculation of share price");
        // emission price is £1 initially.
        myFund.invest(myHouseholds[0], 70); // sell 70 shares @ £1 each (initial emission price)
        myFund.invest(myHouseholds[1], 30); // to household #1, + 30 shares to household #2.

        FixedValueContract testEquity = new FixedValueContract();
        testEquity.setValue(200.0);
        myFund.addAsset(testEquity); // assets of the fund = £310.
        myFund.postClearingProcessing();
        Assert.assertEquals(myFund.getEmissionPrice(), 3.0); // (£310 - £10) / 100 shares 
        
        myFund.removeAsset(testEquity); // assets of the fund = £110.
        myFund.postClearingProcessing();
        Assert.assertEquals(myFund.getEmissionPrice(), 1.0); // (£110 - £10) / 100 shares
        
        System.out.println("Done testing share price calculation");
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    /**
      * This test is in two parts.
      * Part 1: a new fund is created with an initial deposit. This deposit is
      *         reserved as private equity. A collection of household stubs are
      *         created with ample starting deposits. For a number of business
      *         cycles, the households all withdraw or contribute random sums to
      *         the fund. The size of these sums is drawn from a Gaussian 
      *         distribution with custom mean and variance. After a fixed number
      *         of business cycles have elapsed, the total amount of cash owned
      *         by each household (be this in fund investments or bank deposits)
      *         is counted. Since the fund has no investment sources and cannot
      *         make a profit, it is expected that no household ever loses or 
      *         gains assets (cash plus fund account value). At every step, it is 
      *         further asserted that the equity of the fund is not significantly
      *         different to the private (constant) equity.
      * Part 2: the fund and all households are destroyed and respawned. Households
      *         begin with ample deposits. The test now continues as in Part 1, except
      *         that the fund deposit account is directly injected with exogenous
      *         profits during each business cycle. The size of these profits is 
      *         drawn from a Gaussian distribution with custom mean and variance.
      *         At the conclusion of Part 2, the amount of cash in the system
      *         is tallied. It is then asserted that the sum of all household deposits
      *         and fund account values is equal to the initial cash in the system 
      *         plus total exogenous profits.
      * Part 3: as Part 2, except the fund now makes exogenous losses as well as
      *         profits. In this test, the fund is deliberately taken to the edge of
      *         its private equity due to market losses.
      */
    public void testPrivateEquityConstancyAndConservationOfCash() {
       
       /*
        * Test parameters
        */
       final int
          numberOfBusinessCycles = 1000;
       final double
          meanFundProfit = 1.e6,
          fundProfitVariance = 2.e5,
          meanHouseholdInvestment = 0.,
          householdInvestmentVariances = 1000.,
          householdInitialDeposits = 1.e7,
          fundInitialDeposits = 0.;
       Random dice = new Random(12345);
       
       myFund = new MutualFund(
          myBank,
          new HomogeneousPortfolioWeighting(),
          new FundamentalistStockReturnExpectationFunction(),
          clearingHouse,
          new NoSmoothingAlgorithm()
          );  // Fresh fund
       
       myFund.debit(fundInitialDeposits);
       final Contract exogenousReturns = myFund.mDepositor.depositAccount;
       Assert.assertEquals(myFund.getEquity(), fundInitialDeposits);
       for(int i = 0; i< myHouseholds.length; ++i) {
          myHouseholds[i] =                             // Ample household liquidity
             new HouseholdStub(myFund, myBank, householdInitialDeposits);
          Assert.assertEquals(myHouseholds[i].getTotalAssets(), householdInitialDeposits, 1.e-5);
       }
       
       /*
        * Part 1
        */
       
       for(int i = 0; i< numberOfBusinessCycles; ++i) {
          // Random household investments and withdrawals
          for(int j = 0; j< N_HOUSEHOLDS; ++j) {
             final double
                investment =
                   dice.nextGaussian() * householdInvestmentVariances + meanHouseholdInvestment;
             try {
                if(investment > 0.)
                   myFund.invest(myHouseholds[j], investment);
                else if(investment < 0.) {
                   final double maximumWithdrawal = myFund.getBalance(myHouseholds[j]);
                   if(maximumWithdrawal == 0.) continue;
                   myFund.requestWithdrawal(myHouseholds[j],
                      Math.min(-investment, maximumWithdrawal));
                }
             }
             catch(final InsufficientFundsException noFunds) {
                Assert.fail();
             }
          }
          myFund.preClearingProcessing();
          // No profits from fund investments.
          try {
             myFund.postClearingProcessing();
          } catch (final InsufficientFundsException unexpectedException) {
             Assert.fail();
          }
          System.out.printf("MutualFund assets:      %16.10g\n", myFund.getTotalAssets());
          System.out.printf("MutualFund liabilities: %16.10g\n", myFund.getTotalLiabilities());
          System.out.printf("MutualFund equity:      %16.10g\n", myFund.getEquity());
          
          // Assertions
          final double postClearingEquity = myFund.getEquity();
          Assert.assertEquals(postClearingEquity, fundInitialDeposits, 1.e-5);
          
          continue;
       }
       
       // Assert no cash has been created
       System.out.printf(
             "household  deposits [open]  fund investments [open]" 
           + "  deposits [close] fund investments [close] sum [close]\n");
       for(int i = 0; i< N_HOUSEHOLDS; ++i) {           // Assert no money creation
          final double
             closingHouseholdDeposits = myHouseholds[i].bankAccount.getBalance(),
             closingFundAccountValue = myFund.getBalance(myHouseholds[i]);
          System.out.printf("%5d %16.10g %16.10g         %16.10g %16.10g         %16.10g\n", 
             i, householdInitialDeposits, 0.,
             closingHouseholdDeposits, closingFundAccountValue,
             closingHouseholdDeposits + closingFundAccountValue
             );
          Assert.assertEquals(
             closingHouseholdDeposits + closingFundAccountValue, householdInitialDeposits, 1.e-6);
       }
       
       /*
        * Part 2
        */
       
       // Rerun with exogenous fund profits
       double
          totalFundProfits = 0.;
       for(int i = 0; i< numberOfBusinessCycles; ++i) {
          // Random household investments and withdrawals
          for(int j = 0; j< N_HOUSEHOLDS; ++j) {
             final double
                investment =
                   dice.nextGaussian() * householdInvestmentVariances + meanHouseholdInvestment;
             try {
                if(investment > 0.)
                   myFund.invest(myHouseholds[j], investment);
                else if(investment < 0.) {
                   final double maximumWithdrawal = myFund.getBalance(myHouseholds[j]);
                   if(maximumWithdrawal == 0.) continue;
                   myFund.requestWithdrawal(myHouseholds[j], 
                      Math.min(-investment, maximumWithdrawal));
                }
             }
             catch(final InsufficientFundsException noFunds) {
                Assert.fail();
             }
          }
          myFund.preClearingProcessing();
          final double
             fundProfits = dice.nextGaussian() * fundProfitVariance + meanFundProfit;
          exogenousReturns.setValue(exogenousReturns.getValue() + fundProfits);
          totalFundProfits += fundProfits;
          try {
             myFund.postClearingProcessing();
          } catch (final InsufficientFundsException unexpectedException) {
             Assert.fail();
          }
          
          System.out.printf("MutualFund profits:     %16.10g\n", fundProfits);
          System.out.printf("MutualFund assets:      %16.10g\n", myFund.getTotalAssets());
          System.out.printf("MutualFund liabilities: %16.10g\n", myFund.getTotalLiabilities());
          System.out.printf("MutualFund equity:      %16.10g\n", myFund.getEquity());
          
          System.out.println(
             "Number of shares: " + myFund.mInvestmentAccount.getNumberOfEmittedShares());
          
          // Assertions
          final double postClearingEquity = myFund.getEquity();
          Assert.assertEquals(postClearingEquity, fundInitialDeposits, 1.e-1);
          
          continue;
       }
       
       // Tally the total amount of cash in the system.
       double totalHouseholdProfits = 0.;
       System.out.printf(
             "household  deposits [open]  fund investments [open]" 
           + "  deposits [close] fund investments [close] sum [close]\n");
       for(int i = 0; i< N_HOUSEHOLDS; ++i) {           // Assert no money creation
          final double
             closingHouseholdDeposits = myHouseholds[i].bankAccount.getBalance(),
             closingFundAccountValue = myFund.getBalance(myHouseholds[i]);
          System.out.printf("%5d %16.10g %16.10g         %16.10g %16.10g         %16.10g\n", 
             i, householdInitialDeposits, 0.,
             closingHouseholdDeposits, closingFundAccountValue,
             closingHouseholdDeposits + closingFundAccountValue
             );
          totalHouseholdProfits +=
             (closingHouseholdDeposits + closingFundAccountValue) - householdInitialDeposits;
       }
       
       System.out.printf(
          "Aggregate household profits: %16.10g. MutualFund profits: %16.10g\n",
          totalHouseholdProfits, totalFundProfits);
       Assert.assertEquals(
          totalHouseholdProfits/Math.pow(10., (int)Math.log10(totalHouseholdProfits)),
          totalFundProfits/Math.pow(10., (int)Math.log10(totalFundProfits)),
          1.e-12
          );
       
       /*
        * Part 3
        */
       
       // Rerun with predetermined fund losses.
       UnivariateFunction forcedFundAssetPosition = new UnivariateFunction() {
          @Override
          public double value(double time) {
             return fundInitialDeposits + 1.e-5 +
                householdInvestmentVariances * (1. + Math.sin(2. * Math.PI * time / 50.));
          }
       };
       for(int i = 0; i< numberOfBusinessCycles; ++i) {
          // Random household investments and withdrawals
          for(int j = 0; j< N_HOUSEHOLDS; ++j) {
             final double
                investment =
                   dice.nextGaussian() * householdInvestmentVariances + meanHouseholdInvestment;
             try {
                if(investment > 0.) {
                   myFund.invest(myHouseholds[j], investment);
                }
                else if(investment < 0.) {
                   final double maximumWithdrawal = myFund.getBalance(myHouseholds[j]);
                   if(maximumWithdrawal == 0.) continue;
                   double withdrawal = Math.min(-investment, maximumWithdrawal);
                   myFund.requestWithdrawal(myHouseholds[j], withdrawal);
                }
             }
             catch(final InsufficientFundsException noFunds) {
                Assert.fail();
             }
          }
         
          
          myFund.preClearingProcessing();
          final double
             profitsNow = forcedFundAssetPosition.value(i) - exogenousReturns.getValue();
          totalFundProfits += profitsNow;
          exogenousReturns.setValue(forcedFundAssetPosition.value(i));
          try {
             myFund.postClearingProcessing();
          } catch (final InsufficientFundsException unexpectedException) {
             Assert.fail();
          }
          
          System.out.printf("MutualFund profits:     %16.10g\n", profitsNow);
          System.out.printf("MutualFund assets:      %16.10g\n", myFund.getTotalAssets());
          System.out.printf("MutualFund liabilities: %16.10g\n", myFund.getTotalLiabilities());
          System.out.printf("MutualFund equity:      %16.10g\n", myFund.getEquity());
          
          System.out.println(
             "Number of shares: " + myFund.mInvestmentAccount.getNumberOfEmittedShares());
          
          // Assertions
          final double postClearingEquity = myFund.getEquity();
          Assert.assertEquals(postClearingEquity, fundInitialDeposits, 1.e-1);
          
          continue;
       }
       
       // Tally the total amount of cash in the system.
       totalHouseholdProfits = 0.;
       System.out.printf(
             "household  deposits [open]  fund investments [open]" 
           + "  deposits [close] fund investments [close] sum [close]\n");
       for(int i = 0; i< N_HOUSEHOLDS; ++i) {           // Assert no money creation
          final double
             closingHouseholdDeposits = myHouseholds[i].bankAccount.getBalance(),
             closingFundAccountValue = myFund.getBalance(myHouseholds[i]);
          System.out.printf("%5d %16.10g %16.10g         %16.10g %16.10g         %16.10g\n", 
             i, householdInitialDeposits, 0.,
             closingHouseholdDeposits, closingFundAccountValue,
             closingHouseholdDeposits + closingFundAccountValue
             );
          totalHouseholdProfits +=
             (closingHouseholdDeposits + closingFundAccountValue) - householdInitialDeposits;
       }
       
       System.out.printf(
          "Aggregate household profits: %16.10g. MutualFund profits: %16.10g\n",
          totalHouseholdProfits, totalFundProfits);
       Assert.assertEquals(
          totalHouseholdProfits/Math.pow(10., (int)Math.log10(Math.abs(totalHouseholdProfits))),
          totalFundProfits/Math.pow(10., (int)Math.log10(Math.abs(totalFundProfits))),
          1.e-12
          );
       
       return;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    public void testLongTermNetWithdrawal() {
       /*
        * Test parameters
        */
       final int
          numberOfBusinessCycles = 1000;
       final double
          periodicFundProfit = 1.e7,
          fundInitialDeposits = 0.,
          householdInitialDeposits = 1.e8 / myHouseholds.length,
          periodicHouseholdWithdrawal = .7 * periodicFundProfit / myHouseholds.length;
       
       myFund = new MutualFund(
          myBank,
          new HomogeneousPortfolioWeighting(),
          new FundamentalistStockReturnExpectationFunction(),
          clearingHouse,
          new NoSmoothingAlgorithm()
          );  // Fresh fund
       
       myFund.debit(fundInitialDeposits);
       final Contract exogenousReturns = myFund.mDepositor.depositAccount;
       for(int i = 0; i< myHouseholds.length; ++i) {
          myHouseholds[i] = new HouseholdStub(myFund, myBank, householdInitialDeposits);
          try {
             myFund.invest(myHouseholds[i], householdInitialDeposits * .9);
          } catch (final InsufficientFundsException unexpectedException) {
             Assert.fail();
          }
       }
       
       double
          totalFundProfits = 0.;
       for(int i = 0; i< numberOfBusinessCycles; ++i) {
          for(int j = 0; j< myHouseholds.length; ++j) {
             try {
                myFund.requestWithdrawal(myHouseholds[j], periodicHouseholdWithdrawal);
             } catch (final InsufficientFundsException unexpectedException) {
                Assert.fail();
             }
          }
          myFund.preClearingProcessing();
          exogenousReturns.setValue(exogenousReturns.getValue() + periodicFundProfit);
          totalFundProfits += periodicFundProfit;
          try {
             myFund.postClearingProcessing();
          } catch (final InsufficientFundsException unexpectedException) {
             Assert.fail();
          }
          
          System.out.printf("MutualFund profits:     %16.10g\n", periodicFundProfit);
          System.out.printf("MutualFund assets:      %16.10g\n", myFund.getTotalAssets());
          System.out.printf("MutualFund liabilities: %16.10g\n", myFund.getTotalLiabilities());
          System.out.printf("MutualFund equity:      %16.10g\n", myFund.getEquity());
          
          System.out.println(
             "Number of shares: " + myFund.mInvestmentAccount.getNumberOfEmittedShares());
          
          // Assertions
          final double postClearingEquity = myFund.getEquity();
          Assert.assertEquals(postClearingEquity, fundInitialDeposits, 1.e-1);
          
          continue;
       }
       
       // Tally the total amount of cash in the system.
       double totalHouseholdProfits = 0.;
       System.out.printf(
             "household  deposits [open]  fund investments [open]" 
           + "  deposits [close] fund investments [close] sum [close]\n");
       for(int i = 0; i< N_HOUSEHOLDS; ++i) {           // Assert no money creation
          final double
             closingHouseholdDeposits = myHouseholds[i].bankAccount.getBalance(),
             closingFundAccountValue = myFund.getBalance(myHouseholds[i]);
          System.out.printf("%5d %16.10g %16.10g         %16.10g %16.10g         %16.10g\n", 
             i, householdInitialDeposits, 0.,
             closingHouseholdDeposits, closingFundAccountValue,
             closingHouseholdDeposits + closingFundAccountValue
             );
          totalHouseholdProfits +=
             (closingHouseholdDeposits + closingFundAccountValue) - householdInitialDeposits;
       }
       
       System.out.printf(
          "Aggregate household profits: %16.10g. MutualFund profits: %16.10g\n",
          totalHouseholdProfits, totalFundProfits);
       Assert.assertEquals(
          totalHouseholdProfits/Math.pow(10., (int)Math.log10(totalHouseholdProfits)),
          totalFundProfits/Math.pow(10., (int)Math.log10(totalFundProfits)),
          1.e-12
          );
       Assert.assertTrue(myFund.mInvestmentAccount.numberOfEmittedShares > 1.);
       
       return;
    }
    
    /**
      * This unit test attempts to induce numerical failures in 
      * fund order book settlement logic and share price formation.
      */
    public void testInvestmentFollowedByCompleteWithdrawal() {
       /*
        * Test parameters
        */
       final int
          numberOfBusinessCycles = 2000;
       final double
          periodicFundProfit = 1.e7,
          fundInitialDeposits = 0.,
          householdInitialDeposits = 1.e9;
       
       final UnivariateFunction investmentSize = new UnivariateFunction() {
          @Override
          public double value(double time) {
             return householdInitialDeposits *
                Math.pow(10., -14. * (1. + Math.sin(2. * Math.PI * time / 100.)));
          }
       };
       
       myFund = new MutualFund(
          myBank,
          new HomogeneousPortfolioWeighting(),
          new FundamentalistStockReturnExpectationFunction(),
          clearingHouse,
          new NoSmoothingAlgorithm()
          );  // Fresh fund
       
       myFund.debit(fundInitialDeposits);
       final Contract exogenousReturns = myFund.mDepositor.depositAccount;
       for(int i = 0; i< myHouseholds.length; ++i) {
          myHouseholds[i] = new HouseholdStub(myFund, myBank, householdInitialDeposits);
          try {
             myFund.invest(myHouseholds[i], householdInitialDeposits * .9);
          } catch (final InsufficientFundsException unexpectedException) {
             Assert.fail();
          }
       }
       
       final double
          startingSystemCash = myBank.getCashReserveValue();
       
       for(int i = 0; i< numberOfBusinessCycles; ++i) {
          for(int j = 0; j< myHouseholds.length; ++j) {
             try {
                if(i % 3 == 0) {
                   // All investors withdraw everything they own.
                   final double accountValue =
                      myFund.getBalance(myHouseholds[j]);
                   myFund.requestWithdrawal(myHouseholds[j], accountValue * (1. - 1.e-15));
                }
                else if(i % 3 == 1) {
                   // No action by any participants
                }
                else {
                   // All investors invest.
                   myFund.invest(myHouseholds[j], investmentSize.value(i));
                }
             } catch (final InsufficientFundsException unexpectedException) {
                //Assert.fail();
             }
          }
          System.out.println("Household investment size: " +investmentSize.value(i));
          myFund.preClearingProcessing();
          exogenousReturns.setValue(exogenousReturns.getValue() + periodicFundProfit);
          try {
             myFund.postClearingProcessing();
          } catch (final InsufficientFundsException unexpectedException) {
             Assert.fail();
          }
          
          System.out.printf("MutualFund profits:     %16.10g\n", periodicFundProfit);
          System.out.printf("MutualFund assets:      %16.10g\n", myFund.getTotalAssets());
          System.out.printf("MutualFund liabilities: %16.10g\n", myFund.getTotalLiabilities());
          System.out.printf("MutualFund equity:      %16.10g\n", myFund.getEquity());
          
          Assert.assertTrue(myFund.getTotalAssets() >= startingFundDeposits);
          
          System.out.println(
             "Number of shares: " + myFund.mInvestmentAccount.getNumberOfEmittedShares());
          
          continue;
       }
       
       System.out.printf("Closing bank cash: %16.10g\n", myBank.getCashReserveValue());
       Assert.assertEquals(
          myBank.getCashReserveValue(), startingSystemCash, 1.e-12 * startingSystemCash);
       
       return;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    @AfterMethod
    public void tearDown() {
       System.out.println("Mutual fund tests ok.");
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    /*
     * Manual entry point
     */
    static public void main(String args[]) {
        try {
            FundTest thisTest = new FundTest();
            thisTest.setUp();
            thisTest.runAllFundsTests();
            thisTest.tearDown();
        } catch(final InsufficientFundsException unexpectedException) {
            System.out.println("Unexpected exception raised. Funds tests failed.");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Helper functions
    //////////////////////////////////////////////////////////////////////////////////////
    
    static public void printBalanceSheet(String title, IAgent agent) {
        Contract c;
        String name;
        double val;     
        System.out.println(title);
        System.out.println("----------------------------------------------------------");
        for(int n=0; n<agent.getAssets().size() || n<agent.getLiabilities().size(); ++n) {
            if(n<agent.getAssets().size()) {
                c = agent.getAssets().get(n);
                name = c.getClass().getCanonicalName();
                name = name.substring(name.length()-20,name.length());
                val = c.getValue();
            } else {
                name = "                ";
                val = 0.0;
            }
            System.out.print(name+" "+val+"\t| ");
            if(n<agent.getLiabilities().size()) {
                c = agent.getLiabilities().get(n);
                name = c.getClass().getCanonicalName();
                name = name.substring(name.length()-20,name.length());
                val = c.getValue();
            } else {
                name = "                ";
                val = 0.0;
            }           
            System.out.println(name+" "+val);
        }
        System.out.println("");
    }
    
}
