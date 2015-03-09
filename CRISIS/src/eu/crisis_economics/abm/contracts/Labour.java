/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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

import java.util.ArrayDeque;
import java.util.Queue;

import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.abm.events.WagePaymentEvent;
import eu.crisis_economics.abm.simulation.CustomSimulationCycleOrdering;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.Simulation.ScheduledMethodInvokation;

public class Labour implements Comparable<Labour> {
    
    private Settlement settlement;
    private Employer employer;
    private Employee employee;
    private int 
        duration,
        instanceUniqueIdentifier;
    private static int
        instancesInExistence;
    private double
        wage,
        quantity,
        creationTime;
    private long
        initialStep;
    private boolean active; 
    private ScheduledMethodInvokation
       scheduleEvent;
    
    /** An object pool of Labour type instances. */
    private enum ObjectPool { // Singleton, Object Pool
        Instance (50);
        
        private Queue<Labour> 
            instancesInUse,
            instancesNotInUse,
            returningInstances0,
            returningInstances1;
        private ObjectPool(int initialPoolPopulation) {
            if(initialPoolPopulation <= 0)
                throw new IllegalArgumentException(
                    "Labour.ObjectPool: initial pool population is " + 
                    "zero or negative.");
            this.instancesInUse = new ArrayDeque<Labour>();
            this.instancesNotInUse = new ArrayDeque<Labour>();
            this.returningInstances0 = new ArrayDeque<Labour>();
            this.returningInstances1 = new ArrayDeque<Labour>();
            for(int i = 0; i< initialPoolPopulation; ++i)
               instancesNotInUse.add(new Labour());
            Simulation.repeat(
               this, "recycleReturnedInstances",
               CustomSimulationCycleOrdering.create(NamedEventOrderings.BEFORE_ALL, 2)
               );
        }
        @SuppressWarnings("unused") // Scheduled
        private void recycleReturnedInstances() {
           this.instancesNotInUse.addAll(returningInstances0);
           returningInstances0.clear();
           returningInstances0.addAll(returningInstances1);
           returningInstances1.clear();;
        }
        /** Release an object form the pool. */
        private Labour release() {
            if(instancesNotInUse.isEmpty())
                this.expandPool();
            Labour unusedInstance = instancesNotInUse.poll();
            instancesInUse.add(unusedInstance);
            return unusedInstance;
        }
        /** Allow the pool to reclaim an instance. */
        private void reclaim(Labour instance) {
            instance.reset();
            instancesInUse.remove(instance);
            returningInstances1.add(instance);
        }
        // Expand the pool.
        private void expandPool() {
            int instancesToCreate = (int)(.5 * instancesInUse.size() + 1);
            for(int i = 0; i< instancesToCreate; ++i)
                instancesNotInUse.add(new Labour());
        }
    }
    
    /** Return an instance of Labour. */
    public static Labour create(
        Employer employer,      // The employer
        Employee employee,      // The employee
        double quantity,        // Total units of labour
        int duration,           // Contract maturity
        double wage             // Total employee remuneration
        ) throws DoubleEmploymentException {
        Labour newInstance = Labour.ObjectPool.Instance.release();
        try {
            employee.disallocateLabour(quantity);
            final double
               maximumEmployerExpense = wage * quantity,
               employerCanAfford = employer.getUnallocatedCash(),
               maximumTransaction = Math.min(employerCanAfford, maximumEmployerExpense),
               quantityRation = maximumTransaction / maximumEmployerExpense;
            newInstance.settlement = SettlementFactory.createLabourSettlement(employer, employee);
            newInstance.settlement.transfer(maximumTransaction);
            
            Simulation.events().post(new WagePaymentEvent(employer, employee, maximumTransaction));
            
            newInstance.employer = employer;
            newInstance.employee = employee;
            quantity *= quantityRation;
            newInstance.quantity = quantity;
            newInstance.duration = duration;
            newInstance.wage = wage;
            newInstance.initialStep = Simulation.getCycleIndex();
            newInstance.creationTime = Simulation.getTime();
        } catch (final Exception failedTransaction) {
            newInstance.reset();
            Labour.ObjectPool.Instance.reclaim(newInstance);
            return null;
        }
        newInstance.employee.startContract(newInstance);
        newInstance.employer.addLabour(newInstance);
        employee.notifyOutgoingEmployment(quantity, wage);
        employer.registerIncomingEmployment(quantity, wage);
        newInstance.scheduleSelf();
        newInstance.active = true;
        return newInstance;
    }
    
    private Labour() { // Empty instance 
        this.reset();
        this.instanceUniqueIdentifier = (++Labour.instancesInExistence);
        this.scheduleEvent = null;
    }
    
    private void reset() { // Flush state
        employee    = null;
        employer    = null;
        duration    = 0;
        wage        = 0.;
        creationTime = -1.;
        quantity    = 0.;
        active      = false;
        initialStep = 0L;
    }
    
    private void scheduleSelf() {
       Simulation.once(this, "step", NamedEventOrderings.BEFORE_ALL);
       if(scheduleEvent == null)
          scheduleEvent = Simulation.once(this, "payWage", NamedEventOrderings.HIRE_WORKERS);
       else 
          Simulation.enqueue(scheduleEvent);
    }
    
    @SuppressWarnings("unused")
    private void step() {
       if (Simulation.getCycleIndex() < initialStep+duration)
          Simulation.once(this, "step", NamedEventOrderings.BEFORE_ALL);
       else
          finishedWith();
    }
    
    /** Call when the instance is no longer required. */
    private void finishedWith() {
        boolean result = employer.removeLabour(this);
        if(!result)
           throw new IllegalStateException();
        try {
            employee.endContract(this);
        } catch (Exception e) {
            throw new RuntimeException(
                "Labour.removeLabour: an exception was thrown " + 
                "disallocating an instance of Labour from the " + 
                "employee. Details follow.\n" + e.getMessage()); 
        }
        Labour.ObjectPool.Instance.reclaim(this);
    }
    
    
    public void payWage(){
        if(!active) return;
        try {
           final double
              wageToPay = wage * quantity;
           settlement.transfer(wageToPay);
           
           Simulation.events().post(new WagePaymentEvent(employer, employee, wageToPay));
           
           Simulation.enqueue(scheduleEvent);
        } catch (final InsufficientFundsException e) {
            /**
              * XXX
              * 
              * Something should surely happen here.
              */
        }
    }
    
    
    public double getWage() {
        return wage;
    }
    
    public double getQuantity() {
        return quantity;
    }
    
    public Employer getEmployer() {
        return employer;
    }

    public Employee getEmployee() {
        return employee;
    }
    
    public double getCreationTime() {
       return creationTime;
    }

    public void fire() {
       // the fire function eliminates this contract from both employer and employee
       this.employer.removeLabour(this);
       this.employee.endContract(this);
    }

    @Override
    public int compareTo(Labour otherInstance) {
        return (this.instanceUniqueIdentifier - otherInstance.instanceUniqueIdentifier);
    }
}