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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
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
import eu.crisis_economics.abm.markets.Seller;
import eu.crisis_economics.abm.markets.nonclearing.CommercialLoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.LoanMarket;
import eu.crisis_economics.abm.markets.nonclearing.Market;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockOrder;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author  olaf
 */
public class LoanMarketLobFig2Test {

    private final int tau = 12;                 //maturity of the loan
    //private final double tickSize = 0.01;     //smallest price interval
    //private final double lotSize  = 1;        //smallest amount of the asset

    private SimState simState;

    private LoanMarket loanMarket;
    @SuppressWarnings("unused")
    private MyLender lender;
    @SuppressWarnings("unused")
    private MyBorrower borrower;

    /**
     * a borrower that submits buy orders
     * @author  olaf
     */
    private class MyBorrower extends Agent implements Borrower, Buyer, Steppable {

        private static final long serialVersionUID = -2753040023925109582L;
        private final double sizes[] ={3,3,3,5};
        private final double prices[]={1.48,1.51,1.55,1.55};        
        private double funds;
        private final Bag liabilities;
        private final LoanMarket market;
        private final Schedule schedule;
        private final Bag orders;
        private double allocatedCash = 0.0;

        /**
         * sets up a borrower with a link to a loan market and some initial funds
         * @param schedule
         * @param market
         * @param funds
         */
        public MyBorrower(final Schedule schedule, final LoanMarket market, final double funds) {
            super();
            this.funds = funds;
            this.orders = new Bag();
            this.liabilities = new Bag();
            this.market = market;
            try {
                this.market.addOrder(this, tau, -3, 1.47); // [1]
                this.market.addOrder(this, tau, -3, 1.48); // [2]
                this.market.addOrder(this, tau, -1, 1.49); // [3]
                this.market.addOrder(this, tau, -2, 1.50); // [4]
            } catch (final OrderException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            } catch (AllocationException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            }
            this.schedule = schedule;
            schedule.scheduleOnce(this);
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

        @Override //borrower
        public void step(final SimState state) {
            try {
                this.market.addOrder(this, tau,
                        -sizes[(int) this.schedule.getSteps()],
                        prices[(int) this.schedule.getSteps()]);
            } catch (final OrderException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            } catch (AllocationException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            }
            if (this.schedule.getSteps() < sizes.length - 1) {
                schedule.scheduleOnce(this);
            }
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
         // TODO Auto-generated method stub
         return 0;
      }
    }
    
    /**
     * a lender that submits buy orders
     * @author  olaf
     */
    private class MyLender extends Agent implements Lender, Seller, Steppable, Buyer {

        private static final long serialVersionUID = 5803242021852004272L;
        private final double sizes[] = {4,4,4,4};
        private final double prices[] = {1.54,1.52,1.47,1.50};
        private double funds;
        private List<Order> orders = new ArrayList<Order>();
        private final Bag assets;
        private final LoanMarket market;
        private final Schedule schedule;
        private final double offset = 4.0;
        private double allocatedCash=0.0;

        /**
         * sets up a lender with a link to a loan market and some initial funds;
         * submit initial orders
         * @param schedule
         * @param market
         * @param funds
         */
        public MyLender(final Schedule schedule, final LoanMarket market, final double funds) {
            super();
            this.funds  = funds;
            this.assets = new Bag();
            this.market = market;
            try {
                this.market.addOrder(this, tau, 2, 1.53); // [5]
                this.market.addOrder(this, tau, 3, 1.54); // [6]
                this.market.addOrder(this, tau, 1, 1.55); // [7]
            } catch (final OrderException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            } catch (AllocationException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            }

            this.schedule = schedule;
            schedule.scheduleOnce(offset,this);
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
        }

        @Override
        public void addOrder(final Order order) {
            this.orders.add(order);
        }

        @Override
        public boolean removeOrder(final Order order) {
            return this.orders.remove(order);
        }

        @Override //lender
        public void step(final SimState state) {
            try {
                this.market.addOrder(this, tau , sizes[(int) this.schedule.getSteps() - (int) offset], prices[(int) this.schedule.getSteps() - (int) offset]);
            } catch (final OrderException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            } catch (AllocationException e) {
                e.printStackTrace();
                Simulation.getSimState().finish();
            }
            if (this.schedule.getSteps()<sizes.length + offset-1) {
                schedule.scheduleOnce(this);
            }
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
        loanMarket.setInstrumentMatchingMode(Market.InstrumentMatchingMode.ASYNCHRONOUS);
        lender = new MyLender(simState.schedule, loanMarket,1500);
        borrower = new MyBorrower(simState.schedule, loanMarket,1000);
    }
    
    @AfterClass
    public void tearDown(){
        simState.finish();
        System.out.println("Test: " + this.getClass().getSimpleName() + " pass.");
    }

    /**
     * Test method for initial scenario.
     */
    @Test(enabled = true)
    public final void testInitInstrument() {
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),1.54);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),3.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidAskSpread()  ,0.03, 0.0001);
        Assert.assertEquals(loanMarket.getInstrument(tau).getMidPrice()  ,1.515, 0.0001);
        Assert.assertEquals(loanMarket.getInstrument(tau).getRelativePrice((Order)(loanMarket.getInstrument(tau).getAskLimitOrders().get(2)))  ,0.02, 0.0001);
        Assert.assertEquals(loanMarket.getInstrument(tau).getRelativePrice((Order)(loanMarket.getInstrument(tau).getBidLimitOrders().get(2)))  ,0.02, 0.0001);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.49);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),9.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.4822);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,4);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,3);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,0);
    }

    /**
     * Test method for scenario at T=0 after BuyOrder(-3, 1.48). [8]
     * inserted as active order
     * 
     */
     //TODO it fails because we reset instrument statistics every time step
    @Test(enabled = false)
    public final void testT0Instrument() {
        simState.schedule.step(simState);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),1.54);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),3.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.49);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),12.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.4817);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,3);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,0);
    }

    /**
     * Test method for scenario at T=1 after BuyOrder(-3, 1.51). [9]
     * inserted as active order
     */
    @Test(enabled = false)
    public final void testT1Instrument() {
        simState.schedule.step(simState);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),1.54);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),3.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.51);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.51);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),15.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.4873);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,6);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,3);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,0);
    }

    /**
     * Test method for scenario at T=2 after BuyOrder(-3, 1.55). [10]
     * match(10,{5,6})->6.1
     */
    @Test(enabled = false)
    public final void testT2Instrument() {
        simState.schedule.step(simState);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),3.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),2.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),1.5333);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),1.5333);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,1.54);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.51);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),15.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.4978);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),3.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,6);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),1.54);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,2);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,2);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,1);
    }

    /**
     * Test method for scenario at T=3 after BuyOrder(-5, 1.55). [11]
     * match(10,{6.1,7})->11.1
     */
    @Test(enabled = false)
    public final void testT3Instrument() {
        simState.schedule.step(simState);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.51);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),17.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.5091);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,7);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,0);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,4);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,1);
    }

    /**
     * Test method for scenario at T=4 after SellOrder(4, 1.54). [12]
     * match(12,{11.1})->12.1
     */
    @Test(enabled = false)
    public final void testT4Instrument() {
        simState.schedule.step(simState);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),0.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),2.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),2.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.539);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),1.5413);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,1.54);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.51);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),15.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.5091);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,6);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),2.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,1);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,1);
    }

    /**
     * Test method for scenario at T=5 after SellOrder(4, 1.52). [13]
     * inserted as active order
     */
    @Test(enabled = false)
    public final void testT5Instrument() {
        simState.schedule.step(simState);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.52);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),1.54);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),2.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.5336);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),1.5413);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,1.52);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.51);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),15.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.5091);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,6);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),2.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,2);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,1);
    }

    /**
     * Test method for scenario at T=6 after SellOrder(4, 1.47). [14]
     * match(14, {9+4})->4.1
     */
    @Test(enabled = false)
    public final void testT6Instrument() {
        simState.schedule.step(simState);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),1.54);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),2.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.5194);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),1.53);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),1.5217);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,1.52);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.49);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),11.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.5091);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,2);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,7);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,2);
    }

    /**
     * Test method for scenario at T=7 after SellOrder(4, 1.50). [15]
     * match(15, {4.1})->15.1
     */
    @Test(enabled = false)
    public final void testT7Instrument() {
        simState.schedule.step(simState);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskPriceAtDepth(1),1.52);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolume(),9.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVolumeAtPrice(1.54),2.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskVWAP()  ,1.5159);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageBuyPrice(),1.5383);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAveragePrice(),1.5277);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAverageSellPrice(),1.5186);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestAsk()  ,1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBestBid()  ,1.49);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidHigh()  ,1.55);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLow()   ,1.47);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidPriceAtDepth(1),1.48);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolume(),10.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVolumeAtPrice(1.49),1.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidVWAP()  ,1.5091);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBuyVolume(),6.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getBidLimitOrders().numObjs,4);
        Assert.assertEquals(loanMarket.getInstrument(tau).getLastPrice(),1.5);
        Assert.assertEquals(loanMarket.getInstrument(tau).getSellVolume(),7.0);
        Assert.assertEquals(loanMarket.getInstrument(tau).getAskLimitOrders().numObjs,3);
        //Assert.assertEquals(loanMarket.getInstrument(tau).getFilledOrders().numObjs,8);
        Assert.assertEquals(loanMarket.getInstrument(tau).getPartiallyFilledOrders().numObjs,2);
    }
    
    /**
      * Manual entry point.
      */
    public static void main(String[] args) {
       try {
          final LoanMarketLobFig2Test
             test = new LoanMarketLobFig2Test();
          test.setUp();
          test.testInitInstrument();
          test.tearDown();
       }
       catch(final Exception e) {
          Assert.fail();
       }
    }
}
