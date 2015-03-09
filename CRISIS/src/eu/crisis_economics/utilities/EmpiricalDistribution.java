/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Victor Spirin
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
package eu.crisis_economics.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.testng.Assert;

import ec.util.MersenneTwisterFast;
import eu.crisis_economics.abm.simulation.Simulation;

/**
 * @author      JKP
 * @category    Utilities
 * @see         
 * @since       1.0
 * @version     1.0
 */
public final class EmpiricalDistribution {
    
    private CircularFifoBuffer<Double> m_dataStream;
    
    private static class SortedDatum // Mutable 
        implements Comparable<SortedDatum> {
        double value;
        int numRecordsLessThan;
        
        SortedDatum(
            double datumValue, 
            int numRecordsLessThan
            ) {
            this.value = datumValue;
            this.numRecordsLessThan = numRecordsLessThan;
        }
        
        SortedDatum(double datumValue) {
            this(datumValue, -1);
        }
        
        @Override
        public boolean equals(Object other) {
            if(other instanceof SortedDatum)
                return ((SortedDatum) other).value == this.value;
            else return false;
        }
        
        @Override
        public int compareTo(SortedDatum other) {
            return Double.compare(this.value, other.value);
        }
        
        @Override
        public String toString() {
            return "value: " + this.value + 
                   ", less than: " + this.numRecordsLessThan;
        }
    }
    
    private TreeSet<SortedDatum> m_sortedData;
    
    private double 
       lastValueInserted,
       minRecordValue,
       maxRecordValue;
    
    public EmpiricalDistribution(int memoryLength) {
        m_dataStream = new CircularFifoBuffer<Double>(memoryLength);
        m_sortedData = new TreeSet<EmpiricalDistribution.SortedDatum>();
        lastValueInserted = Double.NaN;
    }
    
    /** Add a new record */
    public void add(double value) {
        if(m_dataStream.maxSize() == m_dataStream.size()) {
            double valueToRemove = m_dataStream.remove();
            SortedDatum datumToRemove =
                new SortedDatum(valueToRemove);
            m_sortedData.remove(datumToRemove);
            // if(!result) { 
            //    throw new AssertionError(); 
            // }
            this.reconcileDeletion(valueToRemove);
        }
        m_dataStream.add(value);
        SortedDatum
            newSortedRecord = new SortedDatum(value),
            floorRecord = m_sortedData.floor(newSortedRecord);
        if(floorRecord == null) {
            newSortedRecord.numRecordsLessThan = 0;
        } else {
            newSortedRecord.numRecordsLessThan =
                floorRecord.numRecordsLessThan;
            if(newSortedRecord.value != floorRecord.value)
                newSortedRecord.numRecordsLessThan++;
        }
        m_sortedData.add(newSortedRecord);
        this.reconcileInsertion(value);
        lastValueInserted = value;
    }
    
    public void flush() {
        m_dataStream.clear();
        m_sortedData.clear();
        lastValueInserted = Double.NaN;
        maxRecordValue = Double.NaN;
        minRecordValue = Double.NaN;
    }
    
    // Reconcile sorted data after a deletion
    private void reconcileDeletion(double valueDeleted) {
        maxRecordValue = -Double.MAX_VALUE;
        minRecordValue = Double.MAX_VALUE;
        Iterator<SortedDatum> it = m_sortedData.iterator();
        while(it.hasNext()) {
            SortedDatum record = it.next();
            maxRecordValue = Math.max(record.value, maxRecordValue);
            minRecordValue = Math.min(record.value, minRecordValue);
            if(record.value > valueDeleted) 
                record.numRecordsLessThan--;
        }
    }
    
    // Reconcile sorted data after an insertion
    private void reconcileInsertion(double insertionValue) {
        maxRecordValue = -Double.MAX_VALUE;
        minRecordValue = Double.MAX_VALUE;
        Iterator<SortedDatum> it = m_sortedData.iterator();
        while(it.hasNext()) {
            SortedDatum record = it.next();
            maxRecordValue = Math.max(record.value, maxRecordValue);
            minRecordValue = Math.min(record.value, minRecordValue);
            if(record.value > insertionValue) 
                record.numRecordsLessThan++;
        }
    }
    
    /** Get the last recorded value */
    public double getLastAdded() {
        if(lastValueInserted == Double.NaN)
            throw new IllegalStateException(
               "EmpiricalDistribution.getLastAdded: no records.");
        return lastValueInserted;
    }
    
    /** Get the minimum recoded value (range minimum). */
    public double getMaxRecordedValue() {
        if(lastValueInserted == Double.NaN)
            throw new IllegalStateException(
               "EmpiricalDistribution.getMaxRecordedValue: no records.");
        return maxRecordValue;
    }
    
    /** Get the maximum recorded value (range maximum). */
    public double getMinRecordedValue() {
        if(lastValueInserted == Double.NaN)
            throw new IllegalStateException(
               "EmpiricalDistribution.getMinRecordedValue: no records.");
        return minRecordValue;
    }
    
    /** Get the mean value over recorded history. */
    public double meanOverHistory() {
        double result = 0;
        for(double d : m_dataStream)
            result += d;
        result /= m_dataStream.size();
        return result;
    }
    
    /** Empirically estimate the probability 
     *  P(data >= queryValue) based on recoded data 
     *  history.*/
    public double estimateProbabilityNotLessThan(double queryValue) {
        return (double)this.numRecordsNotLessThan(queryValue) / 
               (double)m_dataStream.size();
    }
    
    /** Get the number of records not less than 
     *  (greater than or equal to) the query value.*/
    public int numRecordsNotLessThan(double queryValue) {
        SortedDatum
            queryDatum = new SortedDatum(queryValue),
            ceilingDatum = m_sortedData.ceiling(queryDatum);
        if(ceilingDatum == null) return 0;
        return (m_dataStream.size() - ceilingDatum.numRecordsLessThan);
    }
    
    /** Get the number of records less than the query value. */
    public int numRecordsLessThan(double queryValue) {
        return (m_dataStream.size() - this.numRecordsNotLessThan(queryValue));
    }
    
    /** Get the number of records stored. */
    public int size() { return m_dataStream.size(); }
    
    /** Get the maximum number of records that 
     *  can be stored in this memory. */
    public int maxSize() { return m_dataStream.maxSize(); } 
    
    /** Get a copy of the record history. */
    public Double[] toArray() {
        return m_dataStream.toArray(new Double[0]);
    }
    
    /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, however the following is typical:
     * 
     * if empty: 'empty empirical distribution'
     * otherwise: 'empirical distribution, records: [1, 2, 3, 4]'
     */
    @Override
    public String toString() {
        if(m_dataStream.size() == 0)
            return "empty empirical distribution";
        else { 
            String result = "empirical distribution, records: [";
            Iterator<Double> it = m_dataStream.iterator();
            while(true) {
                double record = it.next();
                result += Double.toString(record);
                if(!it.hasNext()) break;
                result += ", ";
            }
            return result + ']';
        }
    }
    
    //TODO: migrate to /test/
    public static void main(String[] args) {
        System.out.println("testing EmpiricalDistribution type..");
        
        {
            System.out.println("EmpiricalDistribution test 1..");
            EmpiricalDistribution memory = new EmpiricalDistribution(10);
            for(int i = 0; i< 10; ++i) {
                memory.add(i);
                Assert.assertEquals(memory.size(), i+1);
                Assert.assertEquals(memory.maxSize(), 10);
                Assert.assertEquals(memory.getMaxRecordedValue(), (double)i);
                Assert.assertEquals(memory.getMinRecordedValue(), 0.);
            }
            Assert.assertEquals(4.5, memory.meanOverHistory(), 1.e-12);
            for(int i = 0; i< 10; ++i) {
                Assert.assertEquals(i, memory.numRecordsLessThan(i));
                Assert.assertEquals(i, memory.numRecordsNotLessThan(10-i));
                Assert.assertEquals(1-i/10., memory.estimateProbabilityNotLessThan(i), 1.e-12);
            }
            for(int i = 0; i< 10; ++i) { // re-write memory
                memory.add(i);
                Assert.assertEquals(memory.size(), 10);
            }
            Assert.assertEquals(4.5, memory.meanOverHistory(), 1.e-12);
            for(int i = 0; i< 10; ++i) {
                Assert.assertEquals(i, memory.numRecordsLessThan(i));
                Assert.assertEquals(i, memory.numRecordsNotLessThan(10-i));
                Assert.assertEquals(1-i/10., memory.estimateProbabilityNotLessThan(i), 1.e-12);
            }
            for(int i = 0; i< 10; ++i) { // re-write memory
                memory.add(i*10);
                Assert.assertEquals(memory.size(), 10);
                Assert.assertEquals(memory.getMaxRecordedValue(), i == 0 ? 9. : (double)10*i);
                Assert.assertEquals(memory.getMinRecordedValue(), 0.);
            }
            Assert.assertEquals(4.5*10, memory.meanOverHistory(), 1.e-12);
            for(int i = 0; i< 100; ++i) {
                int roundDown = (i+9)/10;
                Assert.assertEquals(roundDown, memory.numRecordsLessThan(i));
                Assert.assertEquals(roundDown, memory.size() - memory.numRecordsNotLessThan(i));
                Assert.assertEquals(1-roundDown/10., memory.estimateProbabilityNotLessThan(i), 1.e-12);
            }
            memory.flush();
            for(int i = 0; i< 10; ++i) {
                Assert.assertEquals(memory.size(), i);
                memory.add(0);
                Assert.assertEquals(0, memory.numRecordsLessThan(0));
                Assert.assertEquals(i+1, memory.numRecordsLessThan(1));
                Assert.assertEquals(memory.getMaxRecordedValue(), 0.);
                Assert.assertEquals(memory.getMinRecordedValue(), 0.);
            }
            System.out.println(memory);
        }
        
        {
            MersenneTwisterFast random = Simulation.getSimState().random;
            for(int i = 0; i< 100; ++i) {
                if(i % 10 == 0)
                    System.out.println("EmpiricalDistribution test " + (i + 1) + "..");
                int memorySize = random.nextInt(100) + 1;
                EmpiricalDistribution memory = 
                    new EmpiricalDistribution(memorySize);
                double queryValue = random.nextDouble();
                ArrayList<Double> 
                    lessThanQueryValue = new ArrayList<Double>(),
                    notLessThanQueryValue = new ArrayList<Double>();
                for(int j = 0; j< 2 * memorySize; ++j) {
                    double insertValue = random.nextDouble();
                    memory.add(insertValue);
                    if(j >= memorySize) {
                        if(insertValue < queryValue)
                            lessThanQueryValue.add(insertValue);
                        else
                            notLessThanQueryValue.add(insertValue);
                    }
                }
                Assert.assertEquals(
                    memory.numRecordsLessThan(queryValue),
                    lessThanQueryValue.size()
                    );
                Assert.assertEquals(
                    memory.numRecordsNotLessThan(queryValue),
                    notLessThanQueryValue.size()
                    );
                Assert.assertEquals(
                    memory.estimateProbabilityNotLessThan(queryValue),
                    notLessThanQueryValue.size()/(double)memorySize
                    );
                System.out.println(memory);
                System.out.flush();
            }
        }
        
        System.out.println("EmpiricalDistribution tests pass");
        System.out.flush();
    }
}
