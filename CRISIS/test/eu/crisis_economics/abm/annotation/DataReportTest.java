/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
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
package eu.crisis_economics.abm.annotation;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import sim.engine.SimState;
import sim.engine.Steppable;

import com.sun.tools.attach.VirtualMachine;

import eu.crisis_economics.abm.annotation.Collect.ChangeType;
import eu.crisis_economics.abm.simulation.EmptySimulation;
import eu.crisis_economics.abm.simulation.Simulation;

@DataReporter
class SimpleValueProvider {
    @Report("intValue")
    int intValue = 1;
    
    public void setValue(int intValue) {
        this.intValue = intValue;
    }
    
    @Report("intValue")
    public int constant20() {
        intValue = 20;
        return 20;
    }
}

@DataCollector
class SimpleValueCollector {
    @Collect("intValue")
    int intValue = 0;
    
    @Collect("intValue")
    List<Integer> intValues = new ArrayList<Integer>();
    
    @Collect("intValue")
    public void collectValue(
        @ReportKey String key,
        @ReportValue int value,
        @ReportSource SimpleValueProvider simpleValueProvider
        ) {
        Assert.assertEquals(value, simpleValueProvider.intValue);
    }
}

@DataReporter
class CollectionProvider {
    @Report("intValue")
    List<Integer> intList = new ArrayList<Integer>();
    
    public void addValue(final int intValue) {
        this.intList.add(intValue);
    }
    
    public void removeValue(final int index) {
        intList.remove(index);
    }
    
    public int getLastValue() {
        return intList.get(intList.size() - 1);
    }
    
    public boolean containsValue(final int intValue) {
        return intList.contains(intValue);
    }
}

@DataCollector
class CollectionCollector {
    
    boolean
        addCollected = false,
        removeCollected = false;
    
    @Collect("intValue")
    int intValue = 0;
    
    @Collect("intValue")
    List<Integer> intValues = new ArrayList<Integer>();
    
    @Collect(value = "intValue", changeType = ChangeType.COLLECTION_ADD)
    int addedValue = 0;
    
    @Collect(value = "intValue", changeType = ChangeType.COLLECTION_ADD)
    List<Integer> addedValues = new ArrayList<Integer>();
    
    @Collect(value = "intValue", changeType = ChangeType.COLLECTION_REMOVE)
    int removedValue = 0;
    
    @Collect(value = "intValue", changeType = ChangeType.COLLECTION_REMOVE)
    List<Integer> removedValues = new ArrayList<Integer>();
    
    @Collect(value = "intValue", changeType = ChangeType.COLLECTION_ADD)
    public void collectValueAdded(@ReportKey String key,
            @ReportValue int value,
            @ReportSource CollectionProvider collectionProvider) {
        Assert.assertEquals(value, collectionProvider.getLastValue());
        addCollected = true;
    }
    
    @Collect(value = "intValue", changeType = ChangeType.COLLECTION_REMOVE)
    public void collectValueRemoved(@ReportValue int value,
            @ReportSource CollectionProvider collectionProvider) {
        Assert.assertFalse(collectionProvider.containsValue(value));
        removeCollected = true;
    }
    
    public boolean isAddCollected() {
        return addCollected;
    }
    
    public boolean isRemoveCollected() {
        return removeCollected;
    }
}

@DataReporter
class ScheduledProvider {
    @Report(value = "fieldValue", interval = 1, order = 10)
    int intValue = 0;
    
    public ScheduledProvider() {
        Simulation.getSimState().schedule.scheduleRepeating(
            new Steppable() {
                private static final long serialVersionUID = 6311027045440510180L;
                
                @Override
                public void step(SimState state) {
                    intValue = (int) Simulation.getFloorTime();
                }
            }, 
            9, 2);
    }
    
    @Report(value = "methodValue", interval = 3, order = 10)
    public int intMethod() {
        return (int) Simulation.getFloorTime();
    }
}

@DataCollector
class ScheduledCollector {
    @Collect("fieldValue")
    List<Integer> fieldValues = new ArrayList<Integer>();
    @Collect("methodValue")
    List<Integer> methodValues = new ArrayList<Integer>();
}

public class DataReportTest {

    @BeforeClass
    public void setupAspectJ() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(
                    "lib/javaagent-0.0.1-SNAPSHOT-jar-with-dependencies.jar",
                    "");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // @Test
    public void simpleValueTest() {
        SimpleValueCollector simpleValueCollector = new SimpleValueCollector();
        
        SimpleValueProvider simpleValueProvider = new SimpleValueProvider();
        
        Assert.assertEquals(simpleValueCollector.intValue,
                simpleValueProvider.intValue);
        
        simpleValueProvider.setValue(2);
        
        Assert.assertEquals(simpleValueCollector.intValue,
                simpleValueProvider.intValue);
        
        // Note that provider.intValue = 3 would not work, as aspectJ
        // cannot catch field-set join points that are not in the context
        // of the class!
        
        Assert.assertEquals(simpleValueCollector.intValues, Arrays.asList(1, 2));
        
        simpleValueProvider.constant20();
        
        Assert.assertEquals(simpleValueCollector.intValue,
           simpleValueProvider.intValue);
        
        // the constant20 method assigns and returns the value 20, and 
        // both are reported!
        Assert.assertEquals(simpleValueCollector.intValues,
           Arrays.asList(1, 2, 20, 20));
    }

    // @Test
    public void collectionTest() {
        CollectionCollector collector = new CollectionCollector();
        
        CollectionProvider provider = new CollectionProvider();
        
        provider.addValue(10);
        
        Assert.assertTrue(collector.isAddCollected());
        Assert.assertEquals(collector.addedValues, Arrays.asList(10));
        Assert.assertTrue(collector.removedValues.isEmpty());
        Assert.assertEquals(collector.intValues, Arrays.asList(10));
        Assert.assertEquals(collector.intValue, 10);
        Assert.assertEquals(collector.addedValue, 10);
        Assert.assertNotEquals(collector.removedValue, 10);
        collector.addCollected = false;
        
        provider.addValue(11);
        Assert.assertTrue(collector.isAddCollected());
        Assert.assertEquals(collector.addedValues, Arrays.asList(10, 11));
        Assert.assertTrue(collector.removedValues.isEmpty());
        Assert.assertEquals(collector.intValues, Arrays.asList(10, 11));
        Assert.assertEquals(collector.intValue, 11);
        Assert.assertEquals(collector.addedValue, 11);
        Assert.assertNotEquals(collector.removedValue, 11);
        
        provider.removeValue(0);
        Assert.assertTrue(collector.isRemoveCollected());
        Assert.assertEquals(collector.addedValues, Arrays.asList(10, 11));
        Assert.assertEquals(collector.removedValues, Arrays.asList(10));
        Assert.assertEquals(collector.intValues, Arrays.asList(10, 11, 10));
        Assert.assertEquals(collector.intValue, 10);
        Assert.assertEquals(collector.addedValue, 11);
        Assert.assertEquals(collector.removedValue, 10);
    }
    
    // @Test
    public void scheduleTest() {
        Simulation simulation = new EmptySimulation(1L);
        
        final ScheduledCollector collector = new ScheduledCollector();
        
        final List<Integer> expectedFieldValues = new ArrayList<Integer>();
        
        final List<Integer> expectedMethodValues = new ArrayList<Integer>();
        
        Assert.assertEquals(collector.fieldValues.size(), 0);
        Assert.assertEquals(collector.methodValues.size(), 0);

        Simulation.getSimState().schedule.scheduleRepeating(new Steppable() {
            private static final long serialVersionUID = -6231445023936207877L;
            @Override
            public void step(SimState state) {
                Assert.assertEquals(collector.fieldValues, expectedFieldValues);
                
                Assert.assertEquals(collector.methodValues,
                        expectedMethodValues);
            }
        }, 5, 1);
        
        Simulation.getSimState().schedule.scheduleRepeating(new Steppable() {
            private static final long serialVersionUID = -3010325503761818913L;
            @Override
            public void step(SimState state) {
                if (state.schedule.getTime() % 2 != 0) {
                    expectedFieldValues.add((int) state.schedule.getTime());
                } else {
                    if (expectedFieldValues.size() == 0) {
                        expectedFieldValues.add(0);
                    } else {
                        expectedFieldValues.add(expectedFieldValues
                                .get(expectedFieldValues.size() - 1));
                    }
                }
                
                Assert.assertEquals(collector.fieldValues, expectedFieldValues);
                
                if (state.schedule.getTime() % 3 == 2) {
                    if (state.schedule.getTime() != 0) {
                        expectedMethodValues
                                .add((int) state.schedule.getTime());
                    }
                }
                
                Assert.assertEquals(collector.methodValues,
                        expectedMethodValues);
            }
        }, 15, 1);
        
        simulation.start();
        for (int i = 0; i < 10; ++i)
            simulation.schedule.step(simulation);
    }
}
