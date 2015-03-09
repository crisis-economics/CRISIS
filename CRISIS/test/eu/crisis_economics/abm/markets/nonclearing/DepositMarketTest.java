/*
 * This file is part of CRISIS, an economics simulator.
 * 
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.DepositHolder;
import eu.crisis_economics.abm.contracts.Depositor;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.LiquidityException;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.DepositMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.DepositInstrument.AccountType;
import eu.crisis_economics.abm.simulation.EmptySimulation;

/**
 * @author Tamás Máhr
 * @author olaf
 */
public class DepositMarketTest {
	private static final double SUM = 1000;
	private SimState simState;
	private DepositMarket depositMarket;
	private MyDepositHolder myDepositHolder;
	private MyDepositor myDepositor;

	@BeforeMethod
	public void setUp() throws OrderException{
		System.out.println("Test: " + this.getClass().getSimpleName());
		simState = new EmptySimulation(0L);
		simState.start();
		depositMarket = new DepositMarket();
		myDepositHolder = new MyDepositHolder(depositMarket);
		myDepositor = new MyDepositor(SUM, depositMarket);
	}
	
	@AfterMethod
	public void tearDown() {
		simState.finish();
	}
	
	@Test
	public final void testStep() {
        System.out.print("Depositor cash: " + myDepositor.getCash());
        
        Assert.assertEquals(myDepositor.getAssets().get(0).getValue(), SUM);
        
        for (int step = 0 ; step < 10 ; step++){
        	if (myDepositor.getAssets().size() > 0){
        		System.out.println("\tDepositor assets: " + myDepositor.getAssets().get(0).getValue());
        		System.out.println("DepositHolder liability: " + myDepositHolder.getLiabilities().get(0).getValue());
        		Assert.assertEquals(myDepositHolder.getLiabilities().get(0), myDepositor.getAssets().get(0));
        	} else {
        		System.out.println();
        		Assert.assertEquals(myDepositor.getCash(), SUM);
        	}
        	if (!simState.schedule.step(simState)) {
        		break;
        	}
        }
	}

	
	private class MyDepositor implements Depositor {
        
        private double cash;
        private final List<Contract> assets = new ArrayList<Contract>();
        private final String uniqueID;
		/**
		 * @return the cash
		 */
		public double getCash() {
			return cash;
		}

		/**
		 * @return the assets
		 */
		@Override
		public List<Contract> getAssets() {
			return assets;
		}

		private final List<Order> orders = new ArrayList<Order>();
		
		public MyDepositor(final double cash, final DepositMarket market) throws OrderException {
			this.cash = cash;
			this.uniqueID = UUID.randomUUID().toString();
			market.addOrder(this, AccountType.DEBIT, cash, 0.03);
		}

		@Override
		public double credit(final double amt) throws InsufficientFundsException {
			if (cash < amt) {
				throw new InsufficientFundsException("MyDepositor does not has enough cash (" + cash +" < " + amt + ")");
			}
			
			cash -= amt;
			return amt;
		}

		@Override
		public void debit(final double amt) {
			cash += amt;
		}

		@Override
		public void addAsset(final Contract depositAccount) {
			assets.add(depositAccount);
		}

		@Override
		public boolean removeAsset(final Contract depositAccount) {
			return assets.remove(depositAccount);
		}

		@Override
		public void addOrder(final Order order) {
			orders.add(order);
		}

		@Override
		public boolean removeOrder(final Order order) {
			return orders.remove(order);
		}

		@Override
		public void updateState(final Order order) {
			System.out.println("Order " + order.getInstrument().getTickerSymbol() + " is updated at " + getClass().getName()+ ".");
		}

		@Override
		public void cashFlowInjection(double amt) {
			// TODO Auto-generated method stub
			
		}
        
        @Override
        public String getUniqueName() {
           return uniqueID;
        }

      @Override
      public double getDepositValue() {
         return cash;
      }
	}
	
	private class MyDepositHolder implements DepositHolder, Party {

		private final List<Contract> liabilities = new ArrayList<Contract>();
		private final List<Order> orders = new ArrayList<Order>();
        private final String uniqueID;
		
		private double funds;
		
		public MyDepositHolder(final DepositMarket market) throws OrderException {
			this.funds = 0.0;
			this.uniqueID = UUID.randomUUID().toString();
			market.addOrder(this, AccountType.DEBIT, -1000000, 0.03);
		}
		
		@Override
		public void addLiability(final Contract depositAccount) {
			liabilities.add(depositAccount);
		}

		@Override
		public boolean removeLiability(final Contract depositAccount) {
			return liabilities.remove(depositAccount);
		}
		
		@Override
		public List<Contract> getLiabilities() {
			return Collections.unmodifiableList( liabilities );
		}

		@Override
		public void addOrder(final Order order) {
			orders.add(order);
		}

		@Override
		public boolean removeOrder(final Order order) {
			return orders.remove(order);
		}

		@Override
		public void updateState(final Order order) {
			System.out.println("Order " + order.getInstrument().getTickerSymbol() + " is updated at " + getClass().getName() + ".");
		}

//		/**
//		 * @return the assets
//		 */
//		public List<Contract> getLiabilities() {
//			return assets;
//		}

		@Override
		public void decreaseCashReserves(final double amount)
				throws LiquidityException {
			if ( amount < 0 ) {
				throw new IllegalArgumentException( "Cannot decrease negative amount: " + amount );
			}
			if ( funds < amount ) {
				throw new LiquidityException( "Deposit holder is in a liquidity crisis" );
			}
			funds -= amount;
		}

		@Override
		public void increaseCashReserves(final double amount) {
			if ( amount < 0 ) {
				throw new IllegalArgumentException( "Cannot increase negative amount: " + amount );
			}
			funds += amount;
		}

		@Override
		public boolean isBankrupt() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void handleBankruptcy() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cashFlowInjection(double amount) {
			// TODO Auto-generated method stub
			
		}

      @Override
      public String getUniqueName() {
         return uniqueID;
      }
	}
	
   static public void main(String[] args) {
      DepositMarketTest test = new DepositMarketTest();
      try {
         test.setUp();
      } catch (OrderException e) {
         Assert.fail();
      }
      test.testStep();
      test.tearDown();
   }
}
