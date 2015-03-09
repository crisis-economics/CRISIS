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
package eu.crisis_economics.abm.markets.nonclearing;

import static eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.any;
import static eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.only;
import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.markets.LoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.LoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.Filter;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

public class MarketTest {
	
	private class MyBank extends StrategyBank {

		public MyBank(final double initialCash) {
			super(initialCash, EmptyBankStrategy.factory());
		}
		@Override
		public void updateState(final Order order) {
			System.out.println("MyBank executed order: "+order.toString());
		}


	}
	
	DepositingHousehold household;
	MyBank bank;
	Firm firm;
	LoanMarket market;
	int maturity;
	private SimState simState;
	
	@BeforeClass
	public void setUp(){
		System.out.println("Test: " + this.getClass().getSimpleName());
		simState = new EmptySimulation(1L);
		simState.start();
		bank = new MyBank(10000);
		firm = new StockReleasingFirm(bank, 0, bank, 0, 0, new InstanceIDAgentNameFactory());
		household = new DepositingHousehold(bank);
	}
	
	@AfterClass
	public void tearDown(){
		simState.finish();
	}
	
	@Test
	public void verifyInstrument(){
		market = new CommercialLoanMarket();
		maturity = 5;
		market.addInstrument(maturity);
		try {
			market.addOrder(bank, maturity, 1000, 0.05);
			Assert.assertEquals(market.getInstrument(maturity).toString(),"LOAN5 (symbol) 1 (askLimitOrders) 0 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.05 (BestAsk) 0.05 (AskLow) 1000.0 (AskVolume) 0.0 (BuyVolume) 0.0 (BidHigh) 0.0 (BidLow) 0.0 (BestBid) 0.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) ");
		} catch (final OrderException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}

	/**
	 * test scenario: 
	 * <ol>  
	 * <li>arrival of sell order 1000</li>
	 * <li>arrival of buy order 500</li>
	 * <li>arrival of buy order 500</li>
	 * </ol>
	 * test for immediate matching on asynchronous instrument.
	 */
	@Test(enabled = false)
	public void verifyAsynchronousInstrument(){
		market = new CommercialLoanMarket();
		maturity = 5;
		market.addInstrument(maturity);
		market.getInstrument(maturity).setAsynchronous();

		try {
			market.addOrder( bank, maturity, 1000, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 1 (askLimitOrders) 0 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.05 (BestAsk) 0.05 (AskLow) 1000.0 (AskVolume) 0.0 (BuyVolume) 0.0 (BidHigh) 0.0 (BidLow) 0.0 (BestBid) 0.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			market.addOrder( firm, maturity, - 500, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 1 (askLimitOrders) 0 (bidLimitOrders) 1 (filledOrders) 1 (partiallyFilledOrders) 0.05 (AskHigh) 0.05 (BestAsk) 0.05 (AskLow) 500.0 (AskVolume) 500.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.0 (BestBid) 0.0 (BidVolume) 0.0 (SellVolume) 0.05 (LastPrice) " );
			market.addOrder( firm, maturity, - 500, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 0 (askLimitOrders) 0 (bidLimitOrders) 3 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.0 (BestAsk) 0.05 (AskLow) 0.0 (AskVolume) 1000.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.0 (BestBid) 0.0 (BidVolume) 0.0 (SellVolume) 0.05 (LastPrice) " );
		} catch (final OrderException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}

	/**
	 * test scenario: 
	 * <ol>  
	 * <li>arrival of buy order 500</li>
	 * <li>arrival of buy order 500</li>
	 * <li>arrival of sell order 1000</li>
	 * </ol>
	 * test for immediate matching. (negative case)
	 */
	@Test(enabled = false)
	public void verifyAsynchronousInstrument1(){
		market = new CommercialLoanMarket();
		maturity = 5;
		market.addInstrument(maturity);
		market.getInstrument(maturity).setAsynchronous();
		try {
			market.addOrder( firm, maturity, - 500, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 0 (askLimitOrders) 1 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.0 (AskHigh) 0.0 (BestAsk) 0.0 (AskLow) 0.0 (AskVolume) 0.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.05 (BestBid) 500.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			market.addOrder( firm, maturity, - 500, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 0 (askLimitOrders) 2 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.0 (AskHigh) 0.0 (BestAsk) 0.0 (AskLow) 0.0 (AskVolume) 0.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.05 (BestBid) 1000.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			market.addOrder( bank, maturity, 1000, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 0 (askLimitOrders) 0 (bidLimitOrders) 3 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.0 (BestAsk) 0.05 (AskLow) 0.0 (AskVolume) 0.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.0 (BestBid) 0.0 (BidVolume) 1000.0 (SellVolume) 0.05 (LastPrice) " );
		} catch (final OrderException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}

	/**
	 * test scenario: 
	 * <ol>  
	 * <li>arrival of sell order 1000</li>
	 * <li>arrival of buy order 500</li>
	 * <li>arrival of buy order 500</li>
	 * </ol>
	 * test for immediate matching.
	 */
	@Test(enabled = false)
	public void verifySynchronousInstrument(){
		market = new CommercialLoanMarket();
		maturity = 5;
		market.addInstrument(maturity);
		market.getInstrument(maturity).setSynchronous();
		try {
			market.addOrder( bank, maturity, 1000, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 1 (askLimitOrders) 0 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.05 (BestAsk) 0.05 (AskLow) 1000.0 (AskVolume) 0.0 (BuyVolume) 0.0 (BidHigh) 0.0 (BidLow) 0.0 (BestBid) 0.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			market.addOrder( firm, maturity, - 500, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 1 (askLimitOrders) 1 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.05 (BestAsk) 0.05 (AskLow) 1000.0 (AskVolume) 0.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.05 (BestBid) 500.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			market.addOrder( firm, maturity, - 500, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 1 (askLimitOrders) 2 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.05 (BestAsk) 0.05 (AskLow) 1000.0 (AskVolume) 0.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.05 (BestBid) 1000.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			//next step
			simState.schedule.step( simState );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 0 (askLimitOrders) 0 (bidLimitOrders) 3 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.0 (BestAsk) 0.0 (AskLow) 0.0 (AskVolume) 1000.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.0 (BestBid) 1000.0 (BidVolume) 0.0 (SellVolume) 0.05 (LastPrice) " );
		} catch (final OrderException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}

	/**
	 * test scenario: 
	 * <ol>  
	 * <li>arrival of buy order 500</li>
	 * <li>arrival of buy order 500</li>
	 * <li>arrival of sell order 1000</li>
	 * </ol>
	 * test for immediate matching. (negative case)
	 */
	@Test(enabled = false)
	public void verifySynchronousInstrument1(){
		System.out.println("verifySynchronousInstrument1:");
		market = new CommercialLoanMarket();
		maturity = 5;
		market.addInstrument(maturity);
		market.getInstrument(maturity).setSynchronous();
		try {
			market.addOrder( firm, maturity, - 500, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 0 (askLimitOrders) 1 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.0 (AskHigh) 0.0 (BestAsk) 0.0 (AskLow) 0.0 (AskVolume) 0.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.05 (BestBid) 500.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			market.addOrder( firm, maturity, - 500, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 0 (askLimitOrders) 2 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.0 (AskHigh) 0.0 (BestAsk) 0.0 (AskLow) 0.0 (AskVolume) 0.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.05 (BestBid) 1000.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			market.addOrder( bank, maturity, 1000, 0.05 );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 1 (askLimitOrders) 2 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.05 (BestAsk) 0.05 (AskLow) 1000.0 (AskVolume) 0.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.05 (BestBid) 1000.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) " );
			//next step
			simState.schedule.step( simState );
			Assert.assertEquals(
					market.getInstrument( maturity ).toString(),
					"LOAN5 (symbol) 0 (askLimitOrders) 0 (bidLimitOrders) 3 (filledOrders) 0 (partiallyFilledOrders) 0.05 (AskHigh) 0.0 (BestAsk) 0.0 (AskLow) 0.0 (AskVolume) 1000.0 (BuyVolume) 0.05 (BidHigh) 0.05 (BidLow) 0.0 (BestBid) 1000.0 (BidVolume) 0.0 (SellVolume) 0.05 (LastPrice) " );
		} catch (final OrderException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}
	
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	private static Filter none = new Filter() {
		@Override
		public boolean matches(final Order order) {
			return false;
		}
	};
	
	@Test(enabled = false)
	public void testAsynchMatchingWithoutFilter()
			throws OrderException {
		market = new CommercialLoanMarket();
		
		LoanOrder firmOrder;
		try {
			firmOrder = market.addOrder( firm, 1, - 10, 1 );
			assertEquals( firmOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		LoanOrder bankOrder;
		try {
			bankOrder = market.addOrder( bank, 1, 10, 1 );
			assertEquals( bankOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}
	
	@Test(enabled = false)
	public void testAsynchOnlyMatchingFilter()
			throws OrderException {
		market = new CommercialLoanMarket();
		
		LoanOrder firmOrder;
		try {
			firmOrder = market.addOrder( firm, 1, - 10, 1, only( bank ) );
			assertEquals( firmOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		final MyBank bank2 = new MyBank( 10000 );
		LoanOrder bankOrder2;
		try {
			bankOrder2 = market.addOrder( bank2, 1, 10, 1, any() );
			assertEquals( bankOrder2.getNumberOfTrades(), 0 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		LoanOrder bankOrder1;
		try {
			bankOrder1 = market.addOrder( bank, 1, 10, 1, only( firm ) );
			assertEquals( bankOrder1.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}
	
	@Test(enabled = false)
	public void testAsynchMatching()
			throws OrderException {
		market = new CommercialLoanMarket();
		
		LoanOrder firmOrder;
		try {
			firmOrder = market.addOrder( firm, 1, - 10, 1, any() );
			assertEquals( firmOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		LoanOrder bankOrder;
		try {
			bankOrder = market.addOrder( bank, 1, 10, 1, any() );
			assertEquals( bankOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}
	
	@Test
	public void testAsynchNoMatching()
			throws OrderException {
		market = new CommercialLoanMarket();
		
		LoanOrder firmOrder;
		try {
			firmOrder = market.addOrder( firm, 1, - 10, 1, none );
			assertEquals( firmOrder.getNumberOfTrades(), 0 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		LoanOrder bankOrder;
		try {
			bankOrder = market.addOrder( bank, 1, 10, 1, none );
			assertEquals( bankOrder.getNumberOfTrades(), 0 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
	}
	
	private CommercialLoanMarket createSynchronouseCommercialLoanMarket(final int maturity) {
		final CommercialLoanMarket ret = new CommercialLoanMarket();
		market.addInstrument( maturity );
		market.getInstrument( maturity ).setSynchronous();
		return ret;
	}
	
	@Test(enabled = false)
	public void testSynchMatchingWithoutFilters()
			throws OrderException {
		market = createSynchronouseCommercialLoanMarket( 1 );
		
		LoanOrder firmOrder;
		try {
			firmOrder = market.addOrder( firm, 1, - 10, 1, any() );
			assertEquals( firmOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		LoanOrder bankOrder;
		try {
			bankOrder = market.addOrder( bank, 1, 10, 1, any() );
			assertEquals( bankOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		
	}
	
	@Test(enabled = false)
	public void testSynchMatching()
			throws OrderException {
		market = createSynchronouseCommercialLoanMarket( 1 );
		
		LoanOrder firmOrder;
		try {
			firmOrder = market.addOrder( firm, 1, - 10, 1, any() );
			assertEquals( firmOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		LoanOrder bankOrder;
		try {
			bankOrder = market.addOrder( bank, 1, 10, 1, any() );
			assertEquals( bankOrder.getNumberOfTrades(), 1 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		
	}
	
	@Test
	public void testSynchNoMatching()
			throws OrderException {
		market = createSynchronouseCommercialLoanMarket( 1 );
		
		LoanOrder firmOrder;
		try {
			firmOrder = market.addOrder( firm, 1, - 10, 1, none );
			assertEquals( firmOrder.getNumberOfTrades(), 0 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		LoanOrder bankOrder;
		try {
			bankOrder = market.addOrder( bank, 1, 10, 1, none );
			assertEquals( bankOrder.getNumberOfTrades(), 0 );
		} catch (AllocationException e) {
			Assert.fail( "Unexpected exception", e );
			e.printStackTrace();
			Simulation.getSimState().finish();
		}
		
	}
	
   /**
     * Manual entry point
     */
   static public void main(String[] args) {
      try {
         final MarketTest
            test = new MarketTest();
         test.setUp();
         test.verifyInstrument();
         test.testAsynchNoMatching();
         test.tearDown();
      }
      catch (final Exception e) {
         Assert.fail();
      }
      try {
         final MarketTest
            test = new MarketTest();
         test.setUp();
         test.verifyInstrument();
         test.testSynchNoMatching();
         test.tearDown();
      }
      catch (final Exception e) {
         Assert.fail();
      }
   }
}

