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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.agent.InstanceIDAgentNameFactory;
import eu.crisis_economics.abm.bank.StrategyBank;
import eu.crisis_economics.abm.bank.strategies.BankStrategy;
import eu.crisis_economics.abm.bank.strategies.EmptyBankStrategy;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.firm.StrategyFirm;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.InstrumentEvent;
import eu.crisis_economics.abm.markets.nonclearing.InstrumentListener;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.Market.InstrumentMatchingMode;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.BankStrategyFactory;

public class MarketMatchingTest {
	
	private static class MyBankStrategy extends EmptyBankStrategy {
		private static final int COMMERCIAL_LOAN_MATURITY = 5;
		private final double[] ORDER_SIZE = new double[]{10,20,30,40};
		private final double[] ORDER_PRICE = new double[]{1,1.5,3,4};
		
		public MyBankStrategy(final StrategyBank bank) {
		   super(bank);
		}
		
		@Override
		public void considerCommercialLoanMarkets() {
			for (final Market market : getBank().getMarketsOfType(CommercialLoanMarket.class)) {
				try {
					((CommercialLoanMarket) market).addOrder(getBank(),
								COMMERCIAL_LOAN_MATURITY,
								ORDER_SIZE[(int) Simulation.getSimState().schedule.getSteps()],	// <0
								ORDER_PRICE[(int) Simulation.getSimState().schedule.getSteps()]);
					System.out.println("Bank Order: "+ORDER_PRICE[(int) Simulation.getSimState().schedule.getSteps()]+" (rate) "+ORDER_SIZE[(int) Simulation.getSimState().schedule.getSteps()]+" (size)");
				} catch (final OrderException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				} catch (AllocationException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				}
			}
		}
      
      public static BankStrategyFactory factory() {
         return new BankStrategyFactory() {
            @Override
            public BankStrategy create(
               StrategyBank bank) {
               return new MyBankStrategy(bank);
            }
         };
      }
   }
	
	private class MyBank extends StrategyBank implements InstrumentListener {

		private final String[] EXECUTED_ORDER = new String[] {
				"LOAN5 (instrument) 10.0 (size) 0.0 (openSize) 10.0 (executedSize) SELL (side) LIMIT (type) NOT_PROCESSED (status) 1.0 (price) ",
				"LOAN5 (instrument) 20.0 (size) 0.0 (openSize) 20.0 (executedSize) SELL (side) LIMIT (type) NOT_PROCESSED (status) 1.5 (price) ", 	//TODO
				"LOAN5 (instrument) 30.0 (size) 0.0 (openSize) 30.0 (executedSize) SELL (side) LIMIT (type) NOT_PROCESSED (status) 3.0 (price) ",	//TODO
				"LOAN5 (instrument) 40.0 (size) 0.0 (openSize) 40.0 (executedSize) SELL (side) LIMIT (type) NOT_PROCESSED (status) 4.0 (price) "
		};

		public MyBank(final double initialCash) {
			super(initialCash, MyBankStrategy.factory());
		}
		
		@Override
		public void updateState(final Order order) {
			Assert.assertEquals(order.toString(),EXECUTED_ORDER[(int) Simulation.getSimState().schedule.getSteps()]," in step "+Simulation.getSimState().schedule.getSteps());
			System.out.println("MyBank executed order: "+order.toString());
		}
		
		@Override
		public void newContract(final InstrumentEvent instrumentEvent) {
			System.out.println("MyBank new contract: "+instrumentEvent.getContract().toString());
		}

		@Override
		public void orderUpdated(final InstrumentEvent instrumentEvent) {
			System.out.println("MyBank order updated: "+instrumentEvent.getOrder().toString());
		}
	}

	private class MyFirm extends StrategyFirm {

		private static final int COMMERCIAL_LOAN_MATURITY = 5;
		private final String[] EXECUTED_ORDER = new String[] {
				"LOAN5 (instrument) 10.0 (size) 0.0 (openSize) 10.0 (executedSize) BUY (side) LIMIT (type) NOT_PROCESSED (status) 1.0 (price) ",
				"LOAN5 (instrument) 20.0 (size) 0.0 (openSize) 20.0 (executedSize) BUY (side) LIMIT (type) NOT_PROCESSED (status) 2.0 (price) ",	//TODO
				"LOAN5 (instrument) 30.0 (size) 0.0 (openSize) 30.0 (executedSize) BUY (side) LIMIT (type) NOT_PROCESSED (status) 3.5 (price) ",	//TODO
				"LOAN5 (instrument) 40.0 (size) 0.0 (openSize) 40.0 (executedSize) BUY (side) LIMIT (type) NOT_PROCESSED (status) 4.0 (price) "
		};
		private final double[] ORDER_SIZE = new double[]{-10,-20,-30,-40};
		private final double[] ORDER_PRICE = new double[]{1,2,3.5,4};

      public MyFirm(
         final DepositHolder depositHolder,
         final double initialDeposit,
         final StockHolder stockHolder,
         final int numberOfShares
         ) {
         super(
            depositHolder,
            initialDeposit,
            stockHolder,
            numberOfShares,
            new InstanceIDAgentNameFactory()
            );
         // TODO Auto-generated constructor stub
      }

		@Override
		public void updateState(final Order order) {
			Assert.assertEquals(order.toString(),EXECUTED_ORDER[(int) Simulation.getSimState().schedule.getSteps()]," in step "+Simulation.getSimState().schedule.getSteps());
			System.out.println("MyFirm executed order: "+order.toString());
		}

		@Override
		protected void considerProduction() {
		}

		@Override
		protected void considerCommercialLoanMarkets() {
			for (final Market market : getMarketsOfType(CommercialLoanMarket.class)) {
				try {
					((CommercialLoanMarket) market).addOrder(this,
								COMMERCIAL_LOAN_MATURITY,
								ORDER_SIZE[(int) Simulation.getSimState().schedule.getSteps()],	// <0
								ORDER_PRICE[(int) Simulation.getSimState().schedule.getSteps()]);
					System.out.println("Firm Order: "+ORDER_PRICE[(int) Simulation.getSimState().schedule.getSteps()]+" (rate) "+ORDER_SIZE[(int) Simulation.getSimState().schedule.getSteps()]+" (size)");
				} catch (final OrderException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				} catch (AllocationException e) {
					e.printStackTrace();
					Simulation.getSimState().finish();
				}
			}
		}

		@Override
		protected void considerDepositPayment() {
		}

		@Override
		public void cashFlowInjection(double amt) {
		}

	}

	private static final int COMMERCIAL_LOAN_MATURITY = 5;

	private final double[] LAST_PRICE = new double[]{0,1.0,1.5,3.0,4.0};

	MyBank bank;
	MyFirm firm;
	CommercialLoanMarket market;
	int maturity;
	private SimState simState;
	
	@BeforeMethod
	public void setUp(){
		System.out.println("Test: " + this.getClass().getSimpleName());
		simState = new EmptySimulation(123L);
		simState.start();
	}
	
	@AfterMethod
	public void tearDown(){
		simState.finish();
		System.out.println("Test: " + this.getClass().getSimpleName() + " pass.");
	}
	
	@Test(enabled = false)
	public void verifyAsynchronousMarket(){
		bank = new MyBank(10000);
		firm = new MyFirm(bank,0,bank,100);
		market = new CommercialLoanMarket();
		market.setInstrumentMatchingMode(InstrumentMatchingMode.ASYNCHRONOUS);
		bank.addMarket(market);
		firm.addMarket(market);
	    do{
	    	if (!simState.schedule.step(simState)) {
	    		break;
	    	}
	    	//Assert.assertEquals(market.getInstrument(COMMERCIAL_LOAN_MATURITY).getLastPrice(),LAST_PRICE[(int) Simulation.getSimState().schedule.getSteps()]," in step "+Simulation.getSimState().schedule.getSteps());
	    } while(Simulation.getSimState().schedule.getSteps() < 4);
	}

	@Test(enabled = false)
	public void verifySynchronousMarket(){
		bank = new MyBank(10000);
		firm = new MyFirm(bank,0,bank,100);
		market = new CommercialLoanMarket();
		market.setInstrumentMatchingMode(InstrumentMatchingMode.SYNCHRONOUS);
		market.addListener(bank);
		bank.addMarket(market);
		firm.addMarket(market);
	    do{
	    	if (!simState.schedule.step(simState)) {
	    		break;
	    	}
	    	Assert.assertEquals(market.getInstrument(COMMERCIAL_LOAN_MATURITY).getLastPrice(),LAST_PRICE[(int) Simulation.getSimState().schedule.getSteps()]," in step "+Simulation.getSimState().schedule.getSteps());
	    } while(Simulation.getSimState().schedule.getSteps() < 4);
	}

   /**
     * Manual entry point.
     */
   static public void main(String[] args) {
      final MarketMatchingTest
         test = new MarketMatchingTest();
      try {
         test.setUp();
         test.tearDown();
      } catch (Exception e) {
         Assert.fail();
      }
   }
}