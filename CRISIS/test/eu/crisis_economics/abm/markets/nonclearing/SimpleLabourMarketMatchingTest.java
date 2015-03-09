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

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.algorithms.matching.ForagerMatchingAlgorithm;
import eu.crisis_economics.abm.algorithms.matching.HomogeneousRationingAlgorithm;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.inventory.SimpleResourceInventory;
import eu.crisis_economics.abm.markets.Buyer;
import eu.crisis_economics.abm.markets.Party;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.nonclearing.Order;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author jakob
 */
public class SimpleLabourMarketMatchingTest {

    private static final int DefaultMaturity = 3;

    private class MyFirm extends Agent implements Steppable, Employer, Party, Buyer {
        private static final long serialVersionUID = 798787376635372174L;
        
        private double initialCash;
        private SimpleLabourMarket labourMarket;
        private Bag orderList;
        private Bag labourList;
        
        private SimpleResourceInventory cashLedger;
        
        public Bag getLabourList() {
            return labourList;
        }
        
        public MyFirm(double initialCash) {
            this.initialCash = initialCash;
            this.orderList = new Bag();
            this.labourList = new Bag();
            
            Simulation.getSimState().schedule.scheduleOnce(this);
            
            cashLedger = new SimpleResourceInventory(initialCash);
        }
        
        public void setLabourMarket(SimpleLabourMarket labourMarket) {
            this.labourMarket = labourMarket;
            
        }
        
        @Override
        public void step(SimState state) {
            System.out.println("firm step");
            try {
                labourMarket.addOrder(this, DefaultMaturity, -3, 1.5);
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
        public double credit(double cashAmount)
                throws InsufficientFundsException {
            try {
                cashLedger.pull(cashAmount);
            } catch (Exception fatalError) {
                System.out
                        .println("SimpleLabourMarketMatchingTest: test failed (exception thrown). Details follow:");
                fatalError.printStackTrace();
            }
            return cashAmount;
        }

        @Override
        public void debit(double cashAmount) {
            cashLedger.push(cashAmount);
        }

        @Override
        public void addLabour(Labour labour) {
            this.labourList.add(labour);
        }

        @Override
        public boolean removeLabour(Labour labour) {
            return this.labourList.remove(labour);
        }

        @Override
        public double getLabourForce() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void disallocateLabour(double size) {
            // TODO Auto-generated method stub

        }

        @Override
        public void cashFlowInjection(double amt) {
            // TODO Auto-generated method stub

        }

        public double getCash() {
            return cashLedger.getStoredQuantity();
        }

        @Override
        public void registerIncomingEmployment(
           double labourEmployed, double unitLabourWage) { }

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
            return cashLedger.getAllocated();
        }
        
        @Override
        public <T> T accept(final AgentOperation<T> operation) {
           return operation.operateOn(this);
        }

      @Override
      public double getUnallocatedCash() {
         return cashLedger.getUnallocated();
      }
    }
    
    private class MyHousehold extends Agent implements Steppable, Employee, Party {
        private static final long serialVersionUID = 8929224611187266751L;
        
        private SimpleLabourMarket labourMarket;
        private int labourEndowment;
        private double reserveWage;
        private Labour labour;
        private double cash;
        private Bag orderList;

        public MyHousehold() {
            this.labourEndowment = 1;
            this.reserveWage = 1.5;
            this.cash = 0;
            this.orderList = new Bag();
            Simulation.getSimState().schedule.scheduleOnce(this);
        }
        
        public void setLabourMarket(SimpleLabourMarket labourMarket) {
            this.labourMarket = labourMarket;
        }
        
        @Override
        public void step(SimState state) {
            System.out.println("household step");
            try {
                
                labourMarket.addOrder(this, DefaultMaturity,
                        this.labourEndowment, this.reserveWage);
                
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
            return this.orderList.remove(labour);
        }

        @Override
        public void updateState(Order order) { }

        @Override
        public double credit(double amt) throws InsufficientFundsException {
            if (cash < amt) {
                throw new InsufficientFundsException("Not enough cash to pay "
                        + amt);
            }
            this.cash -= amt;

            return amt;
        }

        @Override
        public void debit(double amt) {
            this.cash += amt;
            System.out.println("household " + this + " " + this.cash);

        }

        @Override
        public void startContract(Labour labour) {
            this.labour = labour;
        }

        @Override
        public double getLabourAmountEmployed() {
            return (this.labour != null) ? labour.getQuantity() : 0.;
        }

        public Labour getLabour() {
            return labour;
        }

        @Override
        public void allocateLabour(double amount) throws AllocationException {
            // TODO Auto-generated method stub
        }

        @Override
        public void disallocateLabour(double amount)
                throws IllegalArgumentException {
            // TODO Auto-generated method stub
        }

        @Override
        public void cashFlowInjection(double amt) {
            // TODO Auto-generated method stub
        }

        @Override
        public void notifyOutgoingEmployment(
           double labourEmployed, double wagePerUnitLabour) { }
        
        @Override
        public void notifyIncomingWageOrBenefit(double totalPayment) { }

      @Override
      public void endContract(Labour labour) { }
      
      @Override
      public <T> T accept(final AgentOperation<T> operation) {
         return operation.operateOn(this);
      }

      @Override
      public double getMaximumLabourSupply() {
         return labourEndowment;
      }
      
      @Override
      public double getLabourAmountUnemployed() {
         return getMaximumLabourSupply() - getLabourAmountEmployed();
      }
      
    } // end MyHousehold

    private MyFirm firm1;
 // private MyFirm firm2;
    private MyHousehold household1;
    private MyHousehold household2;
    private SimpleLabourMarket labourMarket;
    private Simulation simState;

    @BeforeMethod
    public void setUp() {
    	System.out.println("Test: " + this.getClass().getSimpleName() + " begins.");
        simState = new EmptySimulation(123L);
        simState.start();

        firm1 = new MyFirm(1000);
        // firm2 = new MyFirm(1000);
        household1 = new MyHousehold();
        household2 = new MyHousehold();
        labourMarket = 
            new SimpleLabourMarket(
               new ForagerMatchingAlgorithm(new HomogeneousRationingAlgorithm()));
        firm1.setLabourMarket(labourMarket);
        // firm2.setLabourMarket(labourMarket);
        household1.setLabourMarket(labourMarket);
        household2.setLabourMarket(labourMarket);
    }

    @AfterMethod
    public void tearDown() {
        simState.finish();
        System.out.println("Test: " + this.getClass().getSimpleName() + " ends.");
    }

    @Test
    public void verifyAsynchronousMarket() {
        System.out.println("in test");
        do {
            if (Simulation.getCycleIndex() == 1) {
                AssertJUnit.assertEquals(
                        "I am in time step"
                                + Simulation.getSimState().schedule.getSteps(),
                        2, firm1.getLabourList().size());
                AssertJUnit.assertNotNull(household1.getLabour());
                AssertJUnit.assertNotNull(household2.getLabour());
                System.out.println(household1.getLabour());

            }

            if (!simState.schedule.step(simState)) {
                break;
            }
        } while (Simulation.getCycleIndex() < 7);

        AssertJUnit.assertEquals(firm1.initialCash - household1.cash
                - household2.cash, firm1.getCash(), 0.0001);
        System.out.println(household2.cash);
        System.out.println(household2.labour);
    }
    
    static public void main(String[] args) {
        SimpleLabourMarketMatchingTest test = new SimpleLabourMarketMatchingTest();
        test.setUp();
        test.verifyAsynchronousMarket();
        test.tearDown();
    }
}
