/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 Olaf Bochmann
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Jakob Grazzini
 * Copyright (C) 2015 Alessandro Gobbi
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
package eu.crisis_economics.abm.contracts;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sim.util.Bag;
import eu.crisis_economics.abm.contracts.DoubleEmploymentException;
import eu.crisis_economics.abm.firm.ExogenousEmployee;
import eu.crisis_economics.abm.inventory.Inventory;
import eu.crisis_economics.abm.inventory.SimpleResourceInventory;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author olaf
 */
public class LabourTest {
    private MyEmployer employer;
    private ExogenousEmployee employee;
    private Simulation simState;
    
    private class MyEmployer implements Employer {
        
        public Inventory cashLedger;
        private Bag labourList;
        private String uniqueName;
        
        public MyEmployer(double funds) {
            this.cashLedger = new SimpleResourceInventory(funds);
            this.labourList = new Bag();
            this.uniqueName = java.util.UUID.randomUUID().toString();
        }
        
        @Override
        public double credit(final double amt)
                throws InsufficientFundsException {
            try {
                if (cashLedger.getStoredQuantity() < amt) {
                    throw new InsufficientFundsException("Insufficient Funds!");
                }
                cashLedger.pull(amt);
                return (amt);
            } finally { }
        }
        
        @Override
        public void debit(final double amt) {
           cashLedger.push(amt);
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
            return this.labourList.size();
        }
        
        @Override
        public void disallocateLabour(double size) { }
        
        @Override
        public void cashFlowInjection(double amt) { }
        
        @Override
        public double allocateCash(double positiveAmount) {
           return cashLedger.changeAllocatedBy(positiveAmount);
        }
        
        @Override
        public double getAllocatedCash() {
           return cashLedger.getAllocated();
        }
        
        @Override
        public double getUnallocatedCash() {
           return cashLedger.getUnallocated();
        }
        
        @Override
        public double disallocateCash(double d) {
           return -cashLedger.changeAllocatedBy(-d);
        }
        
        @Override
        public void registerIncomingEmployment(
           double labourEmployed, double unitLabourWage) {
        }
        
        @Override
        public String getUniqueName() {
           return uniqueName; 
        }
    }
    
    @BeforeMethod
    public void setUp() {
        System.out.println("Test: " + this.getClass().getSimpleName() + " begins.");
        this.simState = new EmptySimulation(System.currentTimeMillis());
        this.simState.start();
        employer = new MyEmployer(100.0);
        employee = new ExogenousEmployee(100.0);
    }
    
    @AfterMethod
    public void tearDown() {
        simState.finish();
        System.out.println("Test: " + this.getClass().getSimpleName()
                + " ends.");
    }
    
    /**
     * Test transfer of wages and employment. Test method for
     * {@link eu.crisis_economics.abm.contracts.Labour#step()}.
     */
    @Test
    public final void testWages() {
        System.out.println(this.getClass().getSimpleName() + ".testWages..");
        AssertJUnit.assertEquals(0., employer.getLabourForce());
        AssertJUnit.assertFalse(employee.getLabourAmountEmployed() > 0.);
        try {
            Labour.create(employer, employee, 1, 10, 1.0);
        } catch (DoubleEmploymentException e) {
            AssertJUnit.fail();
        }
        do {
            if (!simState.schedule.step(simState)) {
                break;
            }
            if (Simulation.getCycleIndex() < 10) {
               System.out.printf(
                  "time: %8.5g cycle: %2d employer funds: %8.3g employed: %5.2g\n",
                  Simulation.getTime(),
                  Simulation.getCycleIndex(),
                  employer.cashLedger.getStoredQuantity(),
                  employer.getLabourForce()
                  );
               /* AssertJUnit.assertEquals(
                  "in step " + simState.schedule.getSteps(), 1.,
                  employer.getLabourForce()); */
               // AssertJUnit.assertTrue(employee.getLabourAmountEmployed() > 0.);
            } else
                break;
        } while (true);
        AssertJUnit.assertEquals(90.0, employer.cashLedger.getStoredQuantity());
     // AssertJUnit.assertEquals(110.0, employee.getFunds());
        AssertJUnit.assertEquals(10, Simulation.getCycleIndex());
        AssertJUnit.assertEquals(0., employer.getLabourForce());
        AssertJUnit.assertFalse(employee.getLabourAmountEmployed() > 0.);
    }
    
    /**
     * Test double employment. Test method for
     * {@link eu.crisis_economics.abm.contracts.Employee#setEmployment()}.
     */
    @Test
    public final void testSetEmployment() {
        System.out.println(this.getClass().getSimpleName()
                + ".testSetEmployment..");
        AssertJUnit.assertEquals(0., employer.getLabourForce());
        AssertJUnit.assertFalse(employee.getLabourAmountEmployed() > 0.);
        try {
            Labour.create(employer, employee, 1, 10, 1.);
        } catch (DoubleEmploymentException e1) {
            AssertJUnit.fail();
        }
        AssertJUnit.assertEquals(1., employer.getLabourForce());
        AssertJUnit.assertTrue(employee.getLabourAmountEmployed() > 0.);
//        try {
//            Labour.create(employer, employee, 1, 10, 1.);
//            AssertJUnit.fail();
//        } catch (DoubleEmploymentException e) {
//            // do nothing
//        }
        AssertJUnit.assertEquals(1., employer.getLabourForce());
        AssertJUnit.assertTrue(employee.getLabourAmountEmployed() > 0.);
    }
    
    static public void main(String[] args) {
        LabourTest test = new LabourTest();
        test.setUp();
        test.testWages();
        test.testSetEmployment();
        test.tearDown();
    }
}
