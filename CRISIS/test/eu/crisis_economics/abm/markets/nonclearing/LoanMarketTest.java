
/*
 * This file is part of CRISIS, an economics simulator.
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
 * 
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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import sim.engine.SimState;
import sim.util.Bag;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.CrisisMasonUtils;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.markets.Buyer;
import eu.crisis_economics.abm.markets.LoanOrder;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.LoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.LoanSeller;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author  olaf
 */
public class LoanMarketTest {

    private final int tau = 12;
    private final double interestRate = 0.001;

    private SimState simState;
    private LoanMarket loanMarket;
    private MyLender lender;
    private MyBorrower borrower;

    /**
     * @author  olaf
     */
    private class MyBorrower extends Agent implements Borrower, Buyer {

        private double funds;
        private final Bag orders;
        /**
         * @uml.property  name="liabilities"
         * @uml.associationEnd  
         */
        private final Bag liabilities;
        /**
         * @uml.property  name="market"
         * @uml.associationEnd  
         */
        private final LoanMarket market;
        private double allocatedCash=0.0;

        public MyBorrower(final LoanMarket market, final double funds) {
            super();
            this.funds = funds;
            this.orders = new Bag();
            this.liabilities = new Bag();
            this.market = market;
            try {
                this.market.addOrder(this, tau , -funds, interestRate);
            } catch (final OrderException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            } catch (AllocationException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            }
        }

        @Override
        public double credit(final double amt) throws InsufficientFundsException {
            try {
                if (funds < amt) {
                    throw new InsufficientFundsException("Insufficient Funds!");
                } 
                funds -= amt;
                return (amt);
            }
            finally {}
        }

        @Override
        public void debit(final double amt) {
            funds += amt;           
        }

        @Override
        public void addLiability(final Contract loan) {
            liabilities.add(loan);
            
        }

        @Override
        public boolean removeLiability(final Contract loan) {
            return liabilities.remove(loan);
        }

        @Override
        public List<Contract> getLiabilities() {
            return CrisisMasonUtils.bagToList( liabilities );
        }

        @Override
        public void updateState(final Order order) {
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
        public void handleBankruptcy() {
            Simulation.getSimState().finish();
        }

        @Override
        public boolean isBankrupt() {
            return false;
        }

      @Override
      public double allocateCash(double amount) {
         double allocatable = Math.max(funds - allocatedCash, 0.);
         amount = Math.min(allocatable, amount);
         allocatedCash += amount;
         return amount;
      }
      
      @Override
      public double disallocateCash(double amount) {
         amount = Math.min(allocatedCash, amount);
         allocatedCash -= amount;
         return amount;
      }
      
      @Override
      public double getAllocatedCash() {
         return allocatedCash;
      }

        @Override
        public double calculateAllocatedCashFromActiveOrders() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void cashFlowInjection(double amt) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void registerNewLoanTransfer(
           double principalCashValue, Lender lender, double interestRate) { }
        
        @Override
        public void registerNewLoanRepaymentInstallment(
           double installmentCashValue, Lender lender, double interestRate) { }

      @Override
      public double getUnallocatedCash() {
         return funds - getAllocatedCash();
      }
    }
    
    /**
     * @author  olaf
     */
    private class MyLender extends Agent implements Lender, LoanSeller, Buyer {

        private double funds;
        private List<Order> orders = new ArrayList<Order>();
        /**
         * @uml.property  name="assets"
         * @uml.associationEnd  
         */
        private final Bag assets;
        /**
         * @uml.property  name="market"
         * @uml.associationEnd  
         */
        private final LoanMarket market;
        private double allocatedCash=0.0;

        public MyLender(final LoanMarket market, final double funds) {
            super();
            this.funds  = funds;
            this.assets = new Bag();
            this.market = market;
            try {
                this.market.addOrder(this, tau , funds, interestRate);
                Assert.assertEquals(allocatedCash, funds);
            } catch (final OrderException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            } catch (AllocationException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            }
        }

        @Override
        public double credit(final double amt) throws InsufficientFundsException {
            try {
                if (funds < amt) {
                    throw new InsufficientFundsException("Insufficient Funds!");
                } 
                funds -= amt;
                return (amt);
            }
            finally {}
        }

        @Override
        public void debit(final double amt) {
            funds += amt;           
        }

        @Override
        public void addAsset(final Contract loan) {
            assets.add(loan);
            
        }

        @Override
        public boolean removeAsset(final Contract loan) {
            return assets.remove(loan);
        }
        
        @Override
        public List<Contract> getAssets() {
            return Collections.unmodifiableList( CrisisMasonUtils.<Contract>bagToList( assets ) );
        }

        @Override
        public void updateState(final Order order) {
            Assert.assertEquals(order.getOpenSize(), 500.0);
            Assert.assertEquals(allocatedCash, 1500.0);
        }

        @Override
        public void addOrder(final Order order) {
            this.orders.add(order);
        }

        @Override
        public boolean removeOrder(final Order order) {
            return this.orders.remove(order);
        }

      @Override
      public double allocateCash(double amount) {
         double allocatable = Math.max(funds - allocatedCash, 0.);
         amount = Math.min(allocatable, amount);
         allocatedCash += amount;
         return amount;
      }
      
      @Override
      public double disallocateCash(double amount) {
         amount = Math.min(allocatedCash, amount);
         allocatedCash -= amount;
         return amount;
      }
      
      @Override
      public double getAllocatedCash() {
         return allocatedCash;
      }

        @Override
        public double calculateAllocatedCashFromActiveOrders() {
            double cash = 0.0;
            int i = 0;
            for (Order order: orders) {
                if (order instanceof StockOrder){
                    if (((StockOrder)order).getSide()==Order.Side.BUY) {
                        cash += ((StockOrder)order).getOpenSize() * ((StockOrder)order).getPrice();
                        i++;
                    }
                    if (order instanceof LoanOrder){
                        if (((LoanOrder)order).getSide()==Order.Side.SELL) {
                            cash += ((LoanOrder)order).getOpenSize();
                            i++;
                        }
                    }
                }
            }
            if (cash != this.allocatedCash) {
                throw new RuntimeException("Stockflow inconsistency: allocated cash ( "+this.allocatedCash+" ), differs from acual allocation ( "+cash+" ) in "+i+" active orders.");
            }
            return cash;
        }

        @Override
        public void cashFlowInjection(double amt) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public double getUnallocatedCash() {
           throw new UnsupportedOperationException();
        }


    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUp() throws Exception {
        System.out.println("Test: " + this.getClass().getSimpleName());
        simState = new EmptySimulation(123L);
        simState.start();
        loanMarket = new CommercialLoanMarket();
        lender = new MyLender(loanMarket,1500);
        borrower = new MyBorrower(loanMarket,1000);
    }
    
    @AfterClass
    public void tearDown(){
        simState.finish();
        System.out.println("Test: " + this.getClass().getSimpleName() + " pass.");
    }

    /**
     * Test method for {@link eu.crisis_economics.abm.markets.nonclearing.LoanMarket#getInstrument(int)}.
     */
    // @Test
    /*
     * TODO: disabling until further notice. Instrument names 
     */
    public final void testGetInstrument() {
        Assert.assertNotNull(loanMarket.getInstrument(tau));
    }

    /**
     * Test method for {@link eu.crisis_economics.abm.markets.nonclearing.LoanMarket#step(sim.engine.SimState)}.
     */
    @Test
    public final void testStep() {
        long steps = 0;
        System.out.println("Steps: " + steps + "\tTime: " + simState.schedule.getTime() + "\tLender: " + lender.funds + "\tBorrower: " + borrower.funds + "\tVolume: " + loanMarket.getInstrument(tau).getBuyVolume());
        Assert.assertEquals(lender.allocatedCash, 500.0);
        do{
            if (!simState.schedule.step(simState)) {
                break;
            }
            steps = Simulation.getCycleIndex();
            loanMarket.cancelAllOrders();
            Assert.assertEquals(lender.allocatedCash, 0.0);         
            System.out.println("Steps: " + steps + "\tTime: " + simState.schedule.getTime() + "\tLender: " + lender.funds + "\tBorrower: " + borrower.funds + "\tVolume: " + loanMarket.getInstrument(tau).getBuyVolume());
        } while(steps < tau+1);
        simState.finish();
        Assert.assertEquals(1506.5119106836619, lender.funds);
        Assert.assertEquals(993.4880893163381, borrower.funds);
    }
   
   /**
     * Manual entry point.
     */
   static public void main(String[] args) {
      final LoanMarketTest test = new LoanMarketTest();
      try {
         test.setUp();
         test.testGetInstrument();
         test.testStep();
         test.tearDown();
      } catch (Exception e) {
         Assert.fail();
      }
   }
}
