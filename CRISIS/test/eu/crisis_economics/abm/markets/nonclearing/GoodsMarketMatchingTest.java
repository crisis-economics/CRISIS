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
import java.util.List;

import org.junit.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.GoodsAccount;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.household.Household;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.SimpleResourceInventory;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.inventory.goods.SimpleGoodsRepository;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.nonclearing.GoodsMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author jakob e o frate ro cazz
 */
public class GoodsMarketMatchingTest {

    public static final double E = 0.0001; // error in double testing

    @SuppressWarnings("serial")
    private class MyFirm extends Agent implements Steppable, GoodsBuyer, GoodsSeller, Party {
        
        private GoodsMarket goodsMarket;
        private Bag orderList;
        private String goodsType;
        private ArrayList<Contract> assetList;
        private double productionCost;
        private String otherGoodsType;
        
        private GoodsRepository
           goodsWarehouse;
        
        private SimpleResourceInventory
           cashLedger;
        
        public MyFirm(
            double initialCash, 
            String goodsType, 
            String otherGoodsType,
            double initialQuantity, 
            double productionCost,
            GoodsMarket goodsMarket
            ) {
            this.orderList = new Bag();
            this.assetList = new ArrayList<Contract>();
            this.goodsType = goodsType;
            this.otherGoodsType = otherGoodsType;
            this.productionCost = productionCost;
            this.goodsMarket = goodsMarket;
            
            GoodsAccount.create(goodsType, this, initialQuantity, productionCost);
            
            Simulation.getSimState().schedule.scheduleOnce(this);
            
            cashLedger = new SimpleResourceInventory(initialCash);
            goodsWarehouse = new SimpleGoodsRepository();
            goodsWarehouse.push(goodsType, initialQuantity);
        }

        @Override
        public void step(SimState state) {
            System.out.println("firm step");
            try {
                goodsMarket.addOrder(this, goodsType, 9, productionCost * 1.5);// Party,
                                                                               // goods
                                                                               // Type,
                                                                               // quantity,
                                                                               // price
                goodsMarket.addOrder(this, otherGoodsType, -1, 5);
            } catch (AllocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (OrderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void addOrder(Order order) {
            this.orderList.add(order);
        }

        @Override
        public boolean removeOrder(Order order) {
            return this.orderList.remove(order);
        }

        @Override
        public void updateState(Order order) { }

        @Override
        public double credit(double cashAmount) throws InsufficientFundsException {
            try {
                cashLedger.pull(cashAmount);
            } catch (Exception fatalError) {
                System.out.println("CallGoodsMarketMatchingTest: test failed (exception thrown). Details follow:");
                fatalError.printStackTrace();
            }
            return cashAmount;
        }
        
        @Override
        public void debit(double cashAmount) {
            cashLedger.push(cashAmount);
        }
        
        @Override
        public double allocateCash(double amount) {
           return cashLedger.changeAllocatedBy(amount);
        }
        
        @Override
        public double disallocateCash(double amount) {
           return -cashLedger.changeAllocatedBy(-amount);
        }
        
        @Override
        public double getAllocatedCash() {
           return cashLedger.getAllocated();
        }

        @Override
        public double calculateAllocatedCashFromActiveOrders() {
            return 0;
        }

        @Override
        public void addAsset(Contract asset) {
            this.assetList.add(asset);
        }

        @Override
        public boolean removeAsset(Contract asset) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public List<Contract> getAssets() {
            return this.assetList;
        }

        @Override
        public void cashFlowInjection(double amt) {
            // TODO Auto-generated method stub
        }

        @Override
        public GoodsRepository getGoodsRepository() {
            return goodsWarehouse;
        }

        @Override
        public void registerIncomingTradeVolume(
            String goodsType, double goodsAmount, double unitPricePaid) { }
        
        @Override
        public void registerOutgoingTradeVolume(
            String goodsType, double goodsAmount, double unitPricePaid) { }
        
        @Override
        public <T> T accept(final AgentOperation<T> operation) {
           return operation.operateOn(this);
        }

      @Override
      public double getUnallocatedCash() {
         return cashLedger.getUnallocated();
      }

      @Override
      public double getGoodsSellingPrice() {
         throw new UnsupportedOperationException();
      }
    } // end Myfirm
    
    @SuppressWarnings("serial")
    private class MyHousehold extends Agent implements Steppable, GoodsBuyer, Party, Household {
        
        private Bag orderList;
        private GoodsMarket goodsMarket;
        private ArrayList<Contract> assetList;
        
        private GoodsRepository goodsRepository;
        
        private Inventory cashLedger;
        
        private final String
           firstGoodType,
           secondGoodType;
        
        public MyHousehold(
            GoodsMarket goodsMarket,
            final String firstGoodType,
            final String secondGoodType
            ) {
            this.orderList = new Bag();
            this.assetList = new ArrayList<Contract>();
            Simulation.getSimState().schedule.scheduleOnce(this);
            this.goodsMarket = goodsMarket;
            
            cashLedger = new SimpleResourceInventory(100);
            goodsRepository = new SimpleGoodsRepository();
            
            this.firstGoodType = firstGoodType;
            this.secondGoodType = secondGoodType;
        }

        @Override
        public void step(SimState state) {
            System.out.println("household step");
            try {
                goodsMarket.addOrder(this, firstGoodType, -3, 5);
                goodsMarket.addOrder(this, secondGoodType, -4, 5);
            } catch (AllocationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (OrderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void addOrder(Order order) {
            this.orderList.add(order);
        }

        @Override
        public boolean removeOrder(Order order) {
            return false;
        }

        @Override
        public void updateState(Order order) {
            // TODO Auto-generated method stub
        }

        @Override
        public double credit(double cashAmount) throws InsufficientFundsException {
            try {
                cashLedger.pull(cashAmount);
            } catch (Exception fatalError) {
                System.out.println("GoodsMarketMatchingTest: test failed (exception thrown). Details follow:");
                fatalError.printStackTrace();
            }
            return cashAmount;
        }
        
        @Override
        public void debit(double cashAmount) {
            cashLedger.push(cashAmount);
        }
        
        @Override
        public void addAsset(Contract asset) {
            this.assetList.add(asset);
        }
        
        @Override
        public boolean removeAsset(Contract asset) {
            this.assetList.remove(asset);
            return false;
        }
        
        @Override
        public List<Contract> getAssets() {
            return this.assetList;
        }
        
        @Override
        public double allocateCash(double amount) {
           double allocatedBefore = cashLedger.getAllocated();
           cashLedger.changeAllocatedBy(+amount);
           return cashLedger.getAllocated() - allocatedBefore;
        }
        
        @Override
        public double disallocateCash(double amount) {
           double disallocatedBefore = cashLedger.getUnallocated();
           cashLedger.changeAllocatedBy(-amount);
           return cashLedger.getUnallocated() - disallocatedBefore;
        }
        
        @Override
        public double getAllocatedCash() {
           return cashLedger.getAllocated();
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
        
        public double getCash() {
            return cashLedger.getStoredQuantity();
        }

        @Override
        public GoodsRepository getGoodsRepository() {
            return goodsRepository;
        }
        
        @Override
        public void registerIncomingTradeVolume(
            String goodsType, double goodsAmount, double unitPricePaid) { }
        
        @Override
        public <T> T accept(AgentOperation<T> operation) {
           return operation.operateOn(this);
        }

      @Override
      public double getUnallocatedCash() {
         return cashLedger.getUnallocated();
      }

      @Override
      public double getDepositValue() {
         return cashLedger.getStoredQuantity();
      }
    }

    private MyFirm firm1;
    private MyFirm firm2;
    private MyHousehold household1;
    private MyHousehold household2;
    private GoodsMarket goodsMarket;
    private Simulation simState;

    @BeforeMethod
    public void setUp() {
    	System.out.println("Test: " + this.getClass().getSimpleName());
        simState = new EmptySimulation(123L);
        simState.start();
        goodsMarket = new GoodsMarket();
        
        firm1 = new MyFirm(100, "Good 1", "Good 2", 50, 1, goodsMarket);
        firm2 = new MyFirm(100, "Good 2", "Good 1", 50, 2, goodsMarket);
        household1 = new MyHousehold(goodsMarket, "Good 1", "Good 2");
        household2 = new MyHousehold(goodsMarket, "Good 1", "Good 2");
    }

    @AfterMethod
    public void tearDown() {
        simState.finish();
        System.out.println("Test: " + this.getClass().getSimpleName() + " pass.");
    }

    @Test
    public void verifyAsynchronousMarket() {
        System.out.println("in test");

        do {
            if (Simulation.getSimState().schedule.getSteps() == 1) {
            /// AssertJUnit.assertEquals(2, household1.getAssets().size()); // TODO: restore when the goods repository is integrated into equity
                AssertJUnit.assertEquals(
                   3.0, household2.getGoodsRepository().getStoredQuantity("Good 1"), E);
                AssertJUnit.assertEquals(
                   3.0, household1.getGoodsRepository().getStoredQuantity("Good 1"), E);
                AssertJUnit.assertEquals(
                   4.0, household1.getGoodsRepository().getStoredQuantity("Good 2"), E);
                AssertJUnit.assertEquals(
                   4.0, household2.getGoodsRepository().getStoredQuantity("Good 2"), E);
                
                AssertJUnit.assertEquals(
                   43.0, firm1.getGoodsRepository().getStoredQuantity("Good 1"), E);
                AssertJUnit.assertEquals(
                   1.0, firm1.getGoodsRepository().getStoredQuantity("Good 2"), E);
                AssertJUnit.assertEquals(
                   41.0, firm2.getGoodsRepository().getStoredQuantity("Good 2"), E);
                AssertJUnit.assertEquals(
                   1.0, firm2.getGoodsRepository().getStoredQuantity("Good 1"), E);
            }

            if (!simState.schedule.step(simState)) {
                // System.out.println(((Labour)
                // firm1.getLabourList().get(1)).getWage());
                break;
            }

        } while (Simulation.getSimState().schedule.getSteps() < 50);
        System.out.println(household1.getCash());
        System.out.println(household2.getCash());
    }
    
    static public void main(String[] args) {
       try {
          GoodsMarketMatchingTest test = new GoodsMarketMatchingTest();
          test.setUp();
          test.verifyAsynchronousMarket();
          test.tearDown();
       }
       catch(Exception e) {
          Assert.fail();
       }
    }
}
