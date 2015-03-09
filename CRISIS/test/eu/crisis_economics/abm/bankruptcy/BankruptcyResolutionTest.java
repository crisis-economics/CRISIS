/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Milan Lovric
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
package eu.crisis_economics.abm.bankruptcy;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;

import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.agent.ComputeCapitalAdequacyRatioOperation;
import eu.crisis_economics.abm.agent.ComputeRiskWeightedAssetsOperation;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.algorithms.portfolio.returns.FundamentalistStockReturnExpectationFunction;
import eu.crisis_economics.abm.algorithms.portfolio.smoothing.NoSmoothingAlgorithm;
import eu.crisis_economics.abm.algorithms.portfolio.weighting.LinearAdaptivePortfolioWeighting;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.StockTradingBank;
import eu.crisis_economics.abm.bank.bankruptcy.BailinBankruptcyResolution;
import eu.crisis_economics.abm.bank.bankruptcy.BailoutBankruptcyResolution;
import eu.crisis_economics.abm.bank.bankruptcy.BankBankruptcyHandlerUtils;
import eu.crisis_economics.abm.bank.bankruptcy.ComputeCARBankruptcyResolutionAmountOperation;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.LenderInsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.LoanFactory;
import eu.crisis_economics.abm.contracts.settlements.SettlementParty;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.contracts.stocks.StockOwnershipTracker;
import eu.crisis_economics.abm.contracts.stocks.TargetValueStockMarketResponseFunction;
import eu.crisis_economics.abm.contracts.stocks.UniqueStockExchange;
import eu.crisis_economics.abm.firm.FirmStub;
import eu.crisis_economics.abm.firm.plugins.CreditDemandFunction;
import eu.crisis_economics.abm.fund.MutualFund;
import eu.crisis_economics.abm.government.Government;
import eu.crisis_economics.abm.intermediary.Intermediary;
import eu.crisis_economics.abm.intermediary.SingleBeneficiaryIntermediary;
import eu.crisis_economics.abm.intermediary.SingleBeneficiaryIntermediaryFactory;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.test.TestConstants;

/**
 * @author  Milan Lovric
 */
public class BankruptcyResolutionTest {

	private Simulation state;
	private double timeToSample;
	private StockTradingBank myBank;
	private MutualFund myFund;
	private FirmStub
	   myFirm1,
	   myFirm2;
//	private HouseholdStub
//	   myHousehold1,
//	   myHousehold2;
	private ClearingHouse clearingHouse;

   @BeforeMethod
   public void setUp() {
      System.out.println("Test: " + this.getClass().getSimpleName());
      
      state = new EmptySimulation(1L);
      timeToSample = NamedEventOrderings.AFTER_ALL.getUnitIntervalTime();
      state.start();
      clearingHouse = new ClearingHouse();
      
      // we need StockTradingBank so that the bank can keep stocks as assets
      myBank = new StockTradingBank(-200);// (-200);//(200000);
      
      myFund = new MutualFund(myBank,
         new LinearAdaptivePortfolioWeighting(),
         new FundamentalistStockReturnExpectationFunction(),
         clearingHouse,
         new NoSmoothingAlgorithm());
      myFund.debit(100000.);
      myFirm1 = new FirmStub(myBank,
         10000,
         myFund,
         0,
         0,
         new CreditDemandFunction.RiskNeutralCreditDemandFunction(),
         new InstanceIDAgentNameFactory()
         );
      myFirm2 = new FirmStub(myBank,
         20000,
         myFund,
         0,
         0,
         new CreditDemandFunction.RiskNeutralCreditDemandFunction(),
         new InstanceIDAgentNameFactory()
         );
//      myHousehold1 = new HouseholdStub(myFund,
//         myBank,
//         5000);
//      myHousehold2 = new HouseholdStub(myFund,
//         myBank,
//         1000);

		double count = UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(myBank);
		System.out.println("Total shares emitted by the bank: " + count);

		// bank's initial balance sheet
		System.out.println("\nBANK'S INITIAL STATE");
		printBankStatus(myBank);

		// bank releases shares to the fund
		System.out.println("Bank's cash reserve: " + myBank.getCashReserveValue());
		System.out.println("MutualFund's balance: " + myFund.getBalance());

		// bank releases shares to the fund
		try {
			myBank.releaseShares(100, 0.5, myFund);
		} catch (InsufficientFundsException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// bank releases some more shares
		try {
			UniqueStockExchange.Instance.generateSharesFor(myBank.getUniqueName(), myFund, 100, 0.4);
		} catch (InsufficientFundsException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		count = UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(myBank);
		System.out.println("Total shares emitted by the bank: " + count);

		System.out.println("Bank's cash reserve: " + myBank.getCashReserveValue());
		System.out.println("MutualFund's balance: " + myFund.getBalance());

		// Create a bond (the fund giving a loan to the bank)
		try {
			LoanFactory.createCouponBond(myBank, myFund, 400.0, 0.02, 2);
		} catch (LenderInsufficientFundsException e1) {
			Assert.fail();
		}

		// Create a loan (a bank giving a commercial loan to a firm)
		try {
			LoanFactory.createFixedRateMortgage(myFirm1, myBank, 500.0, 0.05, 10);
		} catch (LenderInsufficientFundsException e1) {
			Assert.fail();
		}

		// Create a loan (a bank giving a mortgage to a household)
		// TODO Currently households are not borrowers!
		//		try {
		//			LoanFactory.createFixedRateMortgage(myHousehold1, myBank, 1000.0, 0.03, 100);
		//		} catch (LenderInsufficientFundsException e1) {
		//			Assert.fail();
		//		}

		// firm generates shares for the bank
		try {
			UniqueStockExchange.Instance.setStockPrice(myFirm2.getUniqueName(), 30.0);
			UniqueStockExchange.Instance.generateSharesFor(myFirm2.getUniqueName(), myBank, 10.0, 30.0);
		} catch (InsufficientFundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// bank's balance sheet after loans, bonds and stocks were created
		System.out.println("\nAFTER LOANS, BONDS AND STOCKS");
		printBankStatus(myBank);

		// reduce the value of assets (firm's stocks owned by the bank)
		UniqueStockExchange.Instance.setStockPrice(myFirm2.getUniqueName(), 0.01);

		// bank's balance sheet after reducing the value of assets
		System.out.println("\nAFTER ASSET VALUE DROPPED");
		printBankStatus(myBank);
	}

	/**
	 * Advance the simulation until the next @{link timeToSample}.
	 */
	private void advanceUntilSampleTime() {
		double
		startTime = state.schedule.getTime(),
		startFloorTime = Math.floor(startTime),
		endTime = startFloorTime + timeToSample;
		if(endTime <= startTime)
			endTime += 1.;
		while(state.schedule.getTime() <= endTime)
			state.schedule.step(state);
	}

	/**
	 * Advance the simulation to time zero.
	 */
	private void advanceToTimeZero() {
		while(state.schedule.getTime() < 0.)
			state.schedule.step(state);
	}
   
   @AfterMethod
   public void tearDown() {
      System.out.println(getClass().getSimpleName() + " tests pass.");
      state.finish();
      state = null;
   }
   
   @Test
   public void testBailin() {
      advanceToTimeZero();
      
      final AgentOperation<Double>
         riskWeightedAssetsCalculation = new ComputeRiskWeightedAssetsOperation(),
         CARCalculation = new ComputeCapitalAdequacyRatioOperation(riskWeightedAssetsCalculation),
         equityTargetCalculation = new ComputeCARBankruptcyResolutionAmountOperation(
            0.08,
            riskWeightedAssetsCalculation
            );
      
      myBank.setResolutionMethod(
         BankBankruptcyHandlerUtils.orderedHandlersAsPolicy(
            new BailinBankruptcyResolution(
               equityTargetCalculation,
               new SingleBeneficiaryIntermediaryFactory() {
                  @Override
                  public SingleBeneficiaryIntermediary createFor(SettlementParty beneficiary) {
                     return new SingleBeneficiaryIntermediary(
                        beneficiary, clearingHouse);
                  }
               }
            )));
      
		// liabilities before bail-in
		double totalLiabilities = myBank.getTotalLiabilities();

		boolean bailin = false;
		double amount = myBank.accept(equityTargetCalculation);
		System.out.println("The amount of bail-in needed to reach 0.08 = " + amount);

		// check if bail-in is needed (determineBailnAmount returns positive value)
		if (amount > 0)	{	
			myBank.handleBankruptcy();
			bailin = true;
		} else 
			System.out.println("Bail-in will not be executed because bank's CAR is already better than the 8% target.");

		if (bailin) { //if there was a bail-in

			// bank's balance sheet after bail-in
			System.out.println("\nAFTER BAIL-IN");
			printBankStatus(myBank);

			// if RAW are not zero, i.e. CAR is finite
			if (myBank.accept(riskWeightedAssetsCalculation) != 0)
				// check that CAR after bail-in is at 8% target
				AssertJUnit.assertEquals(0.08, myBank.accept(CARCalculation), TestConstants.DELTA);
			else
				// check that CAR is positive infinity (i.e. equity is positive).
				AssertJUnit.assertEquals(Double.POSITIVE_INFINITY, myBank.accept(CARCalculation));

			// check that the total value of issued shares is the same as the new equity
			double count = UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(myBank);
			double price = UniqueStockExchange.Instance.getStockPrice(myBank); //not myBank.getEmissionPrice()!
			System.out.println("Shares emitted by the bank: " + count + " at price: " + price);
			AssertJUnit.assertEquals(myBank.getEquity(), count*price, TestConstants.DELTA);

			// check that the amount of liabilities written off (the change in liabilities)
			// is the same as the bail-in amount
			double changeInLiabilities = totalLiabilities - myBank.getTotalLiabilities();
			AssertJUnit.assertEquals(amount, changeInLiabilities, TestConstants.DELTA);

			/*
			 * Create a clearing house and add the intermediary to the 
			 * registered stockholders.
			 */

			// add stock instrument to the clearing house
			clearingHouse.addMarket(new ClearingStockMarket(myBank, clearingHouse));

			// get stock ownership tracker
			StockOwnershipTracker sot =
			    UniqueStockExchange.Instance.getOwnershipTracker(myBank.getUniqueName());

//			// add intermediaries to the clearing house (leave out funds)
//			for (StockHolder sh: sot)
//				if (sh instanceof Intermediary) 
//					clearingHouse.addStockMarketParticipant(sh);

			// create another trading party
			class CustomBank extends StockTradingBank implements ClearingMarketParticipant {
				public CustomBank(double initialCash) {
					super(initialCash);
				}

				@Override
				public MarketResponseFunction getMarketResponseFunction(
					final ClearingMarketInformation marketInformation
					) {
					return new TargetValueStockMarketResponseFunction(   // Stock Market
					    super.getNumberOfSharesOwnedIn(myBank.getUniqueName()),
						1.0
						);
				}
			}

			// add trading bank to the clearing house
			CustomBank competitor = new CustomBank(1000.0);
			clearingHouse.addStockMarketParticipant(competitor);

			double intermediaryOwnedShares = 0.0; // intermediaries
			double nonIntermediaryOwnedShares = 0.0; // funds AND competitor bank
			double competitorBankOwnderShares = 0.0; // only competitor bank
			// check stock ownerships
			for (StockHolder sh: sot)
				if (sh instanceof Intermediary) {
					intermediaryOwnedShares += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
					System.out.println("Found intermediary with the following number of shares: " + ((Intermediary)sh).getNumberOfSharesOwnedIn(myBank.getUniqueName()));
				} else {
					nonIntermediaryOwnedShares += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
					System.out.println("Found non-intermediary with the following number of shares: " + sh.getNumberOfSharesOwnedIn(myBank.getUniqueName()));

					//if non-intermediary is the competitor bank, also store its value separately
					if (sh instanceof CustomBank) 
						competitorBankOwnderShares += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
				}

			System.out.println("Total intermediary-owned number of shares: " + intermediaryOwnedShares);
			System.out.println("Total non-intermediary-owned number of shares: " + nonIntermediaryOwnedShares);
			System.out.println("Total competitor-bank-owned number of shares: " + competitorBankOwnderShares);

			// move simulation to the 100th time step
			for (int i=0; i<100; i++) advanceUntilSampleTime();

			// check stock ownerships in the 100th step
			double intermediaryOwnedSharesNext = 0.0; // intermediaries
			double nonIntermediaryOwnedSharesNext = 0.0; // funds AND competitor bank
			double competitorBankOwnderSharesNext = 0.0; // only competitor bank

			for (StockHolder sh: sot)
				if (sh instanceof Intermediary) {
					intermediaryOwnedSharesNext += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
					System.out.println("Found intermediary with the following number of shares: " + ((Intermediary)sh).getNumberOfSharesOwnedIn(myBank.getUniqueName()));
				} else {
					nonIntermediaryOwnedSharesNext += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
					System.out.println("Found non-intermediary with the following number of shares: " + sh.getNumberOfSharesOwnedIn(myBank.getUniqueName()));
					//if non-intermediary is the competitor bank, also store its value separately
					if (sh instanceof CustomBank) 
						competitorBankOwnderSharesNext += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());

				}

			System.out.println("Total intermediary-owned number of shares: " + intermediaryOwnedSharesNext);
			System.out.println("Total non-intermediary-owned number of shares: " + nonIntermediaryOwnedSharesNext);
			System.out.println("Total competitor-bank-owned number of shares: " + competitorBankOwnderSharesNext);

			// check that intermediaries sold all of their shares
			AssertJUnit.assertEquals(0.0, intermediaryOwnedSharesNext, TestConstants.DELTA);

			// check that the competitor bank has some shares
			AssertJUnit.assertTrue(competitor.hasShareIn(myBank.getUniqueName()));

			// check that the other bank bought all of the shares from the intermediaries
			AssertJUnit.assertEquals(intermediaryOwnedShares, competitorBankOwnderSharesNext, TestConstants.DELTA);

			// check that non-intermediaries (other than the competitor bank) have the same number of shares (because we didn't add them to the clearing house)
			AssertJUnit.assertEquals(nonIntermediaryOwnedShares-competitorBankOwnderShares, nonIntermediaryOwnedSharesNext-competitorBankOwnderSharesNext, TestConstants.DELTA);
		}
	}
   
   @Test
   public void testBailout() {
      advanceToTimeZero();
      
      final AgentOperation<Double>
         riskWeightedAssetsCalculation = new ComputeRiskWeightedAssetsOperation(),
         CARCalculation = new ComputeCapitalAdequacyRatioOperation(riskWeightedAssetsCalculation),
         equityTargetCalculation = new ComputeCARBankruptcyResolutionAmountOperation(
            0.08,
            riskWeightedAssetsCalculation
            );
      
      // add stock instrument to the clearing house
      clearingHouse.addMarket(new ClearingStockMarket(myBank, clearingHouse));
      
      Government
         government = new Government(10000, clearingHouse);
      clearingHouse.addStockMarketParticipant(government);
      
      myBank.setResolutionMethod(
         BankBankruptcyHandlerUtils.orderedHandlersAsPolicy(
            new BailoutBankruptcyResolution(equityTargetCalculation, government)));
      
      boolean
         bailout = false;
      double
         amount = myBank.accept(equityTargetCalculation);
      System.out.println("The amount of bail-out needed to reach 0.08 = " + amount);
      
      // check if bail-out is needed (determineBankruptcyResolutionAmount returns positive value)
      if (amount > 0) {
         myBank.handleBankruptcy();
         bailout = true;
      } else
         System.out.println(
            "Bail-out will not be executed because bank's CAR is already better"
          + " than the 8% target.");

		if (bailout) { // if there was a bail-out
			// bank's balance sheet after bail-out
			System.out.println("\nAFTER BAIL-OUT");
			printBankStatus(myBank);

			if (myBank.accept(riskWeightedAssetsCalculation) != 0) // if RAW are not zero, i.e. CAR is finite
				// check that CAR after bail-out is at 8% target
				AssertJUnit.assertEquals(0.08, myBank.accept(CARCalculation), TestConstants.DELTA);
			else
				// check that CAR is positive infinity (i.e. equity is positive).
				AssertJUnit.assertEquals(Double.POSITIVE_INFINITY,
				   myBank.accept(CARCalculation));

			// check that the total value of issued shares is the same as the new equity
			double count = UniqueStockExchange.Instance.getNumberOfEmittedSharesIn(myBank);
			double price = UniqueStockExchange.Instance.getStockPrice(myBank); //not myBank.getEmissionPrice()!
			System.out.println("Shares emitted by the bank: " + count + " at price: " + price);
			AssertJUnit.assertEquals(myBank.getEquity(), count*price, TestConstants.DELTA);

			// check that the government got all the shares
			AssertJUnit.assertTrue(government.hasShareIn(myBank.getUniqueName()));
			AssertJUnit.assertEquals(count, government.getNumberOfSharesOwnedIn(myBank.getUniqueName()), TestConstants.DELTA);

			/*
			 * Create a clearing house and add the government to the 
			 * registered stockholders.
			 */
			// get stock ownership tracker
			StockOwnershipTracker sot = UniqueStockExchange.Instance.getOwnershipTracker(myBank.getUniqueName());

			// add government to the clearing house
			clearingHouse.addStockMarketParticipant(government);
			clearingHouse.addBorrower(government);

			// create another trading party
			class CustomBank extends StockTradingBank implements ClearingMarketParticipant {
				public CustomBank(double initialCash) {
					super(initialCash);
				}

				@Override
				public MarketResponseFunction getMarketResponseFunction(
					final ClearingMarketInformation marketInformation
					) {
						return new TargetValueStockMarketResponseFunction(
						    super.getNumberOfSharesOwnedIn(myBank.getUniqueName()), 
						    1.0
						    );
				}
			}

			// add trading bank to the clearing house
			CustomBank competitor = new CustomBank(1.e7);
			clearingHouse.addStockMarketParticipant(competitor);

			double governmentOwnedShares = 0.0; // government
			double competitorBankOwnderShares = 0.0; // only competitor bank
			// check stock ownerships
			for (StockHolder sh: sot)
				if (sh instanceof Government) {
					governmentOwnedShares += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
					System.out.println("Found government with the following number of shares: " + sh.getNumberOfSharesOwnedIn(myBank.getUniqueName()));
				} else 	if (sh instanceof CustomBank)  { 
					System.out.println("Found non-intermediary with the following number of shares: " + sh.getNumberOfSharesOwnedIn(myBank.getUniqueName()));
					competitorBankOwnderShares += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
				} else throw new RuntimeException("Found owner of shares that is neither the government nor the competitor bank!");

			System.out.println("Total government-owned number of shares: " + governmentOwnedShares);
			System.out.println("Total competitor-bank-owned number of shares: " + competitorBankOwnderShares);

			// move simulation to the 100th time step
			for (int i=0; i<100; i++) advanceUntilSampleTime();

			// check stock ownerships in the 100th step
			double governmentOwnedSharesNext = 0.0; // government
			double competitorBankOwnderSharesNext = 0.0; // only competitor bank
			// check stock ownerships
			for (StockHolder sh: sot)
				if (sh instanceof Government) {
					governmentOwnedSharesNext += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
					System.out.println("Found government with the following number of shares: " + sh.getNumberOfSharesOwnedIn(myBank.getUniqueName()));
				} else 	if (sh instanceof CustomBank)  { 
					System.out.println("Found non-intermediary with the following number of shares: " + sh.getNumberOfSharesOwnedIn(myBank.getUniqueName()));
					competitorBankOwnderSharesNext += sh.getNumberOfSharesOwnedIn(myBank.getUniqueName());
				} else throw new RuntimeException("Found owner of shares that is neither the government nor the competitor bank!");

			System.out.println("Total government-owned number of shares: " + governmentOwnedSharesNext);
			System.out.println("Total competitor-bank-owned number of shares: " + competitorBankOwnderSharesNext);

			// check that government sold all of its shares
			AssertJUnit.assertEquals(0.0, governmentOwnedSharesNext, TestConstants.DELTA);

			// check that the competitor bank has shares
			AssertJUnit.assertTrue(competitor.hasShareIn(myBank.getUniqueName()));

			// check that the competitor bank bought all of the shares from the government
			AssertJUnit.assertEquals(governmentOwnedShares, competitorBankOwnderSharesNext, TestConstants.DELTA);
		}
	}

   private void printAssets(Bank myBank) {
      double totalAssets = 0.;
      System.out.println("Bank's assets are: ");
      for (final Contract contract : myBank.getAssets()) {
         totalAssets += contract.getValue();
         System.out.println("(A) " + contract.getUniqueId() + ": "
               + contract.getValue());
      }
      System.out.println("Bank's total assets: " + totalAssets);
   }
   
   private void printLiabilities(final Bank myBank) {
      double totalLiabilities = 0.;
      System.out.println("Bank's liabilities are: ");
      for (final Contract contract : myBank.getLiabilities()) {
         totalLiabilities += contract.getValue();
         System.out.println("(L) " + contract.getUniqueId() + ": "
               + contract.getValue());
      }
      System.out.println("Bank's total liabilities: " + totalLiabilities);
   }
   
   private void printBankStatus(
      Bank myBank) {
      System.out.println("Bank's cash reserve: " + myBank.getCashReserveValue());
      printAssets(myBank);
      printLiabilities(myBank);
      System.out.println("Bank's equity: " + myBank.getEquity());
      System.out.println("Bank's RWA: "
         + myBank.accept(new ComputeRiskWeightedAssetsOperation()));
      System.out.println("Bank's CAR: "
         + myBank.accept(new ComputeCapitalAdequacyRatioOperation(
            new ComputeRiskWeightedAssetsOperation())));
   }
   
   /**
     * Manual entry point
     */
   public static void main(final String[] args) {
      // BAIL-IN
      try {
         final BankruptcyResolutionTest
            test = new BankruptcyResolutionTest();
         test.setUp();
         test.testBailin();
         test.tearDown();
      } catch (Exception e) {
         e.printStackTrace();
         Assert.fail();
      }
      
      // BAIL-OUT
      try {
         final BankruptcyResolutionTest
            test = new BankruptcyResolutionTest();
         test.setUp();
         test.testBailout();
         test.tearDown();
      } catch (Exception e) {
         e.printStackTrace();
         Assert.fail();
      }
   }
}
