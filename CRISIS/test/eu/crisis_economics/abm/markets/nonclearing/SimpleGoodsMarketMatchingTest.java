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

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.util.Bag;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.algorithms.matching.ForagerMatchingAlgorithm;
import eu.crisis_economics.abm.algorithms.matching.HomogeneousRationingAlgorithm;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.GoodsAccount;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.inventory.SimpleResourceInventory;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository.SimpleGoodsClassifier;
import eu.crisis_economics.abm.inventory.goods.GoodsRepository;
import eu.crisis_economics.abm.inventory.goods.SimpleGoodsRepository;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.GoodsSeller;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsInstrument;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author jakob e o frate ro cazz
 */
public class SimpleGoodsMarketMatchingTest {

    public static final double E = 0.0001; // error in double testing

    private class MyFirm extends Agent implements GoodsBuyer, GoodsSeller, Party {
        private Bag orderList;
        private ArrayList<Contract> assetList;
        private DurableGoodsRepository goodsWarehouse;    // Goods repository
        
        private SimpleResourceInventory cashLedger;
        
        public MyFirm(
            double initialCash,
            String goodsType, 
            int otherGoodsType,
            double initialQuantity, 
            double productionCost,
            SimpleGoodsMarket goodsMarket
            ) {
            this.orderList = new Bag();
            this.assetList = new ArrayList<Contract>();
            GoodsAccount.create(goodsType, this, initialQuantity, productionCost);
            
            cashLedger = new SimpleResourceInventory(initialCash);
            goodsWarehouse = new DurableGoodsRepository(goodsMarket.getGoodsClassifier());
            goodsWarehouse.push(goodsType, initialQuantity);
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
        public double allocateCash(double cashAmount) {
           return cashLedger.changeAllocatedBy(cashAmount);
        }
        
        @Override
        public double disallocateCash(double cashAmount) {
           return -cashLedger.changeAllocatedBy(-cashAmount);
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
            return false;
        }

        @Override
        public List<Contract> getAssets() {
            return this.assetList;
        }
        
        @Override
        public void cashFlowInjection(double amt) { }
        
        @Override
        public DurableGoodsRepository getGoodsRepository() {
           return goodsWarehouse;
        }
        
        @Override
        public void registerIncomingTradeVolume(
           String goodsType, double goodsAmount, double unitCostPaid) { }
        
        @Override
        public void registerOutgoingTradeVolume(
           String goodsType, double goodsAmount, double unitPricePaid) { }

      @Override
      public double getUnallocatedCash() {
         return cashLedger.getUnallocated();
      }
      
      @Override
      public double getGoodsSellingPrice() {
         throw new UnsupportedOperationException();
      }
    }

    private class MyHousehold extends Agent implements GoodsBuyer, Party {
        private double initialCash;
        private Bag orderList;
        private ArrayList<Contract> assetList;
        
        private GoodsRepository goodsRepository;
        
        private SimpleResourceInventory cashLedger;
        
        public MyHousehold(SimpleGoodsMarket goodsMarket) {
            this.initialCash = 1000;
            this.orderList = new Bag();
            this.assetList = new ArrayList<Contract>();
            cashLedger = new SimpleResourceInventory(initialCash);
            goodsRepository = new SimpleGoodsRepository();
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
        public double allocateCash(double cashAmount) {
           return cashLedger.changeAllocatedBy(cashAmount);
        }
        
        @Override
        public double disallocateCash(double cashAmount) {
           return -cashLedger.changeAllocatedBy(-cashAmount);
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
        public void cashFlowInjection(double amt) { }
        
        @Override
        public GoodsRepository getGoodsRepository() {
            return goodsRepository;
        }
        
        @Override
        public void registerIncomingTradeVolume(
           String goodsType, double goodsAmount, double unitCostPaid) { }
        
      @Override
      public double getUnallocatedCash() {
         return cashLedger.getUnallocated();
      }
    }

    private MyFirm firm1;
    private MyFirm firm2;
    private MyHousehold household1;
    private MyHousehold household2;
    private SimpleGoodsMarket goodsMarket;
    private Simulation simState;

    @BeforeMethod
    public void setUp() {
        System.out.println("Test: " + this.getClass().getSimpleName() + " begins.");
        simState = new EmptySimulation(123L);
        simState.start();
        
        goodsMarket = new SimpleGoodsMarket(
           new SimpleGoodsClassifier(),
           new ForagerMatchingAlgorithm(new HomogeneousRationingAlgorithm())
           );
        
        firm1 = new MyFirm(100, "1", 2, 50, 1, goodsMarket);
        firm2 = new MyFirm(100, "2", 1, 50, 2, goodsMarket);
        household1 = new MyHousehold(goodsMarket);
        household2 = new MyHousehold(goodsMarket);
    }

    @AfterMethod
    public void tearDown() {
        simState.finish();
        System.out.println("Test: " + this.getClass().getSimpleName() + " ends.");
    }

    @Test
    public void verifyAsynchronousMarket() {
        System.out.println(this.getClass().getSimpleName() + 
            ".verifyAsynchronousMarket..");
        do {
            if (!simState.schedule.step(simState)) {
                break;
            }
        } while (Simulation.getSimState().schedule.getSteps() < 1);
        
        SimpleGoodsInstrument
            firstInstrument = null,
            secondInstrument = null;
        
        goodsMarket.addInstrument("1");
        goodsMarket.addInstrument("2");
        
        goodsMarket.getInstrument("1").registerSeller(firm1);
        goodsMarket.getInstrument("2").registerSeller(firm2);
        
        try {
            
            // Goods type 1
            
            // 21 units of supply
            
            goodsMarket.addOrder(firm1, "1", 5, 1);
            goodsMarket.addOrder(firm1, "1", 5, 2);
            goodsMarket.addOrder(firm1, "1", 5, 3);
            goodsMarket.addOrder(firm1, "1", 6, 4);
            
            // Goods type 2
            
            // 14 units of supply
            
            goodsMarket.addOrder(firm2, "2", 4, 10);
            goodsMarket.addOrder(firm2, "2", 3, 13);
            goodsMarket.addOrder(firm2, "2", 4, 8);
            goodsMarket.addOrder(firm2, "2", 3, 15);
            
            firstInstrument = goodsMarket.getGoodsInstrument("1");
            secondInstrument = goodsMarket.getGoodsInstrument("2");
            
            {
            double worstAskPrice = firstInstrument.getWorstAskPriceAmongSellers();
            
            // 21 units of demand
            
            goodsMarket.addOrder(household1, "1", -4, worstAskPrice);
            goodsMarket.addOrder(household1, "1", -4, worstAskPrice);
            goodsMarket.addOrder(household2, "1", -8, worstAskPrice);
            goodsMarket.addOrder(household2, "1", -5, worstAskPrice);
            }
            
            {
            double worstAskPrice = secondInstrument.getWorstAskPriceAmongSellers();
            
            // 14 units of demand
            
            goodsMarket.addOrder(household1, "2", -4, worstAskPrice);
            goodsMarket.addOrder(household1, "2", -3, worstAskPrice);
            goodsMarket.addOrder(household2, "2", -6, worstAskPrice);
            goodsMarket.addOrder(household2, "2", -1, worstAskPrice);
            }
        } catch (Exception e) {
            Assert.fail();
        }
        
        firstInstrument.matchOrders();
        secondInstrument.matchOrders();
        
        // Goods market matching procedures are subject to change, 
        // so we merely check that the above market has cleared.
        
        // Check trading quantities
        
        {
        List<Double>
           tradedVolumeSeries = new ArrayList<Double>(firstInstrument.getTradingVolumes());
        AssertJUnit.assertEquals(
            21.0, tradedVolumeSeries.get(tradedVolumeSeries.size() - 1), E);
        tradedVolumeSeries = new ArrayList<Double>(secondInstrument.getTradingVolumes());
        AssertJUnit.assertEquals(
            14.0, tradedVolumeSeries.get(tradedVolumeSeries.size() - 1), E);
        }
        
        // Check exchange of goods and money
        
        AssertJUnit.assertEquals(
            household1.getGoodsRepository().getStoredQuantity("1"), 8., E);
        AssertJUnit.assertEquals(
            household1.getGoodsRepository().getStoredQuantity("2"), 7., E);
        
        AssertJUnit.assertEquals(
            household2.getGoodsRepository().getStoredQuantity("1"), 13., E);
        AssertJUnit.assertEquals(
            household2.getGoodsRepository().getStoredQuantity("2"), 7., E);
    }
    
    public static void main(String[] args) {
        SimpleGoodsMarketMatchingTest test = new SimpleGoodsMarketMatchingTest();
        test.setUp();
        test.verifyAsynchronousMarket();
        test.tearDown();
    }
}
