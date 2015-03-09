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
import eu.crisis_economics.abm.firm.Firm;
import eu.crisis_economics.abm.firm.StockReleasingFirm;
import eu.crisis_economics.abm.household.DepositingHousehold;
import eu.crisis_economics.abm.markets.LoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.LoanInstrument;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.EmptySimulation;


/**
 * instrument test
 * @author  olaf
 */
public class InstrumentTest {
	
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
	 * @uml.property  name="instrument"
	 * @uml.associationEnd  
	 */
	LoanInstrument instrument;
	
	/**
	 * @uml.property  name="bOrder"
	 * @uml.associationEnd  
	 */
	LoanOrder bOrder;
	/**
	 * @uml.property  name="bOrder2"
	 * @uml.associationEnd  
	 */
	LoanOrder bOrder2;
	/**
	 * @uml.property  name="bOrder3"
	 * @uml.associationEnd  
	 */
	LoanOrder bOrder3;
	/**
	 * @uml.property  name="bOrder4"
	 * @uml.associationEnd  
	 */
	LoanOrder bOrder4;
	/**
	 * @uml.property  name="bOrder5"
	 * @uml.associationEnd  
	 */
	LoanOrder bOrder5;
	/**
	 * @uml.property  name="sOrder"
	 * @uml.associationEnd  
	 */
	LoanOrder sOrder;
	/**
	 * @uml.property  name="sOrder2"
	 * @uml.associationEnd  
	 */
	LoanOrder sOrder2;
	/**
	 * @uml.property  name="sOrder3"
	 * @uml.associationEnd  
	 */
	LoanOrder sOrder3;
	/**
	 * @uml.property  name="sOrder4"
	 * @uml.associationEnd  
	 */
	LoanOrder sOrder4;
	/**
	 * @uml.property  name="sOrder5"
	 * @uml.associationEnd  
	 */
	LoanOrder sOrder5;

	private SimState simState;

	
	@BeforeClass
	public void setUp(){
		System.out.println("Test: " + this.getClass().getSimpleName());
		simState = new EmptySimulation(1L);
		simState.start();
		StrategyBank bank = new StrategyBank(0, EmptyBankStrategy.factory());
		firm = new StockReleasingFirm(bank, 0, bank, 0, 0, new InstanceIDAgentNameFactory());
		updatedOrders = new LinkedBlockingQueue<Order>();
		instrument = new LoanInstrument(5,updatedOrders);
	}
	
	@AfterClass
	public void tearDown(){
		simState.finish();
		System.out.println("Test: " + this.getClass().getSimpleName() + " pass.");
	}
	
	@Test
	public void testInstrumentName(){
		Assert.assertEquals(instrument.getTickerSymbol(), "LOAN5");
	}
	
	@Test
	public void insertBuyOrders() throws InterruptedException{
		try {
			bOrder  = new CommercialLoanOrder(firm, instrument,  -500.0, 24.0620);
		} catch (final OrderException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		try {
			bOrder2 = new CommercialLoanOrder(firm, instrument, -5000.0, 24.0600);
		} catch (final OrderException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		try {
			bOrder3 = new CommercialLoanOrder(firm, instrument, -6000.0, 24.0610);
		} catch (final OrderException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			bOrder4 = new CommercialLoanOrder(firm, instrument, -1100.0, 24.0550);
		} catch (final OrderException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			bOrder5 = new CommercialLoanOrder(firm, instrument,  -100.0, 24.0600);
		} catch (final OrderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
// ----------------------------------------------------------------------------------------------------------------------
// A recent TestNG issue cause some @BeforeClass initialization take place before the current class' @AfterClass cleanup
// is finished. This cause nondeterministic test failures, so we should avoid this.
//
// For the details, please visit this thread:
// 		https://groups.google.com/d/topic/testng-users/KygOcRvd078/discussion
		
//	}
//	
//	@Test (dependsOnMethods = {"insertBuyOrders"})
//	public void verifyInsertionOfNewBuyOrders(){
// ----------------------------------------------------------------------------------------------------------------------
		
		Assert.assertEquals(instrument.getBidLimitOrders().size(), 5);
		System.out.println();
		for(int i=0; i<instrument.getBidLimitOrders().size(); i++){
			System.out.println(instrument.getBidLimitOrders().get(i));
		}
		
	}
   
   /**
     * Manual entry point
     */
   public static final void main(String[] args) {
      try {
         final InstrumentTest
            test = new InstrumentTest();
         test.setUp();
         test.testInstrumentName();
         test.tearDown();
      }
      catch(final Exception e) {
         Assert.fail();
      }
   }
}
