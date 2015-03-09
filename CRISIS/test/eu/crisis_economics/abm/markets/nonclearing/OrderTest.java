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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.bank.Bank;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.LoanInstrument;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.EmptySimulation;

/**
 * order test
 * @author  olaf
 */
public class OrderTest {
	
	private class MyBank extends StrategyBank {

		public MyBank(final double initialCash) {
			super(initialCash, EmptyBankStrategy.factory());
		}

	}
	
	/**
	 * holds all updated orders that have been either fully or partially processed, these are pushed to subscribed clients
	 */
	BlockingQueue<Order> updatedOrders;
	
	/**
	 * @uml.property  name="household"
	 * @uml.associationEnd  
	 */
	DepositingHousehold household;
	/**
	 * @uml.property  name="bank"
	 * @uml.associationEnd  
	 */
	Bank bank;
	/**
	 * @uml.property  name="firm"
	 * @uml.associationEnd  
	 */
	Firm firm;
	/**
	 * @uml.property  name="order"
	 * @uml.associationEnd  
	 */
	Order order;
	/**
	 * @uml.property  name="instrument"
	 * @uml.associationEnd  
	 */
	LoanInstrument instrument;

	private SimState simState;
	
	@BeforeClass
	public void setUp(){
		System.out.println("Test: " + this.getClass().getSimpleName());
		simState = new EmptySimulation(1L);
		simState.start();		
		bank  = new MyBank(0);
		firm  = new StockReleasingFirm(
		   bank, 0., (StockHolder) bank, 0., 0., new InstanceIDAgentNameFactory());
		household = new DepositingHousehold(bank);
		updatedOrders = new LinkedBlockingQueue<Order>();
		instrument = new LoanInstrument(5,updatedOrders);
		try {
			order  = new CommercialLoanOrder(bank,instrument, -200.0, 15.0);
		} catch (final OrderException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public void tearDownTest(){
		simState.finish();
	}
	
	@Test
	public void verifyInstrument(){
		Assert.assertEquals(order.getInstrument().toString(),"LOAN5 (symbol) 0 (askLimitOrders) 1 (bidLimitOrders) 0 (filledOrders) 0 (partiallyFilledOrders) 0.0 (AskHigh) 0.0 (BestAsk) 0.0 (AskLow) 0.0 (AskVolume) 0.0 (BuyVolume) 15.0 (BidHigh) 15.0 (BidLow) 15.0 (BestBid) 200.0 (BidVolume) 0.0 (SellVolume) 0.0 (LastPrice) ");
	}
	@Test
	public void verifyRegisteredSeller(){
		Order myOrder = null;
		try {
			myOrder  = new CommercialLoanOrder(firm, instrument, 200.0, 15.0);
		} catch (final OrderException e) {
			//e.printStackTrace();
		}
		Assert.assertNull(myOrder);
	}	
	@Test
	public void verifyRegisteredBuyer(){
		Order myOrder = null;
		try {
			myOrder  = new CommercialLoanOrder(household, instrument, 200.0, 15.0);
		} catch (final OrderException e) {
			//e.printStackTrace();
		}
		Assert.assertNull(myOrder);
	}	
	@Test
	public void verifySideAndType(){
		Assert.assertEquals(order.getSide(), Order.Side.BUY);
		Assert.assertEquals(order.getType(), Order.Type.LIMIT);
	}
	
	@Test
	public void verifyTotalVolumeAndLimitPrice(){
		Assert.assertEquals(order.getSize(), 200.0);
		Assert.assertEquals(order.getPrice(), 15.0);
	}
	
	@Test 
	public void verifyVolumeCalculations(){
		order.execute(20, 14);
		order.execute(30, 11);
		Assert.assertEquals(order.getExecutedSize(), 50.0);
		Assert.assertEquals(order.getOpenSize(), 150.0);
		Assert.assertNotNull(order.getTrades());
		Assert.assertEquals(order.isFilled(), false);
		Assert.assertEquals(order.isClosed(), false);
	
// ----------------------------------------------------------------------------------------------------------------------
// A recent TestNG issue cause some @BeforeClass initialization take place before the current class' @AfterClass cleanup
// is finished. This cause nondeterministic test failures, so we should avoid this.
//
// For the details, please visit this thread:
//	 		https://groups.google.com/d/topic/testng-users/KygOcRvd078/discussion
//		}
//			
//	@Test (dependsOnMethods = {"verifyVolumeCalculations"})
//	public void verifyAverageCalculationsAndFills(){
		Assert.assertEquals(order.getAverageExecutedPrice(), 12.2);
		Assert.assertEquals(order.getLastExecutedPrice(), 11.0);
		Assert.assertEquals(order.getNumberOfTrades(), 2);
		Assert.assertEquals(order.getLastExecutedVolume(), 30.0);
	}
	
	public static void main(String[] args) {
	   OrderTest test = new OrderTest();
	   System.out.println("Test: OrderTest..");
	   try {
           test.setUp();
           test.verifyInstrument();
           test.tearDownTest();
	   }
	   catch(Exception e) {
	      Assert.fail();
	   }
	   System.out.println("OrderTest tests OK.");
	   return;
	}
}

